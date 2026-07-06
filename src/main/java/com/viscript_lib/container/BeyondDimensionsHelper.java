package com.viscript_lib.container;

import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import com.viscript_lib.register.IContainerHelper;
import com.viscript_lib.util.item.ItemStackCompareMode;
import com.viscript_lib.util.item.ItemUtil;
import com.viscript_lib.util.math.Clamp;
import com.wintercogs.beyonddimensions.api.dimensionnet.DimensionsNet;
import com.wintercogs.beyonddimensions.api.dimensionnet.UnifiedStorage;
import com.wintercogs.beyonddimensions.api.ids.BDConstants;
import com.wintercogs.beyonddimensions.api.storage.key.KeyAmount;
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
    public int getItemStackCount(ServerPlayer player, ItemStack item) {
        return getItemStackCount(player, item, ItemStackCompareMode.ALL_COMPONENTS, List.of());
    }

    @Override
    public int getItemStackCount(ServerPlayer player, ItemStack item,
                                 ItemStackCompareMode compareMode,
                                 List<String> components) {
        DimensionsNet net = DimensionsNet.getPrimaryNetFromPlayer(player);
        if (net != null) {
            UnifiedStorage storage = net.getUnifiedStorage();
            return getMatchingItemCount(storage, item, compareMode, components);
        }
        return 0;
    }

    @Override
    public int removeItemStackByCount(ServerPlayer player, ItemStack item, int count) {
        return removeItemStackByCount(player, item, count, ItemStackCompareMode.ALL_COMPONENTS, List.of());
    }

    @Override
    public int removeItemStackByCount(ServerPlayer player, ItemStack item, int count,
                                      ItemStackCompareMode compareMode,
                                      List<String> components) {
        DimensionsNet net = DimensionsNet.getPrimaryNetFromPlayer(player);
        if (net == null) return count;
        if (count <= 0) return 0;

        var storage = net.getUnifiedStorage();

        return removeMatchingItems(storage, item, count, compareMode, components);
    }

    private static int getMatchingItemCount(UnifiedStorage storage, ItemStack item,
                                            ItemStackCompareMode compareMode,
                                            List<String> components) {
        int count = 0;
        for (KeyAmount keyAmount : storage.getStorage()) {
            Object stack = keyAmount.toStack();
            if (stack instanceof ItemStack itemStack && ItemUtil.isSameItem(itemStack, item, compareMode, components)) {
                count += Clamp.clamp(keyAmount.amount(), 0, Integer.MAX_VALUE);
            }
        }
        return count;
    }

    private static int removeMatchingItems(UnifiedStorage storage, ItemStack item, int count,
                                           ItemStackCompareMode compareMode,
                                           List<String> components) {
        int remain = count;
        // UnifiedStorage#getStorage 是基于内部 slotIndex 的视图，扣除时会改变它；先复制一份避免遍历中跳项。
        for (KeyAmount keyAmount : new ArrayList<>(storage.getStorage())) {
            if (remain <= 0) break;

            Object stack = keyAmount.toStack();
            if (stack instanceof ItemStack itemStack && ItemUtil.isSameItem(itemStack, item, compareMode, components)) {
                KeyAmount extracted = storage.extract(keyAmount.key(), remain, false, false);
                remain -= Clamp.clamp(extracted.amount(), 0, Integer.MAX_VALUE);
            }
        }
        return remain;
    }
}
