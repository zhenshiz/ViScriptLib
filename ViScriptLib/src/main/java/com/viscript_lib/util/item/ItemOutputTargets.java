package com.viscript_lib.util.item;

import com.lowdragmc.lowdraglib2.registry.AutoRegistry;
import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import com.viscript_lib.ViScriptLibRegistries;
import com.viscript_lib.register.IContainerHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * 提供基于 VSL 库存兼容注册表的物品输出目标查询和插入操作。
 *
 * <p>输出目标与物品查询、删除共享 {@link IContainerHelper} 扩展点。库存兼容实现只有在
 * {@link IContainerHelper#supportsItemOutput()} 返回 {@code true} 时才会出现在输出目标列表中。
 */
public final class ItemOutputTargets {
    /** 原版玩家背包 helper 的稳定注册名，同时也是默认输出目标 ID。 */
    public static final String PLAYER_INVENTORY = "inventory";

    /** “超越维度”维度背包 helper 的稳定注册名。 */
    public static final String BEYOND_DIMENSIONS = "beyonddimensions";

    /** Backpacked 已装备背包 helper 的稳定注册名。 */
    public static final String BACKPACKED = "backpacked";

    private ItemOutputTargets() {
    }

    /** 处理输出目标未接收的物品数量。 */
    @FunctionalInterface
    public interface OverflowHandler {
        /**
         * 接收目标容器溢出的物品，并返回仍未处理的数量。
         *
         * <p>每次发货至多调用一次，且数量始终大于零。模板是独立的单个物品副本，
         * 原始组件会保留；实现必须按 <code>count</code> 处理数量。
         *
         * @param player 接收物品的服务器玩家
         * @param template 提供物品和组件的单个物品模板
         * @param count 目标容器未接收的数量
         * @return 未处理的数量，范围为零到 <code>count</code>
         */
        long handle(ServerPlayer player, ItemStack template, long count);
    }

    /**
     * 返回按照注册优先级排列的全部可用输出实现类型。
     *
     * <p>此处的“可用”表示实现声明支持输出，并不表示目标对某个具体玩家已经开通。
     *
     * @return 不可修改的输出目标列表
     */
    public static List<IContainerHelper> values() {
        List<IContainerHelper> targets = new ArrayList<>();
        for (AutoRegistry.Holder<LDLRegister, IContainerHelper, Supplier<IContainerHelper>> holder
                : ViScriptLibRegistries.ContainerHelper) {
            IContainerHelper helper = holder.value().get();
            if (helper.supportsItemOutput()) {
                targets.add(helper);
            }
        }
        return List.copyOf(targets);
    }

    /**
     * 返回始终存在的原版玩家背包输出目标。
     *
     * @return 玩家背包 helper
     * @throws IllegalStateException 内置原版 helper 未注册或未声明支持输出时抛出
     */
    public static IContainerHelper playerInventory() {
        var holder = ViScriptLibRegistries.ContainerHelper.get(PLAYER_INVENTORY);
        if (holder == null) {
            throw new IllegalStateException("Missing built-in item output target: " + PLAYER_INVENTORY);
        }
        IContainerHelper helper = holder.value().get();
        if (!helper.supportsItemOutput()) {
            throw new IllegalStateException("Built-in inventory helper does not support item output");
        }
        return helper;
    }

    /**
     * 解析外部请求的输出目标。未知、未注册或不支持输出的 ID 会安全回退到玩家背包。
     *
     * @param targetId 请求的 {@link IContainerHelper#name()} 注册名
     * @return 对应输出目标或玩家背包目标
     */
    public static IContainerHelper resolve(String targetId) {
        var holder = targetId == null ? null : ViScriptLibRegistries.ContainerHelper.get(targetId);
        if (holder == null) return playerInventory();
        IContainerHelper helper = holder.value().get();
        return helper.supportsItemOutput() ? helper : playerInventory();
    }

    /**
     * 返回选择器中位于当前目标之后的输出目标。
     *
     * @param current 当前目标
     * @return 下一个输出目标；列表末尾会循环到第一个目标
     */
    public static IContainerHelper next(IContainerHelper current) {
        List<IContainerHelper> targets = values();
        if (targets.isEmpty()) return playerInventory();

        String currentId = current == null ? "" : current.name();
        for (int index = 0; index < targets.size(); index++) {
            if (Objects.equals(targets.get(index).name(), currentId)) {
                return targets.get((index + 1) % targets.size());
            }
        }
        return targets.getFirst();
    }

    /**
     * 把一个物品堆发送到指定输出目标。
     *
     * <p>物品堆的当前数量作为请求总量。目标容器装不下的部分在玩家脚下生成掉落物。
     * 未知或不可用的目标回退到玩家背包。
     *
     * @param player 接收物品的服务器玩家
     * @param targetId 请求的输出目标注册名
     * @param stack 待输出的物品堆
     * @return 既未插入容器、也未成功生成掉落物的数量；全部交付时为零
     */
    public static long giveItem(ServerPlayer player, String targetId, ItemStack stack) {
        if (stack == null) return 0L;
        return giveItem(player, targetId, stack, stack.getCount());
    }

    /**
     * 把指定总量的物品发送到输出目标，并将余量掉落在玩家脚下。
     *
     * <p><code>template</code> 只提供物品和组件信息，其当前堆叠数量不会改变请求数量。调用方若要求
     * “目标不可用时整个操作失败”，必须在改变业务状态前先调用
     * {@link IContainerHelper#isItemOutputAvailable(ServerPlayer)} 完成校验。未知或不可用目标回退
     * 到玩家背包；可用目标因容量不足返回的余量直接生成掉落物。掉落物保留组件，
     * 按最大堆叠数量拆分，并限制为该玩家拾取。
     *
     * @param player 接收物品的服务器玩家
     * @param targetId 请求的输出目标注册名
     * @param template 提供物品及组件信息的模板
     * @param count 待输出的总数量；小于或等于零时视为零
     * @return 既未插入容器、也未成功生成掉落物的数量；全部交付时为零
     */
    public static long giveItem(ServerPlayer player, String targetId, ItemStack template, long count) {
        return giveItem(player, targetId, template, count, ItemOutputTargets::dropAtPlayer);
    }

    /**
     * 把指定总量的物品发送到输出目标，并将余量交给指定回调。
     *
     * <p>未知或不可用的目标回退到玩家背包。容器只插入一次；存在余量时才调用回调。
     * 回调的返回值限制在零到余量之间，不会额外执行默认掉落逻辑。玩家或模板为空、
     * 模板物品为空或数量非正时，不插入物品、不调用回调，返回非负请求数量。
     *
     * @param player 接收物品的服务器玩家
     * @param targetId 请求的输出目标注册名
     * @param template 提供物品及组件信息的模板，不会被修改
     * @param count 待输出的总数量；小于或等于零时视为零
     * @param overflowHandler 处理未插入数量的非空回调
     * @return 容器和回调处理后仍未交付的数量
     */
    public static long giveItem(ServerPlayer player, String targetId, ItemStack template, long count,
                                OverflowHandler overflowHandler) {
        Objects.requireNonNull(overflowHandler, "overflowHandler");
        count = Math.max(0L, count);
        if (player == null || template == null || template.isEmpty() || count == 0L) return count;

        IContainerHelper target = resolve(targetId);
        if (!target.isItemOutputAvailable(player)) target = playerInventory();
        long remaining = clampRemaining(target.insertItemForPlayer(player, template.copyWithCount(1), count), count);
        if (remaining > 0L) {
            remaining = clampRemaining(overflowHandler.handle(player, template.copyWithCount(1), remaining), remaining);
        }
        return remaining;
    }

    private static long dropAtPlayer(ServerPlayer player, ItemStack template, long count) {
        int stackLimit = Math.max(1, template.getMaxStackSize());
        while (count > 0L) {
            int batch = (int) Math.min(count, stackLimit);
            var dropped = new ItemEntity(player.serverLevel(), player.getX(), player.getY() + 0.1, player.getZ(),
                    template.copyWithCount(batch), 0, 0, 0);
            dropped.setDefaultPickUpDelay();
            dropped.setTarget(player.getUUID());
            if (!player.serverLevel().addFreshEntity(dropped)) break;
            count -= batch;
        }
        return count;
    }

    private static long clampRemaining(long remaining, long requested) {
        return Math.clamp(remaining, 0L, requested);
    }
}
