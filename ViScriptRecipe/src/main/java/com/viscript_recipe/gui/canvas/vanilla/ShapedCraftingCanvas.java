package com.viscript_recipe.gui.canvas.vanilla;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.data.vanilla.CraftingRemainderMode;
import com.viscript_recipe.data.vanilla.CraftingRemainderRule;
import com.viscript_recipe.data.vanilla.ShapedCraftingRecipeData;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.canvas.ShapedGridHelper;
import com.viscript_recipe.gui.editor.RecipeEditorUi;
import com.viscript_recipe.gui.editor.RecipeGridFactory;
import com.viscript_recipe.gui.views.NavigationView;
import com.viscript_recipe.gui.views.PropertiesView;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ShapedCraftingCanvas extends RecipeCanvas<ShapedCraftingRecipeData> {
    public static CraftingRemainderRule[] visualRemainders;

    public ShapedCraftingCanvas(NavigationView navigationView, RecipeEntry entry) {super(navigationView, entry);}

    @Override
    public void initVisualState() {
        super.initVisualState();
        visualRemainders = defaultedArrays(new CraftingRemainderRule[9], CraftingRemainderRule.defaultRule());
    }

    @Override
    public void load() {
        var data = getData();
        ShapedGridHelper.loadGrid(this, data.getPattern(), data.getKey(), 3, 3, 3);
        var remainders = data.getRemainders();
        for (int i = 0; i < remainders.size(); i++) setVisualRemainder(i, remainders.get(i));
        setVisualOutput(0, data.getResult());
    }

    @Override
    public void save() {
        var data = getData();
        var patterns = ShapedGridHelper.saveGrid(this, 3, 3, 3);
        data.setPattern(patterns.pattern()).setKey(patterns.key());
        var remainders = new ArrayList<CraftingRemainderRule>();
        for (int i = 0; i < 9; i++) remainders.add(getVisualRemainder(i));
        data.setRemainders(remainders).setResult(getVisualOutput(0).getItem());
    }

    @Override
    public UIElement createCanvas() {
        return BasicRecipeCanvasFactory.createCraftingCanvas(createGrid(), createOutputSlot(0, OUTPUT_SLOT_SIZE));
    }

    private UIElement createGrid() {
        return RecipeGridFactory.borderedGrid(3, 3, SLOT_SIZE, (index, row, col) ->
                createIngredientSlot(index, SLOT_SIZE));
    }

    @Override
    public void setVisualIngredient(int index, RecipeIngredient ingredient) {
        try {
            setVisualRemainder(index, CraftingRemainderRule.defaultRule());
            visualIngredientSlots[index].setIngredient(ingredient);
        } catch (Exception ignored) {
        }
    }

    public static CraftingRemainderRule getVisualRemainder(int index) {
        try {
            return visualRemainders[index].copy();
        } catch (Exception e) {
            return CraftingRemainderRule.defaultRule();
        }
    }

    public static void setVisualRemainder(int index, CraftingRemainderRule rule) {
        try {
            visualRemainders[index] = rule;
        } catch (Exception ignored) {
        }
    }

    public void buildRemainderProperties(UIElement content) {
        var remainder = getVisualRemainder(selectedSlotIndex());
        var mode = remainder.getMode();
        content.addChildren(sectionTitle("viscript_recipe.editor.properties.remainder"),
                field("viscript_recipe.config.remainder.mode",
                        RecipeEditorUi.selector(List.of(CraftingRemainderMode.values()), mode,
                                CraftingRemainderMode::displayName, value -> {
                                    var updated = remainder.copy().setMode(value);
                                    if (value != CraftingRemainderMode.REPLACE) updated.setItem(ItemStack.EMPTY);
                                    setVisualRemainder(selectedSlotIndex(), updated);
                                    reloadProperties();
                                }
                        ),
                        Component.translatable("viscript_recipe.editor.remainder.tip.default"),
                        Component.translatable("viscript_recipe.editor.remainder.tip.consume"),
                        Component.translatable("viscript_recipe.editor.remainder.tip.replace"))
        );
        if (mode == CraftingRemainderMode.REPLACE) {
            content.addChild(PropertiesView.createItemStackConfigurator(
                    "viscript_recipe.config.remainder.item",
                    () -> remainder.getItem().copy(), stack -> {
                        var updated = remainder.copy().setMode(CraftingRemainderMode.REPLACE).setItem(stack);
                        setVisualRemainder(selectedSlotIndex(), updated);
                    }
            ));
        }
    }
}
