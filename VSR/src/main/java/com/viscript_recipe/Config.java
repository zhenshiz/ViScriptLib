package com.viscript_recipe;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLLoader;

import java.nio.file.Files;
import java.nio.file.Path;

public class Config {
    public static final String CONFIG_FILE_NAME = ViScriptRecipe.MOD_ID + "_config.toml";
    private static final String SHOWCASE_ONLY_PATH = "recipes.showcase_only_viscript_recipes";

    public static final ForgeConfigSpec CONFIG_SPEC;
    public static final ForgeConfigSpec.BooleanValue SHOWCASE_ONLY_VISCRIPT_RECIPES;

    static {
        ForgeConfigSpec.Builder CONFIG_BUILDER = new ForgeConfigSpec.Builder();
        CONFIG_BUILDER.push("recipes");
        SHOWCASE_ONLY_VISCRIPT_RECIPES = CONFIG_BUILDER
                .translation("viscript_recipe.configuration.showcase_only_viscript_recipes")
                .define("showcase_only_viscript_recipes", false);
        CONFIG_BUILDER.pop();
        CONFIG_SPEC = CONFIG_BUILDER.build();
    }

    public static void reloadRuntimeConfigFromDisk() {
        var path = configPath();
        if (path == null || !Files.isRegularFile(path)) {
            return;
        }
        try (var config = CommentedFileConfig.of(path)) {
            config.load();
            CONFIG_SPEC.correct(config);
            reloadBoolean(config, SHOWCASE_ONLY_PATH, SHOWCASE_ONLY_VISCRIPT_RECIPES, path);
        } catch (Exception e) {
            ViScriptRecipe.LOGGER.warn("Failed to reload ViScriptRecipe config from disk", e);
        }
    }

    private static void reloadBoolean(CommentedFileConfig config, String configPath, ForgeConfigSpec.BooleanValue value, Path filePath) {
        Object rawValue = config.get(configPath);
        if (rawValue instanceof Boolean booleanValue) {
            value.set(booleanValue);
            return;
        }
        ViScriptRecipe.LOGGER.warn("Ignoring invalid {} value in {}: {}", configPath, filePath, rawValue);
    }

    private static Path configPath() {
        String fileName = ConfigTracker.INSTANCE.getConfigFileName(ViScriptRecipe.MOD_ID, ModConfig.Type.COMMON);
        if (CONFIG_FILE_NAME.equals(fileName)) {
            return FMLLoader.getGamePath().resolve("config/" + CONFIG_FILE_NAME);
        }
        return null;
    }
}
