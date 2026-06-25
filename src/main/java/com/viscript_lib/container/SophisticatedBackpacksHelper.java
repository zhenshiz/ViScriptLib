package com.viscript_lib.container;

import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import com.viscript_lib.register.IContainerHelper;
import com.viscript_lib.util.item.ItemStackCompareMode;
import com.viscript_lib.util.item.ItemUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.p3pp3rf1y.sophisticatedbackpacks.SophisticatedBackpacks;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackStorage;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.BackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.IBackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.common.gui.BackpackContext;
import net.p3pp3rf1y.sophisticatedbackpacks.network.BackpackContentsPayload;
import net.p3pp3rf1y.sophisticatedbackpacks.util.PlayerInventoryProvider;
import net.p3pp3rf1y.sophisticatedcore.inventory.InventoryHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * 精妙背包兼容
 */
@LDLRegister(name = SophisticatedBackpacks.MOD_ID, registry = IContainerHelper.CONTAINER_HELPER_ID, modID = SophisticatedBackpacks.MOD_ID)
public class SophisticatedBackpacksHelper implements IContainerHelper {
    @Override
    public int getItemStackCount(ServerPlayer player, ItemStack item) {
        return getItemStackCount(player, item, ItemStackCompareMode.ALL_COMPONENTS, List.of());
    }

    @Override
    public int getItemStackCount(ServerPlayer player, ItemStack item,
                                 ItemStackCompareMode compareMode,
                                 List<DataComponentType<?>> components) {
        int count = 0;
        for (ItemStack itemStack : getItemsFromInventoryBackpack(player)) {
            if (ItemUtil.isSameItem(itemStack, item, compareMode, components)) {
                count += itemStack.getCount();
            }
        }
        return count;
    }

    //从精妙背包中扣除指定物品
    @Override
    public int removeItemStackByCount(ServerPlayer player, ItemStack item, int count) {
        return removeItemStackByCount(player, item, count, ItemStackCompareMode.ALL_COMPONENTS, List.of());
    }

    @Override
    public int removeItemStackByCount(ServerPlayer player, ItemStack item, int count,
                                      ItemStackCompareMode compareMode,
                                      List<DataComponentType<?>> components) {
        if (count <= 0) return 0;

        final int[] remain = {count};
        PlayerInventoryProvider.get().runOnBackpacks(player, (backpack, inventoryName, identifier, index) -> {
            final boolean[] changed = {false};
            IBackpackWrapper wrapper = BackpackWrapper.fromStack(backpack);
            InventoryHandler inventoryHandler = wrapper.getInventoryHandler();
            for (int i = 0; i < inventoryHandler.getSlots(); i++) {
                if (remain[0] <= 0) break;
                ItemStack stackInSlot = inventoryHandler.getStackInSlot(i);
                if (ItemUtil.isSameItem(stackInSlot, item, compareMode, components)) {
                    int canRemove = Math.min(stackInSlot.getCount(), remain[0]);
                    ItemStack removed = inventoryHandler.extractItem(i, canRemove, false);
                    remain[0] -= removed.getCount();
                    changed[0] |= !removed.isEmpty();
                }
            }
            if (changed[0]) {
                syncBackpackContents(player, wrapper);
            }
            return remain[0] <= 0;
        });

        return remain[0];
    }

    //获取玩家所有背包中所有的物品，不包括玩家物品栏
    public static List<ItemStack> getItemsFromInventoryBackpack(Player player) {
        List<ItemStack> items = new ArrayList<>();
        PlayerInventoryProvider.get().runOnBackpacks(player, (backpack, inventoryName, identifier, index) -> {
            addHandlerItems(items, BackpackWrapper.fromStack(backpack).getInventoryHandler());
            return false;
        });
        return items;
    }


    //获取玩家背包中所有的背包
    public static List<ItemStack> getAllInventoryBackpack(Player player) {
        List<ItemStack> items = new ArrayList<>();
        PlayerInventoryProvider.get().runOnBackpacks(player, (backpack, inventoryName, identifier, index) -> {
            items.add(backpack);
            return false;
        });
        return items;
    }

    //获取背包中所有的物品
    public static List<ItemStack> getItemsFromBackpackItem(ItemStack itemStack) {
        List<ItemStack> items = new ArrayList<>();
        BackpackWrapper.fromExistingData(itemStack)
                .ifPresent(wrapper -> addHandlerItems(items, wrapper.getInventoryHandler()));
        return items;
    }

    public static void modifyInventoryBackpack(ServerPlayer player, ItemStack backpackItem, Consumer<IItemHandler> action) {
        PlayerInventoryProvider.get().runOnBackpacks(player, (backpack, inventoryName, identifier, index) -> {
            if (!ItemStack.isSameItemSameComponents(backpack, backpackItem)) return false;
            modifyBackpack(player, BackpackWrapper.fromStack(backpack), action);
            return false;
        });
    }

    public static void modifyBackpack(ServerPlayer player, BackpackContext backpackContext, Consumer<IItemHandler> action) {
        IBackpackWrapper wrapper = backpackContext.getBackpackWrapper(player);
        if (wrapper == IBackpackWrapper.Noop.INSTANCE) return;
        modifyBackpack(player, wrapper, action);
    }

    private static void modifyBackpack(ServerPlayer player, IBackpackWrapper wrapper, Consumer<IItemHandler> action) {
        InventoryHandler inventoryHandler = wrapper.getInventoryHandler();
        action.accept(inventoryHandler);
        syncBackpackContents(player, wrapper);
    }

    private static void addHandlerItems(List<ItemStack> items, IItemHandler handler) {
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack item = handler.getStackInSlot(i);
            if (!item.isEmpty()) {
                items.add(item.copy());
            }
        }
    }

    private static void syncBackpackContents(ServerPlayer player, IBackpackWrapper wrapper) {
        UUID uuid = wrapper.getContentsUuid().orElse(null);
        if (uuid == null) return;
        CompoundTag backpackContent = BackpackStorage.get().getOrCreateBackpackContents(uuid);
        player.connection.send(new BackpackContentsPayload(uuid, backpackContent));
    }
}
