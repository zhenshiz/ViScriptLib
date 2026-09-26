package com.viscript_lib.register;

import com.lowdragmc.lowdraglib2.registry.ILDLRegister;
import com.viscript_lib.ViScriptLib;
import com.viscript_lib.util.item.ItemStackCompareMode;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.List;
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
    long getItemStackCount(ServerPlayer player, ItemStack item);

    /**
     * 按指定物品组件比较模式获取物品数量。
     *
     * @param player 玩家
     * @param item 物品
     * @param compareMode 物品比较模式
     * @param nbtKeys 参与或排除比较的nbt键列表，语义由 compareMode 决定
     * @return 该物品的数量
     */
    default long getItemStackCount(ServerPlayer player, ItemStack item,
                                  ItemStackCompareMode compareMode,
                                  List<String> nbtKeys) {
        return getItemStackCount(player, item);
    }

    /**
     * 删除物品
     *
     * @param player 玩家
     * @param item   物品
     * @param count  要删除的物品数量
     * @return 删除后剩余的数量
     */
    long removeItemStackByCount(ServerPlayer player, ItemStack item, long count);

    /**
     * 按指定物品组件比较模式删除物品。
     *
     * @param player 玩家
     * @param item 物品
     * @param count 要删除的物品数量
     * @param compareMode 物品比较模式
     * @param nbtKeys 参与或排除比较的nbt键列表，语义由 compareMode 决定
     * @return 删除后剩余的数量
     */
    default long removeItemStackByCount(ServerPlayer player, ItemStack item, long count,
                                       ItemStackCompareMode compareMode,
                                       List<String> nbtKeys) {
        return removeItemStackByCount(player, item, count);
    }


    /**
     * 判断该库存兼容是否可以作为一个显式的物品输出目标。
     *
     * <p>查询和删除能力默认不会自动暴露为输出目标。只有能够明确、安全地接收物品的实现
     * 才应覆盖此方法并返回 {@code true}。
     *
     * @return 可以在输出目标选择器中使用时返回 {@code true}
     */
    default boolean supportsItemOutput() {
        return false;
    }

    /**
     * 返回输出目标选择器中显示的图标。
     *
     * @return 输出目标图标；不支持输出的实现可保留默认空物品堆
     */
    default ItemStack getItemOutputIcon() {
        return ItemStack.EMPTY;
    }

    /**
     * 返回输出目标名称的翻译键。
     *
     * @return 输出目标名称翻译键
     */
    default String getItemOutputTranslationKey() {
        return "viscript_lib.item_output_target." + name();
    }

    /**
     * 判断当前玩家现在是否可以使用该输出目标。
     *
     * @param player 接收物品的服务器玩家
     * @return 当前目标可用时返回 {@code true}
     */
    default boolean isItemOutputAvailable(ServerPlayer player) {
        return supportsItemOutput();
    }

    /**
     * 返回当前输出目标不可用的原因，不包含调用方自己的业务结果描述。
     *
     * <p>例如商店可以在该原因后补充“购买已取消”，而其它 VSL 使用方可以采用自己的处理方式。
     *
     * @param player 尝试使用输出目标的服务器玩家
     * @return 可直接显示或嵌入其它消息的不可用原因
     */
    default Component getItemOutputUnavailableReason(ServerPlayer player) {
        return Component.translatable(
                "viscript_lib.item_output_target.unavailable",
                Component.translatable(getItemOutputTranslationKey())
        );
    }

    /**
     * 尝试将指定总量的物品插入此输出目标。
     *
     * <p><code>template</code> 只提供物品及组件信息，其堆叠数量不作为请求数量。实现可以按底层
     * 库存能力分批插入，但不得生成世界掉落物，也不得静默丢弃未插入的部分。默认实现不执行
     * 插入并返回完整请求数量。
     *
     * @param player 接收物品的服务器玩家
     * @param template 待插入物品的模板
     * @param count 待插入的总数量；小于或等于零时视为零
     * @return 未能插入的剩余数量
     */
    default long insertItemForPlayer(ServerPlayer player, ItemStack template, long count) {
        return Math.max(0L, count);
    }
}
