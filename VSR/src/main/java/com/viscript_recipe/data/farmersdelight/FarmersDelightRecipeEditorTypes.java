package com.viscript_recipe.data.farmersdelight;

import com.viscript_recipe.data.RecipeEditorCategory;
import com.viscript_recipe.data.RecipeEditorType;
import com.viscript_recipe.data.RecipeEditorTypes;
import com.viscript_recipe.gui.canvas.farmersdelight.FarmerCookingPotCanvas;
import com.viscript_recipe.gui.canvas.farmersdelight.FarmerCuttingCanvas;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public final class FarmersDelightRecipeEditorTypes {
    public static final String MOD_ID = "farmersdelight";

    public static final ResourceLocation COOKING_POT = farmer("cooking_pot");
    public static final ResourceLocation CUTTING_BOARD = farmer("cutting_board");
    public static final ResourceLocation COOKING = farmer("cooking");
    public static final ResourceLocation CUTTING = farmer("cutting");

    private static final List<String> REQUIRED_MODS = List.of(MOD_ID);
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
        RecipeEditorTypes.registerCategory(new RecipeEditorCategory(
                COOKING_POT,
                "viscript_recipe.editor.category.farmersdelight.cooking_pot",
                MOD_ID,
                REQUIRED_MODS,
                COOKING
        ));
        RecipeEditorTypes.registerCategory(new RecipeEditorCategory(
                CUTTING_BOARD,
                "viscript_recipe.editor.category.farmersdelight.cutting_board",
                MOD_ID,
                REQUIRED_MODS,
                CUTTING
        ));
    }

    private static void registerTypes() {
        RecipeEditorTypes.register(new RecipeEditorType(
                COOKING, COOKING_POT,
                "viscript_recipe.editor.type.farmersdelight.cooking",
                FarmerCookingPotRecipeData.class, FarmerCookingPotRecipeData::new,
                FarmerCookingPotCanvas::new, MOD_ID
        ));
        RecipeEditorTypes.register(new RecipeEditorType(
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
