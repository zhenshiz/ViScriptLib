package com.viscript_recipe.data.farmersdelight;

import com.viscript_recipe.data.RecipeEditorCategory;
import com.viscript_recipe.data.RecipeEditorLayout;
import com.viscript_recipe.data.RecipeEditorTypes;
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
                COOKING,
                RecipeEditorLayout.FARMERS_COOKING_POT
        ));
        RecipeEditorTypes.registerCategory(new RecipeEditorCategory(
                CUTTING_BOARD,
                "viscript_recipe.editor.category.farmersdelight.cutting_board",
                MOD_ID,
                REQUIRED_MODS,
                CUTTING,
                RecipeEditorLayout.FARMERS_CUTTING_BOARD
        ));
    }

    private static void registerTypes() {
/*        RecipeEditorTypes.register(new RecipeEditorType(
                COOKING,
                COOKING_POT,
                "viscript_recipe.editor.type.farmersdelight.cooking",
                REQUIRED_MODS,
                false,
                entry -> entry.getFarmerCookingPot().compile(),
                entry -> false,
                (entry, value) -> {
                },
                entry -> entry.getFarmerCookingPot().getResult(),
                (entry, stack) -> entry.getFarmerCookingPot().setResult(stack.copy())
        ));
        RecipeEditorTypes.register(new RecipeEditorType(
                CUTTING,
                CUTTING_BOARD,
                "viscript_recipe.editor.type.farmersdelight.cutting",
                REQUIRED_MODS,
                false,
                entry -> entry.getFarmerCuttingBoard().compile(),
                entry -> false,
                (entry, value) -> {
                },
                entry -> entry.getFarmerCuttingBoard().getResults().isEmpty()
                        ? net.minecraft.world.item.ItemStack.EMPTY
                        : entry.getFarmerCuttingBoard().getResults().get(0).getItem(),
                (entry, stack) -> {
                    if (entry.getFarmerCuttingBoard().getResults().isEmpty()) {
                        entry.getFarmerCuttingBoard().getResults().add(new FarmerCuttingResultData());
                    }
                    entry.getFarmerCuttingBoard().getResults().get(0).setItem(stack.copy());
                }
        ));*/
    }

    private static ResourceLocation farmer(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
