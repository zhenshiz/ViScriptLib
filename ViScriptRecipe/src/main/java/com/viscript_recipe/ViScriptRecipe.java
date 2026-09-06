package com.viscript_recipe;

import com.lowdragmc.lowdraglib2.gui.factory.PlayerUIMenuType;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.mojang.logging.LogUtils;
import com.viscript_recipe.client.RecipeDeltaClientEvents;
import com.viscript_recipe.gui.editor.RecipeEditor;
import com.viscript_recipe.recipe.RecipeDeltaServerEvents;
import com.viscript_recipe.recipe.importer.RecipeImporter;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLLoader;
import org.slf4j.Logger;

@Mod(ViScriptRecipe.MOD_ID)
public class ViScriptRecipe {
    public static final String MOD_ID = "viscript_recipe";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final ResourceLocation placeholder = id("placeholder");

    public ViScriptRecipe() {
        RecipeDeltaServerEvents.register();
        PlayerUIMenuType.register(RecipeEditor.WINDOW_ID, ignored -> player -> {
            if (player.level().isClientSide) {
                return RecipeEditor.createUI();
            }
            return new ModularUI(UI.empty());
        });
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.CONFIG_SPEC, Config.CONFIG_FILE_NAME);
        if (isClient()) RecipeDeltaClientEvents.register();
        for (var holder : IModModule.MODULES) {
            var module = holder.value().get();
            module.registerEditorTypes();
            RecipeImporter.HANDLERS.add(module.importHandler());
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

    public static boolean isDevEnv() {
        return !FMLLoader.isProduction();
    }

    public static boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }
}
