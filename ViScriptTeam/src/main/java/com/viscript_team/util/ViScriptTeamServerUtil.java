package com.viscript_team.util;

import com.lowdragmc.lowdraglib2.integration.kjs.KJSBindings;
import com.viscript_team.ViScriptTeam;
import com.viscript_team.data.faction.Faction;
import com.viscript_team.data.faction.FactionAttitude;
import com.viscript_team.data.faction.FactionSavedData;
import com.viscript_team.data.party.Party;
import com.viscript_team.data.party.PartyStandingStrategy;
import com.viscript_team.network.FactionNameTagSync;
import dev.latvian.mods.kubejs.typings.Info;
import lombok.experimental.UtilityClass;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

@KJSBindings(value = "ViScriptTeamUtil", modId = ViScriptTeam.MOD_ID)
@UtilityClass
public class ViScriptTeamServerUtil {
    @Info("服务端获取阵营存档数据")
    public static FactionSavedData getFactionData(ServerLevel level) {
        return FactionSavedData.get(level);
    }

    @Info("服务端创建阵营，已存在时返回 false")
    public static boolean createFaction(ServerLevel level, String factionId) {
        FactionSavedData data = getFactionData(level);
        String id = Faction.normalizeId(factionId);
        if (data.getFaction(id).isPresent()) {
            return false;
        }
        data.getOrCreateFaction(id);
        return true;
    }

    @Info("服务端删除阵营，删除成功时同步所有玩家的阵营名牌缓存")
    public static boolean deleteFaction(ServerLevel level, String factionId) {
        boolean removed = getFactionData(level).removeFaction(factionId);
        if (removed) {
            FactionNameTagSync.syncAll(level.getServer());
        }
        return removed;
    }

    @Info("服务端获取所有阵营 ID")
    public static Set<String> getFactionIds(ServerLevel level) {
        return new TreeSet<>(getFactionData(level).getFactionIds());
    }

    @Nullable
    @Info("服务端获取阵营数据，不存在时返回 null")
    public static Faction getFaction(ServerLevel level, String factionId) {
        return getFactionData(level).getFaction(factionId).orElse(null);
    }

    @Info("服务端获取阵营的敌对阵营 ID")
    public static Set<String> getEnemyFactions(ServerLevel level, String factionId) {
        Faction faction = getFaction(level, factionId);
        return faction == null ? Set.of() : new TreeSet<>(faction.getEnemyFactions());
    }

    @Info("服务端添加单向敌对阵营关系，发生变化时返回 true")
    public static boolean addEnemyFaction(ServerLevel level, String factionId, String enemyFactionId) {
        return getFactionData(level).addEnemyFaction(factionId, enemyFactionId);
    }

    @Info("服务端移除单向敌对阵营关系，发生变化时返回 true")
    public static boolean removeEnemyFaction(ServerLevel level, String factionId, String enemyFactionId) {
        return getFactionData(level).removeEnemyFaction(factionId, enemyFactionId);
    }

    @Info("服务端设置单个实体的阵营")
    public static void setEntityFaction(Entity entity, @Nullable String factionId) {
        setEntityFaction(List.of(entity), factionId);
    }

    @Info("服务端批量设置实体阵营，返回处理的实体数量")
    public static int setEntityFaction(Collection<? extends Entity> targets, @Nullable String factionId) {
        MinecraftServer server = null;
        for (Entity entity : targets) {
            FactionApi.data(entity.level()).ifPresent(data -> data.setEntityFaction(entity.getUUID(), factionId));
            if (server == null && !entity.level().isClientSide()) {
                server = entity.level().getServer();
            }
        }
        if (server != null) {
            FactionNameTagSync.syncAll(server);
        }
        return targets.size();
    }

    @Info("服务端清除单个实体的阵营")
    public static void clearEntityFaction(Entity entity) {
        setEntityFaction(entity, null);
    }

    @Info("服务端批量清除实体阵营，返回处理的实体数量")
    public static int clearEntityFaction(Collection<? extends Entity> targets) {
        return setEntityFaction(targets, null);
    }

    @Nullable
    @Info("服务端获取实体阵营 ID，不存在时返回 null")
    public static String getEntityFactionId(Entity entity) {
        return FactionApi.getEntityFactionId(entity).orElse(null);
    }

    @Info("服务端设置玩家对阵营的声望")
    public static void setPlayerStanding(ServerPlayer player, String factionId, int points) {
        FactionApi.setPlayerStanding(player, factionId, points);
    }

