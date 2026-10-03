package com.viscript_lib.container;

import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import com.mrcrayfish.backpacked.BackpackHelper;
import com.mrcrayfish.backpacked.inventory.BackpackInventory;
import com.mrcrayfish.backpacked.inventory.BackpackedInventoryAccess;
import com.viscript_lib.register.IContainerHelper;
import com.viscript_lib.util.item.ItemStackCompareMode;
import com.viscript_lib.util.item.ItemUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.wrapper.InvWrapper;

import java.util.List;

/**
 * 支持玩家已装备的 Backpacked 背包中的物品查询、扣除和输出。
 *
 * <p>仅访问已解锁的装备栏和库存格子，不访问物品栏中未装备的背包或世界中的背包。
 * 物品修改立即写回背包物品，输出遵守背包的容量及物品准入规则。
 */
@LDLRegister(name = "backpacked", registry = IContainerHelper.CONTAINER_HELPER_ID, modID = "backpacked")
public class BackpackedHelper implements IContainerHelper {
    @Override
    public long getItemStackCount(ServerPlayer player, ItemStack item) {
        return getItemStackCount(player, item, ItemStackCompareMode.ALL_COMPONENTS, List.of());
    }

    @Override
    public long getItemStackCount(ServerPlayer player, ItemStack item,
                                  ItemStackCompareMode compareMode,
                                  List<String> components) {
        long count = 0L;
        for (var inventory : inventories(player)) {
            count = ItemUtil.saturatedAdd(count,
                    ItemUtil.getItemCountByContainer(inventory, item, compareMode, components));
        }
        return count;
    }

    @Override
    public long removeItemStackByCount(ServerPlayer player, ItemStack item, long count) {
        return removeItemStackByCount(player, item, count, ItemStackCompareMode.ALL_COMPONENTS, List.of());
    }

    @Override
    public long removeItemStackByCount(ServerPlayer player, ItemStack item, long count,
                                       ItemStackCompareMode compareMode,
                                       List<String> components) {
        count = Math.max(0L, count);
        if (count == 0L) return count;
        for (var inventory : inventories(player)) {
            long remaining = ItemUtil.removeItemByContainer(inventory, item, count, compareMode, components);
            if (remaining < count) inventory.saveItemsToStack();
            count = remaining;
            if (count == 0L) break;
        }
        return count;
    }

    @Override
    public boolean supportsItemOutput() {
        return true;
    }

    @Override
    public ItemStack getItemOutputIcon() {
        return new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation("backpacked", "backpack")));
    }

    @Override
    public boolean isItemOutputAvailable(ServerPlayer player) {
        return player != null && BackpackHelper.firstAvailableBackpackIndex(player) >= 0;
    }

    @Override
    public Component getItemOutputUnavailableReason(ServerPlayer player) {
        return Component.translatable("viscript_lib.item_output_target.backpacked_unavailable");
    }

    @Override
    public long insertItemForPlayer(ServerPlayer player, ItemStack template, long count) {
        count = Math.max(0L, count);
        if (template == null || template.isEmpty() || count == 0L) return count;
        for (var inventory : inventories(player)) {
            long remaining = ItemUtil.insertItemByHandler(new InvWrapper(inventory), template, count);
            if (remaining < count) inventory.saveItemsToStack();
            count = remaining;
            if (count == 0L) break;
        }
        return count;
    }

    private static List<BackpackInventory> inventories(ServerPlayer player) {
        return player instanceof BackpackedInventoryAccess access
                ? access.backpacked$streamNonNullBackpackInventories().toList() : List.of();
    }
}
