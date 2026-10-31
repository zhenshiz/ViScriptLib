package com.viscript_lib.ai;

import com.lowdragmc.lowdraglib2.registry.AutoRegistry;
import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import com.viscript_lib.ViScriptLib;
import com.viscript_lib.ViScriptLibRegistries;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * 查找并在服务端权限范围内调用已注册的知识提供器。
 */
public final class AiKnowledgeService {
    private AiKnowledgeService() {
    }

    /**
     * 为每个已注册的知识提供器创建一个实例。
     *
     * @return 不可修改的提供器实例列表
     */
    public static Collection<AiKnowledgeProvider> providers() {
        var result = new ArrayList<AiKnowledgeProvider>();
        for (AutoRegistry.Holder<LDLRegister, AiKnowledgeProvider, Supplier<AiKnowledgeProvider>> holder
                : ViScriptLibRegistries.AI_KNOWLEDGE_PROVIDERS) {
            try {
                var provider = holder.value().get();
                if (provider != null) result.add(provider);
            } catch (RuntimeException exception) {
                ViScriptLib.LOGGER.error("Cannot create AI knowledge provider {}", holder.clazz().getName(), exception);
            }
        }
        return List.copyOf(result);
    }

    /**
     * 按稳定标识符查找知识提供器。
     *
     * @param id 提供器标识符
     * @return 匹配的提供器；不存在时为空
     */
    public static Optional<AiKnowledgeProvider> find(ResourceLocation id) {
        if (id == null) return Optional.empty();
        return providers().stream().filter(provider -> id.equals(provider.id())).findFirst();
    }

    /**
     * 在当前服务端权限范围内执行知识查询。
     *
     * @param providerId 提供器标识符
     * @param request 查询请求
     * @param context 服务端权限上下文
     * @return 查询结果；提供器不存在、权限不足或执行失败时返回错误结果
     */
    public static AiQueryResult query(ResourceLocation providerId, AiQueryRequest request,
                                      AiAccessContext context) {
        java.util.Objects.requireNonNull(providerId, "providerId");
        if (context == null) {
            return AiQueryResult.failure(providerId, 1, "AI permission was not granted");
        }
        if (!context.level().getServer().isSameThread()) {
            return AiQueryResult.failure(providerId, 1, "AI queries require the server thread");
        }
        if (request == null) return AiQueryResult.failure(providerId, 1, "A query request is required");
        var provider = find(providerId);
        if (provider.isEmpty()) {
            return AiQueryResult.failure(providerId, 1, "Unknown AI knowledge provider");
        }
        if (!context.allows(provider.get().requiredCapability())) {
            return AiQueryResult.failure(providerId, 1, "AI permission was not granted");
        }
        try {
            var result = provider.get().query(request, context);
            if (!result.providerId().equals(providerId) || result.entries().size() > request.limit()) {
                return AiQueryResult.failure(providerId, 1, "Knowledge provider returned an invalid or oversized page");
            }
            return result;
        } catch (RuntimeException exception) {
            ViScriptLib.LOGGER.error("AI knowledge provider {} failed", providerId, exception);
            return AiQueryResult.failure(providerId, 1, "Knowledge provider failed to process the request");
        }
    }
}
