package com.viscript_lib.container;

import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import com.viscript_lib.register.IContainerHelper;
import com.viscript_lib.util.item.ItemStackCompareMode;
import com.viscript_lib.util.item.ItemUtil;
import com.wintercogs.beyonddimensions.api.dimensionnet.DimensionsNet;
import com.wintercogs.beyonddimensions.api.dimensionnet.UnifiedStorage;
import com.wintercogs.beyonddimensions.api.ids.BDConstants;
import com.wintercogs.beyonddimensions.api.storage.key.KeyAmount;
import com.wintercogs.beyonddimensions.api.storage.key.impl.ItemStackKey;
import com.wintercogs.beyonddimensions.common.init.BDItems;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * 超越维度兼容维度背包
 */
@LDLRegister(name = BDConstants.MODID, registry = IContainerHelper.CONTAINER_HELPER_ID, modID = BDConstants.MODID)
public class BeyondDimensionsHelper implements IContainerHelper {
    @Override
    public long getItemStackCount(ServerPlayer player, ItemStack item) {
        return getItemStackCount(player, item, ItemStackCompareMode.ALL_COMPONENTS, List.of());
    }

    @Override
    public long getItemStackCount(ServerPlayer player, ItemStack item,
                                  ItemStackCompareMode compareMode,
                                  List<DataComponentType<?>> components) {
        DimensionsNet net = DimensionsNet.getPrimaryNetFromPlayer(player);
        if (net != null) {
            UnifiedStorage storage = net.getUnifiedStorage();
            return getMatchingItemCount(storage, item, compareMode, components);
        }
        return 0L;
    }

    @Override
    public long removeItemStackByCount(ServerPlayer player, ItemStack item, long count) {
        return removeItemStackByCount(player, item, count, ItemStackCompareMode.ALL_COMPONENTS, List.of());
    }

    @Override
    public long removeItemStackByCount(ServerPlayer player, ItemStack item, long count,
                                       ItemStackCompareMode compareMode,
                                       List<DataComponentType<?>> components) {
        count = Math.max(0L, count);
        DimensionsNet net = DimensionsNet.getPrimaryNetFromPlayer(player);
        if (net == null) return count;
        if (count == 0L) return 0L;

        var storage = net.getUnifiedStorage();

        return removeMatchingItems(storage, item, count, compareMode, components);
    }

    private static long getMatchingItemCount(UnifiedStorage storage, ItemStack item,
                                             ItemStackCompareMode compareMode,
                                             List<DataComponentType<?>> components) {
        long count = 0L;
        for (KeyAmount keyAmount : storage.getStorage()) {
            Object stack = keyAmount.toStack();
            if (stack instanceof ItemStack itemStack && ItemUtil.isSameItem(itemStack, item, compareMode, components)) {
                count = ItemUtil.saturatedAdd(count, keyAmount.amount());
            }
        }
        return count;
    }

    private static long removeMatchingItems(UnifiedStorage storage, ItemStack item, long count,
                                            ItemStackCompareMode compareMode,
                                            List<DataComponentType<?>> components) {
        long remain = count;
        // UnifiedStorage#getStorage 是基于内部 slotIndex 的视图，扣除时会改变它；先复制一份避免遍历中跳项。
        for (KeyAmount keyAmount : new ArrayList<>(storage.getStorage())) {
            if (remain <= 0) break;

            Object stack = keyAmount.toStack();
            if (stack instanceof ItemStack itemStack && ItemUtil.isSameItem(itemStack, item, compareMode, components)) {
                KeyAmount extracted = storage.extract(keyAmount.key(), remain, false, false);
                remain -= Math.clamp(extracted.amount(), 0L, remain);
            }
        }
        return remain;
    }

    @Override
    public boolean supportsItemOutput() {
        return true;
    }

    @Override
    public ItemStack getItemOutputIcon() {
        return new ItemStack(BDItems.NET_TERMINAL_ITEM.get());
    }

    @Override
    public String getItemOutputTranslationKey() {
        return "viscript_lib.item_output_target.dimension_network";
    }

    @Override
    public boolean isItemOutputAvailable(ServerPlayer player) {
        return DimensionsNet.hasPrimaryNet(player);
    }

    @Override
    public Component getItemOutputUnavailableReason(ServerPlayer player) {
        return Component.translatable("viscript_lib.item_output_target.dimension_network_unavailable");
    }

    @Override
    public long insertItemForPlayer(ServerPlayer player, ItemStack template, long count) {
        count = Math.max(0L, count);
        if (template == null || template.isEmpty() || count == 0L) return count;

        DimensionsNet network = DimensionsNet.getPrimaryNetFromPlayer(player);
        if (network == null) return count;
        KeyAmount remaining = network.getUnifiedStorage().insert(
                new ItemStackKey(template.copyWithCount(1)),
                count,
                false
        );
        return Math.clamp(remaining.amount(), 0L, count);
    }
}
