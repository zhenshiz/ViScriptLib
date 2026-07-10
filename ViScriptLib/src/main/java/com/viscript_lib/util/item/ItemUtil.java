package com.viscript_lib.util.item;

import com.viscript_lib.ViScriptLibRegistries;
import com.viscript_lib.register.IContainerHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ItemUtil {
    //删除玩家物品，兼容背包，精妙背包，超越维度等库存模组
    public static int removeItemForPlayer(ServerPlayer player, ItemStack itemStack, int count) {
        return removeItemForPlayer(player, itemStack, count, ItemStackCompareMode.ALL_COMPONENTS, List.of());
    }

    /**
     * 按指定物品组件比较模式删除玩家物品。
     *
     * @param player 玩家
     * @param itemStack 要删除的物品
     * @param count 要删除的数量
     * @param compareMode 物品比较模式
     * @param components 参与或排除比较的组件列表，语义由 compareMode 决定
     */
    public static int removeItemForPlayer(ServerPlayer player, ItemStack itemStack, int count,
                                           ItemStackCompareMode compareMode,
                                           List<String> components) {
        for (var containerHelperSupplierHolder : ViScriptLibRegistries.ContainerHelper) {
            IContainerHelper iContainerHelper = containerHelperSupplierHolder.value().get();
            if (count > 0) {
                try {
                    count = iContainerHelper.removeItemStackByCount(player, itemStack, count, compareMode, components);
                } catch (Throwable ignored) {
                }
            }
        }
        return count;
    }

    //获取玩家物品，兼容背包，精妙背包，超越维度等库存模组
    public static int getItemForPlayerCount(ServerPlayer player, ItemStack item) {
        return getItemForPlayerCount(player, item, ItemStackCompareMode.ALL_COMPONENTS, List.of());
    }

    /**
     * 按指定物品组件比较模式统计玩家物品数量。
     *
     * @param player 玩家
     * @param item 物品
     * @param compareMode 物品比较模式
     * @param components 参与或排除比较的组件列表，语义由 compareMode 决定
     * @return 玩家持有的匹配物品数量
     */
    public static int getItemForPlayerCount(ServerPlayer player, ItemStack item,
                                            ItemStackCompareMode compareMode,
                                            List<String> components) {
        int count = 0;
        if (player != null) {
            for (var containerHelperSupplierHolder : ViScriptLibRegistries.ContainerHelper) {
                IContainerHelper iContainerHelper = containerHelperSupplierHolder.value().get();
                try {
                    count += iContainerHelper.getItemStackCount(player, item, compareMode, components);
                } catch (Throwable ignored) {
                }
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
        return getItemCountByContainer(container, item, ItemStackCompareMode.ALL_COMPONENTS, List.of());
    }

    /**
     * 按指定物品组件比较模式获取容器中的物品数量。
     *
     * @param container 背包
     * @param item 物品
     * @param compareMode 物品比较模式
     * @param components 参与或排除比较的组件列表，语义由 compareMode 决定
     * @return 该物品在背包里的数量
     */
    public static int getItemCountByContainer(Container container, ItemStack item,
                                              ItemStackCompareMode compareMode,
                                              List<String> components) {
        int count = 0;
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (isSameItem(stack, item, compareMode, components)) {
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
        return removeItemByContainer(container, item, count, ItemStackCompareMode.ALL_COMPONENTS, List.of());
    }

    /**
     * 按指定物品组件比较模式删除容器中的物品。
     *
     * @param container 背包
     * @param item 要删的物品
     * @param count 要求数量
     * @param compareMode 物品比较模式
     * @param components 参与或排除比较的组件列表，语义由 compareMode 决定
     * @return 删了后还有的数量
     */
    public static int removeItemByContainer(Container container, ItemStack item, int count,
                                            ItemStackCompareMode compareMode,
                                            List<String> components) {
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (isSameItem(stack, item, compareMode, components)) {
                int toRemove = Math.min(count, stack.getCount());
                stack.shrink(toRemove);
                count -= toRemove;
            }
        }
        return count;
    }

    /**
     * 根据比较模式判断两个物品是否相同。
     *
     * @param itemA 第一个物品
     * @param itemB 第二个物品
     * @param compareMode 物品比较模式，传空时默认比较所有组件
     * @param components 参与或排除比较的组件列表，语义由 compareMode 决定
     * @return 符合比较规则时返回 <code>true</code>
     */
    public static boolean isSameItem(ItemStack itemA, ItemStack itemB,
                                     ItemStackCompareMode compareMode,
                                     List<String> components) {
        ItemStackCompareMode mode = compareMode == null ? ItemStackCompareMode.ALL_COMPONENTS : compareMode;
        return switch (mode) {
            case ALL_COMPONENTS -> isSameItemWithAllComponents(itemA, itemB);
            case INCLUDE_COMPONENTS -> isSameItemWithOnlyComponents(itemA, itemB, components);
            case EXCLUDE_COMPONENTS -> isSameItemExcludingComponents(itemA, itemB, components);
        };
    }

    /**
     * 比较两个物品是否相同，但忽略指定物品组件。
     *
     * <p>常用于原版工具、武器这类场景，比如传入 <code>DataComponents.DAMAGE</code>
     * 后，耐久不同的同款物品也会被视为相同。
     *
     * @param itemA 第一个物品
     * @param itemB 第二个物品
     * @param excludedComponents 不参与比较的物品组件
     * @return 物品类型相同，且未排除的组件都相同时返回 <code>true</code>
     */
    public static boolean isSameItemExcludingComponents(ItemStack itemA, ItemStack itemB,
                                                        List<String> excludedComponents) {
        if (shouldFailItemComparison(itemA, itemB)) {
            return false;
        }

        Set<String> excluded = toComponentSet(excludedComponents);
        Set<String> componentTypes = getNbt(itemA).getAllKeys();
        componentTypes.addAll(getNbt(itemB).getAllKeys());

        for (String component : componentTypes) {
            if (!excluded.contains(component) && !Objects.equals(getNbt(itemA).get(component), getNbt(itemB).get(component))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 比较两个物品是否相同，并且只比较指定物品组件。
     *
     * <p>常用于模组把关键身份信息放在组件里的场景，比如枪械的 gun id。
     * 没有传入的其它组件不会影响比较结果。
     *
     * @param itemA 第一个物品
     * @param itemB 第二个物品
     * @param includedComponents 需要参与比较的物品组件
     * @return 物品类型相同，且指定组件都相同时返回 <code>true</code>
     */
    public static boolean isSameItemWithOnlyComponents(ItemStack itemA, ItemStack itemB,
                                                       List<String> includedComponents) {
        if (shouldFailItemComparison(itemA, itemB)) {
            return false;
        }

        for (String component : toComponentSet(includedComponents)) {
            if (!Objects.equals(getNbt(itemA).get(component), getNbt(itemB).get(component))) {
                return false;
            }
        }
        return true;
    }

    private static boolean isSameItemWithAllComponents(ItemStack itemA, ItemStack itemB) {
        if (itemA == null || itemB == null) {
            return false;
        }
        return ItemStack.isSameItemSameTags(itemA, itemB);
    }

    private static boolean shouldFailItemComparison(ItemStack itemA, ItemStack itemB) {
        if (itemA == null || itemB == null) {
            return true;
        }
        if (itemA == itemB) {
            return false;
        }
        if (itemA.isEmpty() || itemB.isEmpty()) {
            return !itemA.isEmpty() || !itemB.isEmpty();
        }
        return !ItemStack.isSameItem(itemA, itemB);
    }

    private static Set<String> toComponentSet(List<String> components) {
        if (components == null || components.isEmpty()) {
            return Set.of();
        }
        return new HashSet<>(components);
    }
    
    /**避免愚蠢的{@link ItemStack#getOrCreateTag()}给没有nbt标签的物品塞一个空的nbt*/
    public static CompoundTag getNbt(ItemStack stack) {
        return stack.getTag() == null ? new CompoundTag() : stack.getTag();
    }
}
