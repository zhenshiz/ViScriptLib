package com.viscript_team;

import com.viscript_team.event.FactionEvents;
import com.viscript_team.client.FactionClientEvents;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

@Mod(ViScriptTeam.MOD_ID)
public class ViScriptTeam {
    public static final String MOD_ID = "viscript_team";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ViScriptTeam() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.CONFIG_SPEC, "%s_config.toml".formatted(MOD_ID));
        MinecraftForge.EVENT_BUS.register(FactionEvents.class);
        if (isClient()) {
            MinecraftForge.EVENT_BUS.register(FactionClientEvents.class);
            //ModLoadingContext.get().registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        }
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    public static String formattedMod(String path) {
        return ("%s:" + path).formatted(MOD_ID);
    }

    public static boolean isPresentResource(ResourceLocation resourceLocation) {
        return Minecraft.getInstance().getResourceManager().getResource(resourceLocation).isPresent();
    }

    public static boolean isClient() {
        return FMLEnvironment.dist.isClient();
    }
}
