package com.viscript_lib.ai;

import net.minecraft.nbt.CompoundTag;

import java.util.List;

/**
 * 描述一次不产生副作用的修改预览。
 *
 * @param valid 请求是否可应用；存在错误时自动设为 {@code false}
 * @param summary 可供展示的修改摘要
 * @param changes 结构化差异；构造和读取时均复制
 * @param errors 可供展示的校验错误
 */
public record AiPreviewResult(boolean valid, String summary, List<CompoundTag> changes, List<String> errors) {
    public AiPreviewResult {
        changes = changes.stream().map(CompoundTag::copy).toList();
        errors = List.copyOf(errors);
        valid = valid && errors.isEmpty();
    }

    @Override
    public List<CompoundTag> changes() {
        return changes.stream().map(CompoundTag::copy).toList();
    }
}
