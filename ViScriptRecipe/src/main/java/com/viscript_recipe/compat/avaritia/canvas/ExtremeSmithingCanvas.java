package com.viscript_recipe.compat.avaritia.canvas;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.viscript_recipe.compat.avaritia.data.AvaritiaExtremeSmithingRecipeData;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.editor.IngredientDisplaySlot;
import com.viscript_recipe.gui.views.NavigationView;

public class ExtremeSmithingCanvas extends RecipeCanvas<AvaritiaExtremeSmithingRecipeData> {
    public ExtremeSmithingCanvas(NavigationView navigationView, RecipeEntry entry) {super(navigationView, entry);}

    @Override
    public void load() {
        var data = getData();
        loadIngredientSlot(0, data.getTemplate());
        loadIngredientSlot(1, data.getBase());
        loadIngredients(data.getAdditions(), 2);
        setVisualOutput(0, data.getResult());
    }

    @Override
    public void save() {
        var data = getData();
        data.setTemplate(getVisualIngredient(0));
        data.setBase(getVisualIngredient(1));
        data.setAdditions(getIngredients(3, 2, true));
        data.setResult(getVisualOutput(0).getItem());
    }

    @Override
    public UIElement createCanvas() {
        var ingredients = new IngredientDisplaySlot[5];
        for (int i = 0; i < ingredients.length; i++) ingredients[i] = createIngredientSlot(i, JEI_SLOT_SIZE);
        configureJeiOverlaySlotVisual(ingredients);
        var output = createOutputSlot(0, JEI_SLOT_SIZE);
        configureJeiOverlaySlotVisual(output);
        return AvaritiaCanvasFactory.createExtremeSmithingCanvas(ingredients, output);
    }
}
