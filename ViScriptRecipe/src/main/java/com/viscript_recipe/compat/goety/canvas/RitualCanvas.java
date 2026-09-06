package com.viscript_recipe.compat.goety.canvas;

import com.Polarice3.Goety.common.research.ResearchList;
import com.google.common.util.concurrent.Runnables;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.viscript_recipe.compat.goety.GoetyRecipeUiSupport;
import com.viscript_recipe.compat.goety.data.GoetyRitualCraftType;
import com.viscript_recipe.compat.goety.data.GoetyRitualRecipeData;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.editor.IngredientDisplaySlot;
import com.viscript_recipe.gui.editor.RecipeSearchComponents;
import com.viscript_recipe.gui.views.NavigationView;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

import static com.viscript_recipe.compat.goety.canvas.BrazierCanvas.useJeiCanvas;

public class RitualCanvas extends RecipeCanvas<GoetyRitualRecipeData> {
    static final UIElement typeIcon = new UIElement();
    static final UIElement researchIcon = new UIElement();
    static final Label infoLabel = emptyLabel();
    static {
        tooltip(typeIcon, "viscript_recipe.editor.goety.ritual.type_icon");
        tooltip(researchIcon, "viscript_recipe.editor.goety.ritual.research_scroll");
        BrazierCanvas.centerLabel(infoLabel);
    }

    public RitualCanvas(NavigationView navigationView, RecipeEntry entry) { super(navigationView, entry); }

    @Override
    public void load() {
        var data = getData();
        loadIngredientSlot(0, data.getActivationItem());
        loadIngredients(data.getIngredients(), 1);
        setVisualOutput(0, data.getResult());
        updatePreview();
    }

    @Override
    public void save() {
        var data = getData();
        data.setActivationItem(getVisualIngredient(0)).setResult(getVisualOutput(0).getItem());
        data.setIngredients(getIngredients(12, 1, true));
    }

    @Override
    public UIElement createCanvas() {
        var activation = createIngredientSlot(0, useJeiCanvas ? JEI_SLOT_SIZE : SLOT_SIZE);
        tooltip(activation, "viscript_recipe.editor.goety.ritual.activation_slot");
        if (useJeiCanvas) configureJeiOverlaySlotVisual(activation);
        var ingredients = new IngredientDisplaySlot[12];
        for (int i = 0; i < ingredients.length; i++) {
            var slot = createIngredientSlot(i + 1, useJeiCanvas ? JEI_SLOT_SIZE : SLOT_SIZE);
            tooltip(slot, Component.translatable("viscript_recipe.editor.goety.ritual.ingredient_slot", i + 1));
            if (useJeiCanvas) configureJeiOverlaySlotVisual(slot);
            ingredients[i] = slot;
        }
        var output = createOutputSlot(0, useJeiCanvas ? JEI_SLOT_SIZE : OUTPUT_SLOT_SIZE);
        if (useJeiCanvas) configureJeiOverlaySlotVisual(output);
        return GoetyCanvasFactory.createRitualCanvas(activation, ingredients, output,
                typeIcon, researchIcon, infoLabel, useJeiCanvas);
    }

