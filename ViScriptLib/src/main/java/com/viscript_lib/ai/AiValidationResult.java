package com.viscript_lib.ai;

import java.util.List;

/**
 * 描述不产生副作用的校验结果。
 *
 * @param valid 请求是否有效；存在错误时自动设为 {@code false}
 * @param errors 可供展示的校验错误
 */
public record AiValidationResult(boolean valid, List<String> errors) {
    public AiValidationResult {
        errors = List.copyOf(errors);
        valid = valid && errors.isEmpty();
    }

    /**
     * 创建无错误的校验结果。
     *
     * @return 有效的校验结果
     */
    public static AiValidationResult success() {
        return new AiValidationResult(true, List.of());
    }

    /**
     * 创建无效的校验结果。
     *
     * @param errors 可供展示的错误列表
     * @return 无效的校验结果
     */
    public static AiValidationResult invalid(List<String> errors) {
        return new AiValidationResult(false, errors);
    }
}
