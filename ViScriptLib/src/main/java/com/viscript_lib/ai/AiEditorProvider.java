package com.viscript_lib.ai;

import com.lowdragmc.lowdraglib2.registry.ILDLRegister;
import com.viscript_lib.ViScriptLib;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

/**
 * 将附属模组的工程和运行时文档接入统一 AI 扩展接口。
 *
 * <p>实现负责领域校验、版本冲突检测、差异生成及文件事务。应用时必须检查
 * {@code expectedRevision}，拒绝覆盖预览后已被修改的目标；覆盖文件时必须保留备份。
 * 此接口不提供任意路径写入能力。
 */
public interface AiEditorProvider extends ILDLRegister<AiEditorProvider, Supplier<AiEditorProvider>> {
    String REGISTRY_ID = ViScriptLib.MOD_ID + ":ai_editor_provider";

    /**
     * 返回编辑器的稳定标识符。
     *
     * @return 完整的编辑器标识符
     */
    ResourceLocation id();

    /**
     * 返回支持的文档种类和结构版本。
     *
     * @return 编辑器描述
     */
    AiEditorDescriptor descriptor();

    /**
     * 校验文档、逻辑目标和预期版本，不产生修改。
     *
     * @param request 文档修改请求
     * @param context 服务端权限上下文
     * @return 校验结果
     */
    AiValidationResult validate(AiEditRequest request, AiAccessContext context);

    /**
     * 生成相对于当前版本的修改预览，不产生修改。
     *
     * @param request 文档修改请求
     * @param context 服务端权限上下文
     * @return 变更预览
     */
    AiPreviewResult preview(AiEditRequest request, AiAccessContext context);

    /**
     * 通过编辑器的正常运行路径应用已授权并确认的修改。
     *
     * <p>实现必须重新校验权限、文档和版本，并遵守
     * 编辑器的用户确认策略。确认不能由模型请求中的布尔字段替代，本地设置也不能替代服务端授权。
     *
     * @param request 已授权并确认的文档修改请求
     * @param context 当前服务端权限上下文
     * @return 实际应用结果
     */
    AiApplyResult apply(AiEditRequest request, AiAccessContext context);
}
