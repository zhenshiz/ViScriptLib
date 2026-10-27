package com.viscript_lib;

import com.lowdragmc.lowdraglib2.registry.AutoRegistry;
import com.viscript_lib.ai.AiEditorProvider;
import com.viscript_lib.ai.AiKnowledgeProvider;
import com.viscript_lib.ai.AiToolProvider;
import com.viscript_lib.register.ICommand;
import com.viscript_lib.register.IContainerHelper;
import com.viscript_lib.stage.PlayerStages;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ViScriptLibRegistries {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, ViScriptLib.MOD_ID);
    public static final Supplier<AttachmentType<PlayerStages>> STAGES = ATTACHMENT_TYPES.register("stages",
            () -> AttachmentType.builder(PlayerStages::new)
                    .serialize(PlayerStages.CODEC)
                    .sync((holder, player) -> holder == player, PlayerStages.STREAM_CODEC)
                    .copyOnDeath()
                    .build());

    public static AutoRegistry.LDLibRegister<ICommand, Supplier<ICommand>> COMMANDS;
    public static AutoRegistry.LDLibRegister<IContainerHelper, Supplier<IContainerHelper>> ContainerHelper;
    public static AutoRegistry.LDLibRegister<AiKnowledgeProvider, Supplier<AiKnowledgeProvider>> AI_KNOWLEDGE_PROVIDERS;
    public static AutoRegistry.LDLibRegister<AiToolProvider, Supplier<AiToolProvider>> AI_TOOL_PROVIDERS;
    public static AutoRegistry.LDLibRegister<AiEditorProvider, Supplier<AiEditorProvider>> AI_EDITOR_PROVIDERS;

    static {
        COMMANDS = AutoRegistry.LDLibRegister
                .create(ResourceLocation.parse(ICommand.COMMAND_ID), ICommand.class, AutoRegistry::noArgsCreator);
        ContainerHelper = AutoRegistry.LDLibRegister
                .create(ResourceLocation.parse(IContainerHelper.CONTAINER_HELPER_ID), IContainerHelper.class, AutoRegistry::noArgsCreator);
        AI_KNOWLEDGE_PROVIDERS = AutoRegistry.LDLibRegister
                .create(ResourceLocation.parse(AiKnowledgeProvider.REGISTRY_ID), AiKnowledgeProvider.class, AutoRegistry::noArgsCreator);
        AI_TOOL_PROVIDERS = AutoRegistry.LDLibRegister
                .create(ResourceLocation.parse(AiToolProvider.REGISTRY_ID), AiToolProvider.class, AutoRegistry::noArgsCreator);
        AI_EDITOR_PROVIDERS = AutoRegistry.LDLibRegister
                .create(ResourceLocation.parse(AiEditorProvider.REGISTRY_ID), AiEditorProvider.class, AutoRegistry::noArgsCreator);
    }
}
