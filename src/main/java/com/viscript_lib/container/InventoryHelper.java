package com.viscript_lib.container;

import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import com.viscript_lib.register.IContainerHelper;
import com.viscript_lib.util.item.ItemStackCompareMode;
import com.viscript_lib.util.item.ItemUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.items.wrapper.PlayerMainInvWrapper;

import java.util.List;

/**
 * 原版兼容 玩家背包和末影箱
 */
@LDLRegister(name = "inventory", registry = IContainerHelper.CONTAINER_HELPER_ID, priority = 99)
public class InventoryHelper implements IContainerHelper {
    @Override
    public long getItemStackCount(ServerPlayer player, ItemStack item) {
        return getItemStackCount(player, item, ItemStackCompareMode.ALL_COMPONENTS, List.of());
    }

    @Override
    public long getItemStackCount(ServerPlayer player, ItemStack item,
                                  ItemStackCompareMode compareMode,
                                  List<DataComponentType<?>> components) {
        long count = ItemUtil.getItemCountByContainer(
                player.getInventory(),
                item,
                compareMode,
                components
        );
        count = ItemUtil.saturatedAdd(count, ItemUtil.getItemCountByContainer(
                player.inventoryMenu.getCraftSlots(),
                item,
                compareMode,
                components
        ));
        ItemStack carried = player.containerMenu.getCarried();
        if (ItemUtil.isSameItem(carried, item, compareMode, components)) {
            count = ItemUtil.saturatedAdd(count, carried.getCount());
        }
        count = ItemUtil.saturatedAdd(count, ItemUtil.getItemCountByContainer(
                player.getEnderChestInventory(),
                item,
                compareMode,
                components
        ));

        return count;
    }

    @Override
    public long removeItemStackByCount(ServerPlayer player, ItemStack item, long count) {
        return removeItemStackByCount(player, item, count, ItemStackCompareMode.ALL_COMPONENTS, List.of());
    }

    @Override
    public long removeItemStackByCount(ServerPlayer player, ItemStack item, long count,
                                       ItemStackCompareMode compareMode,
                                       List<DataComponentType<?>> components) {
        count = ItemUtil.removeItemByContainer(
                player.getInventory(),
                item,
                count,
                compareMode,
                components
        );
        count = ItemUtil.removeItemByContainer(
                player.inventoryMenu.getCraftSlots(),
                item,
                count,
                compareMode,
                components
        );
        ItemStack carried = player.containerMenu.getCarried();
        if (count > 0L && ItemUtil.isSameItem(carried, item, compareMode, components)) {
            int removed = (int) Math.min(count, (long) carried.getCount());
            carried.shrink(removed);
            count -= removed;
            if (carried.isEmpty()) {
                player.containerMenu.setCarried(ItemStack.EMPTY);
            }
        }
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
    public long insertItemForPlayer(ServerPlayer player, ItemStack template, long count) {
        return ItemUtil.insertItemByHandler(new PlayerMainInvWrapper(player.getInventory()), template, count);
    }
}
