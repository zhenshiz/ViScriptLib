package com.viscript_recipe.gui.canvas.vanilla;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.data.vanilla.ShapelessCraftingRecipeData;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.editor.RecipeGridFactory;
import com.viscript_recipe.gui.views.NavigationView;

public class ShapelessCraftingCanvas extends RecipeCanvas<ShapelessCraftingRecipeData> {
    public ShapelessCraftingCanvas(NavigationView navigationView, RecipeEntry entry) {super(navigationView, entry);}

    @Override
    public void load() {
        var data = getData();
        loadIngredients(data.getIngredients());
        setVisualOutput(0, data.getResult());
    }

    @Override
    public void save() {
        var data = getData();
        data.setIngredients(getIngredients(9));
        data.setResult(getVisualOutput(0).getItem());
    }

    @Override
    public UIElement createCanvas() {
        return BasicRecipeCanvasFactory.createCraftingCanvas(createGrid(), createOutputSlot(0, OUTPUT_SLOT_SIZE));
    }

    private UIElement createGrid() {
        return RecipeGridFactory.borderedGrid(3, 3, SLOT_SIZE, (index, row, col) ->
                createIngredientSlot(index, SLOT_SIZE));
    }
}
