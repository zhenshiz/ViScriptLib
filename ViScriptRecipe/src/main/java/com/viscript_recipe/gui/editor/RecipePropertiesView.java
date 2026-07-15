package com.viscript_recipe.gui.editor;

import com.lowdragmc.lowdraglib2.configurator.accessors.BlockAccessor;
import com.lowdragmc.lowdraglib2.configurator.accessors.FluidStackAccessor;
import com.lowdragmc.lowdraglib2.configurator.accessors.ItemStackAccessor;
import com.lowdragmc.lowdraglib2.configurator.ui.ConfiguratorGroup;
import com.lowdragmc.lowdraglib2.configurator.ui.TagKeySearchComponent;
import com.lowdragmc.lowdraglib2.editor.ui.View;
import com.lowdragmc.lowdraglib2.gui.texture.FluidStackTexture;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.Icons;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Switch;
import com.lowdragmc.lowdraglib2.gui.ui.utils.UIElementProvider;
import com.viscript_recipe.data.*;
import com.viscript_recipe.data.create.CreateFluidIngredientData;
import com.viscript_recipe.data.create.CreateFluidIngredientKind;
import com.viscript_recipe.data.create.CreateSequencedAssemblyStepKind;
import com.viscript_recipe.data.goety.GoetyBrewingEntityKind;
import com.viscript_recipe.data.vanilla.CraftingRemainderMode;
import com.viscript_recipe.data.vanilla.CraftingRemainderRule;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class RecipePropertiesView extends View {
    private final RecipeEditorController controller;
    private final UIElement content = RecipeEditorUi.column();
    private boolean rebuilding;
    private PropertiesStructureKey lastStructureKey;

    public RecipePropertiesView(RecipeEditorController controller) {
        super("viscript_recipe.view.recipe_properties", Icons.SETTINGS);
        this.controller = controller;
        addChild(createRoot());
        controller.addListener(this::refresh);
        refresh();
    }

    private UIElement createRoot() {
        var root = RecipeEditorUi.panelRoot();
        var scroller = new ScrollerView();
        scroller.layout(layout -> {
            layout.widthPercent(100);
            layout.flex(1);
        });
        scroller.verticalScroller.headButton.setDisplay(false);
        scroller.verticalScroller.tailButton.setDisplay(false);
        scroller.horizontalScroller.setDisplay(false);
        scroller.addScrollViewChild(content.layout(layout -> {
            layout.widthPercent(100);
            layout.gapAll(6);
        }));
        root.addChild(scroller);
        return root;
    }

    private void refresh() {
        if (rebuilding) {
            return;
        }
        var structureKey = createStructureKey();
        if (structureKey.equals(lastStructureKey)) {
            return;
        }
        rebuilding = true;
        try {
            lastStructureKey = structureKey;
            content.clearAllChildren();
            var entry = controller.getSelectedEntry();
            if (entry == null) {
                content.addChild(RecipeEditorUi.label(Component.translatable("viscript_recipe.editor.no_entry")));
                return;
            }
            switch (controller.getSlotSelection().kind()) {
                case RECIPE -> buildRecipeProperties(entry);
                case INGREDIENT -> buildIngredientProperties(entry);
                case FLUID -> buildFluidProperties();
                case RESULT -> buildResultProperties();
                case CONTAINER -> buildContainerProperties();
                case CUTTING_RESULT -> buildCuttingResultProperties();
                case CREATE_RESULT -> buildCreateResultProperties();
                case CREATE_TRANSITIONAL -> buildCreateSequencedTransitionalProperties();
                case CREATE_SEQUENCED_STEP -> buildCreateSequencedStepProperties(entry);
                case ARS_NOUVEAU_OUTPUT -> buildArsNouveauOutputProperties();
            }
        } finally {
            rebuilding = false;
        }
    }

    private PropertiesStructureKey createStructureKey() {
        var entry = controller.getSelectedEntry();
        var selection = controller.getSlotSelection();
        return new PropertiesStructureKey(
                entry == null ? 0 : System.identityHashCode(entry),
                controller.getSelectedCategory(),
                entry == null ? null : entry.getType(),
                selection,
                selectedIngredientKind(entry, selection),
                selectedRemainderMode(entry, selection),
                selectedCreateFluidIngredientKind(),
                selectedCreateFluidIngredientSignature(),
                entry != null && controller.supportsNotification(entry),
                entry != null && controller.isCookingEntry(entry),
                entry != null && controller.isFarmersCookingPotEntry(entry),
                entry != null && controller.isFarmersCuttingBoardEntry(entry),
                entry != null && controller.isFarmersCuttingBoardEntry(entry) && controller.getFarmersCuttingCustomSound(entry),
                entry != null && controller.isIronAlchemistCauldronEntry(entry),
                entry != null && controller.isIronAlchemistCauldronFillEntry(entry),
                entry != null && controller.isIronAlchemistCauldronEmptyEntry(entry),
                entry != null && controller.isIronAlchemistCauldronBrewEntry(entry),
                entry != null && controller.isIceAndFireDragonForgeEntry(entry),
                entry != null && controller.isCreateMechanicalCraftingEntry(entry),
                entry != null && controller.isCreateSequencedAssemblyEntry(entry),
                entry != null && controller.isCreateProcessingEntry(entry),
                entry != null && controller.isExtendedCraftingTableEntry(entry),
                entry != null && controller.isExtendedCraftingShapedTableEntry(entry),
                entry != null && controller.isExtendedCraftingEnderCrafterEntry(entry),
                entry != null && controller.isExtendedCraftingFluxCrafterEntry(entry),
                entry != null && controller.isExtendedCraftingCombinationEntry(entry),
                entry != null && controller.isExtendedCraftingCompressorEntry(entry),
                entry != null && controller.isAvaritiaTableEntry(entry),
                entry != null && controller.isAvaritiaShapedTableEntry(entry),
                entry != null && controller.isAvaritiaCompressorEntry(entry),
                entry != null && controller.isGoetyRitualEntry(entry) && entry.getGoetyRitual().isHasSacrifice(),
                entry != null && controller.isGoetyRitualEntry(entry) && entry.getGoetyRitual().isHasSummon(),
                entry != null && controller.isGoetyRitualEntry(entry) && entry.getGoetyRitual().isHasConversion(),
                entry != null && controller.isGoetyRitualEntry(entry) && entry.getGoetyRitual().isHasStructure(),
                entry != null && controller.isGoetyRitualEntry(entry) && entry.getGoetyRitual().isHasEnchantment(),
                entry != null && controller.isGoetyBrewingEntry(entry)
                        ? entry.getGoetyBrewing().getEntityKind()
                        : null,
                controller.isSelectedExtendedCraftingCompressorInput(),
                controller.isSelectedCreateFluidInput(),
                controller.isSelectedCreateFluidOutput(),
                controller.selectedCreateDurationAllowed(),
                controller.selectedCreateHeatAllowed(),
                controller.selectedCreateKeepHeldItemAllowed(),
                controller.selectedCreateOutputChanceAllowed(),
                controller.selectedCreateCountedInputSignature(),
                createSequencedStructureSignature(entry)
        );
    }

    private String createSequencedStructureSignature(RecipeEntry entry) {
        if (entry == null || !controller.isCreateSequencedAssemblyEntry(entry)) {
            return "";
        }
        var builder = new StringBuilder().append(controller.selectedCreateSequencedStepCount()).append(':');
        for (int i = 0; i < controller.selectedCreateSequencedStepCount(); i++) {
            builder.append(controller.getCreateSequencedStepKind(entry, i).getSerializedName()).append(';');
        }
        return builder.toString();
    }

    private IngredientValueKind selectedIngredientKind(RecipeEntry entry, WorkbenchSlotSelection selection) {
        if (entry == null || selection.kind() != WorkbenchSlotSelection.Kind.INGREDIENT) {
            return null;
        }
        return editableValue(controller.getSelectedIngredient()).getKind();
    }

    private CraftingRemainderMode selectedRemainderMode(RecipeEntry entry, WorkbenchSlotSelection selection) {
        if (entry == null || !entry.isType(RecipeEditorTypes.CRAFTING_SHAPED) || selection.kind() != WorkbenchSlotSelection.Kind.INGREDIENT) {
            return null;
        }
        return remainderMode(controller.getSelectedRemainder());
    }

    private CreateFluidIngredientKind selectedCreateFluidIngredientKind() {
        if (!controller.isSelectedCreateFluidInput()) {
            return null;
        }
        var ingredient = controller.getSelectedCreateFluidIngredient();
        return ingredient.getKind() == null ? CreateFluidIngredientKind.FLUID : ingredient.getKind();
    }

    private String selectedCreateFluidIngredientSignature() {
        if (!controller.isSelectedCreateFluidInput()) {
            return "";
        }
        var ingredient = controller.getSelectedCreateFluidIngredient();
        var kind = ingredient.getKind() == null ? CreateFluidIngredientKind.FLUID : ingredient.getKind();
        if (kind == CreateFluidIngredientKind.TAG) {
            return "tag:" + ingredient.getTag() + ":" + Math.max(1, ingredient.getAmount());
        }
        var stack = ingredient.getFluid();
        if (stack == null || stack.isEmpty()) {
            return "fluid:empty";
        }
        var fluidId = stack.getFluid().toString();
        return "fluid:" + fluidId + ":" + stack.getAmount();
    }

    private record PropertiesStructureKey(
            int entryIdentity,
            ResourceLocation selectedCategory,
            ResourceLocation type,
            WorkbenchSlotSelection selection,
            IngredientValueKind ingredientKind,
            CraftingRemainderMode remainderMode,
            CreateFluidIngredientKind createFluidIngredientKind,
            String createFluidIngredientSignature,
            boolean supportsNotification,
            boolean cooking,
            boolean farmersCookingPot,
            boolean farmersCuttingBoard,
            boolean farmersCuttingCustomSound,
            boolean ironAlchemistCauldron,
            boolean ironAlchemistCauldronFill,
            boolean ironAlchemistCauldronEmpty,
            boolean ironAlchemistCauldronBrew,
            boolean iceAndFireDragonForge,
            boolean createMechanicalCrafting,
            boolean createSequencedAssembly,
            boolean createProcessing,
            boolean extendedCraftingTable,
            boolean extendedCraftingShapedTable,
            boolean extendedCraftingEnderCrafter,
            boolean extendedCraftingFluxCrafter,
            boolean extendedCraftingCombination,
            boolean extendedCraftingCompressor,
            boolean avaritiaTable,
            boolean avaritiaShapedTable,
            boolean avaritiaCompressor,
            boolean goetyRitualSacrifice,
            boolean goetyRitualSummon,
            boolean goetyRitualConversion,
            boolean goetyRitualStructure,
            boolean goetyRitualEnchantment,
            GoetyBrewingEntityKind goetyBrewingEntityKind,
            boolean selectedExtendedCraftingCompressorInput,
            boolean selectedCreateFluidInput,
            boolean selectedCreateFluidOutput,
            boolean createDurationAllowed,
            boolean createHeatAllowed,
            boolean createKeepHeldItemAllowed,
            boolean createOutputChanceAllowed,
            int createCountedInputSignature,
            String createSequencedSignature
    ) {
    }

    private void buildRecipeProperties(RecipeEntry entry) {
        content.addChildren(
                RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.recipe"),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.entry.type",
                        RecipeEditorUi.selector(
                                controller.availableTypesForSelectedCategory(),
                                controller.getSelectedRecipeType(),
                                type -> type.displayName(),
                                controller::setSelectedRecipeType
                        )),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.entry.enabled",
                        new Switch()
                                .setOn(entry.isEnabled(), false)
                                .setOnSwitchChanged(value -> {
                                    entry.setEnabled(value);
                                    controller.notifyChanged();
                                })),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.entry.recipe_id",
                        RecipeEditorUi.resourceLocationField(entry.getRecipeId(), value -> {
                            entry.setRecipeId(value);
                            controller.notifyChanged();
                        })),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.entry.operation",
                        RecipeEditorUi.selector(
                                List.of(RecipeOperation.ADD, RecipeOperation.REPLACE, RecipeOperation.REMOVE),
                                entry.getOperation(),
                                operation -> Component.translatable("viscript_recipe.editor.operation." + operation.getSerializedName()),
                                operation -> {
                                    entry.setOperation(operation);
                                    controller.notifyChanged();
                                }
                        ))
        );
        if (controller.supportsNotification(entry)) {
            content.addChild(RecipeEditorUi.fieldGroup("viscript_recipe.config.recipe.show_notification",
                    new Switch()
                            .setOn(controller.showNotification(entry), false)
                            .setOnSwitchChanged(value -> controller.setShowNotification(entry, value))));
        }
        if (controller.isCookingEntry(entry)) {
            RecipePropertiesSections.buildCooking(content, controller, entry);
        }
        if (controller.isFarmersCookingPotEntry(entry)) {
            RecipePropertiesSections.buildFarmersCooking(content, controller, entry);
        }
        if (controller.isFarmersCuttingBoardEntry(entry)) {
            RecipePropertiesSections.buildFarmersCutting(content, controller, entry);
        }
        if (controller.isIronAlchemistCauldronEntry(entry)) {
            RecipePropertiesSections.buildAlchemistCauldron(content, controller, entry);
        }
        if (controller.isIceAndFireDragonForgeEntry(entry)) {
            RecipePropertiesSections.buildDragonForge(content, controller, entry);
        }
        if (controller.isCataclysmAmethystBlessEntry(entry)) {
            RecipePropertiesSections.buildCataclysmAmethystBless(content, controller, entry);
        }
        if (controller.isTouhouLittleMaidAltarEntry(entry)) {
            RecipePropertiesSections.buildTouhouLittleMaidAltar(content, controller, entry);
        }
        if (controller.isGoetyCursedInfuserEntry(entry)) {
            RecipePropertiesSections.buildGoetyCursedInfuser(content, controller, entry);
        }
        if (controller.isGoetyRitualEntry(entry)) {
            RecipePropertiesSections.buildGoetyRitual(content, controller, entry);
        }
        if (controller.isGoetyBrazierEntry(entry)) {
            RecipePropertiesSections.buildGoetyBrazier(content, controller, entry);
        }
        if (controller.isGoetyPulverizeEntry(entry)) {
            RecipePropertiesSections.buildGoetyPulverize(content, controller, entry);
        }
        if (controller.isGoetyBrewingEntry(entry)) {
            RecipePropertiesSections.buildGoetyBrewing(content, controller, entry);
        }
        if (controller.isCreateMechanicalCraftingEntry(entry)) {
            RecipePropertiesSections.buildCreateMechanicalCrafting(content, controller, entry);
        }
        if (controller.isCreateSequencedAssemblyEntry(entry)) {
            RecipePropertiesSections.buildCreateSequencedAssembly(content, controller, entry);
        }
        if (controller.isCreateProcessingEntry(entry)) {
            RecipePropertiesSections.buildCreateProcessing(content, controller, entry);
        }
        if (controller.isExtendedCraftingTableEntry(entry)) {
            RecipePropertiesSections.buildExtendedCraftingTable(content, controller, entry);
        }
        if (controller.isExtendedCraftingEnderCrafterEntry(entry)) {
            RecipePropertiesSections.buildExtendedCraftingEnderCrafter(content, controller, entry);
        }
        if (controller.isExtendedCraftingFluxCrafterEntry(entry)) {
            RecipePropertiesSections.buildExtendedCraftingFluxCrafter(content, controller, entry);
        }
        if (controller.isExtendedCraftingCombinationEntry(entry)) {
            RecipePropertiesSections.buildExtendedCraftingCombination(content, controller, entry);
        }
        if (controller.isExtendedCraftingCompressorEntry(entry)) {
            RecipePropertiesSections.buildExtendedCraftingCompressor(content, controller, entry);
        }
        if (controller.isAvaritiaTableEntry(entry)) {
            RecipePropertiesSections.buildAvaritiaTable(content, controller, entry);
        }
        if (controller.isAvaritiaCompressorEntry(entry)) {
            RecipePropertiesSections.buildAvaritiaCompressor(content, controller, entry);
        }
        if (controller.isArsNouveauApparatusLayoutEntry(entry) || controller.isArsNouveauImbuementLayoutEntry(entry) || controller.isArsNouveauGlyphLayoutEntry(entry)) {
            RecipePropertiesSections.buildArsNouveauApparatus(content, controller, entry, () -> rebuilding);
        }
        if (controller.isArsNouveauCrushEntry(entry)) {
            RecipePropertiesSections.buildArsNouveauCrush(content, controller, entry);
        }
        if (controller.isKaleidoscopePotEntry(entry)
                || controller.isKaleidoscopeStockpotEntry(entry)
                || controller.isKaleidoscopeMillstoneEntry(entry)
                || controller.isKaleidoscopeChoppingBoardEntry(entry)
                || controller.isKaleidoscopeSteamerEntry(entry)
                || controller.isKaleidoscopeTeapotEntry(entry)) {
            RecipePropertiesSections.buildKaleidoscope(content, controller, entry, () -> rebuilding);
        }
    }

    private void buildIngredientProperties(RecipeEntry entry) {
        var ingredient = copyIngredient(controller.getSelectedIngredient());
        var value = editableValue(ingredient);
        var availableKinds = controller.availableIngredientKindsForSelectedSlot();
        var selectedKind = availableKinds.contains(value.getKind()) ? value.getKind() : availableKinds.get(0);
        content.addChildren(
                RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.ingredient"),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.ingredient.value.kind",
                        RecipeEditorUi.selector(
                                availableKinds,
                                selectedKind,
                                kind -> Component.translatable("viscript_recipe.editor.ingredient.kind." + kind.getSerializedName()),
                                kind -> setIngredientKind(ingredient, value, kind)
                        ))
        );
        if (controller.isCreateAutoPackingEntry(entry)) {
            content.addChild(RecipeEditorUi.fieldGroup("viscript_recipe.config.create.auto_packing.grid_size",
                    RecipeEditorUi.selector(
                            controller.createAutoPackingGridSizes(),
                            controller.getCreateAutoPackingGridSize(entry),
                            controller::createAutoPackingGridSizeDisplayName,
                            gridSize -> controller.setCreateAutoPackingGridSize(entry, gridSize)
                    )));
        }

        if (selectedKind == IngredientValueKind.ITEM && controller.isSelectedCreateItemApplicationBlockInput()) {
            content.addChild(createBlockConfigurator(
                    "viscript_recipe.editor.create.item_application.base_block",
                    () -> ingredientBlock(ingredient),
                    block -> setIngredientBlock(ingredient, block)
            ));
        } else if (selectedKind == IngredientValueKind.ITEM) {
            content.addChild(createItemStackConfigurator(
                    "viscript_recipe.editor.ingredient.item_slot",
                    () -> ingredientItemStack(ingredient),
                    stack -> setIngredientItem(ingredient, stack)
            ));
        } else if (selectedKind == IngredientValueKind.TAG) {
            content.addChild(createItemTagConfigurator(ingredient, value));
        } else if (selectedKind == IngredientValueKind.ITEM_ABILITY) {
            content.addChild(createItemAbilityConfigurator(ingredient, value));
        }
        if (controller.isSelectedExtendedCraftingCompressorInput()) {
            content.addChild(RecipeEditorUi.fieldGroup("viscript_recipe.config.extendedcrafting.counted_ingredient.count",
                    RecipeEditorUi.intField(controller.getSelectedExtendedCraftingCompressorInputCount(), 1, Integer.MAX_VALUE,
                            controller::setSelectedExtendedCraftingCompressorInputCount)));
        }
        if (entry.isType(RecipeEditorTypes.CRAFTING_SHAPED)) {
            buildRemainderProperties();
        }
    }

    private void buildRemainderProperties() {
        var remainder = controller.getSelectedRemainder();
        var mode = remainderMode(remainder);
        content.addChildren(
                RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.remainder"),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.remainder.mode",
                        RecipeEditorUi.selector(
                                List.of(CraftingRemainderMode.DEFAULT, CraftingRemainderMode.CONSUME, CraftingRemainderMode.REPLACE),
                                mode,
                                value -> Component.translatable("viscript_recipe.editor.remainder.mode." + value.getSerializedName()),
                                value -> {
                                    var updated = remainder.copy();
                                    updated.setMode(value);
                                    if (value != CraftingRemainderMode.REPLACE) {
                                        updated.setItem(ItemStack.EMPTY);
                                    }
                                    controller.setSelectedRemainder(updated);
                                }
                        ),
                        Component.translatable("viscript_recipe.editor.remainder.tip.default"),
                        Component.translatable("viscript_recipe.editor.remainder.tip.consume"),
                        Component.translatable("viscript_recipe.editor.remainder.tip.replace"))
        );

        if (mode == CraftingRemainderMode.REPLACE) {
            content.addChild(createItemStackConfigurator(
                    "viscript_recipe.config.remainder.item",
                    () -> remainder.getItem() == null ? ItemStack.EMPTY : remainder.getItem().copy(),
                    stack -> {
                        var updated = remainder.copy();
                        updated.setMode(CraftingRemainderMode.REPLACE);
                        updated.setItem(stack == null ? ItemStack.EMPTY : stack.copy());
                        controller.setSelectedRemainder(updated);
                    }
            ));
        }
    }

    private void buildResultProperties() {
        content.addChildren(
                RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.result"),
                createItemStackConfigurator(
                        "viscript_recipe.config.recipe.result",
                        controller::getSelectedResult,
                        stack -> controller.setSelectedResult(normalizeResultStack(stack))
                )
        );
    }

    private void buildCreateSequencedTransitionalProperties() {
        content.addChildren(
                RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.create.sequenced_assembly.transitional_item"),
                createItemStackConfigurator(
                        "viscript_recipe.config.create.sequenced_assembly.transitional_item",
                        controller::getVisualCreateSequencedTransitional,
                        stack -> controller.setVisualCreateSequencedTransitional(stack == null ? ItemStack.EMPTY : stack.copyWithCount(1))
                )
        );
    }

    private void buildCreateSequencedStepProperties(RecipeEntry entry) {
        RecipePropertiesSections.buildSelectedCreateSequencedStep(content, controller, entry, () -> buildRecipeProperties(entry));
        var selection = controller.getSlotSelection();
        if (controller.isCreateSequencedAssemblyEntry(entry)
                && selection.kind() == WorkbenchSlotSelection.Kind.CREATE_SEQUENCED_STEP
                && selection.index() >= 0
                && selection.index() < controller.selectedCreateSequencedStepCount()
                && controller.getCreateSequencedStepKind(entry, selection.index()) == CreateSequencedAssemblyStepKind.FILLING) {
            buildCreateFluidIngredientProperties();
        }
    }

    private void buildContainerProperties() {
        content.addChildren(
                RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.farmersdelight.container"),
                createItemStackConfigurator(
                        "viscript_recipe.config.farmersdelight.cooking.container",
                        controller::getSelectedContainer,
                        stack -> controller.setSelectedContainer(stack == null ? ItemStack.EMPTY : stack.copy())
                )
        );
    }

    private void buildCuttingResultProperties() {
        content.addChildren(
                RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.farmersdelight.cutting_result"),
                createItemStackConfigurator(
                        "viscript_recipe.config.farmersdelight.cutting.result_item",
                        controller::getSelectedCuttingResult,
                        stack -> controller.setSelectedCuttingResult(normalizeResultStack(stack))
                ),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.farmersdelight.cutting.chance",
                        RecipeEditorUi.floatField(controller.getSelectedCuttingChance(), 0, 1,
                                controller::setSelectedCuttingChance))
        );
    }

    private void buildCreateResultProperties() {
        content.addChildren(
                RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.create.output"),
                createItemStackConfigurator(
                        "viscript_recipe.config.create.output.item",
                        controller::getSelectedCreateOutput,
                        stack -> controller.setSelectedCreateOutput(normalizeResultStack(stack))
                )
        );
        if (controller.selectedCreateOutputChanceAllowed()) {
            var labelKey = controller.getSelectedEntry() != null && controller.isCreateSequencedAssemblyEntry(controller.getSelectedEntry())
                    ? "viscript_recipe.config.create.output.weight"
                    : "viscript_recipe.config.create.output.chance";
            var max = controller.getSelectedEntry() != null && controller.isCreateSequencedAssemblyEntry(controller.getSelectedEntry())
                    ? Integer.MAX_VALUE
                    : 1;
            content.addChild(RecipeEditorUi.fieldGroup(labelKey,
                    RecipeEditorUi.floatField(controller.getSelectedCreateOutputChance(), 0, max,
                            controller::setSelectedCreateOutputChance)));
        }
    }

    private void buildArsNouveauOutputProperties() {
        content.addChildren(
                RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.ars_nouveau.output"),
                createItemStackConfigurator(
                        "viscript_recipe.config.ars_nouveau.crush.output_item",
                        controller::getSelectedArsNouveauOutput,
                        stack -> controller.setSelectedArsNouveauOutput(normalizeResultStack(stack))
                ),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.ars_nouveau.crush.chance",
                        RecipeEditorUi.floatField(controller.getSelectedArsNouveauOutputChance(), 0, 1,
                                controller::setSelectedArsNouveauOutputChance)),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.ars_nouveau.crush.max_range",
                        RecipeEditorUi.intField(controller.getSelectedArsNouveauOutputMaxRange(), 1, Integer.MAX_VALUE,
                                controller::setSelectedArsNouveauOutputMaxRange))
        );
    }

    private void buildFluidProperties() {
        if (controller.isSelectedCreateFluidInput()) {
            buildCreateFluidIngredientProperties();
            return;
        }
        content.addChildren(
                RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.fluid"),
                createFluidStackConfigurator(
                        controller.selectedFluidConfigNameKey(),
                        controller::getSelectedFluid,
                        stack -> controller.setSelectedFluid(stack == null ? FluidStack.EMPTY : stack.copy())
                )
        );
    }

    private void buildCreateFluidIngredientProperties() {
        var ingredient = controller.getSelectedCreateFluidIngredient();
        var kind = ingredient.getKind() == null ? CreateFluidIngredientKind.FLUID : ingredient.getKind();
        content.addChildren(
                RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.create.fluid_ingredient"),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.create.fluid_ingredient.kind",
                        RecipeEditorUi.selector(
                                List.of(CreateFluidIngredientKind.FLUID, CreateFluidIngredientKind.TAG),
                                kind,
                                value -> Component.translatable("viscript_recipe.editor.create.fluid_ingredient.kind." + value.getSerializedName()),
                                controller::setSelectedCreateFluidIngredientKind
                        ))
        );

        if (kind == CreateFluidIngredientKind.TAG) {
            content.addChildren(
                    createFluidTagConfigurator(ingredient),
                    RecipeEditorUi.fieldGroup("viscript_recipe.config.create.fluid_ingredient.amount",
                            RecipeEditorUi.intField(Math.max(1, ingredient.getAmount()), 1, Integer.MAX_VALUE,
                                    controller::setSelectedCreateFluidIngredientAmount))
            );
        } else {
            content.addChild(createFluidStackConfigurator(
                    "viscript_recipe.config.create.fluid_ingredient.fluid",
                    controller::getSelectedFluid,
                    stack -> controller.setSelectedFluid(stack == null ? FluidStack.EMPTY : stack.copy())
            ));
        }
    }

    private UIElement createItemStackConfigurator(String nameKey, Supplier<ItemStack> supplier, Consumer<ItemStack> consumer) {
        var configurator = new ItemStackAccessor().create(
                nameKey,
                () -> {
                    var stack = supplier.get();
                    return stack == null ? ItemStack.EMPTY : stack.copy();
                },
                stack -> {
                    if (!rebuilding) {
                        consumer.accept(stack == null ? ItemStack.EMPTY : stack.copy());
                    }
                },
                true,
                null,
                this
        );
        configurator.layout(layout -> layout.widthPercent(100));
        if (configurator instanceof ConfiguratorGroup group) {
            group.setCollapse(false);
        }
        return configurator;
    }

    private UIElement createBlockConfigurator(String nameKey, Supplier<Block> supplier, Consumer<Block> consumer) {
        var configurator = new BlockAccessor().create(
                nameKey,
                () -> {
                    var block = supplier.get();
                    return block == null ? Blocks.AIR : block;
                },
                block -> {
                    if (!rebuilding) {
                        consumer.accept(block == null ? Blocks.AIR : block);
                    }
                },
                true,
                null,
                this
        );
        configurator.layout(layout -> layout.widthPercent(100));
        return configurator;
    }

    private UIElement createFluidStackConfigurator(String nameKey, Supplier<FluidStack> supplier, Consumer<FluidStack> consumer) {
        var configurator = new FluidStackAccessor().create(
                nameKey,
                () -> {
                    var stack = supplier.get();
                    return stack == null ? FluidStack.EMPTY : stack.copy();
                },
                stack -> {
                    if (!rebuilding) {
                        consumer.accept(stack == null ? FluidStack.EMPTY : stack.copy());
                    }
                },
                true,
                null,
                this
        );
        configurator.layout(layout -> layout.widthPercent(100));
        if (configurator instanceof ConfiguratorGroup group) {
            group.setCollapse(false);
        }
        return configurator;
    }

    private UIElement createItemTagConfigurator(RecipeIngredient ingredient, RecipeIngredientValue value) {
        var configurator = new TagKeySearchComponent.Item(
                "viscript_recipe.config.ingredient.value.tag",
                () -> itemTag(value.getTag()),
                tag -> {
                    if (!rebuilding) {
                        ingredient.getValues().clear();
                        value.setKind(IngredientValueKind.TAG);
                        value.setTag(tag.location());
                        ingredient.getValues().add(value);
                        controller.setSelectedIngredient(ingredient);
                    }
                },
                itemTag(defaultTag()),
                true
        );
        configurator.layout(layout -> layout.widthPercent(100));
        configurator.searchComponent.searchStyle(style -> {
            style.maxItemCount(8);
            style.scrollerViewHeight(120);
        });
        return configurator;
    }

    private UIElement createFluidTagConfigurator(CreateFluidIngredientData ingredient) {
        var configurator = new TagKeySearchComponent<Fluid>(
                "viscript_recipe.config.create.fluid_ingredient.tag",
                () -> fluidTag(ingredient.getTag()),
                tag -> {
                    if (!rebuilding) {
                        controller.setSelectedCreateFluidIngredientTag(tag.location());
                    }
                },
                fluidTag(defaultFluidTag()),
                true,
                BuiltInRegistries.FLUID,
                UIElementProvider.iconText(
                        this::fluidTagIcon,
                        tag -> Component.literal(tag.location().toString())
                )
        );
        configurator.layout(layout -> layout.widthPercent(100));
        configurator.searchComponent.searchStyle(style -> {
            style.maxItemCount(8);
            style.scrollerViewHeight(120);
        });
        return configurator;
    }

    private IGuiTexture fluidTagIcon(TagKey<Fluid> tag) {
        var fluids = BuiltInRegistries.FLUID.getTag(tag)
                .map(holders -> holders.stream()
                        .map(Holder::value)
                        .filter(fluid -> fluid != Fluids.EMPTY)
                        .toList())
                .orElseGet(List::of);
        var sourceFluids = fluids.stream()
                .filter(fluid -> fluid.defaultFluidState().isSource())
                .toList();
        var displayFluids = sourceFluids.isEmpty() ? fluids : sourceFluids;
        if (displayFluids.isEmpty()) {
            return IGuiTexture.EMPTY;
        }
        var stacks = displayFluids.stream()
                .map(fluid -> new FluidStack(fluid, 1000))
                .filter(stack -> !stack.isEmpty())
                .toArray(FluidStack[]::new);
        return stacks.length == 0 ? IGuiTexture.EMPTY : new FluidStackTexture(stacks);
    }

    private UIElement createItemAbilityConfigurator(RecipeIngredient ingredient, RecipeIngredientValue value) {
        return RecipeEditorUi.fieldGroup(
                "viscript_recipe.config.ingredient.value.item_ability",
                RecipeEditorUi.selector(
                        controller.itemAbilityChoices(),
                        itemAbilityValue(value),
                        controller::itemAbilityDisplayName,
                        itemAbility -> {
                            if (!rebuilding) {
                                ingredient.getValues().clear();
                                value.setKind(IngredientValueKind.ITEM_ABILITY);
                                value.setItemAbility(itemAbility == null || itemAbility.isBlank() ? "knife_dig" : itemAbility);
                                ingredient.getValues().add(value);
                                controller.setSelectedIngredient(ingredient);
                            }
                        }
                )
        );
    }

    private TagKey<Item> itemTag(ResourceLocation tag) {
        return TagKey.create(Registries.ITEM, tag == null ? defaultTag() : tag);
    }

    private TagKey<Fluid> fluidTag(ResourceLocation tag) {
        return TagKey.create(Registries.FLUID, tag == null ? defaultFluidTag() : tag);
    }

    private ResourceLocation defaultTag() {
        return new ResourceLocation("minecraft", "planks");
    }

    private ResourceLocation defaultFluidTag() {
        return new ResourceLocation("c", "water");
    }

    private ItemStack normalizeResultStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        var copy = stack.copy();
        copy.setCount(Math.max(0, Math.min(99, copy.getCount())));
        return copy;
    }

    private void setIngredientKind(RecipeIngredient ingredient, RecipeIngredientValue currentValue, IngredientValueKind kind) {
        var updated = new RecipeIngredientValue().setKind(kind);
        ingredient.getValues().clear();
        if (kind == IngredientValueKind.ITEM) {
            var stack = controller.normalizeSelectedIngredientItemStack(currentValue.getItem());
            if (!stack.isEmpty()) {
                updated.setItem(stack);
                ingredient.getValues().add(updated);
            }
        } else if (kind == IngredientValueKind.TAG) {
            updated.setTag(currentValue.getTag() == null
                    ? new ResourceLocation("minecraft", "planks")
                    : currentValue.getTag());
            ingredient.getValues().add(updated);
        } else if (kind == IngredientValueKind.ITEM_ABILITY) {
            updated.setItemAbility(itemAbilityValue(currentValue));
            ingredient.getValues().add(updated);
        }
        controller.setSelectedIngredient(ingredient);
    }

    private ItemStack ingredientItemStack(RecipeIngredient ingredient) {
        for (var value : ingredient.getValues()) {
            if (value.getKind() == IngredientValueKind.ITEM && value.getItem() != null) {
                return value.getItem().copy();
            }
        }
        return ItemStack.EMPTY;
    }

    private Block ingredientBlock(RecipeIngredient ingredient) {
        var stack = ingredientItemStack(ingredient);
        return stack.getItem() instanceof BlockItem blockItem ? blockItem.getBlock() : Blocks.AIR;
    }

    private RecipeIngredientValue editableValue(RecipeIngredient ingredient) {
        if (ingredient.getValues().isEmpty()) {
            return new RecipeIngredientValue()
                    .setKind(IngredientValueKind.ITEM)
                    .setItem(ItemStack.EMPTY);
        }
        return ingredient.getValues().get(0);
    }

    private void setIngredientItem(RecipeIngredient ingredient, ItemStack stack) {
        ingredient.getValues().clear();
        var normalizedStack = controller.normalizeSelectedIngredientItemStack(stack);
        if (!normalizedStack.isEmpty()) {
            ingredient.getValues().add(new RecipeIngredientValue()
                    .setKind(IngredientValueKind.ITEM)
                    .setItem(normalizedStack));
        }
        controller.setSelectedIngredient(ingredient);
    }

    private void setIngredientBlock(RecipeIngredient ingredient, Block block) {
        ingredient.getValues().clear();
        if (block != null && block != Blocks.AIR && block.asItem() != Items.AIR) {
            ingredient.getValues().add(new RecipeIngredientValue()
                    .setKind(IngredientValueKind.ITEM)
                    .setItem(new ItemStack(block)));
        }
        controller.setSelectedIngredient(ingredient);
    }

    private CraftingRemainderMode remainderMode(CraftingRemainderRule remainder) {
        return remainder.getMode() == null ? CraftingRemainderMode.DEFAULT : remainder.getMode();
    }

    private RecipeIngredient copyIngredient(RecipeIngredient original) {
        var copy = new RecipeIngredient();
        for (var value : original.getValues()) {
            var valueCopy = new RecipeIngredientValue()
                    .setKind(value.getKind())
                    .setTag(value.getTag())
                    .setItemAbility(value.getItemAbility());
            if (value.getItem() != null) {
                valueCopy.setItem(value.getItem().copy());
            }
            copy.getValues().add(valueCopy);
        }
        return copy;
    }

    private String itemAbilityValue(RecipeIngredientValue value) {
        var itemAbility = value == null ? null : value.getItemAbility();
        return itemAbility == null || itemAbility.isBlank() ? "knife_dig" : itemAbility;
    }
}
