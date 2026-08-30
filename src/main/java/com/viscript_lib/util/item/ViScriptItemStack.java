package com.viscript_lib.util.item;

import com.lowdragmc.lowdraglib2.Platform;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * 表示供 ViScript 持久化数据和用户界面使用的容错物品栈。
 *
 * <p>{@link #CODEC} 使用与原版物品栈相同的 {@code id}、{@code count} 和
 * {@code components} 字段，因此原先直接保存的物品栈可以迁移到此类型。非空物品栈的
 * 数量范围为 {@code 1} 到 {@link Integer#MAX_VALUE}，不依赖原版 Codec 声明的
 * {@code 99} 上限。
 *
 * <p>输入中的物品 ID 未注册时，此类型会显示屏障占位符，并保留完整的原始序列化数据。
 * 只要调用方没有使用新的原版物品栈替换此对象，再次保存就会原样写回缺失物品的
 * ID、数量和组件；重新安装对应模组后可在下次载入时恢复。
 *
 * <p>此类型只负责 ViScript 数据边界。游戏逻辑需要使用原版物品栈 API 时，应通过
 * {@link #toItemStack()} 取得独立副本。
 */
public final class ViScriptItemStack {
    private static final ThreadLocal<Deque<Consumer<ResourceLocation>>> MISSING_ITEM_LISTENERS =
            new ThreadLocal<>();
    private static final Codec<ItemStack> UNBOUNDED_STACK_CODEC = Codec.lazyInitialized(
            () -> RecordCodecBuilder.create(instance -> instance.group(
                    ItemStack.ITEM_NON_AIR_CODEC.fieldOf("id").forGetter(ItemStack::getItemHolder),
                    ExtraCodecs.intRange(1, Integer.MAX_VALUE)
                            .fieldOf("count")
                            .orElse(1)
                            .forGetter(ItemStack::getCount),
                    DataComponentPatch.CODEC
                            .optionalFieldOf("components", DataComponentPatch.EMPTY)
                            .forGetter(ItemStack::getComponentsPatch)
            ).apply(instance, ItemStack::new))
    );
    private static final Codec<ItemStack> OPTIONAL_UNBOUNDED_STACK_CODEC =
            ExtraCodecs.optionalEmptyMap(UNBOUNDED_STACK_CODEC).xmap(
                    optional -> optional.orElse(ItemStack.EMPTY),
                    stack -> stack.isEmpty() ? Optional.empty() : Optional.of(stack)
            );

    /**
     * 使用无损缺失物品回退和无界正整数数量的物品栈 Codec。
     */
    public static final Codec<ViScriptItemStack> CODEC = new Codec<>() {
        @Override
        public <T> DataResult<Pair<ViScriptItemStack, T>> decode(DynamicOps<T> ops, T input) {
            var map = ops.getMap(input).result();
            if (map.isPresent()) {
                var idValue = map.get().get("id");
                if (idValue != null) {
                    var idResult = ResourceLocation.CODEC.parse(ops, idValue);
                    if (idResult.isError()) {
                        return idResult.map(id -> Pair.of(new ViScriptItemStack(), ops.empty()));
                    }

                    var itemId = idResult.result().orElseThrow();
                    if (!BuiltInRegistries.ITEM.containsKey(itemId)) {
                        return decodeMissingStack(ops, input, itemId, map.get().get("count"));
                    }
                }
            }

            return decodeKnownStack(ops, input)
                    .map(pair -> pair.mapFirst(ViScriptItemStack::new));
        }

        @Override
        public <T> DataResult<T> encode(ViScriptItemStack input, DynamicOps<T> ops, T prefix) {
            Objects.requireNonNull(input, "input");
            if (input.missingSerializedStack != null) {
                var serialized = NbtOps.INSTANCE.convertTo(ops, input.missingSerializedStack);
                return ops.getMap(serialized).flatMap(map -> ops.mergeToMap(prefix, map));
            }
            return encodeKnownStack(input.itemStack, ops, prefix);
        }

        @Override
        public String toString() {
            return "ViScriptItemStackCodec";
        }
    };

    /**
     * 使用 {@link #CODEC} 在注册表友好缓冲区中传输物品栈的流 Codec。
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, ViScriptItemStack> STREAM_CODEC =
            ByteBufCodecs.fromCodecWithRegistriesTrusted(CODEC);

    private ItemStack itemStack;
    @Nullable
    private ResourceLocation missingItemId;
    @Nullable
    private CompoundTag missingSerializedStack;

    /**
     * 创建一个空物品栈包装。
     */
    public ViScriptItemStack() {
        this(ItemStack.EMPTY);
    }

    /**
     * 创建指定物品栈的独立包装副本。
     *
     * @param itemStack 需要包装的物品栈
     */
    public ViScriptItemStack(ItemStack itemStack) {
        this.itemStack = Objects.requireNonNull(itemStack, "itemStack").copy();
    }

    private ViScriptItemStack(ItemStack itemStack, ResourceLocation missingItemId,
                              CompoundTag missingSerializedStack) {
        this.itemStack = itemStack;
        this.missingItemId = missingItemId;
        this.missingSerializedStack = missingSerializedStack;
    }

    /**
     * 在指定操作期间监听解码出的未注册物品 ID。
     *
     * <p>监听器按当前线程和嵌套层级隔离，并且无论操作正常返回还是抛出异常都会被移除。
     * 监听只提供通知，不改变 {@link #CODEC} 的容错结果；没有监听器时，缺失物品仍会自动
     * 解码为保留原始数据的屏障占位符。
     *
     * @param  listener 接收未注册物品 ID 的监听器
     * @param  operation 需要执行的解码或数据加载操作
     * @param  <T> 操作返回值类型
     * @return 操作返回值
     * @throws Exception 操作抛出的异常
     */
    public static <T> T withMissingItemListener(Consumer<ResourceLocation> listener,
                                                Callable<T> operation) throws Exception {
        Objects.requireNonNull(listener, "listener");
        Objects.requireNonNull(operation, "operation");

        var listeners = MISSING_ITEM_LISTENERS.get();
        if (listeners == null) {
            listeners = new ArrayDeque<>();
            MISSING_ITEM_LISTENERS.set(listeners);
        }
        listeners.push(listener);
        try {
            return operation.call();
        } finally {
            listeners.pop();
            if (listeners.isEmpty()) {
                MISSING_ITEM_LISTENERS.remove();
            }
        }
    }

    /**
     * 转换为当前用于编辑、显示或游戏逻辑的原版物品栈。
     *
     * <p>缺失物品返回屏障占位符。修改返回值不会改变此对象；需要替换持久化内容时应调用
     * {@link #ViScriptItemStack(ItemStack)} 创建新的包装。
     *
     * @return 当前物品栈或屏障占位符的独立副本
     */
    public ItemStack toItemStack() {
        return itemStack.copy();
    }

    /**
     * 创建具有指定数量的独立包装副本。
     *
     * <p>正数最高可设置为 {@link Integer#MAX_VALUE}。缺失物品的原始序列化数据会同步更新；
     * {@code 0} 或负数会返回空物品栈包装。
     *
     * @param count 新的物品数量
     * @return 具有指定数量的独立包装副本
     */
    public ViScriptItemStack copyWithCount(int count) {
        if (count <= 0) {
            return new ViScriptItemStack();
        }

        if (missingSerializedStack == null || missingItemId == null) {
            var copy = itemStack.copy();
            copy.setCount(count);
            return new ViScriptItemStack(copy);
        }

        var placeholder = itemStack.copy();
        placeholder.setCount(count);
        var serialized = missingSerializedStack.copy();
        serialized.putInt("count", count);
        return new ViScriptItemStack(placeholder, missingItemId, serialized);
    }

    /**
     * 返回当前是否正在显示未注册物品的占位符。
     *
     * @return 已保留缺失物品原始数据时返回 {@code true}
     */
    public boolean isMissingItem() {
        return missingSerializedStack != null;
    }

    /**
     * 返回当前序列化物品 ID。
     *
     * <p>缺失物品返回保留的原始 ID，而不是屏障 ID。
     *
     * @return 当前非空物品的注册表 ID；空物品栈时为空
     */
    public Optional<ResourceLocation> getItemId() {
        if (missingItemId != null) {
            return Optional.of(missingItemId);
        }
        if (itemStack.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(BuiltInRegistries.ITEM.getKey(itemStack.getItem()));
    }

    private static <T> DataResult<Pair<ViScriptItemStack, T>> decodeMissingStack(
            DynamicOps<T> ops, T input, ResourceLocation itemId, @Nullable T countValue) {
        var countResult = countValue == null
                ? DataResult.success(1)
                : Codec.INT.parse(ops, countValue)
                .flatMap(count -> count > 0
                        ? DataResult.success(count)
                        : DataResult.error(() -> "Item count must be positive: " + count));

        return countResult.flatMap(count -> {
            var serialized = ops.convertTo(NbtOps.INSTANCE, input);
            if (!(serialized instanceof CompoundTag compoundTag)) {
                return DataResult.error(() -> "Missing item stack must be encoded as a map");
            }

            notifyMissingItem(itemId);
            var placeholder = createBarrierPlaceholder(itemId);
            placeholder.setCount(count);
            return DataResult.success(Pair.of(
                    new ViScriptItemStack(placeholder, itemId, compoundTag.copy()),
                    ops.empty()
            ));
        });
    }

    private static void notifyMissingItem(ResourceLocation itemId) {
        var listeners = MISSING_ITEM_LISTENERS.get();
        if (listeners != null && !listeners.isEmpty()) {
            listeners.peek().accept(itemId);
        }
    }

    private static ItemStack createBarrierPlaceholder(ResourceLocation itemId) {
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

    private static <T> DataResult<Pair<ItemStack, T>> decodeKnownStack(DynamicOps<T> ops, T input) {
        var result = OPTIONAL_UNBOUNDED_STACK_CODEC.decode(ops, input);
        if (result.isSuccess()) {
            return result;
        }

        result = OPTIONAL_UNBOUNDED_STACK_CODEC.decode(
                Platform.getClientRegistryAccess().createSerializationContext(ops), input);
        if (result.isSuccess()) {
            return result;
        }

        return OPTIONAL_UNBOUNDED_STACK_CODEC.decode(
                Platform.getServerRegistryAccess().createSerializationContext(ops), input);
    }

    private static <T> DataResult<T> encodeKnownStack(ItemStack stack, DynamicOps<T> ops, T prefix) {
        var result = OPTIONAL_UNBOUNDED_STACK_CODEC.encode(stack, ops, prefix);
        if (result.isSuccess()) {
            return result;
        }

        result = OPTIONAL_UNBOUNDED_STACK_CODEC.encode(
                stack, Platform.getClientRegistryAccess().createSerializationContext(ops), prefix);
        if (result.isSuccess()) {
            return result;
        }

        return OPTIONAL_UNBOUNDED_STACK_CODEC.encode(
                stack, Platform.getServerRegistryAccess().createSerializationContext(ops), prefix);
    }
}
