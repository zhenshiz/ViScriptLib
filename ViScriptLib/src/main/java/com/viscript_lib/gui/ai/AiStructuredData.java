package com.viscript_lib.gui.ai;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lowdragmc.lowdraglib2.Platform;
import com.lowdragmc.lowdraglib2.configurator.annotation.ConfigNumber;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.lowdragmc.lowdraglib2.nodegraphtookit.model.constant.Constant;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.SyncValueHolder;
import com.lowdragmc.lowdraglib2.syncdata.annotation.ManagedCodec;
import com.lowdragmc.lowdraglib2.syncdata.ManagedFieldUtils;
import com.lowdragmc.lowdraglib2.syncdata.annotation.ReadOnlyManaged;
import com.lowdragmc.lowdraglib2.utils.PersistedParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import java.util.Locale;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Collection;

/** 使用 LDLib2 的持久化协议读取和校验 AI 领域参数，不暴露任意 Java 成员。 */
public final class AiStructuredData {
    private AiStructuredData() { }

    /**
     * 读取对象实际声明的持久化字段，包括默认值。
     *
     * @param value 领域数据对象
     * @return 与 LDLib2 字段编码一致的 JSON 对象
     */
    public static JsonObject read(Object value) {
        var provider = Platform.getFrozenRegistry();
        var ops = provider.createSerializationContext(JsonOps.INSTANCE);
        var result = new JsonObject();
        for (var field : fields(value)) {
            String key = PersistedParser.persistedKey(field);
            var managed = ManagedFieldUtils.createKey(field);
            var encoded = managed.createRef(value).readPersisted(ops);
            try {
                Object raw = field.get(value);
                if (!field.isAnnotationPresent(ManagedCodec.class) && !field.isAnnotationPresent(ReadOnlyManaged.class)) {
                    if (raw instanceof IPersistedSerializable) encoded = read(raw);
                    else if (raw instanceof Collection<?> collection && encoded != null && encoded.isJsonArray()
                            && encoded.getAsJsonArray().size() == collection.size()) {
                        var full = new JsonArray();
                        int i = 0;
                        for (Object element : collection) {
                            full.add(element instanceof IPersistedSerializable ? read(element) : encoded.getAsJsonArray().get(i));
                            i++;
                        }
                        encoded = full;
                    }
                }
                if (encoded != null) result.add(key, encoded);
            } catch (IllegalAccessException exception) { throw new IllegalArgumentException("Cannot read persisted field: " + key, exception); }
        }
        return result;
    }

