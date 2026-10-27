package com.viscript_lib.ai;

import net.minecraft.nbt.CompoundTag;

import java.util.List;

/**
 * 描述实际应用修改后的结果。
 *
 * @param applied 请求是否已完整应用
 * @param summary 可供展示的结果摘要
 * @param changes 实际应用的结构化差异；构造和读取时均复制
 * @param errors 可供展示的失败信息
 */
public record AiApplyResult(boolean applied, String summary, List<CompoundTag> changes, List<String> errors) {
    public AiApplyResult {
        changes = changes.stream().map(CompoundTag::copy).toList();
        errors = List.copyOf(errors);
    }

    @Override
    public List<CompoundTag> changes() {
        return changes.stream().map(CompoundTag::copy).toList();
    }
}
