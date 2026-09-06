package com.viscript_recipe.compat.goety.canvas;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.TextWrap;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.viscript_recipe.compat.goety.data.GoetyBrazierRecipeData;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.editor.IngredientDisplaySlot;
import com.viscript_recipe.gui.views.NavigationView;
import net.minecraft.network.chat.Component;

public class BrazierCanvas extends RecipeCanvas<GoetyBrazierRecipeData> {
    static final boolean useJeiCanvas = GoetyCanvasFactory.hasJeiSkin();
    static final Label soulLabel = emptyLabel();
    static {centerLabel(soulLabel);}

    public BrazierCanvas(NavigationView navigationView, RecipeEntry entry) {super(navigationView, entry);}

    @Override
    public void load() {
        var data = getData();
        loadIngredients(data.getIngredients());
        setVisualOutput(0, data.getResult());
        updateSoulLabel();
    }

    @Override
    public void save() {
        var data = getData();
        data.setIngredients(getIngredients(3, true));
        data.setResult(getVisualOutput(0).getItem());
    }

    @Override
    public UIElement createCanvas() {
        var inputs = new IngredientDisplaySlot[3];
        for (int i = 0; i < 3; i++) {
            inputs[i] = createIngredientSlot(i, useJeiCanvas ? JEI_SLOT_SIZE : SLOT_SIZE);
            if (useJeiCanvas) configureJeiOverlaySlotVisual(inputs[i]);
        }
        var output = createOutputSlot(0, useJeiCanvas ? JEI_SLOT_SIZE : OUTPUT_SLOT_SIZE);
        if (useJeiCanvas) configureJeiOverlaySlotVisual(output);
        return GoetyCanvasFactory.createBrazierCanvas(inputs, output, soulLabel, useJeiCanvas);
    }

    @Override
    public void buildRecipeProperties(UIElement content) {
        var data = getData();
        content.addChildren(sectionTitle("viscript_recipe.editor.properties.goety.brazier"),
                intField("viscript_recipe.config.goety.soul_cost", data.getSoulCost(), 0, Integer.MAX_VALUE,
                        data::setSoulCost, this::updateSoulLabel)
        );
    }

    private void updateSoulLabel() {
        soulLabel.setText(Component.translatable("viscript_recipe.editor.goety.soul_cost",
                Math.max(0, getData().getSoulCost())));
    }

    static void centerLabel(Label label) {
        label.textStyle(style -> style.textAlignHorizontal(Horizontal.CENTER).textWrap(TextWrap.HOVER_ROLL));
    }
}
