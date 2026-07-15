package com.viscript_recipe.recipe;

import com.lowdragmc.lowdraglib2.LDLib2;
import com.viscript_recipe.ViScriptRecipe;

import java.nio.file.Path;

public final class RecipeAssetPaths {
    public static final String RECIPE_SUFFIX = ".recipe";

    private RecipeAssetPaths() {
    }

    public static Path recipeDirectory() {
        return LDLib2.getAssetsDir().toPath().resolve(ViScriptRecipe.MOD_ID).resolve("recipes");
    }
}
