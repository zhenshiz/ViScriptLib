package com.vss_market;

import com.mojang.logging.LogUtils;
import com.vss_market.data.MarketSerializers;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLLoader;
import org.slf4j.Logger;

@Mod(VSSMarket.MOD_ID)
public class VSSMarket {
    public static final String MOD_ID = "vss_market";
    public static final Logger LOGGER = LogUtils.getLogger();

    public VSSMarket() {
        MarketSerializers.register();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.CONFIG_SPEC, "%s_config.toml".formatted(MOD_ID));
/*        if (FMLEnvironment.dist.isClient()) {
            ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, ConfigurationScreen::new);
        }*/
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

    public static boolean isDevEnv() {
        return !FMLLoader.isProduction();
    }

    public static boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }
}
