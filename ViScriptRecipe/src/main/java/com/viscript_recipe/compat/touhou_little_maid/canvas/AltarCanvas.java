package com.viscript_recipe.compat.touhou_little_maid.canvas;

import com.lowdragmc.lowdraglib2.configurator.ui.TagConfigurator;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.viscript_recipe.compat.touhou_little_maid.data.TouhouLittleMaidAltarRecipeData;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.editor.IngredientDisplaySlot;
import com.viscript_recipe.gui.editor.RecipeSearchComponents;
import com.viscript_recipe.gui.views.NavigationView;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;

import java.util.Locale;

public class AltarCanvas extends RecipeCanvas<TouhouLittleMaidAltarRecipeData> {
    static final Label powerLabel = emptyLabel();
    static final Label resultDescriptionLabel = emptyLabel();

    public AltarCanvas(NavigationView navigationView, RecipeEntry entry) {super(navigationView, entry);}

    @Override
    public void load() {
        var data = getData();
        loadIngredients(data.getIngredients());
        setVisualOutput(0, data.getResult());
    }

    @Override
    public void save() {
        var data = getData();
        data.setIngredients(getIngredients(TouhouLittleMaidAltarRecipeData.INPUT_COUNT, true));
        data.setResult(getVisualOutput(0).getItem());
    }

    @Override
    public UIElement createCanvas() {
        var inputs = new IngredientDisplaySlot[TouhouLittleMaidAltarRecipeData.INPUT_COUNT];
        for (int i = 0; i < inputs.length; i++) {
            inputs[i] = createIngredientSlot(i, JEI_SLOT_SIZE);
            configureJeiOverlaySlotVisual(inputs[i]);
        }
        var output = createOutputSlot(0, JEI_SLOT_SIZE);
        configureJeiOverlaySlotVisual(output);
        return TouhouLittleMaidAltarCanvasFactory.createCanvas(inputs, output, powerLabel,
                resultDescriptionLabel, RecipeCanvas::selectRecipe);
    }

    @Override
    public void buildRecipeProperties(UIElement content) {
        updateLabels();
        var data = getData();
        content.addChildren(sectionTitle("viscript_recipe.editor.properties.touhou_little_maid.altar"),
                floatField("viscript_recipe.config.touhou_little_maid.altar.power",
                        data.getPower(), 0, Float.MAX_VALUE, data::setPower, this::updateLabels),
                RecipeSearchComponents.entityType("viscript_recipe.config.touhou_little_maid.altar.entity",
                        data::getEntityType, data::setEntityType,
                        RecipeCanvas::reloadProperties, EntityType.ITEM).style(style -> style.tooltips(
                        Component.translatable("viscript_recipe.config.touhou_little_maid.altar.entity.tooltip")))/*,
                textField("viscript_recipe.config.touhou_little_maid.altar.lang",
                        data.getLangKey(), data::setLangKey, this::updateLabels)*/);
        if (!data.isItemCraft()) content.addChild(new TagConfigurator("", data::getExtraData, data::setExtraData, new CompoundTag(), true));
    }

    private void updateLabels() {
        var data = getData();
        powerLabel.setText(Component.literal(String.format(Locale.ROOT, "×%.2f", data.getPower())));
        if (data.isItemCraft()) {
            var result = getVisualOutput(0).getItem().getHoverName();
            resultDescriptionLabel.setText(Component.translatable("jei.touhou_little_maid.altar_craft.result", result));
        } else resultDescriptionLabel.setText(Component.empty());
    }
}
