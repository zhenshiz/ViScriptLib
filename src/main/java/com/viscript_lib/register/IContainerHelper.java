package com.viscript_lib.register;

import com.lowdragmc.lowdraglib2.registry.ILDLRegister;
import com.viscript_lib.ViScriptLib;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

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
     * 删除物品
     *
     * @param player 玩家
     * @param item   物品
     * @param count  要删除的物品数量
     * @return 删除后剩余的数量
     */
    int removeItemStackByCount(ServerPlayer player, ItemStack item, int count);
}
