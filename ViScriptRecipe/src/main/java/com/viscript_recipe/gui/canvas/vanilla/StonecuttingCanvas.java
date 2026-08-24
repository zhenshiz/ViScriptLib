package com.viscript_recipe.gui.canvas.vanilla;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.data.vanilla.StonecuttingRecipeData;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.views.NavigationView;

public class StonecuttingCanvas extends RecipeCanvas<StonecuttingRecipeData> {
    static final boolean useJeiCanvas = VanillaStonecuttingCanvasFactory.hasJeiSkin();

    public StonecuttingCanvas(NavigationView navigationView, RecipeEntry entry) {super(navigationView, entry);}

    @Override
    public void load() {
        var data = getData();
        var ingredient = data.getIngredient();
        loadIngredientSlot(0, ingredient);
        setVisualOutput(0, data.getResult());
    }

    @Override
    public void save() {
        var data = getData();
        data.setIngredient(getVisualIngredient(0));
        data.setResult(getVisualOutput(0).getItem());
    }

    @Override
    public UIElement createCanvas() {
        var ingredientSlot = createIngredientSlot(0, useJeiCanvas ? JEI_SLOT_SIZE : SLOT_SIZE);
        var outputSlot = createOutputSlot(0, useJeiCanvas ? JEI_SLOT_SIZE : OUTPUT_SLOT_SIZE);
        if (!useJeiCanvas) {
            return BasicRecipeCanvasFactory.createCookingCanvas(ingredientSlot, outputSlot);
        }
        configureJeiOverlaySlotVisual(ingredientSlot, outputSlot);
        return VanillaStonecuttingCanvasFactory.createCanvas(ingredientSlot, outputSlot);
    }
}
