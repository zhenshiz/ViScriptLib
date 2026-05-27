package com.viscript_lib.container;

import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import com.viscript_lib.register.IContainerHelper;
import com.viscriptshop.util.ItemUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * 原版兼容 玩家背包和末影箱
 */
@LDLRegister(name = "inventory", registry = IContainerHelper.CONTAINER_HELPER_ID, priority = 99)
public class InventoryHelper implements IContainerHelper {
    @Override
    public int getItemStackCount(ServerPlayer player, ItemStack item) {
        int count = 0;

        //背包
        count += player.getInventory().clearOrCountMatchingItems(itemStack -> ItemStack.isSameItemSameComponents(itemStack, item), 0, player.inventoryMenu.getCraftSlots());

        //末影箱
        count += ItemUtil.getItemCountByContainer(player.getEnderChestInventory(), item);

        return count;
    }

    @Override
    public int removeItemStackByCount(ServerPlayer player, ItemStack item, int count) {

        //背包
        count -= player.getInventory().clearOrCountMatchingItems(itemStack -> ItemStack.isSameItemSameComponents(itemStack, item), count, player.inventoryMenu.getCraftSlots());

        //末影箱
        count = ItemUtil.removeItemByContainer(player.getEnderChestInventory(), item, count);

        return count;
    }
}
