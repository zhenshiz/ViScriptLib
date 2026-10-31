package com.viscript_lib.gui.ai;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Arrays;
import java.util.Set;

/** 为模型工具构建 Schema，并严格读取领域参数。 */
public final class AiJson {
    private AiJson() { }

    /**
     * 创建拒绝未知字段的对象 Schema。
     *
     * @return 含空属性表的新建 Schema
     */
    public static JsonObject object() {
        var value = new JsonObject();
        value.addProperty("type", "object");
        value.add("properties", new JsonObject());
        value.addProperty("additionalProperties", false);
        return value;
    }

    /**
     * 添加或替换一个属性的 Schema。
     *
     * @param schema 含有属性表的对象 Schema
     * @param name 属性名称
     * @param type JSON Schema 类型名称
     * @param description 向模型提供的属性说明
     * @return 已加入属性表的 Schema，可继续设置范围或枚举
     */
    public static JsonObject property(JsonObject schema, String name, String type, String description) {
        var value = new JsonObject();
        value.addProperty("type", type);
        value.addProperty("description", description);
        schema.getAsJsonObject("properties").add(name, value);
        return value;
    }

    /**
     * 替换对象 Schema 的必填属性列表。
     *
     * @param schema 对象 Schema
     * @param names 必填属性名称
     */
    public static void required(JsonObject schema, String... names) {
        var array = new JsonArray();
        for (String name : names) array.add(name);
        schema.add("required", array);
    }

    /**
     * 检查对象没有声明范围以外的字段。
     *
     * @param input 待检查的参数
     * @param allowed 允许的字段名称
     * @throws IllegalArgumentException 存在未知字段
     */
    public static void keys(JsonObject input, String... allowed) {
        Set<String> keys = Set.copyOf(Arrays.asList(allowed));
        for (String key : input.keySet()) {
            if (!keys.contains(key)) throw new IllegalArgumentException("Unknown field: " + key);
        }
    }

    /**
     * 读取必需的字符串，不自动把其他 JSON 类型转换为文本。
     *
     * @param input 参数对象
     * @param key 字段名称
     * @param maxLength 最大 UTF-16 长度
     * @return 字符串原值
     * @throws IllegalArgumentException 字段缺失、类型不符或超长
     */
    public static String string(JsonObject input, String key, int maxLength) {
        JsonElement value = input.get(key);
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
            throw new IllegalArgumentException("Expected string: " + key);
        }
        String text = value.getAsString();
        if (text.length() > maxLength) throw new IllegalArgumentException("Too long: " + key);
        return text;
    }

    /**
     * 读取必需的字符串，不设置内容长度上限。
     *
     * @param input 参数对象
     * @param key 字段名称
     * @return 字符串原值
     * @throws IllegalArgumentException 字段缺失或类型不符
     */
    public static String string(JsonObject input, String key) {
        return string(input, key, Integer.MAX_VALUE);
    }

    /**
     * 读取闭区间内的精确整数；字段缺失时返回默认值。
     *
     * @param input 参数对象
     * @param key 字段名称
     * @param fallback 缺失字段时的默认值，调用者应确保其在范围内
     * @param min 最小值
     * @param max 最大值
     * @return 参数值或默认值
     * @throws IllegalArgumentException 值不是数字、含小数或超出范围
     */
    public static int integer(JsonObject input, String key, int fallback, int min, int max) {
        if (!input.has(key)) return fallback;
        try {
            var value = input.get(key);
            if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) throw new ArithmeticException();
            int number = value.getAsBigDecimal().intValueExact();
            if (number < min || number > max) throw new ArithmeticException();
            return number;
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("Expected integer " + key + " in [" + min + ", " + max + "]");
        }
    }

    /**
     * 读取必需的布尔值，不接受字符串形式的布尔值。
     *
     * @param input 参数对象
     * @param key 字段名称
     * @return 布尔值
     * @throws IllegalArgumentException 字段缺失或类型不符
     */
    public static boolean bool(JsonObject input, String key) {
        var value = input.get(key);
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isBoolean()) {
            throw new IllegalArgumentException("Expected boolean: " + key);
        }
        return value.getAsBoolean();
    }
}
