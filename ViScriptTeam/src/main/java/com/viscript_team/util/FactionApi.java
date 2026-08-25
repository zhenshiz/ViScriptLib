package com.viscript_team.util;

import com.viscript_team.data.faction.Faction;
import com.viscript_team.data.faction.FactionAttitude;
import com.viscript_team.data.faction.FactionSavedData;
import com.viscript_team.data.party.Party;
import com.viscript_team.data.party.PartyStandingStrategy;
import com.viscript_team.network.FactionNameTagSync;
import lombok.experimental.UtilityClass;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;

@UtilityClass
public class FactionApi {
    public static FactionSavedData data(ServerLevel level) {
        return FactionSavedData.get(level);
    }

    public static Optional<FactionSavedData> data(Level level) {
        if (level.isClientSide() || level.getServer() == null) {
            return Optional.empty();
        }
        return Optional.of(FactionSavedData.get(level.getServer().overworld()));
    }

    public static Faction getOrCreateFaction(ServerLevel level, String factionId) {
        return data(level).getOrCreateFaction(factionId);
    }

    public static void setEntityFaction(Entity entity, @Nullable String factionId) {
        data(entity.level()).ifPresent(data -> data.setEntityFaction(entity.getUUID(), factionId));
        if (!entity.level().isClientSide() && entity.level().getServer() != null) {
            FactionNameTagSync.syncAll(entity.level().getServer());
        }
    }

    public static Optional<String> getEntityFactionId(Entity entity) {
        return data(entity.level()).flatMap(data -> data.getEntityFaction(entity.getUUID()));
    }

    public static Optional<Faction> getEntityFaction(Entity entity) {
        return data(entity.level()).flatMap(data -> data.getEntityFaction(entity.getUUID()).flatMap(data::getFaction));
    }

    public static int getPlayerStanding(ServerPlayer player, String factionId) {
        return data(player.serverLevel()).getPlayerStanding(player.getUUID(), factionId);
    }

    public static void setPlayerStanding(ServerPlayer player, String factionId, int points) {
        data(player.serverLevel()).setPlayerStanding(player.getUUID(), factionId, points);
        syncPlayerAndPartyViews(player);
    }

    public static void addPlayerStanding(ServerPlayer player, String factionId, int delta) {
        data(player.serverLevel()).addPlayerStanding(player.getUUID(), factionId, delta);
        syncPlayerAndPartyViews(player);
    }

    public static FactionAttitude getPlayerAttitude(ServerPlayer player, String factionId) {
        return data(player.serverLevel()).getPlayerAttitude(player.getUUID(), factionId);
    }

    public static int getPlayerEffectiveStanding(ServerPlayer player, String factionId) {
        return data(player.serverLevel()).getPlayerEffectiveStanding(player.getUUID(), factionId);
    }

    public static int getPlayerEffectiveStanding(ServerPlayer player, String factionId, PartyStandingStrategy strategy) {
        return data(player.serverLevel()).getPlayerEffectiveStanding(player.getUUID(), factionId, strategy);
    }

    public static FactionAttitude getPlayerEffectiveAttitude(ServerPlayer player, String factionId) {
        return data(player.serverLevel()).getPlayerEffectiveAttitude(player.getUUID(), factionId);
    }

    public static FactionAttitude getPlayerEffectiveAttitude(ServerPlayer player, String factionId, PartyStandingStrategy strategy) {
        return data(player.serverLevel()).getPlayerEffectiveAttitude(player.getUUID(), factionId, strategy);
    }

    public static OptionalInt getPartyEffectiveStanding(ServerLevel level, String partyId, String factionId) {
        return data(level).getPartyEffectiveStanding(partyId, factionId);
    }

    public static OptionalInt getPartyEffectiveStanding(ServerLevel level, String partyId, String factionId, PartyStandingStrategy strategy) {
        return data(level).getPartyEffectiveStanding(partyId, factionId, strategy);
    }

    public static Optional<FactionAttitude> getPartyEffectiveAttitude(ServerLevel level, String partyId, String factionId) {
        return data(level).getPartyEffectiveAttitude(partyId, factionId);
    }

    public static Optional<FactionAttitude> getPartyEffectiveAttitude(ServerLevel level, String partyId, String factionId, PartyStandingStrategy strategy) {
        return data(level).getPartyEffectiveAttitude(partyId, factionId, strategy);
    }

    public static Optional<Party> getPlayerParty(ServerPlayer player) {
        return data(player.serverLevel()).getPlayerParty(player.getUUID());
    }

    public static Optional<String> getPlayerPartyId(ServerPlayer player) {
        return getPlayerParty(player).map(Party::getId);
    }

    public static boolean isSameParty(ServerPlayer firstPlayer, ServerPlayer secondPlayer) {
        return data(firstPlayer.serverLevel()).isSameParty(firstPlayer.getUUID(), secondPlayer.getUUID());
    }

    public static boolean canHurt(LivingEntity attacker, LivingEntity target) {
        if (attacker == target || attacker.level().isClientSide()) {
            return true;
        }
        Optional<FactionSavedData> optionalData = data(attacker.level());
        return optionalData.map(savedData -> canHurt(savedData, attacker, target)).orElse(true);
    }

