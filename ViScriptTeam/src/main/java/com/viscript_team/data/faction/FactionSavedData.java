package com.viscript_team.data.faction;

import com.lowdragmc.lowdraglib2.Platform;
import com.viscript_team.ViScriptTeam;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_team.data.party.Party;
import com.viscript_team.data.party.PartyStandingStrategy;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.UUID;

public class FactionSavedData extends SavedData implements IPersistedSerializable {
    private static final String DATA_NAME = ViScriptTeam.MOD_ID + "_factions";

    @Persisted
    private final List<Faction> factions = new ArrayList<>();
    @Persisted
    private final List<EntityFactionEntry> entityFactions = new ArrayList<>();
    @Persisted
    private final List<PlayerFactionStandings> playerStandings = new ArrayList<>();
    @Persisted
    private final List<Party> parties = new ArrayList<>();

    public static FactionSavedData get(ServerLevel level) {
        // 阵营数据是整个存档共享的，所以统一挂在主世界 SavedData 上。
        return level.getServer().overworld().getDataStorage().computeIfAbsent(FactionSavedData::load, FactionSavedData::new, DATA_NAME);
    }

    public static FactionSavedData load(CompoundTag tag) {
        FactionSavedData data = new FactionSavedData();
        data.deserializeNBT(Platform.getFrozenRegistry(), tag);
        data.sanitize();
        return data;
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag tag) {
        // 游戏只负责 SavedData 外壳，具体字段序列化交给 LDLib2。
        tag.merge(serializeNBT(Platform.getFrozenRegistry()));
        return tag;
    }

    public Set<String> getFactionIds() {
        Set<String> ids = new HashSet<>();
        factions.forEach(faction -> ids.add(faction.getId()));
        return ids;
    }

    public List<EntityFactionEntry> getEntityFactionEntries() {
        return List.copyOf(entityFactions);
    }

    public Optional<Faction> getFaction(String factionId) {
        String id = Faction.normalizeId(factionId);
        return factions.stream().filter(faction -> faction.getId().equals(id)).findFirst();
    }

    public Faction getOrCreateFaction(String factionId) {
        String id = Faction.normalizeId(factionId);
        return getFaction(id).orElseGet(() -> {
            Faction faction = new Faction(id);
            factions.add(faction);
            setDirty();
            return faction;
        });
    }

    public boolean removeFaction(String factionId) {
        String id = Faction.normalizeId(factionId);
        if (!factions.removeIf(faction -> faction.getId().equals(id))) {
            return false;
        }
        entityFactions.removeIf(entry -> entry.getFactionId().equals(id));
        playerStandings.forEach(standings -> standings.getStandings().removeIf(standing -> standing.getFactionId().equals(id)));
        factions.forEach(faction -> faction.removeEnemyFaction(id));
        playerStandings.removeIf(PlayerFactionStandings::isEmpty);
        setDirty();
        return true;
    }

    public void setEntityFaction(UUID entityId, @Nullable String factionId) {
        if (factionId == null || factionId.isBlank()) {
            if (entityFactions.removeIf(entry -> entry.getEntityId().equals(entityId))) {
                setDirty();
            }
            return;
        }

        String id = Faction.normalizeId(factionId);
        getOrCreateFaction(id);
        Optional<EntityFactionEntry> entry = findEntityFactionEntry(entityId);
        if (entry.isPresent()) {
            if (!entry.get().getFactionId().equals(id)) {
                entry.get().setFactionId(id);
                setDirty();
            }
        } else {
            entityFactions.add(new EntityFactionEntry(entityId, id));
            setDirty();
        }
    }

    public Optional<String> getEntityFaction(UUID entityId) {
        return findEntityFactionEntry(entityId).map(EntityFactionEntry::getFactionId);
    }

    public int getPlayerStanding(UUID playerId, String factionId) {
        String id = Faction.normalizeId(factionId);
        Optional<PlayerFactionStandings> standings = findPlayerStandings(playerId);
        Optional<StandingEntry> standing = standings.flatMap(value -> value.findStanding(id));
        if (standing.isPresent()) {
            return standing.get().getPoints();
        }
        return getOrCreateFaction(id).getDefaultPoints();
    }

