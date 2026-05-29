package com.viscript_lib;

import com.lowdragmc.lowdraglib2.Platform;
import com.lowdragmc.lowdraglib2.registry.AutoRegistry;
import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import com.mojang.logging.LogUtils;
import com.viscript_lib.event.RegisterAccessorEvent;
import com.viscript_lib.register.ICommand;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.slf4j.Logger;

import java.util.function.Supplier;

@Mod(ViScriptLib.MOD_ID)
public class ViScriptLib {
    public static final String MOD_ID = "viscript_lib";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ViScriptLib(IEventBus modEventBus, ModContainer modContainer, Dist dist) {
        NeoForge.EVENT_BUS.post(new RegisterAccessorEvent());
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
    }

    //注册指令
    private void onRegisterCommands(RegisterCommandsEvent event) {
        for (AutoRegistry.Holder<LDLRegister, ICommand, Supplier<ICommand>> command : ViScriptLibRegistries.COMMANDS) {
            command.value().get().register(event.getDispatcher(), event.getBuildContext(), event.getCommandSelection());
        }
    }

    //Just Enough Characters
    public static boolean isJECharactersLoaded() {
        return Platform.isModLoaded("jecharacters");
    }
}