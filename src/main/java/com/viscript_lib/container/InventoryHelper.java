package com.viscript_lib.container;

import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import com.viscript_lib.register.IContainerHelper;
import com.viscript_lib.util.item.ItemStackCompareMode;
import com.viscript_lib.util.item.ItemUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import java.util.List;

/**
 * 原版兼容 玩家背包和末影箱
 */
@LDLRegister(name = "inventory", registry = IContainerHelper.CONTAINER_HELPER_ID, priority = 99)
public class InventoryHelper implements IContainerHelper {
    @Override
    public int getItemStackCount(ServerPlayer player, ItemStack item) {
        return getItemStackCount(player, item, ItemStackCompareMode.ALL_COMPONENTS, List.of());
    }

    @Override
    public int getItemStackCount(ServerPlayer player, ItemStack item,
                                 ItemStackCompareMode compareMode,
                                 List<DataComponentType<?>> components) {
        int count = 0;

        //背包
        count += player.getInventory().clearOrCountMatchingItems(
                itemStack -> ItemUtil.isSameItem(itemStack, item, compareMode, components),
                0,
                player.inventoryMenu.getCraftSlots()
        );

        //末影箱
        count += ItemUtil.getItemCountByContainer(player.getEnderChestInventory(), item, compareMode, components);

        return count;
    }

    @Override
    public int removeItemStackByCount(ServerPlayer player, ItemStack item, int count) {
        return removeItemStackByCount(player, item, count, ItemStackCompareMode.ALL_COMPONENTS, List.of());
    }

    @Override
    public int removeItemStackByCount(ServerPlayer player, ItemStack item, int count,
                                      ItemStackCompareMode compareMode,
                                      List<DataComponentType<?>> components) {

        //背包
        count -= player.getInventory().clearOrCountMatchingItems(
                itemStack -> ItemUtil.isSameItem(itemStack, item, compareMode, components),
                count,
                player.inventoryMenu.getCraftSlots()
        );

        //末影箱
        count = ItemUtil.removeItemByContainer(player.getEnderChestInventory(), item, count, compareMode, components);

        return count;
    }

    @Override
    public boolean supportsItemOutput() {
        return true;
    }

    @Override
    public ItemStack getItemOutputIcon() {
        return new ItemStack(Items.CHEST);
    }

    @Override
    public String getItemOutputTranslationKey() {
        return "viscript_lib.item_output_target.player_inventory";
    }

    @Override
    public ItemStack insertItemForPlayer(ServerPlayer player, ItemStack stack) {
        ItemHandlerHelper.giveItemToPlayer(player, stack);
        return ItemStack.EMPTY;
    }
}
