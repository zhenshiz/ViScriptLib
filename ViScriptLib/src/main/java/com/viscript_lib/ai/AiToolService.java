package com.viscript_lib.ai;

import com.lowdragmc.lowdraglib2.registry.AutoRegistry;
import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import com.viscript_lib.ViScriptLibRegistries;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * 查找 VSL 和附属模组注册的 AI 工具。
 */
public final class AiToolService {
    private AiToolService() {
    }

    /**
     * 为每个已注册的 AI 工具创建一个实例。
     *
     * @return 不可修改的工具实例列表
     */
    public static Collection<AiToolProvider> providers() {
        var result = new ArrayList<AiToolProvider>();
        for (AutoRegistry.Holder<LDLRegister, AiToolProvider, Supplier<AiToolProvider>> holder
                : ViScriptLibRegistries.AI_TOOL_PROVIDERS) {
            var provider = holder.value().get();
            if (provider != null) result.add(provider);
        }
        return List.copyOf(result);
    }

    /**
     * 按稳定标识符查找工具。
     *
     * @param id 工具标识符
     * @return 匹配的工具；不存在时为空
     */
    public static Optional<AiToolProvider> find(ResourceLocation id) {
        if (id == null) return Optional.empty();
        return providers().stream().filter(provider -> id.equals(provider.id())).findFirst();
    }
}
