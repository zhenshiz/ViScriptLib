package com.viscript_recipe.gui.editor;

import com.lowdragmc.lowdraglib2.Platform;
import com.viscript_recipe.data.*;
import com.viscript_recipe.data.ars_nouveau.*;
import com.viscript_recipe.data.avaritia.AvaritiaCompressorRecipeData;
import com.viscript_recipe.data.avaritia.AvaritiaRecipeEditorTypes;
import com.viscript_recipe.data.cataclysm.CataclysmAmethystBlessRecipeData;
import com.viscript_recipe.data.cataclysm.CataclysmRecipeEditorTypes;
import com.viscript_recipe.data.cataclysm.CataclysmWeaponFusionRecipeData;
import com.viscript_recipe.data.create.*;
import com.viscript_recipe.data.extendedcrafting.*;
import com.viscript_recipe.data.farmersdelight.FarmerCookingPotRecipeData;
import com.viscript_recipe.data.farmersdelight.FarmerCuttingRecipeData;
import com.viscript_recipe.data.farmersdelight.FarmerCuttingResultData;
import com.viscript_recipe.data.farmersdelight.FarmersDelightRecipeEditorTypes;
import com.viscript_recipe.data.goety.*;
import com.viscript_recipe.data.iceandfire.DragonForgeRecipeData;
import com.viscript_recipe.data.iceandfire.IceAndFireRecipeEditorTypes;
import com.viscript_recipe.data.irons_spellbooks.IronArcaneAnvilRecipeData;
import com.viscript_recipe.data.irons_spellbooks.IronNoAdditionSmithingRecipeData;
import com.viscript_recipe.data.irons_spellbooks.IronSpellbooksRecipeEditorTypes;
import com.viscript_recipe.data.kaleidoscope_cookery.*;
import com.viscript_recipe.data.spore.SporeGraftingRecipeData;
import com.viscript_recipe.data.spore.SporeRecipeEditorTypes;
import com.viscript_recipe.data.spore.SporeSurgeryRecipeData;
import com.viscript_recipe.data.touhou_little_maid.TouhouLittleMaidAltarRecipeData;
import com.viscript_recipe.data.touhou_little_maid.TouhouLittleMaidRecipeEditorTypes;
import com.viscript_recipe.data.vanilla.*;
import com.viscript_recipe.recipe.importer.RecipeImportResult;
import com.viscript_recipe.recipe.importer.RecipeImporter;
import lombok.Getter;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class RecipeEditorController {
    private static final char[] SHAPED_SYMBOLS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_=+[]{};:,.<>/?|~".toCharArray();
    private static final int CRAFTING_GRID_SLOT_COUNT = 9;
    private static final int MECHANICAL_CRAFTING_GRID_SIZE = 9;
    private static final int MAX_INGREDIENT_SLOTS = MECHANICAL_CRAFTING_GRID_SIZE * MECHANICAL_CRAFTING_GRID_SIZE;
    private static final int CREATE_MAX_ITEM_OUTPUTS = 15;
    private static final int CREATE_MAX_FLUID_INPUTS = 2;
    private static final int CREATE_MAX_FLUID_OUTPUTS = 2;
    private static final int CREATE_FLUID_OUTPUT_INDEX_OFFSET = CREATE_MAX_FLUID_INPUTS;
    private static final int CREATE_SEQUENCED_STEP_INGREDIENT_OFFSET = 10;
    private static final int CREATE_SEQUENCED_MAX_STEPS = 8;
    private static final int CREATE_SEQUENCED_MAX_OUTPUTS = 9;
    private static final int ARS_NOUVEAU_MAX_INPUTS = 9;
    private static final int ARS_NOUVEAU_IMBUEMENT_INPUTS = 4;
    private static final int ARS_NOUVEAU_MAX_CRUSH_OUTPUTS = 6;
    private static final int KALEIDOSCOPE_MAX_INPUTS = 9;
    private static final int KALEIDOSCOPE_CARRIER_SLOT = 9;
    private static final int EXTENDED_CRAFTING_TABLE_GRID_SIZE = 9;
    private static final int EXTENDED_CRAFTING_SMALL_GRID_SIZE = 3;
    private static final int EXTENDED_CRAFTING_COMBINATION_MAX_PEDESTALS = 8;
    private static final int EXTENDED_CRAFTING_COMPRESSOR_MAX_INPUTS = 8;
    private static final List<String> DRAGON_FORGE_DRAGON_TYPES = List.of("fire", "ice", "lightning");
    private static final List<String> ITEM_ABILITY_CHOICES = List.of(
            "knife_dig",
            "axe_dig",
            "axe_strip",
            "shovel_dig",
            "pickaxe_dig",
            "sword_dig",
            "shears_dig"
    );

    @Getter
    private final RecipeProject project;
    private final List<Runnable> listeners = new ArrayList<>();

    @Getter
    @Nullable
    private RecipeEntry selectedEntry;
    @Getter
    private ResourceLocation selectedCategory = RecipeEditorTypes.CRAFTING_TABLE;
    @Getter
    private WorkbenchSlotSelection slotSelection = WorkbenchSlotSelection.RECIPE;

    private ItemStack[] visualIngredients = emptyIngredientStacks();
    private RecipeIngredient[] visualIngredientData = emptyIngredientData();
    private CraftingRemainderRule[] visualRemainders = emptyRemainderData();
    private ItemStack visualResult = ItemStack.EMPTY;
    private ItemStack visualContainer = ItemStack.EMPTY;
    private ItemStack[] visualCuttingResults = emptyCuttingResultStacks();
    private float[] visualCuttingChances = emptyCuttingResultChances();
    private ItemStack[] visualCreateOutputs = emptyCreateOutputStacks();
    private float[] visualCreateOutputChances = emptyCreateOutputChances();
    private CreateFluidIngredientData[] visualCreateFluidInputs = emptyCreateFluidInputs();
    private FluidStack[] visualCreateFluidOutputs = emptyCreateFluidOutputs();
    private ItemStack visualCreateSequencedTransitional = ItemStack.EMPTY;
    private ItemStack[] visualArsNouveauOutputs = emptyArsNouveauOutputStacks();
    private float[] visualArsNouveauOutputChances = emptyArsNouveauOutputChances();
    private int[] visualArsNouveauOutputMaxRanges = emptyArsNouveauOutputMaxRanges();
    private ItemStack[] loadedIngredientStacks = emptyIngredientStacks();
    private CraftingRemainderRule[] loadedRemainders = emptyRemainderData();
    @Getter
    private boolean selectedContainsUnsupportedIngredients;
    @Getter
    private boolean refreshing;

    public RecipeEditorController(RecipeProject project) {
        this.project = project;
        selectedEntry = findFirstAvailableEntry();
        selectedCategory = selectedEntry == null ? firstAvailableCategoryId() : categoryOf(selectedEntry);
        if (selectedEntry == null) {
            addEntry();
        } else {
            loadSelectedEntryToVisualState();
        }
    }

    public void addListener(Runnable listener) {
        listeners.add(listener);
    }

    public void notifyChanged() {
        for (var listener : List.copyOf(listeners)) {
            listener.run();
        }
    }

    public RecipeFile recipeFile() {
        return project.getRecipeFile();
    }

    private ResourceLocation firstAvailableCategoryId() {
        return RecipeEditorTypes.availableCategories().stream()
                .findFirst()
                .map(RecipeEditorCategory::id)
                .orElse(RecipeEditorTypes.CRAFTING_TABLE);
    }

    @Nullable
    private RecipeEntry findFirstAvailableEntry() {
        for (var entry : recipeFile().getEntries()) {
            if (RecipeEditorTypes.get(entry.getType())
                    .filter(RecipeEditorType::isAvailable)
                    .flatMap(type -> RecipeEditorTypes.getCategory(type.category()))
                    .filter(RecipeEditorCategory::isAvailable)
                    .isPresent()) {
                return entry;
            }
        }
        return null;
    }

    @Nullable
    private RecipeEntry findFirstEntryInCategory(ResourceLocation category) {
        for (var entry : recipeFile().getEntries()) {
            if (isEntryInCategory(entry, category)) {
                return entry;
            }
        }
        return null;
    }

    @Nullable
    private RecipeEntry findNearestEntryInCategory(int preferredIndex, ResourceLocation category) {
        var entries = recipeFile().getEntries();
        if (entries.isEmpty()) {
            return null;
        }
        for (int i = Math.min(preferredIndex, entries.size() - 1); i >= 0; i--) {
            var entry = entries.get(i);
            if (isEntryInCategory(entry, category)) {
                return entry;
            }
        }
        for (int i = Math.max(0, preferredIndex); i < entries.size(); i++) {
            var entry = entries.get(i);
            if (isEntryInCategory(entry, category)) {
                return entry;
            }
        }
        return null;
    }

    public boolean isEntryInSelectedCategory(RecipeEntry entry) {
        return isEntryInCategory(entry, selectedCategory);
    }

    private boolean isEntryInCategory(RecipeEntry entry, ResourceLocation category) {
        return RecipeEditorTypes.isInCategory(entry.getType(), category);
    }

    private ResourceLocation categoryOf(RecipeEntry entry) {
        return RecipeEditorTypes.get(entry.getType())
                .map(RecipeEditorType::category)
                .orElse(selectedCategory);
    }

    public void addEntry() {
        saveVisualStateToSelectedEntry();
        var type = RecipeEditorTypes.defaultTypeForCategory(selectedCategory);
        var entry = new RecipeEntry()
                .setType(type)
                .setRecipeId(nextGeneratedRecipeId());
        RecipeDefaultDataInitializer.apply(entry, type);
        recipeFile().getEntries().add(entry);
        selectedEntry = entry;
        slotSelection = WorkbenchSlotSelection.RECIPE;
        loadSelectedEntryToVisualState();
        notifyChanged();
    }

    public void addWorkbenchEntry() {
        setSelectedCategory(RecipeEditorTypes.requireCategory(RecipeEditorTypes.CRAFTING_TABLE));
        addEntry();
    }

    public void addShapedEntry() {
        addWorkbenchEntry();
    }

    public void duplicateEntry(RecipeEntry entry) {
        if (entry == null) {
            return;
        }
        var entries = recipeFile().getEntries();
        var sourceIndex = entries.indexOf(entry);
        if (sourceIndex < 0) {
            return;
        }
        saveVisualStateToSelectedEntry();
        var duplicate = copyEntry(entry)
                .setRecipeId(nextDuplicateRecipeId(entry.getRecipeId()));
        entries.add(sourceIndex + 1, duplicate);
        selectedCategory = categoryOf(duplicate);
        selectedEntry = duplicate;
        slotSelection = WorkbenchSlotSelection.RECIPE;
        loadSelectedEntryToVisualState();
        notifyChanged();
    }

    public RecipeImportResult importRecipe(ResourceLocation recipeId) {
        if (recipeId == null) {
            return RecipeImportResult.failure("viscript_recipe.editor.import_recipe.error.invalid_id");
        }
        if (recipeIdExists(recipeId)) {
            return RecipeImportResult.failure("viscript_recipe.editor.import_recipe.error.duplicate_id");
        }
        saveVisualStateToSelectedEntry();
        var result = RecipeImporter.importRecipe(recipeId);
        if (!result.successful() || result.entry() == null) {
            return result;
        }
        var entry = result.entry();
        var entries = recipeFile().getEntries();
        var insertIndex = selectedEntry == null ? entries.size() : entries.indexOf(selectedEntry) + 1;
        if (insertIndex <= 0 || insertIndex > entries.size()) {
            insertIndex = entries.size();
        }
        entries.add(insertIndex, entry);
        selectedCategory = categoryOf(entry);
        selectedEntry = entry;
        slotSelection = WorkbenchSlotSelection.RECIPE;
        loadSelectedEntryToVisualState();
        notifyChanged();
        return result;
    }

    public void removeEntry(RecipeEntry entry) {
        var entries = recipeFile().getEntries();
        var removedIndex = entries.indexOf(entry);
        entries.remove(entry);
        var nextEntry = findNearestEntryInCategory(removedIndex, selectedCategory);
        if (nextEntry == null) {
            selectedEntry = null;
            slotSelection = WorkbenchSlotSelection.RECIPE;
            clearVisualState();
        } else {
            selectedEntry = nextEntry;
            slotSelection = WorkbenchSlotSelection.RECIPE;
            loadSelectedEntryToVisualState();
        }
        notifyChanged();
    }

    private RecipeEntry copyEntry(RecipeEntry source) {
        var provider = Platform.getFrozenRegistry();
        var copy = new RecipeEntry();
        copy.deserializeNBT(provider, source.serializeNBT(provider).copy());
        return copy;
    }

    private ResourceLocation nextDuplicateRecipeId(@Nullable ResourceLocation sourceId) {
        var sourcePath = sourceId == null ? "recipe" : sourceId.getPath();
        var namespace = recipeFile().getRecipeNamespace();
        var copyPath = sourcePath + "_copy";
        var candidate = new ResourceLocation(namespace, copyPath);
        if (!recipeIdExists(candidate)) {
            return candidate;
        }
        var index = 2;
        while (true) {
            candidate = new ResourceLocation(namespace, copyPath + "_" + index);
            if (!recipeIdExists(candidate)) {
                return candidate;
            }
            index++;
        }
    }

    private ResourceLocation nextGeneratedRecipeId() {
        var namespace = recipeFile().getRecipeNamespace();
        var index = recipeFile().getEntries().size() + 1;
        while (true) {
            var candidate = new ResourceLocation(namespace, "recipe_" + index);
            if (!recipeIdExists(candidate)) {
                return candidate;
            }
            index++;
        }
    }

    private boolean recipeIdExists(ResourceLocation recipeId) {
        for (var entry : recipeFile().getEntries()) {
            if (recipeId.equals(entry.getRecipeId())) {
                return true;
            }
        }
        return false;
    }

    public void reorderSelectedCategoryEntries(List<RecipeEntry> orderedEntries) {
        if (orderedEntries == null || orderedEntries.isEmpty()) {
            return;
        }
        var entries = recipeFile().getEntries();
        var visibleEntries = entries.stream()
                .filter(this::isEntryInSelectedCategory)
                .toList();
        if (visibleEntries.size() != orderedEntries.size() || !containsSameEntries(visibleEntries, orderedEntries)) {
            return;
        }
        if (sameEntryOrder(visibleEntries, orderedEntries)) {
            return;
        }
        saveVisualStateToSelectedEntry();
        var nextIndex = 0;
        for (int i = 0; i < entries.size(); i++) {
            if (isEntryInSelectedCategory(entries.get(i))) {
                entries.set(i, orderedEntries.get(nextIndex++));
            }
        }
        notifyChanged();
    }

    public List<RecipeEditorCategory> availableCategories() {
        return RecipeEditorTypes.availableCategories();
    }

    public List<RecipeEditorType> availableTypesForSelectedCategory() {
        return RecipeEditorTypes.availableInCategory(selectedCategory);
    }

    @Nullable
    public RecipeEditorCategory getSelectedCategoryData() {
        return RecipeEditorTypes.getCategory(selectedCategory).orElse(null);
    }

    public Component selectedCategoryDisplayName() {
        return RecipeEditorTypes.getCategory(selectedCategory)
                .map(RecipeEditorCategory::displayName)
                .orElseGet(() -> Component.translatable("viscript_recipe.editor.category.unknown", selectedCategory.toString()));
    }

    public void setSelectedCategory(RecipeEditorCategory category) {
        if (category == null || !category.isAvailable() || selectedCategory.equals(category.id())) {
            return;
        }
        saveVisualStateToSelectedEntry();
        selectedCategory = category.id();
        selectedEntry = findFirstEntryInCategory(selectedCategory);
        slotSelection = WorkbenchSlotSelection.RECIPE;
        if (selectedEntry == null) {
            clearVisualState();
        } else {
            loadSelectedEntryToVisualState();
        }
        notifyChanged();
    }

    @Nullable
    public RecipeEditorType getSelectedRecipeType() {
        return selectedEntry == null ? null : RecipeEditorTypes.get(selectedEntry.getType()).orElse(null);
    }

    @Nullable
    public RecipeEditorType getSelectedWorkbenchType() {
        return getSelectedRecipeType();
    }

    public Component typeDisplayName(RecipeEntry entry) {
        var typeId = entry.getType();
        return RecipeEditorTypes.get(typeId)
                .map(RecipeEditorType::displayName)
                .orElseGet(() -> Component.translatable("viscript_recipe.editor.type.unknown", typeId.toString()));
    }

    public void setSelectedRecipeType(RecipeEditorType type) {
        if (selectedEntry == null || type == null) {
            return;
        }
        var currentType = AvaritiaRecipeEditorTypes.normalizeAlias(selectedEntry.getType());
        if (currentType.equals(type.id())) {
            if (!selectedEntry.getType().equals(type.id())) {
                selectedEntry.setType(type.id());
                notifyChanged();
            }
            return;
        }
        if (!type.isAvailable() || !type.category().equals(selectedCategory)) {
            return;
        }
        saveVisualStateToSelectedEntry();
        selectedEntry.setType(type.id());
        RecipeDefaultDataInitializer.apply(selectedEntry, type.id());
        slotSelection = WorkbenchSlotSelection.RECIPE;
        loadSelectedEntryToVisualState();
        notifyChanged();
    }

    public void setSelectedWorkbenchType(RecipeEditorType type) {
        setSelectedRecipeType(type);
    }

    public void selectEntry(RecipeEntry entry) {
        if (selectedEntry == entry) {
            selectRecipeProperties();
            return;
        }
        saveVisualStateToSelectedEntry();
        selectedEntry = entry;
        slotSelection = WorkbenchSlotSelection.RECIPE;
        loadSelectedEntryToVisualState();
        notifyChanged();
    }

    public void selectRecipeProperties() {
        slotSelection = WorkbenchSlotSelection.RECIPE;
        notifyChanged();
    }

    private boolean containsSameEntries(List<RecipeEntry> currentEntries, List<RecipeEntry> orderedEntries) {
        var remaining = Collections.newSetFromMap(new IdentityHashMap<RecipeEntry, Boolean>());
        remaining.addAll(currentEntries);
        for (var entry : orderedEntries) {
            if (!remaining.remove(entry)) {
                return false;
            }
        }
        return remaining.isEmpty();
    }

    private boolean sameEntryOrder(List<RecipeEntry> currentEntries, List<RecipeEntry> orderedEntries) {
        for (int i = 0; i < currentEntries.size(); i++) {
            if (currentEntries.get(i) != orderedEntries.get(i)) {
                return false;
            }
        }
        return true;
    }

    public void selectIngredientSlot(int index) {
        if (selectedEntry == null) {
            return;
        }
        if (!isValidIngredientSlotIndex(index)) {
            return;
        }
        if (isSelectedSingleInputLayout() && index != 0) {
            return;
        }
        if (isSelectedSmithingLayout() && index > 2) {
            return;
        }
        if (isSelectedAvaritiaExtremeSmithingLayout() && index > 4) {
            return;
        }
        if (isSelectedAvaritiaCompressorLayout() && index != 0) {
            return;
        }
        if (isSelectedTwoInputSmithingLayout() && index > 1) {
            return;
        }
        if (isSelectedAlchemistCauldronLayout() && index != 0) {
            return;
        }
        if (isSelectedDragonForgeLayout() && index > 1) {
            return;
        }
        if (isSelectedCataclysmWeaponFusionLayout() && index > 1) {
            return;
        }
        if (isSelectedCataclysmAmethystBlessLayout() && index != 0) {
            return;
        }
        if (isSelectedTouhouLittleMaidAltarLayout() && index >= TouhouLittleMaidAltarRecipeData.INPUT_COUNT) {
            return;
        }
        if (isSelectedSporeSurgeryLayout() && index >= SporeSurgeryRecipeData.INPUT_COUNT) {
            return;
        }
        if (isSelectedSporeGraftingLayout() && index >= SporeGraftingRecipeData.INPUT_COUNT) {
            return;
        }
        if (isSelectedFarmersCookingPotLayout() && index > 5) {
            return;
        }
        if (isSelectedFarmersCuttingBoardLayout() && index > 1) {
            return;
        }
        if (isSelectedCreateProcessingLayout() && index >= selectedCreateItemInputCount()) {
            return;
        }
        if (isSelectedCreateSequencedAssemblyLayout() && !isValidCreateSequencedIngredientIndex(index)) {
            return;
        }
        if (isSelectedExtendedCraftingTableLayout() && !isValidExtendedCraftingTableIngredientIndex(index)) {
            return;
        }
        if ((isSelectedArsNouveauApparatusLayout() || isSelectedArsNouveauGlyphLayout() || isSelectedArsNouveauImbuementLayout())
                && index >= selectedArsNouveauInputCount()) {
            return;
        }
        if (isSelectedArsNouveauApparatusLayout() && selectedArsNouveauApparatusHasDerivedPreview() && index == 0) {
            return;
        }
        if (isSelectedArsNouveauCrushLayout() && index != 0) {
            return;
        }
        slotSelection = WorkbenchSlotSelection.ingredient(index);
        notifyChanged();
    }

    private boolean isValidIngredientSlotIndex(int index) {
        if (index < 0 || index >= visualIngredients.length) {
            return false;
        }
        if (isSelectedCreateSequencedAssemblyLayout()) {
            return isValidCreateSequencedIngredientIndex(index);
        }
        if (isSelectedCreateMechanicalCraftingLayout()) {
            var row = index / MECHANICAL_CRAFTING_GRID_SIZE;
            var col = index % MECHANICAL_CRAFTING_GRID_SIZE;
            return row < selectedCreateMechanicalCraftingHeight()
                    && col < selectedCreateMechanicalCraftingWidth();
        }
        if (isSelectedExtendedCraftingTableLayout()) {
            return isValidExtendedCraftingTableIngredientIndex(index);
        }
        if (isSelectedAvaritiaCompressorLayout()) {
            return index == 0;
        }
        if (isSelectedAvaritiaExtremeSmithingLayout()) {
            return index < 5;
        }
        if (isSelectedSporeSurgeryLayout()) {
            return index < SporeSurgeryRecipeData.INPUT_COUNT;
        }
        if (isSelectedTouhouLittleMaidAltarLayout()) {
            return index < TouhouLittleMaidAltarRecipeData.INPUT_COUNT;
        }
        if (isSelectedSporeGraftingLayout()) {
            return index < SporeGraftingRecipeData.INPUT_COUNT;
        }
        if (isSelectedKaleidoscopePotLayout() || isSelectedKaleidoscopeStockpotLayout()) {
            return index < KALEIDOSCOPE_MAX_INPUTS || index == KALEIDOSCOPE_CARRIER_SLOT;
        }
        if (isSelectedKaleidoscopeMillstoneLayout()
                || isSelectedKaleidoscopeChoppingBoardLayout()
                || isSelectedKaleidoscopeSteamerLayout()
                || isSelectedKaleidoscopeTeapotLayout()) {
            return index == 0;
        }
        if (isSelectedArsNouveauApparatusLayout() || isSelectedArsNouveauGlyphLayout() || isSelectedArsNouveauImbuementLayout()) {
            if (isSelectedArsNouveauApparatusLayout() && selectedArsNouveauApparatusHasDerivedPreview() && index == 0) {
                return false;
            }
            return index < selectedArsNouveauInputCount();
        }
        if (isSelectedArsNouveauCrushLayout()) {
            return index == 0;
        }
        return index < CRAFTING_GRID_SLOT_COUNT;
    }

    private boolean isValidExtendedCraftingTableIngredientIndex(int index) {
        if (selectedEntry != null && isExtendedCraftingUltimateSingularityEntry(selectedEntry)) {
            return false;
        }
        var row = index / EXTENDED_CRAFTING_TABLE_GRID_SIZE;
        var col = index % EXTENDED_CRAFTING_TABLE_GRID_SIZE;
        return row < selectedLargeCraftingGridHeight()
                && col < selectedLargeCraftingGridWidth();
    }

    private boolean isValidCreateSequencedIngredientIndex(int index) {
        if (index == 0) {
            return true;
        }
        var stepIndex = createSequencedStepIndexFromIngredientSlot(index);
        return selectedEntry != null && stepIndex >= 0 && stepIndex < createSequencedStepCount(selectedEntry);
    }

    public void selectAlchemistFluidSlot(int index) {
        if (selectedEntry == null || !isIronAlchemistCauldronEntry(selectedEntry)) {
            return;
        }
        if (index < 0 || index > 1) {
            return;
        }
        if (index == 1 && !isIronAlchemistCauldronBrewEntry(selectedEntry)) {
            return;
        }
        slotSelection = WorkbenchSlotSelection.fluid(index);
        notifyChanged();
    }

    public void selectResultSlot() {
        if (selectedEntry == null) {
            return;
        }
        slotSelection = WorkbenchSlotSelection.RESULT;
        notifyChanged();
    }

    public void selectContainerSlot() {
        if (selectedEntry == null || !isFarmersCookingPotEntry(selectedEntry)) {
            return;
        }
        slotSelection = WorkbenchSlotSelection.CONTAINER;
        notifyChanged();
    }

    public void selectCuttingResultSlot(int index) {
        if (selectedEntry == null || !isFarmersCuttingBoardEntry(selectedEntry)) {
            return;
        }
        if (index < 0 || index >= 4) {
            return;
        }
        slotSelection = WorkbenchSlotSelection.cuttingResult(index);
        notifyChanged();
    }

    public void selectCreateOutputSlot(int index) {
        if (selectedEntry == null || (!isCreateProcessingEntry(selectedEntry) && !isCreateSequencedAssemblyEntry(selectedEntry))) {
            return;
        }
        var maxOutputs = isCreateSequencedAssemblyEntry(selectedEntry)
                ? CREATE_SEQUENCED_MAX_OUTPUTS
                : selectedCreateKind().map(CreateProcessingKind::maxItemOutputs).orElse(0);
        if (index < 0 || index >= Math.min(CREATE_MAX_ITEM_OUTPUTS, maxOutputs)) {
            return;
        }
        slotSelection = WorkbenchSlotSelection.createResult(index);
        notifyChanged();
    }

    public void selectArsNouveauOutputSlot(int index) {
        if (selectedEntry == null || !isArsNouveauCrushEntry(selectedEntry)) {
            return;
        }
        if (index < 0 || index >= ARS_NOUVEAU_MAX_CRUSH_OUTPUTS) {
            return;
        }
        slotSelection = WorkbenchSlotSelection.arsNouveauOutput(index);
        notifyChanged();
    }

    public void selectCreateFluidInputSlot(int index) {
        if (selectedEntry == null || (!isCreateProcessingEntry(selectedEntry) && !isCreateSequencedAssemblyEntry(selectedEntry))) {
            return;
        }
        if (isCreateSequencedAssemblyEntry(selectedEntry)) {
            if (index < 0 || index >= createSequencedStepCount(selectedEntry)) {
                return;
            }
            slotSelection = WorkbenchSlotSelection.fluid(index);
            notifyChanged();
            return;
        }
        var maxInputs = selectedCreateKind().map(CreateProcessingKind::maxFluidInputs).orElse(0);
        if (index < 0 || index >= Math.min(CREATE_MAX_FLUID_INPUTS, maxInputs)) {
            return;
        }
        slotSelection = WorkbenchSlotSelection.fluid(index);
        notifyChanged();
    }

    public void selectCreateFluidOutputSlot(int index) {
        if (selectedEntry == null || !isCreateProcessingEntry(selectedEntry)) {
            return;
        }
        var maxOutputs = selectedCreateKind().map(CreateProcessingKind::maxFluidOutputs).orElse(0);
        if (index < 0 || index >= Math.min(CREATE_MAX_FLUID_OUTPUTS, maxOutputs)) {
            return;
        }
        slotSelection = WorkbenchSlotSelection.fluid(CREATE_FLUID_OUTPUT_INDEX_OFFSET + index);
        notifyChanged();
    }

    public void selectCreateSequencedTransitionalSlot() {
        if (selectedEntry == null || !isCreateSequencedAssemblyEntry(selectedEntry)) {
            return;
        }
        slotSelection = WorkbenchSlotSelection.createTransitional();
        notifyChanged();
    }

    public void selectCreateSequencedStep(int index) {
        if (selectedEntry == null || !isCreateSequencedAssemblyEntry(selectedEntry)) {
            return;
        }
        if (index < 0 || index >= createSequencedStepCount(selectedEntry)) {
            return;
        }
        slotSelection = WorkbenchSlotSelection.createSequencedStep(index);
        notifyChanged();
    }

    public ItemStack getVisualIngredient(int index) {
        return visualIngredients[index].copy();
    }

    public RecipeIngredient snapshotVisualIngredient(int index) {
        if (index < 0 || index >= visualIngredientData.length) {
            return new RecipeIngredient();
        }
        return copyIngredient(visualIngredientData[index]);
    }

    public ItemStack[] getVisualIngredientTagStacks(int index) {
        if (index < 0 || index >= visualIngredientData.length) {
            return new ItemStack[0];
        }
        var ingredient = visualIngredientData[index];
        if (ingredient == null || ingredient.getValues().isEmpty()) {
            return new ItemStack[0];
        }
        if (ingredient.getValues().size() == 1) {
            var value = ingredient.getValues().get(0);
            return switch (value.getKind()) {
                case ITEM -> value.getItem() == null || value.getItem().isEmpty()
                        ? new ItemStack[0]
                        : new ItemStack[]{isCreateCountedItemInputSlot(index) ? value.getItem().copy() : value.getItem().copyWithCount(1)};
                case TAG -> value.getTag() == null ? new ItemStack[0] : itemsFromTag(value.getTag());
                case ITEM_ABILITY -> value.getItemAbility() == null || value.getItemAbility().isBlank()
                        ? new ItemStack[0]
                        : new ItemStack[]{itemFromAbility(value.getItemAbility())};
            };
        }
        var stacks = new ArrayList<ItemStack>();
        for (var value : ingredient.getValues()) {
            if (value == null) {
                continue;
            }
            switch (value.getKind()) {
                case ITEM -> {
                    if (value.getItem() != null && !value.getItem().isEmpty()) {
                        stacks.add(value.getItem().copyWithCount(1));
                    }
                }
                case TAG -> {
                    if (value.getTag() != null) {
                        stacks.addAll(List.of(itemsFromTag(value.getTag())));
                    }
                }
                case ITEM_ABILITY -> {
                    if (value.getItemAbility() != null && !value.getItemAbility().isBlank()) {
                        stacks.add(itemFromAbility(value.getItemAbility()));
                    }
                }
            }
        }
        return stacks.toArray(ItemStack[]::new);
    }

    @Nullable
    public ResourceLocation getVisualIngredientTag(int index) {
        if (index < 0 || index >= visualIngredientData.length) {
            return null;
        }
        var ingredient = visualIngredientData[index];
        if (ingredient == null || ingredient.getValues().size() != 1) {
            return null;
        }
        var value = ingredient.getValues().get(0);
        return value.getKind() == IngredientValueKind.TAG ? value.getTag() : null;
    }

    @Nullable
    public String getVisualIngredientItemAbility(int index) {
        if (index < 0 || index >= visualIngredientData.length) {
            return null;
        }
        var ingredient = visualIngredientData[index];
        if (ingredient == null || ingredient.getValues().size() != 1) {
            return null;
        }
        var value = ingredient.getValues().get(0);
        return value.getKind() == IngredientValueKind.ITEM_ABILITY ? value.getItemAbility() : null;
    }

    public void setVisualIngredient(int index, ItemStack stack) {
        if (selectedEntry == null) {
            return;
        }
        if (index < 0 || index >= visualIngredients.length) {
            return;
        }
        if (isCreateAutoPackingEntry(selectedEntry)) {
            setCreateAutoPackingIngredient(selectedEntry, RecipeIngredient.item(normalizeStack(stack)));
            notifyChanged();
            return;
        }
        visualIngredients[index] = normalizeVisualIngredientStack(index, stack);
        visualIngredientData[index] = visualIngredients[index].isEmpty()
                ? new RecipeIngredient()
                : ingredientForVisualItemStack(index, visualIngredients[index]);
        if (visualIngredients[index].isEmpty()) {
            visualRemainders[index] = CraftingRemainderRule.defaultRule();
        }
        saveVisualStateToSelectedEntry();
        notifyChanged();
    }

    public void clearVisualIngredient(int index) {
        setVisualIngredient(index, ItemStack.EMPTY);
    }

    public void setVisualIngredientFromDrag(int index, RecipeIngredient ingredient) {
        if (selectedEntry == null || index < 0 || index >= visualIngredientData.length) {
            return;
        }
        setIngredientForSlot(selectedEntry, index, copyIngredient(ingredient));
        if (preservesIngredientSlotPositionsOnEdit(selectedEntry)) {
            refreshUnsupportedIngredientStatus();
        } else {
            refreshVisualStateFromData();
        }
        notifyChanged();
    }

    public ItemStack getVisualResult() {
        return visualResult.copy();
    }

    public void setVisualResult(ItemStack stack) {
        if (selectedEntry == null) {
            return;
        }
        visualResult = stack == null ? ItemStack.EMPTY : stack.copy();
        saveVisualStateToSelectedEntry();
        notifyChanged();
    }

    public void clearVisualResult() {
        setVisualResult(ItemStack.EMPTY);
    }

    public ItemStack getVisualContainer() {
        return visualContainer.copy();
    }

    public void setVisualContainer(ItemStack stack) {
        if (selectedEntry == null || !isFarmersCookingPotEntry(selectedEntry)) {
            return;
        }
        visualContainer = stack == null ? ItemStack.EMPTY : stack.copy();
        saveVisualStateToSelectedEntry();
        notifyChanged();
    }

    public void clearVisualContainer() {
        setVisualContainer(ItemStack.EMPTY);
    }

    public ItemStack getVisualCuttingResult(int index) {
        if (index < 0 || index >= visualCuttingResults.length) {
            return ItemStack.EMPTY;
        }
        return visualCuttingResults[index].copy();
    }

    public float getVisualCuttingChance(int index) {
        if (index < 0 || index >= visualCuttingChances.length) {
            return 1.0F;
        }
        return Math.max(0, Math.min(1, visualCuttingChances[index]));
    }

    public void setVisualCuttingResult(int index, ItemStack stack) {
        if (selectedEntry == null || !isFarmersCuttingBoardEntry(selectedEntry) || index < 0 || index >= visualCuttingResults.length) {
            return;
        }
        visualCuttingResults[index] = stack == null ? ItemStack.EMPTY : stack.copy();
        if (!visualCuttingResults[index].isEmpty() && visualCuttingChances[index] <= 0) {
            visualCuttingChances[index] = 1.0F;
        }
        saveVisualStateToSelectedEntry();
        notifyChanged();
    }

    public void clearVisualCuttingResult(int index) {
        setVisualCuttingResult(index, ItemStack.EMPTY);
    }

    public ItemStack getVisualCreateOutput(int index) {
        if (index < 0 || index >= visualCreateOutputs.length) {
            return ItemStack.EMPTY;
        }
        return visualCreateOutputs[index].copy();
    }

    public void setVisualCreateOutput(int index, ItemStack stack) {
        if (selectedEntry == null || (!isCreateProcessingEntry(selectedEntry) && !isCreateSequencedAssemblyEntry(selectedEntry)) || index < 0 || index >= visualCreateOutputs.length) {
            return;
        }
        visualCreateOutputs[index] = stack == null ? ItemStack.EMPTY : stack.copy();
        if (!visualCreateOutputs[index].isEmpty() && visualCreateOutputChances[index] <= 0) {
            visualCreateOutputChances[index] = 1.0F;
        }
        saveVisualStateToSelectedEntry();
        notifyChanged();
    }

    public void clearVisualCreateOutput(int index) {
        setVisualCreateOutput(index, ItemStack.EMPTY);
    }

    public ItemStack getVisualArsNouveauOutput(int index) {
        if (index < 0 || index >= visualArsNouveauOutputs.length) {
            return ItemStack.EMPTY;
        }
        return visualArsNouveauOutputs[index].copy();
    }

    public void setVisualArsNouveauOutput(int index, ItemStack stack) {
        if (selectedEntry == null || !isArsNouveauCrushEntry(selectedEntry) || index < 0 || index >= visualArsNouveauOutputs.length) {
            return;
        }
        visualArsNouveauOutputs[index] = stack == null ? ItemStack.EMPTY : stack.copy();
        if (!visualArsNouveauOutputs[index].isEmpty()) {
            if (visualArsNouveauOutputChances[index] <= 0) {
                visualArsNouveauOutputChances[index] = 1.0F;
            }
            if (visualArsNouveauOutputMaxRanges[index] <= 0) {
                visualArsNouveauOutputMaxRanges[index] = 1;
            }
        }
        saveVisualStateToSelectedEntry();
        notifyChanged();
    }

    public void clearVisualArsNouveauOutput(int index) {
        setVisualArsNouveauOutput(index, ItemStack.EMPTY);
    }

    public CreateFluidIngredientData getVisualCreateFluidInput(int index) {
        if (index < 0 || index >= visualCreateFluidInputs.length) {
            return CreateFluidIngredientData.empty();
        }
        var input = visualCreateFluidInputs[index];
        return input == null ? CreateFluidIngredientData.empty() : input.copy();
    }

    public FluidStack getVisualCreateFluidInputDisplay(int index) {
        var input = getVisualCreateFluidInput(index);
        if (input.getKind() == CreateFluidIngredientKind.TAG) {
            var stacks = fluidsFromTag(input.getTag(), input.getAmount());
            return stacks.length == 0 ? FluidStack.EMPTY : stacks[0].copy();
        }
        return input.getFluid() == null ? FluidStack.EMPTY : input.getFluid().copy();
    }

    public FluidStack[] getVisualCreateFluidInputTagStacks(int index) {
        var input = getVisualCreateFluidInput(index);
        return input.getKind() == CreateFluidIngredientKind.TAG
                ? fluidsFromTag(input.getTag(), input.getAmount())
                : new FluidStack[0];
    }

    @Nullable
    public ResourceLocation getVisualCreateFluidInputTag(int index) {
        var input = getVisualCreateFluidInput(index);
        return input.getKind() == CreateFluidIngredientKind.TAG ? input.getTag() : null;
    }

    public void setVisualCreateFluidInput(int index, CreateFluidIngredientData input) {
        if (selectedEntry != null && isCreateSequencedAssemblyEntry(selectedEntry)) {
            setCreateSequencedStepFluidIngredient(index, input);
            return;
        }
        if (selectedEntry == null || !isCreateProcessingEntry(selectedEntry) || index < 0 || index >= visualCreateFluidInputs.length) {
            return;
        }
        visualCreateFluidInputs[index] = input == null ? CreateFluidIngredientData.empty() : input.copy();
        saveVisualStateToSelectedEntry();
        notifyChanged();
    }

    public void clearVisualCreateFluidInput(int index) {
        setVisualCreateFluidInput(index, CreateFluidIngredientData.fluid(FluidStack.EMPTY));
    }

    public FluidStack getVisualCreateFluidOutput(int index) {
        if (index < 0 || index >= visualCreateFluidOutputs.length) {
            return FluidStack.EMPTY;
        }
        return visualCreateFluidOutputs[index] == null ? FluidStack.EMPTY : visualCreateFluidOutputs[index].copy();
    }

    public void setVisualCreateFluidOutput(int index, FluidStack stack) {
        if (selectedEntry == null || !isCreateProcessingEntry(selectedEntry) || index < 0 || index >= visualCreateFluidOutputs.length) {
            return;
        }
        visualCreateFluidOutputs[index] = stack == null ? FluidStack.EMPTY : stack.copy();
        saveVisualStateToSelectedEntry();
        notifyChanged();
    }

    public void clearVisualCreateFluidOutput(int index) {
        setVisualCreateFluidOutput(index, FluidStack.EMPTY);
    }

    public RecipeIngredient getSelectedIngredient() {
        if (selectedEntry == null || slotSelection.kind() != WorkbenchSlotSelection.Kind.INGREDIENT) {
            return new RecipeIngredient();
        }
        var ingredient = visualIngredientData[slotSelection.index()];
        return ingredient == null ? getIngredientForSlot(selectedEntry, slotSelection.index()) : ingredient;
    }

    public CraftingRemainderRule getSelectedRemainder() {
        if (selectedEntry == null || !selectedEntry.isType(RecipeEditorTypes.CRAFTING_SHAPED) || slotSelection.kind() != WorkbenchSlotSelection.Kind.INGREDIENT) {
            return CraftingRemainderRule.defaultRule();
        }
        var remainder = visualRemainders[slotSelection.index()];
        return remainder == null ? CraftingRemainderRule.defaultRule() : remainder.copy();
    }

    public void setSelectedRemainder(CraftingRemainderRule remainder) {
        if (selectedEntry == null || !selectedEntry.isType(RecipeEditorTypes.CRAFTING_SHAPED) || slotSelection.kind() != WorkbenchSlotSelection.Kind.INGREDIENT) {
            return;
        }
        visualRemainders[slotSelection.index()] = remainder == null ? CraftingRemainderRule.defaultRule() : remainder.copy();
        saveVisualStateToSelectedEntry();
        notifyChanged();
    }

    public void setSelectedIngredient(RecipeIngredient ingredient) {
        if (selectedEntry == null || slotSelection.kind() != WorkbenchSlotSelection.Kind.INGREDIENT) {
            return;
        }
        setIngredientForSlot(selectedEntry, slotSelection.index(), ingredient);
        if (preservesIngredientSlotPositionsOnEdit(selectedEntry)) {
            refreshUnsupportedIngredientStatus();
        } else {
            refreshVisualStateFromData();
        }
        notifyChanged();
    }

    private boolean preservesIngredientSlotPositionsOnEdit(RecipeEntry entry) {
        return entry.isType(RecipeEditorTypes.CRAFTING_SHAPELESS)
                || isExtendedCraftingShapelessTableEntry(entry)
                || (isExtendedCraftingEnderCrafterEntry(entry) && !isExtendedCraftingShapedEnderCrafterEntry(entry))
                || (isExtendedCraftingFluxCrafterEntry(entry) && !isExtendedCraftingShapedFluxCrafterEntry(entry))
                || isAvaritiaShapelessTableEntry(entry);
    }

    public ItemStack getSelectedResult() {
        if (selectedEntry == null) {
            return ItemStack.EMPTY;
        }
        return getResult(selectedEntry).copy();
    }

    public void setSelectedResult(ItemStack stack) {
        if (selectedEntry == null) {
            return;
        }
        setResult(selectedEntry, stack == null ? ItemStack.EMPTY : stack.copy());
        visualResult = getResult(selectedEntry).copy();
        notifyChanged();
    }

    public boolean showNotification(RecipeEntry entry) {
        return RecipeEditorTypes.require(entry.getType()).showNotification(entry);
    }

    public boolean supportsNotification(RecipeEntry entry) {
        return RecipeEditorTypes.require(entry.getType()).supportsNotification();
    }

    public void setShowNotification(RecipeEntry entry, boolean value) {
        var type = RecipeEditorTypes.require(entry.getType());
        if (!type.supportsNotification()) {
            return;
        }
        type.setShowNotification(entry, value);
        notifyChanged();
    }

    public ItemStack getResult(RecipeEntry entry) {
        return RecipeEditorTypes.require(entry.getType()).result(entry);
    }

    public void setResult(RecipeEntry entry, ItemStack result) {
        RecipeEditorTypes.require(entry.getType()).setResult(entry, result);
    }

    public FluidStack getSelectedFluid() {
        if (selectedEntry == null) {
            return FluidStack.EMPTY;
        }
        if (isSelectedCreateFluidInput() && isCreateSequencedAssemblyEntry(selectedEntry)) {
            return getCreateSequencedStepFluidIngredient(slotSelection.index()).getFluid();
        }
        if (slotSelection.kind() != WorkbenchSlotSelection.Kind.FLUID) {
            return FluidStack.EMPTY;
        }
        if (isCreateProcessingEntry(selectedEntry)) {
            if (isSelectedCreateFluidOutput()) {
                return getVisualCreateFluidOutput(selectedCreateFluidOutputIndex());
            }
            return getVisualCreateFluidInput(selectedCreateFluidInputIndex()).getFluid();
        }
        if (!isIronAlchemistCauldronEntry(selectedEntry)) {
            return FluidStack.EMPTY;
        }
        return getAlchemistFluid(selectedEntry, slotSelection.index());
    }

    public void setSelectedFluid(FluidStack stack) {
        if (selectedEntry == null) {
            return;
        }
        if (isSelectedCreateFluidInput() && isCreateSequencedAssemblyEntry(selectedEntry)) {
            var input = getCreateSequencedStepFluidIngredient(slotSelection.index());
            var fluid = stack == null ? FluidStack.EMPTY : stack.copy();
            input.setKind(CreateFluidIngredientKind.FLUID);
            input.setFluid(fluid);
            input.setAmount(Math.max(1, fluid.getAmount()));
            setCreateSequencedStepFluidIngredient(slotSelection.index(), input);
            return;
        }
        if (slotSelection.kind() != WorkbenchSlotSelection.Kind.FLUID) {
            return;
        }
        if (isCreateProcessingEntry(selectedEntry)) {
            if (isSelectedCreateFluidOutput()) {
                setVisualCreateFluidOutput(selectedCreateFluidOutputIndex(), stack);
            } else {
                var input = getVisualCreateFluidInput(selectedCreateFluidInputIndex());
                var fluid = stack == null ? FluidStack.EMPTY : stack.copy();
                input.setKind(CreateFluidIngredientKind.FLUID);
                input.setFluid(fluid);
                input.setAmount(Math.max(1, fluid.getAmount()));
                setVisualCreateFluidInput(selectedCreateFluidInputIndex(), input);
            }
            return;
        }
        if (!isIronAlchemistCauldronEntry(selectedEntry)) {
            return;
        }
        setAlchemistFluid(selectedEntry, slotSelection.index(), stack);
        notifyChanged();
    }

    public String selectedFluidConfigNameKey() {
        if (selectedEntry != null && isCreateSequencedAssemblyEntry(selectedEntry)) {
            return "viscript_recipe.config.create.sequenced_assembly.step.fluid_ingredient";
        }
        if (selectedEntry != null && isCreateProcessingEntry(selectedEntry)) {
            return isSelectedCreateFluidOutput()
                    ? "viscript_recipe.config.create.fluid_output"
                    : "viscript_recipe.config.create.fluid_ingredient.fluid";
        }
        if (selectedEntry == null || !isIronAlchemistCauldronEntry(selectedEntry)) {
            return "viscript_recipe.config.irons_spellbooks.alchemist_cauldron.fluid";
        }
        return alchemistFluidConfigNameKey(selectedEntry, slotSelection.index());
    }

    public void saveProject() {
        saveVisualStateToSelectedEntry();
        if (project.getEditor() != null) {
            project.getEditor().saveProject(this::notifyChanged);
        }
    }

    public void refreshVisualStateFromData() {
        loadSelectedEntryToVisualState();
    }

    public void saveVisualState() {
        saveVisualStateToSelectedEntry();
    }

    public boolean isSelectedCookingLayout() {
        if (selectedEntry != null) {
            return RecipeEditorTypes.layoutForType(selectedEntry.getType()) == RecipeEditorLayout.COOKING;
        }
        return RecipeEditorTypes.getCategory(selectedCategory)
                .map(RecipeEditorCategory::layout)
                .orElse(RecipeEditorLayout.CRAFTING_GRID) == RecipeEditorLayout.COOKING;
    }

    public boolean isSelectedSingleInputLayout() {
        if (selectedEntry != null) {
            var layout = RecipeEditorTypes.layoutForType(selectedEntry.getType());
            return layout == RecipeEditorLayout.COOKING || layout == RecipeEditorLayout.SINGLE_INPUT;
        }
        return RecipeEditorTypes.getCategory(selectedCategory)
                .map(RecipeEditorCategory::layout)
                .map(layout -> layout == RecipeEditorLayout.COOKING || layout == RecipeEditorLayout.SINGLE_INPUT)
                .orElse(false);
    }

    public boolean isSelectedSmithingLayout() {
        if (selectedEntry != null) {
            return RecipeEditorTypes.layoutForType(selectedEntry.getType()) == RecipeEditorLayout.SMITHING;
        }
        return RecipeEditorTypes.getCategory(selectedCategory)
                .map(RecipeEditorCategory::layout)
                .orElse(RecipeEditorLayout.CRAFTING_GRID) == RecipeEditorLayout.SMITHING;
    }

    public boolean isSelectedNoAdditionSmithingLayout() {
        return selectedEntry != null && isIronNoAdditionSmithingEntry(selectedEntry);
    }

    public boolean isSelectedArcaneAnvilLayout() {
        if (selectedEntry != null) {
            return isIronArcaneAnvilEntry(selectedEntry);
        }
        return selectedCategory.equals(IronSpellbooksRecipeEditorTypes.ARCANE_ANVIL);
    }

    public boolean isSelectedTwoInputSmithingLayout() {
        return isSelectedArcaneAnvilLayout() || isSelectedNoAdditionSmithingLayout();
    }

    public int selectedSmithingInputCount() {
        return isSelectedTwoInputSmithingLayout() ? 2 : 3;
    }

    public boolean isSelectedAlchemistCauldronLayout() {
        if (selectedEntry != null) {
            return RecipeEditorTypes.layoutForType(selectedEntry.getType()) == RecipeEditorLayout.ALCHEMIST_CAULDRON;
        }
        return RecipeEditorTypes.getCategory(selectedCategory)
                .map(RecipeEditorCategory::layout)
                .orElse(RecipeEditorLayout.CRAFTING_GRID) == RecipeEditorLayout.ALCHEMIST_CAULDRON;
    }

    public boolean isSelectedDragonForgeLayout() {
        if (selectedEntry != null) {
            return RecipeEditorTypes.layoutForType(selectedEntry.getType()) == RecipeEditorLayout.DRAGON_FORGE;
        }
        return RecipeEditorTypes.getCategory(selectedCategory)
                .map(RecipeEditorCategory::layout)
                .orElse(RecipeEditorLayout.CRAFTING_GRID) == RecipeEditorLayout.DRAGON_FORGE;
    }

    public boolean isSelectedCataclysmWeaponFusionLayout() {
        return isSelectedLayout(RecipeEditorLayout.CATACLYSM_WEAPON_FUSION);
    }

    public boolean isSelectedCataclysmAmethystBlessLayout() {
        return isSelectedLayout(RecipeEditorLayout.CATACLYSM_AMETHYST_BLESS);
    }

    public boolean isSelectedTouhouLittleMaidAltarLayout() {
        return isSelectedLayout(RecipeEditorLayout.TOUHOU_LITTLE_MAID_ALTAR);
    }

    public boolean isSelectedSporeSurgeryLayout() {
        if (selectedEntry != null) {
            return isSporeSurgeryEntry(selectedEntry);
        }
        return selectedCategory.equals(SporeRecipeEditorTypes.SURGERY_TABLE);
    }

    public boolean isSelectedSporeGraftingLayout() {
        return selectedEntry != null && isSporeGraftingEntry(selectedEntry);
    }

    public boolean isSelectedGoetyCursedInfuserLayout() {
        return isSelectedLayout(RecipeEditorLayout.GOETY_CURSED_INFUSER);
    }

    public boolean isSelectedGoetyRitualLayout() {
        return isSelectedLayout(RecipeEditorLayout.GOETY_RITUAL);
    }

    public boolean isSelectedGoetyBrazierLayout() {
        return isSelectedLayout(RecipeEditorLayout.GOETY_BRAZIER);
    }

    public boolean isSelectedGoetyPulverizeLayout() {
        return isSelectedLayout(RecipeEditorLayout.GOETY_PULVERIZE);
    }

    public boolean isSelectedGoetyBrewingLayout() {
        return isSelectedLayout(RecipeEditorLayout.GOETY_BREWING);
    }

    public boolean isSelectedFarmersCookingPotLayout() {
        if (selectedEntry != null) {
            return RecipeEditorTypes.layoutForType(selectedEntry.getType()) == RecipeEditorLayout.FARMERS_COOKING_POT;
        }
        return RecipeEditorTypes.getCategory(selectedCategory)
                .map(RecipeEditorCategory::layout)
                .orElse(RecipeEditorLayout.CRAFTING_GRID) == RecipeEditorLayout.FARMERS_COOKING_POT;
    }

    public boolean isSelectedFarmersCuttingBoardLayout() {
        if (selectedEntry != null) {
            return RecipeEditorTypes.layoutForType(selectedEntry.getType()) == RecipeEditorLayout.FARMERS_CUTTING_BOARD;
        }
        return RecipeEditorTypes.getCategory(selectedCategory)
                .map(RecipeEditorCategory::layout)
                .orElse(RecipeEditorLayout.CRAFTING_GRID) == RecipeEditorLayout.FARMERS_CUTTING_BOARD;
    }

    public boolean isSelectedCreateProcessingLayout() {
        if (selectedEntry != null) {
            return RecipeEditorTypes.layoutForType(selectedEntry.getType()) == RecipeEditorLayout.CREATE_PROCESSING;
        }
        return RecipeEditorTypes.getCategory(selectedCategory)
                .map(RecipeEditorCategory::layout)
                .orElse(RecipeEditorLayout.CRAFTING_GRID) == RecipeEditorLayout.CREATE_PROCESSING;
    }

    public boolean isSelectedCreateMechanicalCraftingLayout() {
        if (selectedEntry != null) {
            return RecipeEditorTypes.layoutForType(selectedEntry.getType()) == RecipeEditorLayout.CREATE_MECHANICAL_CRAFTING;
        }
        return RecipeEditorTypes.getCategory(selectedCategory)
                .map(RecipeEditorCategory::layout)
                .orElse(RecipeEditorLayout.CRAFTING_GRID) == RecipeEditorLayout.CREATE_MECHANICAL_CRAFTING;
    }

    public boolean isSelectedCreateSequencedAssemblyLayout() {
        if (selectedEntry != null) {
            return RecipeEditorTypes.layoutForType(selectedEntry.getType()) == RecipeEditorLayout.CREATE_SEQUENCED_ASSEMBLY;
        }
        return RecipeEditorTypes.getCategory(selectedCategory)
                .map(RecipeEditorCategory::layout)
                .orElse(RecipeEditorLayout.CRAFTING_GRID) == RecipeEditorLayout.CREATE_SEQUENCED_ASSEMBLY;
    }

    public boolean isSelectedExtendedCraftingTableLayout() {
        return isSelectedLayout(RecipeEditorLayout.EXTENDED_CRAFTING_TABLE);
    }

    public boolean isSelectedLargeCraftingGridLayout() {
        return isSelectedCreateMechanicalCraftingLayout() || isSelectedExtendedCraftingTableLayout();
    }

    public boolean isSelectedArsNouveauApparatusLayout() {
        if (selectedEntry != null) {
            return RecipeEditorTypes.layoutForType(selectedEntry.getType()) == RecipeEditorLayout.ARS_NOUVEAU_APPARATUS;
        }
        return RecipeEditorTypes.getCategory(selectedCategory)
                .map(RecipeEditorCategory::layout)
                .orElse(RecipeEditorLayout.CRAFTING_GRID) == RecipeEditorLayout.ARS_NOUVEAU_APPARATUS;
    }

    public boolean isSelectedArsNouveauGlyphLayout() {
        if (selectedEntry != null) {
            return RecipeEditorTypes.layoutForType(selectedEntry.getType()) == RecipeEditorLayout.ARS_NOUVEAU_GLYPH;
        }
        return RecipeEditorTypes.getCategory(selectedCategory)
                .map(RecipeEditorCategory::layout)
                .orElse(RecipeEditorLayout.CRAFTING_GRID) == RecipeEditorLayout.ARS_NOUVEAU_GLYPH;
    }

    public boolean isSelectedArsNouveauImbuementLayout() {
        if (selectedEntry != null) {
            return RecipeEditorTypes.layoutForType(selectedEntry.getType()) == RecipeEditorLayout.ARS_NOUVEAU_IMBUEMENT;
        }
        return RecipeEditorTypes.getCategory(selectedCategory)
                .map(RecipeEditorCategory::layout)
                .orElse(RecipeEditorLayout.CRAFTING_GRID) == RecipeEditorLayout.ARS_NOUVEAU_IMBUEMENT;
    }

    public boolean isSelectedArsNouveauCrushLayout() {
        if (selectedEntry != null) {
            return RecipeEditorTypes.layoutForType(selectedEntry.getType()) == RecipeEditorLayout.ARS_NOUVEAU_CRUSH;
        }
        return RecipeEditorTypes.getCategory(selectedCategory)
                .map(RecipeEditorCategory::layout)
                .orElse(RecipeEditorLayout.CRAFTING_GRID) == RecipeEditorLayout.ARS_NOUVEAU_CRUSH;
    }

    public boolean isSelectedKaleidoscopePotLayout() {
        return isSelectedLayout(RecipeEditorLayout.KALEIDOSCOPE_POT);
    }

    public boolean isSelectedKaleidoscopeStockpotLayout() {
        return isSelectedLayout(RecipeEditorLayout.KALEIDOSCOPE_STOCKPOT);
    }

    public boolean isSelectedKaleidoscopeMillstoneLayout() {
        return isSelectedLayout(RecipeEditorLayout.KALEIDOSCOPE_MILLSTONE);
    }

    public boolean isSelectedKaleidoscopeChoppingBoardLayout() {
        return isSelectedLayout(RecipeEditorLayout.KALEIDOSCOPE_CHOPPING_BOARD);
    }

    public boolean isSelectedKaleidoscopeSteamerLayout() {
        return isSelectedLayout(RecipeEditorLayout.KALEIDOSCOPE_STEAMER);
    }

    public boolean isSelectedKaleidoscopeTeapotLayout() {
        return isSelectedLayout(RecipeEditorLayout.KALEIDOSCOPE_TEAPOT);
    }

    public boolean isSelectedAvaritiaCompressorLayout() {
        return isSelectedLayout(RecipeEditorLayout.AVARITIA_COMPRESSOR);
    }

    public boolean isSelectedAvaritiaExtremeSmithingLayout() {
        return isSelectedLayout(RecipeEditorLayout.AVARITIA_EXTREME_SMITHING);
    }

    public boolean isSelectedExtendedCraftingCombinationLayout() {
        if (selectedEntry != null) {
            return isExtendedCraftingCombinationEntry(selectedEntry);
        }
        return ExtendedCraftingRecipeEditorTypes.CRAFTING_CORE.equals(selectedCategory);
    }

    public boolean isSelectedExtendedCraftingCompressorLayout() {
        if (selectedEntry != null) {
            return isExtendedCraftingCompressorEntry(selectedEntry);
        }
        return ExtendedCraftingRecipeEditorTypes.COMPRESSOR.equals(selectedCategory);
    }

    public boolean isSelectedExtendedCraftingFluxCrafterLayout() {
        if (selectedEntry != null) {
            return isExtendedCraftingFluxCrafterEntry(selectedEntry);
        }
        return ExtendedCraftingRecipeEditorTypes.FLUX_CRAFTER.equals(selectedCategory);
    }

    private boolean isSelectedLayout(RecipeEditorLayout layout) {
        if (selectedEntry != null) {
            return RecipeEditorTypes.layoutForType(selectedEntry.getType()) == layout;
        }
        return RecipeEditorTypes.getCategory(selectedCategory)
                .map(RecipeEditorCategory::layout)
                .orElse(RecipeEditorLayout.CRAFTING_GRID) == layout;
    }

    public boolean isSelectedCraftingGridLayout() {
        if (selectedEntry != null) {
            return RecipeEditorTypes.layoutForType(selectedEntry.getType()) == RecipeEditorLayout.CRAFTING_GRID;
        }
        return RecipeEditorTypes.getCategory(selectedCategory)
                .map(RecipeEditorCategory::layout)
                .orElse(RecipeEditorLayout.CRAFTING_GRID) == RecipeEditorLayout.CRAFTING_GRID;
    }

    public boolean isCookingEntry(RecipeEntry entry) {
        return RecipeEditorTypes.layoutForType(entry.getType()) == RecipeEditorLayout.COOKING;
    }

    public boolean isStonecuttingEntry(RecipeEntry entry) {
        return entry.isType(RecipeEditorTypes.STONECUTTING);
    }

    public boolean isSmithingTransformEntry(RecipeEntry entry) {
        return entry.isType(RecipeEditorTypes.SMITHING_TRANSFORM);
    }

    public boolean isIronNoAdditionSmithingEntry(RecipeEntry entry) {
        return entry.isType(IronSpellbooksRecipeEditorTypes.SMITHING_TRANSFORM_NO_ADDITION);
    }

    public boolean isIronArcaneAnvilEntry(RecipeEntry entry) {
        return entry.isType(IronSpellbooksRecipeEditorTypes.ARCANE_ANVIL_TRANSFORM);
    }

    public boolean isIronAlchemistCauldronEntry(RecipeEntry entry) {
        return RecipeEditorTypes.layoutForType(entry.getType()) == RecipeEditorLayout.ALCHEMIST_CAULDRON;
    }

    public boolean isIronAlchemistCauldronFillEntry(RecipeEntry entry) {
        return entry.isType(IronSpellbooksRecipeEditorTypes.ALCHEMIST_CAULDRON_FILL);
    }

    public boolean isIronAlchemistCauldronEmptyEntry(RecipeEntry entry) {
        return entry.isType(IronSpellbooksRecipeEditorTypes.ALCHEMIST_CAULDRON_EMPTY);
    }

    public boolean isIronAlchemistCauldronBrewEntry(RecipeEntry entry) {
        return entry.isType(IronSpellbooksRecipeEditorTypes.ALCHEMIST_CAULDRON_BREW);
    }

    public boolean isIceAndFireDragonForgeEntry(RecipeEntry entry) {
        return entry.isType(IceAndFireRecipeEditorTypes.DRAGONFORGE);
    }

    public boolean isCataclysmWeaponFusionEntry(RecipeEntry entry) {
        return entry.isType(CataclysmRecipeEditorTypes.WEAPON_FUSION);
    }

    public boolean isCataclysmAmethystBlessEntry(RecipeEntry entry) {
        return entry.isType(CataclysmRecipeEditorTypes.AMETHYST_BLESS);
    }

    public boolean isTouhouLittleMaidAltarEntry(RecipeEntry entry) {
        return entry.isType(TouhouLittleMaidRecipeEditorTypes.ALTAR_RECIPE);
    }

    public boolean isSporeSurgeryEntry(RecipeEntry entry) {
        return entry.isType(SporeRecipeEditorTypes.SURGERY);
    }

    public boolean isSporeGraftingEntry(RecipeEntry entry) {
        return entry.isType(SporeRecipeEditorTypes.GRAFTING);
    }

    public boolean isGoetyCursedInfuserEntry(RecipeEntry entry) {
        return entry.isType(GoetyRecipeEditorTypes.CURSED_INFUSER_RECIPE);
    }

    public boolean isGoetyRitualEntry(RecipeEntry entry) {
        return entry.isType(GoetyRecipeEditorTypes.RITUAL);
    }

    public boolean isGoetyBrazierEntry(RecipeEntry entry) {
        return entry.isType(GoetyRecipeEditorTypes.BRAZIER);
    }

    public boolean isGoetyPulverizeEntry(RecipeEntry entry) {
        return entry.isType(GoetyRecipeEditorTypes.PULVERIZE);
    }

    public boolean isGoetyBrewingEntry(RecipeEntry entry) {
        return entry.isType(GoetyRecipeEditorTypes.BREWING);
    }

    public boolean isFarmersCookingPotEntry(RecipeEntry entry) {
        return entry.isType(FarmersDelightRecipeEditorTypes.COOKING);
    }

    public boolean isFarmersCuttingBoardEntry(RecipeEntry entry) {
        return entry.isType(FarmersDelightRecipeEditorTypes.CUTTING);
    }

    public boolean isKaleidoscopePotEntry(RecipeEntry entry) {
        return entry.isType(KaleidoscopeCookeryRecipeEditorTypes.POT);
    }

    public boolean isKaleidoscopeStockpotEntry(RecipeEntry entry) {
        return entry.isType(KaleidoscopeCookeryRecipeEditorTypes.STOCKPOT);
    }

    public boolean isKaleidoscopeMillstoneEntry(RecipeEntry entry) {
        return entry.isType(KaleidoscopeCookeryRecipeEditorTypes.MILLSTONE);
    }

    public boolean isKaleidoscopeChoppingBoardEntry(RecipeEntry entry) {
        return entry.isType(KaleidoscopeCookeryRecipeEditorTypes.CHOPPING_BOARD);
    }

    public boolean isKaleidoscopeSteamerEntry(RecipeEntry entry) {
        return entry.isType(KaleidoscopeCookeryRecipeEditorTypes.STEAMER);
    }

    public boolean isKaleidoscopeTeapotEntry(RecipeEntry entry) {
        return entry.isType(KaleidoscopeCookeryRecipeEditorTypes.TEAPOT);
    }

    public boolean isCreateProcessingEntry(RecipeEntry entry) {
        return RecipeEditorTypes.layoutForType(entry.getType()) == RecipeEditorLayout.CREATE_PROCESSING;
    }

    public boolean isCreateMechanicalCraftingEntry(RecipeEntry entry) {
        return entry.isType(RecipeEditorTypes.CREATE_MECHANICAL_CRAFTING);
    }

    public boolean isCreateSequencedAssemblyEntry(RecipeEntry entry) {
        return entry.isType(RecipeEditorTypes.CREATE_SEQUENCED_ASSEMBLY);
    }

    public boolean isExtendedCraftingTableEntry(RecipeEntry entry) {
        return ExtendedCraftingRecipeEditorTypes.isTableType(entry.getType());
    }

    public boolean isExtendedCraftingShapedTableEntry(RecipeEntry entry) {
        return ExtendedCraftingRecipeEditorTypes.isShapedTableType(entry.getType());
    }

    public boolean isExtendedCraftingShapelessTableEntry(RecipeEntry entry) {
        return ExtendedCraftingRecipeEditorTypes.isShapelessTableType(entry.getType());
    }

    public boolean isExtendedCraftingUltimateSingularityEntry(RecipeEntry entry) {
        return entry.isType(ExtendedCraftingRecipeEditorTypes.ULTIMATE_SINGULARITY);
    }

    public boolean isExtendedCraftingEnderCrafterEntry(RecipeEntry entry) {
        return entry.isType(ExtendedCraftingRecipeEditorTypes.SHAPED_ENDER_CRAFTER)
                || entry.isType(ExtendedCraftingRecipeEditorTypes.SHAPELESS_ENDER_CRAFTER);
    }

    public boolean isExtendedCraftingShapedEnderCrafterEntry(RecipeEntry entry) {
        return entry.isType(ExtendedCraftingRecipeEditorTypes.SHAPED_ENDER_CRAFTER);
    }

    public boolean isExtendedCraftingFluxCrafterEntry(RecipeEntry entry) {
        return entry.isType(ExtendedCraftingRecipeEditorTypes.SHAPED_FLUX_CRAFTER)
                || entry.isType(ExtendedCraftingRecipeEditorTypes.SHAPELESS_FLUX_CRAFTER);
    }

    public boolean isExtendedCraftingShapedFluxCrafterEntry(RecipeEntry entry) {
        return entry.isType(ExtendedCraftingRecipeEditorTypes.SHAPED_FLUX_CRAFTER);
    }

    public boolean isExtendedCraftingCombinationEntry(RecipeEntry entry) {
        return entry.isType(ExtendedCraftingRecipeEditorTypes.COMBINATION);
    }

    public boolean isExtendedCraftingCompressorEntry(RecipeEntry entry) {
        return entry.isType(ExtendedCraftingRecipeEditorTypes.COMPRESSOR_RECIPE);
    }

    public boolean isAvaritiaTableEntry(RecipeEntry entry) {
        return AvaritiaRecipeEditorTypes.isTableType(entry.getType());
    }

    public boolean isAvaritiaNormalTableEntry(RecipeEntry entry) {
        return AvaritiaRecipeEditorTypes.isNormalTableType(entry.getType());
    }

    public boolean isAvaritiaShapedTableEntry(RecipeEntry entry) {
        return AvaritiaRecipeEditorTypes.isShapedTableType(entry.getType())
                || AvaritiaRecipeEditorTypes.isNoConsumeCatalystType(entry.getType());
    }

    public boolean isAvaritiaShapelessTableEntry(RecipeEntry entry) {
        return AvaritiaRecipeEditorTypes.isShapelessTableType(entry.getType());
    }

    public boolean isAvaritiaCompressorEntry(RecipeEntry entry) {
        return entry.isType(AvaritiaRecipeEditorTypes.COMPRESSOR);
    }

    public boolean isAvaritiaExtremeSmithingEntry(RecipeEntry entry) {
        return entry.isType(AvaritiaRecipeEditorTypes.EXTREME_SMITHING);
    }

    public boolean isArsNouveauApparatusEntry(RecipeEntry entry) {
        return entry.isType(ArsNouveauRecipeEditorTypes.APPARATUS);
    }

    public boolean isArsNouveauArmorUpgradeEntry(RecipeEntry entry) {
        return entry.isType(ArsNouveauRecipeEditorTypes.ARMOR_UPGRADE);
    }

    public boolean isArsNouveauEnchantmentEntry(RecipeEntry entry) {
        return entry.isType(ArsNouveauRecipeEditorTypes.ENCHANTMENT);
    }

    public boolean isArsNouveauImbuementEntry(RecipeEntry entry) {
        return entry.isType(ArsNouveauRecipeEditorTypes.IMBUEMENT);
    }

    public boolean isArsNouveauGlyphEntry(RecipeEntry entry) {
        return entry.isType(ArsNouveauRecipeEditorTypes.GLYPH);
    }

    public boolean isArsNouveauCrushEntry(RecipeEntry entry) {
        return entry.isType(ArsNouveauRecipeEditorTypes.CRUSH);
    }

    public boolean isArsNouveauPedestalOnlyEntry(RecipeEntry entry) {
        return entry.isType(ArsNouveauRecipeEditorTypes.REACTIVE_ENCHANTMENT)
                || entry.isType(ArsNouveauRecipeEditorTypes.SPELL_WRITE)
                || entry.isType(ArsNouveauRecipeEditorTypes.PRESTIDIGITATION);
    }

    public boolean isArsNouveauApparatusLayoutEntry(RecipeEntry entry) {
        return RecipeEditorTypes.layoutForType(entry.getType()) == RecipeEditorLayout.ARS_NOUVEAU_APPARATUS;
    }

    public boolean isArsNouveauGlyphLayoutEntry(RecipeEntry entry) {
        return RecipeEditorTypes.layoutForType(entry.getType()) == RecipeEditorLayout.ARS_NOUVEAU_GLYPH;
    }

    public boolean isArsNouveauImbuementLayoutEntry(RecipeEntry entry) {
        return RecipeEditorTypes.layoutForType(entry.getType()) == RecipeEditorLayout.ARS_NOUVEAU_IMBUEMENT;
    }

    public int selectedArsNouveauInputCount() {
        if (isSelectedArsNouveauImbuementLayout()) {
            return ARS_NOUVEAU_IMBUEMENT_INPUTS;
        }
        return ARS_NOUVEAU_MAX_INPUTS;
    }

    public int selectedArsNouveauCrushOutputCount() {
        return ARS_NOUVEAU_MAX_CRUSH_OUTPUTS;
    }

    public ItemStack selectedArsNouveauWorkstationStack() {
        if (selectedEntry == null) {
            if (ArsNouveauRecipeEditorTypes.IMBUEMENT_CHAMBER.equals(selectedCategory)) {
                return new ItemStack(itemFromRegistry("ars_nouveau:imbuement_chamber", Items.ENCHANTING_TABLE));
            }
            if (ArsNouveauRecipeEditorTypes.SCRIBES_TABLE.equals(selectedCategory)) {
                return new ItemStack(itemFromRegistry("ars_nouveau:scribes_table", Items.LECTERN));
            }
            return new ItemStack(itemFromRegistry("ars_nouveau:enchanting_apparatus", Items.ENCHANTING_TABLE));
        }
        if (isArsNouveauImbuementEntry(selectedEntry)) {
            return new ItemStack(itemFromRegistry("ars_nouveau:imbuement_chamber", Items.ENCHANTING_TABLE));
        }
        if (isArsNouveauGlyphEntry(selectedEntry)) {
            return new ItemStack(itemFromRegistry("ars_nouveau:scribes_table", Items.LECTERN));
        }
        return new ItemStack(itemFromRegistry("ars_nouveau:enchanting_apparatus", Items.ENCHANTING_TABLE));
    }

    public boolean selectedArsNouveauApparatusHasDerivedPreview() {
        return selectedEntry != null
                && (isArsNouveauArmorUpgradeEntry(selectedEntry)
                || isArsNouveauEnchantmentEntry(selectedEntry)
                || isArsNouveauPedestalOnlyEntry(selectedEntry));
    }

    public ItemStack selectedArsNouveauApparatusCenterPreview() {
        if (selectedEntry == null) {
            return ItemStack.EMPTY;
        }
        if (isArsNouveauArmorUpgradeEntry(selectedEntry)) {
            return new ItemStack(itemFromRegistry("ars_nouveau:arcanist_robes", Items.LEATHER_CHESTPLATE));
        }
        if (isArsNouveauEnchantmentEntry(selectedEntry)) {
            return new ItemStack(getArsNouveauEnchantmentLevel(selectedEntry) <= 1 ? Items.BOOK : Items.ENCHANTED_BOOK);
        }
        if (isArsNouveauPedestalOnlyEntry(selectedEntry)) {
            return new ItemStack(itemFromRegistry("ars_nouveau:spell_parchment", Items.PAPER));
        }
        return ItemStack.EMPTY;
    }

    public ItemStack selectedArsNouveauApparatusOutputPreview() {
        if (selectedEntry == null) {
            return ItemStack.EMPTY;
        }
        if (isArsNouveauArmorUpgradeEntry(selectedEntry)) {
            return new ItemStack(itemFromRegistry("ars_nouveau:arcanist_robes", Items.LEATHER_CHESTPLATE));
        }
        if (isArsNouveauEnchantmentEntry(selectedEntry)) {
            return new ItemStack(Items.ENCHANTED_BOOK);
        }
        if (isArsNouveauPedestalOnlyEntry(selectedEntry)) {
            return selectedEntry.isType(ArsNouveauRecipeEditorTypes.REACTIVE_ENCHANTMENT)
                    ? new ItemStack(Items.ENCHANTED_BOOK)
                    : new ItemStack(itemFromRegistry("ars_nouveau:spell_parchment", Items.PAPER));
        }
        return ItemStack.EMPTY;
    }

    public Component selectedArsNouveauApparatusTierLabel() {
        if (selectedEntry != null && isArsNouveauArmorUpgradeEntry(selectedEntry)) {
            return Component.translatable("ars_nouveau.tier", getArsNouveauArmorUpgradeTier(selectedEntry));
        }
        return Component.empty();
    }

    public Component selectedArsNouveauApparatusSourceLabel() {
        var source = selectedEntry == null ? 0 : getArsNouveauSourceCost(selectedEntry);
        return source <= 0 ? Component.empty() : Component.translatable("ars_nouveau.source", source);
    }

    public Component arsNouveauInputSlotName(int index) {
        if (selectedEntry == null) {
            if (ArsNouveauRecipeEditorTypes.IMBUEMENT_CHAMBER.equals(selectedCategory)) {
                return Component.translatable(index == 0
                        ? "viscript_recipe.editor.ars_nouveau.input"
                        : "viscript_recipe.editor.ars_nouveau.pedestal_item");
            }
            return Component.translatable("viscript_recipe.editor.ars_nouveau.input");
        }
        if (isArsNouveauApparatusEntry(selectedEntry)) {
            return Component.translatable(index == 0
                    ? "viscript_recipe.editor.ars_nouveau.reagent"
                    : "viscript_recipe.editor.ars_nouveau.pedestal_item");
        }
        if (isArsNouveauArmorUpgradeEntry(selectedEntry)
                || isArsNouveauEnchantmentEntry(selectedEntry)
                || isArsNouveauPedestalOnlyEntry(selectedEntry)) {
            return Component.translatable("viscript_recipe.editor.ars_nouveau.pedestal_item");
        }
        if (isArsNouveauImbuementEntry(selectedEntry)) {
            return Component.translatable(index == 0
                    ? "viscript_recipe.editor.ars_nouveau.input"
                    : "viscript_recipe.editor.ars_nouveau.pedestal_item");
        }
        if (isArsNouveauGlyphEntry(selectedEntry)) {
            return Component.translatable("viscript_recipe.editor.ars_nouveau.glyph_input");
        }
        return Component.translatable("viscript_recipe.editor.ars_nouveau.input");
    }

    public int getArsNouveauSourceCost(RecipeEntry entry) {
        if (isArsNouveauApparatusEntry(entry)) {
            return Math.max(0, entry.getArsNouveauApparatus().getSourceCost());
        }
        if (isArsNouveauArmorUpgradeEntry(entry)) {
            return Math.max(0, entry.getArsNouveauArmorUpgrade().getSourceCost());
        }
        if (isArsNouveauEnchantmentEntry(entry)) {
            return Math.max(0, entry.getArsNouveauEnchantment().getSourceCost());
        }
        if (isArsNouveauImbuementEntry(entry)) {
            return Math.max(0, entry.getArsNouveauImbuement().getSource());
        }
        if (isArsNouveauPedestalOnlyEntry(entry)) {
            return Math.max(0, entry.getArsNouveauPedestalOnly().getSourceCost());
        }
        return 0;
    }

    public void setArsNouveauSourceCost(RecipeEntry entry, int sourceCost) {
        var normalized = Math.max(0, sourceCost);
        if (isArsNouveauApparatusEntry(entry)) {
            entry.getArsNouveauApparatus().setSourceCost(normalized);
            notifyChanged();
        } else if (isArsNouveauArmorUpgradeEntry(entry)) {
            entry.getArsNouveauArmorUpgrade().setSourceCost(normalized);
            notifyChanged();
        } else if (isArsNouveauEnchantmentEntry(entry)) {
            entry.getArsNouveauEnchantment().setSourceCost(normalized);
            notifyChanged();
        } else if (isArsNouveauImbuementEntry(entry)) {
            entry.getArsNouveauImbuement().setSource(normalized);
            notifyChanged();
        } else if (isArsNouveauPedestalOnlyEntry(entry)) {
            entry.getArsNouveauPedestalOnly().setSourceCost(normalized);
            notifyChanged();
        }
    }

    public int getArsNouveauArmorUpgradeTier(RecipeEntry entry) {
        return isArsNouveauArmorUpgradeEntry(entry) ? Math.max(2, entry.getArsNouveauArmorUpgrade().getTier() + 1) : 2;
    }

    public void setArsNouveauArmorUpgradeTier(RecipeEntry entry, int tier) {
        if (!isArsNouveauArmorUpgradeEntry(entry)) {
            return;
        }
        entry.getArsNouveauArmorUpgrade().setTier(Math.max(1, tier - 1));
        notifyChanged();
    }

    public ResourceLocation getArsNouveauEnchantmentId(RecipeEntry entry) {
        var fallback = new ResourceLocation("sharpness");
        return isArsNouveauEnchantmentEntry(entry) && entry.getArsNouveauEnchantment().getEnchantment() != null
                ? entry.getArsNouveauEnchantment().getEnchantment()
                : fallback;
    }

    public void setArsNouveauEnchantmentId(RecipeEntry entry, ResourceLocation enchantment) {
        if (!isArsNouveauEnchantmentEntry(entry) || enchantment == null) {
            return;
        }
        entry.getArsNouveauEnchantment().setEnchantment(enchantment);
        notifyChanged();
    }

    public int getArsNouveauEnchantmentLevel(RecipeEntry entry) {
        return isArsNouveauEnchantmentEntry(entry) ? Math.max(1, entry.getArsNouveauEnchantment().getLevel()) : 1;
    }

    public void setArsNouveauEnchantmentLevel(RecipeEntry entry, int level) {
        if (!isArsNouveauEnchantmentEntry(entry)) {
            return;
        }
        entry.getArsNouveauEnchantment().setLevel(Math.max(1, level));
        notifyChanged();
    }

    public boolean getArsNouveauKeepNbtOfReagent(RecipeEntry entry) {
        return isArsNouveauApparatusEntry(entry) && entry.getArsNouveauApparatus().isKeepNbtOfReagent();
    }

    public void setArsNouveauKeepNbtOfReagent(RecipeEntry entry, boolean keepNbtOfReagent) {
        if (!isArsNouveauApparatusEntry(entry)) {
            return;
        }
        entry.getArsNouveauApparatus().setKeepNbtOfReagent(keepNbtOfReagent);
        notifyChanged();
    }

    public int getArsNouveauGlyphExperience(RecipeEntry entry) {
        return isArsNouveauGlyphEntry(entry) ? Math.max(0, entry.getArsNouveauGlyph().getExp()) : 0;
    }

    public void setArsNouveauGlyphExperience(RecipeEntry entry, int exp) {
        if (!isArsNouveauGlyphEntry(entry)) {
            return;
        }
        entry.getArsNouveauGlyph().setExp(Math.max(0, exp));
        notifyChanged();
    }

    public boolean getArsNouveauCrushSkipBlockPlace(RecipeEntry entry) {
        return isArsNouveauCrushEntry(entry) && entry.getArsNouveauCrush().isSkipBlockPlace();
    }

    public void setArsNouveauCrushSkipBlockPlace(RecipeEntry entry, boolean skipBlockPlace) {
        if (!isArsNouveauCrushEntry(entry)) {
            return;
        }
        entry.getArsNouveauCrush().setSkipBlockPlace(skipBlockPlace);
        notifyChanged();
    }

    public boolean isCreateAutoPackingEntry(RecipeEntry entry) {
        return entry.isType(CreateProcessingKind.AUTO_PACKING.typeId());
    }

    public boolean isSelectedCreateItemApplicationBlockInput() {
        return selectedEntry != null
                && selectedEntry.isType(CreateProcessingKind.ITEM_APPLICATION.typeId())
                && slotSelection.kind() == WorkbenchSlotSelection.Kind.INGREDIENT
                && slotSelection.index() == 0;
    }

    public java.util.Optional<CreateProcessingKind> selectedCreateKind() {
        if (selectedEntry != null) {
            return CreateProcessingKind.byType(selectedEntry.getType());
        }
        return RecipeEditorTypes.defaultTypeForCategory(selectedCategory) == null
                ? java.util.Optional.empty()
                : CreateProcessingKind.byType(RecipeEditorTypes.defaultTypeForCategory(selectedCategory));
    }

    public int selectedCreateItemInputCount() {
        var kind = selectedCreateKind().orElse(null);
        var capacity = createVisibleItemInputCapacity(kind);
        if (kind == null || selectedEntry == null || !supportsCreateCountedItemInputs(kind)) {
            return capacity;
        }
        var extraWeight = 0;
        var lastOccupiedSlot = -1;
        for (int i = 0; i < capacity; i++) {
            var weight = CreateItemInputCounts.slotWeight(visualIngredientData[i]);
            if (weight <= 0) {
                continue;
            }
            extraWeight += Math.max(0, weight - 1);
            lastOccupiedSlot = i;
        }
        var visibleCount = capacity - Math.min(capacity - 1, extraWeight);
        if (lastOccupiedSlot >= 0) {
            visibleCount = Math.max(visibleCount, lastOccupiedSlot + 1);
        }
        return Math.max(1, Math.min(capacity, visibleCount));
    }

    public boolean isSelectedCreateCountedItemInput() {
        if (selectedEntry == null
                || !isCreateProcessingEntry(selectedEntry)
                || slotSelection.kind() != WorkbenchSlotSelection.Kind.INGREDIENT) {
            return false;
        }
        var kind = selectedCreateKind().orElse(null);
        return supportsCreateCountedItemInputs(kind)
                && slotSelection.index() >= 0
                && slotSelection.index() < createVisibleItemInputCapacity(kind);
    }

    private boolean isCreateCountedItemInputSlot(int index) {
        if (selectedEntry == null || !isCreateProcessingEntry(selectedEntry)) {
            return false;
        }
        var kind = selectedCreateKind().orElse(null);
        return supportsCreateCountedItemInputs(kind)
                && index >= 0
                && index < createVisibleItemInputCapacity(kind);
    }

    public ItemStack normalizeSelectedIngredientItemStack(ItemStack stack) {
        if (stack == null || stack.isEmpty() || stack.is(Items.AIR)) {
            return ItemStack.EMPTY;
        }
        var copy = stack.copy();
        if (isSelectedCreateCountedItemInput()) {
            copy.setCount(Math.max(1, Math.min(selectedCreateItemInputMaxWeight(slotSelection.index()), copy.getCount())));
        } else {
            copy.setCount(1);
        }
        return copy;
    }

    public int selectedCreateCountedInputSignature() {
        var kind = selectedCreateKind().orElse(null);
        if (selectedEntry == null || !supportsCreateCountedItemInputs(kind)) {
            return 0;
        }
        var capacity = createVisibleItemInputCapacity(kind);
        var signature = selectedCreateItemInputCount();
        for (int i = 0; i < capacity; i++) {
            signature = 31 * signature + CreateItemInputCounts.slotWeight(visualIngredientData[i]);
        }
        return signature;
    }

    private int selectedCreateItemInputMaxWeight(int slot) {
        var kind = selectedCreateKind().orElse(null);
        if (!supportsCreateCountedItemInputs(kind)) {
            return 1;
        }
        return createItemInputMaxWeight(slot, createVisibleItemInputCapacity(kind));
    }

    private int createItemInputMaxWeight(int slot, int capacity) {
        var usedByOtherSlots = 0;
        for (int i = 0; i < capacity; i++) {
            if (i == slot) {
                continue;
            }
            usedByOtherSlots += CreateItemInputCounts.slotWeight(visualIngredientData[i]);
        }
        return Math.max(1, capacity - usedByOtherSlots);
    }

    private int createVisibleItemInputCapacity(CreateProcessingKind kind) {
        return kind == null ? 0 : Math.min(9, kind.maxItemInputs());
    }

    private boolean supportsCreateCountedItemInputs(CreateProcessingKind kind) {
        return kind == CreateProcessingKind.MIXING
                || kind == CreateProcessingKind.COMPACTING
                || kind == CreateProcessingKind.AUTOMATIC_SHAPELESS;
    }

    public int selectedCreateFluidInputCount() {
        return Math.min(CREATE_MAX_FLUID_INPUTS, selectedCreateKind().map(CreateProcessingKind::maxFluidInputs).orElse(0));
    }

    public int selectedCreateItemOutputCount() {
        return Math.min(CREATE_MAX_ITEM_OUTPUTS, selectedCreateKind().map(CreateProcessingKind::maxItemOutputs).orElse(0));
    }

    public int selectedCreateFluidOutputCount() {
        return Math.min(CREATE_MAX_FLUID_OUTPUTS, selectedCreateKind().map(CreateProcessingKind::maxFluidOutputs).orElse(0));
    }

    public int selectedCreateSequencedStepCount() {
        return selectedEntry == null ? 0 : createSequencedStepCount(selectedEntry);
    }

    public boolean isSelectedCreateSequencedStep(int index) {
        return selectedEntry != null
                && isCreateSequencedAssemblyEntry(selectedEntry)
                && slotSelection.kind() == WorkbenchSlotSelection.Kind.CREATE_SEQUENCED_STEP
                && slotSelection.index() == index;
    }

    public int selectedCreateSequencedOutputCount() {
        return CREATE_SEQUENCED_MAX_OUTPUTS;
    }

    public ItemStack getVisualCreateSequencedTransitional() {
        return visualCreateSequencedTransitional.copy();
    }

    public void setVisualCreateSequencedTransitional(ItemStack stack) {
        if (selectedEntry == null || !isCreateSequencedAssemblyEntry(selectedEntry)) {
            return;
        }
        visualCreateSequencedTransitional = stack == null ? ItemStack.EMPTY : stack.copyWithCount(1);
        selectedEntry.getCreateSequencedAssembly().setTransitionalItem(visualCreateSequencedTransitional.copy());
        notifyChanged();
    }

    public void clearVisualCreateSequencedTransitional() {
        setVisualCreateSequencedTransitional(ItemStack.EMPTY);
    }

    public CreateSequencedAssemblyStepKind getCreateSequencedStepKind(RecipeEntry entry, int index) {
        var kind = getCreateSequencedStep(entry, index).getKind();
        return kind == null ? CreateSequencedAssemblyStepKind.DEPLOYING : kind;
    }

    public void setCreateSequencedStepKind(RecipeEntry entry, int index, CreateSequencedAssemblyStepKind kind) {
        if (!isCreateSequencedAssemblyEntry(entry)) {
            return;
        }
        getCreateSequencedStep(entry, index).setKind(kind == null ? CreateSequencedAssemblyStepKind.DEPLOYING : kind);
        loadSelectedEntryToVisualState();
        notifyChanged();
    }

    public List<CreateSequencedAssemblyStepKind> createSequencedStepKinds() {
        return List.of(CreateSequencedAssemblyStepKind.DEPLOYING, CreateSequencedAssemblyStepKind.PRESSING,
                CreateSequencedAssemblyStepKind.CUTTING, CreateSequencedAssemblyStepKind.FILLING);
    }

    public Component createSequencedStepKindDisplayName(CreateSequencedAssemblyStepKind kind) {
        var normalized = kind == null ? CreateSequencedAssemblyStepKind.DEPLOYING : kind;
        return Component.translatable("viscript_recipe.editor.create.sequenced_assembly.step.kind." + normalized.getSerializedName());
    }

    public int getCreateSequencedLoops(RecipeEntry entry) {
        return Math.max(1, entry.getCreateSequencedAssembly().getLoops());
    }

    public void setCreateSequencedLoops(RecipeEntry entry, int loops) {
        if (!isCreateSequencedAssemblyEntry(entry)) {
            return;
        }
        entry.getCreateSequencedAssembly().setLoops(Math.max(1, loops));
        notifyChanged();
    }

    public int getCreateSequencedStepProcessingTime(RecipeEntry entry, int index) {
        return Math.max(0, getCreateSequencedStep(entry, index).getProcessingTime());
    }

    public void setCreateSequencedStepProcessingTime(RecipeEntry entry, int index, int processingTime) {
        if (!isCreateSequencedAssemblyEntry(entry)) {
            return;
        }
        getCreateSequencedStep(entry, index).setProcessingTime(Math.max(0, processingTime));
        notifyChanged();
    }

    public boolean getCreateSequencedStepKeepHeldItem(RecipeEntry entry, int index) {
        return getCreateSequencedStep(entry, index).isKeepHeldItem();
    }

    public void setCreateSequencedStepKeepHeldItem(RecipeEntry entry, int index, boolean keepHeldItem) {
        if (!isCreateSequencedAssemblyEntry(entry)) {
            return;
        }
        getCreateSequencedStep(entry, index).setKeepHeldItem(keepHeldItem);
        notifyChanged();
    }

    public void addCreateSequencedStep(RecipeEntry entry) {
        if (!isCreateSequencedAssemblyEntry(entry)) {
            return;
        }
        var sequence = entry.getCreateSequencedAssembly().getSequence();
        if (sequence == null) {
            sequence = new ArrayList<>();
            entry.getCreateSequencedAssembly().setSequence(sequence);
        }
        if (sequence.size() >= CREATE_SEQUENCED_MAX_STEPS) {
            return;
        }
        sequence.add(new CreateSequencedAssemblyStepData());
        slotSelection = WorkbenchSlotSelection.createSequencedStep(sequence.size() - 1);
        loadSelectedEntryToVisualState();
        notifyChanged();
    }

    public void removeCreateSequencedStep(RecipeEntry entry, int index) {
        if (!isCreateSequencedAssemblyEntry(entry)) {
            return;
        }
        var sequence = entry.getCreateSequencedAssembly().getSequence();
        if (sequence == null || sequence.size() <= 1 || index < 0 || index >= sequence.size()) {
            return;
        }
        sequence.remove(index);
        if (slotSelection.kind() == WorkbenchSlotSelection.Kind.CREATE_SEQUENCED_STEP) {
            slotSelection = WorkbenchSlotSelection.RECIPE;
        }
        loadSelectedEntryToVisualState();
        notifyChanged();
    }

    public CreateFluidIngredientData getCreateSequencedStepFluidIngredient(int index) {
        if (selectedEntry == null || !isCreateSequencedAssemblyEntry(selectedEntry)) {
            return CreateFluidIngredientData.empty();
        }
        var step = getCreateSequencedStep(selectedEntry, index);
        return step.getFluidIngredient() == null ? CreateFluidIngredientData.empty() : step.getFluidIngredient().copy();
    }

    public FluidStack getCreateSequencedStepFluidDisplay(int index) {
        var ingredient = getCreateSequencedStepFluidIngredient(index);
        if (ingredient.getKind() == CreateFluidIngredientKind.TAG) {
            var stacks = fluidsFromTag(ingredient.getTag(), ingredient.getAmount());
            return stacks.length == 0 ? FluidStack.EMPTY : stacks[0].copy();
        }
        return ingredient.getFluid() == null ? FluidStack.EMPTY : ingredient.getFluid().copy();
    }

    public FluidStack[] getCreateSequencedStepFluidTagStacks(int index) {
        var ingredient = getCreateSequencedStepFluidIngredient(index);
        return ingredient.getKind() == CreateFluidIngredientKind.TAG
                ? fluidsFromTag(ingredient.getTag(), ingredient.getAmount())
                : new FluidStack[0];
    }

    @Nullable
    public ResourceLocation getCreateSequencedStepFluidTag(int index) {
        var ingredient = getCreateSequencedStepFluidIngredient(index);
        return ingredient.getKind() == CreateFluidIngredientKind.TAG ? ingredient.getTag() : null;
    }

    public void setCreateSequencedStepFluidIngredient(int index, CreateFluidIngredientData ingredient) {
        if (selectedEntry == null || !isCreateSequencedAssemblyEntry(selectedEntry)) {
            return;
        }
        getCreateSequencedStep(selectedEntry, index).setFluidIngredient(ingredient == null ? CreateFluidIngredientData.empty() : ingredient.copy());
        notifyChanged();
    }

    public int selectedCreateMechanicalCraftingWidth() {
        return selectedEntry == null
                ? 3
                : selectedEntry.getCreateMechanicalCrafting().normalizedWidth();
    }

    public int selectedCreateMechanicalCraftingHeight() {
        return selectedEntry == null
                ? 3
                : selectedEntry.getCreateMechanicalCrafting().normalizedHeight();
    }

    public int selectedLargeCraftingGridWidth() {
        if (selectedEntry != null && isExtendedCraftingTableEntry(selectedEntry)) {
            return selectedExtendedCraftingTableGridWidth(selectedEntry);
        }
        if (selectedEntry != null && isAvaritiaTableEntry(selectedEntry)) {
            return selectedAvaritiaTableGridWidth(selectedEntry);
        }
        return selectedCreateMechanicalCraftingWidth();
    }

    public int selectedLargeCraftingGridHeight() {
        if (selectedEntry != null && isExtendedCraftingTableEntry(selectedEntry)) {
            return selectedExtendedCraftingTableGridHeight(selectedEntry);
        }
        if (selectedEntry != null && isAvaritiaTableEntry(selectedEntry)) {
            return selectedAvaritiaTableGridHeight(selectedEntry);
        }
        return selectedCreateMechanicalCraftingHeight();
    }

    public Component selectedLargeCraftingGridSizeLabel() {
        if (selectedEntry != null && isExtendedCraftingTableEntry(selectedEntry)) {
            return Component.translatable("viscript_recipe.editor.extendedcrafting.table.size", selectedLargeCraftingGridWidth(), selectedLargeCraftingGridHeight());
        }
        if (selectedEntry != null && isAvaritiaTableEntry(selectedEntry)) {
            return Component.translatable("viscript_recipe.editor.avaritia.table.size", selectedLargeCraftingGridWidth(), selectedLargeCraftingGridHeight());
        }
        return Component.translatable("viscript_recipe.editor.create.mechanical_crafting.size", selectedLargeCraftingGridWidth(), selectedLargeCraftingGridHeight());
    }

    public ItemStack selectedLargeCraftingGridWorkstationStack() {
        if (selectedEntry != null && isExtendedCraftingTableEntry(selectedEntry)) {
            return new ItemStack(itemFromRegistry(
                    ExtendedCraftingRecipeEditorTypes.tableItemForTier(getExtendedCraftingTableTier(selectedEntry)).toString(),
                    Items.CRAFTING_TABLE
            ));
        }
        if (selectedEntry != null && isAvaritiaTableEntry(selectedEntry)) {
            return new ItemStack(itemFromRegistry(
                    AvaritiaRecipeEditorTypes.tableItemForTier(getAvaritiaTableTier(selectedEntry)).toString(),
                    Items.CRAFTING_TABLE
            ));
        }
        if (selectedEntry == null && RecipeEditorTypes.getCategory(selectedCategory)
                .map(RecipeEditorCategory::layout)
                .orElse(RecipeEditorLayout.CRAFTING_GRID) == RecipeEditorLayout.EXTENDED_CRAFTING_TABLE) {
            var defaultType = RecipeEditorTypes.defaultTypeForCategory(selectedCategory);
            if (AvaritiaRecipeEditorTypes.isTableType(defaultType)) {
                var tier = AvaritiaRecipeEditorTypes.tableTierForType(defaultType);
                return new ItemStack(itemFromRegistry(AvaritiaRecipeEditorTypes.tableItemForTier(tier).toString(), Items.CRAFTING_TABLE));
            }
            var tier = ExtendedCraftingRecipeEditorTypes.tableTierForType(defaultType);
            return new ItemStack(itemFromRegistry(ExtendedCraftingRecipeEditorTypes.tableItemForTier(tier).toString(), Items.CRAFTING_TABLE));
        }
        return new ItemStack(itemFromRegistry("create:mechanical_crafter", Items.CRAFTING_TABLE));
    }

    private int selectedExtendedCraftingTableGridWidth(RecipeEntry entry) {
        return ExtendedCraftingRecipeEditorTypes.tableGridSizeForTier(getExtendedCraftingTableTier(entry));
    }

    private int selectedExtendedCraftingTableGridHeight(RecipeEntry entry) {
        return ExtendedCraftingRecipeEditorTypes.tableGridSizeForTier(getExtendedCraftingTableTier(entry));
    }

    private int selectedAvaritiaTableGridWidth(RecipeEntry entry) {
        return AvaritiaRecipeEditorTypes.tableGridSizeForTier(getAvaritiaTableTier(entry));
    }

    private int selectedAvaritiaTableGridHeight(RecipeEntry entry) {
        return AvaritiaRecipeEditorTypes.tableGridSizeForTier(getAvaritiaTableTier(entry));
    }

    public int getCreateMechanicalCraftingWidth(RecipeEntry entry) {
        return entry.getCreateMechanicalCrafting().normalizedWidth();
    }

    public void setCreateMechanicalCraftingWidth(RecipeEntry entry, int width) {
        if (!isCreateMechanicalCraftingEntry(entry)) {
            return;
        }
        entry.getCreateMechanicalCrafting().setWidth(width);
        writeCreateMechanicalCraftingRecipe(entry.getCreateMechanicalCrafting());
        loadSelectedEntryToVisualState();
        notifyChanged();
    }

    public int getCreateMechanicalCraftingHeight(RecipeEntry entry) {
        return entry.getCreateMechanicalCrafting().normalizedHeight();
    }

    public void setCreateMechanicalCraftingHeight(RecipeEntry entry, int height) {
        if (!isCreateMechanicalCraftingEntry(entry)) {
            return;
        }
        entry.getCreateMechanicalCrafting().setHeight(height);
        writeCreateMechanicalCraftingRecipe(entry.getCreateMechanicalCrafting());
        loadSelectedEntryToVisualState();
        notifyChanged();
    }

    public boolean getCreateMechanicalCraftingAcceptMirrored(RecipeEntry entry) {
        return entry.getCreateMechanicalCrafting().isAcceptMirrored();
    }

    public void setCreateMechanicalCraftingAcceptMirrored(RecipeEntry entry, boolean acceptMirrored) {
        if (!isCreateMechanicalCraftingEntry(entry)) {
            return;
        }
        entry.getCreateMechanicalCrafting().setAcceptMirrored(acceptMirrored);
        notifyChanged();
    }

    public int getExtendedCraftingTableWidth(RecipeEntry entry) {
        return entry.getExtendedCraftingTable().normalizedWidth();
    }

    public void setExtendedCraftingTableWidth(RecipeEntry entry, int width) {
        if (!isExtendedCraftingShapedTableEntry(entry)) {
            return;
        }
        entry.getExtendedCraftingTable().setWidth(width);
        writeExtendedCraftingTableRecipe(entry.getExtendedCraftingTable());
        loadSelectedEntryToVisualState();
        notifyChanged();
    }

    public int getExtendedCraftingTableHeight(RecipeEntry entry) {
        return entry.getExtendedCraftingTable().normalizedHeight();
    }

    public void setExtendedCraftingTableHeight(RecipeEntry entry, int height) {
        if (!isExtendedCraftingShapedTableEntry(entry)) {
            return;
        }
        entry.getExtendedCraftingTable().setHeight(height);
        writeExtendedCraftingTableRecipe(entry.getExtendedCraftingTable());
        loadSelectedEntryToVisualState();
        notifyChanged();
    }

    public int getExtendedCraftingTableTier(RecipeEntry entry) {
        var tier = entry.getExtendedCraftingTable().normalizedTier();
        return tier == 0 ? ExtendedCraftingRecipeEditorTypes.tableTierForType(entry.getType()) : tier;
    }

    public void setExtendedCraftingTableTier(RecipeEntry entry, int tier) {
        if (!isExtendedCraftingTableEntry(entry)) {
            return;
        }
        var gridSize = ExtendedCraftingRecipeEditorTypes.tableGridSizeForTier(tier);
        entry.getExtendedCraftingTable()
                .setTier(tier)
                .setWidth(gridSize)
                .setHeight(gridSize);
        if (isExtendedCraftingShapedTableEntry(entry)) {
            writeExtendedCraftingTableRecipe(entry.getExtendedCraftingTable());
        }
        loadSelectedEntryToVisualState();
        notifyChanged();
    }

    public List<Integer> extendedCraftingTableTiers() {
        return List.of(1, 2, 3, 4);
    }

    public Component extendedCraftingTableTierDisplayName(int tier) {
        var gridSize = ExtendedCraftingRecipeEditorTypes.tableGridSizeForTier(tier);
        return Component.translatable("viscript_recipe.config.extendedcrafting.table.size.value", gridSize, gridSize);
    }

    public int getAvaritiaTableWidth(RecipeEntry entry) {
        return entry.getAvaritiaTable().normalizedWidth();
    }

    public void setAvaritiaTableWidth(RecipeEntry entry, int width) {
        if (!isAvaritiaShapedTableEntry(entry)) {
            return;
        }
        entry.getAvaritiaTable().setWidth(width);
        writeAvaritiaTableRecipe(entry);
        loadSelectedEntryToVisualState();
        notifyChanged();
    }

    public int getAvaritiaTableHeight(RecipeEntry entry) {
        return entry.getAvaritiaTable().normalizedHeight();
    }

    public void setAvaritiaTableHeight(RecipeEntry entry, int height) {
        if (!isAvaritiaShapedTableEntry(entry)) {
            return;
        }
        entry.getAvaritiaTable().setHeight(height);
        writeAvaritiaTableRecipe(entry);
        loadSelectedEntryToVisualState();
        notifyChanged();
    }

    public int getAvaritiaTableTier(RecipeEntry entry) {
        var tier = entry.getAvaritiaTable().normalizedTier();
        return tier == 0 ? AvaritiaRecipeEditorTypes.tableTierForType(entry.getType()) : tier;
    }

    public void setAvaritiaTableTier(RecipeEntry entry, int tier) {
        if (!isAvaritiaTableEntry(entry)) {
            return;
        }
        entry.getAvaritiaTable().setTier(tier);
        if (isAvaritiaShapedTableEntry(entry)) {
            var gridSize = AvaritiaRecipeEditorTypes.tableGridSizeForTier(tier);
            entry.getAvaritiaTable()
                    .setWidth(gridSize)
                    .setHeight(gridSize);
            writeAvaritiaTableRecipe(entry);
        }
        loadSelectedEntryToVisualState();
        notifyChanged();
    }

    public List<Integer> avaritiaTableTiers() {
        return List.of(1, 2, 3, 4);
    }

    public Component avaritiaTableTierDisplayName(int tier) {
        return Component.translatable(
                "viscript_recipe.config.avaritia.table.size.value",
                AvaritiaRecipeEditorTypes.tableGridSizeForTier(tier),
                AvaritiaRecipeEditorTypes.tableGridSizeForTier(tier)
        );
    }

    public boolean getAvaritiaTableCompatible(RecipeEntry entry) {
        return isAvaritiaShapedTableEntry(entry) && entry.getAvaritiaTable().isCompatible();
    }

    public void setAvaritiaTableCompatible(RecipeEntry entry, boolean compatible) {
        if (!isAvaritiaShapedTableEntry(entry)) {
            return;
        }
        entry.getAvaritiaTable().setCompatible(compatible);
        notifyChanged();
    }

    public int getAvaritiaCompressorInputCount(RecipeEntry entry) {
        return Math.max(1, entry.getAvaritiaCompressor().getInputCount());
    }

    public void setAvaritiaCompressorInputCount(RecipeEntry entry, int inputCount) {
        if (!isAvaritiaCompressorEntry(entry)) {
            return;
        }
        entry.getAvaritiaCompressor().setInputCount(Math.max(1, inputCount));
        notifyChanged();
    }

    public int getAvaritiaCompressorTimeCost(RecipeEntry entry) {
        return Math.max(1, entry.getAvaritiaCompressor().getTimeCost());
    }

    public void setAvaritiaCompressorTimeCost(RecipeEntry entry, int timeCost) {
        if (!isAvaritiaCompressorEntry(entry)) {
            return;
        }
        entry.getAvaritiaCompressor().setTimeCost(Math.max(1, timeCost));
        notifyChanged();
    }

    public int getExtendedCraftingEnderCraftingTime(RecipeEntry entry) {
        return Math.max(0, entry.getExtendedCraftingEnderCrafter().getCraftingTime());
    }

    public void setExtendedCraftingEnderCraftingTime(RecipeEntry entry, int craftingTime) {
        if (!isExtendedCraftingEnderCrafterEntry(entry)) {
            return;
        }
        entry.getExtendedCraftingEnderCrafter().setCraftingTime(Math.max(0, craftingTime));
        notifyChanged();
    }

    public int getExtendedCraftingFluxPowerRequired(RecipeEntry entry) {
        return Math.max(0, entry.getExtendedCraftingFluxCrafter().getPowerRequired());
    }

    public void setExtendedCraftingFluxPowerRequired(RecipeEntry entry, int powerRequired) {
        if (!isExtendedCraftingFluxCrafterEntry(entry)) {
            return;
        }
        entry.getExtendedCraftingFluxCrafter().setPowerRequired(Math.max(0, powerRequired));
        notifyChanged();
    }

    public int getExtendedCraftingFluxPowerRate(RecipeEntry entry) {
        return Math.max(0, entry.getExtendedCraftingFluxCrafter().getPowerRate());
    }

    public void setExtendedCraftingFluxPowerRate(RecipeEntry entry, int powerRate) {
        if (!isExtendedCraftingFluxCrafterEntry(entry)) {
            return;
        }
        entry.getExtendedCraftingFluxCrafter().setPowerRate(Math.max(0, powerRate));
        notifyChanged();
    }

    public int getExtendedCraftingCombinationPowerCost(RecipeEntry entry) {
        return Math.max(0, entry.getExtendedCraftingCombination().getPowerCost());
    }

    public void setExtendedCraftingCombinationPowerCost(RecipeEntry entry, int powerCost) {
        if (!isExtendedCraftingCombinationEntry(entry)) {
            return;
        }
        entry.getExtendedCraftingCombination().setPowerCost(Math.max(0, powerCost));
        notifyChanged();
    }

    public int getExtendedCraftingCombinationPowerRate(RecipeEntry entry) {
        return Math.max(0, entry.getExtendedCraftingCombination().getPowerRate());
    }

    public void setExtendedCraftingCombinationPowerRate(RecipeEntry entry, int powerRate) {
        if (!isExtendedCraftingCombinationEntry(entry)) {
            return;
        }
        entry.getExtendedCraftingCombination().setPowerRate(Math.max(0, powerRate));
        notifyChanged();
    }

    public int getExtendedCraftingCompressorPowerCost(RecipeEntry entry) {
        return Math.max(0, entry.getExtendedCraftingCompressor().getPowerCost());
    }

    public void setExtendedCraftingCompressorPowerCost(RecipeEntry entry, int powerCost) {
        if (!isExtendedCraftingCompressorEntry(entry)) {
            return;
        }
        entry.getExtendedCraftingCompressor().setPowerCost(Math.max(0, powerCost));
        notifyChanged();
    }

    public int getExtendedCraftingCompressorPowerRate(RecipeEntry entry) {
        return Math.max(0, entry.getExtendedCraftingCompressor().getPowerRate());
    }

    public void setExtendedCraftingCompressorPowerRate(RecipeEntry entry, int powerRate) {
        if (!isExtendedCraftingCompressorEntry(entry)) {
            return;
        }
        entry.getExtendedCraftingCompressor().setPowerRate(Math.max(0, powerRate));
        notifyChanged();
    }

    public boolean isSelectedExtendedCraftingCompressorInput() {
        return selectedEntry != null
                && isExtendedCraftingCompressorEntry(selectedEntry)
                && slotSelection.kind() == WorkbenchSlotSelection.Kind.INGREDIENT
                && slotSelection.index() > 0
                && slotSelection.index() <= EXTENDED_CRAFTING_COMPRESSOR_MAX_INPUTS;
    }

    public int getSelectedExtendedCraftingCompressorInputCount() {
        if (!isSelectedExtendedCraftingCompressorInput()) {
            return 1;
        }
        return Math.max(1, getExtendedCraftingCompressorInputData(selectedEntry, slotSelection.index() - 1).getCount());
    }

    public void setSelectedExtendedCraftingCompressorInputCount(int count) {
        if (!isSelectedExtendedCraftingCompressorInput()) {
            return;
        }
        getExtendedCraftingCompressorInputData(selectedEntry, slotSelection.index() - 1).setCount(Math.max(1, count));
        saveVisualStateToSelectedEntry();
        notifyChanged();
    }

    public boolean isSelectedCreateFluidInput() {
        if (selectedEntry != null
                && isCreateSequencedAssemblyEntry(selectedEntry)) {
            if (slotSelection.kind() == WorkbenchSlotSelection.Kind.FLUID) {
                return slotSelection.index() >= 0 && slotSelection.index() < createSequencedStepCount(selectedEntry);
            }
            return slotSelection.kind() == WorkbenchSlotSelection.Kind.CREATE_SEQUENCED_STEP
                    && slotSelection.index() >= 0
                    && slotSelection.index() < createSequencedStepCount(selectedEntry)
                    && getCreateSequencedStepKind(selectedEntry, slotSelection.index()) == CreateSequencedAssemblyStepKind.FILLING;
        }
        return selectedEntry != null
                && isCreateProcessingEntry(selectedEntry)
                && slotSelection.kind() == WorkbenchSlotSelection.Kind.FLUID
                && slotSelection.index() >= 0
                && slotSelection.index() < CREATE_FLUID_OUTPUT_INDEX_OFFSET;
    }

    public boolean isSelectedCreateFluidOutput() {
        return selectedEntry != null
                && isCreateProcessingEntry(selectedEntry)
                && slotSelection.kind() == WorkbenchSlotSelection.Kind.FLUID
                && slotSelection.index() >= CREATE_FLUID_OUTPUT_INDEX_OFFSET;
    }

    public int selectedCreateFluidInputIndex() {
        if (selectedEntry != null && isCreateSequencedAssemblyEntry(selectedEntry)) {
            return Math.max(0, Math.min(CREATE_SEQUENCED_MAX_STEPS - 1, slotSelection.index()));
        }
        return Math.max(0, Math.min(CREATE_MAX_FLUID_INPUTS - 1, slotSelection.index()));
    }

    public int selectedCreateFluidOutputIndex() {
        return Math.max(0, Math.min(CREATE_MAX_FLUID_OUTPUTS - 1, slotSelection.index() - CREATE_FLUID_OUTPUT_INDEX_OFFSET));
    }

    public boolean isSelectedFarmersCuttingToolSlot() {
        return selectedEntry != null
                && isFarmersCuttingBoardEntry(selectedEntry)
                && slotSelection.kind() == WorkbenchSlotSelection.Kind.INGREDIENT
                && slotSelection.index() == 1;
    }

    public List<String> dragonForgeDragonTypes() {
        return DRAGON_FORGE_DRAGON_TYPES;
    }

    public String getDragonForgeDragonType(RecipeEntry entry) {
        return normalizeDragonType(entry.getIceAndFireDragonForge().getDragonType());
    }

    public void setDragonForgeDragonType(RecipeEntry entry, String dragonType) {
        if (!isIceAndFireDragonForgeEntry(entry)) {
            return;
        }
        entry.getIceAndFireDragonForge().setDragonType(normalizeDragonType(dragonType));
        notifyChanged();
    }

    public Component dragonForgeDragonTypeDisplayName(String dragonType) {
        return Component.translatable("viscript_recipe.editor.dragon_forge.dragon_type." + normalizeDragonType(dragonType));
    }

    public Component selectedDragonForgeDragonTypeDisplayName() {
        if (selectedEntry == null || !isIceAndFireDragonForgeEntry(selectedEntry)) {
            return dragonForgeDragonTypeDisplayName("fire");
        }
        return dragonForgeDragonTypeDisplayName(selectedEntry.getIceAndFireDragonForge().getDragonType());
    }

    public FluidStack getVisualAlchemistMiddleFluid() {
        if (selectedEntry == null || !isIronAlchemistCauldronEntry(selectedEntry)) {
            return FluidStack.EMPTY;
        }
        var data = selectedEntry.getIronAlchemistCauldron();
        if (isIronAlchemistCauldronBrewEntry(selectedEntry)) {
            return copyFluid(data.getBaseFluid());
        }
        return copyFluid(data.getFluid());
    }

    public FluidStack getVisualAlchemistResultFluid() {
        if (selectedEntry == null || !isIronAlchemistCauldronEntry(selectedEntry)) {
            return FluidStack.EMPTY;
        }
        var data = selectedEntry.getIronAlchemistCauldron();
        if (isIronAlchemistCauldronBrewEntry(selectedEntry)) {
            return data.displayResultFluid();
        }
        return FluidStack.EMPTY;
    }

    public boolean isVisualAlchemistResultFluidOutput() {
        return selectedEntry != null
                && isIronAlchemistCauldronBrewEntry(selectedEntry)
                && !getVisualAlchemistResultFluid().isEmpty();
    }

    public Component alchemistMiddleFluidLabel() {
        if (selectedEntry != null && isIronAlchemistCauldronFillEntry(selectedEntry)) {
            return Component.translatable("viscript_recipe.editor.alchemist_cauldron.result_fluid");
        }
        if (selectedEntry != null && isIronAlchemistCauldronBrewEntry(selectedEntry)) {
            return Component.translatable("viscript_recipe.editor.alchemist_cauldron.base_fluid");
        }
        return Component.translatable("viscript_recipe.editor.alchemist_cauldron.input_fluid");
    }

    public Component alchemistResultLabel() {
        return Component.translatable("viscript_recipe.editor.alchemist_cauldron.result_item");
    }

    public void setVisualAlchemistFluid(int index, FluidStack stack) {
        if (selectedEntry == null || !isIronAlchemistCauldronEntry(selectedEntry)) {
            return;
        }
        setAlchemistFluid(selectedEntry, index, stack);
        notifyChanged();
    }

    public void clearVisualAlchemistFluid(int index) {
        setVisualAlchemistFluid(index, FluidStack.EMPTY);
    }

    public float getCookingExperience(RecipeEntry entry) {
        return Math.max(0, Math.min(Integer.MAX_VALUE, entry.getCooking().getExperience()));
    }

    public void setCookingExperience(RecipeEntry entry, float experience) {
        entry.getCooking().setExperience(Math.max(0, Math.min(Integer.MAX_VALUE, experience)));
        notifyChanged();
    }

    public int getCookingTime(RecipeEntry entry) {
        return Math.max(1, entry.getCooking().getCookingTime());
    }

    public void setCookingTime(RecipeEntry entry, int cookingTime) {
        entry.getCooking().setCookingTime(Math.max(1, cookingTime));
        notifyChanged();
    }

    public float getFarmersCookingExperience(RecipeEntry entry) {
        return Math.max(0, Math.min(Integer.MAX_VALUE, entry.getFarmerCookingPot().getExperience()));
    }

    public void setFarmersCookingExperience(RecipeEntry entry, float experience) {
        entry.getFarmerCookingPot().setExperience(Math.max(0, Math.min(Integer.MAX_VALUE, experience)));
        notifyChanged();
    }

    public int getFarmersCookingTime(RecipeEntry entry) {
        return Math.max(1, entry.getFarmerCookingPot().getCookingTime());
    }

    public void setFarmersCookingTime(RecipeEntry entry, int cookingTime) {
        entry.getFarmerCookingPot().setCookingTime(Math.max(1, cookingTime));
        notifyChanged();
    }

    public boolean getFarmersCuttingCustomSound(RecipeEntry entry) {
        return entry.getFarmerCuttingBoard().isCustomSound();
    }

    public void setFarmersCuttingCustomSound(RecipeEntry entry, boolean customSound) {
        entry.getFarmerCuttingBoard().setCustomSound(customSound);
        notifyChanged();
    }

    public ResourceLocation getFarmersCuttingSound(RecipeEntry entry) {
        var sound = entry.getFarmerCuttingBoard().getSound();
        return sound == null ? new ResourceLocation("item.axe.strip") : sound;
    }

    public void setFarmersCuttingSound(RecipeEntry entry, ResourceLocation sound) {
        entry.getFarmerCuttingBoard().setSound(sound == null ? new ResourceLocation("item.axe.strip") : sound);
        notifyChanged();
    }

    public int getCreateProcessingTime(RecipeEntry entry) {
        return Math.max(0, entry.getCreateProcessing().getProcessingTime());
    }

    public void setCreateProcessingTime(RecipeEntry entry, int processingTime) {
        if (!isCreateProcessingEntry(entry)) {
            return;
        }
        entry.getCreateProcessing().setProcessingTime(Math.max(0, processingTime));
        notifyChanged();
    }

    public CreateHeatCondition getCreateHeatRequirement(RecipeEntry entry) {
        var condition = entry.getCreateProcessing().getHeatRequirement();
        return condition == null ? CreateHeatCondition.NONE : condition;
    }

    public void setCreateHeatRequirement(RecipeEntry entry, CreateHeatCondition heatCondition) {
        if (!isCreateProcessingEntry(entry)) {
            return;
        }
        entry.getCreateProcessing().setHeatRequirement(heatCondition == null ? CreateHeatCondition.NONE : heatCondition);
        notifyChanged();
    }

    public boolean getCreateKeepHeldItem(RecipeEntry entry) {
        return entry.getCreateProcessing().isKeepHeldItem();
    }

    public void setCreateKeepHeldItem(RecipeEntry entry, boolean keepHeldItem) {
        if (!isCreateProcessingEntry(entry)) {
            return;
        }
        entry.getCreateProcessing().setKeepHeldItem(keepHeldItem);
        notifyChanged();
    }

    public boolean selectedCreateDurationAllowed() {
        return selectedCreateKind().map(CreateProcessingKind::durationAllowed).orElse(false);
    }

    public boolean selectedCreateHeatAllowed() {
        return selectedCreateKind().map(CreateProcessingKind::heatAllowed).orElse(false);
    }

    public boolean selectedCreateKeepHeldItemAllowed() {
        return selectedCreateKind().map(CreateProcessingKind::keepHeldItemAllowed).orElse(false);
    }

    public boolean selectedCreateOutputChanceAllowed() {
        if (selectedEntry != null && isCreateSequencedAssemblyEntry(selectedEntry)) {
            return true;
        }
        return selectedCreateKind()
                .map(kind -> kind != CreateProcessingKind.BLOCK_CUTTING
                        && kind != CreateProcessingKind.BLASTING
                        && kind != CreateProcessingKind.SMOKING)
                .orElse(false);
    }

    public List<Integer> createAutoPackingGridSizes() {
        return List.of(2, 3);
    }

    public int getCreateAutoPackingGridSize(RecipeEntry entry) {
        if (!isCreateAutoPackingEntry(entry)) {
            return 3;
        }
        return autoPackingGridSize(entry.getCreateProcessing());
    }

    public void setCreateAutoPackingGridSize(RecipeEntry entry, int gridSize) {
        if (!isCreateAutoPackingEntry(entry)) {
            return;
        }
        var normalized = gridSize <= 2 ? 2 : 3;
        var ingredient = firstCreateIngredient(entry.getCreateProcessing());
        if (isIngredientEmpty(ingredient)) {
            ingredient = RecipeIngredient.item(CreateProcessingKind.AUTO_PACKING.defaultInput());
        }
        writeRepeatedAutoPackingIngredient(entry, ingredient, normalized);
        notifyChanged();
    }

    public Component createAutoPackingGridSizeDisplayName(int gridSize) {
        return Component.translatable("viscript_recipe.editor.create.auto_packing.grid_" + (gridSize <= 2 ? 2 : 3));
    }

    public Component createHeatDisplayName(CreateHeatCondition condition) {
        var value = condition == null ? CreateHeatCondition.NONE : condition;
        return Component.translatable("viscript_recipe.editor.create.heat." + value.getSerializedName());
    }

    public ItemStack getSelectedContainer() {
        if (selectedEntry == null || !isFarmersCookingPotEntry(selectedEntry)) {
            return ItemStack.EMPTY;
        }
        return selectedEntry.getFarmerCookingPot().getContainer() == null
                ? ItemStack.EMPTY
                : selectedEntry.getFarmerCookingPot().getContainer().copy();
    }

    public void setSelectedContainer(ItemStack stack) {
        if (selectedEntry == null || !isFarmersCookingPotEntry(selectedEntry)) {
            return;
        }
        selectedEntry.getFarmerCookingPot().setContainer(stack == null ? ItemStack.EMPTY : stack.copy());
        visualContainer = selectedEntry.getFarmerCookingPot().getContainer().copy();
        notifyChanged();
    }

    public ItemStack getSelectedCuttingResult() {
        if (selectedEntry == null || !isFarmersCuttingBoardEntry(selectedEntry) || slotSelection.kind() != WorkbenchSlotSelection.Kind.CUTTING_RESULT) {
            return ItemStack.EMPTY;
        }
        return getVisualCuttingResult(slotSelection.index());
    }

    public void setSelectedCuttingResult(ItemStack stack) {
        if (slotSelection.kind() == WorkbenchSlotSelection.Kind.CUTTING_RESULT) {
            setVisualCuttingResult(slotSelection.index(), stack);
        }
    }

    public float getSelectedCuttingChance() {
        if (selectedEntry == null || !isFarmersCuttingBoardEntry(selectedEntry) || slotSelection.kind() != WorkbenchSlotSelection.Kind.CUTTING_RESULT) {
            return 1.0F;
        }
        var index = slotSelection.index();
        if (index < 0 || index >= visualCuttingChances.length) {
            return 1.0F;
        }
        return Math.max(0, Math.min(1, visualCuttingChances[index]));
    }

    public void setSelectedCuttingChance(float chance) {
        if (selectedEntry == null || !isFarmersCuttingBoardEntry(selectedEntry) || slotSelection.kind() != WorkbenchSlotSelection.Kind.CUTTING_RESULT) {
            return;
        }
        var index = slotSelection.index();
        if (index < 0 || index >= visualCuttingChances.length) {
            return;
        }
        visualCuttingChances[index] = Math.max(0, Math.min(1, chance));
        saveVisualStateToSelectedEntry();
        notifyChanged();
    }

    public ItemStack getSelectedCreateOutput() {
        if (selectedEntry == null || (!isCreateProcessingEntry(selectedEntry) && !isCreateSequencedAssemblyEntry(selectedEntry)) || slotSelection.kind() != WorkbenchSlotSelection.Kind.CREATE_RESULT) {
            return ItemStack.EMPTY;
        }
        return getVisualCreateOutput(slotSelection.index());
    }

    public void setSelectedCreateOutput(ItemStack stack) {
        if (slotSelection.kind() == WorkbenchSlotSelection.Kind.CREATE_RESULT) {
            setVisualCreateOutput(slotSelection.index(), stack);
        }
    }

    public ItemStack getSelectedArsNouveauOutput() {
        if (selectedEntry == null || !isArsNouveauCrushEntry(selectedEntry) || slotSelection.kind() != WorkbenchSlotSelection.Kind.ARS_NOUVEAU_OUTPUT) {
            return ItemStack.EMPTY;
        }
        return getVisualArsNouveauOutput(slotSelection.index());
    }

    public void setSelectedArsNouveauOutput(ItemStack stack) {
        if (slotSelection.kind() == WorkbenchSlotSelection.Kind.ARS_NOUVEAU_OUTPUT) {
            setVisualArsNouveauOutput(slotSelection.index(), stack);
        }
    }

    public float getSelectedArsNouveauOutputChance() {
        if (selectedEntry == null || !isArsNouveauCrushEntry(selectedEntry) || slotSelection.kind() != WorkbenchSlotSelection.Kind.ARS_NOUVEAU_OUTPUT) {
            return 1.0F;
        }
        var index = slotSelection.index();
        if (index < 0 || index >= visualArsNouveauOutputChances.length) {
            return 1.0F;
        }
        return Math.max(0, Math.min(1, visualArsNouveauOutputChances[index]));
    }

    public void setSelectedArsNouveauOutputChance(float chance) {
        if (selectedEntry == null || !isArsNouveauCrushEntry(selectedEntry) || slotSelection.kind() != WorkbenchSlotSelection.Kind.ARS_NOUVEAU_OUTPUT) {
            return;
        }
        var index = slotSelection.index();
        if (index < 0 || index >= visualArsNouveauOutputChances.length) {
            return;
        }
        visualArsNouveauOutputChances[index] = Math.max(0, Math.min(1, chance));
        saveVisualStateToSelectedEntry();
        notifyChanged();
    }

    public int getSelectedArsNouveauOutputMaxRange() {
        if (selectedEntry == null || !isArsNouveauCrushEntry(selectedEntry) || slotSelection.kind() != WorkbenchSlotSelection.Kind.ARS_NOUVEAU_OUTPUT) {
            return 1;
        }
        var index = slotSelection.index();
        if (index < 0 || index >= visualArsNouveauOutputMaxRanges.length) {
            return 1;
        }
        return Math.max(1, visualArsNouveauOutputMaxRanges[index]);
    }

    public void setSelectedArsNouveauOutputMaxRange(int maxRange) {
        if (selectedEntry == null || !isArsNouveauCrushEntry(selectedEntry) || slotSelection.kind() != WorkbenchSlotSelection.Kind.ARS_NOUVEAU_OUTPUT) {
            return;
        }
        var index = slotSelection.index();
        if (index < 0 || index >= visualArsNouveauOutputMaxRanges.length) {
            return;
        }
        visualArsNouveauOutputMaxRanges[index] = Math.max(1, maxRange);
        saveVisualStateToSelectedEntry();
        notifyChanged();
    }

    public float getSelectedCreateOutputChance() {
        if (selectedEntry == null || (!isCreateProcessingEntry(selectedEntry) && !isCreateSequencedAssemblyEntry(selectedEntry)) || slotSelection.kind() != WorkbenchSlotSelection.Kind.CREATE_RESULT) {
            return 1.0F;
        }
        var index = slotSelection.index();
        if (index < 0 || index >= visualCreateOutputChances.length) {
            return 1.0F;
        }
        return isCreateSequencedAssemblyEntry(selectedEntry)
                ? Math.max(0, visualCreateOutputChances[index])
                : Math.max(0, Math.min(1, visualCreateOutputChances[index]));
    }

    public void setSelectedCreateOutputChance(float chance) {
        if (selectedEntry == null || (!isCreateProcessingEntry(selectedEntry) && !isCreateSequencedAssemblyEntry(selectedEntry)) || slotSelection.kind() != WorkbenchSlotSelection.Kind.CREATE_RESULT) {
            return;
        }
        var index = slotSelection.index();
        if (index < 0 || index >= visualCreateOutputChances.length) {
            return;
        }
        visualCreateOutputChances[index] = isCreateSequencedAssemblyEntry(selectedEntry)
                ? Math.max(0, chance)
                : Math.max(0, Math.min(1, chance));
        saveVisualStateToSelectedEntry();
        notifyChanged();
    }

    public CreateFluidIngredientData getSelectedCreateFluidIngredient() {
        if (!isSelectedCreateFluidInput()) {
            return CreateFluidIngredientData.empty();
        }
        if (selectedEntry != null && isCreateSequencedAssemblyEntry(selectedEntry)) {
            return getCreateSequencedStepFluidIngredient(selectedCreateFluidInputIndex());
        }
        return getVisualCreateFluidInput(selectedCreateFluidInputIndex());
    }

    public void setSelectedCreateFluidIngredient(CreateFluidIngredientData ingredient) {
        if (!isSelectedCreateFluidInput()) {
            return;
        }
        setVisualCreateFluidInput(selectedCreateFluidInputIndex(), ingredient);
    }

    public void setSelectedCreateFluidIngredientKind(CreateFluidIngredientKind kind) {
        if (!isSelectedCreateFluidInput()) {
            return;
        }
        var ingredient = getSelectedCreateFluidIngredient();
        var resolvedKind = kind == null ? CreateFluidIngredientKind.FLUID : kind;
        ingredient.setKind(resolvedKind);
        if (resolvedKind == CreateFluidIngredientKind.TAG) {
            if (ingredient.getTag() == null) {
                ingredient.setTag(new ResourceLocation("c", "milk"));
            }
            if (ingredient.getAmount() <= 0) {
                ingredient.setAmount(1000);
            }
        }
        setSelectedCreateFluidIngredient(ingredient);
    }

    public void setSelectedCreateFluidIngredientTag(ResourceLocation tag) {
        if (!isSelectedCreateFluidInput()) {
            return;
        }
        var ingredient = getSelectedCreateFluidIngredient();
        ingredient.setKind(CreateFluidIngredientKind.TAG);
        ingredient.setTag(tag == null ? new ResourceLocation("c", "milk") : tag);
        if (ingredient.getAmount() <= 0) {
            ingredient.setAmount(1000);
        }
        setSelectedCreateFluidIngredient(ingredient);
    }

    public void setSelectedCreateFluidIngredientAmount(int amount) {
        if (!isSelectedCreateFluidInput()) {
            return;
        }
        var ingredient = getSelectedCreateFluidIngredient();
        ingredient.setAmount(Math.max(1, amount));
        if (ingredient.getFluid() != null && !ingredient.getFluid().isEmpty()) {
            ingredient.setFluid(ingredient.getFluid().copy());
        }
        setSelectedCreateFluidIngredient(ingredient);
    }

    public List<IngredientValueKind> availableIngredientKindsForSelectedSlot() {
        return isSelectedFarmersCuttingToolSlot()
                ? List.of(IngredientValueKind.ITEM, IngredientValueKind.TAG, IngredientValueKind.ITEM_ABILITY)
                : List.of(IngredientValueKind.ITEM, IngredientValueKind.TAG);
    }

    public List<String> itemAbilityChoices() {
        return ITEM_ABILITY_CHOICES;
    }

    public Component itemAbilityDisplayName(String itemAbility) {
        var key = itemAbility == null ? "knife_dig" : itemAbility;
        return Component.translatable("viscript_recipe.editor.item_ability." + key);
    }

    public Component selectedKaleidoscopePotStirFryLabel() {
        var count = selectedEntry == null || !isKaleidoscopePotEntry(selectedEntry)
                ? 0
                : Math.max(0, selectedEntry.getKaleidoscopePot().getStirFryCount());
        return Component.translatable("jei.kaleidoscope_cookery.pot.stir_fry_count", count);
    }

    public Component selectedKaleidoscopeTeapotTimeLabel() {
        var time = selectedEntry == null || !isKaleidoscopeTeapotEntry(selectedEntry)
                ? 0
                : Math.max(1, selectedEntry.getKaleidoscopeTeapot().getTime()) / 20;
        return Component.translatable("jei.kaleidoscope_cookery.teapot.time", time);
    }

    public int selectedKaleidoscopeTeapotIngredientCount() {
        return selectedEntry == null || !isKaleidoscopeTeapotEntry(selectedEntry)
                ? 1
                : Math.max(1, selectedEntry.getKaleidoscopeTeapot().getIngredientCount());
    }

    public ItemStack getVisualKaleidoscopeTeapotFluidBucket() {
        if (selectedEntry == null || !isKaleidoscopeTeapotEntry(selectedEntry)) {
            return new ItemStack(Items.WATER_BUCKET);
        }
        var fluidId = selectedEntry.getKaleidoscopeTeapot().getTeaFluid();
        var fluid = fluidId == null ? Fluids.WATER : BuiltInRegistries.FLUID.get(fluidId);
        var bucket = fluid == null || fluid == Fluids.EMPTY ? Items.WATER_BUCKET : fluid.getBucket();
        return bucket == Items.AIR ? ItemStack.EMPTY : new ItemStack(bucket);
    }

    public void setVisualKaleidoscopeTeapotFluidBucket(ItemStack stack) {
        if (selectedEntry == null || !isKaleidoscopeTeapotEntry(selectedEntry)) {
            return;
        }
        var fluidId = fluidIdFromBucket(stack);
        if (fluidId == null) {
            return;
        }
        selectedEntry.getKaleidoscopeTeapot().setTeaFluid(fluidId);
        notifyChanged();
    }

    public void resetKaleidoscopeTeapotFluidBucket() {
        if (selectedEntry == null || !isKaleidoscopeTeapotEntry(selectedEntry)) {
            return;
        }
        selectedEntry.getKaleidoscopeTeapot().setTeaFluid(new ResourceLocation("water"));
        notifyChanged();
    }

    public int getDragonForgeCookTime(RecipeEntry entry) {
        return Math.max(1, entry.getIceAndFireDragonForge().getCookTime());
    }

    public void setDragonForgeCookTime(RecipeEntry entry, int cookTime) {
        if (!isIceAndFireDragonForgeEntry(entry)) {
            return;
        }
        entry.getIceAndFireDragonForge().setCookTime(Math.max(1, cookTime));
        notifyChanged();
    }

    public int getCataclysmAmethystBlessTime(RecipeEntry entry) {
        return Math.max(1, entry.getCataclysmAmethystBless().getTime());
    }

    public void setCataclysmAmethystBlessTime(RecipeEntry entry, int time) {
        if (!isCataclysmAmethystBlessEntry(entry)) {
            return;
        }
        entry.getCataclysmAmethystBless().setTime(Math.max(1, time));
        notifyChanged();
    }

    private void saveVisualStateToSelectedEntry() {
        if (refreshing || selectedEntry == null) {
            return;
        }
        if (selectedContainsUnsupportedIngredients && !ingredientSlotsChanged() && !remainderSlotsChanged()) {
            writeResultOnly();
            return;
        }
        if (selectedEntry.isType(RecipeEditorTypes.CRAFTING_SHAPED)) {
            writeShapedRecipe(selectedEntry.getShaped());
        } else if (selectedEntry.isType(RecipeEditorTypes.CRAFTING_SHAPELESS)) {
            writeShapelessRecipe(selectedEntry.getShapeless());
        } else if (isCookingEntry(selectedEntry)) {
            writeCookingRecipe(selectedEntry.getCooking());
        } else if (isStonecuttingEntry(selectedEntry)) {
            writeStonecuttingRecipe(selectedEntry.getStonecutting());
        } else if (isSmithingTransformEntry(selectedEntry)) {
            writeSmithingTransformRecipe(selectedEntry.getSmithingTransform());
        } else if (isIronAlchemistCauldronEntry(selectedEntry)) {
            writeIronAlchemistCauldronRecipe(selectedEntry);
        } else if (isIronArcaneAnvilEntry(selectedEntry)) {
            writeIronArcaneAnvilRecipe(selectedEntry.getIronArcaneAnvil());
        } else if (isIronNoAdditionSmithingEntry(selectedEntry)) {
            writeIronNoAdditionSmithingRecipe(selectedEntry.getIronNoAdditionSmithing());
        } else if (isIceAndFireDragonForgeEntry(selectedEntry)) {
            writeDragonForgeRecipe(selectedEntry.getIceAndFireDragonForge());
        } else if (isCataclysmWeaponFusionEntry(selectedEntry)) {
            writeCataclysmWeaponFusionRecipe(selectedEntry.getCataclysmWeaponFusion());
        } else if (isCataclysmAmethystBlessEntry(selectedEntry)) {
            writeCataclysmAmethystBlessRecipe(selectedEntry.getCataclysmAmethystBless());
        } else if (isTouhouLittleMaidAltarEntry(selectedEntry)) {
            writeTouhouLittleMaidAltarRecipe(selectedEntry.getTouhouLittleMaidAltar());
        } else if (isSporeSurgeryEntry(selectedEntry)) {
            writeSporeSurgeryRecipe(selectedEntry.getSporeSurgery());
        } else if (isSporeGraftingEntry(selectedEntry)) {
            writeSporeGraftingRecipe(selectedEntry.getSporeGrafting());
        } else if (isGoetyCursedInfuserEntry(selectedEntry)) {
            writeGoetyCursedInfuserRecipe(selectedEntry.getGoetyCursedInfuser());
        } else if (isGoetyRitualEntry(selectedEntry)) {
            writeGoetyRitualRecipe(selectedEntry.getGoetyRitual());
        } else if (isGoetyBrazierEntry(selectedEntry)) {
            writeGoetyBrazierRecipe(selectedEntry.getGoetyBrazier());
        } else if (isGoetyPulverizeEntry(selectedEntry)) {
            writeGoetyPulverizeRecipe(selectedEntry.getGoetyPulverize());
        } else if (isGoetyBrewingEntry(selectedEntry)) {
            writeGoetyBrewingRecipe(selectedEntry.getGoetyBrewing());
        } else if (isFarmersCookingPotEntry(selectedEntry)) {
            writeFarmerCookingPotRecipe(selectedEntry.getFarmerCookingPot());
        } else if (isFarmersCuttingBoardEntry(selectedEntry)) {
            writeFarmerCuttingRecipe(selectedEntry.getFarmerCuttingBoard());
        } else if (isKaleidoscopePotEntry(selectedEntry)) {
            writeKaleidoscopePotRecipe(selectedEntry.getKaleidoscopePot());
        } else if (isKaleidoscopeStockpotEntry(selectedEntry)) {
            writeKaleidoscopeStockpotRecipe(selectedEntry.getKaleidoscopeStockpot());
        } else if (isKaleidoscopeMillstoneEntry(selectedEntry)) {
            writeKaleidoscopeMillstoneRecipe(selectedEntry.getKaleidoscopeMillstone());
        } else if (isKaleidoscopeChoppingBoardEntry(selectedEntry)) {
            writeKaleidoscopeChoppingBoardRecipe(selectedEntry.getKaleidoscopeChoppingBoard());
        } else if (isKaleidoscopeSteamerEntry(selectedEntry)) {
            writeKaleidoscopeSteamerRecipe(selectedEntry.getKaleidoscopeSteamer());
        } else if (isKaleidoscopeTeapotEntry(selectedEntry)) {
            writeKaleidoscopeTeapotRecipe(selectedEntry.getKaleidoscopeTeapot());
        } else if (isCreateMechanicalCraftingEntry(selectedEntry)) {
            writeCreateMechanicalCraftingRecipe(selectedEntry.getCreateMechanicalCrafting());
        } else if (isCreateSequencedAssemblyEntry(selectedEntry)) {
            writeCreateSequencedAssemblyRecipe(selectedEntry.getCreateSequencedAssembly());
        } else if (isCreateProcessingEntry(selectedEntry)) {
            writeCreateProcessingRecipe(selectedEntry);
        } else if (isExtendedCraftingTableEntry(selectedEntry)) {
            writeExtendedCraftingTableRecipe(selectedEntry.getExtendedCraftingTable());
        } else if (isExtendedCraftingUltimateSingularityEntry(selectedEntry)) {
            selectedEntry.getExtendedCraftingUltimateSingularity().setResult(visualResult.copy());
        } else if (isExtendedCraftingEnderCrafterEntry(selectedEntry)) {
            writeExtendedCraftingEnderCrafterRecipe(selectedEntry);
        } else if (isExtendedCraftingFluxCrafterEntry(selectedEntry)) {
            writeExtendedCraftingFluxCrafterRecipe(selectedEntry);
        } else if (isExtendedCraftingCombinationEntry(selectedEntry)) {
            writeExtendedCraftingCombinationRecipe(selectedEntry.getExtendedCraftingCombination());
        } else if (isExtendedCraftingCompressorEntry(selectedEntry)) {
            writeExtendedCraftingCompressorRecipe(selectedEntry.getExtendedCraftingCompressor());
        } else if (isAvaritiaTableEntry(selectedEntry)) {
            writeAvaritiaTableRecipe(selectedEntry);
        } else if (isAvaritiaCompressorEntry(selectedEntry)) {
            writeAvaritiaCompressorRecipe(selectedEntry.getAvaritiaCompressor());
        } else if (isAvaritiaExtremeSmithingEntry(selectedEntry)) {
            writeAvaritiaExtremeSmithingRecipe(selectedEntry);
        } else if (isArsNouveauApparatusEntry(selectedEntry)) {
            writeArsNouveauApparatusRecipe(selectedEntry.getArsNouveauApparatus());
        } else if (isArsNouveauArmorUpgradeEntry(selectedEntry)) {
            writeArsNouveauArmorUpgradeRecipe(selectedEntry.getArsNouveauArmorUpgrade());
        } else if (isArsNouveauEnchantmentEntry(selectedEntry)) {
            writeArsNouveauEnchantmentRecipe(selectedEntry.getArsNouveauEnchantment());
        } else if (isArsNouveauImbuementEntry(selectedEntry)) {
            writeArsNouveauImbuementRecipe(selectedEntry.getArsNouveauImbuement());
        } else if (isArsNouveauGlyphEntry(selectedEntry)) {
            writeArsNouveauGlyphRecipe(selectedEntry.getArsNouveauGlyph());
        } else if (isArsNouveauCrushEntry(selectedEntry)) {
            writeArsNouveauCrushRecipe(selectedEntry.getArsNouveauCrush());
        } else if (isArsNouveauPedestalOnlyEntry(selectedEntry)) {
            writeArsNouveauPedestalOnlyRecipe(selectedEntry.getArsNouveauPedestalOnly());
        }
        selectedContainsUnsupportedIngredients = false;
        loadedIngredientStacks = copyStacks(visualIngredients);
        loadedRemainders = copyRemainders(visualRemainders);
    }

    private void writeResultOnly() {
        if (selectedEntry != null) {
            setResult(selectedEntry, visualResult.copy());
        }
    }

    private void writeShapedRecipe(ShapedCraftingRecipeData shaped) {
        var itemSymbols = new LinkedHashMap<String, Character>();
        var keyEntries = new ArrayList<ShapedKeyEntry>();
        var pattern = new ArrayList<String>();
        var remainders = new ArrayList<CraftingRemainderRule>();
        var symbolIndex = 0;
        for (int row = 0; row < 3; row++) {
            var builder = new StringBuilder();
            for (int col = 0; col < 3; col++) {
                var slot = row * 3 + col;
                var stack = visualIngredients[slot];
                var ingredient = ingredientForVisualSlot(slot);
                if (stack.isEmpty() && isIngredientEmpty(ingredient)) {
                    builder.append(' ');
                    remainders.add(CraftingRemainderRule.defaultRule());
                    continue;
                }
                var ingredientKey = ingredientKey(ingredient, stack, slot);
                var symbol = itemSymbols.get(ingredientKey);
                if (symbol == null) {
                    if (symbolIndex >= SHAPED_SYMBOLS.length) {
                        continue;
                    }
                    symbol = SHAPED_SYMBOLS[symbolIndex++];
                    itemSymbols.put(ingredientKey, symbol);
                    keyEntries.add(ShapedKeyEntry.of(String.valueOf(symbol), ingredient));
                }
                builder.append(symbol);
                remainders.add(visualRemainders[slot] == null ? CraftingRemainderRule.defaultRule() : visualRemainders[slot].copy());
            }
            pattern.add(builder.toString());
        }
        if (keyEntries.isEmpty()) {
            shaped.setPattern(new ArrayList<>());
            shaped.setKey(new ArrayList<>());
            shaped.setRemainders(new ArrayList<>());
            shaped.setResult(visualResult.copy());
            return;
        }
        shaped.setPattern(pattern);
        shaped.setKey(keyEntries);
        shaped.setRemainders(remainders);
        shaped.setResult(visualResult.copy());
    }

    private void writeCreateMechanicalCraftingRecipe(CreateMechanicalCraftingRecipeData data) {
        var itemSymbols = new LinkedHashMap<String, Character>();
        var keyEntries = new ArrayList<ShapedKeyEntry>();
        var pattern = new ArrayList<String>();
        var symbolIndex = 0;
        var width = data.normalizedWidth();
        var height = data.normalizedHeight();
        for (int row = 0; row < height; row++) {
            var builder = new StringBuilder();
            for (int col = 0; col < width; col++) {
                var slot = row * MECHANICAL_CRAFTING_GRID_SIZE + col;
                var stack = visualIngredients[slot];
                var ingredient = ingredientForVisualSlot(slot);
                if (stack.isEmpty() && isIngredientEmpty(ingredient)) {
                    builder.append(' ');
                    continue;
                }
                var ingredientKey = ingredientKey(ingredient, stack, slot);
                var symbol = itemSymbols.get(ingredientKey);
                if (symbol == null) {
                    if (symbolIndex >= SHAPED_SYMBOLS.length) {
                        builder.append(' ');
                        continue;
                    }
                    symbol = SHAPED_SYMBOLS[symbolIndex++];
                    itemSymbols.put(ingredientKey, symbol);
                    keyEntries.add(ShapedKeyEntry.of(String.valueOf(symbol), ingredient));
                }
                builder.append(symbol);
            }
            pattern.add(builder.toString());
        }
        if (keyEntries.isEmpty()) {
            data.setPattern(new ArrayList<>());
            data.setKey(new ArrayList<>());
            data.setResult(visualResult.copy());
            return;
        }
        data.setPattern(pattern);
        data.setKey(keyEntries);
        data.setResult(visualResult.copy());
    }

    private void writeShapelessRecipe(ShapelessCraftingRecipeData shapeless) {
        var ingredients = new ArrayList<RecipeIngredient>();
        for (int i = 0; i < CRAFTING_GRID_SLOT_COUNT; i++) {
            var ingredient = ingredientForVisualSlot(i);
            if (!isIngredientEmpty(ingredient)) {
                ingredients.add(ingredient);
            }
        }
        shapeless.setIngredients(ingredients);
        shapeless.setResult(visualResult.copy());
    }

    private void writeCookingRecipe(CookingRecipeData cooking) {
        cooking.setIngredient(ingredientForVisualSlot(0));
        cooking.setResult(visualResult.copy());
    }

    private void writeStonecuttingRecipe(StonecuttingRecipeData stonecutting) {
        stonecutting.setIngredient(ingredientForVisualSlot(0));
        stonecutting.setResult(visualResult.copy());
    }

    private void writeSmithingTransformRecipe(SmithingTransformRecipeData smithingTransform) {
        smithingTransform.setTemplate(ingredientForVisualSlot(0));
        smithingTransform.setBase(ingredientForVisualSlot(1));
        smithingTransform.setAddition(ingredientForVisualSlot(2));
        smithingTransform.setResult(visualResult.copy());
    }

    private void writeIronAlchemistCauldronRecipe(RecipeEntry entry) {
        var data = entry.getIronAlchemistCauldron();
        data.setInput(ingredientForVisualSlot(0));
        if (isIronAlchemistCauldronBrewEntry(entry)) {
            data.setByproduct(ItemStack.EMPTY);
        } else {
            data.setResult(visualResult.copy());
        }
    }

    private FluidStack getAlchemistFluid(RecipeEntry entry, int index) {
        if (!isIronAlchemistCauldronEntry(entry)) {
            return FluidStack.EMPTY;
        }
        var data = entry.getIronAlchemistCauldron();
        if (isIronAlchemistCauldronBrewEntry(entry)) {
            return index == 1 ? data.firstResultFluid() : copyFluid(data.getBaseFluid());
        }
        return index == 0 ? copyFluid(data.getFluid()) : FluidStack.EMPTY;
    }

    private void setAlchemistFluid(RecipeEntry entry, int index, FluidStack stack) {
        if (!isIronAlchemistCauldronEntry(entry)) {
            return;
        }
        var copy = copyFluid(stack);
        var data = entry.getIronAlchemistCauldron();
        if (isIronAlchemistCauldronBrewEntry(entry)) {
            if (index == 1) {
                data.setFirstResultFluid(copy);
            } else {
                data.setBaseFluid(copy);
            }
            return;
        }
        if (index == 0) {
            data.setFluid(copy);
        }
    }

    private String alchemistFluidConfigNameKey(RecipeEntry entry, int index) {
        if (isIronAlchemistCauldronBrewEntry(entry)) {
            return index == 1
                    ? "viscript_recipe.config.irons_spellbooks.alchemist_cauldron.result_fluid"
                    : "viscript_recipe.config.irons_spellbooks.alchemist_cauldron.base_fluid";
        }
        return "viscript_recipe.config.irons_spellbooks.alchemist_cauldron.fluid";
    }

    private void writeIronNoAdditionSmithingRecipe(IronNoAdditionSmithingRecipeData smithing) {
        smithing.setTemplate(ingredientForVisualSlot(0));
        smithing.setBase(ingredientForVisualSlot(1));
        smithing.setResult(visualResult.copy());
    }

    private void writeIronArcaneAnvilRecipe(IronArcaneAnvilRecipeData arcaneAnvil) {
        arcaneAnvil.setInput(ingredientForVisualSlot(0));
        arcaneAnvil.setMaterial(ingredientForVisualSlot(1));
        arcaneAnvil.setResult(visualResult.copy());
    }

    private void writeDragonForgeRecipe(DragonForgeRecipeData dragonForge) {
        dragonForge.setInput(ingredientForVisualSlot(0));
        dragonForge.setBlood(ingredientForVisualSlot(1));
        dragonForge.setResult(visualResult.copy());
    }

    private void writeCataclysmWeaponFusionRecipe(CataclysmWeaponFusionRecipeData data) {
        data.setBase(ingredientForVisualSlot(0));
        data.setAddition(ingredientForVisualSlot(1));
        data.setResult(visualResult.copy());
    }

    private void writeCataclysmAmethystBlessRecipe(CataclysmAmethystBlessRecipeData data) {
        data.setIngredient(ingredientForVisualSlot(0));
        data.setResult(visualResult.copy());
    }

    private void writeTouhouLittleMaidAltarRecipe(TouhouLittleMaidAltarRecipeData data) {
        for (int index = 0; index < TouhouLittleMaidAltarRecipeData.INPUT_COUNT; index++) {
            data.setIngredient(index, ingredientForVisualSlot(index));
        }
        data.setResult(visualResult.copy());
    }

    private void writeSporeSurgeryRecipe(SporeSurgeryRecipeData surgery) {
        for (int i = 0; i < SporeSurgeryRecipeData.INPUT_COUNT; i++) {
            surgery.setIngredient(i, ingredientForVisualSlot(i));
        }
        surgery.setResult(visualResult.copy());
    }

    private void writeSporeGraftingRecipe(SporeGraftingRecipeData grafting) {
        for (int i = 0; i < SporeGraftingRecipeData.INPUT_COUNT; i++) {
            grafting.setIngredient(i, ingredientForVisualSlot(i));
        }
        grafting.setResult(visualResult.copy());
    }

    private void writeGoetyCursedInfuserRecipe(GoetyCursedInfuserRecipeData data) {
        data.setIngredient(ingredientForVisualSlot(0));
        data.setResult(visualResult.copy());
    }

    private void writeGoetyRitualRecipe(GoetyRitualRecipeData data) {
        data.setActivationItem(ingredientForVisualSlot(0));
        for (int i = 0; i < GoetyRitualRecipeData.MAX_PEDESTAL_INGREDIENTS; i++) {
            data.setIngredient(i, ingredientForVisualSlot(i + 1));
        }
        data.setResult(visualResult.copy());
    }

    private void writeGoetyBrazierRecipe(GoetyBrazierRecipeData data) {
        for (int i = 0; i < GoetyBrazierRecipeData.INPUT_COUNT; i++) {
            data.setIngredient(i, ingredientForVisualSlot(i));
        }
        data.setResult(visualResult.copy());
    }

    private void writeGoetyPulverizeRecipe(GoetyPulverizeRecipeData data) {
        data.setIngredient(ingredientForVisualSlot(0));
        data.setVisibleResult(visualResult);
    }

    private void writeGoetyBrewingRecipe(GoetyBrewingRecipeData data) {
        data.setIngredient(ingredientForVisualSlot(0));
        visualResult = data.visibleResult();
    }

    private void writeFarmerCookingPotRecipe(FarmerCookingPotRecipeData cookingPot) {
        var ingredients = new ArrayList<RecipeIngredient>();
        for (int i = 0; i < 6; i++) {
            var ingredient = ingredientForVisualSlot(i);
            if (!isIngredientEmpty(ingredient)) {
                ingredients.add(ingredient);
            }
        }
        cookingPot.setIngredients(ingredients);
        cookingPot.setResult(visualResult.copy());
        cookingPot.setContainer(visualContainer.copy());
    }

    private void writeFarmerCuttingRecipe(FarmerCuttingRecipeData cutting) {
        cutting.setInput(ingredientForVisualSlot(0));
        cutting.setTool(ingredientForVisualSlot(1));
        var results = new ArrayList<FarmerCuttingResultData>();
        for (int i = 0; i < visualCuttingResults.length; i++) {
            var stack = visualCuttingResults[i];
            if (stack != null && !stack.isEmpty()) {
                results.add(new FarmerCuttingResultData()
                        .setItem(stack.copy())
                        .setChance(Math.max(0, Math.min(1, visualCuttingChances[i]))));
            }
        }
        cutting.setResults(results);
        visualResult = results.isEmpty() ? ItemStack.EMPTY : results.get(0).getItem().copy();
    }

    private void writeKaleidoscopePotRecipe(KaleidoscopePotRecipeData data) {
        data.setIngredients(visualIngredientList(KALEIDOSCOPE_MAX_INPUTS));
        data.setCarrier(ingredientForVisualSlot(KALEIDOSCOPE_CARRIER_SLOT));
        data.setResult(visualResult.copy());
    }

    private void writeKaleidoscopeStockpotRecipe(KaleidoscopeStockpotRecipeData data) {
        data.setIngredients(visualIngredientList(KALEIDOSCOPE_MAX_INPUTS));
        data.setCarrier(ingredientForVisualSlot(KALEIDOSCOPE_CARRIER_SLOT));
        data.setResult(visualResult.copy());
    }

    private void writeKaleidoscopeMillstoneRecipe(KaleidoscopeMillstoneRecipeData data) {
        data.setIngredient(ingredientForVisualSlot(0));
        data.setResult(visualResult.copy());
    }

    private void writeKaleidoscopeChoppingBoardRecipe(KaleidoscopeChoppingBoardRecipeData data) {
        data.setIngredient(ingredientForVisualSlot(0));
        data.setResult(visualResult.copy());
    }

    private void writeKaleidoscopeSteamerRecipe(KaleidoscopeSteamerRecipeData data) {
        data.setIngredient(ingredientForVisualSlot(0));
        data.setResult(visualResult.copy());
    }

    private void writeKaleidoscopeTeapotRecipe(KaleidoscopeTeapotRecipeData data) {
        data.setIngredient(ingredientForVisualSlot(0));
        data.setResult(visualResult.copy());
    }

    private ArrayList<RecipeIngredient> visualIngredientList(int maxInputs) {
        var ingredients = new ArrayList<RecipeIngredient>();
        for (int i = 0; i < Math.min(maxInputs, visualIngredientData.length); i++) {
            var ingredient = ingredientForVisualSlot(i);
            if (!isIngredientEmpty(ingredient)) {
                ingredients.add(ingredient);
            }
        }
        return ingredients;
    }

    private void writeCreateSequencedAssemblyRecipe(CreateSequencedAssemblyRecipeData data) {
        data.setIngredient(ingredientForVisualSlot(0));
        data.setTransitionalItem(visualCreateSequencedTransitional.copyWithCount(1));
        var sequence = data.getSequence();
        if (sequence == null) {
            sequence = new ArrayList<>();
            data.setSequence(sequence);
        }
        for (int i = 0; i < Math.min(sequence.size(), CREATE_SEQUENCED_MAX_STEPS); i++) {
            var step = sequence.get(i);
            if (step == null) {
                step = new CreateSequencedAssemblyStepData();
                sequence.set(i, step);
            }
            var ingredientSlot = createSequencedIngredientSlotIndex(i);
            if (ingredientSlot >= 0 && ingredientSlot < visualIngredientData.length) {
                step.setIngredient(ingredientForVisualSlot(ingredientSlot));
            }
        }
        var outputs = new ArrayList<CreateProcessingOutputData>();
        for (int i = 0; i < Math.min(CREATE_SEQUENCED_MAX_OUTPUTS, visualCreateOutputs.length); i++) {
            var stack = visualCreateOutputs[i];
            if (stack != null && !stack.isEmpty()) {
                outputs.add(new CreateProcessingOutputData()
                        .setItem(stack.copy())
                        .setChance(Math.max(0, visualCreateOutputChances[i])));
            }
        }
        data.setOutputs(outputs);
        visualResult = outputs.isEmpty() ? ItemStack.EMPTY : outputs.get(0).getItem().copy();
    }

    private void writeCreateProcessingRecipe(RecipeEntry entry) {
        var kind = CreateProcessingKind.byType(entry.getType()).orElse(null);
        if (kind == null) {
            return;
        }
        var data = entry.getCreateProcessing();
        if (kind == CreateProcessingKind.AUTO_PACKING) {
            writeCreateAutoPackingRecipe(data);
            return;
        }
        var ingredients = new ArrayList<RecipeIngredient>();
        var visualIngredientCount = createVisibleItemInputCapacity(kind);
        var countedInputs = supportsCreateCountedItemInputs(kind);
        var remainingIngredientWeight = visualIngredientCount;
        for (int i = 0; i < visualIngredientCount; i++) {
            var ingredient = ingredientForVisualSlot(i);
            if (!isIngredientEmpty(ingredient)) {
                var normalizedIngredient = countedInputs
                        ? CreateItemInputCounts.copyWithClampedWeight(ingredient, remainingIngredientWeight)
                        : ingredient;
                if (!isIngredientEmpty(normalizedIngredient)) {
                    ingredients.add(normalizedIngredient);
                    remainingIngredientWeight -= Math.max(1, CreateItemInputCounts.slotWeight(normalizedIngredient));
                    if (countedInputs && remainingIngredientWeight <= 0) {
                        break;
                    }
                }
            }
        }
        var existingIngredients = data.getIngredients();
        if (!countedInputs && existingIngredients != null && existingIngredients.size() > visualIngredientCount) {
            for (int i = visualIngredientCount; i < Math.min(existingIngredients.size(), kind.maxItemInputs()); i++) {
                var ingredient = existingIngredients.get(i);
                if (!isIngredientEmpty(ingredient)) {
                    ingredients.add(ingredient);
                }
            }
        }
        var fluidIngredients = new ArrayList<CreateFluidIngredientData>();
        var visualFluidInputCount = Math.min(CREATE_MAX_FLUID_INPUTS, kind.maxFluidInputs());
        var lastFluidInputIndex = -1;
        for (int i = 0; i < visualFluidInputCount; i++) {
            if (!isCreateFluidIngredientEmpty(visualCreateFluidInputs[i])) {
                lastFluidInputIndex = i;
            }
        }
        for (int i = 0; i <= lastFluidInputIndex; i++) {
            var ingredient = visualCreateFluidInputs[i];
            fluidIngredients.add(ingredient == null ? CreateFluidIngredientData.empty() : ingredient.copy());
        }
        var outputs = new ArrayList<CreateProcessingOutputData>();
        var visualOutputCount = Math.min(CREATE_MAX_ITEM_OUTPUTS, kind.maxItemOutputs());
        for (int i = 0; i < visualOutputCount; i++) {
            var stack = visualCreateOutputs[i];
            if (stack != null && !stack.isEmpty()) {
                outputs.add(new CreateProcessingOutputData()
                        .setItem(stack.copy())
                        .setChance(Math.max(0, Math.min(1, visualCreateOutputChances[i]))));
            }
        }
        var existingOutputs = data.getOutputs();
        if (existingOutputs != null && existingOutputs.size() > visualOutputCount) {
            for (int i = visualOutputCount; i < Math.min(existingOutputs.size(), kind.maxItemOutputs()); i++) {
                var output = existingOutputs.get(i);
                if (output != null && output.getItem() != null && !output.getItem().isEmpty()) {
                    outputs.add(output);
                }
            }
        }
        var fluidOutputs = new ArrayList<FluidStack>();
        for (int i = 0; i < Math.min(CREATE_MAX_FLUID_OUTPUTS, kind.maxFluidOutputs()); i++) {
            var stack = visualCreateFluidOutputs[i];
            if (stack != null && !stack.isEmpty()) {
                fluidOutputs.add(stack.copy());
            }
        }
        data.setIngredients(ingredients);
        data.setFluidIngredients(fluidIngredients);
        data.setOutputs(outputs);
        data.setFluidOutputs(fluidOutputs);
        visualResult = outputs.isEmpty() ? ItemStack.EMPTY : outputs.get(0).getItem().copy();
    }

    private void writeExtendedCraftingTableRecipe(ExtendedCraftingTableRecipeData data) {
        if (selectedEntry != null && isExtendedCraftingShapedTableEntry(selectedEntry)) {
            var gridSize = ExtendedCraftingRecipeEditorTypes.tableGridSizeForTier(getExtendedCraftingTableTier(selectedEntry));
            data.setWidth(gridSize).setHeight(gridSize);
            var pattern = buildVisualShapedPattern(gridSize, gridSize, EXTENDED_CRAFTING_TABLE_GRID_SIZE);
            data.setPattern(pattern.pattern());
            data.setKey(pattern.key());
        } else if (selectedEntry != null && isExtendedCraftingShapelessTableEntry(selectedEntry)) {
            var gridSize = ExtendedCraftingRecipeEditorTypes.tableGridSizeForTier(getExtendedCraftingTableTier(selectedEntry));
            data.setShapelessIngredients(visualIngredientList(gridSize * gridSize));
        }
        data.setResult(visualResult.copy());
    }

    private void writeExtendedCraftingEnderCrafterRecipe(RecipeEntry entry) {
        var data = entry.getExtendedCraftingEnderCrafter();
        if (isExtendedCraftingShapedEnderCrafterEntry(entry)) {
            var pattern = buildVisualShapedPattern(3, 3, EXTENDED_CRAFTING_SMALL_GRID_SIZE);
            data.setPattern(pattern.pattern());
            data.setKey(pattern.key());
        } else {
            data.setShapelessIngredients(visualIngredientList(CRAFTING_GRID_SLOT_COUNT));
        }
        data.setResult(visualResult.copy());
    }

    private void writeExtendedCraftingFluxCrafterRecipe(RecipeEntry entry) {
        var data = entry.getExtendedCraftingFluxCrafter();
        if (isExtendedCraftingShapedFluxCrafterEntry(entry)) {
            var pattern = buildVisualShapedPattern(3, 3, EXTENDED_CRAFTING_SMALL_GRID_SIZE);
            data.setPattern(pattern.pattern());
            data.setKey(pattern.key());
        } else {
            data.setShapelessIngredients(visualIngredientList(CRAFTING_GRID_SLOT_COUNT));
        }
        data.setResult(visualResult.copy());
    }

    private void writeExtendedCraftingCombinationRecipe(ExtendedCraftingCombinationRecipeData data) {
        data.setInput(ingredientForVisualSlot(0));
        var pedestalItems = new ArrayList<RecipeIngredient>();
        for (int i = 1; i <= EXTENDED_CRAFTING_COMBINATION_MAX_PEDESTALS; i++) {
            var ingredient = ingredientForVisualSlot(i);
            if (!isIngredientEmpty(ingredient)) {
                pedestalItems.add(ingredient);
            }
        }
        data.setPedestalItems(pedestalItems);
        data.setResult(visualResult.copy());
    }

    private void writeExtendedCraftingCompressorRecipe(ExtendedCraftingCompressorRecipeData data) {
        data.setCatalyst(ingredientForVisualSlot(0));
        var inputs = new ArrayList<ExtendedCraftingCountedIngredientData>();
        var existingInputs = data.getInputs();
        for (int i = 0; i < EXTENDED_CRAFTING_COMPRESSOR_MAX_INPUTS; i++) {
            var ingredient = ingredientForVisualSlot(i + 1);
            if (!isIngredientEmpty(ingredient)) {
                var count = existingInputs != null && i < existingInputs.size() && existingInputs.get(i) != null
                        ? Math.max(1, existingInputs.get(i).getCount())
                        : 1;
                inputs.add(new ExtendedCraftingCountedIngredientData()
                        .setIngredient(ingredient)
                        .setCount(count));
            }
        }
        data.setInputs(inputs);
        data.setResult(visualResult.copy());
    }

    private void writeAvaritiaTableRecipe(RecipeEntry entry) {
        var data = entry.getAvaritiaTable();
        if (isAvaritiaShapedTableEntry(entry)) {
            var gridSize = AvaritiaRecipeEditorTypes.tableGridSizeForTier(getAvaritiaTableTier(entry));
            data.setWidth(gridSize).setHeight(gridSize);
            var pattern = buildVisualShapedPattern(gridSize, gridSize, EXTENDED_CRAFTING_TABLE_GRID_SIZE);
            data.setPattern(pattern.pattern());
            data.setKey(pattern.key());
        } else if (isAvaritiaShapelessTableEntry(entry)) {
            var gridSize = AvaritiaRecipeEditorTypes.tableGridSizeForTier(getAvaritiaTableTier(entry));
            data.setShapelessIngredients(visualIngredientList(gridSize * gridSize));
        }
        data.setResult(visualResult.copy());
    }

    private void writeAvaritiaCompressorRecipe(AvaritiaCompressorRecipeData data) {
        data.setIngredient(ingredientForVisualSlot(0));
        data.setResult(visualResult.copy());
    }

    private void writeAvaritiaExtremeSmithingRecipe(RecipeEntry entry) {
        var data = entry.getAvaritiaExtremeSmithing();
        data.setTemplate(ingredientForVisualSlot(0));
        data.setBase(ingredientForVisualSlot(1));
        data.setAdditions(new ArrayList<>(List.of(
                ingredientForVisualSlot(2),
                ingredientForVisualSlot(3),
                ingredientForVisualSlot(4)
        )));
        data.setResult(visualResult.copy());
    }

    private VisualPattern buildVisualShapedPattern(int width, int height, int gridSize) {
        var itemSymbols = new LinkedHashMap<String, Character>();
        var keyEntries = new ArrayList<ShapedKeyEntry>();
        var pattern = new ArrayList<String>();
        var symbolIndex = 0;
        for (int row = 0; row < height; row++) {
            var builder = new StringBuilder();
            for (int col = 0; col < width; col++) {
                var slot = row * gridSize + col;
                var stack = visualIngredients[slot];
                var ingredient = ingredientForVisualSlot(slot);
                if (stack.isEmpty() && isIngredientEmpty(ingredient)) {
                    builder.append(' ');
                    continue;
                }
                var ingredientKey = ingredientKey(ingredient, stack, slot);
                var symbol = itemSymbols.get(ingredientKey);
                if (symbol == null) {
                    if (symbolIndex >= SHAPED_SYMBOLS.length) {
                        builder.append(' ');
                        continue;
                    }
                    symbol = SHAPED_SYMBOLS[symbolIndex++];
                    itemSymbols.put(ingredientKey, symbol);
                    keyEntries.add(ShapedKeyEntry.of(String.valueOf(symbol), ingredient));
                }
                builder.append(symbol);
            }
            pattern.add(builder.toString());
        }
        return keyEntries.isEmpty()
                ? new VisualPattern(new ArrayList<>(), new ArrayList<>())
                : new VisualPattern(pattern, keyEntries);
    }

    private void writeArsNouveauApparatusRecipe(ArsNouveauApparatusRecipeData data) {
        data.setReagent(ingredientForVisualSlot(0));
        data.setPedestalItems(arsNouveauExtraIngredients());
        data.setResult(visualResult.copy());
    }

    private void writeArsNouveauArmorUpgradeRecipe(ArsNouveauArmorUpgradeRecipeData data) {
        data.setPedestalItems(arsNouveauExtraIngredients());
    }

    private void writeArsNouveauEnchantmentRecipe(ArsNouveauEnchantmentRecipeData data) {
        data.setPedestalItems(arsNouveauExtraIngredients());
    }

    private void writeArsNouveauImbuementRecipe(ArsNouveauImbuementRecipeData data) {
        data.setInput(ingredientForVisualSlot(0));
        data.setPedestalItems(arsNouveauExtraIngredients(ARS_NOUVEAU_IMBUEMENT_INPUTS));
        data.setResult(visualResult.copy());
    }

    private void writeArsNouveauGlyphRecipe(ArsNouveauGlyphRecipeData data) {
        var inputs = new ArrayList<RecipeIngredient>();
        for (int i = 0; i < ARS_NOUVEAU_MAX_INPUTS; i++) {
            var ingredient = ingredientForVisualSlot(i);
            if (!isIngredientEmpty(ingredient)) {
                inputs.add(ingredient);
            }
        }
        data.setInputs(inputs);
        data.setResult(visualResult.copy());
    }

    private void writeArsNouveauCrushRecipe(ArsNouveauCrushRecipeData data) {
        data.setInput(ingredientForVisualSlot(0));
        var outputs = new ArrayList<ArsNouveauCrushOutputData>();
        for (int i = 0; i < ARS_NOUVEAU_MAX_CRUSH_OUTPUTS; i++) {
            var stack = visualArsNouveauOutputs[i];
            if (stack != null && !stack.isEmpty()) {
                outputs.add(new ArsNouveauCrushOutputData()
                        .setItem(stack.copy())
                        .setChance(Math.max(0, Math.min(1, visualArsNouveauOutputChances[i])))
                        .setMaxRange(Math.max(1, visualArsNouveauOutputMaxRanges[i])));
            }
        }
        data.setOutputs(outputs);
        visualResult = outputs.isEmpty() ? ItemStack.EMPTY : outputs.get(0).getItem().copy();
    }

    private void writeArsNouveauPedestalOnlyRecipe(ArsNouveauPedestalOnlyRecipeData data) {
        data.setPedestalItems(arsNouveauExtraIngredients());
    }

    private ArrayList<RecipeIngredient> arsNouveauExtraIngredients() {
        return arsNouveauExtraIngredients(ARS_NOUVEAU_MAX_INPUTS);
    }

    private ArrayList<RecipeIngredient> arsNouveauExtraIngredients(int inputCount) {
        var ingredients = new ArrayList<RecipeIngredient>();
        for (int i = 1; i < Math.min(inputCount, ARS_NOUVEAU_MAX_INPUTS); i++) {
            var ingredient = ingredientForVisualSlot(i);
            if (!isIngredientEmpty(ingredient)) {
                ingredients.add(ingredient);
            }
        }
        return ingredients;
    }

    private void writeCreateAutoPackingRecipe(CreateProcessingRecipeData data) {
        writeCreateAutoPackingRecipe(data, autoPackingGridSize(data));
    }

    private void writeCreateAutoPackingRecipe(CreateProcessingRecipeData data, int gridSize) {
        var ingredient = ingredientForVisualSlot(0);
        if (isIngredientEmpty(ingredient)) {
            ingredient = firstCreateIngredient(data);
        }
        var ingredients = new ArrayList<RecipeIngredient>();
        if (!isIngredientEmpty(ingredient)) {
            for (int i = 0; i < gridSize * gridSize; i++) {
                ingredients.add(copyIngredient(ingredient));
            }
        }
        var outputs = new ArrayList<CreateProcessingOutputData>();
        var stack = visualCreateOutputs[0];
        if (stack != null && !stack.isEmpty()) {
            outputs.add(new CreateProcessingOutputData()
                    .setItem(stack.copy())
                    .setChance(Math.max(0, Math.min(1, visualCreateOutputChances[0]))));
        }
        data.setIngredients(ingredients);
        data.setFluidIngredients(new ArrayList<>());
        data.setOutputs(outputs);
        data.setFluidOutputs(new ArrayList<>());
        visualResult = outputs.isEmpty() ? ItemStack.EMPTY : outputs.get(0).getItem().copy();
        refreshAutoPackingVisualIngredients(ingredient, gridSize);
    }

    private RecipeIngredient getIngredientForSlot(RecipeEntry entry, int index) {
        if (entry.isType(RecipeEditorTypes.CRAFTING_SHAPED)) {
            return getShapedSlotIngredient(entry.getShaped(), index);
        }
        if (entry.isType(RecipeEditorTypes.CRAFTING_SHAPELESS) && index < entry.getShapeless().getIngredients().size()) {
            return entry.getShapeless().getIngredients().get(index);
        }
        if (isCookingEntry(entry) && index == 0) {
            return entry.getCooking().getIngredient();
        }
        if (isStonecuttingEntry(entry) && index == 0) {
            return entry.getStonecutting().getIngredient();
        }
        if (isSmithingTransformEntry(entry)) {
            return getSmithingSlotIngredient(entry.getSmithingTransform(), index);
        }
        if (isIronArcaneAnvilEntry(entry)) {
            return getIronArcaneAnvilSlotIngredient(entry.getIronArcaneAnvil(), index);
        }
        if (isIronNoAdditionSmithingEntry(entry)) {
            return getIronNoAdditionSmithingSlotIngredient(entry.getIronNoAdditionSmithing(), index);
        }
        if (isIronAlchemistCauldronEntry(entry) && index == 0) {
            return entry.getIronAlchemistCauldron().getInput();
        }
        if (isIceAndFireDragonForgeEntry(entry)) {
            return getDragonForgeSlotIngredient(entry.getIceAndFireDragonForge(), index);
        }
        if (isCataclysmWeaponFusionEntry(entry)) {
            return switch (index) {
                case 0 -> entry.getCataclysmWeaponFusion().getBase();
                case 1 -> entry.getCataclysmWeaponFusion().getAddition();
                default -> new RecipeIngredient();
            };
        }
        if (isCataclysmAmethystBlessEntry(entry) && index == 0) {
            var ingredient = entry.getCataclysmAmethystBless().getIngredient();
            return ingredient == null ? new RecipeIngredient() : ingredient;
        }
        if (isTouhouLittleMaidAltarEntry(entry)
                && index >= 0 && index < TouhouLittleMaidAltarRecipeData.INPUT_COUNT) {
            return entry.getTouhouLittleMaidAltar().ingredient(index);
        }
        if (isSporeSurgeryEntry(entry) && index >= 0 && index < SporeSurgeryRecipeData.INPUT_COUNT) {
            return entry.getSporeSurgery().ingredient(index);
        }
        if (isSporeGraftingEntry(entry) && index >= 0 && index < SporeGraftingRecipeData.INPUT_COUNT) {
            return entry.getSporeGrafting().ingredient(index);
        }
        if (isGoetyCursedInfuserEntry(entry) && index == 0) {
            return entry.getGoetyCursedInfuser().getIngredient();
        }
        if (isGoetyRitualEntry(entry)) {
            if (index == 0) {
                return entry.getGoetyRitual().getActivationItem();
            }
            if (index <= GoetyRitualRecipeData.MAX_PEDESTAL_INGREDIENTS) {
                return entry.getGoetyRitual().ingredient(index - 1);
            }
        }
        if (isGoetyBrazierEntry(entry) && index >= 0 && index < GoetyBrazierRecipeData.INPUT_COUNT) {
            return entry.getGoetyBrazier().ingredient(index);
        }
        if (isGoetyPulverizeEntry(entry) && index == 0) {
            return entry.getGoetyPulverize().getIngredient();
        }
        if (isGoetyBrewingEntry(entry) && index == 0) {
            return entry.getGoetyBrewing().getIngredient();
        }
        if (isFarmersCookingPotEntry(entry) && index >= 0 && index < 6) {
            return getFarmerCookingPotSlotIngredient(entry.getFarmerCookingPot(), index);
        }
        if (isFarmersCuttingBoardEntry(entry) && index >= 0 && index <= 1) {
            return getFarmerCuttingSlotIngredient(entry.getFarmerCuttingBoard(), index);
        }
        if (isKaleidoscopePotEntry(entry) && index == KALEIDOSCOPE_CARRIER_SLOT) {
            return entry.getKaleidoscopePot().getCarrier() == null ? new RecipeIngredient() : entry.getKaleidoscopePot().getCarrier();
        }
        if (isKaleidoscopePotEntry(entry) && index >= 0 && index < KALEIDOSCOPE_MAX_INPUTS) {
            return getArsNouveauListIngredient(entry.getKaleidoscopePot().getIngredients(), index);
        }
        if (isKaleidoscopeStockpotEntry(entry) && index == KALEIDOSCOPE_CARRIER_SLOT) {
            return entry.getKaleidoscopeStockpot().getCarrier() == null ? new RecipeIngredient() : entry.getKaleidoscopeStockpot().getCarrier();
        }
        if (isKaleidoscopeStockpotEntry(entry) && index >= 0 && index < KALEIDOSCOPE_MAX_INPUTS) {
            return getArsNouveauListIngredient(entry.getKaleidoscopeStockpot().getIngredients(), index);
        }
        if (isKaleidoscopeMillstoneEntry(entry) && index == 0) {
            return entry.getKaleidoscopeMillstone().getIngredient() == null ? new RecipeIngredient() : entry.getKaleidoscopeMillstone().getIngredient();
        }
        if (isKaleidoscopeChoppingBoardEntry(entry) && index == 0) {
            return entry.getKaleidoscopeChoppingBoard().getIngredient() == null ? new RecipeIngredient() : entry.getKaleidoscopeChoppingBoard().getIngredient();
        }
        if (isKaleidoscopeSteamerEntry(entry) && index == 0) {
            return entry.getKaleidoscopeSteamer().getIngredient() == null ? new RecipeIngredient() : entry.getKaleidoscopeSteamer().getIngredient();
        }
        if (isKaleidoscopeTeapotEntry(entry) && index == 0) {
            return entry.getKaleidoscopeTeapot().getIngredient() == null ? new RecipeIngredient() : entry.getKaleidoscopeTeapot().getIngredient();
        }
        if (isCreateMechanicalCraftingEntry(entry)) {
            return getCreateMechanicalCraftingSlotIngredient(entry.getCreateMechanicalCrafting(), index);
        }
        if (isCreateSequencedAssemblyEntry(entry)) {
            return getCreateSequencedAssemblySlotIngredient(entry.getCreateSequencedAssembly(), index);
        }
        if (isCreateProcessingEntry(entry) && index >= 0 && index < 9) {
            return getCreateProcessingSlotIngredient(entry.getCreateProcessing(), index);
        }
        if (isExtendedCraftingTableEntry(entry)) {
            return getExtendedCraftingTableSlotIngredient(entry, index);
        }
        if (isExtendedCraftingEnderCrafterEntry(entry)) {
            return getExtendedCraftingEnderCrafterSlotIngredient(entry, index);
        }
        if (isExtendedCraftingFluxCrafterEntry(entry)) {
            return getExtendedCraftingFluxCrafterSlotIngredient(entry, index);
        }
        if (isExtendedCraftingCombinationEntry(entry)) {
            return getExtendedCraftingCombinationSlotIngredient(entry.getExtendedCraftingCombination(), index);
        }
        if (isExtendedCraftingCompressorEntry(entry)) {
            return getExtendedCraftingCompressorSlotIngredient(entry.getExtendedCraftingCompressor(), index);
        }
        if (isAvaritiaTableEntry(entry)) {
            return getAvaritiaTableSlotIngredient(entry, index);
        }
        if (isAvaritiaCompressorEntry(entry) && index == 0) {
            return entry.getAvaritiaCompressor().getIngredient() == null ? new RecipeIngredient() : entry.getAvaritiaCompressor().getIngredient();
        }
        if (isAvaritiaExtremeSmithingEntry(entry)) {
            return getAvaritiaExtremeSmithingSlotIngredient(entry, index);
        }
        if (isArsNouveauApparatusEntry(entry)) {
            return getArsNouveauApparatusSlotIngredient(entry.getArsNouveauApparatus(), index);
        }
        if (isArsNouveauArmorUpgradeEntry(entry)) {
            return getArsNouveauArmorUpgradeSlotIngredient(entry.getArsNouveauArmorUpgrade(), index);
        }
        if (isArsNouveauEnchantmentEntry(entry)) {
            return getArsNouveauEnchantmentSlotIngredient(entry.getArsNouveauEnchantment(), index);
        }
        if (isArsNouveauImbuementEntry(entry)) {
            return getArsNouveauImbuementSlotIngredient(entry.getArsNouveauImbuement(), index);
        }
        if (isArsNouveauGlyphEntry(entry)) {
            return getArsNouveauGlyphSlotIngredient(entry.getArsNouveauGlyph(), index);
        }
        if (isArsNouveauCrushEntry(entry) && index == 0) {
            return entry.getArsNouveauCrush().getInput();
        }
        if (isArsNouveauPedestalOnlyEntry(entry)) {
            return getArsNouveauListIngredient(entry.getArsNouveauPedestalOnly().getPedestalItems(), index - 1);
        }
        return new RecipeIngredient();
    }

    private RecipeIngredient getArsNouveauApparatusSlotIngredient(ArsNouveauApparatusRecipeData data, int index) {
        if (index == 0) {
            return data.getReagent() == null ? new RecipeIngredient() : data.getReagent();
        }
        return getArsNouveauListIngredient(data.getPedestalItems(), index - 1);
    }

    private RecipeIngredient getArsNouveauArmorUpgradeSlotIngredient(ArsNouveauArmorUpgradeRecipeData data, int index) {
        return getArsNouveauListIngredient(data.getPedestalItems(), index - 1);
    }

    private RecipeIngredient getArsNouveauEnchantmentSlotIngredient(ArsNouveauEnchantmentRecipeData data, int index) {
        return getArsNouveauListIngredient(data.getPedestalItems(), index - 1);
    }

    private RecipeIngredient getArsNouveauImbuementSlotIngredient(ArsNouveauImbuementRecipeData data, int index) {
        if (index == 0) {
            return data.getInput() == null ? new RecipeIngredient() : data.getInput();
        }
        return getArsNouveauListIngredient(data.getPedestalItems(), index - 1);
    }

    private RecipeIngredient getArsNouveauGlyphSlotIngredient(ArsNouveauGlyphRecipeData data, int index) {
        return getArsNouveauListIngredient(data.getInputs(), index);
    }

    private RecipeIngredient getArsNouveauListIngredient(List<RecipeIngredient> ingredients, int index) {
        if (ingredients == null || index < 0 || index >= ingredients.size()) {
            return new RecipeIngredient();
        }
        var ingredient = ingredients.get(index);
        return ingredient == null ? new RecipeIngredient() : ingredient;
    }

    private RecipeIngredient getCreateSequencedAssemblySlotIngredient(CreateSequencedAssemblyRecipeData data, int index) {
        if (index == 0) {
            return data.getIngredient() == null ? new RecipeIngredient() : data.getIngredient();
        }
        var stepIndex = createSequencedStepIndexFromIngredientSlot(index);
        if (stepIndex < 0 || data.getSequence() == null || stepIndex >= data.getSequence().size()) {
            return new RecipeIngredient();
        }
        var step = data.getSequence().get(stepIndex);
        return step == null || step.getIngredient() == null ? new RecipeIngredient() : step.getIngredient();
    }

    private RecipeIngredient getCreateMechanicalCraftingSlotIngredient(CreateMechanicalCraftingRecipeData data, int index) {
        var row = index / MECHANICAL_CRAFTING_GRID_SIZE;
        var col = index % MECHANICAL_CRAFTING_GRID_SIZE;
        if (row >= data.normalizedHeight() || col >= data.normalizedWidth()) {
            return new RecipeIngredient();
        }
        if (row >= data.getPattern().size()) {
            return new RecipeIngredient();
        }
        var line = data.getPattern().get(row);
        if (line == null || col >= line.length()) {
            return new RecipeIngredient();
        }
        var symbol = line.charAt(col);
        return ingredientForMechanicalSymbol(data, symbol);
    }

    private RecipeIngredient getCreateProcessingSlotIngredient(CreateProcessingRecipeData data, int index) {
        var ingredients = data.getIngredients();
        if (ingredients == null || index >= ingredients.size()) {
            return new RecipeIngredient();
        }
        return ingredients.get(index);
    }

    private RecipeIngredient getExtendedCraftingTableSlotIngredient(RecipeEntry entry, int index) {
        var data = entry.getExtendedCraftingTable();
        if (isExtendedCraftingShapedTableEntry(entry)) {
            return getShapedGridSlotIngredient(data.getPattern(), data.getKey(), index, data.normalizedWidth(), data.normalizedHeight(), EXTENDED_CRAFTING_TABLE_GRID_SIZE);
        }
        if (isExtendedCraftingShapelessTableEntry(entry)) {
            return getListIngredient(data.getShapelessIngredients(), index);
        }
        return new RecipeIngredient();
    }

    private RecipeIngredient getExtendedCraftingEnderCrafterSlotIngredient(RecipeEntry entry, int index) {
        var data = entry.getExtendedCraftingEnderCrafter();
        if (isExtendedCraftingShapedEnderCrafterEntry(entry)) {
            return getShapedGridSlotIngredient(data.getPattern(), data.getKey(), index, 3, 3, EXTENDED_CRAFTING_SMALL_GRID_SIZE);
        }
        return getListIngredient(data.getShapelessIngredients(), index);
    }

    private RecipeIngredient getExtendedCraftingFluxCrafterSlotIngredient(RecipeEntry entry, int index) {
        var data = entry.getExtendedCraftingFluxCrafter();
        if (isExtendedCraftingShapedFluxCrafterEntry(entry)) {
            return getShapedGridSlotIngredient(data.getPattern(), data.getKey(), index, 3, 3, EXTENDED_CRAFTING_SMALL_GRID_SIZE);
        }
        return getListIngredient(data.getShapelessIngredients(), index);
    }

    private RecipeIngredient getExtendedCraftingCombinationSlotIngredient(ExtendedCraftingCombinationRecipeData data, int index) {
        if (index == 0) {
            return data.getInput() == null ? new RecipeIngredient() : data.getInput();
        }
        return getListIngredient(data.getPedestalItems(), index - 1);
    }

    private RecipeIngredient getExtendedCraftingCompressorSlotIngredient(ExtendedCraftingCompressorRecipeData data, int index) {
        if (index == 0) {
            return data.getCatalyst() == null ? new RecipeIngredient() : data.getCatalyst();
        }
        var inputs = data.getInputs();
        var inputIndex = index - 1;
        if (inputs == null || inputIndex < 0 || inputIndex >= inputs.size()) {
            return new RecipeIngredient();
        }
        var input = inputs.get(inputIndex);
        return input == null || input.getIngredient() == null ? new RecipeIngredient() : input.getIngredient();
    }

    private RecipeIngredient getAvaritiaTableSlotIngredient(RecipeEntry entry, int index) {
        var data = entry.getAvaritiaTable();
        if (isAvaritiaShapedTableEntry(entry)) {
            return getShapedGridSlotIngredient(data.getPattern(), data.getKey(), index, selectedAvaritiaTableGridWidth(entry), selectedAvaritiaTableGridHeight(entry), EXTENDED_CRAFTING_TABLE_GRID_SIZE);
        }
        if (isAvaritiaShapelessTableEntry(entry)) {
            return getListIngredient(data.getShapelessIngredients(), index);
        }
        return new RecipeIngredient();
    }

    private RecipeIngredient getAvaritiaExtremeSmithingSlotIngredient(RecipeEntry entry, int index) {
        var data = entry.getAvaritiaExtremeSmithing();
        return switch (index) {
            case 0 -> data.getTemplate();
            case 1 -> data.getBase();
            case 2 -> data.addition(0);
            case 3 -> data.addition(1);
            case 4 -> data.addition(2);
            default -> new RecipeIngredient();
        };
    }

    private RecipeIngredient getShapedGridSlotIngredient(List<String> pattern, List<ShapedKeyEntry> key, int index, int width, int height, int gridSize) {
        var row = index / gridSize;
        var col = index % gridSize;
        if (row >= height || col >= width || row >= safeList(pattern).size()) {
            return new RecipeIngredient();
        }
        var line = safeList(pattern).get(row);
        if (line == null || col >= line.length()) {
            return new RecipeIngredient();
        }
        return ingredientForSymbol(key, line.charAt(col));
    }

    private RecipeIngredient getListIngredient(List<RecipeIngredient> ingredients, int index) {
        if (ingredients == null || index < 0 || index >= ingredients.size()) {
            return new RecipeIngredient();
        }
        var ingredient = ingredients.get(index);
        return ingredient == null ? new RecipeIngredient() : ingredient;
    }

    private RecipeIngredient getFarmerCookingPotSlotIngredient(FarmerCookingPotRecipeData cookingPot, int index) {
        var ingredients = cookingPot.getIngredients();
        if (ingredients == null || index >= ingredients.size()) {
            return new RecipeIngredient();
        }
        return ingredients.get(index);
    }

    private RecipeIngredient getFarmerCuttingSlotIngredient(FarmerCuttingRecipeData cutting, int index) {
        return switch (index) {
            case 0 -> cutting.getInput();
            case 1 -> cutting.getTool();
            default -> new RecipeIngredient();
        };
    }

    private RecipeIngredient getDragonForgeSlotIngredient(DragonForgeRecipeData dragonForge, int index) {
        return switch (index) {
            case 0 -> dragonForge.getInput();
            case 1 -> dragonForge.getBlood();
            default -> new RecipeIngredient();
        };
    }

    private RecipeIngredient getIronNoAdditionSmithingSlotIngredient(IronNoAdditionSmithingRecipeData smithing, int index) {
        return switch (index) {
            case 0 -> smithing.getTemplate();
            case 1 -> smithing.getBase();
            default -> new RecipeIngredient();
        };
    }

    private RecipeIngredient getIronArcaneAnvilSlotIngredient(IronArcaneAnvilRecipeData arcaneAnvil, int index) {
        return switch (index) {
            case 0 -> arcaneAnvil.getInput();
            case 1 -> arcaneAnvil.getMaterial();
            default -> new RecipeIngredient();
        };
    }

    private RecipeIngredient getSmithingSlotIngredient(SmithingTransformRecipeData smithingTransform, int index) {
        return switch (index) {
            case 0 -> smithingTransform.getTemplate();
            case 1 -> smithingTransform.getBase();
            case 2 -> smithingTransform.getAddition();
            default -> new RecipeIngredient();
        };
    }

    private RecipeIngredient getShapedSlotIngredient(ShapedCraftingRecipeData shaped, int index) {
        var row = index / 3;
        var col = index % 3;
        if (row >= shaped.getPattern().size()) {
            return new RecipeIngredient();
        }
        var line = shaped.getPattern().get(row);
        if (col >= line.length()) {
            return new RecipeIngredient();
        }
        var symbol = line.charAt(col);
        for (var keyEntry : shaped.getKey()) {
            if (keyEntry.getSymbol() != null && keyEntry.getSymbol().length() == 1 && keyEntry.getSymbol().charAt(0) == symbol) {
                return keyEntry.getIngredient();
            }
        }
        return new RecipeIngredient();
    }

    private void setIngredientForSlot(RecipeEntry entry, int index, RecipeIngredient ingredient) {
        if (entry.isType(RecipeEditorTypes.CRAFTING_SHAPED)) {
            setShapedSlotIngredient(entry.getShaped(), index, ingredient);
        } else if (entry.isType(RecipeEditorTypes.CRAFTING_SHAPELESS)) {
            setShapelessSlotIngredient(entry.getShapeless(), index, ingredient);
        } else if (isCookingEntry(entry) && index == 0) {
            setCookingSlotIngredient(entry.getCooking(), ingredient);
        } else if (isStonecuttingEntry(entry) && index == 0) {
            setStonecuttingSlotIngredient(entry.getStonecutting(), ingredient);
        } else if (isSmithingTransformEntry(entry) && index >= 0 && index <= 2) {
            setSmithingSlotIngredient(entry.getSmithingTransform(), index, ingredient);
        } else if (isIronArcaneAnvilEntry(entry) && index >= 0 && index <= 1) {
            setIronArcaneAnvilSlotIngredient(entry.getIronArcaneAnvil(), index, ingredient);
        } else if (isIronNoAdditionSmithingEntry(entry) && index >= 0 && index <= 1) {
            setIronNoAdditionSmithingSlotIngredient(entry.getIronNoAdditionSmithing(), index, ingredient);
        } else if (isIronAlchemistCauldronEntry(entry) && index == 0) {
            setIronAlchemistCauldronIngredient(entry, ingredient);
        } else if (isIceAndFireDragonForgeEntry(entry) && index >= 0 && index <= 1) {
            setDragonForgeSlotIngredient(entry.getIceAndFireDragonForge(), index, ingredient);
        } else if (isCataclysmWeaponFusionEntry(entry) && index >= 0 && index <= 1) {
            setCataclysmWeaponFusionSlotIngredient(entry.getCataclysmWeaponFusion(), index, ingredient);
        } else if (isCataclysmAmethystBlessEntry(entry) && index == 0) {
            setCataclysmAmethystBlessSlotIngredient(entry.getCataclysmAmethystBless(), ingredient);
        } else if (isTouhouLittleMaidAltarEntry(entry)
                && index >= 0 && index < TouhouLittleMaidAltarRecipeData.INPUT_COUNT) {
            entry.getTouhouLittleMaidAltar().setIngredient(index, ingredient);
        } else if (isSporeSurgeryEntry(entry) && index >= 0 && index < SporeSurgeryRecipeData.INPUT_COUNT) {
            entry.getSporeSurgery().setIngredient(index, ingredient);
        } else if (isSporeGraftingEntry(entry) && index >= 0 && index < SporeGraftingRecipeData.INPUT_COUNT) {
            entry.getSporeGrafting().setIngredient(index, ingredient);
        } else if (isGoetyCursedInfuserEntry(entry) && index == 0) {
            entry.getGoetyCursedInfuser().setIngredient(ingredient);
        } else if (isGoetyRitualEntry(entry) && index == 0) {
            entry.getGoetyRitual().setActivationItem(ingredient);
        } else if (isGoetyRitualEntry(entry) && index > 0 && index <= GoetyRitualRecipeData.MAX_PEDESTAL_INGREDIENTS) {
            entry.getGoetyRitual().setIngredient(index - 1, ingredient);
        } else if (isGoetyBrazierEntry(entry) && index >= 0 && index < GoetyBrazierRecipeData.INPUT_COUNT) {
            entry.getGoetyBrazier().setIngredient(index, ingredient);
        } else if (isGoetyPulverizeEntry(entry) && index == 0) {
            entry.getGoetyPulverize().setIngredient(ingredient);
        } else if (isGoetyBrewingEntry(entry) && index == 0) {
            entry.getGoetyBrewing().setIngredient(ingredient);
        } else if (isFarmersCookingPotEntry(entry) && index >= 0 && index < 6) {
            setFarmerCookingPotSlotIngredient(entry.getFarmerCookingPot(), index, ingredient);
        } else if (isFarmersCuttingBoardEntry(entry) && index >= 0 && index <= 1) {
            setFarmerCuttingSlotIngredient(entry.getFarmerCuttingBoard(), index, ingredient);
        } else if (isKaleidoscopePotEntry(entry) && index == KALEIDOSCOPE_CARRIER_SLOT) {
            setKaleidoscopePotSlotIngredient(entry.getKaleidoscopePot(), index, ingredient);
        } else if (isKaleidoscopePotEntry(entry) && index >= 0 && index < KALEIDOSCOPE_MAX_INPUTS) {
            setKaleidoscopePotSlotIngredient(entry.getKaleidoscopePot(), index, ingredient);
        } else if (isKaleidoscopeStockpotEntry(entry) && index == KALEIDOSCOPE_CARRIER_SLOT) {
            setKaleidoscopeStockpotSlotIngredient(entry.getKaleidoscopeStockpot(), index, ingredient);
        } else if (isKaleidoscopeStockpotEntry(entry) && index >= 0 && index < KALEIDOSCOPE_MAX_INPUTS) {
            setKaleidoscopeStockpotSlotIngredient(entry.getKaleidoscopeStockpot(), index, ingredient);
        } else if (isKaleidoscopeMillstoneEntry(entry) && index == 0) {
            setKaleidoscopeMillstoneSlotIngredient(entry.getKaleidoscopeMillstone(), ingredient);
        } else if (isKaleidoscopeChoppingBoardEntry(entry) && index == 0) {
            setKaleidoscopeChoppingBoardSlotIngredient(entry.getKaleidoscopeChoppingBoard(), ingredient);
        } else if (isKaleidoscopeSteamerEntry(entry) && index == 0) {
            setKaleidoscopeSteamerSlotIngredient(entry.getKaleidoscopeSteamer(), ingredient);
        } else if (isKaleidoscopeTeapotEntry(entry) && index == 0) {
            setKaleidoscopeTeapotSlotIngredient(entry.getKaleidoscopeTeapot(), ingredient);
        } else if (isCreateMechanicalCraftingEntry(entry)) {
            setCreateMechanicalCraftingSlotIngredient(entry.getCreateMechanicalCrafting(), index, ingredient);
        } else if (isCreateSequencedAssemblyEntry(entry)) {
            setCreateSequencedAssemblySlotIngredient(entry.getCreateSequencedAssembly(), index, ingredient);
        } else if (isCreateProcessingEntry(entry) && index >= 0 && index < 9) {
            setCreateProcessingSlotIngredient(entry, index, ingredient);
        } else if (isExtendedCraftingTableEntry(entry)) {
            setExtendedCraftingTableSlotIngredient(entry, index, ingredient);
        } else if (isExtendedCraftingEnderCrafterEntry(entry)) {
            setExtendedCraftingEnderCrafterSlotIngredient(entry, index, ingredient);
        } else if (isExtendedCraftingFluxCrafterEntry(entry)) {
            setExtendedCraftingFluxCrafterSlotIngredient(entry, index, ingredient);
        } else if (isExtendedCraftingCombinationEntry(entry) && index >= 0 && index <= EXTENDED_CRAFTING_COMBINATION_MAX_PEDESTALS) {
            setExtendedCraftingCombinationSlotIngredient(entry.getExtendedCraftingCombination(), index, ingredient);
        } else if (isExtendedCraftingCompressorEntry(entry) && index >= 0 && index <= EXTENDED_CRAFTING_COMPRESSOR_MAX_INPUTS) {
            setExtendedCraftingCompressorSlotIngredient(entry.getExtendedCraftingCompressor(), index, ingredient);
        } else if (isAvaritiaTableEntry(entry)) {
            setAvaritiaTableSlotIngredient(entry, index, ingredient);
        } else if (isAvaritiaCompressorEntry(entry) && index == 0) {
            setAvaritiaCompressorSlotIngredient(entry.getAvaritiaCompressor(), ingredient);
        } else if (isAvaritiaExtremeSmithingEntry(entry) && index >= 0 && index <= 4) {
            setAvaritiaExtremeSmithingSlotIngredient(entry, index, ingredient);
        } else if (isArsNouveauApparatusEntry(entry) && index >= 0 && index < ARS_NOUVEAU_MAX_INPUTS) {
            setArsNouveauApparatusSlotIngredient(entry.getArsNouveauApparatus(), index, ingredient);
        } else if (isArsNouveauArmorUpgradeEntry(entry) && index > 0 && index < ARS_NOUVEAU_MAX_INPUTS) {
            setArsNouveauArmorUpgradeSlotIngredient(entry.getArsNouveauArmorUpgrade(), index, ingredient);
        } else if (isArsNouveauEnchantmentEntry(entry) && index > 0 && index < ARS_NOUVEAU_MAX_INPUTS) {
            setArsNouveauEnchantmentSlotIngredient(entry.getArsNouveauEnchantment(), index, ingredient);
        } else if (isArsNouveauImbuementEntry(entry) && index >= 0 && index < ARS_NOUVEAU_IMBUEMENT_INPUTS) {
            setArsNouveauImbuementSlotIngredient(entry.getArsNouveauImbuement(), index, ingredient);
        } else if (isArsNouveauGlyphEntry(entry) && index >= 0 && index < ARS_NOUVEAU_MAX_INPUTS) {
            setArsNouveauGlyphSlotIngredient(entry.getArsNouveauGlyph(), index, ingredient);
        } else if (isArsNouveauCrushEntry(entry) && index == 0) {
            setArsNouveauCrushSlotIngredient(entry.getArsNouveauCrush(), ingredient);
        } else if (isArsNouveauPedestalOnlyEntry(entry) && index > 0 && index < ARS_NOUVEAU_MAX_INPUTS) {
            setArsNouveauPedestalOnlySlotIngredient(entry.getArsNouveauPedestalOnly(), index, ingredient);
        }
    }

    private void setKaleidoscopePotSlotIngredient(KaleidoscopePotRecipeData data, int index, RecipeIngredient ingredient) {
        setVisualIngredientData(index, ingredient);
        writeKaleidoscopePotRecipe(data);
    }

    private void setKaleidoscopeStockpotSlotIngredient(KaleidoscopeStockpotRecipeData data, int index, RecipeIngredient ingredient) {
        setVisualIngredientData(index, ingredient);
        writeKaleidoscopeStockpotRecipe(data);
    }

    private void setKaleidoscopeMillstoneSlotIngredient(KaleidoscopeMillstoneRecipeData data, RecipeIngredient ingredient) {
        setVisualIngredientData(0, ingredient);
        writeKaleidoscopeMillstoneRecipe(data);
    }

    private void setKaleidoscopeChoppingBoardSlotIngredient(KaleidoscopeChoppingBoardRecipeData data, RecipeIngredient ingredient) {
        setVisualIngredientData(0, ingredient);
        writeKaleidoscopeChoppingBoardRecipe(data);
    }

    private void setKaleidoscopeSteamerSlotIngredient(KaleidoscopeSteamerRecipeData data, RecipeIngredient ingredient) {
        setVisualIngredientData(0, ingredient);
        writeKaleidoscopeSteamerRecipe(data);
    }

    private void setKaleidoscopeTeapotSlotIngredient(KaleidoscopeTeapotRecipeData data, RecipeIngredient ingredient) {
        setVisualIngredientData(0, ingredient);
        writeKaleidoscopeTeapotRecipe(data);
    }

    private void setArsNouveauApparatusSlotIngredient(ArsNouveauApparatusRecipeData data, int index, RecipeIngredient ingredient) {
        setVisualIngredientData(index, ingredient);
        writeArsNouveauApparatusRecipe(data);
    }

    private void setArsNouveauArmorUpgradeSlotIngredient(ArsNouveauArmorUpgradeRecipeData data, int index, RecipeIngredient ingredient) {
        setVisualIngredientData(index, ingredient);
        writeArsNouveauArmorUpgradeRecipe(data);
    }

    private void setArsNouveauEnchantmentSlotIngredient(ArsNouveauEnchantmentRecipeData data, int index, RecipeIngredient ingredient) {
        setVisualIngredientData(index, ingredient);
        writeArsNouveauEnchantmentRecipe(data);
    }

    private void setArsNouveauImbuementSlotIngredient(ArsNouveauImbuementRecipeData data, int index, RecipeIngredient ingredient) {
        setVisualIngredientData(index, ingredient);
        writeArsNouveauImbuementRecipe(data);
    }

    private void setArsNouveauGlyphSlotIngredient(ArsNouveauGlyphRecipeData data, int index, RecipeIngredient ingredient) {
        setVisualIngredientData(index, ingredient);
        writeArsNouveauGlyphRecipe(data);
    }

    private void setArsNouveauCrushSlotIngredient(ArsNouveauCrushRecipeData data, RecipeIngredient ingredient) {
        setVisualIngredientData(0, ingredient);
        writeArsNouveauCrushRecipe(data);
    }

    private void setArsNouveauPedestalOnlySlotIngredient(ArsNouveauPedestalOnlyRecipeData data, int index, RecipeIngredient ingredient) {
        setVisualIngredientData(index, ingredient);
        writeArsNouveauPedestalOnlyRecipe(data);
    }

    private void setVisualIngredientData(int index, RecipeIngredient ingredient) {
        visualIngredients[index] = itemFromIngredient(ingredient);
        visualIngredientData[index] = ingredient == null ? new RecipeIngredient() : ingredient;
        visualRemainders[index] = CraftingRemainderRule.defaultRule();
    }

    private void setCreateSequencedAssemblySlotIngredient(CreateSequencedAssemblyRecipeData data, int index, RecipeIngredient ingredient) {
        visualIngredients[index] = itemFromIngredient(ingredient);
        visualIngredientData[index] = ingredient == null ? new RecipeIngredient() : ingredient;
        visualRemainders[index] = CraftingRemainderRule.defaultRule();
        writeCreateSequencedAssemblyRecipe(data);
    }

    private void setCreateMechanicalCraftingSlotIngredient(CreateMechanicalCraftingRecipeData data, int index, RecipeIngredient ingredient) {
        visualIngredients[index] = itemFromIngredient(ingredient);
        visualIngredientData[index] = ingredient == null ? new RecipeIngredient() : ingredient;
        visualRemainders[index] = CraftingRemainderRule.defaultRule();
        writeCreateMechanicalCraftingRecipe(data);
    }

    private void setCreateProcessingSlotIngredient(RecipeEntry entry, int index, RecipeIngredient ingredient) {
        if (isCreateAutoPackingEntry(entry)) {
            setCreateAutoPackingIngredient(entry, ingredient);
            return;
        }
        var kind = CreateProcessingKind.byType(entry.getType()).orElse(null);
        visualIngredients[index] = supportsCreateCountedItemInputs(kind) ? itemFromCreateIngredient(ingredient) : itemFromIngredient(ingredient);
        visualIngredientData[index] = ingredient == null ? new RecipeIngredient() : ingredient;
        visualRemainders[index] = CraftingRemainderRule.defaultRule();
        writeCreateProcessingRecipe(entry);
    }

    private void setExtendedCraftingTableSlotIngredient(RecipeEntry entry, int index, RecipeIngredient ingredient) {
        setVisualIngredientData(index, ingredient);
        writeExtendedCraftingTableRecipe(entry.getExtendedCraftingTable());
    }

    private void setExtendedCraftingEnderCrafterSlotIngredient(RecipeEntry entry, int index, RecipeIngredient ingredient) {
        setVisualIngredientData(index, ingredient);
        writeExtendedCraftingEnderCrafterRecipe(entry);
    }

    private void setExtendedCraftingFluxCrafterSlotIngredient(RecipeEntry entry, int index, RecipeIngredient ingredient) {
        setVisualIngredientData(index, ingredient);
        writeExtendedCraftingFluxCrafterRecipe(entry);
    }

    private void setExtendedCraftingCombinationSlotIngredient(ExtendedCraftingCombinationRecipeData data, int index, RecipeIngredient ingredient) {
        setVisualIngredientData(index, ingredient);
        writeExtendedCraftingCombinationRecipe(data);
    }

    private void setExtendedCraftingCompressorSlotIngredient(ExtendedCraftingCompressorRecipeData data, int index, RecipeIngredient ingredient) {
        setVisualIngredientData(index, ingredient);
        writeExtendedCraftingCompressorRecipe(data);
    }

    private void setAvaritiaTableSlotIngredient(RecipeEntry entry, int index, RecipeIngredient ingredient) {
        setVisualIngredientData(index, ingredient);
        writeAvaritiaTableRecipe(entry);
    }

    private void setAvaritiaCompressorSlotIngredient(AvaritiaCompressorRecipeData data, RecipeIngredient ingredient) {
        setVisualIngredientData(0, ingredient);
        writeAvaritiaCompressorRecipe(data);
    }

    private void setAvaritiaExtremeSmithingSlotIngredient(RecipeEntry entry, int index, RecipeIngredient ingredient) {
        setVisualIngredientData(index, ingredient);
        writeAvaritiaExtremeSmithingRecipe(entry);
    }

    private void setCreateAutoPackingIngredient(RecipeEntry entry, RecipeIngredient ingredient) {
        writeRepeatedAutoPackingIngredient(entry, ingredient, getCreateAutoPackingGridSize(entry));
    }

    private void setFarmerCookingPotSlotIngredient(FarmerCookingPotRecipeData cookingPot, int index, RecipeIngredient ingredient) {
        visualIngredients[index] = itemFromIngredient(ingredient);
        visualIngredientData[index] = ingredient == null ? new RecipeIngredient() : ingredient;
        visualRemainders[index] = CraftingRemainderRule.defaultRule();
        writeFarmerCookingPotRecipe(cookingPot);
    }

    private void setFarmerCuttingSlotIngredient(FarmerCuttingRecipeData cutting, int index, RecipeIngredient ingredient) {
        visualIngredients[index] = itemFromIngredient(ingredient);
        visualIngredientData[index] = ingredient == null ? new RecipeIngredient() : ingredient;
        visualRemainders[index] = CraftingRemainderRule.defaultRule();
        writeFarmerCuttingRecipe(cutting);
    }

    private void setDragonForgeSlotIngredient(DragonForgeRecipeData dragonForge, int index, RecipeIngredient ingredient) {
        visualIngredients[index] = itemFromIngredient(ingredient);
        visualIngredientData[index] = ingredient == null ? new RecipeIngredient() : ingredient;
        visualRemainders[index] = CraftingRemainderRule.defaultRule();
        writeDragonForgeRecipe(dragonForge);
    }

    private void setCataclysmWeaponFusionSlotIngredient(CataclysmWeaponFusionRecipeData data, int index, RecipeIngredient ingredient) {
        visualIngredients[index] = itemFromIngredient(ingredient);
        visualIngredientData[index] = ingredient == null ? new RecipeIngredient() : ingredient;
        visualRemainders[index] = CraftingRemainderRule.defaultRule();
        writeCataclysmWeaponFusionRecipe(data);
    }

    private void setCataclysmAmethystBlessSlotIngredient(CataclysmAmethystBlessRecipeData data, RecipeIngredient ingredient) {
        visualIngredients[0] = itemFromIngredient(ingredient);
        visualIngredientData[0] = ingredient == null ? new RecipeIngredient() : ingredient;
        visualRemainders[0] = CraftingRemainderRule.defaultRule();
        writeCataclysmAmethystBlessRecipe(data);
    }

    private void setIronNoAdditionSmithingSlotIngredient(IronNoAdditionSmithingRecipeData smithing, int index, RecipeIngredient ingredient) {
        visualIngredients[index] = itemFromIngredient(ingredient);
        visualIngredientData[index] = ingredient == null ? new RecipeIngredient() : ingredient;
        visualRemainders[index] = CraftingRemainderRule.defaultRule();
        writeIronNoAdditionSmithingRecipe(smithing);
    }

    private void setIronArcaneAnvilSlotIngredient(IronArcaneAnvilRecipeData arcaneAnvil, int index, RecipeIngredient ingredient) {
        visualIngredients[index] = itemFromIngredient(ingredient);
        visualIngredientData[index] = ingredient == null ? new RecipeIngredient() : ingredient;
        visualRemainders[index] = CraftingRemainderRule.defaultRule();
        writeIronArcaneAnvilRecipe(arcaneAnvil);
    }

    private void setIronAlchemistCauldronIngredient(RecipeEntry entry, RecipeIngredient ingredient) {
        visualIngredients[0] = itemFromIngredient(ingredient);
        visualIngredientData[0] = ingredient == null ? new RecipeIngredient() : ingredient;
        visualRemainders[0] = CraftingRemainderRule.defaultRule();
        writeIronAlchemistCauldronRecipe(entry);
    }

    private void setSmithingSlotIngredient(SmithingTransformRecipeData smithingTransform, int index, RecipeIngredient ingredient) {
        visualIngredients[index] = itemFromIngredient(ingredient);
        visualIngredientData[index] = ingredient == null ? new RecipeIngredient() : ingredient;
        visualRemainders[index] = CraftingRemainderRule.defaultRule();
        writeSmithingTransformRecipe(smithingTransform);
    }

    private void setStonecuttingSlotIngredient(StonecuttingRecipeData stonecutting, RecipeIngredient ingredient) {
        visualIngredients[0] = itemFromIngredient(ingredient);
        visualIngredientData[0] = ingredient == null ? new RecipeIngredient() : ingredient;
        visualRemainders[0] = CraftingRemainderRule.defaultRule();
        writeStonecuttingRecipe(stonecutting);
    }

    private void setCookingSlotIngredient(CookingRecipeData cooking, RecipeIngredient ingredient) {
        visualIngredients[0] = itemFromIngredient(ingredient);
        visualIngredientData[0] = ingredient == null ? new RecipeIngredient() : ingredient;
        visualRemainders[0] = CraftingRemainderRule.defaultRule();
        writeCookingRecipe(cooking);
    }

    private void setShapelessSlotIngredient(ShapelessCraftingRecipeData shapeless, int index, RecipeIngredient ingredient) {
        visualIngredients[index] = itemFromIngredient(ingredient);
        visualIngredientData[index] = ingredient == null ? new RecipeIngredient() : ingredient;
        visualRemainders[index] = CraftingRemainderRule.defaultRule();
        writeShapelessRecipe(shapeless);
    }

    private void setShapedSlotIngredient(ShapedCraftingRecipeData shaped, int index, RecipeIngredient ingredient) {
        visualIngredients[index] = itemFromIngredient(ingredient);
        visualIngredientData[index] = ingredient == null ? new RecipeIngredient() : ingredient;
        if (isIngredientEmpty(visualIngredientData[index])) {
            visualRemainders[index] = CraftingRemainderRule.defaultRule();
        }
        writeShapedRecipe(shaped);
    }

    private boolean isIngredientEmpty(RecipeIngredient ingredient) {
        if (ingredient == null || ingredient.getValues().isEmpty()) {
            return true;
        }
        for (var value : ingredient.getValues()) {
            switch (value.getKind()) {
                case ITEM -> {
                    if (value.getItem() != null && !value.getItem().isEmpty()) {
                        return false;
                    }
                }
                case TAG -> {
                    if (value.getTag() != null) {
                        return false;
                    }
                }
                case ITEM_ABILITY -> {
                    if (value.getItemAbility() != null && !value.getItemAbility().isBlank()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean isCreateFluidIngredientEmpty(CreateFluidIngredientData ingredient) {
        if (ingredient == null) {
            return true;
        }
        var kind = ingredient.getKind() == null ? CreateFluidIngredientKind.FLUID : ingredient.getKind();
        if (kind == CreateFluidIngredientKind.TAG) {
            return ingredient.getTag() == null || ingredient.getAmount() <= 0;
        }
        return ingredient.getFluid() == null || ingredient.getFluid().isEmpty() || ingredient.getFluid().getAmount() <= 0;
    }

    private void loadSelectedEntryToVisualState() {
        refreshing = true;
        try {
            clearVisualState();
            selectedContainsUnsupportedIngredients = false;
            if (selectedEntry == null) {
                return;
            }
            if (selectedEntry.isType(RecipeEditorTypes.CRAFTING_SHAPED)) {
                loadShaped(selectedEntry.getShaped());
            } else if (selectedEntry.isType(RecipeEditorTypes.CRAFTING_SHAPELESS)) {
                loadShapeless(selectedEntry.getShapeless());
            } else if (isCookingEntry(selectedEntry)) {
                loadCooking(selectedEntry.getCooking());
            } else if (isStonecuttingEntry(selectedEntry)) {
                loadStonecutting(selectedEntry.getStonecutting());
            } else if (isSmithingTransformEntry(selectedEntry)) {
                loadSmithingTransform(selectedEntry.getSmithingTransform());
            } else if (isIronAlchemistCauldronEntry(selectedEntry)) {
                loadIronAlchemistCauldron(selectedEntry);
            } else if (isIronArcaneAnvilEntry(selectedEntry)) {
                loadIronArcaneAnvil(selectedEntry.getIronArcaneAnvil());
            } else if (isIronNoAdditionSmithingEntry(selectedEntry)) {
                loadIronNoAdditionSmithing(selectedEntry.getIronNoAdditionSmithing());
            } else if (isIceAndFireDragonForgeEntry(selectedEntry)) {
                loadDragonForge(selectedEntry.getIceAndFireDragonForge());
            } else if (isCataclysmWeaponFusionEntry(selectedEntry)) {
                loadCataclysmWeaponFusion(selectedEntry.getCataclysmWeaponFusion());
            } else if (isCataclysmAmethystBlessEntry(selectedEntry)) {
                loadCataclysmAmethystBless(selectedEntry.getCataclysmAmethystBless());
            } else if (isTouhouLittleMaidAltarEntry(selectedEntry)) {
                loadTouhouLittleMaidAltar(selectedEntry.getTouhouLittleMaidAltar());
            } else if (isSporeSurgeryEntry(selectedEntry)) {
                loadSporeSurgery(selectedEntry.getSporeSurgery());
            } else if (isSporeGraftingEntry(selectedEntry)) {
                loadSporeGrafting(selectedEntry.getSporeGrafting());
            } else if (isGoetyCursedInfuserEntry(selectedEntry)) {
                loadGoetyCursedInfuser(selectedEntry.getGoetyCursedInfuser());
            } else if (isGoetyRitualEntry(selectedEntry)) {
                loadGoetyRitual(selectedEntry.getGoetyRitual());
            } else if (isGoetyBrazierEntry(selectedEntry)) {
                loadGoetyBrazier(selectedEntry.getGoetyBrazier());
            } else if (isGoetyPulverizeEntry(selectedEntry)) {
                loadGoetyPulverize(selectedEntry.getGoetyPulverize());
            } else if (isGoetyBrewingEntry(selectedEntry)) {
                loadGoetyBrewing(selectedEntry.getGoetyBrewing());
            } else if (isFarmersCookingPotEntry(selectedEntry)) {
                loadFarmerCookingPot(selectedEntry.getFarmerCookingPot());
            } else if (isFarmersCuttingBoardEntry(selectedEntry)) {
                loadFarmerCutting(selectedEntry.getFarmerCuttingBoard());
            } else if (isKaleidoscopePotEntry(selectedEntry)) {
                loadKaleidoscopePot(selectedEntry.getKaleidoscopePot());
            } else if (isKaleidoscopeStockpotEntry(selectedEntry)) {
                loadKaleidoscopeStockpot(selectedEntry.getKaleidoscopeStockpot());
            } else if (isKaleidoscopeMillstoneEntry(selectedEntry)) {
                loadKaleidoscopeMillstone(selectedEntry.getKaleidoscopeMillstone());
            } else if (isKaleidoscopeChoppingBoardEntry(selectedEntry)) {
                loadKaleidoscopeChoppingBoard(selectedEntry.getKaleidoscopeChoppingBoard());
            } else if (isKaleidoscopeSteamerEntry(selectedEntry)) {
                loadKaleidoscopeSteamer(selectedEntry.getKaleidoscopeSteamer());
            } else if (isKaleidoscopeTeapotEntry(selectedEntry)) {
                loadKaleidoscopeTeapot(selectedEntry.getKaleidoscopeTeapot());
            } else if (isCreateMechanicalCraftingEntry(selectedEntry)) {
                loadCreateMechanicalCrafting(selectedEntry.getCreateMechanicalCrafting());
            } else if (isCreateSequencedAssemblyEntry(selectedEntry)) {
                loadCreateSequencedAssembly(selectedEntry.getCreateSequencedAssembly());
            } else if (isCreateProcessingEntry(selectedEntry)) {
                loadCreateProcessing(selectedEntry);
            } else if (isExtendedCraftingTableEntry(selectedEntry)) {
                loadExtendedCraftingTable(selectedEntry);
            } else if (isExtendedCraftingUltimateSingularityEntry(selectedEntry)) {
                loadExtendedCraftingUltimateSingularity(selectedEntry);
            } else if (isExtendedCraftingEnderCrafterEntry(selectedEntry)) {
                loadExtendedCraftingEnderCrafter(selectedEntry);
            } else if (isExtendedCraftingFluxCrafterEntry(selectedEntry)) {
                loadExtendedCraftingFluxCrafter(selectedEntry);
            } else if (isExtendedCraftingCombinationEntry(selectedEntry)) {
                loadExtendedCraftingCombination(selectedEntry.getExtendedCraftingCombination());
            } else if (isExtendedCraftingCompressorEntry(selectedEntry)) {
                loadExtendedCraftingCompressor(selectedEntry.getExtendedCraftingCompressor());
            } else if (isAvaritiaTableEntry(selectedEntry)) {
                loadAvaritiaTable(selectedEntry);
            } else if (isAvaritiaCompressorEntry(selectedEntry)) {
                loadAvaritiaCompressor(selectedEntry.getAvaritiaCompressor());
            } else if (isAvaritiaExtremeSmithingEntry(selectedEntry)) {
                loadAvaritiaExtremeSmithing(selectedEntry);
            } else if (isArsNouveauApparatusEntry(selectedEntry)) {
                loadArsNouveauApparatus(selectedEntry.getArsNouveauApparatus());
            } else if (isArsNouveauArmorUpgradeEntry(selectedEntry)) {
                loadArsNouveauArmorUpgrade(selectedEntry.getArsNouveauArmorUpgrade());
            } else if (isArsNouveauEnchantmentEntry(selectedEntry)) {
                loadArsNouveauEnchantment(selectedEntry.getArsNouveauEnchantment());
            } else if (isArsNouveauImbuementEntry(selectedEntry)) {
                loadArsNouveauImbuement(selectedEntry.getArsNouveauImbuement());
            } else if (isArsNouveauGlyphEntry(selectedEntry)) {
                loadArsNouveauGlyph(selectedEntry.getArsNouveauGlyph());
            } else if (isArsNouveauCrushEntry(selectedEntry)) {
                loadArsNouveauCrush(selectedEntry.getArsNouveauCrush());
            } else if (isArsNouveauPedestalOnlyEntry(selectedEntry)) {
                loadArsNouveauPedestalOnly(selectedEntry.getArsNouveauPedestalOnly());
            }
            loadedIngredientStacks = copyStacks(visualIngredients);
            loadedRemainders = copyRemainders(visualRemainders);
        } finally {
            refreshing = false;
        }
    }

    private void clearVisualState() {
        visualIngredients = emptyIngredientStacks();
        visualIngredientData = emptyIngredientData();
        visualRemainders = emptyRemainderData();
        visualResult = ItemStack.EMPTY;
        visualContainer = ItemStack.EMPTY;
        visualCuttingResults = emptyCuttingResultStacks();
        visualCuttingChances = emptyCuttingResultChances();
        visualCreateOutputs = emptyCreateOutputStacks();
        visualCreateOutputChances = emptyCreateOutputChances();
        visualCreateFluidInputs = emptyCreateFluidInputs();
        visualCreateFluidOutputs = emptyCreateFluidOutputs();
        visualCreateSequencedTransitional = ItemStack.EMPTY;
        visualArsNouveauOutputs = emptyArsNouveauOutputStacks();
        visualArsNouveauOutputChances = emptyArsNouveauOutputChances();
        visualArsNouveauOutputMaxRanges = emptyArsNouveauOutputMaxRanges();
        loadedIngredientStacks = emptyIngredientStacks();
        loadedRemainders = emptyRemainderData();
        selectedContainsUnsupportedIngredients = false;
    }

    private void loadShaped(ShapedCraftingRecipeData shaped) {
        var key = new LinkedHashMap<Character, ItemStack>();
        for (var entry : shaped.getKey()) {
            var symbol = entry.getSymbol();
            if (symbol == null || symbol.length() != 1) {
                continue;
            }
            if (containsUnsupportedIngredientValue(entry.getIngredient())) {
                selectedContainsUnsupportedIngredients = true;
            }
            key.put(symbol.charAt(0), itemFromIngredient(entry.getIngredient()));
        }
        for (int row = 0; row < Math.min(3, shaped.getPattern().size()); row++) {
            var line = shaped.getPattern().get(row);
            for (int col = 0; col < Math.min(3, line.length()); col++) {
                var symbol = line.charAt(col);
                var stack = key.getOrDefault(symbol, ItemStack.EMPTY);
                if (line.charAt(col) != ' ' && stack.isEmpty()) {
                    selectedContainsUnsupportedIngredients = true;
                }
                visualIngredients[row * 3 + col] = stack.copy();
                visualIngredientData[row * 3 + col] = ingredientForSymbol(shaped, symbol);
                visualRemainders[row * 3 + col] = remainderForPatternSlot(shaped, row, col);
            }
        }
        visualResult = shaped.getResult().copy();
    }

    private void loadCreateMechanicalCrafting(CreateMechanicalCraftingRecipeData data) {
        var key = new LinkedHashMap<Character, RecipeIngredient>();
        for (var entry : data.getKey()) {
            var symbol = entry.getSymbol();
            if (symbol == null || symbol.length() != 1) {
                continue;
            }
            if (containsUnsupportedIngredientValue(entry.getIngredient())) {
                selectedContainsUnsupportedIngredients = true;
            }
            key.put(symbol.charAt(0), entry.getIngredient());
        }
        var height = data.normalizedHeight();
        var width = data.normalizedWidth();
        for (int row = 0; row < Math.min(height, data.getPattern().size()); row++) {
            var line = data.getPattern().get(row);
            if (line == null) {
                continue;
            }
            for (int col = 0; col < Math.min(width, line.length()); col++) {
                var symbol = line.charAt(col);
                var index = row * MECHANICAL_CRAFTING_GRID_SIZE + col;
                var ingredient = symbol == ' ' ? new RecipeIngredient() : key.getOrDefault(symbol, new RecipeIngredient());
                if (symbol != ' ' && isIngredientEmpty(ingredient)) {
                    selectedContainsUnsupportedIngredients = true;
                }
                visualIngredients[index] = itemFromIngredient(ingredient);
                visualIngredientData[index] = ingredient;
                visualRemainders[index] = CraftingRemainderRule.defaultRule();
            }
        }
        visualResult = data.getResult() == null ? ItemStack.EMPTY : data.getResult().copy();
    }

    private void loadShapeless(ShapelessCraftingRecipeData shapeless) {
        for (int i = 0; i < Math.min(9, shapeless.getIngredients().size()); i++) {
            var ingredient = shapeless.getIngredients().get(i);
            if (containsUnsupportedIngredientValue(ingredient)) {
                selectedContainsUnsupportedIngredients = true;
            }
            visualIngredients[i] = itemFromIngredient(ingredient);
            visualIngredientData[i] = ingredient;
        }
        visualResult = shapeless.getResult().copy();
    }

    private void loadCooking(CookingRecipeData cooking) {
        var ingredient = cooking.getIngredient();
        if (containsUnsupportedIngredientValue(ingredient)) {
            selectedContainsUnsupportedIngredients = true;
        }
        visualIngredients[0] = itemFromIngredient(ingredient);
        visualIngredientData[0] = ingredient == null ? new RecipeIngredient() : ingredient;
        visualRemainders[0] = CraftingRemainderRule.defaultRule();
        visualResult = cooking.getResult().copy();
    }

    private void loadStonecutting(StonecuttingRecipeData stonecutting) {
        var ingredient = stonecutting.getIngredient();
        if (containsUnsupportedIngredientValue(ingredient)) {
            selectedContainsUnsupportedIngredients = true;
        }
        visualIngredients[0] = itemFromIngredient(ingredient);
        visualIngredientData[0] = ingredient == null ? new RecipeIngredient() : ingredient;
        visualRemainders[0] = CraftingRemainderRule.defaultRule();
        visualResult = stonecutting.getResult().copy();
    }

    private void loadSmithingTransform(SmithingTransformRecipeData smithingTransform) {
        loadSmithingIngredientSlot(0, smithingTransform.getTemplate());
        loadSmithingIngredientSlot(1, smithingTransform.getBase());
        loadSmithingIngredientSlot(2, smithingTransform.getAddition());
        visualResult = smithingTransform.getResult().copy();
    }

    private void loadSmithingIngredientSlot(int index, RecipeIngredient ingredient) {
        if (containsUnsupportedIngredientValue(ingredient)) {
            selectedContainsUnsupportedIngredients = true;
        }
        visualIngredients[index] = itemFromIngredient(ingredient);
        visualIngredientData[index] = ingredient == null ? new RecipeIngredient() : ingredient;
        visualRemainders[index] = CraftingRemainderRule.defaultRule();
    }

    private void loadIronAlchemistCauldron(RecipeEntry entry) {
        var data = entry.getIronAlchemistCauldron();
        var ingredient = data.getInput();
        if (containsUnsupportedIngredientValue(ingredient)) {
            selectedContainsUnsupportedIngredients = true;
        }
        visualIngredients[0] = itemFromIngredient(ingredient);
        visualIngredientData[0] = ingredient == null ? new RecipeIngredient() : ingredient;
        visualRemainders[0] = CraftingRemainderRule.defaultRule();
        visualResult = isIronAlchemistCauldronBrewEntry(entry)
                ? ItemStack.EMPTY
                : (data.getResult() == null ? ItemStack.EMPTY : data.getResult().copy());
    }

    private void loadIronNoAdditionSmithing(IronNoAdditionSmithingRecipeData smithing) {
        loadSmithingIngredientSlot(0, smithing.getTemplate());
        loadSmithingIngredientSlot(1, smithing.getBase());
        visualResult = smithing.getResult().copy();
    }

    private void loadIronArcaneAnvil(IronArcaneAnvilRecipeData arcaneAnvil) {
        loadSmithingIngredientSlot(0, arcaneAnvil.getInput());
        loadSmithingIngredientSlot(1, arcaneAnvil.getMaterial());
        visualResult = arcaneAnvil.getResult().copy();
    }

    private void loadDragonForge(DragonForgeRecipeData dragonForge) {
        loadSmithingIngredientSlot(0, dragonForge.getInput());
        loadSmithingIngredientSlot(1, dragonForge.getBlood());
        visualResult = dragonForge.getResult().copy();
    }

    private void loadCataclysmWeaponFusion(CataclysmWeaponFusionRecipeData data) {
        loadIngredientSlot(0, data.getBase());
        loadIngredientSlot(1, data.getAddition());
        visualResult = data.getResult() == null ? ItemStack.EMPTY : data.getResult().copy();
    }

    private void loadCataclysmAmethystBless(CataclysmAmethystBlessRecipeData data) {
        loadIngredientSlot(0, data.getIngredient());
        visualResult = data.getResult() == null ? ItemStack.EMPTY : data.getResult().copy();
    }

    private void loadTouhouLittleMaidAltar(TouhouLittleMaidAltarRecipeData data) {
        for (int index = 0; index < TouhouLittleMaidAltarRecipeData.INPUT_COUNT; index++) {
            loadIngredientSlot(index, data.ingredient(index));
        }
        visualResult = data.getResult() == null ? ItemStack.EMPTY : data.getResult().copy();
    }

    private void loadSporeSurgery(SporeSurgeryRecipeData surgery) {
        for (int i = 0; i < SporeSurgeryRecipeData.INPUT_COUNT; i++) {
            loadIngredientSlot(i, surgery.ingredient(i));
        }
        visualResult = surgery.getResult() == null ? ItemStack.EMPTY : surgery.getResult().copy();
    }

    private void loadSporeGrafting(SporeGraftingRecipeData grafting) {
        for (int i = 0; i < SporeGraftingRecipeData.INPUT_COUNT; i++) {
            loadIngredientSlot(i, grafting.ingredient(i));
        }
        visualResult = grafting.getResult() == null ? ItemStack.EMPTY : grafting.getResult().copy();
    }

    private void loadGoetyCursedInfuser(GoetyCursedInfuserRecipeData data) {
        loadIngredientSlot(0, data.getIngredient());
        visualResult = data.getResult() == null ? ItemStack.EMPTY : data.getResult().copy();
    }

    private void loadGoetyRitual(GoetyRitualRecipeData data) {
        loadIngredientSlot(0, data.getActivationItem());
        for (int i = 0; i < GoetyRitualRecipeData.MAX_PEDESTAL_INGREDIENTS; i++) {
            loadIngredientSlot(i + 1, data.ingredient(i));
        }
        visualResult = data.getResult() == null ? ItemStack.EMPTY : data.getResult().copy();
    }

    private void loadGoetyBrazier(GoetyBrazierRecipeData data) {
        for (int i = 0; i < GoetyBrazierRecipeData.INPUT_COUNT; i++) {
            loadIngredientSlot(i, data.ingredient(i));
        }
        visualResult = data.getResult() == null ? ItemStack.EMPTY : data.getResult().copy();
    }

    private void loadGoetyPulverize(GoetyPulverizeRecipeData data) {
        loadIngredientSlot(0, data.getIngredient());
        visualResult = data.visibleResult();
    }

    private void loadGoetyBrewing(GoetyBrewingRecipeData data) {
        loadIngredientSlot(0, data.getIngredient());
        visualResult = data.visibleResult();
    }

    private void loadFarmerCookingPot(FarmerCookingPotRecipeData cookingPot) {
        var ingredients = cookingPot.getIngredients();
        if (ingredients != null) {
            for (int i = 0; i < Math.min(6, ingredients.size()); i++) {
                loadIngredientSlot(i, ingredients.get(i));
            }
        }
        visualResult = cookingPot.getResult() == null ? ItemStack.EMPTY : cookingPot.getResult().copy();
        visualContainer = cookingPot.getContainer() == null ? ItemStack.EMPTY : cookingPot.getContainer().copy();
    }

    private void loadFarmerCutting(FarmerCuttingRecipeData cutting) {
        loadIngredientSlot(0, cutting.getInput());
        loadIngredientSlot(1, cutting.getTool());
        visualCuttingResults = emptyCuttingResultStacks();
        visualCuttingChances = emptyCuttingResultChances();
        var results = cutting.getResults();
        if (results != null) {
            for (int i = 0; i < Math.min(4, results.size()); i++) {
                var result = results.get(i);
                if (result == null) {
                    continue;
                }
                visualCuttingResults[i] = result.getItem() == null ? ItemStack.EMPTY : result.getItem().copy();
                visualCuttingChances[i] = Math.max(0, Math.min(1, result.getChance()));
            }
        }
        visualResult = visualCuttingResults[0].copy();
    }

    private void loadKaleidoscopePot(KaleidoscopePotRecipeData data) {
        loadIngredientList(data.getIngredients(), KALEIDOSCOPE_MAX_INPUTS);
        loadIngredientSlot(KALEIDOSCOPE_CARRIER_SLOT, data.getCarrier());
        visualResult = data.getResult() == null ? ItemStack.EMPTY : data.getResult().copy();
    }

    private void loadKaleidoscopeStockpot(KaleidoscopeStockpotRecipeData data) {
        loadIngredientList(data.getIngredients(), KALEIDOSCOPE_MAX_INPUTS);
        loadIngredientSlot(KALEIDOSCOPE_CARRIER_SLOT, data.getCarrier());
        visualResult = data.getResult() == null ? ItemStack.EMPTY : data.getResult().copy();
    }

    private void loadKaleidoscopeMillstone(KaleidoscopeMillstoneRecipeData data) {
        loadIngredientSlot(0, data.getIngredient());
        visualResult = data.getResult() == null ? ItemStack.EMPTY : data.getResult().copy();
    }

    private void loadKaleidoscopeChoppingBoard(KaleidoscopeChoppingBoardRecipeData data) {
        loadIngredientSlot(0, data.getIngredient());
        visualResult = data.getResult() == null ? ItemStack.EMPTY : data.getResult().copy();
    }

    private void loadKaleidoscopeSteamer(KaleidoscopeSteamerRecipeData data) {
        loadIngredientSlot(0, data.getIngredient());
        visualResult = data.getResult() == null ? ItemStack.EMPTY : data.getResult().copy();
    }

    private void loadKaleidoscopeTeapot(KaleidoscopeTeapotRecipeData data) {
        loadIngredientSlot(0, data.getIngredient());
        visualResult = data.getResult() == null ? ItemStack.EMPTY : data.getResult().copy();
    }

    private void loadIngredientList(List<RecipeIngredient> ingredients, int maxInputs) {
        if (ingredients == null) {
            return;
        }
        for (int i = 0; i < Math.min(maxInputs, ingredients.size()); i++) {
            loadIngredientSlot(i, ingredients.get(i));
        }
    }

    private void loadCreateSequencedAssembly(CreateSequencedAssemblyRecipeData data) {
        loadIngredientSlot(0, data.getIngredient());
        visualCreateSequencedTransitional = data.getTransitionalItem() == null
                ? ItemStack.EMPTY
                : data.getTransitionalItem().copyWithCount(1);
        var sequence = data.getSequence();
        if (sequence != null) {
            for (int i = 0; i < Math.min(CREATE_SEQUENCED_MAX_STEPS, sequence.size()); i++) {
                var step = sequence.get(i);
                if (step == null) {
                    continue;
                }
                loadIngredientSlot(createSequencedIngredientSlotIndex(i), step.getIngredient());
            }
        }
        visualCreateOutputs = emptyCreateOutputStacks();
        visualCreateOutputChances = emptyCreateOutputChances();
        var outputs = data.getOutputs();
        if (outputs != null) {
            for (int i = 0; i < Math.min(CREATE_SEQUENCED_MAX_OUTPUTS, outputs.size()); i++) {
                var output = outputs.get(i);
                if (output == null) {
                    continue;
                }
                visualCreateOutputs[i] = output.getItem() == null ? ItemStack.EMPTY : output.getItem().copy();
                visualCreateOutputChances[i] = Math.max(0, output.getChance());
            }
        }
        visualResult = visualCreateOutputs[0].copy();
    }

    private void loadCreateProcessing(RecipeEntry entry) {
        var kind = CreateProcessingKind.byType(entry.getType()).orElse(null);
        if (kind == null) {
            return;
        }
        var data = entry.getCreateProcessing();
        if (kind == CreateProcessingKind.AUTO_PACKING) {
            loadCreateAutoPacking(data);
            return;
        }
        var ingredients = data.getIngredients();
        if (ingredients != null) {
            var visualIngredientCount = createVisibleItemInputCapacity(kind);
            if (supportsCreateCountedItemInputs(kind)) {
                var remainingIngredientWeight = visualIngredientCount;
                var visualIndex = 0;
                for (var ingredient : ingredients) {
                    if (visualIndex >= visualIngredientCount || remainingIngredientWeight <= 0) {
                        break;
                    }
                    var normalizedIngredient = CreateItemInputCounts.copyWithClampedWeight(ingredient, remainingIngredientWeight);
                    if (isIngredientEmpty(normalizedIngredient)) {
                        continue;
                    }
                    loadCreateIngredientSlot(visualIndex, normalizedIngredient);
                    remainingIngredientWeight -= Math.max(1, CreateItemInputCounts.slotWeight(normalizedIngredient));
                    visualIndex++;
                }
            } else {
                for (int i = 0; i < Math.min(visualIngredientCount, ingredients.size()); i++) {
                    loadIngredientSlot(i, ingredients.get(i));
                }
            }
        }
        visualCreateFluidInputs = emptyCreateFluidInputs();
        var fluidInputs = data.getFluidIngredients();
        if (fluidInputs != null) {
            for (int i = 0; i < Math.min(Math.min(CREATE_MAX_FLUID_INPUTS, kind.maxFluidInputs()), fluidInputs.size()); i++) {
                var input = fluidInputs.get(i);
                visualCreateFluidInputs[i] = input == null ? CreateFluidIngredientData.empty() : input.copy();
            }
        }
        visualCreateOutputs = emptyCreateOutputStacks();
        visualCreateOutputChances = emptyCreateOutputChances();
        var outputs = data.getOutputs();
        if (outputs != null) {
            for (int i = 0; i < Math.min(Math.min(CREATE_MAX_ITEM_OUTPUTS, kind.maxItemOutputs()), outputs.size()); i++) {
                var output = outputs.get(i);
                if (output == null) {
                    continue;
                }
                visualCreateOutputs[i] = output.getItem() == null ? ItemStack.EMPTY : output.getItem().copy();
                visualCreateOutputChances[i] = Math.max(0, Math.min(1, output.getChance()));
            }
        }
        visualCreateFluidOutputs = emptyCreateFluidOutputs();
        var fluidOutputs = data.getFluidOutputs();
        if (fluidOutputs != null) {
            for (int i = 0; i < Math.min(Math.min(CREATE_MAX_FLUID_OUTPUTS, kind.maxFluidOutputs()), fluidOutputs.size()); i++) {
                var output = fluidOutputs.get(i);
                visualCreateFluidOutputs[i] = output == null ? FluidStack.EMPTY : output.copy();
            }
        }
        visualResult = visualCreateOutputs[0].copy();
    }

    private void loadCreateAutoPacking(CreateProcessingRecipeData data) {
        var gridSize = autoPackingGridSize(data);
        var ingredient = firstCreateIngredient(data);
        if (containsUnsupportedIngredientValue(ingredient)) {
            selectedContainsUnsupportedIngredients = true;
        }
        refreshAutoPackingVisualIngredients(ingredient, gridSize);
        visualCreateOutputs = emptyCreateOutputStacks();
        visualCreateOutputChances = emptyCreateOutputChances();
        var outputs = data.getOutputs();
        if (outputs != null && !outputs.isEmpty()) {
            var output = outputs.get(0);
            if (output != null) {
                visualCreateOutputs[0] = output.getItem() == null ? ItemStack.EMPTY : output.getItem().copy();
                visualCreateOutputChances[0] = Math.max(0, Math.min(1, output.getChance()));
            }
        }
        visualResult = visualCreateOutputs[0].copy();
    }

    private void loadExtendedCraftingTable(RecipeEntry entry) {
        var data = entry.getExtendedCraftingTable();
        if (isExtendedCraftingShapedTableEntry(entry)) {
            var gridSize = ExtendedCraftingRecipeEditorTypes.tableGridSizeForTier(getExtendedCraftingTableTier(entry));
            loadShapedGrid(data.getPattern(), data.getKey(), gridSize, gridSize, EXTENDED_CRAFTING_TABLE_GRID_SIZE);
        } else {
            loadIngredientList(data.getShapelessIngredients(), ExtendedCraftingRecipeEditorTypes.tableGridSizeForTier(getExtendedCraftingTableTier(entry))
                    * ExtendedCraftingRecipeEditorTypes.tableGridSizeForTier(getExtendedCraftingTableTier(entry)));
        }
        visualResult = data.getResult() == null ? ItemStack.EMPTY : data.getResult().copy();
    }

    private void loadExtendedCraftingUltimateSingularity(RecipeEntry entry) {
        var data = entry.getExtendedCraftingUltimateSingularity();
        visualResult = data.getResult() == null ? ItemStack.EMPTY : data.getResult().copy();
    }

    private void loadExtendedCraftingEnderCrafter(RecipeEntry entry) {
        var data = entry.getExtendedCraftingEnderCrafter();
        if (isExtendedCraftingShapedEnderCrafterEntry(entry)) {
            loadShapedGrid(data.getPattern(), data.getKey(), EXTENDED_CRAFTING_SMALL_GRID_SIZE, EXTENDED_CRAFTING_SMALL_GRID_SIZE, EXTENDED_CRAFTING_SMALL_GRID_SIZE);
        } else {
            loadIngredientList(data.getShapelessIngredients(), CRAFTING_GRID_SLOT_COUNT);
        }
        visualResult = data.getResult() == null ? ItemStack.EMPTY : data.getResult().copy();
    }

    private void loadExtendedCraftingFluxCrafter(RecipeEntry entry) {
        var data = entry.getExtendedCraftingFluxCrafter();
        if (isExtendedCraftingShapedFluxCrafterEntry(entry)) {
            loadShapedGrid(data.getPattern(), data.getKey(), EXTENDED_CRAFTING_SMALL_GRID_SIZE, EXTENDED_CRAFTING_SMALL_GRID_SIZE, EXTENDED_CRAFTING_SMALL_GRID_SIZE);
        } else {
            loadIngredientList(data.getShapelessIngredients(), CRAFTING_GRID_SLOT_COUNT);
        }
        visualResult = data.getResult() == null ? ItemStack.EMPTY : data.getResult().copy();
    }

    private void loadExtendedCraftingCombination(ExtendedCraftingCombinationRecipeData data) {
        loadIngredientSlot(0, data.getInput());
        var pedestalItems = data.getPedestalItems();
        if (pedestalItems != null) {
            for (int i = 0; i < Math.min(EXTENDED_CRAFTING_COMBINATION_MAX_PEDESTALS, pedestalItems.size()); i++) {
                loadIngredientSlot(i + 1, pedestalItems.get(i));
            }
        }
        visualResult = data.getResult() == null ? ItemStack.EMPTY : data.getResult().copy();
    }

    private void loadExtendedCraftingCompressor(ExtendedCraftingCompressorRecipeData data) {
        loadIngredientSlot(0, data.getCatalyst());
        var inputs = data.getInputs();
        if (inputs != null) {
            for (int i = 0; i < Math.min(EXTENDED_CRAFTING_COMPRESSOR_MAX_INPUTS, inputs.size()); i++) {
                var input = inputs.get(i);
                if (input != null) {
                    loadIngredientSlot(i + 1, input.getIngredient());
                }
            }
        }
        visualResult = data.getResult() == null ? ItemStack.EMPTY : data.getResult().copy();
    }

    private void loadAvaritiaTable(RecipeEntry entry) {
        var data = entry.getAvaritiaTable();
        if (isAvaritiaShapedTableEntry(entry)) {
            loadShapedGrid(data.getPattern(), data.getKey(), selectedAvaritiaTableGridWidth(entry), selectedAvaritiaTableGridHeight(entry), EXTENDED_CRAFTING_TABLE_GRID_SIZE);
            visualResult = data.getResult() == null ? ItemStack.EMPTY : data.getResult().copy();
        } else if (isAvaritiaShapelessTableEntry(entry)) {
            var gridSize = AvaritiaRecipeEditorTypes.tableGridSizeForTier(getAvaritiaTableTier(entry));
            loadIngredientList(data.getShapelessIngredients(), gridSize * gridSize);
            visualResult = data.getResult() == null ? ItemStack.EMPTY : data.getResult().copy();
        }
    }

    private void loadAvaritiaCompressor(AvaritiaCompressorRecipeData data) {
        loadIngredientSlot(0, data.getIngredient());
        visualResult = data.getResult() == null ? ItemStack.EMPTY : data.getResult().copy();
    }

    private void loadAvaritiaExtremeSmithing(RecipeEntry entry) {
        var data = entry.getAvaritiaExtremeSmithing();
        loadSmithingIngredientSlot(0, data.getTemplate());
        loadSmithingIngredientSlot(1, data.getBase());
        loadSmithingIngredientSlot(2, data.addition(0));
        loadSmithingIngredientSlot(3, data.addition(1));
        loadSmithingIngredientSlot(4, data.addition(2));
        visualResult = data.getResult() == null ? ItemStack.EMPTY : data.getResult().copy();
    }

    private void loadShapedGrid(List<String> pattern, List<ShapedKeyEntry> keyEntries, int width, int height, int gridSize) {
        var key = new LinkedHashMap<Character, RecipeIngredient>();
        for (var entry : safeList(keyEntries)) {
            var symbol = entry.getSymbol();
            if (symbol == null || symbol.length() != 1) {
                continue;
            }
            if (containsUnsupportedIngredientValue(entry.getIngredient())) {
                selectedContainsUnsupportedIngredients = true;
            }
            key.put(symbol.charAt(0), entry.getIngredient());
        }
        var safePattern = safeList(pattern);
        for (int row = 0; row < Math.min(height, safePattern.size()); row++) {
            var line = safePattern.get(row);
            if (line == null) {
                continue;
            }
            for (int col = 0; col < Math.min(width, line.length()); col++) {
                var symbol = line.charAt(col);
                var index = row * gridSize + col;
                var ingredient = symbol == ' ' ? new RecipeIngredient() : key.getOrDefault(symbol, new RecipeIngredient());
                if (symbol != ' ' && isIngredientEmpty(ingredient)) {
                    selectedContainsUnsupportedIngredients = true;
                }
                visualIngredients[index] = itemFromIngredient(ingredient);
                visualIngredientData[index] = ingredient;
                visualRemainders[index] = CraftingRemainderRule.defaultRule();
            }
        }
    }

    private void loadArsNouveauApparatus(ArsNouveauApparatusRecipeData data) {
        loadIngredientSlot(0, data.getReagent());
        loadArsNouveauExtraIngredients(data.getPedestalItems());
        visualResult = data.getResult() == null ? ItemStack.EMPTY : data.getResult().copy();
    }

    private void loadArsNouveauArmorUpgrade(ArsNouveauArmorUpgradeRecipeData data) {
        visualIngredients[0] = selectedArsNouveauApparatusCenterPreview();
        loadArsNouveauExtraIngredients(data.getPedestalItems());
        visualResult = selectedArsNouveauApparatusOutputPreview();
    }

    private void loadArsNouveauEnchantment(ArsNouveauEnchantmentRecipeData data) {
        visualIngredients[0] = selectedArsNouveauApparatusCenterPreview();
        loadArsNouveauExtraIngredients(data.getPedestalItems());
        visualResult = selectedArsNouveauApparatusOutputPreview();
    }

    private void loadArsNouveauImbuement(ArsNouveauImbuementRecipeData data) {
        loadIngredientSlot(0, data.getInput());
        loadArsNouveauExtraIngredients(data.getPedestalItems(), ARS_NOUVEAU_IMBUEMENT_INPUTS);
        visualResult = data.getResult() == null ? ItemStack.EMPTY : data.getResult().copy();
    }

    private void loadArsNouveauGlyph(ArsNouveauGlyphRecipeData data) {
        var inputs = data.getInputs();
        if (inputs != null) {
            for (int i = 0; i < Math.min(ARS_NOUVEAU_MAX_INPUTS, inputs.size()); i++) {
                loadIngredientSlot(i, inputs.get(i));
            }
        }
        visualResult = data.getResult() == null ? ItemStack.EMPTY : data.getResult().copy();
    }

    private void loadArsNouveauCrush(ArsNouveauCrushRecipeData data) {
        loadIngredientSlot(0, data.getInput());
        visualArsNouveauOutputs = emptyArsNouveauOutputStacks();
        visualArsNouveauOutputChances = emptyArsNouveauOutputChances();
        visualArsNouveauOutputMaxRanges = emptyArsNouveauOutputMaxRanges();
        var outputs = data.getOutputs();
        if (outputs != null) {
            for (int i = 0; i < Math.min(ARS_NOUVEAU_MAX_CRUSH_OUTPUTS, outputs.size()); i++) {
                var output = outputs.get(i);
                if (output == null) {
                    continue;
                }
                visualArsNouveauOutputs[i] = output.getItem() == null ? ItemStack.EMPTY : output.getItem().copy();
                visualArsNouveauOutputChances[i] = Math.max(0, Math.min(1, output.getChance()));
                visualArsNouveauOutputMaxRanges[i] = Math.max(1, output.getMaxRange());
            }
        }
        visualResult = visualArsNouveauOutputs[0].copy();
    }

    private void loadArsNouveauPedestalOnly(ArsNouveauPedestalOnlyRecipeData data) {
        visualIngredients[0] = selectedArsNouveauApparatusCenterPreview();
        loadArsNouveauExtraIngredients(data.getPedestalItems());
        visualResult = selectedArsNouveauApparatusOutputPreview();
    }

    private void loadArsNouveauExtraIngredients(List<RecipeIngredient> ingredients) {
        loadArsNouveauExtraIngredients(ingredients, ARS_NOUVEAU_MAX_INPUTS);
    }

    private void loadArsNouveauExtraIngredients(List<RecipeIngredient> ingredients, int inputCount) {
        if (ingredients == null) {
            return;
        }
        for (int i = 0; i < Math.min(inputCount - 1, ingredients.size()); i++) {
            loadIngredientSlot(i + 1, ingredients.get(i));
        }
    }

    private void refreshAutoPackingVisualIngredients(RecipeIngredient ingredient, int gridSize) {
        var count = gridSize * gridSize;
        for (int i = 0; i < visualIngredients.length; i++) {
            if (i < count && !isIngredientEmpty(ingredient)) {
                visualIngredients[i] = itemFromIngredient(ingredient);
                visualIngredientData[i] = copyIngredient(ingredient);
            } else {
                visualIngredients[i] = ItemStack.EMPTY;
                visualIngredientData[i] = new RecipeIngredient();
            }
            visualRemainders[i] = CraftingRemainderRule.defaultRule();
        }
    }

    private void loadIngredientSlot(int index, RecipeIngredient ingredient) {
        if (containsUnsupportedIngredientValue(ingredient)) {
            selectedContainsUnsupportedIngredients = true;
        }
        visualIngredients[index] = itemFromIngredient(ingredient);
        visualIngredientData[index] = ingredient == null ? new RecipeIngredient() : ingredient;
        visualRemainders[index] = CraftingRemainderRule.defaultRule();
    }

    private void loadCreateIngredientSlot(int index, RecipeIngredient ingredient) {
        if (containsUnsupportedIngredientValue(ingredient)) {
            selectedContainsUnsupportedIngredients = true;
        }
        visualIngredients[index] = itemFromCreateIngredient(ingredient);
        visualIngredientData[index] = ingredient == null ? new RecipeIngredient() : ingredient;
        visualRemainders[index] = CraftingRemainderRule.defaultRule();
    }

    private boolean ingredientSlotsChanged() {
        if (loadedIngredientStacks.length != visualIngredients.length) {
            return true;
        }
        for (int i = 0; i < visualIngredients.length; i++) {
            if (!sameIngredientItem(loadedIngredientStacks[i], visualIngredients[i])) {
                return true;
            }
        }
        return false;
    }

    private boolean sameIngredientItem(ItemStack left, ItemStack right) {
        if (left.isEmpty() || right.isEmpty()) {
            return left.isEmpty() == right.isEmpty();
        }
        return ItemStack.matches(left, right);
    }

    private boolean remainderSlotsChanged() {
        if (loadedRemainders.length != visualRemainders.length) {
            return true;
        }
        for (int i = 0; i < visualRemainders.length; i++) {
            if (!sameRemainder(loadedRemainders[i], visualRemainders[i])) {
                return true;
            }
        }
        return false;
    }

    private boolean sameRemainder(CraftingRemainderRule left, CraftingRemainderRule right) {
        var leftRule = left == null ? CraftingRemainderRule.defaultRule() : left;
        var rightRule = right == null ? CraftingRemainderRule.defaultRule() : right;
        if (leftRule.getMode() != rightRule.getMode()) {
            return false;
        }
        var leftItem = leftRule.getItem() == null ? ItemStack.EMPTY : leftRule.getItem();
        var rightItem = rightRule.getItem() == null ? ItemStack.EMPTY : rightRule.getItem();
        return ItemStack.matches(leftItem, rightItem);
    }

    private boolean containsUnsupportedIngredientValue(@Nullable RecipeIngredient ingredient) {
        if (ingredient == null) {
            return false;
        }
        if (ingredient.getValues().isEmpty()) {
            return false;
        }
        if (ingredient.getValues().size() != 1) {
            return true;
        }
        var value = ingredient.getValues().get(0);
        return switch (value.getKind()) {
            case ITEM -> value.getItem() == null || value.getItem().isEmpty();
            case TAG -> value.getTag() == null || itemsFromTag(value.getTag()).length == 0;
            case ITEM_ABILITY -> value.getItemAbility() == null || value.getItemAbility().isBlank();
        };
    }

    private ItemStack itemFromIngredient(@Nullable RecipeIngredient ingredient) {
        if (ingredient == null) {
            return ItemStack.EMPTY;
        }
        for (var value : ingredient.getValues()) {
            if (value.getKind() == IngredientValueKind.ITEM && value.getItem() != null && !value.getItem().isEmpty()) {
                return value.getItem().copyWithCount(1);
            }
            if (value.getKind() == IngredientValueKind.TAG && value.getTag() != null) {
                var tagItems = itemsFromTag(value.getTag());
                if (tagItems.length > 0) {
                    return tagItems[0].copyWithCount(1);
                }
            }
            if (value.getKind() == IngredientValueKind.ITEM_ABILITY && value.getItemAbility() != null) {
                return itemFromAbility(value.getItemAbility());
            }
        }
        return ItemStack.EMPTY;
    }

    private ItemStack itemFromCreateIngredient(@Nullable RecipeIngredient ingredient) {
        if (ingredient == null) {
            return ItemStack.EMPTY;
        }
        for (var value : ingredient.getValues()) {
            if (value.getKind() == IngredientValueKind.ITEM && value.getItem() != null && !value.getItem().isEmpty()) {
                var stack = value.getItem().copy();
                stack.setCount(Math.max(1, Math.min(99, stack.getCount())));
                return stack;
            }
            if (value.getKind() == IngredientValueKind.TAG && value.getTag() != null) {
                var tagItems = itemsFromTag(value.getTag());
                if (tagItems.length > 0) {
                    return tagItems[0].copyWithCount(1);
                }
            }
            if (value.getKind() == IngredientValueKind.ITEM_ABILITY && value.getItemAbility() != null) {
                return itemFromAbility(value.getItemAbility());
            }
        }
        return ItemStack.EMPTY;
    }

    private ItemStack[] itemsFromTag(ResourceLocation tag) {
        if (tag == null) {
            return new ItemStack[0];
        }
        return BuiltInRegistries.ITEM.getTag(TagKey.create(Registries.ITEM, tag))
                .map(holders -> holders.stream()
                        .map(Holder::value)
                        .filter(item -> item != Items.AIR)
                        .map(ItemStack::new)
                        .filter(stack -> !stack.isEmpty())
                .toArray(ItemStack[]::new))
                .orElseGet(() -> new ItemStack[0]);
    }

    private ItemStack[] blockItemsFromTag(ResourceLocation tag) {
        if (tag == null) {
            return new ItemStack[0];
        }
        return BuiltInRegistries.BLOCK.getTag(TagKey.create(Registries.BLOCK, tag))
                .map(holders -> holders.stream()
                        .map(Holder::value)
                        .map(block -> block.asItem())
                        .filter(item -> item != Items.AIR)
                        .map(ItemStack::new)
                        .filter(stack -> !stack.isEmpty())
                        .toArray(ItemStack[]::new))
                .orElseGet(() -> new ItemStack[0]);
    }

    private FluidStack[] fluidsFromTag(ResourceLocation tag, int amount) {
        if (tag == null) {
            return new FluidStack[0];
        }
        var normalizedAmount = Math.max(1, amount);
        return BuiltInRegistries.FLUID.getTag(TagKey.create(Registries.FLUID, tag))
                .map(holders -> displayFluidsFromHolders(holders.stream()
                        .map(Holder::value)
                        .filter(fluid -> fluid != Fluids.EMPTY)
                        .toList(), normalizedAmount))
                .orElseGet(() -> new FluidStack[0]);
    }

    private FluidStack[] displayFluidsFromHolders(List<Fluid> fluids, int amount) {
        var sourceFluids = fluids.stream()
                .filter(fluid -> fluid.defaultFluidState().isSource())
                .toList();
        var displayFluids = sourceFluids.isEmpty() ? fluids : sourceFluids;
        return displayFluids.stream()
                .map(fluid -> new FluidStack(fluid, amount))
                .filter(stack -> !stack.isEmpty())
                .toArray(FluidStack[]::new);
    }

    @Nullable
    private ResourceLocation fluidIdFromBucket(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return new ResourceLocation("water");
        }
        var bucket = stack.getItem();
        for (var fluid : BuiltInRegistries.FLUID) {
            if (fluid != Fluids.EMPTY && fluid.getBucket() == bucket) {
                return BuiltInRegistries.FLUID.getKey(fluid);
            }
        }
        return null;
    }

    private void refreshUnsupportedIngredientStatus() {
        selectedContainsUnsupportedIngredients = false;
        for (var ingredient : visualIngredientData) {
            if (containsUnsupportedIngredientValue(ingredient)) {
                selectedContainsUnsupportedIngredients = true;
                return;
            }
        }
    }

    private RecipeIngredient ingredientForSymbol(ShapedCraftingRecipeData shaped, char symbol) {
        if (symbol == ' ') {
            return new RecipeIngredient();
        }
        return ingredientForSymbol(shaped.getKey(), symbol);
    }

    private RecipeIngredient ingredientForMechanicalSymbol(CreateMechanicalCraftingRecipeData data, char symbol) {
        if (symbol == ' ') {
            return new RecipeIngredient();
        }
        return ingredientForSymbol(data.getKey(), symbol);
    }

    private RecipeIngredient ingredientForSymbol(List<ShapedKeyEntry> key, char symbol) {
        if (symbol == ' ') {
            return new RecipeIngredient();
        }
        for (var entry : safeList(key)) {
            if (entry.getSymbol() != null && entry.getSymbol().length() == 1 && entry.getSymbol().charAt(0) == symbol) {
                return entry.getIngredient();
            }
        }
        return new RecipeIngredient();
    }

    private CraftingRemainderRule remainderForPatternSlot(ShapedCraftingRecipeData shaped, int row, int col) {
        var width = shaped.getPattern().isEmpty() ? 0 : shaped.getPattern().get(0).length();
        var remainderIndex = row * width + col;
        if (remainderIndex < 0 || remainderIndex >= shaped.getRemainders().size()) {
            return CraftingRemainderRule.defaultRule();
        }
        var remainder = shaped.getRemainders().get(remainderIndex);
        return remainder == null ? CraftingRemainderRule.defaultRule() : remainder.copy();
    }

    private RecipeIngredient ingredientForVisualSlot(int slot) {
        var ingredient = visualIngredientData[slot];
        if (!isIngredientEmpty(ingredient)) {
            return ingredient;
        }
        var stack = visualIngredients[slot];
        return stack.isEmpty() ? new RecipeIngredient() : RecipeIngredient.item(stack);
    }

    private RecipeIngredient itemOnlyIngredientForVisualSlot(int slot) {
        var stack = visualIngredients[slot];
        return stack.isEmpty() ? new RecipeIngredient() : RecipeIngredient.item(stack);
    }

    private RecipeIngredient firstCreateIngredient(CreateProcessingRecipeData data) {
        var ingredients = data.getIngredients();
        if (ingredients == null) {
            return new RecipeIngredient();
        }
        for (var ingredient : ingredients) {
            if (!isIngredientEmpty(ingredient)) {
                return copyIngredient(ingredient);
            }
        }
        return new RecipeIngredient();
    }

    private int createSequencedIngredientSlotIndex(int stepIndex) {
        return CREATE_SEQUENCED_STEP_INGREDIENT_OFFSET + stepIndex;
    }

    private int createSequencedStepIndexFromIngredientSlot(int slotIndex) {
        return slotIndex - CREATE_SEQUENCED_STEP_INGREDIENT_OFFSET;
    }

    private int createSequencedStepCount(RecipeEntry entry) {
        if (entry == null || !isCreateSequencedAssemblyEntry(entry)) {
            return 0;
        }
        var sequence = entry.getCreateSequencedAssembly().getSequence();
        return sequence == null ? 0 : Math.min(CREATE_SEQUENCED_MAX_STEPS, sequence.size());
    }

    private CreateSequencedAssemblyStepData getCreateSequencedStep(RecipeEntry entry, int index) {
        var data = entry.getCreateSequencedAssembly();
        var sequence = data.getSequence();
        if (sequence == null) {
            sequence = new ArrayList<>();
            data.setSequence(sequence);
        }
        while (sequence.size() <= Math.max(0, index)) {
            sequence.add(new CreateSequencedAssemblyStepData());
        }
        var step = sequence.get(Math.max(0, index));
        if (step == null) {
            step = new CreateSequencedAssemblyStepData();
            sequence.set(Math.max(0, index), step);
        }
        return step;
    }

    private ExtendedCraftingCountedIngredientData getExtendedCraftingCompressorInputData(RecipeEntry entry, int index) {
        var data = entry.getExtendedCraftingCompressor();
        var inputs = data.getInputs();
        if (inputs == null) {
            inputs = new ArrayList<>();
            data.setInputs(inputs);
        }
        var normalizedIndex = Math.max(0, Math.min(EXTENDED_CRAFTING_COMPRESSOR_MAX_INPUTS - 1, index));
        while (inputs.size() <= normalizedIndex) {
            inputs.add(new ExtendedCraftingCountedIngredientData());
        }
        var input = inputs.get(normalizedIndex);
        if (input == null) {
            input = new ExtendedCraftingCountedIngredientData();
            inputs.set(normalizedIndex, input);
        }
        return input;
    }

    private int autoPackingGridSize(CreateProcessingRecipeData data) {
        var count = 0;
        var ingredients = data.getIngredients();
        if (ingredients != null) {
            for (var ingredient : ingredients) {
                if (!isIngredientEmpty(ingredient)) {
                    count++;
                }
            }
        }
        return count <= 4 ? 2 : 3;
    }

    private void writeRepeatedAutoPackingIngredient(RecipeEntry entry, RecipeIngredient ingredient, int gridSize) {
        var data = entry.getCreateProcessing();
        refreshAutoPackingVisualIngredients(ingredient, gridSize);
        var ingredients = new ArrayList<RecipeIngredient>();
        if (!isIngredientEmpty(ingredient)) {
            for (int i = 0; i < gridSize * gridSize; i++) {
                ingredients.add(copyIngredient(ingredient));
            }
        }
        data.setIngredients(ingredients);
        writeCreateAutoPackingRecipe(data, gridSize);
    }

    private RecipeIngredient copyIngredient(RecipeIngredient original) {
        var copy = new RecipeIngredient();
        if (original == null) {
            return copy;
        }
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

    private String ingredientKey(RecipeIngredient ingredient, ItemStack stack, int slot) {
        if (ingredient != null && ingredient.getValues().size() == 1) {
            var value = ingredient.getValues().get(0);
            return switch (value.getKind()) {
                case ITEM -> value.getItem() == null || value.getItem().isEmpty()
                        ? "empty:" + slot
                        : "item:" + value.getItem().hashCode();
                case TAG -> "tag:" + value.getTag();
                case ITEM_ABILITY -> "item_ability:" + value.getItemAbility();
            };
        }
        if (!stack.isEmpty()) {
            return "item:" + stack.hashCode();
        }
        return "ingredient:" + slot;
    }

    private ItemStack normalizeStack(ItemStack stack) {
        if (stack == null || stack.isEmpty() || stack.is(Items.AIR)) {
            return ItemStack.EMPTY;
        }
        return stack.copyWithCount(1);
    }

    private ItemStack normalizeVisualIngredientStack(int index, ItemStack stack) {
        if (stack == null || stack.isEmpty() || stack.is(Items.AIR)) {
            return ItemStack.EMPTY;
        }
        var copy = stack.copy();
        var kind = selectedCreateKind().orElse(null);
        if (selectedEntry != null
                && isCreateProcessingEntry(selectedEntry)
                && supportsCreateCountedItemInputs(kind)
                && index >= 0
                && index < createVisibleItemInputCapacity(kind)) {
            copy.setCount(Math.max(1, Math.min(createItemInputMaxWeight(index, createVisibleItemInputCapacity(kind)), copy.getCount())));
            return copy;
        }
        return copy.copyWithCount(1);
    }

    private RecipeIngredient ingredientForVisualItemStack(int index, ItemStack stack) {
        var kind = selectedCreateKind().orElse(null);
        if (selectedEntry != null
                && isCreateProcessingEntry(selectedEntry)
                && supportsCreateCountedItemInputs(kind)
                && index >= 0
                && index < createVisibleItemInputCapacity(kind)) {
            return CreateItemInputCounts.item(stack, createItemInputMaxWeight(index, createVisibleItemInputCapacity(kind)));
        }
        return RecipeIngredient.item(stack);
    }

    private String normalizeDragonType(String dragonType) {
        return DRAGON_FORGE_DRAGON_TYPES.contains(dragonType) ? dragonType : "fire";
    }

    private ItemStack itemFromAbility(String itemAbility) {
        return switch (itemAbility) {
            case "axe_dig", "axe_strip" -> new ItemStack(Items.IRON_AXE);
            case "shovel_dig" -> new ItemStack(Items.IRON_SHOVEL);
            case "pickaxe_dig" -> new ItemStack(Items.IRON_PICKAXE);
            case "sword_dig" -> new ItemStack(Items.IRON_SWORD);
            case "shears_dig" -> new ItemStack(Items.SHEARS);
            default -> new ItemStack(itemFromRegistry("farmersdelight:iron_knife", Items.IRON_SWORD));
        };
    }

    private static net.minecraft.world.item.Item itemFromRegistry(String id, net.minecraft.world.item.Item fallback) {
        var location = ResourceLocation.tryParse(id);
        if (location == null) {
            return fallback;
        }
        var item = BuiltInRegistries.ITEM.get(location);
        return item == Items.AIR ? fallback : item;
    }

    private static <T> List<T> safeList(List<T> list) {
        return list == null ? List.of() : list;
    }

    private static ItemStack[] emptyIngredientStacks() {
        var stacks = new ItemStack[MAX_INGREDIENT_SLOTS];
        Arrays.fill(stacks, ItemStack.EMPTY);
        return stacks;
    }

    private static CraftingRemainderRule[] emptyRemainderData() {
        var remainders = new CraftingRemainderRule[MAX_INGREDIENT_SLOTS];
        Arrays.setAll(remainders, ignored -> CraftingRemainderRule.defaultRule());
        return remainders;
    }

    private static ItemStack[] emptyCuttingResultStacks() {
        var stacks = new ItemStack[4];
        Arrays.fill(stacks, ItemStack.EMPTY);
        return stacks;
    }

    private static float[] emptyCuttingResultChances() {
        var chances = new float[4];
        Arrays.fill(chances, 1.0F);
        return chances;
    }

    private static ItemStack[] emptyCreateOutputStacks() {
        var stacks = new ItemStack[CREATE_MAX_ITEM_OUTPUTS];
        Arrays.fill(stacks, ItemStack.EMPTY);
        return stacks;
    }

    private static float[] emptyCreateOutputChances() {
        var chances = new float[CREATE_MAX_ITEM_OUTPUTS];
        Arrays.fill(chances, 1.0F);
        return chances;
    }

    private static CreateFluidIngredientData[] emptyCreateFluidInputs() {
        var inputs = new CreateFluidIngredientData[CREATE_MAX_FLUID_INPUTS];
        Arrays.setAll(inputs, ignored -> CreateFluidIngredientData.empty());
        return inputs;
    }

    private static FluidStack[] emptyCreateFluidOutputs() {
        var outputs = new FluidStack[CREATE_MAX_FLUID_OUTPUTS];
        Arrays.fill(outputs, FluidStack.EMPTY);
        return outputs;
    }

    private static ItemStack[] emptyArsNouveauOutputStacks() {
        var stacks = new ItemStack[ARS_NOUVEAU_MAX_CRUSH_OUTPUTS];
        Arrays.fill(stacks, ItemStack.EMPTY);
        return stacks;
    }

    private static float[] emptyArsNouveauOutputChances() {
        var chances = new float[ARS_NOUVEAU_MAX_CRUSH_OUTPUTS];
        Arrays.fill(chances, 1.0F);
        return chances;
    }

    private static int[] emptyArsNouveauOutputMaxRanges() {
        var maxRanges = new int[ARS_NOUVEAU_MAX_CRUSH_OUTPUTS];
        Arrays.fill(maxRanges, 1);
        return maxRanges;
    }

    private static RecipeIngredient[] emptyIngredientData() {
        var ingredients = new RecipeIngredient[MAX_INGREDIENT_SLOTS];
        Arrays.setAll(ingredients, ignored -> new RecipeIngredient());
        return ingredients;
    }

    private record VisualPattern(ArrayList<String> pattern, ArrayList<ShapedKeyEntry> key) {
    }

    private static ItemStack[] copyStacks(ItemStack[] stacks) {
        var copy = new ItemStack[stacks.length];
        for (int i = 0; i < stacks.length; i++) {
            copy[i] = stacks[i].copy();
        }
        return copy;
    }

    private static CraftingRemainderRule[] copyRemainders(CraftingRemainderRule[] remainders) {
        var copy = new CraftingRemainderRule[remainders.length];
        for (int i = 0; i < remainders.length; i++) {
            copy[i] = remainders[i] == null ? CraftingRemainderRule.defaultRule() : remainders[i].copy();
        }
        return copy;
    }

    private static FluidStack copyFluid(FluidStack stack) {
        return stack == null ? FluidStack.EMPTY : stack.copy();
    }

}
