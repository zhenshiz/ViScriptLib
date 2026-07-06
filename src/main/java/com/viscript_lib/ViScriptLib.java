package com.viscript_lib;

import com.lowdragmc.lowdraglib2.Platform;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod(ViScriptLib.MOD_ID)
public class ViScriptLib {
    public static final String MOD_ID = "viscript_lib";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ViScriptLib() {
        MinecraftForge.EVENT_BUS.addListener(this::onRegisterCommands);
    }

    //注册指令
    private void onRegisterCommands(RegisterCommandsEvent event) {
        for (var command : ViScriptLibRegistries.COMMANDS) {
            command.value().get().register(event.getDispatcher(), event.getBuildContext(), event.getCommandSelection());
        }
    }

    //Just Enough Characters
    public static boolean isJECharactersLoaded() {
        return Platform.isModLoaded("jecharacters");
    }
}
