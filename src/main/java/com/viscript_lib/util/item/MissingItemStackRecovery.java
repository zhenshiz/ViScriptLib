package com.viscript_lib.util.item;

import com.lowdragmc.lowdraglib2.utils.LDLibExtraCodecs;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 为反序列化期间遇到的缺失物品提供作用域回调。
 *
 * <p>{@link #CODEC} 与 LDLib2 的物品栈 Codec 保持相同的正常编解码行为。只有输入中的
 * 物品 ID 未注册，并且当前线程通过 {@link #withHandler(MissingItemHandler, RecoveryOperation)}
 * 安装了回调时，才会使用回调返回的物品栈继续反序列化。回调仅在对应操作执行期间生效，
 * 不会改变游戏其它位置加载物品栈时的容错规则。通过 LDLib2 默认访问器反序列化的
 * {@link ItemStack} 字段会在该作用域中自动使用此 Codec。
 */
public final class MissingItemStackRecovery {
    private static final ThreadLocal<Deque<MissingItemHandler>> HANDLERS = new ThreadLocal<>();

    /**
     * 支持缺失物品恢复回调的物品栈 Codec。
     *
     * <p>直接调用 Mojang Codec 的自定义序列化器应使用本字段代替
     * {@link ItemStack#OPTIONAL_CODEC}。通过 LDLib2 默认访问器持久化的物品栈会在恢复作用域中
     * 自动使用本字段，不需要逐个字段指定 Codec。ViScript Lib 不会替换 LDLib2 的全局
     * {@link ItemStack} Accessor。
     */
    public static final Codec<ItemStack> CODEC = new Codec<>() {
        @Override
        public <T> DataResult<Pair<ItemStack, T>> decode(DynamicOps<T> ops, T input) {
            if (!isRecoveryActive()) {
                return LDLibExtraCodecs.ITEM_STACK.decode(ops, input);
            }

            var recovered = recoverSerializedStack(ops, input);
            if (recovered.isPresent()) {
                return DataResult.success(Pair.of(recovered.get(), input));
            }
            return LDLibExtraCodecs.ITEM_STACK.decode(ops, input);
        }

        @Override
        public <T> DataResult<T> encode(ItemStack input, DynamicOps<T> ops, T prefix) {
            return LDLibExtraCodecs.ITEM_STACK.encode(input, ops, prefix);
        }

        @Override
        public String toString() {
            return "ViScriptMissingItemStackRecoveryCodec";
        }
    };

    private MissingItemStackRecovery() {
    }

    /**
     * 在指定操作期间安装缺失物品回调。
     *
     * <p>回调按当前线程和嵌套层级隔离。嵌套调用优先使用最内层回调，并且无论操作正常返回
     * 还是抛出异常，回调都会被移除。
     *
     * @param  handler 发现缺失物品时调用的处理器
     * @param  operation 需要执行的反序列化操作
     * @param  <T> 操作返回值类型
     * @param  <E> 操作可能抛出的异常类型
     * @return 操作返回值
     * @throws E 操作抛出的异常
     */
    public static <T, E extends Exception> T withHandler(MissingItemHandler handler,
                                                         RecoveryOperation<T, E> operation) throws E {
        Objects.requireNonNull(handler, "handler");
        Objects.requireNonNull(operation, "operation");

        var handlers = HANDLERS.get();
        if (handlers == null) {
            handlers = new ArrayDeque<>();
            HANDLERS.set(handlers);
        }
        handlers.push(handler);
        try {
            return operation.run();
        } finally {
            handlers.pop();
            if (handlers.isEmpty()) {
                HANDLERS.remove();
            }
        }
    }

    /**
     * 调用当前作用域中的缺失物品处理器。
     *
     * <p>自定义序列化器在确认物品 ID 不存在后，可以调用本方法接入编辑器安装的恢复回调。
     * 处理器返回 {@code null} 时视为拒绝恢复。
     *
     * @param  context 缺失物品 ID 和原始序列化数据
     * @return 处理器提供的替代物品栈；没有处理器或处理器拒绝恢复时为空
     */
    public static Optional<ItemStack> recover(MissingItemContext context) {
        Objects.requireNonNull(context, "context");
        var handlers = HANDLERS.get();
        if (handlers == null || handlers.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(handlers.peek().recover(context));
    }

    /**
     * 尝试从序列化输入中恢复未注册的物品栈。
     *
     * <p>此方法只在当前线程存在恢复处理器，并且输入包含未注册物品 ID 时调用处理器。正常物品、
     * 非物品栈格式和处理器拒绝恢复的输入均返回空值。
     *
     * @param  ops 输入数据使用的动态操作
     * @param  input 待检查的序列化输入
     * @param  <T> 序列化输入类型
     * @return 处理器提供的替代物品栈；输入不需要恢复时为空
     */
    public static <T> Optional<ItemStack> recoverSerializedStack(DynamicOps<T> ops, T input) {
        Objects.requireNonNull(ops, "ops");
        Objects.requireNonNull(input, "input");
        if (!isRecoveryActive()) {
            return Optional.empty();
        }

        var missingItemId = findMissingItemId(ops, input);
        return missingItemId == null
                ? Optional.empty()
                : recover(new MissingItemContext(missingItemId, toNbt(ops, input)));
    }

    /**
     * 返回当前线程是否处于缺失物品恢复操作中。
     *
     * @return 当前线程存在恢复处理器时返回 {@code true}
     */
    public static boolean isRecoveryActive() {
        var handlers = HANDLERS.get();
        return handlers != null && !handlers.isEmpty();
    }

    /**
     * 创建表示缺失物品的屏障占位符。
     *
     * <p>占位符名称为原始物品 ID，Lore 会提示该物品不存在。占位符不保留原物品数量和组件，
     * 保存包含此占位符的数据会产生破坏性变更。
     *
     * @param  context 缺失物品上下文
     * @return 带缺失提示的屏障物品栈
     */
    public static ItemStack createBarrierPlaceholder(MissingItemContext context) {
        Objects.requireNonNull(context, "context");
        return createBarrierPlaceholder(context.itemId());
    }

    /**
     * 创建表示指定缺失物品 ID 的屏障占位符。
     *
     * @param  itemId 原始物品 ID
     * @return 带缺失提示的屏障物品栈
     */
    public static ItemStack createBarrierPlaceholder(ResourceLocation itemId) {
        Objects.requireNonNull(itemId, "itemId");
        var placeholder = new ItemStack(Items.BARRIER);
        placeholder.set(DataComponents.CUSTOM_NAME,
                Component.literal(itemId.toString()).withStyle(ChatFormatting.RED));
        placeholder.set(DataComponents.LORE, new ItemLore(List.of(
                Component.translatable("viscript_lib.missing_item.placeholder_lore")
                        .withStyle(style -> style
                                .withColor(ChatFormatting.RED)
                                .withItalic(false))
        )));
        return placeholder;
    }

    private static <T> ResourceLocation findMissingItemId(DynamicOps<T> ops, T input) {
        var map = ops.getMap(input).result();
        if (map.isEmpty()) return null;

        var idValue = map.get().get("id");
        if (idValue == null) return null;

        var idText = ops.getStringValue(idValue).result();
        if (idText.isEmpty()) return null;

        var itemId = ResourceLocation.tryParse(idText.get());
        return itemId != null && !BuiltInRegistries.ITEM.containsKey(itemId) ? itemId : null;
    }

    private static <T> Tag toNbt(DynamicOps<T> ops, T input) {
        return ops.convertTo(NbtOps.INSTANCE, input);
    }

    /**
     * 描述一次缺失物品栈解码。
     *
     * @param itemId 原始物品 ID
     * @param serializedStack 原始物品栈序列化数据的 NBT 表示
     */
    public record MissingItemContext(ResourceLocation itemId, Tag serializedStack) {
        /**
         * 创建缺失物品上下文。
         *
         * @param itemId 原始物品 ID
         * @param serializedStack 原始物品栈序列化数据的 NBT 表示
         */
        public MissingItemContext {
            Objects.requireNonNull(itemId, "itemId");
            Objects.requireNonNull(serializedStack, "serializedStack");
        }
    }

    /**
     * 处理反序列化期间发现的缺失物品。
     */
    @FunctionalInterface
    public interface MissingItemHandler {
        /**
         * 返回用于继续反序列化的替代物品栈。
         *
         * @param  context 缺失物品上下文
         * @return 替代物品栈；返回 {@code null} 表示拒绝恢复
         */
        @Nullable
        ItemStack recover(MissingItemContext context);
    }

    /**
     * 表示需要在缺失物品回调作用域中执行的操作。
     *
     * @param <T> 操作返回值类型
     * @param <E> 操作可能抛出的异常类型
     */
    @FunctionalInterface
    public interface RecoveryOperation<T, E extends Exception> {
        /**
         * 执行操作。
         *
         * @return 操作返回值
         * @throws E 操作抛出的异常
         */
        T run() throws E;
    }
}