    public static boolean canTarget(LivingEntity attacker, LivingEntity target) {
        if (attacker == target || attacker.level().isClientSide()) {
            return true;
        }
        Optional<FactionSavedData> optionalData = data(attacker.level());
        return optionalData.map(savedData -> canTarget(savedData, attacker, target)).orElse(true);
    }

    public static boolean canHurtFromSource(DamageSource source, LivingEntity target) {
        // 优先追溯伤害来源实体，简单直接伤害再回退到 directEntity。
        Entity attacker = source.getEntity();
        LivingEntity livingAttacker;
        if (attacker instanceof LivingEntity entityAttacker) {
            livingAttacker = entityAttacker;
        } else {
            attacker = source.getDirectEntity();
            if (!(attacker instanceof LivingEntity directLivingAttacker)) {
                return true;
            }
            livingAttacker = directLivingAttacker;
        }
        return canHurt(livingAttacker, target);
    }

    public static boolean isHostileTo(LivingEntity observer, LivingEntity target) {
        Optional<FactionSavedData> optionalData = data(observer.level());
        return optionalData.map(savedData -> isHostileTo(savedData, observer, target)).orElse(false);
    }

    public static boolean shouldActivelyTarget(LivingEntity attacker, LivingEntity target) {
        return isHostileTo(attacker, target) && canTarget(attacker, target);
    }

    private static boolean canHurt(FactionSavedData data, LivingEntity attacker, LivingEntity target) {
        if (attacker instanceof Player attackerPlayer && target instanceof Player targetPlayer
                && !data.canPartyMembersHurt(attackerPlayer.getUUID(), targetPlayer.getUUID())) {
            return false;
        }

        Optional<String> attackerFactionId = data.getEntityFaction(attacker.getUUID());
        Optional<String> targetFactionId = data.getEntityFaction(target.getUUID());

        if (attackerFactionId.isPresent() && targetFactionId.isPresent() && attackerFactionId.get().equals(targetFactionId.get())) {
            return data.getFaction(attackerFactionId.get()).map(Faction::allowsFriendlyFire).orElse(true);
        }

        if (attackerFactionId.isPresent() && target instanceof Player player) {
            return canFactionHurtPlayer(data, attackerFactionId.get(), player.getUUID());
        }

        if (attacker instanceof Player player && targetFactionId.isPresent()) {
            return canPlayerHurtFaction(data, player.getUUID(), targetFactionId.get());
        }

        return true;
    }

    private static boolean canTarget(FactionSavedData data, LivingEntity attacker, LivingEntity target) {
        // 非玩家实体对玩家看声望点数，非玩家实体之间看攻击方阵营的敌对列表。
        Optional<String> attackerFactionId = data.getEntityFaction(attacker.getUUID());
        if (attackerFactionId.isEmpty()) {
            return true;
        }

        Optional<Faction> attackerFaction = data.getFaction(attackerFactionId.get());
        if (attackerFaction.isEmpty()) {
            return true;
        }

        if (target instanceof Player player) {
            return data.getPlayerEffectiveAttitude(player.getUUID(), attackerFaction.get().getId()) == FactionAttitude.HOSTILE;
        }

        Optional<String> targetFactionId = data.getEntityFaction(target.getUUID());
        if (targetFactionId.isEmpty()) {
            return true;
        }

        if (attackerFaction.get().getId().equals(targetFactionId.get())) {
            return false;
        }

        return attackerFaction.get().attacksEnemyFactions() && data.isEnemyFaction(attackerFaction.get().getId(), targetFactionId.get());
    }

    private static boolean isHostileTo(FactionSavedData data, LivingEntity observer, LivingEntity target) {
        Optional<String> observerFactionId = data.getEntityFaction(observer.getUUID());
        if (observerFactionId.isEmpty()) {
            return false;
        }

        if (target instanceof Player player) {
            return data.getPlayerEffectiveAttitude(player.getUUID(), observerFactionId.get()) == FactionAttitude.HOSTILE;
        }

        Optional<String> targetFactionId = data.getEntityFaction(target.getUUID());
        return targetFactionId.filter(s -> data.isEnemyFaction(observerFactionId.get(), s)).isPresent();
    }

    private static boolean canFactionHurtPlayer(FactionSavedData data, String factionId, UUID playerId) {
        Optional<Faction> faction = data.getFaction(factionId);
        if (faction.isEmpty()) {
            return true;
        }
        if (data.getPlayerEffectiveAttitude(playerId, factionId) != FactionAttitude.FRIENDLY) {
            return true;
        }
        return faction.get().allowsFriendlyFire();
    }

    private static boolean canPlayerHurtFaction(FactionSavedData data, UUID playerId, String factionId) {
        Optional<Faction> faction = data.getFaction(factionId);
        if (faction.isEmpty()) {
            return true;
        }
        if (data.getPlayerEffectiveAttitude(playerId, factionId) != FactionAttitude.FRIENDLY) {
            return true;
        }
        return faction.get().allowsFriendlyFire();
    }

    private static void syncPlayerAndPartyViews(ServerPlayer player) {
        Optional<Party> party = getPlayerParty(player);
        if (party.isEmpty()) {
            FactionNameTagSync.sync(player);
            return;
        }
        for (UUID memberId : party.get().getMembers()) {
            ServerPlayer member = player.server.getPlayerList().getPlayer(memberId);
            if (member != null) {
                FactionNameTagSync.sync(member);
            }
        }
    }
}
