package com.viscript_recipe.gui.canvas.farmersdelight;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.data.RecipeOutputData;
import com.viscript_recipe.data.farmersdelight.FarmerCookingPotRecipeData;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.editor.IngredientDisplaySlot;
import com.viscript_recipe.gui.editor.RecipeEditorUi;
import com.viscript_recipe.gui.editor.RecipeGridFactory;
import com.viscript_recipe.gui.views.NavigationView;
import com.viscript_recipe.gui.views.PropertiesView;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;

public class FarmerCookingPotCanvas extends RecipeCanvas<FarmerCookingPotRecipeData> {
    static final boolean useJeiCanvas = FarmersDelightCanvasFactory.hasJeiCookingPotSkin();
    static UIElement cookingTimeIcon = new UIElement();
    static UIElement experienceIcon = new UIElement();

    public FarmerCookingPotCanvas(NavigationView navigationView, RecipeEntry entry) {super(navigationView, entry);}

    @Override
    public void load() {
        var cookingPot = getData();
        var ingredients = cookingPot.getIngredients();
        for (int i = 0; i < Math.min(6, ingredients.size()); i++) {
            loadIngredientSlot(i, ingredients.get(i));
        }
        setVisualOutput(0, cookingPot.getResult());
        setExtraItem(cookingPot.getContainer());
    }

    @Override
    public void save() {
        var data = getData();
        var ingredients = new ArrayList<RecipeIngredient>();
        for (int i = 0; i < 6; i++) {
            var ingredient = getVisualIngredient(i);
            if (!ingredient.isEmpty()) ingredients.add(ingredient);
        }
        data.setIngredients(ingredients);
        data.setResult(getVisualOutput(0).getItem());
    }

    @Override
    public void setExtraItem(int index, ItemStack item) {
        super.setExtraItem(index, item);
        getData().setContainer(item.copyWithCount(1));
    }

    @Override
    public void buildRecipeProperties(UIElement content) {
        var data = getData();
        content.addChildren(
                RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.farmersdelight.cooking_pot"),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.cooking.experience",
                        RecipeEditorUi.floatField(data.getExperience(), 0, Integer.MAX_VALUE, i -> {
                            data.setExperience(i); updateExperienceIcon();
                        })),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.cooking.cooking_time",
                        RecipeEditorUi.intField(data.getCookingTime(), 1, 72000, i -> {
                            data.setCookingTime(i); updateCookingTimeIcon();
                        }))
        );
    }

    @Override
    public void buildExtraItemProperties(UIElement content) {
        content.addChildren(
                RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.farmersdelight.container"),
                PropertiesView.createItemStackConfigurator(
                        "viscript_recipe.config.farmersdelight.cooking.container",
                        this::getExtraItem, this::setExtraItem
                )
        );
    }

    @Override
    public void setVisualOutput(int index, RecipeOutputData output) {
        super.setVisualOutput(index, output);
        // 让预览槽和输出槽保持一致
        if (index == 0) {
            visualOutputs[14] = output.copy();
            visualOutputSlots[14].setItem(output.getItem(), false);
        } else if (index == 14) {
            visualOutputs[0] = output.copy();
            visualOutputSlots[0].setItem(output.getItem(), false);
        }
    }

    @Override
    public UIElement createCanvas() {
        //noinspection deprecation
        var containerSlot = configureExtraItemSlot(createEditorSlot(useJeiCanvas ? JEI_SLOT_SIZE : SLOT_SIZE),
                Component.translatable("viscript_recipe.editor.farmersdelight.cooking.container_slot"));
        extraItemSlots[0] = containerSlot;
        var ingredientSlots = new IngredientDisplaySlot[6];
        var outputSlot = createOutputSlot(0, useJeiCanvas ? JEI_SLOT_SIZE : OUTPUT_SLOT_SIZE);
        var previewSlot = createOutputSlot(14, useJeiCanvas ? JEI_SLOT_SIZE : OUTPUT_SLOT_SIZE);

        if (useJeiCanvas) {
            for (int index = 0; index < 6; index++) {
                var slot = createIngredientSlot(index, JEI_SLOT_SIZE);
                configureJeiOverlaySlotVisual(slot);
                ingredientSlots[index] = slot;
            }
            configureJeiOverlaySlotVisual(previewSlot);
            configureJeiOverlaySlotVisual(containerSlot);
            configureJeiOverlaySlotVisual(outputSlot);
            return FarmersDelightCanvasFactory.createJeiCookingPotCanvas(
                    ingredientSlots, previewSlot, containerSlot, outputSlot,
                    updateCookingTimeIcon(), updateExperienceIcon()
            );
        }
        return FarmersDelightCanvasFactory.createCookingPotCanvas(
                RecipeGridFactory.borderedGrid(3, 2, SLOT_SIZE,
                        (index, row, col) -> createIngredientSlot(index, SLOT_SIZE)),
                FarmersDelightCanvasFactory.createHeatSource(createItemIcon(new ItemStack(Items.CAMPFIRE), 30)),
                FarmersDelightCanvasFactory.createPotPreview(
                        createItemIcon(new ItemStack(itemFromRegistry("farmersdelight:cooking_pot", Items.CAULDRON)), 18),
                        previewSlot
                ),
                FarmersDelightCanvasFactory.createServingRow(containerSlot, outputSlot)
        );
    }

    private UIElement updateCookingTimeIcon() {
        return cookingTimeIcon.style(style -> style.tooltips(Component.translatable(
                "viscript_recipe.editor.cooking.time_seconds", getData().getCookingTime() / 20
        )));
    }

    private UIElement updateExperienceIcon() {
        return experienceIcon.style(style -> style.tooltips(Component.translatable(
                "viscript_recipe.editor.cooking.experience_value", getData().getExperience()
        )));
    }
}
