package com.viscript_team.network;

import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacketDistributor;
import com.viscript_team.data.faction.Faction;
import com.viscript_team.data.faction.FactionSavedData;
import com.viscript_team.network.s2c.S2CPayload;
import com.viscript_team.util.FactionEditorService;
import lombok.experimental.UtilityClass;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import java.util.Comparator;

@UtilityClass
public class FactionEditorSync {
    public static void open(ServerPlayer player) {
        if (FactionEditorService.canEdit(player)) {
            RPCPacketDistributor.rpcToPlayer(player, S2CPayload.OPEN_FACTION_EDITOR, createSnapshot(player, null));
        }
    }

    public static void refreshAll(MinecraftServer server, ServerPlayer actor, FactionEditorService.Result result) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (FactionEditorService.canEdit(player)) {
                refresh(player, player.getUUID().equals(actor.getUUID()) ? result : null);
            }
        }
    }

    private static void refresh(ServerPlayer player, @Nullable FactionEditorService.Result result) {
        RPCPacketDistributor.rpcToPlayer(player, S2CPayload.SYNC_FACTION_EDITOR, createSnapshot(player, result));
    }

    private static CompoundTag createSnapshot(ServerPlayer viewer, @Nullable FactionEditorService.Result result) {
        CompoundTag snapshot = new CompoundTag();
        if (result != null && !result.messageKey().isBlank()) {
            snapshot.putString("noticeKey", result.messageKey());
        }

        ListTag factionTags = new ListTag();
        FactionSavedData data = FactionSavedData.get(viewer.serverLevel());
        data.getFactionIds().stream()
                .map(id -> data.getFaction(id).orElse(null))
                .filter(java.util.Objects::nonNull)
                .sorted(Comparator.comparing(Faction::getId, String.CASE_INSENSITIVE_ORDER))
                .map(FactionEditorSync::writeFaction)
                .forEach(factionTags::add);
        snapshot.put("factions", factionTags);
        return snapshot;
    }

    private static CompoundTag writeFaction(Faction faction) {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", faction.getId());
        tag.putString("name", faction.getName());
        tag.putInt("color", faction.getColor());
        tag.putBoolean("friendlyFire", faction.isFriendlyFire());
        tag.putBoolean("attackEnemyFactions", faction.isAttackEnemyFactions());

        ListTag enemies = new ListTag();
        faction.getEnemyFactions().stream()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .map(StringTag::valueOf)
                .forEach(enemies::add);
        tag.put("enemyFactions", enemies);
        return tag;
    }
}
