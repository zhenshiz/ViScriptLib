package com.viscript_lib.util.item;

import com.lowdragmc.lowdraglib2.Platform;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
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
 * <p>输入中的物品 ID 未注册，或物品组件无法完整解码时，此类型会显示屏障占位符，并
 * 保留完整的原始序列化数据。组件解码失败包括组件内部引用的附魔等数据驱动注册表内容
 * 不存在的情况。只要调用方没有使用新的原版物品栈替换此对象，再次保存就会原样写回
 * 物品 ID、数量和组件；重新安装对应模组或恢复兼容版本后可在下次载入时恢复。
 *
 * <p>此类型只负责 ViScript 数据边界。游戏逻辑需要使用原版物品栈 API 时，应通过
 * {@link #toItemStack()} 取得独立副本。
 */
public final class ViScriptItemStack {
    private static final ResourceLocation ENCHANTMENTS_COMPONENT_ID =
            ResourceLocation.withDefaultNamespace("enchantments");
    private static final ResourceLocation STORED_ENCHANTMENTS_COMPONENT_ID =
            ResourceLocation.withDefaultNamespace("stored_enchantments");
    private static final ThreadLocal<Deque<Consumer<UnavailableItem>>> UNAVAILABLE_ITEM_LISTENERS =
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
     * 使用无损不可解析物品回退和无界正整数数量的物品栈 Codec。
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
                    var countResult = decodeCount(ops, map.get().get("count"));
                    if (countResult.isError()) {
                        return countResult.map(count -> Pair.of(new ViScriptItemStack(), ops.empty()));
                    }
                    var count = countResult.result().orElseThrow();
                    if (!BuiltInRegistries.ITEM.containsKey(itemId)) {
                        return decodeUnavailableStack(
                                ops,
                                input,
                                itemId,
                                count,
                                "Unknown item: " + itemId,
                                new UnavailableCause(UnavailableCauseType.MISSING_ITEM, itemId)
                        );
                    }

                    var decoded = decodeKnownStack(ops, input);
                    if (decoded.isSuccess()) {
                        return decoded.map(pair -> pair.mapFirst(ViScriptItemStack::new));
                    }

                    var decodeError = decoded.error()
                            .map(DataResult.Error::message)
                            .orElse("Unknown item stack decode error");
                    return decodeUnavailableStack(ops, input, itemId, count, decodeError, null);
                }
            }

            return decodeKnownStack(ops, input)
                    .map(pair -> pair.mapFirst(ViScriptItemStack::new));
        }

        @Override
        public <T> DataResult<T> encode(ViScriptItemStack input, DynamicOps<T> ops, T prefix) {
            Objects.requireNonNull(input, "input");
            if (input.unresolvedSerializedStack != null) {
                var serialized = NbtOps.INSTANCE.convertTo(ops, input.unresolvedSerializedStack);
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
    private ResourceLocation serializedItemId;
    @Nullable
    private CompoundTag unresolvedSerializedStack;
    @Nullable
    private UnavailableItem unavailability;

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

    private ViScriptItemStack(ItemStack itemStack, ResourceLocation serializedItemId,
                              CompoundTag unresolvedSerializedStack, UnavailableItem unavailability) {
        this.itemStack = itemStack;
        this.serializedItemId = serializedItemId;
        this.unresolvedSerializedStack = unresolvedSerializedStack;
        this.unavailability = unavailability;
    }

    /**
     * 在指定操作期间监听无法完整解析的物品。
     *
     * <p>监听器按当前线程和嵌套层级隔离，并且无论操作正常返回还是抛出异常都会被移除。
     * 监听只提供通知，不改变 {@link #CODEC} 的容错结果；没有监听器时，不可解析物品仍会
     * 自动解码为保留原始数据的屏障占位符。
     *
     * @param  <T> 操作返回值类型
     * @param  listener 接收原始物品 ID、结构化原因和 Codec 错误的监听器
     * @param  operation 需要执行的解码或数据加载操作
     * @return 操作返回值
     * @throws Exception 操作抛出的异常
     */
    public static <T> T withUnavailableItemListener(Consumer<UnavailableItem> listener,
                                                    Callable<T> operation) throws Exception {
        Objects.requireNonNull(listener, "listener");
        Objects.requireNonNull(operation, "operation");

        var listeners = UNAVAILABLE_ITEM_LISTENERS.get();
        if (listeners == null) {
            listeners = new ArrayDeque<>();
            UNAVAILABLE_ITEM_LISTENERS.set(listeners);
        }
        listeners.push(listener);
        try {
            return operation.call();
        } finally {
            listeners.pop();
            if (listeners.isEmpty()) {
                UNAVAILABLE_ITEM_LISTENERS.remove();
            }
        }
    }

    /**
     * 在指定操作期间监听无法完整解析的物品 ID。
     *
     * @param  <T> 操作返回值类型
     * @param  listener 接收无法完整解析物品 ID 的监听器
     * @param  operation 需要执行的解码或数据加载操作
     * @return 操作返回值
     * @throws Exception 操作抛出的异常
     * @deprecated 使用 {@link #withUnavailableItemListener(Consumer, Callable)} 获取错误原因。
     */
    @Deprecated(forRemoval = false)
    public static <T> T withMissingItemListener(Consumer<ResourceLocation> listener,
                                                Callable<T> operation) throws Exception {
        Objects.requireNonNull(listener, "listener");
        return withUnavailableItemListener(unavailable -> listener.accept(unavailable.itemId()), operation);
    }

    /**
     * 转换为当前用于编辑、显示或游戏逻辑的原版物品栈。
     *
     * <p>无法完整解析的物品返回屏障占位符。修改返回值不会改变此对象；需要替换持久化
     * 内容时应调用 {@link #ViScriptItemStack(ItemStack)} 创建新的包装。返回值只适合临时
     * 显示和运行时读取，不应代替此对象参与持久化或网络传输。
     *
     * @return 当前物品栈或屏障占位符的独立副本
     */
    public ItemStack toItemStack() {
        return itemStack.copy();
    }

    /**
     * 创建具有指定数量的独立包装副本。
     *
     * <p>正数最高可设置为 {@link Integer#MAX_VALUE}。不可解析物品的原始序列化数据会同步
     * 更新；{@code 0} 或负数会返回空物品栈包装。
     *
     * @param count 新的物品数量
     * @return 具有指定数量的独立包装副本
     */
    public ViScriptItemStack copyWithCount(int count) {
        if (count <= 0) {
            return new ViScriptItemStack();
        }

        if (unresolvedSerializedStack == null || serializedItemId == null) {
            var copy = itemStack.copy();
            copy.setCount(count);
            return new ViScriptItemStack(copy);
        }

        var placeholder = itemStack.copy();
        placeholder.setCount(count);
        var serialized = unresolvedSerializedStack.copy();
        serialized.putInt("count", count);
        return new ViScriptItemStack(placeholder, serializedItemId, serialized, unavailability);
    }

    /**
     * 返回当前是否正在显示无法完整解析物品的占位符。
     *
     * @return 已保留不可解析物品原始数据时返回 {@code true}
     */
    public boolean isUnavailable() {
        return unresolvedSerializedStack != null;
    }

    /**
     * 返回当前是否正在显示不可解析物品的占位符。
     *
     * @return 已保留不可解析物品原始数据时返回 {@code true}
     * @deprecated 使用 {@link #isUnavailable()}，当前问题也可能来自缺失或不兼容的组件。
     */
    @Deprecated(forRemoval = false)
    public boolean isMissingItem() {
        return isUnavailable();
    }

    /**
     * 返回当前环境中完整物品解码失败的原因。
     *
     * <p>该信息只用于诊断，不会写入物品序列化数据。
     *
     * @return 不可解析状态的 Codec 错误；正常物品时为空
     */
    public Optional<String> getUnavailabilityError() {
        return getUnavailability().map(UnavailableItem::decodeError);
    }

    /**
     * 返回当前不可解析物品的结构化诊断信息。
     *
     * <p>诊断包含受影响的原始物品 ID、明确识别出的缺失注册表引用以及底层 Codec 错误。
     * 正常物品栈不包含诊断信息。
     *
     * @return 当前不可解析物品的诊断信息；正常物品栈时为空
     */
    public Optional<UnavailableItem> getUnavailability() {
        return Optional.ofNullable(unavailability);
    }

    /**
     * 返回当前序列化物品 ID。
     *
     * <p>不可解析物品返回保留的原始 ID，而不是屏障 ID。
     *
     * @return 当前非空物品的注册表 ID；空物品栈时为空
     */
    public Optional<ResourceLocation> getItemId() {
        if (serializedItemId != null) {
            return Optional.of(serializedItemId);
        }
        if (itemStack.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(BuiltInRegistries.ITEM.getKey(itemStack.getItem()));
    }

    private static <T> DataResult<Integer> decodeCount(DynamicOps<T> ops, @Nullable T countValue) {
        return countValue == null
                ? DataResult.success(1)
                : Codec.INT.parse(ops, countValue)
                .flatMap(count -> count > 0
                        ? DataResult.success(count)
                        : DataResult.error(() -> "Item count must be positive: " + count));
    }

    private static <T> DataResult<Pair<ViScriptItemStack, T>> decodeUnavailableStack(
            DynamicOps<T> ops, T input, ResourceLocation itemId, int count, String decodeError,
            @Nullable UnavailableCause knownCause) {
        var serialized = ops.convertTo(NbtOps.INSTANCE, input);
        if (!(serialized instanceof CompoundTag compoundTag)) {
            return DataResult.error(() -> "Unavailable item stack must be encoded as a map");
        }

        var causes = knownCause == null
                ? detectUnavailableCauses(ops, compoundTag)
                : List.of(knownCause);
        var unavailableItem = new UnavailableItem(itemId, causes, decodeError);
        notifyUnavailableItem(unavailableItem);
        var placeholder = createBarrierPlaceholder(unavailableItem);
        placeholder.setCount(count);
        return DataResult.success(Pair.of(
                new ViScriptItemStack(placeholder, itemId, compoundTag.copy(), unavailableItem),
                ops.empty()
        ));
    }

    private static List<UnavailableCause> detectUnavailableCauses(
            DynamicOps<?> ops, CompoundTag serializedStack) {
        if (!(serializedStack.get("components") instanceof CompoundTag components)) {
            return List.of();
        }

        var causes = new ArrayList<UnavailableCause>();
        for (var serializedComponentKey : components.getAllKeys()) {
            var componentKey = serializedComponentKey.startsWith("!")
                    ? serializedComponentKey.substring(1)
                    : serializedComponentKey;
            var componentId = ResourceLocation.tryParse(componentKey);
            if (componentId == null) {
                continue;
            }
            if (!BuiltInRegistries.DATA_COMPONENT_TYPE.containsKey(componentId)) {
                addCause(causes, new UnavailableCause(
                        UnavailableCauseType.MISSING_COMPONENT,
                        componentId
                ));
                continue;
            }
            if (!serializedComponentKey.startsWith("!")
                    && (componentId.equals(ENCHANTMENTS_COMPONENT_ID)
                    || componentId.equals(STORED_ENCHANTMENTS_COMPONENT_ID))) {
                detectMissingEnchantments(ops, components.get(serializedComponentKey), causes);
            }
        }
        return List.copyOf(causes);
    }

    private static void detectMissingEnchantments(
            DynamicOps<?> ops, @Nullable Tag serializedEnchantments,
            List<UnavailableCause> causes) {
        if (!(serializedEnchantments instanceof CompoundTag enchantments)) {
            return;
        }

        CompoundTag levels;
        var serializedLevels = enchantments.get("levels");
        if (serializedLevels instanceof CompoundTag levelMap) {
            levels = levelMap;
        } else if (serializedLevels == null) {
            levels = enchantments;
        } else {
            return;
        }

        for (var enchantmentKey : levels.getAllKeys()) {
            if (enchantmentKey.equals("show_in_tooltip")) {
                continue;
            }
            var enchantmentId = ResourceLocation.tryParse(enchantmentKey);
            if (enchantmentId != null
                    && !isEnchantmentRegistered(ops, enchantmentId).orElse(true)) {
                addCause(causes, new UnavailableCause(
                        UnavailableCauseType.MISSING_ENCHANTMENT,
                        enchantmentId
                ));
            }
        }
    }

    private static Optional<Boolean> isEnchantmentRegistered(
            DynamicOps<?> ops, ResourceLocation enchantmentId) {
        if (ops instanceof RegistryOps<?> registryOps) {
            var result = findEnchantment(registryOps.lookupProvider, enchantmentId);
            if (result.isPresent()) {
                return result;
            }
        }

        try {
            var result = findEnchantment(Platform.getClientRegistryAccess(), enchantmentId);
            if (result.isPresent()) {
                return result;
            }
        } catch (RuntimeException ignored) {
        }

        try {
            return findEnchantment(Platform.getServerRegistryAccess(), enchantmentId);
        } catch (RuntimeException ignored) {
            return Optional.empty();
        }
    }

    private static Optional<Boolean> findEnchantment(
            RegistryOps.RegistryInfoLookup registryLookup, ResourceLocation enchantmentId) {
        return registryLookup.lookup(Registries.ENCHANTMENT)
                .map(info -> info.getter().get(ResourceKey.create(
                        Registries.ENCHANTMENT,
                        enchantmentId
                )).isPresent());
    }

    private static Optional<Boolean> findEnchantment(
            HolderLookup.Provider registries, ResourceLocation enchantmentId) {
        return registries.lookup(Registries.ENCHANTMENT)
                .map(lookup -> lookup.get(ResourceKey.create(
                        Registries.ENCHANTMENT,
                        enchantmentId
                )).isPresent());
    }

    private static void addCause(List<UnavailableCause> causes, UnavailableCause cause) {
        if (!causes.contains(cause)) {
            causes.add(cause);
        }
    }

    private static void notifyUnavailableItem(UnavailableItem unavailableItem) {
        var listeners = UNAVAILABLE_ITEM_LISTENERS.get();
        if (listeners != null && !listeners.isEmpty()) {
            listeners.peek().accept(unavailableItem);
        }
    }

    private static ItemStack createBarrierPlaceholder(UnavailableItem unavailableItem) {
        var placeholder = new ItemStack(Items.BARRIER);
        placeholder.set(DataComponents.CUSTOM_NAME, createPlaceholderName(unavailableItem));

        var lore = new ArrayList<Component>();
        var primaryCause = unavailableItem.primaryCause();
        if (primaryCause.isPresent()
                && primaryCause.get().type() != UnavailableCauseType.MISSING_ITEM) {
            lore.add(styleLore(Component.translatable(
                    "viscript_lib.unavailable_item.placeholder_lore.original_item",
                    unavailableItem.itemId().toString()
            ), ChatFormatting.GRAY));
        }

        if (unavailableItem.causes().isEmpty()) {
            lore.add(styleLore(Component.translatable(
                    "viscript_lib.unavailable_item.placeholder_lore.decode_error"
            ), ChatFormatting.RED));
        } else {
            for (int index = 0; index < unavailableItem.causes().size(); index++) {
                var cause = unavailableItem.causes().get(index);
                if (index > 0) {
                    lore.add(styleLore(createCauseDescription(cause), ChatFormatting.RED));
                }
                lore.add(styleLore(Component.translatable(
                        "viscript_lib.unavailable_item.placeholder_lore.mod_namespace",
                        cause.sourceNamespace()
                ), ChatFormatting.RED));
            }
        }
        lore.add(styleLore(Component.translatable(
                "viscript_lib.unavailable_item.placeholder_restore_lore"
        ), ChatFormatting.GRAY));
        placeholder.set(DataComponents.LORE, new ItemLore(lore));
        return placeholder;
    }

    private static Component createPlaceholderName(UnavailableItem unavailableItem) {
        var primaryCause = unavailableItem.primaryCause();
        if (primaryCause.isEmpty()) {
            return Component.translatable(
                    "viscript_lib.unavailable_item.placeholder_name.decode_error",
                    unavailableItem.itemId().toString()
            ).withStyle(ChatFormatting.RED);
        }

        var cause = primaryCause.get();
        var translationKey = switch (cause.type()) {
            case MISSING_ITEM -> "viscript_lib.unavailable_item.placeholder_name.missing_item";
            case MISSING_COMPONENT -> "viscript_lib.unavailable_item.placeholder_name.missing_component";
            case MISSING_ENCHANTMENT -> "viscript_lib.unavailable_item.placeholder_name.missing_enchantment";
            case DECODE_ERROR -> "viscript_lib.unavailable_item.placeholder_name.decode_error";
        };
        return Component.translatable(translationKey, cause.missingId().toString())
                .withStyle(ChatFormatting.RED);
    }

    private static Component createCauseDescription(UnavailableCause cause) {
        var translationKey = switch (cause.type()) {
            case MISSING_ITEM -> "viscript_lib.unavailable_item.cause.missing_item";
            case MISSING_COMPONENT -> "viscript_lib.unavailable_item.cause.missing_component";
            case MISSING_ENCHANTMENT -> "viscript_lib.unavailable_item.cause.missing_enchantment";
            case DECODE_ERROR -> "viscript_lib.unavailable_item.cause.decode_error";
        };
        return Component.translatable(translationKey, cause.missingId().toString());
    }

    private static Component styleLore(Component component, ChatFormatting color) {
        return component.copy().withStyle(style -> style
                .withColor(color)
                .withItalic(false));
    }

    /**
     * 标识不可解析物品的主要原因。
     *
     * <p>前三种类型表示已经从原始物品数据和当前注册表中确定了具体缺失引用；
     * {@link #DECODE_ERROR} 表示完整物品解码失败，但无法可靠定位到缺失 ID。
     */
    public enum UnavailableCauseType {
        /** 原始物品 ID 未注册。 */
        MISSING_ITEM,
        /** 物品组件类型未注册。 */
        MISSING_COMPONENT,
        /** 物品组件引用的附魔未注册。 */
        MISSING_ENCHANTMENT,
        /** 完整物品 Codec 失败，但未识别出明确缺失的注册表引用。 */
        DECODE_ERROR
    }

    /**
     * 描述一个无法从当前注册表解析的引用。
     *
     * @param type 缺失引用的注册表类型
     * @param missingId 缺失的完整注册表 ID
     */
    public record UnavailableCause(UnavailableCauseType type, ResourceLocation missingId) {
        public UnavailableCause {
            Objects.requireNonNull(type, "type");
            Objects.requireNonNull(missingId, "missingId");
        }

        /**
         * 返回缺失 ID 的命名空间。
         *
         * <p>命名空间通常等同于注册该内容的模组 ID；数据包也可以使用自定义命名空间，
         * 因此调用方不应把该值视为已验证的已安装模组标识。
         *
         * @return 缺失 ID 的命名空间
         */
        public String sourceNamespace() {
            return missingId.getNamespace();
        }
    }

    /**
     * 描述一个在当前运行环境中无法完整解析的物品。
     *
     * @param itemId 原始物品 ID
     * @param causes 明确识别出的缺失注册表引用；无法分类时为空列表
     * @param decodeError 完整物品 Codec 返回的错误
     */
    public record UnavailableItem(ResourceLocation itemId, List<UnavailableCause> causes,
                                  String decodeError) {
        public UnavailableItem {
            Objects.requireNonNull(itemId, "itemId");
            causes = List.copyOf(Objects.requireNonNull(causes, "causes"));
            Objects.requireNonNull(decodeError, "decodeError");
        }

        /**
         * 创建未提供结构化原因的不可解析物品诊断。
         *
         * @param itemId 原始物品 ID
         * @param decodeError 完整物品 Codec 返回的错误
         */
        public UnavailableItem(ResourceLocation itemId, String decodeError) {
            this(itemId, List.of(), decodeError);
        }

        /**
         * 返回首个明确识别出的不可用原因。
         *
         * @return 首个不可用原因；仅有通用 Codec 错误时为空
         */
        public Optional<UnavailableCause> primaryCause() {
            return causes.isEmpty() ? Optional.empty() : Optional.of(causes.getFirst());
        }

        /**
         * 返回用于概括当前诊断的原因类型。
         *
         * @return 首个结构化原因的类型；未识别出具体引用时返回
         *         {@link UnavailableCauseType#DECODE_ERROR}
         */
        public UnavailableCauseType reason() {
            return primaryCause()
                    .map(UnavailableCause::type)
                    .orElse(UnavailableCauseType.DECODE_ERROR);
        }
    }

    private static <T> DataResult<Pair<ItemStack, T>> decodeKnownStack(DynamicOps<T> ops, T input) {
        var primaryResult = OPTIONAL_UNBOUNDED_STACK_CODEC.decode(ops, input);
        if (primaryResult.isSuccess()) {
            return primaryResult;
        }

        try {
            var clientResult = OPTIONAL_UNBOUNDED_STACK_CODEC.decode(
                    Platform.getClientRegistryAccess().createSerializationContext(ops), input);
            if (clientResult.isSuccess()) {
                return clientResult;
            }
        } catch (RuntimeException ignored) {
        }

        try {
            var serverResult = OPTIONAL_UNBOUNDED_STACK_CODEC.decode(
                    Platform.getServerRegistryAccess().createSerializationContext(ops), input);
            if (serverResult.isSuccess()) {
                return serverResult;
            }
        } catch (RuntimeException ignored) {
        }

        return primaryResult;
    }

    private static <T> DataResult<T> encodeKnownStack(ItemStack stack, DynamicOps<T> ops, T prefix) {
        var primaryResult = OPTIONAL_UNBOUNDED_STACK_CODEC.encode(stack, ops, prefix);
        if (primaryResult.isSuccess()) {
            return primaryResult;
        }

        try {
            var clientResult = OPTIONAL_UNBOUNDED_STACK_CODEC.encode(
                    stack, Platform.getClientRegistryAccess().createSerializationContext(ops), prefix);
            if (clientResult.isSuccess()) {
                return clientResult;
            }
        } catch (RuntimeException ignored) {
        }

        try {
            var serverResult = OPTIONAL_UNBOUNDED_STACK_CODEC.encode(
                    stack, Platform.getServerRegistryAccess().createSerializationContext(ops), prefix);
            if (serverResult.isSuccess()) {
                return serverResult;
            }
        } catch (RuntimeException ignored) {
        }

        return primaryResult;
    }
}
