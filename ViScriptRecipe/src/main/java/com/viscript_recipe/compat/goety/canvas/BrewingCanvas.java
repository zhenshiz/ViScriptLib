package com.viscript_recipe.compat.goety.canvas;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.viscript_recipe.compat.goety.GoetyRecipeUiSupport;
import com.viscript_recipe.compat.goety.data.GoetyBrewingEntityKind;
import com.viscript_recipe.compat.goety.data.GoetyBrewingRecipeData;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.editor.RecipeSearchComponents;
import com.viscript_recipe.gui.views.NavigationView;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;

import java.util.List;

import static com.viscript_recipe.compat.goety.canvas.BrazierCanvas.useJeiCanvas;

public class BrewingCanvas extends RecipeCanvas<GoetyBrewingRecipeData> {
    static final Label infoLabel = emptyLabel();
    static final UIElement outputPreview = createItemIcon(ItemStack.EMPTY, useJeiCanvas ? JEI_SLOT_SIZE : SLOT_SIZE);
    static {
        BrazierCanvas.centerLabel(infoLabel);
        tooltip(outputPreview, "viscript_recipe.editor.goety.brewing.derived_output");
    }

    public BrewingCanvas(NavigationView navigationView, RecipeEntry entry) {super(navigationView, entry);}

    @Override
    public void load() {
        loadIngredientSlot(0, getData().getIngredient());
        updatePreview();
    }

    @Override
    public void save() {
        getData().setIngredient(getVisualIngredient(0));
    }

    @Override
    public UIElement createCanvas() {
        var catalyst = createIngredientSlot(0, useJeiCanvas ? JEI_SLOT_SIZE : SLOT_SIZE);
        tooltip(catalyst, "viscript_recipe.editor.goety.brewing.editable_catalyst");
        if (useJeiCanvas) configureJeiOverlaySlotVisual(catalyst);
        return GoetyCanvasFactory.createBrewingCanvas(catalyst, outputPreview, infoLabel, useJeiCanvas);
    }

    @Override
    public void buildRecipeProperties(UIElement content) {
        var data = getData();
        content.addChildren(sectionTitle("viscript_recipe.editor.properties.goety.brewing"),
                RecipeSearchComponents.mobEffect("viscript_recipe.config.goety.brewing.effect",
                        data::getEffect, data::setEffect, this::updatePreview, MobEffects.POISON
                ),
                intField("viscript_recipe.config.goety.soul_cost", data.getSoulCost(),
                        0, Integer.MAX_VALUE, data::setSoulCost, this::updatePreview),
                intField("viscript_recipe.config.goety.brewing.capacity_extra", data.getCapacityExtra(),
                        0, Integer.MAX_VALUE, data::setCapacityExtra, this::updatePreview),
                intField("viscript_recipe.config.goety.duration", data.getDuration(),
                        1, Integer.MAX_VALUE, data::setDuration, this::updatePreview),
                selector("viscript_recipe.config.goety.brewing.entity_kind",
                        List.of(GoetyBrewingEntityKind.values()),
                        data.getEntityKind(), GoetyBrewingEntityKind::displayName,
                        data::setEntityKind, RecipeCanvas::reloadProperties
                )
        );
        if (data.getEntityKind() == GoetyBrewingEntityKind.ENTITY) {
            content.addChild(RecipeSearchComponents.entityType("viscript_recipe.config.goety.brewing.entity",
                    data::getEntity, data::setEntity, this::updatePreview, EntityType.ZOMBIE
            ));
        } else if (data.getEntityKind() == GoetyBrewingEntityKind.TAG) {
            content.addChild(RecipeSearchComponents.entityTag("viscript_recipe.config.goety.brewing.entity",
                    data::getEntity, data::setEntity, this::updatePreview
            ));
        }
    }

    private void updatePreview() {
        var data = getData();
        setTexture(outputPreview, GoetyRecipeUiSupport.brewPreview(data.getEffect(), data.getDuration()));
        infoLabel.setText(Component.translatable("viscript_recipe.editor.goety.brewing.info", data.getCapacityExtra(), data.getSoulCost()));
    }
}
