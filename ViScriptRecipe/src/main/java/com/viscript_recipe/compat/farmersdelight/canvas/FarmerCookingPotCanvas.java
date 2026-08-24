package com.viscript_recipe.compat.farmersdelight.canvas;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.viscript_recipe.compat.farmersdelight.data.FarmerCookingPotRecipeData;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.data.RecipeOutputData;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.editor.IngredientDisplaySlot;
import com.viscript_recipe.gui.editor.RecipeGridFactory;
import com.viscript_recipe.gui.views.NavigationView;
import com.viscript_recipe.gui.views.PropertiesView;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import static com.viscript_recipe.recipe.RecipeHelper.itemFromRegistry;

public class FarmerCookingPotCanvas extends RecipeCanvas<FarmerCookingPotRecipeData> {
    static final boolean useJeiCanvas = FarmersDelightCanvasFactory.hasJeiCookingPotSkin();
    static UIElement cookingTimeIcon = new UIElement();
    static UIElement experienceIcon = new UIElement();

    public FarmerCookingPotCanvas(NavigationView navigationView, RecipeEntry entry) {super(navigationView, entry);}

    @Override
    public void load() {
        var cookingPot = getData();
        loadIngredients(cookingPot.getIngredients());
        setVisualOutput(0, cookingPot.getResult());
        setExtraItem(cookingPot.getContainer());
        updateCookingTimeIcon();
        updateExperienceIcon();
    }

    @Override
    public void save() {
        getData().setIngredients(getIngredients(6)).setResult(getVisualOutput(0).getItem());
    }

    @Override
    public void setExtraItem(int index, ItemStack item) {
        super.setExtraItem(index, item);
        getData().setContainer(item.copyWithCount(1));
    }

    @Override
    public void buildRecipeProperties(UIElement content) {
        var data = getData();
        content.addChildren(sectionTitle("viscript_recipe.editor.properties.farmersdelight.cooking_pot"),
                floatField("viscript_recipe.config.cooking.experience",
                        data.getExperience(), 0, Integer.MAX_VALUE, data::setExperience, this::updateExperienceIcon),
                intField("viscript_recipe.config.cooking.cooking_time",
                        data.getCookingTime(), 1, 72000, data::setCookingTime, this::updateCookingTimeIcon)
        );
    }

    @Override
    public void buildExtraItemProperties(UIElement content) {
        content.addChildren(sectionTitle("viscript_recipe.editor.properties.farmersdelight.container"),
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
        var containerSlot = createExtraItemSlot(useJeiCanvas ? JEI_SLOT_SIZE : SLOT_SIZE,
                Component.translatable("viscript_recipe.editor.farmersdelight.cooking.container_slot"));
        var ingredientSlots = new IngredientDisplaySlot[6];
        var outputSlot = createOutputSlot(0, useJeiCanvas ? JEI_SLOT_SIZE : OUTPUT_SLOT_SIZE);
        var previewSlot = createOutputSlot(14, useJeiCanvas ? JEI_SLOT_SIZE : OUTPUT_SLOT_SIZE);

        if (useJeiCanvas) {
            for (int index = 0; index < 6; index++) ingredientSlots[index] = createIngredientSlot(index, JEI_SLOT_SIZE);
            configureJeiOverlaySlotVisual(ingredientSlots);
            configureJeiOverlaySlotVisual(previewSlot, containerSlot, outputSlot);
            return FarmersDelightCanvasFactory.createJeiCookingPotCanvas(
                    ingredientSlots, previewSlot, containerSlot, outputSlot,
                    cookingTimeIcon, experienceIcon
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

    private void updateCookingTimeIcon() {
        tooltip(cookingTimeIcon, Component.translatable(
                "viscript_recipe.editor.cooking.time_seconds", getData().getCookingTime() / 20));
    }

    private void updateExperienceIcon() {
        tooltip(experienceIcon, Component.translatable(
                "viscript_recipe.editor.cooking.experience_value", getData().getExperience()));
    }
}