    @Info("服务端批量设置玩家对阵营的声望，返回处理的玩家数量")
    public static int setPlayerStanding(Collection<ServerPlayer> players, String factionId, int points) {
        players.forEach(player -> setPlayerStanding(player, factionId, points));
        return players.size();
    }

    @Info("服务端调整玩家对阵营的声望，并返回调整后的声望")
    public static int addPlayerStanding(ServerPlayer player, String factionId, int delta) {
        FactionApi.addPlayerStanding(player, factionId, delta);
        return getPlayerStanding(player, factionId);
    }

    @Info("服务端批量调整玩家对阵营的声望，返回处理的玩家数量")
    public static int addPlayerStanding(Collection<ServerPlayer> players, String factionId, int delta) {
        players.forEach(player -> addPlayerStanding(player, factionId, delta));
        return players.size();
    }

    @Info("服务端获取玩家对阵营的声望")
    public static int getPlayerStanding(ServerPlayer player, String factionId) {
        return FactionApi.getPlayerStanding(player, factionId);
    }

    @Info("服务端获取玩家对阵营的态度")
    public static FactionAttitude getPlayerAttitude(ServerPlayer player, String factionId) {
        return FactionApi.getPlayerAttitude(player, factionId);
    }

    @Info("服务端获取玩家对阵营的队伍有效声望，默认使用队伍成员最低值")
    public static int getPlayerEffectiveStanding(ServerPlayer player, String factionId) {
        return FactionApi.getPlayerEffectiveStanding(player, factionId);
    }

    @Info("服务端按策略获取玩家对阵营的队伍有效声望，可用策略: min, average, leader, max")
    public static int getPlayerEffectiveStanding(ServerPlayer player, String factionId, String strategy) {
        return FactionApi.getPlayerEffectiveStanding(player, factionId, PartyStandingStrategy.byNameOrDefault(strategy));
    }

    @Info("服务端获取玩家对阵营的队伍有效态度，默认使用队伍成员最低值")
    public static FactionAttitude getPlayerEffectiveAttitude(ServerPlayer player, String factionId) {
        return FactionApi.getPlayerEffectiveAttitude(player, factionId);
    }

    @Info("服务端按策略获取玩家对阵营的队伍有效态度，可用策略: min, average, leader, max")
    public static FactionAttitude getPlayerEffectiveAttitude(ServerPlayer player, String factionId, String strategy) {
        return FactionApi.getPlayerEffectiveAttitude(player, factionId, PartyStandingStrategy.byNameOrDefault(strategy));
    }

    @Info("服务端判断攻击者是否能伤害目标")
    public static boolean canHurt(LivingEntity attacker, LivingEntity target) {
        return FactionApi.canHurt(attacker, target);
    }

    @Info("服务端判断攻击者是否能锁定目标")
    public static boolean canTarget(LivingEntity attacker, LivingEntity target) {
        return FactionApi.canTarget(attacker, target);
    }

    @Info("服务端判断攻击者是否应该主动锁定目标")
    public static boolean shouldActivelyTarget(LivingEntity attacker, LivingEntity target) {
        return FactionApi.shouldActivelyTarget(attacker, target);
    }

    @Info("服务端创建玩家队伍，已存在或队长无效时返回 false")
    public static boolean createParty(ServerLevel level, String partyId, ServerPlayer leader) {
        boolean created = getFactionData(level).createParty(partyId, leader.getUUID());
        if (created) {
            PartyChatService.clear(level.getServer(), Party.normalizeId(partyId));
            FactionNameTagSync.syncAll(level.getServer());
        }
        return created;
    }

    @Info("服务端删除玩家队伍，删除成功时返回 true")
    public static boolean deleteParty(ServerLevel level, String partyId) {
        boolean removed = getFactionData(level).removeParty(partyId);
        if (removed) {
            PartyChatService.clear(level.getServer(), Party.normalizeId(partyId));
            FactionNameTagSync.syncAll(level.getServer());
        }
        return removed;
    }

    @Info("服务端获取所有玩家队伍 ID")
    public static Set<String> getPartyIds(ServerLevel level) {
        return new TreeSet<>(getFactionData(level).getPartyIds());
    }

    @Nullable
    @Info("服务端获取玩家队伍数据，不存在时返回 null")
    public static Party getParty(ServerLevel level, String partyId) {
        return getFactionData(level).getParty(partyId).orElse(null);
    }

