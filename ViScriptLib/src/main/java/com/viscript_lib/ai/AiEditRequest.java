package com.viscript_lib.ai;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

/**
 * 携带带有并发版本约束的文档修改请求。
 *
 * @param editorId 编辑器提供器标识符
 * @param schemaVersion 请求采用的文档结构版本
 * @param kind 目标文档种类
 * @param target 提供器定义的逻辑资源名称；不得直接作为任意文件路径使用
 * @param expectedRevision 预期旧版本；仅新建目标时可为空字符串
 * @param document 新文档；构造和读取时均复制
 */
public record AiEditRequest(ResourceLocation editorId, int schemaVersion, Kind kind, String target,
                             String expectedRevision, CompoundTag document) {
    /** 区分工程保存和运行时应用的权限。 */
    public enum Kind {
        /** 仅编辑工程文档。 */
        PROJECT,
        /** 应用运行时文档。 */
        RUNTIME
    }

    public AiEditRequest {
        target = target.trim();
        expectedRevision = expectedRevision.trim();
        document = document.copy();
    }

    @Override
    public CompoundTag document() {
        return document.copy();
    }
}
