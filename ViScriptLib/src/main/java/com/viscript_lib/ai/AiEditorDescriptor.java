package com.viscript_lib.ai;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

/**
 * 描述可接入 AI 的附属编辑器。
 *
 * @param id 稳定的编辑器标识符
 * @param schemaVersion 文档结构版本，必须为正数
 * @param description 编辑器用途说明
 * @param supportsProject 是否支持工程文档
 * @param supportsRuntime 是否支持运行时文档
 * @param requiresConfirmation 是否必须由可信用户确认后才能应用
 * @param documentSchema 可编辑文档的 JSON Schema；构造和读取时均复制
 */
public record AiEditorDescriptor(ResourceLocation id, int schemaVersion, String description,
                                  boolean supportsProject, boolean supportsRuntime,
                                  boolean requiresConfirmation, JsonObject documentSchema) {
    public AiEditorDescriptor {
        if (schemaVersion < 1) throw new IllegalArgumentException("schemaVersion must be positive");
        documentSchema = documentSchema.deepCopy();
    }

    @Override
    public JsonObject documentSchema() {
        return documentSchema.deepCopy();
    }
}