    public void setPlayerStanding(UUID playerId, String factionId, int points) {
        String id = Faction.normalizeId(factionId);
        Faction faction = getOrCreateFaction(id);
        Optional<PlayerFactionStandings> existingStandings = findPlayerStandings(playerId);
        Optional<StandingEntry> standing = existingStandings.flatMap(value -> value.findStanding(id));
        if (points == faction.getDefaultPoints()) {
            if (standing.isEmpty()) {
                return;
            }
            PlayerFactionStandings standings = existingStandings.orElseThrow();
            standings.getStandings().removeIf(entry -> entry.getFactionId().equals(id));
            if (standings.isEmpty()) {
                playerStandings.remove(standings);
            }
        } else if (standing.isPresent()) {
            if (standing.get().getPoints() == points) {
                return;
            }
            standing.get().setPoints(points);
        } else {
            PlayerFactionStandings standings = existingStandings.orElseGet(() -> {
                PlayerFactionStandings created = new PlayerFactionStandings(playerId);
                playerStandings.add(created);
                return created;
            });
            standings.getStandings().add(new StandingEntry(id, points));
        }
        setDirty();
    }

    public void addPlayerStanding(UUID playerId, String factionId, int delta) {
        setPlayerStanding(playerId, factionId, getPlayerStanding(playerId, factionId) + delta);
    }

    public FactionAttitude getPlayerAttitude(UUID playerId, String factionId) {
        Faction faction = getOrCreateFaction(factionId);
        return faction.attitudeForPoints(getPlayerStanding(playerId, faction.getId()));
    }

    public int getPlayerEffectiveStanding(UUID playerId, String factionId) {
        return getPlayerEffectiveStanding(playerId, factionId, PartyStandingStrategy.defaultStrategy());
    }

    public int getPlayerEffectiveStanding(UUID playerId, String factionId, PartyStandingStrategy strategy) {
        String id = Faction.normalizeId(factionId);
        Optional<Party> party = getPlayerParty(playerId);
        return party.map(value -> calculatePartyStanding(value, id, strategy))
                .orElseGet(() -> getPlayerStanding(playerId, id));
    }

    public FactionAttitude getPlayerEffectiveAttitude(UUID playerId, String factionId) {
        return getPlayerEffectiveAttitude(playerId, factionId, PartyStandingStrategy.defaultStrategy());
    }

    public FactionAttitude getPlayerEffectiveAttitude(UUID playerId, String factionId, PartyStandingStrategy strategy) {
        Faction faction = getOrCreateFaction(factionId);
        return faction.attitudeForPoints(getPlayerEffectiveStanding(playerId, faction.getId(), strategy));
    }

    public boolean addEnemyFaction(String factionId, String enemyFactionId) {
        Faction faction = getOrCreateFaction(factionId);
        getOrCreateFaction(enemyFactionId);
        boolean changed = faction.addEnemyFaction(enemyFactionId);
        if (changed) {
            setDirty();
        }
        return changed;
    }

    public boolean removeEnemyFaction(String factionId, String enemyFactionId) {
        Optional<Faction> faction = getFaction(factionId);
        if (faction.isEmpty()) {
            return false;
        }
        boolean changed = faction.get().removeEnemyFaction(enemyFactionId);
        if (changed) {
            setDirty();
        }
        return changed;
    }

    public boolean isEnemyFaction(String factionId, String targetFactionId) {
        return getFaction(factionId).map(faction -> faction.isEnemyFaction(targetFactionId)).orElse(false);
    }

    public void saveFaction(Faction faction) {
        factions.removeIf(existing -> existing.getId().equals(faction.getId()));
        factions.add(faction);
        setDirty();
    }

    public Set<String> getPartyIds() {
        Set<String> ids = new HashSet<>();
        parties.forEach(party -> ids.add(party.getId()));
        return ids;
    }

    public Optional<Party> getParty(String partyId) {
        String id = Party.normalizeId(partyId);
        return parties.stream().filter(party -> party.getId().equals(id)).findFirst();
    }

    public Optional<Party> getPlayerParty(UUID playerId) {
        return parties.stream().filter(party -> party.containsMember(playerId)).findFirst();
    }

    public OptionalInt getPartyEffectiveStanding(String partyId, String factionId) {
        return getPartyEffectiveStanding(partyId, factionId, PartyStandingStrategy.defaultStrategy());
    }

    public OptionalInt getPartyEffectiveStanding(String partyId, String factionId, PartyStandingStrategy strategy) {
        String id = Faction.normalizeId(factionId);
        return getParty(partyId)
                .map(party -> OptionalInt.of(calculatePartyStanding(party, id, strategy)))
                .orElseGet(OptionalInt::empty);
    }

    public Optional<FactionAttitude> getPartyEffectiveAttitude(String partyId, String factionId) {
        return getPartyEffectiveAttitude(partyId, factionId, PartyStandingStrategy.defaultStrategy());
    }

