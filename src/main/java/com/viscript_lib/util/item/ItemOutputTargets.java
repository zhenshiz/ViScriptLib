package com.viscript_lib.util.item;

import com.lowdragmc.lowdraglib2.registry.AutoRegistry;
import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import com.viscript_lib.ViScriptLibRegistries;
import com.viscript_lib.register.IContainerHelper;
import net.minecraft.server.level.ServerPlayer;
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

    private ItemOutputTargets() {
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
     * 把物品发送到指定输出目标，并将发放阶段未插入的部分交给玩家背包处理。
     *
     * <p>调用方若要求“目标不可用时整个操作失败”，必须在改变业务状态前先调用
     * {@link IContainerHelper#isItemOutputAvailable(ServerPlayer)} 完成校验。这里的背包回退只用于处理
     * 校验后目标失效或目标容量变化等发放阶段竞态，避免已经生成的物品丢失。
     *
     * @param player 接收物品的服务器玩家
     * @param targetId 请求的输出目标注册名
     * @param stack 待输出的物品堆
     */
    public static void giveItem(ServerPlayer player, String targetId, ItemStack stack) {
        if (player == null || stack == null || stack.isEmpty()) return;

        IContainerHelper fallback = playerInventory();
        IContainerHelper target = resolve(targetId);
        ItemStack remaining = stack.copy();
        if (target.isItemOutputAvailable(player)) {
            remaining = Objects.requireNonNullElse(target.insertItemForPlayer(player, remaining), remaining);
        }
        if (!remaining.isEmpty() && !Objects.equals(target.name(), fallback.name())) {
            fallback.insertItemForPlayer(player, remaining);
        }
    }
}