    private static List<Field> fields(Object value) {
        var result = new ArrayList<Field>();
        for (Class<?> type = value.getClass(); type != null; type = type.getSuperclass()) for (var field : type.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) || PersistedParser.persistedKey(field) == null) continue;
            field.setAccessible(true);
            result.add(field);
        }
        return result;
    }

    /**
     * 根据实际持久化字段生成部分更新 Schema，附带默认值、枚举和数值范围。
     *
     * @param value 字段模板对象
     * @return 拒绝未知字段的 JSON Schema
     */
    public static JsonObject schema(Object value) {
        var result = infer(read(value));
        for (var field : fields(value)) {
            var property = result.getAsJsonObject("properties").getAsJsonObject(PersistedParser.persistedKey(field));
            if (property == null) continue;
            var number = field.getAnnotation(ConfigNumber.class);
            if (number != null) {
                property.addProperty("minimum", number.range()[0]);
                property.addProperty("maximum", number.range()[1]);
            }
            var configurable = field.getAnnotation(Configurable.class);
            if (configurable != null) property.addProperty("description", net.minecraft.network.chat.Component.translatable(configurable.name()).getString());
            if (field.getType().isEnum()) {
                var choices = new JsonArray();
                for (Object constant : field.getType().getEnumConstants()) {
                    var ops = Platform.getFrozenRegistry().createSerializationContext(JsonOps.INSTANCE);
                    choices.add(new SyncValueHolder<>("value", field.getGenericType(), constant).ref.readPersisted(ops));
                }
                property.add("enum", choices);
            }
        }
        return result;
    }

    private static JsonObject infer(JsonElement value) {
        var schema = new JsonObject();
        if (value.isJsonObject()) {
            schema = AiJson.object();
            for (var entry : value.getAsJsonObject().entrySet()) schema.getAsJsonObject("properties").add(entry.getKey(), infer(entry.getValue()));
        } else if (value.isJsonArray()) {
            schema.addProperty("type", "array");
            schema.add("items", value.getAsJsonArray().isEmpty() ? new JsonObject() : infer(value.getAsJsonArray().get(0)));
        } else if (value.isJsonPrimitive()) {
            var primitive = value.getAsJsonPrimitive();
            schema.addProperty("type", primitive.isBoolean() ? "boolean" : primitive.isNumber() ? "number" : "string");
        }
        schema.add("default", value.deepCopy());
        return schema;
    }

    /**
     * 合并已有字段并校验解码后的实际结果。
     *
     * <p>对象属性递归合并，数组整体替换。调用者必须传入隔离草稿，因为验证失败前可能已
     * 写入部分字段；会话不会将失败的草稿交给编辑器。
     *
     * @param target 隔离领域对象
     * @param patch 需修改的持久化字段
     * @throws IllegalArgumentException 字段未知、类型不符或不能无损解码
     */
    public static void update(Object target, JsonObject patch) {
        var merged = read(target);
        merge(merged, patch, "");
        var provider = Platform.getFrozenRegistry();
        var changed = new JsonObject();
        for (String key : patch.keySet()) changed.add(key, merged.get(key));
        for (var field : fields(target)) {
            String key = PersistedParser.persistedKey(field);
            if (!changed.has(key) || !field.isAnnotationPresent(ReadOnlyManaged.class)) continue;
            var ref = ManagedFieldUtils.createKey(field).createRef(target);
            var nativeOps = provider.createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE);
            var original = ref.readPersisted(nativeOps);
            var value = JsonOps.INSTANCE.convertTo(nativeOps, changed.remove(key));
            if (original instanceof net.minecraft.nbt.CompoundTag old && value instanceof net.minecraft.nbt.CompoundTag updated
                    && old.contains("uid") && updated.contains("uid")) {
                // JSON 不保留 NBT 数值宽度，而自定义 UID 的反序列化方法可能要求 IntTag 等具体类型。
                updated.put("uid", restoreUidType(updated.get("uid"), old.get("uid")));
            }
            ref.writePersisted(nativeOps, value);
        }
        PersistedParser.deserialize(provider.createSerializationContext(JsonOps.INSTANCE), changed, target, provider);
        requireMatches(patch, read(target), "");
        validateRanges(target, patch, "");
    }

    private static net.minecraft.nbt.Tag restoreUidType(net.minecraft.nbt.Tag value, net.minecraft.nbt.Tag template) {
        if (value instanceof net.minecraft.nbt.NumericTag number && template instanceof net.minecraft.nbt.NumericTag) {
            return switch (template.getId()) {
                case net.minecraft.nbt.Tag.TAG_BYTE -> net.minecraft.nbt.ByteTag.valueOf(number.getAsByte());
                case net.minecraft.nbt.Tag.TAG_SHORT -> net.minecraft.nbt.ShortTag.valueOf(number.getAsShort());
                case net.minecraft.nbt.Tag.TAG_INT -> net.minecraft.nbt.IntTag.valueOf(number.getAsInt());
                case net.minecraft.nbt.Tag.TAG_LONG -> net.minecraft.nbt.LongTag.valueOf(number.getAsLong());
                case net.minecraft.nbt.Tag.TAG_FLOAT -> net.minecraft.nbt.FloatTag.valueOf(number.getAsFloat());
                case net.minecraft.nbt.Tag.TAG_DOUBLE -> net.minecraft.nbt.DoubleTag.valueOf(number.getAsDouble());
                default -> value;
            };
        }
        if (value instanceof net.minecraft.nbt.CompoundTag object && template instanceof net.minecraft.nbt.CompoundTag old)
            for (String key : object.getAllKeys()) if (old.contains(key)) object.put(key, restoreUidType(object.get(key), old.get(key)));
        return value;
    }

    private static void validateRanges(Object target, JsonObject patch, String path) {
        for (var field : fields(target)) {
            String key = PersistedParser.persistedKey(field);
            if (!patch.has(key)) continue;
            try {
                Object value = field.get(target);
                var range = field.getAnnotation(ConfigNumber.class);
                if (range != null && value instanceof Number number
                        && (number.doubleValue() < range.range()[0] || number.doubleValue() > range.range()[1]))
                    throw new IllegalArgumentException("Out of range: " + path + key);
                if (value instanceof IPersistedSerializable && patch.get(key).isJsonObject())
                    validateRanges(value, patch.getAsJsonObject(key), path + key + ".");
            } catch (IllegalAccessException exception) { throw new IllegalArgumentException(exception); }
        }
    }

    private static void merge(JsonObject target, JsonObject patch, String path) {
        for (var entry : patch.entrySet()) {
            var key = entry.getKey();
            if (!target.has(key)) {
                if (path.isEmpty()) throw new IllegalArgumentException("Unknown persisted field: " + key);
                // 空物品栈、组件和映射允许新增编码字段；解码回读会拒绝未被接收的键。
                target.add(key, entry.getValue().deepCopy());
                continue;
            }
            var before = target.get(key);
            var after = entry.getValue();
            if (before.isJsonObject() && after.isJsonObject()) merge(before.getAsJsonObject(), after.getAsJsonObject(), path + key + ".");
            else {
                requireShape(before, after, path + key);
                target.add(key, after.deepCopy());
            }
        }
    }

    private static void requireShape(JsonElement before, JsonElement after, String path) {
        if (after == null || after.isJsonNull()) throw new IllegalArgumentException("Null field: " + path);
        if (before.isJsonNull()) return;
        if (before.isJsonObject() != after.isJsonObject() || before.isJsonArray() != after.isJsonArray()
                || before.isJsonPrimitive() != after.isJsonPrimitive()) throw new IllegalArgumentException("Wrong field type: " + path);
        if (before.isJsonPrimitive()) {
            var a = before.getAsJsonPrimitive();
            var b = after.getAsJsonPrimitive();
            if (a.isBoolean() != b.isBoolean() || a.isNumber() != b.isNumber() || a.isString() != b.isString())
                throw new IllegalArgumentException("Wrong field type: " + path);
            if (b.isNumber() && !Double.isFinite(b.getAsDouble())) throw new IllegalArgumentException("Non-finite field: " + path);
        }
    }

    private static void requireMatches(JsonElement requested, JsonElement actual, String path) {
        if (actual == null || actual.isJsonNull()) throw new IllegalArgumentException("Cannot decode field: " + path);
        if (requested.isJsonObject()) {
            if (!actual.isJsonObject()) throw new IllegalArgumentException("Cannot decode object: " + path);
            for (var e : requested.getAsJsonObject().entrySet()) requireMatches(e.getValue(), actual.getAsJsonObject().get(e.getKey()), path + e.getKey() + ".");
        } else if (requested.isJsonArray()) {
            if (!actual.isJsonArray() || requested.getAsJsonArray().size() != actual.getAsJsonArray().size())
                throw new IllegalArgumentException("Cannot decode array: " + path);
            for (int i = 0; i < requested.getAsJsonArray().size(); i++) requireMatches(requested.getAsJsonArray().get(i), actual.getAsJsonArray().get(i), path + i + ".");
        } else if (requested.isJsonPrimitive() && actual.isJsonPrimitive()
                && requested.getAsJsonPrimitive().isNumber() && actual.getAsJsonPrimitive().isNumber()) {
            if (requested.getAsBigDecimal().compareTo(actual.getAsBigDecimal()) != 0)
                throw new IllegalArgumentException("Invalid numeric field value: " + path);
        } else if (!requested.equals(actual)) throw new IllegalArgumentException("Invalid field value: " + path);
    }

    /**
     * 读取节点常量的实际编码及类型。
     *
     * @param constant 节点选项或输入常量
     * @return 包含类型及 JSON 值的对象
     */
    @SuppressWarnings("unchecked")
    public static JsonObject constant(Constant constant) {
        var result = new JsonObject();
        result.addProperty("type", constant.getTypeHandle() == null ? constant.getType().getTypeName() : constant.getTypeHandle().getIdentification());
        var ops = Platform.getFrozenRegistry().createSerializationContext(JsonOps.INSTANCE);
        JsonElement value;
        if (constant.getCustomCodec() != null) value = ((Codec<Object>) constant.getCustomCodec()).encodeStart(ops, constant.getValue()).getOrThrow(IllegalArgumentException::new);
        else value = new SyncValueHolder<>("value", constant.getType(), constant.getValue()).ref.readPersisted(ops);
        if (value == null || value.isJsonNull()) throw new IllegalArgumentException("Unserializable node constant");
        result.add("value", value);
        return result;
    }

    /**
     * 按节点声明的 Codec 或 LDLib2 访问器解码一个常量。
     *
     * @param constant 草稿节点常量
     * @param value 由读取工具返回的编码格式的 JSON 值
     * @throws IllegalArgumentException 值的类型错误或解码后发生数据丢失
     */
    @SuppressWarnings("unchecked")
    public static void setConstant(Constant constant, JsonElement value) {
        var before = constant(constant).get("value");
        requireShape(before, value, "constant");
        var ops = Platform.getFrozenRegistry().createSerializationContext(JsonOps.INSTANCE);
        Object decoded;
        if (constant.getCustomCodec() != null) decoded = ((Codec<Object>) constant.getCustomCodec()).parse(ops, value).getOrThrow(IllegalArgumentException::new);
        else {
            var holder = new SyncValueHolder<>("value", constant.getType(), null);
            holder.ref.writePersisted(ops, value);
            decoded = holder.getValue();
        }
        if (decoded == null || !constant.trySetValue(decoded)) throw new IllegalArgumentException("Invalid node constant");
        requireMatches(value, constant(constant).get("value"), "constant");
    }

    /**
     * 创建带分页、分区和搜索词的读取 Schema。
     *
     * @param sections 分区的英文说明
     * @return 读取工具的参数 Schema
     */
    public static JsonObject readSchema(String sections) {
        var root = AiJson.object();
        AiJson.property(root, "offset", "integer", "Zero-based page offset.");
        AiJson.property(root, "limit", "integer", "Maximum entries per page.");
        AiJson.property(root, "section", "string", sections);
        AiJson.property(root, "query", "string", "Case-insensitive ID or name filter. Omit for all entries.");
        return root;
    }

    /**
     * 过滤并分页返回条目。
     *
     * @param rows 全部分区条目
     * @param arguments 含可选搜索词的读取参数
     * @param offset 非负起始偏移
     * @param limit 最大返回条目数
     * @return 包含条目、总数及下一页信息的对象
     */
    public static JsonObject page(JsonArray rows, JsonObject arguments, int offset, int limit) {
        String query = arguments.has("query") ? AiJson.string(arguments, "query").toLowerCase(Locale.ROOT) : "";
        var filtered = new JsonArray();
        for (var row : rows) if (row.toString().toLowerCase(Locale.ROOT).contains(query)) filtered.add(row);
        var entries = new JsonArray();
        for (int i = offset; i < Math.min((long) filtered.size(), (long) offset + limit); i++) entries.add(filtered.get(i));
        var result = new JsonObject();
        result.add("entries", entries);
        result.addProperty("total", filtered.size());
        result.addProperty("has_more", (long) offset + entries.size() < filtered.size());
        result.addProperty("next_offset", (long) offset + entries.size());
        return result;
    }
}
