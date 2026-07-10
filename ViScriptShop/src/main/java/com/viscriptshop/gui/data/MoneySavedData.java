package com.viscriptshop.gui.data;

import com.lowdragmc.lowdraglib2.Platform;
import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacketDistributor;
import com.viscriptshop.ShopRegistries;
import com.viscriptshop.network.s2c.S2CPayload;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.UUID;

public class MoneySavedData extends SavedData {
    private final HashMap<UUID, ShopRegistries.Money> data = new HashMap<>();

    public ShopRegistries.Money getMoney(Player player) {
        return data.getOrDefault(player.getUUID(), new ShopRegistries.Money());
    }

    public void setMoney(Player player, ShopRegistries.Money money) {
        if (player.level().isClientSide()) return;
        data.put(player.getUUID(), money);
        setDirty();
        RPCPacketDistributor.rpcToPlayer((ServerPlayer) player, S2CPayload.SYNC_PLAYER_MONEY, money);
    }

    public static MoneySavedData fromNbt(CompoundTag nbt) {
        MoneySavedData data1 = new MoneySavedData();
        for (String s : nbt.getAllKeys()) {
            try {
                ShopRegistries.Money money = new ShopRegistries.Money();
                money.deserializeNBT(Platform.getFrozenRegistry(), nbt.getCompound(s));
                data1.data.put(UUID.fromString(s), money);
            } catch (Exception ignored) {
            }
        }
        return data1;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag nbt) {
        for (UUID uuid : data.keySet()) {
            nbt.put(uuid.toString(), data.get(uuid).serializeNBT(Platform.getFrozenRegistry()));
        }
        return nbt;
    }
}
