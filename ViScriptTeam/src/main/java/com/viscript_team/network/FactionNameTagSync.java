package com.viscript_team.network;

import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacketDistributor;
import com.viscript_team.data.faction.EntityFactionEntry;
import com.viscript_team.data.faction.FactionSavedData;
import com.viscript_team.network.s2c.S2CPayload;
import lombok.experimental.UtilityClass;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

@UtilityClass
public class FactionNameTagSync {
    public static void sync(ServerPlayer player) {
        RPCPacketDistributor.rpcToPlayer(player, S2CPayload.SYNC_FACTION_NAME_TAGS, createPayload(player));
    }

    public static void syncAll(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            sync(player);
        }
    }

    private static CompoundTag createPayload(ServerPlayer player) {
        FactionSavedData data = FactionSavedData.get(player.serverLevel());
        CompoundTag payload = new CompoundTag();
        ListTag entries = new ListTag();
        for (EntityFactionEntry entry : data.getEntityFactionEntries()) {
            data.getFaction(entry.getFactionId()).ifPresent(faction -> {
                CompoundTag item = new CompoundTag();
                item.putUUID("entityId", entry.getEntityId());
                item.putString("attitude", data.getPlayerEffectiveAttitude(player.getUUID(), faction.getId()).name());
                entries.add(item);
            });
        }
        payload.put("entries", entries);
        return payload;
    }
}
