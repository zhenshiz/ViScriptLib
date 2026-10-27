package com.viscript_lib.ai;

import com.lowdragmc.lowdraglib2.registry.ILDLRegister;
import com.viscript_lib.ViScriptLib;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

/**
 * 为附属模组提供结构化工具扩展接口。
 *
 * <p>此接口供可信的服务端运行时使用，不是客户端或模型可直接调用的网络入口。
 * 实现必须在应用前重新验证权限和输入，并根据服务端策略完成用户确认及审计。
 */
public interface AiToolProvider extends ILDLRegister<AiToolProvider, Supplier<AiToolProvider>> {
    String REGISTRY_ID = ViScriptLib.MOD_ID + ":ai_tool_provider";

    /**
     * 返回此工具的稳定标识符。
     *
     * @return 完整的工具标识符
     */
    ResourceLocation id();

    /**
     * 返回输入格式、权限和确认要求。
     *
     * @return 工具描述
     */
    AiToolDescriptor descriptor();

    /**
     * 校验请求，不改变游戏、文件或工程状态。
     *
     * @param request 结构化工具请求
     * @param context 服务端权限上下文
     * @return 校验结果
     */
    AiValidationResult validate(AiToolRequest request, AiAccessContext context);

    /**
     * 生成变更预览，不改变游戏、文件或工程状态。
     *
     * @param request 结构化工具请求
     * @param context 服务端权限上下文
     * @return 变更预览
     */
    AiPreviewResult preview(AiToolRequest request, AiAccessContext context);

    /**
     * 应用已经由可信运行时授权并确认的请求。
     *
     * <p>实现必须在服务端线程重新校验当前权限和请求内容；确认状态不能取自模型输出。
     *
     * @param request 已授权的结构化工具请求
     * @param context 当前服务端权限上下文
     * @return 实际应用结果
     */
    AiApplyResult apply(AiToolRequest request, AiAccessContext context);
}
