package com.viscript_lib;

import com.lowdragmc.lowdraglib2.Platform;
import com.mojang.logging.LogUtils;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod(ViScriptLib.MOD_ID)
@Mod.EventBusSubscriber(modid = ViScriptLib.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ViScriptLib {
    public static final String MOD_ID = "viscript_lib";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ViScriptLib() {
    }

    //注册指令
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        for (var command : ViScriptLibRegistries.COMMANDS) {
            command.value().get().register(event.getDispatcher(), event.getBuildContext(), event.getCommandSelection());
        }
    }

    //Just Enough Characters
    public static boolean isJECharactersLoaded() {
        return Platform.isModLoaded("jecharacters");
    }
}