    public Optional<FactionAttitude> getPartyEffectiveAttitude(String partyId, String factionId, PartyStandingStrategy strategy) {
        OptionalInt standing = getPartyEffectiveStanding(partyId, factionId, strategy);
        if (standing.isEmpty()) {
            return Optional.empty();
        }
        Faction faction = getOrCreateFaction(factionId);
        return Optional.of(faction.attitudeForPoints(standing.getAsInt()));
    }

    public boolean createParty(String partyId, UUID leaderId) {
        return createParty(partyId, partyId, leaderId);
    }

    public boolean createParty(String partyId, String name, UUID leaderId) {
        String id = Party.normalizeId(partyId);
        if (!Party.isValidPlayer(leaderId) || getParty(id).isPresent()) {
            return false;
        }
        removePlayerFromParties(leaderId, null);
        clearPlayerInteractions(leaderId);
        parties.add(new Party(id, leaderId).setName(name));
        setDirty();
        return true;
    }

    public boolean removeParty(String partyId) {
        String id = Party.normalizeId(partyId);
        boolean changed = parties.removeIf(party -> party.getId().equals(id));
        if (changed) {
            setDirty();
        }
        return changed;
    }

    public boolean joinParty(UUID playerId, String partyId) {
        Optional<Party> party = getParty(partyId);
        if (party.isEmpty() || !Party.isValidPlayer(playerId) || party.get().containsMember(playerId)) {
            return false;
        }
        removePlayerFromParties(playerId, party.get().getId());
        clearPlayerInteractions(playerId);
        party.get().addMember(playerId);
        if (!party.get().hasValidLeader()) {
            party.get().setLeaderId(playerId);
        }
        setDirty();
        return true;
    }

    public boolean leaveParty(UUID playerId) {
        Optional<Party> party = getPlayerParty(playerId);
        if (party.isEmpty()) {
            return false;
        }
        removePlayerFromParty(party.get(), playerId);
        pruneEmptyParties();
        setDirty();
        return true;
    }

    public boolean setPartyLeader(String partyId, UUID leaderId) {
        Optional<Party> party = getParty(partyId);
        if (party.isEmpty() || !Party.isValidPlayer(leaderId)) {
            return false;
        }
        removePlayerFromParties(leaderId, party.get().getId());
        party.get().addMember(leaderId);
        if (leaderId.equals(party.get().getLeaderId())) {
            return false;
        }
        party.get().setLeaderId(leaderId);
        setDirty();
        return true;
    }

    public boolean setPartyFriendlyFire(String partyId, boolean friendlyFire) {
        Optional<Party> party = getParty(partyId);
        if (party.isEmpty() || party.get().isFriendlyFire() == friendlyFire) {
            return false;
        }
        party.get().setFriendlyFire(friendlyFire);
        setDirty();
        return true;
    }

    public boolean invitePlayer(String partyId, UUID playerId) {
        Optional<Party> party = getParty(partyId);
        if (party.isEmpty() || getPlayerParty(playerId).isPresent() || !party.get().invitePlayer(playerId)) {
            return false;
        }
        setDirty();
        return true;
    }

    public boolean removePartyInvitation(String partyId, UUID playerId) {
        Optional<Party> party = getParty(partyId);
        if (party.isEmpty() || !party.get().removeInvitation(playerId)) {
            return false;
        }
        setDirty();
        return true;
    }

    public boolean addPartyJoinRequest(String partyId, UUID playerId) {
        Optional<Party> party = getParty(partyId);
        if (party.isEmpty() || getPlayerParty(playerId).isPresent() || !party.get().addJoinRequest(playerId)) {
            return false;
        }
        setDirty();
        return true;
    }

    public boolean removePartyJoinRequest(String partyId, UUID playerId) {
        Optional<Party> party = getParty(partyId);
        if (party.isEmpty() || !party.get().removeJoinRequest(playerId)) {
            return false;
        }
        setDirty();
        return true;
    }

    public boolean isSameParty(UUID firstPlayerId, UUID secondPlayerId) {
        if (!Party.isValidPlayer(firstPlayerId) || !Party.isValidPlayer(secondPlayerId)) {
            return false;
        }
        Optional<Party> party = getPlayerParty(firstPlayerId);
        return party.filter(value -> value.containsMember(secondPlayerId)).isPresent();
    }

    public boolean canPartyMembersHurt(UUID attackerId, UUID targetId) {
        Optional<Party> party = getPlayerParty(attackerId);
        if (party.isEmpty() || !party.get().containsMember(targetId)) {
            return true;
        }
        return party.get().allowsFriendlyFire();
    }