    @Nullable
    @Info("服务端获取玩家所在队伍 ID，不存在时返回 null")
    public static String getPlayerPartyId(ServerPlayer player) {
        return FactionApi.getPlayerPartyId(player).orElse(null);
    }

    @Info("服务端获取玩家队伍成员 UUID")
    public static Set<UUID> getPartyMemberIds(ServerLevel level, String partyId) {
        Party party = getParty(level, partyId);
        return party == null ? Set.of() : party.getMembers();
    }

    @Info("服务端将玩家加入队伍，玩家会自动离开原队伍")
    public static boolean joinParty(ServerPlayer player, String partyId) {
        return joinParty(List.of(player), partyId) > 0;
    }

    @Info("服务端批量将玩家加入队伍，返回发生变化的玩家数量")
    public static int joinParty(Collection<ServerPlayer> players, String partyId) {
        int changed = 0;
        MinecraftServer server = null;
        for (ServerPlayer player : players) {
            if (server == null) {
                server = player.server;
            }
            if (getFactionData(player.serverLevel()).joinParty(player.getUUID(), partyId)) {
                changed++;
            }
        }
        if (changed > 0 && server != null) {
            FactionNameTagSync.syncAll(server);
        }
        return changed;
    }

    @Info("服务端让玩家离开当前队伍")
    public static boolean leaveParty(ServerPlayer player) {
        return leaveParty(List.of(player)) > 0;
    }

    @Info("服务端批量让玩家离开当前队伍，返回发生变化的玩家数量")
    public static int leaveParty(Collection<ServerPlayer> players) {
        int changed = 0;
        MinecraftServer server = null;
        for (ServerPlayer player : players) {
            if (server == null) {
                server = player.server;
            }
            if (getFactionData(player.serverLevel()).leaveParty(player.getUUID())) {
                changed++;
            }
        }
        if (changed > 0 && server != null) {
            FactionNameTagSync.syncAll(server);
        }
        return changed;
    }

    @Info("服务端设置队伍队长，目标玩家会自动加入该队伍")
    public static boolean setPartyLeader(ServerLevel level, String partyId, ServerPlayer leader) {
        boolean changed = getFactionData(level).setPartyLeader(partyId, leader.getUUID());
        if (changed) {
            FactionNameTagSync.syncAll(level.getServer());
        }
        return changed;
    }

    @Info("服务端设置队伍友伤开关")
    public static boolean setPartyFriendlyFire(ServerLevel level, String partyId, boolean friendlyFire) {
        return getFactionData(level).setPartyFriendlyFire(partyId, friendlyFire);
    }

    @Info("服务端判断两个玩家是否在同一队伍")
    public static boolean isSameParty(ServerPlayer firstPlayer, ServerPlayer secondPlayer) {
        return FactionApi.isSameParty(firstPlayer, secondPlayer);
    }

    @Nullable
    @Info("服务端获取队伍对阵营的有效声望，不存在队伍时返回 null，默认使用队伍成员最低值")
    public static Integer getPartyEffectiveStanding(ServerLevel level, String partyId, String factionId) {
        return FactionApi.getPartyEffectiveStanding(level, partyId, factionId).stream().boxed().findFirst().orElse(null);
    }

    @Nullable
    @Info("服务端按策略获取队伍对阵营的有效声望，不存在队伍时返回 null，可用策略: min, average, leader, max")
    public static Integer getPartyEffectiveStanding(ServerLevel level, String partyId, String factionId, String strategy) {
        return FactionApi.getPartyEffectiveStanding(level, partyId, factionId, PartyStandingStrategy.byNameOrDefault(strategy))
                .stream()
                .boxed()
                .findFirst()
                .orElse(null);
    }

    @Nullable
    @Info("服务端获取队伍对阵营的有效态度，不存在队伍时返回 null，默认使用队伍成员最低值")
    public static FactionAttitude getPartyEffectiveAttitude(ServerLevel level, String partyId, String factionId) {
        return FactionApi.getPartyEffectiveAttitude(level, partyId, factionId).orElse(null);
    }

    @Nullable
    @Info("服务端按策略获取队伍对阵营的有效态度，不存在队伍时返回 null，可用策略: min, average, leader, max")
    public static FactionAttitude getPartyEffectiveAttitude(ServerLevel level, String partyId, String factionId, String strategy) {
        return FactionApi.getPartyEffectiveAttitude(level, partyId, factionId, PartyStandingStrategy.byNameOrDefault(strategy)).orElse(null);
    }
}
