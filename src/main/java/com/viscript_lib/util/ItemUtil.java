package com.viscript_lib.util;

import com.lowdragmc.lowdraglib2.registry.AutoRegistry;
import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import com.viscript_lib.ViScriptLibRegistries;
import com.viscript_lib.register.IContainerHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public class ItemUtil {
    //删除玩家物品，兼容背包，精妙背包，超越维度
    public static void removeItemForPlayer(ServerPlayer player, ItemStack itemStack, int count) {
        for (AutoRegistry.Holder<LDLRegister, IContainerHelper, Supplier<IContainerHelper>> containerHelperSupplierHolder : ViScriptLibRegistries.ContainerHelper) {
            IContainerHelper iContainerHelper = containerHelperSupplierHolder.value().get();
            if (count > 0) {
                count = iContainerHelper.removeItemStackByCount(player, itemStack, count);
            }

        }
    }

    //获取玩家物品，兼容背包，精妙背包，超越维度
    public static int getItemForPlayerCount(ServerPlayer player, ItemStack item) {
        int count = 0;
        if (player != null) {
            for (AutoRegistry.Holder<LDLRegister, IContainerHelper, Supplier<IContainerHelper>> containerHelperSupplierHolder : ViScriptLibRegistries.ContainerHelper) {
                IContainerHelper iContainerHelper = containerHelperSupplierHolder.value().get();
                count += iContainerHelper.getItemStackCount(player, item);
            }
        }
        return count;
    }

    /**
     * 获取物品数量
     *
     * @param container 背包
     * @param item      物品
     * @return 该物品在背包里的数量
     */
    public static int getItemCountByContainer(Container container, ItemStack item) {
        int count = 0;
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (ItemStack.isSameItemSameComponents(stack, item)) {
                count += stack.getCount();
            }
        }

        return count;
    }

    /**
     * 删除物品
     *
     * @param container 背包
     * @param item      要删的物品
     * @param count     要求数量
     * @return 删了后还有的数量
     */
    public static int removeItemByContainer(Container container, ItemStack item, int count) {
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (ItemStack.isSameItemSameComponents(stack, item)) {
                int toRemove = Math.min(count, stack.getCount());
                stack.shrink(toRemove);
                count -= toRemove;
            }
        }
        return count;
    }
}