    @Override
    public void buildRecipeProperties(UIElement content) {
        var data = getData();
        content.addChildren(sectionTitle("viscript_recipe.editor.properties.goety.ritual"),
                selector("viscript_recipe.config.goety.ritual.craft_type",
                        List.of(GoetyRitualCraftType.values()), data.getCraftType(),
                        GoetyRitualCraftType::displayName, data::setCraftType, this::updatePreview
                ),
                GoetyRitualSearchComponents.ritualType(data::getRitualType, data::setRitualType, Runnables.doNothing()),
                intField("viscript_recipe.config.goety.soul_cost", data.getSoulCost(), 0, Integer.MAX_VALUE,
                        data::setSoulCost, this::updatePreview),
                intField("viscript_recipe.config.goety.duration", data.getDuration(), 1, Integer.MAX_VALUE,
                        data::setDuration, this::updatePreview),
                selector("viscript_recipe.config.goety.ritual.research",
                        goetyResearchIds(), data.getResearch(), RitualCanvas::goetyResearchName,
                        data::setResearch, this::updatePreview
                )
        );

        content.addChild(switchField("viscript_recipe.config.goety.ritual.has_sacrifice",
                data.isHasSacrifice(), data::setHasSacrifice, RecipeCanvas::reloadProperties));
        if (data.isHasSacrifice()) content.addChildren(
                RecipeSearchComponents.entityTag("viscript_recipe.config.goety.ritual.entity_to_sacrifice",
                        data::getEntityToSacrifice, data::setEntityToSacrifice, Runnables.doNothing()),
                textField("viscript_recipe.config.goety.ritual.entity_to_sacrifice_name",
                            data.getEntityToSacrificeDisplayName(), data::setEntityToSacrificeDisplayName)
        );

        content.addChild(switchField("viscript_recipe.config.goety.ritual.has_summon",
                data.isHasSummon(), data::setHasSummon, RecipeCanvas::reloadProperties));
        if (data.isHasSummon()) content.addChildren(
                RecipeSearchComponents.entityType("viscript_recipe.config.goety.ritual.entity_to_summon",
                        data::getEntityToSummon, data::setEntityToSummon, Runnables.doNothing(), EntityType.ZOMBIE),
                intField("viscript_recipe.config.goety.ritual.summon_life", data.getSummonLife(),
                        -1, Integer.MAX_VALUE, data::setSummonLife)
        );

        content.addChild(switchField("viscript_recipe.config.goety.ritual.has_conversion",
                data.isHasConversion(), data::setHasConversion, RecipeCanvas::reloadProperties));
        if (data.isHasConversion()) content.addChildren(
                RecipeSearchComponents.entityTag("viscript_recipe.config.goety.ritual.entity_to_convert",
                        data::getEntityToConvert, data::setEntityToConvert, Runnables.doNothing()),
                textField("viscript_recipe.config.goety.ritual.entity_to_convert_name",
                            data.getEntityToConvertDisplayName(), data::setEntityToConvertDisplayName),
                RecipeSearchComponents.entityType("viscript_recipe.config.goety.ritual.entity_to_convert_into",
                        data::getEntityToConvertInto, data::setEntityToConvertInto, Runnables.doNothing(), EntityType.ZOMBIE)
        );

        content.addChild(switchField("viscript_recipe.config.goety.ritual.has_structure",
                data.isHasStructure(), data::setHasStructure, RecipeCanvas::reloadProperties));
        if (data.isHasStructure()) content.addChildren(
                RecipeSearchComponents.structureTag("viscript_recipe.config.goety.ritual.structure_to_locate",
                        data::getStructureToLocate, data::setStructureToLocate, Runnables.doNothing()),
                textField("viscript_recipe.config.goety.ritual.structure_name",
                            data.getStructureDisplayName(), data::setStructureDisplayName)
        );

        content.addChild(switchField("viscript_recipe.config.goety.ritual.has_enchantment",
                data.isHasEnchantment(), data::setHasEnchantment, RecipeCanvas::reloadProperties));
        if (data.isHasEnchantment()) content.addChildren(
                GoetyRitualSearchComponents.enchantment("viscript_recipe.config.goety.ritual.enchantment",
                        data::getEnchantment, data::setEnchantment, Runnables.doNothing()),
                intField("viscript_recipe.config.goety.ritual.xp_level_cost", data.getXpLevelCost(),
                        0, Integer.MAX_VALUE, data::setXpLevelCost)
        );
    }

    private void updatePreview() {
        var data = getData();
        var craftType = data.getCraftType();
        setTexture(typeIcon, GoetyRecipeUiSupport.ritualTypeIcon(craftType.getSerializedName()));
        var research = GoetyRecipeUiSupport.researchScroll(data.getResearch());
        setTexture(researchIcon.setDisplay(!research.isEmpty()), research);
        infoLabel.setText(Component.translatable("viscript_recipe.editor.goety.ritual.info",
                craftType.displayName(), data.getSoulCost(), data.getDuration()));
    }

    private static List<String> goetyResearchIds() {
        var ids = new ArrayList<String>();
        ids.add("");
        ResearchList.getResearchList().keySet().stream().sorted().forEach(ids::add);
        return ids;
    }

    private static Component goetyResearchName(String id) {
        if (id.isBlank()) return Component.translatable("viscript_recipe.editor.goety.ritual.research.none");
        return Component.translatableWithFallback("item.goety." + id + "_scroll", id)
                .append(Component.literal(" (" + id + ")"));
    }
}
