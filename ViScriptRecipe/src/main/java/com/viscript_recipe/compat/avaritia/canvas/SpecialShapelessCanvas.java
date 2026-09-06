package com.viscript_recipe.compat.avaritia.canvas;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.viscript_recipe.compat.avaritia.AvaritiaRecipeEditorTypes;
import com.viscript_recipe.compat.avaritia.data.AvaritiaSpecialShapelessRecipeData;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.canvas.vanilla.BasicRecipeCanvasFactory;
import com.viscript_recipe.gui.editor.RecipeGridFactory;
import com.viscript_recipe.gui.editor.SlotSelection;
import com.viscript_recipe.gui.views.NavigationView;
import net.minecraft.world.item.ItemStack;

public class SpecialShapelessCanvas extends RecipeCanvas<AvaritiaSpecialShapelessRecipeData> {
    public SpecialShapelessCanvas(NavigationView navigationView, RecipeEntry entry) {super(navigationView, entry);}

    ItemStack getResult() {
        var data = getData();
        return data.result(entry.getType()).copyWithCount(data.getCount());
    }

    @Override
    public void load() {
        var data = getData();
        loadIngredients(data.getIngredients());
        setVisualOutput(0, getResult());
    }

    @Override
    public void save() {
        getData().setIngredients(getIngredients(81));
    }

    @Override
    public UIElement createCanvas() {
        var grid = RecipeGridFactory.borderedGrid(9, 9, RecipeCanvas.JEI_SLOT_SIZE,
                (index, row, col) -> createIngredientSlot(index, RecipeCanvas.JEI_SLOT_SIZE));
        // 不允许清除物品
        var output = createOutputSlot(0, RecipeCanvas.OUTPUT_SLOT_SIZE);
        removeUIFirstEvent(output, UIEvents.MOUSE_DOWN, false);
        output.addEventListener(UIEvents.MOUSE_DOWN, event -> selectSlot(SlotSelection.result(0)));
        return BasicRecipeCanvasFactory.createCraftingCanvas(grid, output);
    }

    @Override
    public void buildRecipeProperties(UIElement content) {
        if (entry.isType(AvaritiaRecipeEditorTypes.ETERNAL_SINGULARITY)) return;
        var data = getData();
        content.addChildren(sectionTitle("viscript_recipe.editor.properties.avaritia.special_shapeless"),
                textField("viscript_recipe.config.avaritia.group", data.getGroup(), data::setGroup)
        );
    }

    @Override
    public void buildResultProperties(UIElement content) {
        var data = getData();
        content.addChildren(sectionTitle("viscript_recipe.editor.properties.avaritia.special_shapeless"),
                intField("viscript_recipe.config.avaritia.count",
                        data.getCount(), 1, Integer.MAX_VALUE, value -> {
                            data.setCount(value);
                            setSelectedOutput(getResult());
                        })
        );
    }
}
