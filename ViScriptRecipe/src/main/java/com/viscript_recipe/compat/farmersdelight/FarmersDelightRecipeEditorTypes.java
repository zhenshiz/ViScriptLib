package com.viscript_recipe.compat.farmersdelight;

import com.viscript_recipe.compat.farmersdelight.canvas.FarmerCookingPotCanvas;
import com.viscript_recipe.compat.farmersdelight.canvas.FarmerCuttingCanvas;
import com.viscript_recipe.compat.farmersdelight.data.FarmerCookingPotRecipeData;
import com.viscript_recipe.compat.farmersdelight.data.FarmerCuttingRecipeData;
import com.viscript_recipe.data.RecipeEditorCategory;
import com.viscript_recipe.data.RecipeEditorType;
import com.viscript_recipe.data.RecipeEditorTypes;
import net.minecraft.resources.ResourceLocation;

public final class FarmersDelightRecipeEditorTypes {
    public static final String MOD_ID = "farmersdelight";

    public static final ResourceLocation COOKING_POT = farmer("cooking_pot");
    public static final ResourceLocation CUTTING_BOARD = farmer("cutting_board");
    public static final ResourceLocation COOKING = farmer("cooking");
    public static final ResourceLocation CUTTING = farmer("cutting");

    private static boolean registered;

    private FarmersDelightRecipeEditorTypes() {
    }

    public static synchronized void registerAll() {
        if (registered) {
            return;
        }
        registered = true;
        registerCategories();
        registerTypes();
    }

    private static void registerCategories() {
        RecipeEditorTypes.registerCategory(RecipeEditorCategory.of(
                COOKING_POT,
                "viscript_recipe.editor.category.farmersdelight.cooking_pot",
                MOD_ID, COOKING
        ));
        RecipeEditorTypes.registerCategory(RecipeEditorCategory.of(
                CUTTING_BOARD,
                "viscript_recipe.editor.category.farmersdelight.cutting_board",
                MOD_ID, CUTTING
        ));
    }

    private static void registerTypes() {
        RecipeEditorTypes.register(RecipeEditorType.of(
                COOKING, COOKING_POT,
                "viscript_recipe.editor.type.farmersdelight.cooking",
                FarmerCookingPotRecipeData.class, FarmerCookingPotRecipeData::new,
                FarmerCookingPotCanvas::new, MOD_ID
        ));
        RecipeEditorTypes.register(RecipeEditorType.of(
                CUTTING, CUTTING_BOARD,
                "viscript_recipe.editor.type.farmersdelight.cutting",
                FarmerCuttingRecipeData.class, FarmerCuttingRecipeData::new,
                FarmerCuttingCanvas::new, MOD_ID
        ));
    }

    private static ResourceLocation farmer(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
