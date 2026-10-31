package com.viscript_lib;

import com.lowdragmc.lowdraglib2.registry.AutoRegistry;
import com.viscript_lib.ai.AiEditorProvider;
import com.viscript_lib.ai.AiKnowledgeProvider;
import com.viscript_lib.ai.AiToolProvider;
import com.viscript_lib.register.ICommand;
import com.viscript_lib.register.IContainerHelper;
import com.viscript_lib.stage.PlayerStages;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.registries.DeferredRegister;
import net.nikdo53.neobackports.io.attachment.AdvancedCapabilityType;
import net.nikdo53.neobackports.io.attachment.AttachmentType;
import net.nikdo53.neobackports.io.attachment.DataAttachment;
import net.nikdo53.neobackports.registry.NeoForgeRegistries;

import java.util.function.Supplier;

public class ViScriptLibRegistries {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, ViScriptLib.MOD_ID);
    public static class PlayerStagesAttachment extends DataAttachment<PlayerStages> {}
    public static final Capability<PlayerStagesAttachment> STAGES_CAP = CapabilityManager.get(new CapabilityToken<>() {});
    public static final Supplier<AttachmentType<PlayerStages>> STAGES = ATTACHMENT_TYPES.register("stages",
            () -> AttachmentType.builder(STAGES_CAP, PlayerStages::new).canAttachTo(AdvancedCapabilityType.PLAYER)
                    .serialize(PlayerStages.CODEC).sync(PlayerStages.STREAM_CODEC)
                    .copyOnDeath().build());

    public static AutoRegistry.LDLibRegister<ICommand, Supplier<ICommand>> COMMANDS;
    public static AutoRegistry.LDLibRegister<IContainerHelper, Supplier<IContainerHelper>> ContainerHelper;
    public static AutoRegistry.LDLibRegister<AiKnowledgeProvider, Supplier<AiKnowledgeProvider>> AI_KNOWLEDGE_PROVIDERS;
    public static AutoRegistry.LDLibRegister<AiToolProvider, Supplier<AiToolProvider>> AI_TOOL_PROVIDERS;
    public static AutoRegistry.LDLibRegister<AiEditorProvider, Supplier<AiEditorProvider>> AI_EDITOR_PROVIDERS;

    static {
        COMMANDS = AutoRegistry.LDLibRegister
                .create(new ResourceLocation(ICommand.COMMAND_ID), ICommand.class, AutoRegistry::noArgsCreator);
        ContainerHelper = AutoRegistry.LDLibRegister
                .create(new ResourceLocation(IContainerHelper.CONTAINER_HELPER_ID), IContainerHelper.class, AutoRegistry::noArgsCreator);
        AI_KNOWLEDGE_PROVIDERS = AutoRegistry.LDLibRegister
                .create(new ResourceLocation(AiKnowledgeProvider.REGISTRY_ID), AiKnowledgeProvider.class, AutoRegistry::noArgsCreator);
        AI_TOOL_PROVIDERS = AutoRegistry.LDLibRegister
                .create(new ResourceLocation(AiToolProvider.REGISTRY_ID), AiToolProvider.class, AutoRegistry::noArgsCreator);
        AI_EDITOR_PROVIDERS = AutoRegistry.LDLibRegister
                .create(new ResourceLocation(AiEditorProvider.REGISTRY_ID), AiEditorProvider.class, AutoRegistry::noArgsCreator);
    }
}