    private int calculatePartyStanding(Party party, String factionId, PartyStandingStrategy strategy) {
        String id = Faction.normalizeId(factionId);
        PartyStandingStrategy resolvedStrategy = PartyStandingStrategy.orDefault(strategy);
        if (!party.hasMembers()) {
            return getOrCreateFaction(id).getDefaultPoints();
        }

        return switch (resolvedStrategy) {
            case MIN -> minPartyStanding(party, id);
            case AVERAGE -> averagePartyStanding(party, id);
            case LEADER -> leaderPartyStanding(party, id);
            case MAX -> maxPartyStanding(party, id);
        };
    }

    private int minPartyStanding(Party party, String factionId) {
        int defaultPoints = getOrCreateFaction(factionId).getDefaultPoints();
        int result = Integer.MAX_VALUE;
        boolean hasMember = false;
        for (UUID memberId : party.getMembers()) {
            if (!Party.isValidPlayer(memberId)) {
                continue;
            }
            result = Math.min(result, getPlayerStanding(memberId, factionId));
            hasMember = true;
        }
        return hasMember ? result : defaultPoints;
    }

    private int maxPartyStanding(Party party, String factionId) {
        int defaultPoints = getOrCreateFaction(factionId).getDefaultPoints();
        int result = Integer.MIN_VALUE;
        boolean hasMember = false;
        for (UUID memberId : party.getMembers()) {
            if (!Party.isValidPlayer(memberId)) {
                continue;
            }
            result = Math.max(result, getPlayerStanding(memberId, factionId));
            hasMember = true;
        }
        return hasMember ? result : defaultPoints;
    }

    private int averagePartyStanding(Party party, String factionId) {
        int defaultPoints = getOrCreateFaction(factionId).getDefaultPoints();
        long sum = 0L;
        int count = 0;
        for (UUID memberId : party.getMembers()) {
            if (!Party.isValidPlayer(memberId)) {
                continue;
            }
            sum += getPlayerStanding(memberId, factionId);
            count++;
        }
        return count == 0 ? defaultPoints : (int) Math.floorDiv(sum, count);
    }

    private int leaderPartyStanding(Party party, String factionId) {
        if (Party.isValidPlayer(party.getLeaderId()) && party.containsMember(party.getLeaderId())) {
            return getPlayerStanding(party.getLeaderId(), factionId);
        }
        return party.firstMember()
                .map(memberId -> getPlayerStanding(memberId, factionId))
                .orElseGet(() -> getOrCreateFaction(factionId).getDefaultPoints());
    }

    private Optional<EntityFactionEntry> findEntityFactionEntry(UUID entityId) {
        return entityFactions.stream().filter(entry -> entry.getEntityId().equals(entityId)).findFirst();
    }

    private Optional<PlayerFactionStandings> findPlayerStandings(UUID playerId) {
        return playerStandings.stream().filter(entry -> entry.getPlayerId().equals(playerId)).findFirst();
    }

    private void removePlayerFromParties(UUID playerId, @Nullable String exceptPartyId) {
        for (Party party : parties) {
            if (exceptPartyId != null && party.getId().equals(exceptPartyId)) {
                continue;
            }
            removePlayerFromParty(party, playerId);
        }
        pruneEmptyParties();
    }

    private void clearPlayerInteractions(UUID playerId) {
        parties.forEach(party -> party.clearPlayerInteraction(playerId));
    }

    private void removePlayerFromParty(Party party, UUID playerId) {
        if (!party.removeMember(playerId)) {
            return;
        }
        if (playerId.equals(party.getLeaderId())) {
            party.setLeaderId(party.firstMember().orElse(new UUID(0L, 0L)));
        }
    }

    private void pruneEmptyParties() {
        parties.removeIf(party -> !party.hasMembers());
    }

    private void sanitize() {
        factions.forEach(Faction::normalizeStoredData);
        entityFactions.forEach(EntityFactionEntry::normalizeStoredData);
        playerStandings.forEach(PlayerFactionStandings::normalizeStoredData);
        parties.forEach(Party::normalizeStoredData);
        factions.removeIf(faction -> !faction.hasValidId());
        entityFactions.removeIf(entry -> !entry.hasValidData());
        playerStandings.removeIf(entry -> !entry.hasValidPlayer() || entry.isEmpty());
        parties.removeIf(party -> !party.hasValidId() || !party.hasValidLeader() || !party.hasMembers());
    }
}
