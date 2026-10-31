package com.viscript_lib.ai;

import com.google.gson.JsonObject;
import com.viscript_lib.util.math.Clamp;

import java.util.Locale;
import java.util.Set;

/**
 * 描述知识提供器的一次查询及分页条件。
 *
 * @param operation 操作名称；内置提供器支持 {@code types}、{@code list}、{@code get}、{@code tags} 和 {@code tag}
 * @param type 资源类型
 * @param id 单条资源或标签的完整标识符
 * @param namespace 列表的命名空间过滤条件，空字符串表示不过滤
 * @param query 忽略大小写的 ID、显示名或翻译键搜索词
 * @param tag 列表的标签过滤条件，空字符串表示不过滤
 * @param cursor 提供器返回的分页游标；首次查询为空字符串
 * @param limit 页大小；限制在 {@code 1} 至 {@code 128} 之间，编辑器可选择更小的页大小
 * @param includeTags 是否包含条目的标签集合
 */
public record AiQueryRequest(String operation, String type, String id, String namespace,
                              String query, String tag, String cursor, int limit, boolean includeTags) {
    public AiQueryRequest {
        operation = normalized(operation == null || operation.isBlank() ? "list" : operation);
        type = normalized(type);
        id = trimmed(id);
        namespace = normalized(namespace);
        query = normalized(query);
        tag = trimmed(tag);
        cursor = trimmed(cursor);
        limit = Clamp.clamp(limit, 1, 128);
    }

    /**
     * 创建资源列表的第一页查询。
     *
     * @param type 资源类型
     * @return 页大小为 {@code 64} 的列表查询
     */
    public static AiQueryRequest list(String type) {
        return new AiQueryRequest("list", type, "", "", "", "", "", 64, false);
    }

    /**
     * 创建单条资源查询。
     *
     * @param type 资源类型
     * @param id 完整的资源标识符
     * @return 单条资源查询
     */
    public static AiQueryRequest get(String type, String id) {
        return new AiQueryRequest("get", type, id, "", "", "", "", 1, true);
    }

    /**
     * 读取普通 JSON 请求并校验字段类型。
     *
     * @param json 包含查询字段的 JSON 对象
     * @return 规范化后的查询请求
     * @throws IllegalArgumentException 包含未知字段或字段类型不正确时抛出
     */
    public static AiQueryRequest fromJson(JsonObject json) {
        var fields = Set.of("operation", "type", "id", "namespace", "query", "tag", "cursor", "limit", "include_tags");
        if (!fields.containsAll(json.keySet())) throw new IllegalArgumentException("Unknown query field");
        if (string(json, "operation").isBlank()) throw new IllegalArgumentException("operation is required");
        try {
            int limit = 64;
            if (json.has("limit")) {
                if (!json.get("limit").isJsonPrimitive() || !json.getAsJsonPrimitive("limit").isNumber()) {
                    throw new IllegalArgumentException("limit must be an integer");
                }
                limit = json.get("limit").getAsBigDecimal().intValueExact();
            }
            boolean includeTags = false;
            if (json.has("include_tags")) {
                if (!json.get("include_tags").isJsonPrimitive() || !json.getAsJsonPrimitive("include_tags").isBoolean()) {
                    throw new IllegalArgumentException("include_tags must be a boolean");
                }
                includeTags = json.get("include_tags").getAsBoolean();
            }
            return new AiQueryRequest(string(json, "operation"), string(json, "type"), string(json, "id"),
                    string(json, "namespace"), string(json, "query"), string(json, "tag"), string(json, "cursor"), limit, includeTags);
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException("limit must be an integer", exception);
        }
    }

    private static String string(JsonObject json, String field) {
        if (!json.has(field)) return "";
        var value = json.get(field);
        if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
            throw new IllegalArgumentException(field + " must be a string");
        }
        return value.getAsString();
    }

    private static String normalized(String value) {
        return trimmed(value).toLowerCase(Locale.ROOT);
    }

    private static String trimmed(String value) {
        return value == null ? "" : value.trim();
    }
}
