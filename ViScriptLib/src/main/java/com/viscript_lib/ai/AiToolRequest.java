package com.viscript_lib.ai;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

/**
 * 携带单次工具调用的结构化参数。
 *
 * @param toolId 完整的工具标识符
 * @param schemaVersion 请求采用的输入结构版本
 * @param input 参数数据；构造和读取时均复制
 */
public record AiToolRequest(ResourceLocation toolId, int schemaVersion, CompoundTag input) {
    public AiToolRequest {
        input = input.copy();
    }

    @Override
    public CompoundTag input() {
        return input.copy();
    }
}
