package com.viscript_lib;

import com.lowdragmc.lowdraglib2.Platform;
import com.mojang.logging.LogUtils;
import com.viscript_lib.event.RegisterAccessorEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(ViScriptLib.MOD_ID)
public class ViScriptLib {
    public static final String MOD_ID = "viscript_lib";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ViScriptLib(IEventBus modEventBus, ModContainer modContainer, Dist dist) {
        NeoForge.EVENT_BUS.post(new RegisterAccessorEvent());
    }


    //Just Enough Characters
    public static boolean isJECharactersLoaded() {
        return Platform.isModLoaded("jecharacters");
    }
}