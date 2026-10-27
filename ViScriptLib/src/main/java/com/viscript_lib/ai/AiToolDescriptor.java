package com.viscript_lib.ai;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

/**
 * 描述附属模组工具的输入格式和权限需求。
 *
 * @param id 稳定的工具标识符
 * @param schemaVersion 输入结构版本，必须为正数
 * @param description 工具用途说明
 * @param capability 执行此工具需要的能力
 * @param requiresConfirmation 是否必须由可信用户确认后才能应用
 * @param inputSchema 工具输入的 JSON Schema；构造和读取时均复制
 */
public record AiToolDescriptor(ResourceLocation id, int schemaVersion, String description,
                                AiCapability capability, boolean requiresConfirmation, JsonObject inputSchema) {
    public AiToolDescriptor {
        if (schemaVersion < 1) throw new IllegalArgumentException("schemaVersion must be positive");
        inputSchema = inputSchema.deepCopy();
    }

    @Override
    public JsonObject inputSchema() {
        return inputSchema.deepCopy();
    }
}
