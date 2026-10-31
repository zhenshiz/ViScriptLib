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
 * 查找 VSL 和附属模组注册的 AI 编辑器。
 */
public final class AiEditorService {
    private AiEditorService() {
    }

    /**
     * 为每个已注册的 AI 编辑器创建一个实例。
     *
     * @return 不可修改的编辑器实例列表
     */
    public static Collection<AiEditorProvider> providers() {
        var result = new ArrayList<AiEditorProvider>();
        for (AutoRegistry.Holder<LDLRegister, AiEditorProvider, Supplier<AiEditorProvider>> holder
                : ViScriptLibRegistries.AI_EDITOR_PROVIDERS) {
            var provider = holder.value().get();
            if (provider != null) result.add(provider);
        }
        return List.copyOf(result);
    }

    /**
     * 按稳定标识符查找编辑器。
     *
     * @param id 编辑器标识符
     * @return 匹配的编辑器；不存在时为空
     */
    public static Optional<AiEditorProvider> find(ResourceLocation id) {
        if (id == null) return Optional.empty();
        return providers().stream().filter(provider -> id.equals(provider.id())).findFirst();
    }
}
