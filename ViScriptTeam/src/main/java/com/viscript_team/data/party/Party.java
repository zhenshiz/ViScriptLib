package com.viscript_team.data.party;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class Party implements IPersistedSerializable {
    private static final UUID EMPTY_UUID = new UUID(0L, 0L);

    @Persisted
    private String id = "";
    @Persisted
    private String name = "";
    @Persisted
    private int color = 0xFFFFFF;
    @Persisted
    private UUID leaderId = EMPTY_UUID;
    @Persisted
    private boolean friendlyFire;
    @Persisted
    private final Set<UUID> members = new HashSet<>();
    @Persisted
    private final Set<UUID> invitedPlayers = new HashSet<>();
    @Persisted
    private final Set<UUID> joinRequests = new HashSet<>();

    public Party(String id, UUID leaderId) {
        this.id = normalizeId(id);
        this.name = this.id;
        this.leaderId = leaderId;
        addMember(leaderId);
    }

    public static String normalizeId(String id) {
        String normalized = id == null ? "" : id.trim().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Party id cannot be empty");
        }
        return normalized;
    }

    public Party setId(String id) {
        this.id = normalizeId(id);
        return this;
    }

    public Party setName(String name) {
        this.name = name == null || name.isBlank() ? id : name;
        return this;
    }

    public Set<UUID> getMembers() {
        return Collections.unmodifiableSet(members);
    }

    public Set<UUID> getInvitedPlayers() {
        return Collections.unmodifiableSet(invitedPlayers);
    }

    public Set<UUID> getJoinRequests() {
        return Collections.unmodifiableSet(joinRequests);
    }

    public boolean addMember(UUID playerId) {
        if (!isValidPlayer(playerId)) {
            return false;
        }
        return members.add(playerId);
    }

    public boolean removeMember(UUID playerId) {
        return members.remove(playerId);
    }

    public boolean containsMember(UUID playerId) {
        return members.contains(playerId);
    }

    public boolean isLeader(UUID playerId) {
        return isValidPlayer(playerId) && playerId.equals(leaderId);
    }

    public boolean invitePlayer(UUID playerId) {
        if (!isValidPlayer(playerId) || containsMember(playerId)) {
            return false;
        }
        joinRequests.remove(playerId);
        return invitedPlayers.add(playerId);
    }

    public boolean removeInvitation(UUID playerId) {
        return invitedPlayers.remove(playerId);
    }

    public boolean isInvited(UUID playerId) {
        return invitedPlayers.contains(playerId);
    }

    public boolean addJoinRequest(UUID playerId) {
        if (!isValidPlayer(playerId) || containsMember(playerId) || isInvited(playerId)) {
            return false;
        }
        return joinRequests.add(playerId);
    }

    public boolean removeJoinRequest(UUID playerId) {
        return joinRequests.remove(playerId);
    }

    public boolean hasJoinRequest(UUID playerId) {
        return joinRequests.contains(playerId);
    }

    public boolean clearPlayerInteraction(UUID playerId) {
        return invitedPlayers.remove(playerId) | joinRequests.remove(playerId);
    }

    public boolean allowsFriendlyFire() {
        return friendlyFire;
    }

    public boolean hasValidId() {
        return id != null && !id.isBlank();
    }

    public boolean hasValidLeader() {
        return isValidPlayer(leaderId);
    }

    public boolean hasMembers() {
        return !members.isEmpty();
    }

    public void normalizeStoredData() {
        if (hasValidId()) {
            id = normalizeId(id);
        }
        setName(name);
        members.removeIf(playerId -> !isValidPlayer(playerId));
        invitedPlayers.removeIf(playerId -> !isValidPlayer(playerId) || members.contains(playerId));
        joinRequests.removeIf(playerId -> !isValidPlayer(playerId) || members.contains(playerId));
        if (!hasValidLeader()) {
            leaderId = firstMember().orElse(EMPTY_UUID);
        }
        if (hasValidLeader()) {
            members.add(leaderId);
        }
    }

    public Optional<UUID> firstMember() {
        return members.stream().filter(Party::isValidPlayer).sorted().findFirst();
    }

    public static boolean isValidPlayer(UUID playerId) {
        return playerId != null && !EMPTY_UUID.equals(playerId);
    }
}
