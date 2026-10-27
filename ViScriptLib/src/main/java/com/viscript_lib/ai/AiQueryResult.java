package com.viscript_lib.ai;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * 返回知识查询的数据页、环境版本或可供展示的错误。
 *
 * @param providerId 产生结果的提供器标识符
 * @param schemaVersion 提供器数据结构版本
 * @param entries 数据条目
 * @param nextCursor 下一页游标；查询结束时为空字符串
 * @param environment 运行环境和数据版本快照
 * @param error 错误说明；成功时为空字符串
 */
public record AiQueryResult(ResourceLocation providerId, int schemaVersion, List<CompoundTag> entries,
                             String nextCursor, CompoundTag environment, String error) {
    public AiQueryResult {
        entries = entries.stream().map(CompoundTag::copy).toList();
        environment = environment.copy();
    }

    @Override
    public List<CompoundTag> entries() {
        return entries.stream().map(CompoundTag::copy).toList();
    }

    @Override
    public CompoundTag environment() {
        return environment.copy();
    }

    /**
     * 创建成功的数据页并附上当前环境版本。
     *
     * @param providerId 提供器标识符
     * @param schemaVersion 数据结构版本
     * @param entries 数据条目
     * @param nextCursor 下一页游标或空字符串
     * @return 成功结果
     */
    public static AiQueryResult success(ResourceLocation providerId, int schemaVersion,
                                         List<CompoundTag> entries, String nextCursor) {
        return new AiQueryResult(providerId, schemaVersion, entries, nextCursor, AiEnvironment.snapshot(), "");
    }

    /**
     * 创建不包含数据的失败结果。
     *
     * @param providerId 提供器标识符
     * @param schemaVersion 数据结构版本
     * @param error 可供展示的错误说明
     * @return 失败结果
     */
    public static AiQueryResult failure(ResourceLocation providerId, int schemaVersion, String error) {
        return new AiQueryResult(providerId, schemaVersion, List.of(), "", new CompoundTag(), error);
    }

    /**
     * 检查查询是否成功。
     *
     * @return 没有错误时为 {@code true}
     */
    public boolean successful() {
        return error.isEmpty();
    }

    /**
     * 检查是否还有下一页数据。
     *
     * @return 下一页游标非空时为 {@code true}
     */
    public boolean hasMore() {
        return !nextCursor.isEmpty();
    }

    /**
     * 转换为供 AI 运行时使用的普通 JSON 数据。
     *
     * @return 不包含可变 Minecraft 对象的 JSON 结果副本
     */
    public JsonObject toJson() {
        var result = new JsonObject();
        result.addProperty("provider_id", providerId.toString());
        result.addProperty("schema_version", schemaVersion);
        result.addProperty("successful", successful());
        result.addProperty("has_more", hasMore());
        result.addProperty("next_cursor", nextCursor);
        result.addProperty("error", error);
        result.add("environment", NbtOps.INSTANCE.convertTo(JsonOps.INSTANCE, environment));
        var items = new com.google.gson.JsonArray();
        entries.forEach(entry -> items.add(NbtOps.INSTANCE.convertTo(JsonOps.INSTANCE, entry)));
        result.add("entries", items);
        return result;
    }
}
