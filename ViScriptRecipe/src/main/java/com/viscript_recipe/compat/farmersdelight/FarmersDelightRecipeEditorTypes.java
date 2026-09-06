package com.viscript_recipe.compat.farmersdelight;

import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import com.viscript_recipe.IModModule;
import com.viscript_recipe.compat.farmersdelight.canvas.FarmerCookingPotCanvas;
import com.viscript_recipe.compat.farmersdelight.canvas.FarmerCuttingCanvas;
import com.viscript_recipe.compat.farmersdelight.data.FarmerCookingPotRecipeData;
import com.viscript_recipe.compat.farmersdelight.data.FarmerCuttingRecipeData;
import com.viscript_recipe.data.RecipeEditorCategory;
import com.viscript_recipe.data.RecipeEditorType;
import com.viscript_recipe.recipe.importer.RecipeImportHandler;
import net.minecraft.resources.ResourceLocation;

@LDLRegister(registry = IModModule.ID, name = FarmersDelightRecipeEditorTypes.MOD_ID, modID = FarmersDelightRecipeEditorTypes.MOD_ID)
public final class FarmersDelightRecipeEditorTypes implements IModModule {
    public static final String MOD_ID = "farmersdelight";

    public static final ResourceLocation COOKING_POT = farmer("cooking_pot");
    public static final ResourceLocation CUTTING_BOARD = farmer("cutting_board");
    public static final ResourceLocation COOKING = farmer("cooking");
    public static final ResourceLocation CUTTING = farmer("cutting");

    private static boolean registered;

    @Override
    public RecipeImportHandler importHandler() {return FarmersDelightRecipeImporter.INSTANCE;}

    @Override
    public void registerEditorTypes() {
        if (registered) return;
        registered = true;
        registerCategories();
        registerTypes();
    }

    private void registerCategories() {
        registerCategory(RecipeEditorCategory.of(
                COOKING_POT, "viscript_recipe.editor.category.farmersdelight.cooking_pot",
                MOD_ID, COOKING
        ));
        registerCategory(RecipeEditorCategory.of(
                CUTTING_BOARD, "viscript_recipe.editor.category.farmersdelight.cutting_board",
                MOD_ID, CUTTING
        ));
    }

    private void registerTypes() {
        registerEditorType(RecipeEditorType.of(
                COOKING, COOKING_POT,
                "viscript_recipe.editor.type.farmersdelight.cooking",
                FarmerCookingPotRecipeData.class, FarmerCookingPotRecipeData::new,
                FarmerCookingPotCanvas::new, MOD_ID
        ));
        registerEditorType(RecipeEditorType.of(
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
