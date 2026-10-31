package com.viscript_lib.ai;

import com.lowdragmc.lowdraglib2.registry.ILDLRegister;
import com.viscript_lib.ViScriptLib;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

/**
 * 向 AI 运行时提供按页读取的游戏信息。
 *
 * <p>实现通过 LDLib2 自动注册；返回值只能包含稳定 ID 和可序列化数据。
 * 查询不得修改游戏状态，且应通过 {@link AiKnowledgeService} 在服务端线程调用。
 */
public interface AiKnowledgeProvider extends ILDLRegister<AiKnowledgeProvider, Supplier<AiKnowledgeProvider>> {
    String REGISTRY_ID = ViScriptLib.MOD_ID + ":ai_knowledge_provider";

    /**
     * 返回此提供器的稳定标识符。
     *
     * @return 完整的提供器标识符
     */
    ResourceLocation id();

    /**
     * 返回此提供器的查询格式和版本。
     *
     * @return 可供 AI 运行时发现能力的描述
     */
    AiProviderDescriptor descriptor();

    /**
     * 返回读取此提供器数据需要的能力。
     *
     * @return 必须由服务端授予的读取能力
     */
    default AiCapability requiredCapability() {
        return AiCapability.REGISTRY_READ;
    }

    /**
     * 在指定权限上下文中读取一页信息。
     *
     * @param request 已规范化的查询请求
     * @param context 服务端创建的权限上下文
     * @return 数据页或可供展示的错误结果
     */
    AiQueryResult query(AiQueryRequest request, AiAccessContext context);
}
