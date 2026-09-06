package com.viscript_recipe.compat.avaritia.canvas;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.viscript_recipe.compat.avaritia.data.AvaritiaTableRecipeData;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.canvas.ShapedGridHelper;
import com.viscript_recipe.gui.canvas.vanilla.BasicRecipeCanvasFactory;
import com.viscript_recipe.gui.editor.RecipeGridFactory;
import com.viscript_recipe.gui.views.NavigationView;
import net.minecraft.network.chat.Component;

import java.util.List;

import static com.viscript_recipe.compat.avaritia.AvaritiaRecipeEditorTypes.*;

public class TableCanvas extends RecipeCanvas<AvaritiaTableRecipeData> {
    public TableCanvas(NavigationView navigationView, RecipeEntry entry) {super(navigationView, entry);}

    @Override
    public void load() {
        var data = getData(); int size = gridSize();
        if (isShaped()) ShapedGridHelper.loadGrid(this, data.getPattern(), data.getKey(), size, size, 9);
        else loadIngredients(data.getShapelessIngredients());
        setVisualOutput(0, data.getResult());
    }

    @Override
    public void save() {
        var data = getData(); int size = gridSize();
        if (isShaped()) {
            var pattern = ShapedGridHelper.saveGrid(this, size, size, 9);
            data.setPattern(pattern.pattern()).setKey(pattern.key());
        } else data.setShapelessIngredients(getIngredients(size * size));
        data.setResult(getVisualOutput(0).getItem());
    }

    @Override
    public UIElement createCanvas() {
        int size = gridSize();
        var grid = RecipeGridFactory.borderedGrid(size, size, RecipeCanvas.JEI_SLOT_SIZE,
                (index, row, col) -> createIngredientSlot(row * 9 + col, RecipeCanvas.JEI_SLOT_SIZE));
        return BasicRecipeCanvasFactory.createCraftingCanvas(
                grid, createOutputSlot(0, RecipeCanvas.OUTPUT_SLOT_SIZE));
    }

    @Override
    public void buildRecipeProperties(UIElement content) {
        var data = getData();
        content.addChildren(sectionTitle("viscript_recipe.editor.properties.avaritia.table"),
                selector("viscript_recipe.config.avaritia.table.size",
                        List.of(1, 2, 3, 4), effectiveTier(), tier -> {
                            int size = tableGridSizeForTier(tier);
                            return Component.translatable(
                                    "viscript_recipe.config.avaritia.table.size.value", size, size);
                        }, tier -> {
                            int size = tableGridSizeForTier(tier);
                            data.setTier(tier).setWidth(size).setHeight(size);
                            reloadCanvas();
                        })
        );
        if (isShaped()) content.addChild(switchField("viscript_recipe.config.avaritia.table.compatible",
                    data.isCompatible(), data::setCompatible));
    }

    boolean isShaped() {
        return isShapedTableType(entry.getType()) || isNoConsumeCatalystType(entry.getType());
    }

    int effectiveTier() {
        int tier = getData().getTier();
        return tier == 0 ? tableTierForType(entry.getType()) : tier;
    }

    int gridSize() {return tableGridSizeForTier(effectiveTier());}
}
