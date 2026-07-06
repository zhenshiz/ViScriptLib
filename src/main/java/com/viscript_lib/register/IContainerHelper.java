package com.viscript_lib.register;

import com.lowdragmc.lowdraglib2.registry.ILDLRegister;
import com.viscript_lib.ViScriptLib;
import com.viscript_lib.util.item.ItemStackCompareMode;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Supplier;

public interface IContainerHelper extends ILDLRegister<IContainerHelper, Supplier<IContainerHelper>> {
    String CONTAINER_HELPER_ID = ViScriptLib.MOD_ID + ":container_helper";

    /**
     * 获取物品的数量
     *
     * @param player 玩家
     * @param item   物品
     * @return 该物品的数量
     */
    int getItemStackCount(ServerPlayer player, ItemStack item);

    /**
     * 按指定物品组件比较模式获取物品数量。
     *
     * @param player 玩家
     * @param item 物品
     * @param compareMode 物品比较模式
     * @param nbtKeys 参与或排除比较的nbt键列表，语义由 compareMode 决定
     * @return 该物品的数量
     */
    default int getItemStackCount(ServerPlayer player, ItemStack item,
                                  ItemStackCompareMode compareMode,
                                  List<String> nbtKeys) {
        return getItemStackCount(player, item);
    }

    /**
     * 删除物品
     *
     * @param player 玩家
     * @param item   物品
     * @param count  要删除的物品数量
     * @return 删除后剩余的数量
     */
    int removeItemStackByCount(ServerPlayer player, ItemStack item, int count);

    /**
     * 按指定物品组件比较模式删除物品。
     *
     * @param player 玩家
     * @param item 物品
     * @param count 要删除的物品数量
     * @param compareMode 物品比较模式
     * @param nbtKeys 参与或排除比较的nbt键列表，语义由 compareMode 决定
     * @return 删除后剩余的数量
     */
    default int removeItemStackByCount(ServerPlayer player, ItemStack item, int count,
                                       ItemStackCompareMode compareMode,
                                       List<String> nbtKeys) {
        return removeItemStackByCount(player, item, count);
    }
}
