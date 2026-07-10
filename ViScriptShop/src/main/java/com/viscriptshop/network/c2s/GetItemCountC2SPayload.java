package com.viscriptshop.network.c2s;

import com.lowdragmc.lowdraglib2.Platform;
import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacket;
import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacketDistributor;
import com.lowdragmc.lowdraglib2.syncdata.rpc.RPCSender;
import com.viscript_lib.util.CodecUtil;
import com.viscriptshop.gui.data.AggregatedResources;
import com.viscriptshop.gui.data.CategoryInfo;
import com.viscriptshop.gui.data.MerchantInfo;
import com.viscriptshop.network.s2c.S2CPayload;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

public class GetItemCountC2SPayload {
    public static final String GET_ITEM_COUNT = C2SPayload.MOD_ID + "get_item_count";

    @RPCPacket(GET_ITEM_COUNT)
    public static void getItemCount(RPCSender sender, CategoryInfo categoryInfo) {
        ServerPlayer player = sender.asPlayer();
        if (player == null) return;

        AggregatedResources resources = new AggregatedResources();
        categoryInfo.getMerchants().forEach(merchantInfo -> {
            if (categoryInfo.getShopType().equals(CategoryInfo.ShopType.ITEM_FOR_ITEM)) {
                resources.addItemEntry(merchantInfo.getItemA(), 1, merchantInfo.getItemAMatchRule());
                resources.addItemEntry(merchantInfo.getItemB(), 1, merchantInfo.getItemBMatchRule());
            } else if (merchantInfo.getTradeType() == MerchantInfo.TradeType.SELL) {
                resources.addItemEntry(merchantInfo.getItemResult(), 1, null);
            }
        });

        for (AggregatedResources.ItemEntry entry : resources.getItemEntries()) {
            entry.setCount(entry.getItemForPlayerCount(player));
        }
        CompoundTag tag = CodecUtil.serializeList(resources.getItemEntries(), AggregatedResources.ItemEntry.CODEC, Platform.getFrozenRegistry());
        RPCPacketDistributor.rpcToPlayer(player, S2CPayload.GET_ITEM_COUNT, tag);
    }
}
