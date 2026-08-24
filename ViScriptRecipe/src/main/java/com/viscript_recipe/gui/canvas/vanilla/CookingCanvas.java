package com.viscript_recipe.gui.canvas.vanilla;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.viscript_recipe.data.RecipeEditorTypes;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.data.vanilla.CookingRecipeData;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.views.NavigationView;
import net.minecraft.network.chat.Component;

public class CookingCanvas extends RecipeCanvas<CookingRecipeData> {
    static final boolean useJeiCanvas = VanillaCookingCanvasFactory.hasJeiSkin();
    static final Label experienceLabel = emptyLabel();
    static final Label cookingTimeLabel = emptyLabel();

    public CookingCanvas(NavigationView navigationView, RecipeEntry entry) {super(navigationView, entry);}

    @Override
    public void load() {
        var data = getData();
        var ingredient = data.getIngredient();
        loadIngredientSlot(0, ingredient);
        setVisualOutput(0, data.getResult());
        updateExpLabel();
        updateTimeLabel();
    }

    @Override
    public void save() {
        var data = getData();
        data.setIngredient(getVisualIngredient(0));
        data.setResult(getVisualOutput(0).getItem());
    }

    @Override
    public UIElement createCanvas() {
        return entry.isType(RecipeEditorTypes.CAMPFIRE_COOKING) ? createCampfireCookingCanvas() : createCookingCanvas();
    }

    private UIElement createCookingCanvas() {
        var ingredientSlot = createIngredientSlot(0, useJeiCanvas ? JEI_SLOT_SIZE : SLOT_SIZE);
        var outputSlot = createOutputSlot(0, useJeiCanvas ? JEI_SLOT_SIZE : OUTPUT_SLOT_SIZE);
        if (!useJeiCanvas) {
            return BasicRecipeCanvasFactory.createCookingCanvas(ingredientSlot, outputSlot);
        }
        configureJeiOverlaySlotVisual(ingredientSlot, outputSlot);
        return VanillaCookingCanvasFactory.createFurnaceCanvas(
                ingredientSlot, outputSlot, experienceLabel, cookingTimeLabel
        );
    }

    private UIElement createCampfireCookingCanvas() {
        var ingredientSlot = createIngredientSlot(0, useJeiCanvas ? JEI_SLOT_SIZE : SLOT_SIZE);
        var outputSlot = createOutputSlot(0, useJeiCanvas ? JEI_SLOT_SIZE : OUTPUT_SLOT_SIZE);
        if (!useJeiCanvas) {
            return BasicRecipeCanvasFactory.createCookingCanvas(ingredientSlot, outputSlot);
        }
        configureJeiOverlaySlotVisual(ingredientSlot, outputSlot);
        return VanillaCookingCanvasFactory.createCampfireCanvas(
                ingredientSlot, outputSlot, cookingTimeLabel
        );
    }

    @Override
    public void buildRecipeProperties(UIElement content) {
        var data = getData();
        content.addChildren(sectionTitle("viscript_recipe.editor.properties.cooking"),
                floatField("viscript_recipe.config.cooking.experience",
                        data.getExperience(), 0, Integer.MAX_VALUE, data::setExperience, this::updateExpLabel)
                        .setDisplay(!entry.isType(RecipeEditorTypes.CAMPFIRE_COOKING)),
                intField("viscript_recipe.config.cooking.cooking_time",
                        data.getCookingTime(), 1, 72000, data::setCookingTime, this::updateTimeLabel)
        );
    }

    private void updateExpLabel() {
        var data = getData().getExperience();
        experienceLabel.setText(data > 0 ?
                Component.translatable("viscript_recipe.editor.cooking.experience_value", data) : Component.empty());
    }

    private void updateTimeLabel() {
        var data = getData().getCookingTime();
        cookingTimeLabel.setText(data <= 0 ? Component.empty() :
                Component.translatable("viscript_recipe.editor.cooking.time_seconds", data / 20));
    }
}
