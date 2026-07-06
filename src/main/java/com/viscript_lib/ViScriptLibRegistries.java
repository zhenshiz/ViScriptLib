package com.viscript_lib;

import com.lowdragmc.lowdraglib2.registry.AutoRegistry;
import com.viscript_lib.register.ICommand;
import com.viscript_lib.register.IContainerHelper;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

public class ViScriptLibRegistries {
    public static AutoRegistry.LDLibRegister<ICommand, Supplier<ICommand>> COMMANDS;
    public static AutoRegistry.LDLibRegister<IContainerHelper, Supplier<IContainerHelper>> ContainerHelper;

    static {
        COMMANDS = AutoRegistry.LDLibRegister
                .create(new ResourceLocation(ICommand.COMMAND_ID), ICommand.class, AutoRegistry::noArgsCreator);
        ContainerHelper = AutoRegistry.LDLibRegister
                .create(new ResourceLocation(IContainerHelper.CONTAINER_HELPER_ID), IContainerHelper.class, AutoRegistry::noArgsCreator);
    }
}
