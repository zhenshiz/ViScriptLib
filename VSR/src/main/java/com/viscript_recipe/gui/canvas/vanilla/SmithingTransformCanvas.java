package com.viscript_recipe.gui.canvas.vanilla;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.data.vanilla.SmithingTransformRecipeData;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.editor.RecipeEditorUi;
import com.viscript_recipe.gui.views.NavigationView;
import dev.vfyjxf.taffy.style.AlignItems;
import net.minecraft.network.chat.Component;

public class SmithingTransformCanvas extends RecipeCanvas<SmithingTransformRecipeData> {
    static final boolean useJeiCanvas = VanillaSmithingCanvasFactory.hasJeiSkin();

    public SmithingTransformCanvas(NavigationView navigationView, RecipeEntry entry) {super(navigationView, entry);}

    @Override
    public void load() {
        var data = getData();
        loadIngredientSlot(0, data.getTemplate());
        loadIngredientSlot(1, data.getBase());
        loadIngredientSlot(2, data.getAddition());
        setVisualOutput(0, data.getResult());
    }

    @Override
    public void save() {
        var data = getData();
        data.setTemplate(getVisualIngredient(0));
        data.setBase(getVisualIngredient(1));
        data.setAddition(getVisualIngredient(2));
        data.setResult(getVisualOutput(0).getItem());
    }

    @Override
    public UIElement createCanvas() {
        var templateInput = createSmithingInput("viscript_recipe.editor.smithing.template", 0);
        var baseInput = createSmithingInput("viscript_recipe.editor.smithing.base", 1);
        var additionInput = createSmithingInput("viscript_recipe.editor.smithing.addition", 2);
        var outputSlot = createOutputSlot(0, useJeiCanvas ? JEI_SLOT_SIZE : OUTPUT_SLOT_SIZE);
        if (useJeiCanvas) {
            configureJeiOverlaySlotVisual(outputSlot);
            return VanillaSmithingCanvasFactory.createCanvas(
                    templateInput, baseInput, additionInput,
                    VanillaSmithingCanvasFactory.createSlotCell(outputSlot)
            );
        }
        return BasicRecipeCanvasFactory.createSmithingCanvas(templateInput, baseInput, additionInput, outputSlot);
    }

    private UIElement createSmithingInput(String labelKey, int index) {
        var slot = createIngredientSlot(index, useJeiCanvas ? JEI_SLOT_SIZE : SLOT_SIZE);
        if (useJeiCanvas) {
            configureJeiOverlaySlotVisual(slot);
            return VanillaSmithingCanvasFactory.createSlotCell(slot);
        }
        var label = RecipeEditorUi.label(Component.translatable(labelKey));
        return RecipeEditorUi.column().layout(layout -> {
            layout.width(72);
            layout.gapAll(4);
            layout.alignItems(AlignItems.CENTER);
        }).addChildren(label, slot);
    }
}
