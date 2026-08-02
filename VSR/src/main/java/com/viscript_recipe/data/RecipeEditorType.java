package com.viscript_recipe.data;

import com.viscript_recipe.ViScriptRecipe;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.views.NavigationView;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BiFunction;
import java.util.function.Supplier;

public record RecipeEditorType(
        ResourceLocation id, ResourceLocation category, String translationKey,
        Class<? extends IVSRecipeData> dataClass, Supplier<? extends IVSRecipeData> dataSupplier,
        BiFunction<NavigationView, RecipeEntry, RecipeCanvas<?>> canvasSupplier,
        String... requiredMods
) {
    public static RecipeEditorType of(ResourceLocation id, ResourceLocation category, String translationKey,
                                      Class<? extends IVSRecipeData> dataClass, Supplier<? extends IVSRecipeData> dataSupplier,
                                      BiFunction<NavigationView, RecipeEntry, RecipeCanvas<?>> canvasSupplier,
                                      String... requiredMods) {
        return new RecipeEditorType(id, category, translationKey, dataClass, dataSupplier, canvasSupplier, requiredMods);
    }

    public boolean isAvailable() {
        for (var modId : requiredMods) {
            if (!ViScriptRecipe.isModLoaded(modId)) return false;
        }
        return true;
    }

    public Component displayName() {
        return Component.translatable(translationKey);
    }
}
