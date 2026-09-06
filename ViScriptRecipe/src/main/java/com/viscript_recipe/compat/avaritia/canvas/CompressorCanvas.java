package com.viscript_recipe.compat.avaritia.canvas;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.viscript_recipe.compat.avaritia.data.AvaritiaCompressorRecipeData;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.views.NavigationView;

public class CompressorCanvas extends RecipeCanvas<AvaritiaCompressorRecipeData> {
    public CompressorCanvas(NavigationView navigationView, RecipeEntry entry) {super(navigationView, entry);}

    @Override
    public boolean ingredientHasCount(int slotIndex) {return true;}

    @Override
    public void load() {
        var data = getData();
        loadIngredientSlot(0, data.getIngredient());
        setVisualOutput(0, data.getResult());
    }

    @Override
    public void save() {
        getData().setIngredient(getVisualIngredient(0)).setResult(getVisualOutput(0).getItem());
    }

    @Override
    public UIElement createCanvas() {
        var ingredient = createIngredientSlot(0, JEI_SLOT_SIZE);
        var output = createOutputSlot(0, JEI_SLOT_SIZE);
        configureJeiOverlaySlotVisual(ingredient, output);
        return AvaritiaCanvasFactory.createCompressorCanvas(ingredient, output);
    }

    @Override
    public void buildRecipeProperties(UIElement content) {
        var data = getData();
        content.addChildren(sectionTitle("viscript_recipe.editor.properties.avaritia.compressor"),
                intField("viscript_recipe.config.avaritia.compressor.time_cost",
                        data.getTimeCost(), 1, Integer.MAX_VALUE, data::setTimeCost)
        );
    }
}
