package com.viscript_lib.ai;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * 描述知识提供器的查询格式和版本。
 *
 * @param id 稳定的提供器标识符
 * @param schemaVersion 输入输出结构版本，必须为正数
 * @param description 提供器能力说明
 * @param operations 支持的查询操作名称
 * @param inputSchema 查询参数的 JSON Schema；构造和读取时均复制
 */
public record AiProviderDescriptor(ResourceLocation id, int schemaVersion, String description,
                                    List<String> operations, JsonObject inputSchema) {
    public AiProviderDescriptor {
        if (schemaVersion < 1) throw new IllegalArgumentException("schemaVersion must be positive");
        operations = List.copyOf(operations);
        inputSchema = inputSchema.deepCopy();
    }

    @Override
    public JsonObject inputSchema() {
        return inputSchema.deepCopy();
    }
}
