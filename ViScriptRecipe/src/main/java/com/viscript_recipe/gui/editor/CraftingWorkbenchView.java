package com.viscript_recipe.gui.editor;

import com.lowdragmc.lowdraglib2.editor.ui.View;
import com.lowdragmc.lowdraglib2.gui.ColorPattern;
import com.lowdragmc.lowdraglib2.gui.texture.FluidStackTexture;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.Icons;
import com.lowdragmc.lowdraglib2.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.TextWrap;
import com.lowdragmc.lowdraglib2.gui.ui.elements.FluidSlot;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Scene;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.gui.ui.styletemplate.Sprites;
import com.lowdragmc.lowdraglib2.utils.data.BlockInfo;
import com.lowdragmc.lowdraglib2.utils.virtuallevel.TrackedDummyWorld;
import com.viscript_recipe.data.RecipeEditorTypes;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.data.create.CreateProcessingKind;
import com.viscript_recipe.data.create.CreateSequencedAssemblyStepKind;
import com.viscript_recipe.data.touhou_little_maid.TouhouLittleMaidAltarRecipeData;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CraftingWorkbenchView extends View {
    private static final int SLOT_SIZE = 24;
    private static final int JEI_SLOT_SIZE = 18;
    private static final int MECHANICAL_CRAFTING_SLOT_SIZE = 18;
    private static final int MECHANICAL_CRAFTING_GRID_SIZE = 9;
    private static final int OUTPUT_SLOT_SIZE = 30;
    private static final int BLOCK_CUTTING_OUTPUT_COLUMNS = 5;
    private static final int BLOCK_CUTTING_OUTPUT_ROWS = 3;
    private static final int BLOCK_CUTTING_OUTPUT_SLOT_COUNT = BLOCK_CUTTING_OUTPUT_COLUMNS * BLOCK_CUTTING_OUTPUT_ROWS;
    private static final int CREATE_SEQUENCED_STEP_INGREDIENT_OFFSET = 10;
    private static final int ARS_NOUVEAU_IMBUEMENT_PEDESTAL_SLOTS = 3;
    private static final int KALEIDOSCOPE_CARRIER_SLOT = 9;
    private static final int DRAGGED_ITEM_PREVIEW_SIZE = 18;
    private static final ResourceLocation IRON_SCROLL_ID = new ResourceLocation("irons_spellbooks", "scroll");

    private final RecipeEditorController controller;
    private final Map<IngredientDisplaySlot, Integer> ingredientDragSlotIndices = new IdentityHashMap<>();
    private final IngredientDisplaySlot[] craftingIngredientSlots = new IngredientDisplaySlot[9];
    private final ItemSlot craftingOutputSlot = createEditorSlot(OUTPUT_SLOT_SIZE);
    private final boolean useJeiMechanicalCraftingCanvas = CreateMechanicalCraftingCanvasFactory.hasJeiSkin();
    private final IngredientDisplaySlot[] mechanicalCraftingIngredientSlots = new IngredientDisplaySlot[81];
    private final UIElement[] mechanicalCraftingIngredientSlotCells = new UIElement[81];
    private final UIElement[] mechanicalCraftingIngredientRows = new UIElement[9];
    private final ItemSlot mechanicalCraftingOutputSlot = createEditorSlot(OUTPUT_SLOT_SIZE);
    private final ItemSlot mechanicalCraftingJeiOutputSlot = createEditorSlot(JEI_SLOT_SIZE);
    private final Label mechanicalCraftingIngredientCountLabel = RecipeEditorUi.label(Component.empty());
    private final UIElement mechanicalCraftingWorkstationIcon = createItemIcon(ItemStack.EMPTY, 96);
    private UIElement mechanicalCraftingGrid;
    private UIElement mechanicalCraftingFallbackProcess;
    private UIElement mechanicalCraftingJeiProcess;
    private boolean mechanicalCraftingUsesJeiGrid;
    private final boolean useJeiCookingCanvas = VanillaCookingCanvasFactory.hasJeiSkin();
    private final IngredientDisplaySlot cookingIngredientSlot = createIngredientSlot(useJeiCookingCanvas ? JEI_SLOT_SIZE : SLOT_SIZE);
    private final ItemSlot cookingOutputSlot = createEditorSlot(useJeiCookingCanvas ? JEI_SLOT_SIZE : OUTPUT_SLOT_SIZE);
    private final Label cookingExperienceLabel = RecipeEditorUi.label(Component.empty());
    private final Label cookingTimeLabel = RecipeEditorUi.label(Component.empty());
    private final IngredientDisplaySlot campfireCookingIngredientSlot = createIngredientSlot(useJeiCookingCanvas ? JEI_SLOT_SIZE : SLOT_SIZE);
    private final ItemSlot campfireCookingOutputSlot = createEditorSlot(useJeiCookingCanvas ? JEI_SLOT_SIZE : OUTPUT_SLOT_SIZE);
    private final Label campfireCookingTimeLabel = RecipeEditorUi.label(Component.empty());
    private final boolean useJeiStonecuttingCanvas = VanillaStonecuttingCanvasFactory.hasJeiSkin();
    private final IngredientDisplaySlot singleInputIngredientSlot = createIngredientSlot(useJeiStonecuttingCanvas ? JEI_SLOT_SIZE : SLOT_SIZE);
    private final ItemSlot singleInputOutputSlot = createEditorSlot(useJeiStonecuttingCanvas ? JEI_SLOT_SIZE : OUTPUT_SLOT_SIZE);
    private final boolean useJeiFarmersCookingPotCanvas = FarmersDelightCanvasFactory.hasJeiCookingPotSkin();
    private final IngredientDisplaySlot[] farmerCookingIngredientSlots = new IngredientDisplaySlot[6];
    private final ItemSlot farmerCookingPotPreviewSlot = createEditorSlot(useJeiFarmersCookingPotCanvas ? JEI_SLOT_SIZE : OUTPUT_SLOT_SIZE);
    private final ItemSlot farmerCookingOutputSlot = createEditorSlot(useJeiFarmersCookingPotCanvas ? JEI_SLOT_SIZE : OUTPUT_SLOT_SIZE);
    private final ItemSlot farmerCookingContainerSlot = createEditorSlot(useJeiFarmersCookingPotCanvas ? JEI_SLOT_SIZE : SLOT_SIZE);
    private final UIElement farmerCookingTimeIcon = new UIElement();
    private final UIElement farmerCookingExperienceIcon = new UIElement();
    private final boolean useJeiFarmersCuttingBoardCanvas = FarmersDelightCanvasFactory.hasJeiCuttingBoardSkin();
    private final IngredientDisplaySlot[] farmerCuttingIngredientSlots = new IngredientDisplaySlot[2];
    private final ItemSlot[] farmerCuttingResultSlots = new ItemSlot[4];
    private final UIElement[] farmerCuttingResultCells = new UIElement[4];
    private final IngredientDisplaySlot[] smithingIngredientSlots = new IngredientDisplaySlot[3];
    private final UIElement[] smithingInputColumns = new UIElement[3];
    private final Label[] smithingInputLabels = new Label[3];
    private final boolean useJeiSmithingCanvas = VanillaSmithingCanvasFactory.hasJeiSkin();
    private final ItemSlot smithingOutputSlot = createEditorSlot(useJeiSmithingCanvas ? JEI_SLOT_SIZE : OUTPUT_SLOT_SIZE);
    private final boolean useJeiArcaneAnvilCanvas = ArcaneAnvilCanvasFactory.hasJeiSkin();
    private final IngredientDisplaySlot[] arcaneAnvilIngredientSlots = new IngredientDisplaySlot[2];
    private ItemSlot arcaneAnvilOutputSlot;
    private final IngredientDisplaySlot avaritiaCompressorIngredientSlot = createIngredientSlot(JEI_SLOT_SIZE);
    private final ItemSlot avaritiaCompressorOutputSlot = createEditorSlot(JEI_SLOT_SIZE);
    private final IngredientDisplaySlot[] avaritiaSmithingIngredientSlots = new IngredientDisplaySlot[5];
    private final ItemSlot avaritiaSmithingOutputSlot = createEditorSlot(JEI_SLOT_SIZE);
    private final IngredientDisplaySlot[] extendedCombinationIngredientSlots = new IngredientDisplaySlot[9];
    private final ItemSlot extendedCombinationOutputSlot = createEditorSlot(JEI_SLOT_SIZE);
    private final IngredientDisplaySlot extendedCompressorCatalystSlot = createIngredientSlot(JEI_SLOT_SIZE);
    private final IngredientDisplaySlot extendedCompressorInputSlot = createIngredientSlot(JEI_SLOT_SIZE);
    private final ItemSlot extendedCompressorOutputSlot = createEditorSlot(JEI_SLOT_SIZE);
    private final IngredientDisplaySlot[] extendedFluxIngredientSlots = new IngredientDisplaySlot[9];
    private final ItemSlot extendedFluxOutputSlot = createEditorSlot(OUTPUT_SLOT_SIZE);
    private final boolean useJeiAlchemistCanvas = AlchemistCauldronCanvasFactory.hasJeiSkin();
    private final IngredientDisplaySlot alchemistIngredientSlot = createIngredientSlot(useJeiAlchemistCanvas ? JEI_SLOT_SIZE : SLOT_SIZE);
    private final Label alchemistMiddleFluidLabel = RecipeEditorUi.label(Component.empty());
    private final Label alchemistOutputFluidLabel = RecipeEditorUi.label(Component.empty());
    private final Label alchemistOutputLabel = RecipeEditorUi.label(Component.empty());
    private final Label alchemistChanceLabel = RecipeEditorUi.label(Component.empty());
    private final Label alchemistInputPlusLabel = createOperatorPlusLabel();
    private final UIElement alchemistInputArrow = createArrowElement();
    private final UIElement alchemistOutputArrow = createArrowElement();
    private final Label alchemistOutputPlusLabel = createOperatorPlusLabel();
    private final FluidSlot alchemistMiddleFluidSlot = createFluidSlot(useJeiAlchemistCanvas ? 16 : 30);
    private final FluidSlot alchemistResultFluidSlot = createFluidSlot(useJeiAlchemistCanvas ? 16 : 30);
    private final ItemSlot alchemistOutputSlot = createEditorSlot(useJeiAlchemistCanvas ? 16 : OUTPUT_SLOT_SIZE);
    private final boolean useJeiDragonForgeCanvas = DragonForgeCanvasFactory.hasJeiSkin();
    private final IngredientDisplaySlot[] dragonForgeIngredientSlots = new IngredientDisplaySlot[2];
    private final UIElement[] dragonForgeJeiTypeLayers = new UIElement[3];
    private final Label dragonForgeBreathValueLabel = RecipeEditorUi.label(Component.empty());
    private final ItemSlot dragonForgeOutputSlot = createEditorSlot(useJeiDragonForgeCanvas ? JEI_SLOT_SIZE : OUTPUT_SLOT_SIZE);
    private final IngredientDisplaySlot cataclysmWeaponFusionBaseSlot = createIngredientSlot(JEI_SLOT_SIZE);
    private final IngredientDisplaySlot cataclysmWeaponFusionAdditionSlot = createIngredientSlot(JEI_SLOT_SIZE);
    private final ItemSlot cataclysmWeaponFusionResultSlot = createEditorSlot(JEI_SLOT_SIZE);
    private final IngredientDisplaySlot cataclysmAmethystBlessIngredientSlot = createIngredientSlot(JEI_SLOT_SIZE);
    private final ItemSlot cataclysmAmethystBlessResultSlot = createEditorSlot(JEI_SLOT_SIZE);
    private final IngredientDisplaySlot[] touhouLittleMaidAltarIngredientSlots = new IngredientDisplaySlot[TouhouLittleMaidAltarRecipeData.INPUT_COUNT];
    private final ItemSlot touhouLittleMaidAltarResultSlot = createEditorSlot(JEI_SLOT_SIZE);
    private final Label touhouLittleMaidAltarPowerLabel = RecipeEditorUi.label(Component.empty());
    private final Label touhouLittleMaidAltarResultDescriptionLabel = RecipeEditorUi.label(Component.empty());
    private final IngredientDisplaySlot[] sporeSurgeryIngredientSlots = new IngredientDisplaySlot[16];
    private final ItemSlot sporeSurgeryOutputSlot = createEditorSlot(JEI_SLOT_SIZE);
    private final IngredientDisplaySlot[] sporeGraftingIngredientSlots = new IngredientDisplaySlot[3];
    private final ItemSlot sporeGraftingOutputSlot = createEditorSlot(JEI_SLOT_SIZE);
    private final boolean useJeiGoetyCanvas = GoetyCanvasFactory.hasJeiSkin();
    private final IngredientDisplaySlot goetyCursedInfuserIngredientSlot = createIngredientSlot(useJeiGoetyCanvas ? JEI_SLOT_SIZE : SLOT_SIZE);
    private final ItemSlot goetyCursedInfuserOutputSlot = createEditorSlot(useJeiGoetyCanvas ? JEI_SLOT_SIZE : OUTPUT_SLOT_SIZE);
    private final UIElement goetyCursedInfuserMachineIcon = new UIElement();
    private final Label goetyCursedInfuserTimeLabel = RecipeEditorUi.label(Component.empty());
    private final IngredientDisplaySlot goetyRitualActivationSlot = createIngredientSlot(useJeiGoetyCanvas ? JEI_SLOT_SIZE : SLOT_SIZE);
    private final IngredientDisplaySlot[] goetyRitualIngredientSlots = new IngredientDisplaySlot[12];
    private final ItemSlot goetyRitualOutputSlot = createEditorSlot(useJeiGoetyCanvas ? JEI_SLOT_SIZE : OUTPUT_SLOT_SIZE);
    private final UIElement goetyRitualTypeIcon = new UIElement();
    private final UIElement goetyRitualResearchIcon = new UIElement();
    private final Label goetyRitualInfoLabel = RecipeEditorUi.label(Component.empty());
    private final IngredientDisplaySlot[] goetyBrazierIngredientSlots = new IngredientDisplaySlot[3];
    private final ItemSlot goetyBrazierOutputSlot = createEditorSlot(useJeiGoetyCanvas ? JEI_SLOT_SIZE : OUTPUT_SLOT_SIZE);
    private final Label goetyBrazierSoulLabel = RecipeEditorUi.label(Component.empty());
    private final IngredientDisplaySlot goetyPulverizeIngredientSlot = createIngredientSlot(useJeiGoetyCanvas ? JEI_SLOT_SIZE : SLOT_SIZE);
    private final ItemSlot goetyPulverizeOutputSlot = createEditorSlot(useJeiGoetyCanvas ? JEI_SLOT_SIZE : OUTPUT_SLOT_SIZE);
    private final IngredientDisplaySlot goetyBrewingCatalystSlot = createIngredientSlot(useJeiGoetyCanvas ? JEI_SLOT_SIZE : SLOT_SIZE);
    private final UIElement goetyBrewingOutputIcon = GoetyCanvasFactory.readOnlyItem(
            "goety:brew", "viscript_recipe.editor.goety.brewing.derived_output", useJeiGoetyCanvas ? JEI_SLOT_SIZE : SLOT_SIZE);
    private final Label goetyBrewingInfoLabel = RecipeEditorUi.label(Component.empty());
    private final IngredientDisplaySlot[] createIngredientSlots = new IngredientDisplaySlot[9];
    private final FluidDisplaySlot[] createFluidInputSlots = new FluidDisplaySlot[2];
    private final ItemSlot[] createOutputSlots = new ItemSlot[12];
    private final FluidSlot[] createFluidOutputSlots = new FluidSlot[2];
    private final UIElement[] createIngredientSlotCells = new UIElement[9];
    private final UIElement[] createFluidInputColumns = new UIElement[2];
    private final UIElement[] createOutputSlotCells = new UIElement[12];
    private final UIElement[] createFluidOutputColumns = new UIElement[2];
    private final Label createMachineLabel = RecipeEditorUi.label(Component.empty());
    private final UIElement createMachineIcon = new UIElement();
    private final IngredientDisplaySlot createSpoutIngredientSlot = createIngredientSlot(SLOT_SIZE);
    private final FluidDisplaySlot createSpoutFluidInputSlot = createFluidDisplaySlot();
    private final ItemSlot createSpoutOutputSlot = createEditorSlot(OUTPUT_SLOT_SIZE);
    private final IngredientDisplaySlot createDrainIngredientSlot = createIngredientSlot(SLOT_SIZE);
    private final FluidSlot createDrainFluidOutputSlot = createFluidSlot();
    private final ItemSlot createDrainOutputSlot = createEditorSlot(OUTPUT_SLOT_SIZE);
    private final IngredientDisplaySlot createFanIngredientSlot = createIngredientSlot(SLOT_SIZE);
    private final UIElement createFanCatalystIcon = new UIElement();
    private final Label createFanCatalystLabel = RecipeEditorUi.label(Component.empty());
    private final ItemSlot createFanSingleOutputSlot = createEditorSlot(OUTPUT_SLOT_SIZE);
    private final ItemSlot[] createFanOutputSlots = new ItemSlot[12];
    private final UIElement[] createFanOutputSlotCells = new UIElement[12];
    private final IngredientDisplaySlot createCrushingIngredientSlot = createIngredientSlot(SLOT_SIZE);
    private final ItemSlot[] createCrushingOutputSlots = new ItemSlot[7];
    private final UIElement[] createCrushingOutputSlotCells = new UIElement[7];
    private final IngredientDisplaySlot createMillingIngredientSlot = createIngredientSlot(SLOT_SIZE);
    private final ItemSlot[] createMillingOutputSlots = new ItemSlot[4];
    private final UIElement[] createMillingOutputSlotCells = new UIElement[4];
    private final IngredientDisplaySlot createSawIngredientSlot = createIngredientSlot(SLOT_SIZE);
    private final ItemSlot[] createSawOutputSlots = new ItemSlot[4];
    private final UIElement[] createSawOutputSlotCells = new UIElement[4];
    private final UIElement[] createSawOutputRows = new UIElement[2];
    private final ItemSlot[] createBlockCuttingOutputSlots = new ItemSlot[BLOCK_CUTTING_OUTPUT_SLOT_COUNT];
    private final UIElement[] createBlockCuttingOutputSlotCells = new UIElement[BLOCK_CUTTING_OUTPUT_SLOT_COUNT];
    private final UIElement[] createBlockCuttingOutputRows = new UIElement[BLOCK_CUTTING_OUTPUT_ROWS];
    private final IngredientDisplaySlot sandpaperIngredientSlot = createIngredientSlot(SLOT_SIZE);
    private final ItemSlot sandpaperOutputSlot = createEditorSlot(OUTPUT_SLOT_SIZE);
    private final IngredientDisplaySlot[] createAutoPackingIngredientSlots = new IngredientDisplaySlot[9];
    private final UIElement[] createAutoPackingIngredientSlotCells = new UIElement[9];
    private final UIElement[] createAutoPackingIngredientRows = new UIElement[3];
    private final ItemSlot createAutoPackingOutputSlot = createEditorSlot(OUTPUT_SLOT_SIZE);
    private UIElement createAutoPackingInputGrid;
    private final IngredientDisplaySlot[] createPressIngredientSlots = new IngredientDisplaySlot[9];
    private final UIElement[] createPressIngredientSlotCells = new UIElement[9];
    private final UIElement[] createPressIngredientRows = new UIElement[3];
    private final FluidDisplaySlot[] createPressFluidInputSlots = new FluidDisplaySlot[2];
    private final UIElement[] createPressFluidInputColumns = new UIElement[2];
    private final ItemSlot[] createPressOutputSlots = new ItemSlot[4];
    private final UIElement[] createPressOutputSlotCells = new UIElement[4];
    private final UIElement[] createPressOutputRows = new UIElement[2];
    private final FluidSlot[] createPressFluidOutputSlots = new FluidSlot[2];
    private final UIElement[] createPressFluidOutputColumns = new UIElement[2];
    private final Label createPressHeatLabel = RecipeEditorUi.label(Component.empty());
    private final UIElement createBasinMachineIcon = new UIElement();
    private final IngredientDisplaySlot createAutomaticBrewingIngredientSlot = createIngredientSlot(SLOT_SIZE);
    private final FluidDisplaySlot createAutomaticBrewingFluidInputSlot = createFluidDisplaySlot();
    private final FluidSlot createAutomaticBrewingFluidOutputSlot = createFluidSlot();
    private final Label createAutomaticBrewingHeatLabel = RecipeEditorUi.label(Component.empty());
    private final IngredientDisplaySlot createPressingIngredientSlot = createIngredientSlot(SLOT_SIZE);
    private final ItemSlot[] createPressingOutputSlots = new ItemSlot[2];
    private final UIElement[] createPressingOutputSlotCells = new UIElement[2];
    private final IngredientDisplaySlot createDeployerProcessedSlot = createIngredientSlot(SLOT_SIZE);
    private final IngredientDisplaySlot createDeployerHeldSlot = createIngredientSlot(SLOT_SIZE);
    private final ItemSlot[] createDeployerOutputSlots = new ItemSlot[4];
    private final UIElement[] createDeployerOutputSlotCells = new UIElement[4];
    private final UIElement[] createDeployerOutputRows = new UIElement[2];
    private final IngredientDisplaySlot createManualApplicationBlockSlot = createIngredientSlot(SLOT_SIZE);
    private final IngredientDisplaySlot createManualApplicationHeldSlot = createIngredientSlot(SLOT_SIZE);
    private final ItemSlot[] createManualApplicationOutputSlots = new ItemSlot[4];
    private final UIElement[] createManualApplicationOutputSlotCells = new UIElement[4];
    private final UIElement[] createManualApplicationOutputRows = new UIElement[2];
    private final IngredientDisplaySlot createSequencedInputSlot = createIngredientSlot(SLOT_SIZE);
    private final ItemSlot createSequencedTransitionalSlot = createEditorSlot(SLOT_SIZE);
    private final IngredientDisplaySlot[] createSequencedStepIngredientSlots = new IngredientDisplaySlot[8];
    private final UIElement[] createSequencedStepIngredientCells = new UIElement[8];
    private final FluidDisplaySlot[] createSequencedStepFluidSlots = new FluidDisplaySlot[8];
    private final UIElement[] createSequencedStepFluidCells = new UIElement[8];
    private final UIElement[] createSequencedStepCards = new UIElement[8];
    private final UIElement[] createSequencedStepIcons = new UIElement[8];
    private final Label[] createSequencedStepLabels = new Label[8];
    private final ItemSlot[] createSequencedOutputSlots = new ItemSlot[9];
    private final UIElement[] createSequencedOutputSlotCells = new UIElement[9];
    private UIElement createSequencedSecondaryOutputColumn;
    private final Label createSequencedLoopsLabel = RecipeEditorUi.label(Component.empty());
    private final IngredientDisplaySlot[] arsNouveauIngredientSlots = new IngredientDisplaySlot[9];
    private final UIElement[] arsNouveauIngredientSlotCells = new UIElement[9];
    private final ItemSlot arsNouveauResultSlot = createEditorSlot(OUTPUT_SLOT_SIZE);
    private final UIElement arsNouveauApparatusCenterPreviewIcon = new UIElement();
    private final UIElement arsNouveauApparatusOutputPreviewIcon = new UIElement();
    private final Label arsNouveauApparatusSourceLabel = RecipeEditorUi.label(Component.empty());
    private final Label arsNouveauApparatusTierLabel = RecipeEditorUi.label(Component.empty());
    private final IngredientDisplaySlot arsNouveauImbuementInputSlot = createIngredientSlot(SLOT_SIZE);
    private final IngredientDisplaySlot[] arsNouveauImbuementPedestalSlots = new IngredientDisplaySlot[ARS_NOUVEAU_IMBUEMENT_PEDESTAL_SLOTS];
    private final UIElement[] arsNouveauImbuementPedestalSlotCells = new UIElement[ARS_NOUVEAU_IMBUEMENT_PEDESTAL_SLOTS];
    private final UIElement arsNouveauImbuementDefaultCenterIcon = new UIElement();
    private final ItemSlot arsNouveauImbuementResultSlot = createEditorSlot(OUTPUT_SLOT_SIZE);
    private final Label arsNouveauImbuementSourceLabel = RecipeEditorUi.label(Component.empty());
    private final IngredientDisplaySlot[] arsNouveauGlyphIngredientSlots = new IngredientDisplaySlot[9];
    private final UIElement[] arsNouveauGlyphIngredientSlotCells = new UIElement[9];
    private final UIElement arsNouveauGlyphWorkstationIcon = new UIElement();
    private final ItemSlot arsNouveauGlyphResultSlot = createEditorSlot(OUTPUT_SLOT_SIZE);
    private final Label arsNouveauGlyphExpLabel = RecipeEditorUi.label(Component.empty());
    private final IngredientDisplaySlot arsNouveauCrushInputSlot = createIngredientSlot(SLOT_SIZE);
    private final ItemSlot[] arsNouveauCrushOutputSlots = new ItemSlot[6];
    private final UIElement[] arsNouveauCrushOutputSlotCells = new UIElement[6];
    private final IngredientDisplaySlot[] kaleidoscopePotIngredientSlots = new IngredientDisplaySlot[9];
    private final IngredientDisplaySlot kaleidoscopePotCarrierSlot = createIngredientSlot(JEI_SLOT_SIZE);
    private final ItemSlot kaleidoscopePotResultSlot = createEditorSlot(JEI_SLOT_SIZE);
    private final Label kaleidoscopePotStirFryLabel = RecipeEditorUi.label(Component.empty());
    private final IngredientDisplaySlot[] kaleidoscopeStockpotIngredientSlots = new IngredientDisplaySlot[9];
    private final IngredientDisplaySlot kaleidoscopeStockpotCarrierSlot = createIngredientSlot(JEI_SLOT_SIZE);
    private final ItemSlot kaleidoscopeStockpotResultSlot = createEditorSlot(JEI_SLOT_SIZE);
    private final IngredientDisplaySlot kaleidoscopeMillstoneInputSlot = createIngredientSlot(JEI_SLOT_SIZE);
    private final ItemSlot kaleidoscopeMillstoneResultSlot = createEditorSlot(JEI_SLOT_SIZE);
    private final IngredientDisplaySlot kaleidoscopeChoppingBoardInputSlot = createIngredientSlot(JEI_SLOT_SIZE);
    private final ItemSlot kaleidoscopeChoppingBoardResultSlot = createEditorSlot(JEI_SLOT_SIZE);
    private final IngredientDisplaySlot kaleidoscopeSteamerInputSlot = createIngredientSlot(JEI_SLOT_SIZE);
    private final ItemSlot kaleidoscopeSteamerResultSlot = createEditorSlot(JEI_SLOT_SIZE);
    private final ItemSlot kaleidoscopeTeapotFluidBucketSlot = createEditorSlot(JEI_SLOT_SIZE);
    private final IngredientDisplaySlot kaleidoscopeTeapotInputSlot = createIngredientSlot(JEI_SLOT_SIZE);
    private final ItemSlot kaleidoscopeTeapotResultSlot = createEditorSlot(JEI_SLOT_SIZE);
    private final Label kaleidoscopeTeapotTimeLabel = RecipeEditorUi.label(Component.empty());
    private final TrackedDummyWorld createManualApplicationPreviewWorld = new TrackedDummyWorld();
    private final Scene createManualApplicationBlockScene = new Scene();
    private Block createManualApplicationPreviewBlock = Blocks.AIR;
    private final Label titleLabel = RecipeEditorUi.sectionTitle("viscript_recipe.editor.workbench");
    private final Label statusLabel = RecipeEditorUi.label(Component.empty());
    private UIElement craftingCanvas;
    private UIElement mechanicalCraftingCanvas;
    private UIElement cookingCanvas;
    private UIElement campfireCookingCanvas;
    private UIElement singleInputCanvas;
    private UIElement farmersCookingPotCanvas;
    private UIElement farmersCuttingBoardCanvas;
    private UIElement smithingCanvas;
    private UIElement arcaneAnvilCanvas;
    private UIElement avaritiaCompressorCanvas;
    private UIElement avaritiaExtremeSmithingCanvas;
    private UIElement extendedCombinationCanvas;
    private UIElement extendedCompressorCanvas;
    private UIElement extendedFluxCanvas;
    private UIElement alchemistCanvas;
    private UIElement dragonForgeCanvas;
    private UIElement cataclysmWeaponFusionCanvas;
    private UIElement cataclysmAmethystBlessCanvas;
    private UIElement touhouLittleMaidAltarCanvas;
    private UIElement sporeSurgeryCanvas;
    private UIElement sporeGraftingCanvas;
    private UIElement goetyCursedInfuserCanvas;
    private UIElement goetyRitualCanvas;
    private UIElement goetyBrazierCanvas;
    private UIElement goetyPulverizeCanvas;
    private UIElement goetyBrewingCanvas;
    private UIElement createProcessingCanvas;
    private UIElement genericCreateProcessingCanvas;
    private UIElement createSpoutCanvas;
    private UIElement createDrainCanvas;
    private UIElement createFanCanvas;
    private UIElement createFanSingleOutputPanel;
    private UIElement createFanMultiOutputPanel;
    private UIElement createCrushingCanvas;
    private UIElement createMillingCanvas;
    private UIElement createSawCanvas;
    private UIElement createSawOutputGrid;
    private UIElement createBlockCuttingOutputGrid;
    private UIElement createSandpaperCanvas;
    private UIElement createAutoPackingCanvas;
    private UIElement createPressBasinCanvas;
    private UIElement createAutomaticBrewingCanvas;
    private UIElement createPressingCanvas;
    private UIElement createDeployerCanvas;
    private UIElement createManualApplicationCanvas;
    private UIElement createSequencedAssemblyCanvas;
    private UIElement arsNouveauApparatusCanvas;
    private UIElement arsNouveauApparatusCenterPreviewCell;
    private UIElement arsNouveauApparatusOutputPreviewCell;
    private UIElement arsNouveauImbuementCanvas;
    private UIElement arsNouveauGlyphCanvas;
    private UIElement arsNouveauCrushCanvas;
    private UIElement kaleidoscopePotCanvas;
    private UIElement kaleidoscopeStockpotCanvas;
    private UIElement kaleidoscopeMillstoneCanvas;
    private UIElement kaleidoscopeChoppingBoardCanvas;
    private UIElement kaleidoscopeSteamerCanvas;
    private UIElement kaleidoscopeTeapotCanvas;
    private UIElement createPressFluidInputRow;
    private UIElement createPressFluidOutputRow;
    private UIElement createPressHeatPanel;
    private UIElement createAutomaticBrewingHeatPanel;
    private UIElement alchemistResultFluidColumn;
    private UIElement alchemistOutputItemColumn;
    private UIElement canvasStack;
    private RecipeCanvasViewport canvasViewport;

    public CraftingWorkbenchView(RecipeEditorController controller) {
        super("viscript_recipe.view.workbench", Icons.GRID);
        this.controller = controller;
        addChild(createRoot());
        controller.addListener(this::refresh);
        refresh();
    }

    private UIElement createRoot() {
        var root = RecipeEditorUi.column().layout(layout -> {
            layout.widthPercent(100);
            layout.heightPercent(100);
            layout.paddingAll(8);
            layout.gapAll(8);
            layout.minWidth(0);
            layout.minHeight(0);
        }).style(style -> style.backgroundTexture(Sprites.RECT_SOLID));
        root.setOverflowVisible(false);

        var top = RecipeEditorUi.row().layout(layout -> {
            layout.widthPercent(100);
            layout.height(18);
            layout.alignItems(AlignItems.CENTER);
            layout.minWidth(0);
        });
        titleLabel.layout(layout -> {
            layout.flex(1);
            layout.minWidth(0);
        });
        statusLabel.textStyle(style -> style
                .textAlignHorizontal(Horizontal.RIGHT)
                .textWrap(TextWrap.HOVER_ROLL)
                .textColor(ColorPattern.LIGHT_GRAY.color));
        statusLabel.layout(layout -> {
            layout.flex(1);
            layout.minWidth(0);
            layout.height(18);
        });
        top.addChildren(titleLabel, statusLabel);

        root.addChildren(top, createCanvasStack());
        return root;
    }

    private UIElement createCanvasStack() {
        craftingCanvas = createCraftingCanvas();
        mechanicalCraftingCanvas = createMechanicalCraftingCanvas();
        cookingCanvas = createCookingCanvas();
        campfireCookingCanvas = createCampfireCookingCanvas();
        singleInputCanvas = createSingleInputCanvas();
        farmersCookingPotCanvas = createFarmersCookingPotCanvas();
        farmersCuttingBoardCanvas = createFarmersCuttingBoardCanvas();
        smithingCanvas = createSmithingCanvas();
        arcaneAnvilCanvas = createArcaneAnvilCanvas();
        avaritiaCompressorCanvas = createAvaritiaCompressorCanvas();
        avaritiaExtremeSmithingCanvas = createAvaritiaExtremeSmithingCanvas();
        extendedCombinationCanvas = createExtendedCombinationCanvas();
        extendedCompressorCanvas = createExtendedCompressorCanvas();
        extendedFluxCanvas = createExtendedFluxCanvas();
        alchemistCanvas = createAlchemistCanvas();
        dragonForgeCanvas = createDragonForgeCanvas();
        cataclysmWeaponFusionCanvas = createCataclysmWeaponFusionCanvas();
        cataclysmAmethystBlessCanvas = createCataclysmAmethystBlessCanvas();
        touhouLittleMaidAltarCanvas = createTouhouLittleMaidAltarCanvas();
        sporeSurgeryCanvas = createSporeSurgeryCanvas();
        sporeGraftingCanvas = createSporeGraftingCanvas();
        goetyCursedInfuserCanvas = createGoetyCursedInfuserCanvas();
        goetyRitualCanvas = createGoetyRitualCanvas();
        goetyBrazierCanvas = createGoetyBrazierCanvas();
        goetyPulverizeCanvas = createGoetyPulverizeCanvas();
        goetyBrewingCanvas = createGoetyBrewingCanvas();
        createProcessingCanvas = createCreateProcessingCanvas();
        createSequencedAssemblyCanvas = createCreateSequencedAssemblyCanvas();
        arsNouveauApparatusCanvas = createArsNouveauApparatusCanvas();
        arsNouveauImbuementCanvas = createArsNouveauImbuementCanvas();
        arsNouveauGlyphCanvas = createArsNouveauGlyphCanvas();
        arsNouveauCrushCanvas = createArsNouveauCrushCanvas();
        kaleidoscopePotCanvas = createKaleidoscopePotCanvas();
        kaleidoscopeStockpotCanvas = createKaleidoscopeStockpotCanvas();
        kaleidoscopeMillstoneCanvas = createKaleidoscopeMillstoneCanvas();
        kaleidoscopeChoppingBoardCanvas = createKaleidoscopeChoppingBoardCanvas();
        kaleidoscopeSteamerCanvas = createKaleidoscopeSteamerCanvas();
        kaleidoscopeTeapotCanvas = createKaleidoscopeTeapotCanvas();
        canvasStack = RecipeEditorUi.column().layout(layout -> {
            layout.widthPercent(100);
            layout.heightPercent(100);
            layout.minWidth(0);
            layout.minHeight(0);
        }).addChildren(craftingCanvas, mechanicalCraftingCanvas, cookingCanvas, campfireCookingCanvas, singleInputCanvas, farmersCookingPotCanvas, farmersCuttingBoardCanvas, smithingCanvas, arcaneAnvilCanvas, avaritiaCompressorCanvas, avaritiaExtremeSmithingCanvas, extendedCombinationCanvas, extendedCompressorCanvas, extendedFluxCanvas, alchemistCanvas, dragonForgeCanvas, cataclysmWeaponFusionCanvas, cataclysmAmethystBlessCanvas, touhouLittleMaidAltarCanvas, sporeSurgeryCanvas, sporeGraftingCanvas, goetyCursedInfuserCanvas, goetyRitualCanvas, goetyBrazierCanvas, goetyPulverizeCanvas, goetyBrewingCanvas, createProcessingCanvas, createSequencedAssemblyCanvas, arsNouveauApparatusCanvas, arsNouveauImbuementCanvas, arsNouveauGlyphCanvas, arsNouveauCrushCanvas, kaleidoscopePotCanvas, kaleidoscopeStockpotCanvas, kaleidoscopeMillstoneCanvas, kaleidoscopeChoppingBoardCanvas, kaleidoscopeSteamerCanvas, kaleidoscopeTeapotCanvas);
        canvasViewport = new RecipeCanvasViewport(canvasStack);
        return canvasViewport;
    }

    private UIElement createCraftingCanvas() {
        return BasicRecipeCanvasFactory.createCraftingCanvas(createGrid(), configureResultSlot(craftingOutputSlot));
    }

    private UIElement createMechanicalCraftingCanvas() {
        var outputSlot = configureResultSlot(mechanicalCraftingOutputSlot);
        var jeiOutputSlot = configureResultSlot(mechanicalCraftingJeiOutputSlot);
        configureJeiOverlaySlotVisual(jeiOutputSlot);
        var canvas = CreateMechanicalCraftingCanvasFactory.createCanvas(
                createMechanicalCraftingGrid(),
                mechanicalCraftingWorkstationIcon,
                mechanicalCraftingIngredientCountLabel,
                outputSlot,
                jeiOutputSlot,
                useJeiMechanicalCraftingCanvas
        );
        mechanicalCraftingFallbackProcess = canvas.fallbackProcess();
        mechanicalCraftingJeiProcess = canvas.jeiProcess();
        return canvas.root();
    }

    private UIElement createMechanicalCraftingGrid() {
        mechanicalCraftingGrid = RecipeEditorUi.column().layout(layout -> {
            layout.width(mechanicalCraftingGridDimension(3));
            layout.height(mechanicalCraftingGridDimension(3));
            layout.paddingAll(mechanicalCraftingGridPadding());
            layout.gapAll(mechanicalCraftingGridGap());
            layout.alignItems(AlignItems.CENTER);
            layout.justifyContent(AlignContent.CENTER);
        }).style(style -> style
                .backgroundTexture(Sprites.BORDER_DARK)
                .tooltips(Component.translatable("viscript_recipe.editor.create.mechanical_crafting.input_grid")));

        for (int row = 0; row < MECHANICAL_CRAFTING_GRID_SIZE; row++) {
            var rowElement = RecipeEditorUi.row().layout(layout -> {
                layout.widthPercent(100);
                layout.height(MECHANICAL_CRAFTING_SLOT_SIZE);
                layout.gapAll(mechanicalCraftingGridGap());
                layout.alignItems(AlignItems.CENTER);
                layout.justifyContent(AlignContent.CENTER);
            });
            mechanicalCraftingIngredientRows[row] = rowElement;
            for (int col = 0; col < MECHANICAL_CRAFTING_GRID_SIZE; col++) {
                var index = row * MECHANICAL_CRAFTING_GRID_SIZE + col;
                var slot = createIngredientSlot(MECHANICAL_CRAFTING_SLOT_SIZE);
                configureIngredientSlot(slot, index);
                mechanicalCraftingIngredientSlots[index] = slot;
                var cell = new UIElement().layout(layout -> {
                    layout.width(MECHANICAL_CRAFTING_SLOT_SIZE);
                    layout.height(MECHANICAL_CRAFTING_SLOT_SIZE);
                }).addChild(slot);
                mechanicalCraftingIngredientSlotCells[index] = cell;
                rowElement.addChild(cell);
            }
            mechanicalCraftingGrid.addChild(rowElement);
        }
        return mechanicalCraftingGrid;
    }

    private UIElement createSmithingCanvas() {
        var templateInput = createSmithingInput("viscript_recipe.editor.smithing.template", 0);
        var baseInput = createSmithingInput("viscript_recipe.editor.smithing.base", 1);
        var additionInput = createSmithingInput("viscript_recipe.editor.smithing.addition", 2);
        var outputSlot = configureResultSlot(smithingOutputSlot);
        if (useJeiSmithingCanvas) {
            configureJeiOverlaySlotVisual(outputSlot);
            return VanillaSmithingCanvasFactory.createCanvas(
                    templateInput,
                    baseInput,
                    additionInput,
                    VanillaSmithingCanvasFactory.createSlotCell(outputSlot)
            );
        }
        return BasicRecipeCanvasFactory.createSmithingCanvas(
                templateInput,
                baseInput,
                additionInput,
                outputSlot
        );
    }

    private UIElement createSmithingInput(String labelKey, int index) {
        var slot = createIngredientSlot(useJeiSmithingCanvas ? JEI_SLOT_SIZE : SLOT_SIZE);
        configureIngredientSlot(slot, index);
        smithingIngredientSlots[index] = slot;
        if (useJeiSmithingCanvas) {
            configureJeiOverlaySlotVisual(slot);
            var cell = VanillaSmithingCanvasFactory.createSlotCell(slot);
            smithingInputColumns[index] = cell;
            return cell;
        }
        var label = RecipeEditorUi.label(Component.translatable(labelKey));
        smithingInputLabels[index] = label;
        var column = RecipeEditorUi.column().layout(layout -> {
            layout.width(72);
            layout.gapAll(4);
            layout.alignItems(AlignItems.CENTER);
        }).addChildren(
                label,
                slot
        );
        smithingInputColumns[index] = column;
        return column;
    }

    private UIElement createArcaneAnvilCanvas() {
        if (!useJeiArcaneAnvilCanvas) {
            var unavailableCanvas = new UIElement();
            unavailableCanvas.setDisplay(false);
            return unavailableCanvas;
        }
        for (int index = 0; index < arcaneAnvilIngredientSlots.length; index++) {
            var slot = createIngredientSlot(JEI_SLOT_SIZE);
            configureIngredientSlot(slot, index);
            configureJeiOverlaySlotVisual(slot);
            arcaneAnvilIngredientSlots[index] = slot;
        }
        arcaneAnvilOutputSlot = createEditorSlot(JEI_SLOT_SIZE);
        configureResultSlot(arcaneAnvilOutputSlot);
        configureJeiOverlaySlotVisual(arcaneAnvilOutputSlot);
        return ArcaneAnvilCanvasFactory.createCanvas(
                arcaneAnvilIngredientSlots[0],
                arcaneAnvilIngredientSlots[1],
                arcaneAnvilOutputSlot
        );
    }

    private UIElement createAvaritiaCompressorCanvas() {
        configureIngredientSlot(avaritiaCompressorIngredientSlot, 0);
        configureResultSlot(avaritiaCompressorOutputSlot);
        configureJeiOverlaySlotVisual(avaritiaCompressorIngredientSlot);
        configureJeiOverlaySlotVisual(avaritiaCompressorOutputSlot);
        return AvaritiaCanvasFactory.createCompressorCanvas(
                avaritiaCompressorIngredientSlot,
                avaritiaCompressorOutputSlot
        );
    }

    private UIElement createAvaritiaExtremeSmithingCanvas() {
        configureResultSlot(avaritiaSmithingOutputSlot);
        configureJeiOverlaySlotVisual(avaritiaSmithingOutputSlot);
        for (int i = 0; i < avaritiaSmithingIngredientSlots.length; i++) {
            var slot = createIngredientSlot(JEI_SLOT_SIZE);
            configureIngredientSlot(slot, i);
            configureJeiOverlaySlotVisual(slot);
            avaritiaSmithingIngredientSlots[i] = slot;
        }
        return AvaritiaCanvasFactory.createExtremeSmithingCanvas(
                avaritiaSmithingIngredientSlots,
                avaritiaSmithingOutputSlot
        );
    }

    private UIElement createExtendedCombinationCanvas() {
        for (int i = 0; i < extendedCombinationIngredientSlots.length; i++) {
            var slot = createIngredientSlot(JEI_SLOT_SIZE);
            configureIngredientSlot(slot, i);
            configureJeiOverlaySlotVisual(slot);
            extendedCombinationIngredientSlots[i] = slot;
        }
        configureResultSlot(extendedCombinationOutputSlot);
        configureJeiOverlaySlotVisual(extendedCombinationOutputSlot);

        return ExtendedCraftingCanvasFactory.createCombinationCanvas(
                extendedCombinationIngredientSlots,
                extendedCombinationOutputSlot
        );
    }

    private UIElement createExtendedCompressorCanvas() {
        configureIngredientSlot(extendedCompressorCatalystSlot, 0);
        configureIngredientSlot(extendedCompressorInputSlot, 1);
        configureResultSlot(extendedCompressorOutputSlot);
        configureJeiOverlaySlotVisual(extendedCompressorCatalystSlot);
        configureJeiOverlaySlotVisual(extendedCompressorInputSlot);
        configureJeiOverlaySlotVisual(extendedCompressorOutputSlot);
        return ExtendedCraftingCanvasFactory.createCompressorCanvas(
                extendedCompressorCatalystSlot,
                extendedCompressorInputSlot,
                extendedCompressorOutputSlot
        );
    }

    private UIElement createExtendedFluxCanvas() {
        for (int i = 0; i < extendedFluxIngredientSlots.length; i++) {
            var slot = createIngredientSlot(SLOT_SIZE);
            configureIngredientSlot(slot, i);
            extendedFluxIngredientSlots[i] = slot;
        }
        configureResultSlot(extendedFluxOutputSlot);

        return ExtendedCraftingCanvasFactory.createFluxCanvas(
                extendedFluxIngredientSlots,
                extendedFluxOutputSlot
        );
    }

    private UIElement createGrid() {
        return RecipeGridFactory.borderedGrid(3, 3, SLOT_SIZE, (index, row, col) -> {
            var slot = createIngredientSlot(SLOT_SIZE);
            configureIngredientSlot(slot, index);
            craftingIngredientSlots[index] = slot;
            return slot;
        });
    }

    private UIElement createCookingCanvas() {
        configureIngredientSlot(cookingIngredientSlot, 0);
        if (!useJeiCookingCanvas) {
            return BasicRecipeCanvasFactory.createCookingCanvas(
                    cookingIngredientSlot,
                    configureResultSlot(cookingOutputSlot)
            );
        }
        configureJeiOverlaySlotVisual(cookingIngredientSlot);
        configureJeiOverlaySlotVisual(cookingOutputSlot);
        return VanillaCookingCanvasFactory.createFurnaceCanvas(
                cookingIngredientSlot,
                configureResultSlot(cookingOutputSlot),
                cookingExperienceLabel,
                cookingTimeLabel
        );
    }

    private UIElement createCampfireCookingCanvas() {
        configureIngredientSlot(campfireCookingIngredientSlot, 0);
        if (!useJeiCookingCanvas) {
            return BasicRecipeCanvasFactory.createCookingCanvas(
                    campfireCookingIngredientSlot,
                    configureResultSlot(campfireCookingOutputSlot)
            );
        }
        configureJeiOverlaySlotVisual(campfireCookingIngredientSlot);
        configureJeiOverlaySlotVisual(campfireCookingOutputSlot);
        return VanillaCookingCanvasFactory.createCampfireCanvas(
                campfireCookingIngredientSlot,
                configureResultSlot(campfireCookingOutputSlot),
                campfireCookingTimeLabel
        );
    }

    private UIElement createSingleInputCanvas() {
        configureIngredientSlot(singleInputIngredientSlot, 0);
        if (!useJeiStonecuttingCanvas) {
            return BasicRecipeCanvasFactory.createCookingCanvas(
                    singleInputIngredientSlot,
                    configureResultSlot(singleInputOutputSlot)
            );
        }
        configureJeiOverlaySlotVisual(singleInputIngredientSlot);
        configureJeiOverlaySlotVisual(singleInputOutputSlot);
        return VanillaStonecuttingCanvasFactory.createCanvas(
                singleInputIngredientSlot,
                configureResultSlot(singleInputOutputSlot)
        );
    }

    private UIElement createFarmersCookingPotCanvas() {
        if (useJeiFarmersCookingPotCanvas) {
            for (int index = 0; index < farmerCookingIngredientSlots.length; index++) {
                var slot = createIngredientSlot(JEI_SLOT_SIZE);
                configureIngredientSlot(slot, index);
                configureJeiOverlaySlotVisual(slot);
                farmerCookingIngredientSlots[index] = slot;
            }
            configureJeiOverlaySlotVisual(farmerCookingPotPreviewSlot);
            configureJeiOverlaySlotVisual(farmerCookingContainerSlot);
            configureJeiOverlaySlotVisual(farmerCookingOutputSlot);
            return FarmersDelightCanvasFactory.createJeiCookingPotCanvas(
                    farmerCookingIngredientSlots,
                    configureResultSlot(farmerCookingPotPreviewSlot),
                    configureContainerSlot(farmerCookingContainerSlot),
                    configureResultSlot(farmerCookingOutputSlot),
                    farmerCookingTimeIcon,
                    farmerCookingExperienceIcon
            );
        }
        return FarmersDelightCanvasFactory.createCookingPotCanvas(
                createFarmerCookingIngredientGrid(),
                FarmersDelightCanvasFactory.createHeatSource(createItemIcon(new ItemStack(Items.CAMPFIRE), 30)),
                FarmersDelightCanvasFactory.createPotPreview(
                        createItemIcon(new ItemStack(itemFromRegistry("farmersdelight:cooking_pot", Items.CAULDRON)), 18),
                        configureResultSlot(farmerCookingPotPreviewSlot)
                ),
                FarmersDelightCanvasFactory.createServingRow(
                        configureContainerSlot(farmerCookingContainerSlot),
                        configureResultSlot(farmerCookingOutputSlot)
                )
        );
    }

    private UIElement createFarmerCookingIngredientGrid() {
        return RecipeGridFactory.borderedGrid(3, 2, SLOT_SIZE, (index, row, col) -> {
            var slot = createIngredientSlot(SLOT_SIZE);
            configureIngredientSlot(slot, index);
            farmerCookingIngredientSlots[index] = slot;
            return slot;
        });
    }

    private UIElement createFarmersCuttingBoardCanvas() {
        if (useJeiFarmersCuttingBoardCanvas) {
            for (int index = 0; index < farmerCuttingIngredientSlots.length; index++) {
                var slot = createIngredientSlot(JEI_SLOT_SIZE);
                configureIngredientSlot(slot, index);
                configureJeiOverlaySlotVisual(slot);
                farmerCuttingIngredientSlots[index] = slot;
            }
            for (int index = 0; index < farmerCuttingResultSlots.length; index++) {
                var slot = createEditorSlot(JEI_SLOT_SIZE);
                configureCuttingResultSlot(slot, index);
                configureJeiOverlaySlotVisual(slot);
                farmerCuttingResultSlots[index] = slot;
            }
            return FarmersDelightCanvasFactory.createJeiCuttingBoardCanvas(
                    farmerCuttingIngredientSlots[0],
                    farmerCuttingIngredientSlots[1],
                    farmerCuttingResultSlots,
                    farmerCuttingResultCells
            );
        }
        return FarmersDelightCanvasFactory.createCuttingBoardCanvas(
                createFarmerCuttingInput("viscript_recipe.editor.farmersdelight.cutting.input", 0),
                createFarmerCuttingInput("viscript_recipe.editor.farmersdelight.cutting.tool", 1),
                createFarmerCuttingResultGrid()
        );
    }

    private UIElement createFarmerCuttingInput(String labelKey, int index) {
        var slot = createIngredientSlot(SLOT_SIZE);
        configureIngredientSlot(slot, index);
        farmerCuttingIngredientSlots[index] = slot;
        return FarmersDelightCanvasFactory.createCuttingInput(labelKey, slot);
    }

    private UIElement createFarmerCuttingResultGrid() {
        return RecipeGridFactory.borderedGrid(2, 2, SLOT_SIZE, (index, row, col) -> {
            var slot = createEditorSlot(SLOT_SIZE);
            configureCuttingResultSlot(slot, index);
            farmerCuttingResultSlots[index] = slot;
            return slot;
        });
    }

    private UIElement createAlchemistCanvas() {
        configureIngredientSlot(alchemistIngredientSlot, 0);
        configureFluidSlot(alchemistMiddleFluidSlot, 0);
        configureFluidSlot(alchemistResultFluidSlot, 1);
        var outputSlot = configureResultSlot(alchemistOutputSlot);
        if (useJeiAlchemistCanvas) {
            configureJeiOverlaySlotVisual(alchemistIngredientSlot);
            configureJeiOverlayFluidSlotVisual(alchemistMiddleFluidSlot);
            configureJeiOverlayFluidSlotVisual(alchemistResultFluidSlot);
            configureJeiOverlaySlotVisual(outputSlot);
            alchemistResultFluidColumn = alchemistResultFluidSlot;
            alchemistOutputItemColumn = outputSlot;
            alchemistChanceLabel.setDisplay(false);
            return AlchemistCauldronCanvasFactory.createCanvas(
                    alchemistIngredientSlot,
                    alchemistMiddleFluidSlot,
                    alchemistResultFluidSlot,
                    outputSlot,
                    createItemIcon(new ItemStack(itemFromRegistry(
                            "irons_spellbooks:alchemist_cauldron",
                            Items.CAULDRON
                    )), 23),
                    alchemistChanceLabel
            );
        }
        alchemistResultFluidColumn = createFluidColumn(alchemistOutputFluidLabel, alchemistResultFluidSlot);
        alchemistOutputItemColumn = RecipeEditorUi.column().layout(layout -> {
            layout.width(72);
            layout.gapAll(4);
            layout.alignItems(AlignItems.CENTER);
        }).addChildren(
                alchemistOutputLabel,
                outputSlot
        );
        return BasicRecipeCanvasFactory.createAlchemistCanvas(
                RecipeEditorUi.column().layout(layout -> {
                    layout.width(72);
                    layout.gapAll(4);
                    layout.alignItems(AlignItems.CENTER);
                }).addChildren(
                        RecipeEditorUi.label(Component.translatable("viscript_recipe.editor.alchemist_cauldron.input")),
                        alchemistIngredientSlot
                ),
                alchemistInputPlusLabel,
                alchemistInputArrow,
                createFluidColumn(alchemistMiddleFluidLabel, alchemistMiddleFluidSlot),
                alchemistOutputArrow,
                alchemistResultFluidColumn,
                alchemistOutputPlusLabel,
                alchemistOutputItemColumn
        );
    }

    private UIElement createDragonForgeCanvas() {
        var outputSlot = configureResultSlot(dragonForgeOutputSlot);
        if (useJeiDragonForgeCanvas) {
            var inputSlot = createDragonForgeJeiInput(
                    "viscript_recipe.config.iceandfire.dragon_forge.input",
                    0
            );
            var bloodSlot = createDragonForgeJeiInput(
                    "viscript_recipe.config.iceandfire.dragon_forge.blood",
                    1
            );
            configureJeiOverlaySlotVisual(outputSlot);
            return DragonForgeCanvasFactory.createJeiCanvas(
                    inputSlot,
                    bloodSlot,
                    outputSlot,
                    dragonForgeJeiTypeLayers,
                    controller::selectRecipeProperties
            );
        }
        return BasicRecipeCanvasFactory.createDragonForgeCanvas(
                createDragonBreathColumn(),
                createDragonForgeInputColumn(),
                outputSlot
        );
    }

    private IngredientDisplaySlot createDragonForgeJeiInput(String labelKey, int index) {
        var slot = createIngredientSlot(JEI_SLOT_SIZE);
        configureIngredientSlot(slot, index);
        configureJeiOverlaySlotVisual(slot);
        slot.style(style -> style.tooltips(Component.translatable(labelKey)));
        dragonForgeIngredientSlots[index] = slot;
        return slot;
    }

    private UIElement createCataclysmWeaponFusionCanvas() {
        configureIngredientSlot(cataclysmWeaponFusionBaseSlot, 0);
        configureIngredientSlot(cataclysmWeaponFusionAdditionSlot, 1);
        configureJeiOverlaySlotVisual(cataclysmWeaponFusionBaseSlot);
        configureJeiOverlaySlotVisual(cataclysmWeaponFusionAdditionSlot);
        configureJeiOverlaySlotVisual(cataclysmWeaponFusionResultSlot);
        cataclysmWeaponFusionBaseSlot.style(style -> style.tooltips(
                Component.translatable("viscript_recipe.editor.cataclysm.weapon_fusion.base_slot")));
        cataclysmWeaponFusionAdditionSlot.style(style -> style.tooltips(
                Component.translatable("viscript_recipe.editor.cataclysm.weapon_fusion.addition_slot")));
        return CataclysmCanvasFactory.createWeaponFusionCanvas(
                cataclysmWeaponFusionBaseSlot,
                cataclysmWeaponFusionAdditionSlot,
                configureResultSlot(cataclysmWeaponFusionResultSlot)
        );
    }

    private UIElement createCataclysmAmethystBlessCanvas() {
        configureIngredientSlot(cataclysmAmethystBlessIngredientSlot, 0);
        configureJeiOverlaySlotVisual(cataclysmAmethystBlessIngredientSlot);
        configureJeiOverlaySlotVisual(cataclysmAmethystBlessResultSlot);
        cataclysmAmethystBlessIngredientSlot.style(style -> style.tooltips(
                Component.translatable("viscript_recipe.editor.cataclysm.amethyst_bless.ingredient_slot")));
        return CataclysmCanvasFactory.createAmethystBlessCanvas(
                cataclysmAmethystBlessIngredientSlot,
                configureResultSlot(cataclysmAmethystBlessResultSlot)
        );
    }

    private UIElement createTouhouLittleMaidAltarCanvas() {
        for (int index = 0; index < touhouLittleMaidAltarIngredientSlots.length; index++) {
            var slot = createIngredientSlot(JEI_SLOT_SIZE);
            configureIngredientSlot(slot, index);
            configureJeiOverlaySlotVisual(slot);
            touhouLittleMaidAltarIngredientSlots[index] = slot;
        }
        configureJeiOverlaySlotVisual(touhouLittleMaidAltarResultSlot);
        return TouhouLittleMaidAltarCanvasFactory.createCanvas(
                touhouLittleMaidAltarIngredientSlots,
                configureResultSlot(touhouLittleMaidAltarResultSlot),
                touhouLittleMaidAltarPowerLabel,
                touhouLittleMaidAltarResultDescriptionLabel,
                controller::selectRecipeProperties
        );
    }

    private UIElement createSporeSurgeryCanvas() {
        for (int index = 0; index < sporeSurgeryIngredientSlots.length; index++) {
            var slot = createIngredientSlot(JEI_SLOT_SIZE);
            configureIngredientSlot(slot, index);
            configureJeiOverlaySlotVisual(slot);
            sporeSurgeryIngredientSlots[index] = slot;
        }
        configureJeiOverlaySlotVisual(sporeSurgeryOutputSlot);
        return SporeCanvasFactory.createSurgeryCanvas(
                sporeSurgeryIngredientSlots,
                configureResultSlot(sporeSurgeryOutputSlot)
        );
    }

    private UIElement createSporeGraftingCanvas() {
        for (int i = 0; i < sporeGraftingIngredientSlots.length; i++) {
            var slot = createIngredientSlot(JEI_SLOT_SIZE);
            configureIngredientSlot(slot, i);
            configureJeiOverlaySlotVisual(slot);
            sporeGraftingIngredientSlots[i] = slot;
        }
        configureJeiOverlaySlotVisual(sporeGraftingOutputSlot);
        return SporeCanvasFactory.createGraftingCanvas(
                sporeGraftingIngredientSlots,
                configureResultSlot(sporeGraftingOutputSlot)
        );
    }

    private UIElement createGoetyCursedInfuserCanvas() {
        configureIngredientSlot(goetyCursedInfuserIngredientSlot, 0);
        configureResultSlot(goetyCursedInfuserOutputSlot);
        goetyCursedInfuserMachineIcon.style(style -> style.tooltips(Component.translatable(
                "viscript_recipe.editor.goety.cursed_infuser.machine"
        )));
        centerGoetyLabel(goetyCursedInfuserTimeLabel);
        return GoetyCanvasFactory.createCursedInfuserCanvas(
                goetyCursedInfuserIngredientSlot,
                goetyCursedInfuserOutputSlot,
                goetyCursedInfuserMachineIcon,
                goetyCursedInfuserTimeLabel,
                useJeiGoetyCanvas
        );
    }

    private UIElement createGoetyRitualCanvas() {
        configureIngredientSlot(goetyRitualActivationSlot, 0);
        configureGoetyJeiSlot(goetyRitualActivationSlot);
        goetyRitualActivationSlot.style(style -> style.tooltips(Component.translatable(
                "viscript_recipe.editor.goety.ritual.activation_slot"
        )));
        for (int index = 0; index < goetyRitualIngredientSlots.length; index++) {
            var slot = createIngredientSlot(useJeiGoetyCanvas ? JEI_SLOT_SIZE : SLOT_SIZE);
            configureIngredientSlot(slot, index + 1);
            configureGoetyJeiSlot(slot);
            slot.style(style -> style.tooltips(Component.translatable(
                    "viscript_recipe.editor.goety.ritual.ingredient_slot",
                    ingredientDragSlotIndices.getOrDefault(slot, 1)
            )));
            goetyRitualIngredientSlots[index] = slot;
        }
        configureResultSlot(goetyRitualOutputSlot);
        configureGoetyJeiSlot(goetyRitualOutputSlot);
        goetyRitualTypeIcon.style(style -> style.tooltips(Component.translatable(
                "viscript_recipe.editor.goety.ritual.type_icon"
        )));
        goetyRitualResearchIcon.style(style -> style.tooltips(Component.translatable(
                "viscript_recipe.editor.goety.ritual.research_scroll"
        )));
        centerGoetyLabel(goetyRitualInfoLabel);
        return GoetyCanvasFactory.createRitualCanvas(
                goetyRitualActivationSlot,
                goetyRitualIngredientSlots,
                goetyRitualOutputSlot,
                goetyRitualTypeIcon,
                goetyRitualResearchIcon,
                goetyRitualInfoLabel,
                useJeiGoetyCanvas
        );
    }

    private UIElement createGoetyBrazierCanvas() {
        for (int index = 0; index < goetyBrazierIngredientSlots.length; index++) {
            var slot = createIngredientSlot(useJeiGoetyCanvas ? JEI_SLOT_SIZE : SLOT_SIZE);
            configureIngredientSlot(slot, index);
            configureGoetyJeiSlot(slot);
            goetyBrazierIngredientSlots[index] = slot;
        }
        configureResultSlot(goetyBrazierOutputSlot);
        configureGoetyJeiSlot(goetyBrazierOutputSlot);
        centerGoetyLabel(goetyBrazierSoulLabel);
        return GoetyCanvasFactory.createBrazierCanvas(
                goetyBrazierIngredientSlots,
                goetyBrazierOutputSlot,
                goetyBrazierSoulLabel,
                useJeiGoetyCanvas
        );
    }

    private UIElement createGoetyPulverizeCanvas() {
        configureIngredientSlot(goetyPulverizeIngredientSlot, 0);
        configureResultSlot(goetyPulverizeOutputSlot);
        configureGoetyJeiSlot(goetyPulverizeIngredientSlot);
        configureGoetyJeiSlot(goetyPulverizeOutputSlot);
        return GoetyCanvasFactory.createPulverizeCanvas(
                goetyPulverizeIngredientSlot,
                goetyPulverizeOutputSlot,
                useJeiGoetyCanvas
        );
    }

    private UIElement createGoetyBrewingCanvas() {
        configureIngredientSlot(goetyBrewingCatalystSlot, 0);
        configureGoetyJeiSlot(goetyBrewingCatalystSlot);
        goetyBrewingCatalystSlot.style(style -> style.tooltips(Component.translatable(
                "viscript_recipe.editor.goety.brewing.editable_catalyst"
        )));
        centerGoetyLabel(goetyBrewingInfoLabel);
        return GoetyCanvasFactory.createBrewingCanvas(
                goetyBrewingCatalystSlot,
                goetyBrewingOutputIcon,
                goetyBrewingInfoLabel,
                useJeiGoetyCanvas
        );
    }

    private void configureGoetyJeiSlot(ItemSlot slot) {
        if (useJeiGoetyCanvas) {
            configureJeiOverlaySlotVisual(slot);
        }
    }

    private static void centerGoetyLabel(Label label) {
        label.textStyle(style -> style
                .textAlignHorizontal(Horizontal.CENTER)
                .textWrap(TextWrap.HOVER_ROLL));
    }

    private UIElement createCreateProcessingCanvas() {
        genericCreateProcessingCanvas = createGenericCreateProcessingCanvas();
        createSpoutCanvas = createCreateSpoutCanvas();
        createDrainCanvas = createCreateDrainCanvas();
        createFanCanvas = createCreateFanCanvas();
        createCrushingCanvas = createCreateCrushingCanvas();
        createMillingCanvas = createCreateMillingCanvas();
        createSawCanvas = createCreateSawCanvas();
        createAutoPackingCanvas = createCreateAutoPackingCanvas();
        createSandpaperCanvas = createCreateSandpaperCanvas();
        createPressBasinCanvas = createCreatePressBasinCanvas();
        createAutomaticBrewingCanvas = createCreateAutomaticBrewingCanvas();
        createPressingCanvas = createCreatePressingCanvas();
        createDeployerCanvas = createCreateDeployerCanvas();
        createManualApplicationCanvas = createCreateManualApplicationCanvas();
        return CreateProcessingCanvasFactory.createProcessingStack(genericCreateProcessingCanvas, createSpoutCanvas, createDrainCanvas, createFanCanvas, createCrushingCanvas, createMillingCanvas, createSawCanvas, createAutoPackingCanvas, createSandpaperCanvas, createPressBasinCanvas, createAutomaticBrewingCanvas, createPressingCanvas, createDeployerCanvas, createManualApplicationCanvas);
    }

    private UIElement createCreateSequencedAssemblyCanvas() {
        configureIngredientSlot(createSequencedInputSlot, 0);
        configureCreateSequencedTransitionalSlot(createSequencedTransitionalSlot);
        for (int i = 0; i < createSequencedOutputSlots.length; i++) {
            var slot = createEditorSlot(SLOT_SIZE);
            configureCreateOutputSlot(slot, i);
            createSequencedOutputSlots[i] = slot;
        }
        createSequencedLoopsLabel.textStyle(style -> style
                .textAlignHorizontal(Horizontal.CENTER)
                .textColor(ColorPattern.LIGHT_GRAY.color)
                .textWrap(TextWrap.HOVER_ROLL));
        createSequencedLoopsLabel.layout(layout -> layout.width(62).height(16));
        var canvas = CreateSequencedAssemblyCanvasFactory.createCanvas(
                createSequencedInputSlot,
                createSequencedTransitionalSlot,
                createSequencedOutputSlots,
                createSequencedOutputSlotCells,
                createSequencedStepCards,
                createSequencedStepIcons,
                createSequencedStepLabels,
                createSequencedLoopsLabel,
                this::createCreateSequencedStepIngredientCell,
                this::createCreateSequencedStepFluidCell,
                controller::selectCreateSequencedStep
        );
        createSequencedSecondaryOutputColumn = canvas.secondaryOutputColumn();
        return canvas.root();
    }

    private UIElement createCreateSequencedStepIngredientCell(int index) {
        var ingredientSlot = createIngredientSlot(SLOT_SIZE);
        configureCreateSequencedStepIngredientSlot(ingredientSlot, createSequencedIngredientSlotIndex(index));
        createSequencedStepIngredientSlots[index] = ingredientSlot;
        var ingredientCell = createCreateFramedSlot(ingredientSlot, 36);
        configureCreateSequencedStepIngredientCell(ingredientCell, createSequencedIngredientSlotIndex(index));
        createSequencedStepIngredientCells[index] = ingredientCell;
        return ingredientCell;
    }

    private UIElement createCreateSequencedStepFluidCell(int index) {
        var fluidSlot = createFluidDisplaySlot();
        configureCreateSequencedStepFluidSlot(fluidSlot, index);
        createSequencedStepFluidSlots[index] = fluidSlot;
        var fluidCell = createCreateFramedSlot(fluidSlot, 36);
        configureCreateSequencedStepFluidCell(fluidCell, index);
        createSequencedStepFluidCells[index] = fluidCell;
        return fluidCell;
    }

    private UIElement createArsNouveauApparatusCanvas() {
        configureResultSlot(arsNouveauResultSlot);
        arsNouveauApparatusCenterPreviewIcon.style(style -> style.tooltips(Component.translatable("viscript_recipe.editor.ars_nouveau.center_preview")));
        arsNouveauApparatusOutputPreviewIcon.style(style -> style.tooltips(Component.translatable("viscript_recipe.editor.ars_nouveau.output_preview")));
        arsNouveauApparatusSourceLabel.textStyle(style -> style
                .textColor(ColorPattern.WHITE.color)
                .textWrap(TextWrap.HOVER_ROLL));
        arsNouveauApparatusTierLabel.textStyle(style -> style
                .textColor(ColorPattern.WHITE.color)
                .textWrap(TextWrap.HOVER_ROLL));
        for (int i = 0; i < arsNouveauIngredientSlots.length; i++) {
            var slot = createIngredientSlot(SLOT_SIZE);
            configureIngredientSlot(slot, i);
            arsNouveauIngredientSlots[i] = slot;
        }

        var canvas = ArsNouveauCanvasFactory.createApparatusCanvas(
                arsNouveauIngredientSlots,
                arsNouveauIngredientSlotCells,
                arsNouveauResultSlot,
                arsNouveauApparatusCenterPreviewIcon,
                arsNouveauApparatusOutputPreviewIcon,
                arsNouveauApparatusSourceLabel,
                arsNouveauApparatusTierLabel,
                this::arsNouveauIngredientTooltip
        );
        arsNouveauApparatusCenterPreviewCell = canvas.centerPreviewCell();
        arsNouveauApparatusOutputPreviewCell = canvas.outputPreviewCell();
        return canvas.root();
    }

    private UIElement createArsNouveauImbuementCanvas() {
        configureIngredientSlot(arsNouveauImbuementInputSlot, 0);
        configureResultSlot(arsNouveauImbuementResultSlot);
        arsNouveauImbuementDefaultCenterIcon.style(style -> style
                .backgroundTexture(new ItemStackTexture(new ItemStack(itemFromRegistry("ars_nouveau:imbuement_chamber", Items.ENCHANTING_TABLE))))
                .tooltips(Component.translatable("block.ars_nouveau.imbuement_chamber")));
        arsNouveauImbuementSourceLabel.textStyle(style -> style
                .textColor(ColorPattern.WHITE.color)
                .textWrap(TextWrap.HOVER_ROLL));
        for (int i = 0; i < arsNouveauImbuementPedestalSlots.length; i++) {
            var slot = createIngredientSlot(SLOT_SIZE);
            configureIngredientSlot(slot, i + 1);
            arsNouveauImbuementPedestalSlots[i] = slot;
        }

        return ArsNouveauCanvasFactory.createImbuementCanvas(
                arsNouveauImbuementInputSlot,
                arsNouveauImbuementPedestalSlots,
                arsNouveauImbuementPedestalSlotCells,
                arsNouveauImbuementDefaultCenterIcon,
                arsNouveauImbuementResultSlot,
                arsNouveauImbuementSourceLabel,
                this::arsNouveauIngredientTooltip
        );
    }

    private UIElement createArsNouveauGlyphCanvas() {
        configureResultSlot(arsNouveauGlyphResultSlot);
        arsNouveauGlyphWorkstationIcon.style(style -> style
                .backgroundTexture(new ItemStackTexture(controller.selectedArsNouveauWorkstationStack()))
                .tooltips(Component.translatable("block.ars_nouveau.scribes_table")));
        arsNouveauGlyphExpLabel.textStyle(style -> style
                .textAlignHorizontal(Horizontal.LEFT)
                .textColor(ColorPattern.BLACK.color)
                .textWrap(TextWrap.HOVER_ROLL));
        for (int i = 0; i < arsNouveauGlyphIngredientSlots.length; i++) {
            var slot = createIngredientSlot(SLOT_SIZE);
            configureIngredientSlot(slot, i);
            configureFloatingIngredientSlot(slot);
            arsNouveauGlyphIngredientSlots[i] = slot;
        }

        return ArsNouveauCanvasFactory.createGlyphCanvas(
                arsNouveauGlyphIngredientSlots,
                arsNouveauGlyphIngredientSlotCells,
                arsNouveauGlyphWorkstationIcon,
                arsNouveauGlyphResultSlot,
                arsNouveauGlyphExpLabel,
                this::arsNouveauIngredientTooltip
        );
    }

    private static void configureFloatingIngredientSlot(IngredientDisplaySlot slot) {
        slot.style(style -> style.backgroundTexture(IGuiTexture.EMPTY));
        slot.slotStyle(style -> style
                .slotOverlay(ItemSlot.ITEM_SLOT_TEXTURE)
                .showSlotOverlayOnlyEmpty(true));
    }

    private static void configureJeiOverlaySlotVisual(ItemSlot slot) {
        slot.style(style -> style.backgroundTexture(IGuiTexture.EMPTY));
        slot.slotStyle(style -> style.slotOverlay(IGuiTexture.EMPTY));
    }

    private static void configureJeiOverlayFluidSlotVisual(FluidSlot slot) {
        slot.style(style -> style.backgroundTexture(IGuiTexture.EMPTY));
        slot.slotStyle(style -> style.slotOverlay(IGuiTexture.EMPTY));
        slot.amountLabel.setDisplay(false);
    }

    private UIElement createArsNouveauCrushCanvas() {
        configureIngredientSlot(arsNouveauCrushInputSlot, 0);
        for (int i = 0; i < arsNouveauCrushOutputSlots.length; i++) {
            var slot = createEditorSlot(SLOT_SIZE);
            configureArsNouveauOutputSlot(slot, i);
            arsNouveauCrushOutputSlots[i] = slot;
        }
        return ArsNouveauCanvasFactory.createCrushCanvas(
                arsNouveauCrushInputSlot,
                createItemIcon(new ItemStack(itemFromRegistry("ars_nouveau:glyph_crush", Items.IRON_PICKAXE)), 82),
                arsNouveauCrushOutputSlots,
                arsNouveauCrushOutputSlotCells
        );
    }

    private Component arsNouveauIngredientTooltip(int index) {
        return Component.translatable(
                "viscript_recipe.editor.ars_nouveau.ingredient_slot",
                controller.arsNouveauInputSlotName(index)
        );
    }

    private UIElement createKaleidoscopePotCanvas() {
        configureKaleidoscopeGridSlots(kaleidoscopePotIngredientSlots);
        configureKaleidoscopeIngredientSlot(kaleidoscopePotCarrierSlot, KALEIDOSCOPE_CARRIER_SLOT);
        configureKaleidoscopeResultSlot(kaleidoscopePotResultSlot);
        configureKaleidoscopeInfoLabel(kaleidoscopePotStirFryLabel);

        return KaleidoscopeCanvasFactory.createPotCanvas(
                kaleidoscopePotIngredientSlots,
                kaleidoscopePotCarrierSlot,
                kaleidoscopePotResultSlot,
                kaleidoscopePotStirFryLabel
        );
    }

    private UIElement createKaleidoscopeStockpotCanvas() {
        configureKaleidoscopeGridSlots(kaleidoscopeStockpotIngredientSlots);
        configureKaleidoscopeIngredientSlot(kaleidoscopeStockpotCarrierSlot, KALEIDOSCOPE_CARRIER_SLOT);
        configureKaleidoscopeResultSlot(kaleidoscopeStockpotResultSlot);

        return KaleidoscopeCanvasFactory.createStockpotCanvas(
                kaleidoscopeStockpotIngredientSlots,
                kaleidoscopeStockpotCarrierSlot,
                kaleidoscopeStockpotResultSlot
        );
    }

    private UIElement createKaleidoscopeMillstoneCanvas() {
        configureKaleidoscopeIngredientSlot(kaleidoscopeMillstoneInputSlot, 0);
        configureKaleidoscopeResultSlot(kaleidoscopeMillstoneResultSlot);

        return KaleidoscopeCanvasFactory.createMillstoneCanvas(
                kaleidoscopeMillstoneInputSlot,
                kaleidoscopeMillstoneResultSlot
        );
    }

    private UIElement createKaleidoscopeChoppingBoardCanvas() {
        configureKaleidoscopeIngredientSlot(kaleidoscopeChoppingBoardInputSlot, 0);
        configureKaleidoscopeResultSlot(kaleidoscopeChoppingBoardResultSlot);

        return KaleidoscopeCanvasFactory.createChoppingBoardCanvas(
                kaleidoscopeChoppingBoardInputSlot,
                kaleidoscopeChoppingBoardResultSlot
        );
    }

    private UIElement createKaleidoscopeSteamerCanvas() {
        configureKaleidoscopeIngredientSlot(kaleidoscopeSteamerInputSlot, 0);
        configureKaleidoscopeResultSlot(kaleidoscopeSteamerResultSlot);

        return KaleidoscopeCanvasFactory.createSteamerCanvas(
                kaleidoscopeSteamerInputSlot,
                kaleidoscopeSteamerResultSlot
        );
    }

    private UIElement createKaleidoscopeTeapotCanvas() {
        configureKaleidoscopeTeapotFluidBucketSlot(kaleidoscopeTeapotFluidBucketSlot);
        configureKaleidoscopeIngredientSlot(kaleidoscopeTeapotInputSlot, 0);
        configureKaleidoscopeResultSlot(kaleidoscopeTeapotResultSlot);
        configureKaleidoscopeInfoLabel(kaleidoscopeTeapotTimeLabel);

        return KaleidoscopeCanvasFactory.createTeapotCanvas(
                kaleidoscopeTeapotFluidBucketSlot,
                kaleidoscopeTeapotInputSlot,
                kaleidoscopeTeapotResultSlot,
                kaleidoscopeTeapotTimeLabel
        );
    }

    private void configureKaleidoscopeGridSlots(IngredientDisplaySlot[] slots) {
        for (int i = 0; i < slots.length; i++) {
            var slot = createIngredientSlot(JEI_SLOT_SIZE);
            configureKaleidoscopeIngredientSlot(slot, i);
            slots[i] = slot;
        }
    }

    private IngredientDisplaySlot configureKaleidoscopeIngredientSlot(IngredientDisplaySlot slot, int index) {
        configureIngredientSlot(slot, index);
        configureJeiOverlaySlotVisual(slot);
        return slot;
    }

    private ItemSlot configureKaleidoscopeResultSlot(ItemSlot slot) {
        configureResultSlot(slot);
        configureJeiOverlaySlotVisual(slot);
        return slot;
    }

    private ItemSlot configureKaleidoscopeTeapotFluidBucketSlot(ItemSlot slot) {
        slot.registerValueListener(stack -> {
            if (!controller.isRefreshing()) {
                controller.setVisualKaleidoscopeTeapotFluidBucket(stack);
            }
        });
        slot.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            controller.selectRecipeProperties();
            if (event.button == 1) {
                controller.resetKaleidoscopeTeapotFluidBucket();
                event.stopPropagation();
            }
        });
        configureJeiOverlaySlotVisual(slot);
        slot.style(style -> style.tooltips(Component.translatable("viscript_recipe.config.kaleidoscope_cookery.tea_fluid")));
        return slot;
    }

    private void configureKaleidoscopeInfoLabel(Label label) {
        label.textStyle(style -> style
                .textAlignHorizontal(Horizontal.CENTER)
                .textColor(ColorPattern.GRAY.color)
                .textWrap(TextWrap.HOVER_ROLL));
    }

    private UIElement createGenericCreateProcessingCanvas() {
        return CreateProcessingCanvasFactory.createGenericProcessingCanvas(
                createCreateInputSide(),
                createCreateMachineColumn(),
                createCreateOutputSide()
        );
    }

    private UIElement createCreateSpoutCanvas() {
        configureIngredientSlot(createSpoutIngredientSlot, 0);
        configureCreateFluidInputSlot(createSpoutFluidInputSlot, 0);
        configureCreateOutputSlot(createSpoutOutputSlot, 0);
        return CreateProcessingCanvasFactory.createSpoutCanvas(
                createSpoutFluidInputSlot,
                createSpoutIngredientSlot,
                createSpoutOutputSlot
        );
    }

    private UIElement createCreateDrainCanvas() {
        configureIngredientSlot(createDrainIngredientSlot, 0);
        configureCreateFluidOutputSlot(createDrainFluidOutputSlot, 0);
        configureCreateOutputSlot(createDrainOutputSlot, 0);
        return CreateProcessingCanvasFactory.createDrainCanvas(
                createDrainIngredientSlot,
                createDrainFluidOutputSlot,
                createDrainOutputSlot
        );
    }

    private UIElement createCreateFramedSlot(UIElement slot, int size) {
        return CreateProcessingCanvasFactory.framedSlot(slot, size);
    }

    private UIElement createCreateFanCanvas() {
        configureIngredientSlot(createFanIngredientSlot, 0);
        configureCreateOutputSlot(createFanSingleOutputSlot, 0);
        createFanCatalystLabel.textStyle(style -> style
                .textAlignHorizontal(Horizontal.CENTER)
                .textColor(ColorPattern.WHITE.color)
                .textWrap(TextWrap.HOVER_ROLL));
        createFanCatalystLabel.layout(layout -> {
            layout.widthPercent(100);
            layout.height(18);
        });
        var canvas = CreateProcessingCanvasFactory.createFanCanvas(
                createFanIngredientSlot,
                createFanCatalystIcon,
                createFanCatalystLabel,
                createFanSingleOutputSlot,
                createCreateFanOutputGrid()
        );
        createFanSingleOutputPanel = canvas.singleOutputPanel();
        createFanMultiOutputPanel = canvas.multiOutputPanel();
        return canvas.root();
    }

    private UIElement createCreateFanOutputGrid() {
        return RecipeGridFactory.borderedGrid(3, 4, SLOT_SIZE, true, null, (index, row, col) -> {
            var slot = createEditorSlot(SLOT_SIZE);
            configureCreateOutputSlot(slot, index);
            createFanOutputSlots[index] = slot;
            var cell = RecipeGridFactory.slotCell(slot, SLOT_SIZE);
            createFanOutputSlotCells[index] = cell;
            return cell;
        });
    }

    private UIElement createCreateCrushingCanvas() {
        configureIngredientSlot(createCrushingIngredientSlot, 0);
        return CreateProcessingCanvasFactory.createCrushingCanvas(
                createCrushingIngredientSlot,
                createCreateCrushingOutputRow()
        );
    }

    private UIElement createCreateCrushingOutputRow() {
        return RecipeGridFactory.borderedRow(createCrushingOutputSlots.length, SLOT_SIZE, i -> {
            var slot = createEditorSlot(SLOT_SIZE);
            configureCreateOutputSlot(slot, i);
            createCrushingOutputSlots[i] = slot;
            var cell = RecipeGridFactory.slotCell(slot, SLOT_SIZE);
            createCrushingOutputSlotCells[i] = cell;
            return cell;
        });
    }

    private UIElement createCreateMillingCanvas() {
        configureIngredientSlot(createMillingIngredientSlot, 0);
        return CreateProcessingCanvasFactory.createMillingCanvas(
                createMillingIngredientSlot,
                createCreateMillingOutputRow()
        );
    }

    private UIElement createCreateMillingOutputRow() {
        return RecipeGridFactory.borderedRow(createMillingOutputSlots.length, SLOT_SIZE, i -> {
            var slot = createEditorSlot(SLOT_SIZE);
            configureCreateOutputSlot(slot, i);
            createMillingOutputSlots[i] = slot;
            var cell = RecipeGridFactory.slotCell(slot, SLOT_SIZE);
            createMillingOutputSlotCells[i] = cell;
            return cell;
        });
    }

    private UIElement createCreateSawCanvas() {
        configureIngredientSlot(createSawIngredientSlot, 0);
        createSawOutputGrid = createCreateSawOutputGrid();
        createBlockCuttingOutputGrid = createCreateBlockCuttingOutputGrid();
        return CreateProcessingCanvasFactory.createSawCanvas(
                createSawIngredientSlot,
                createSawOutputGrid,
                createBlockCuttingOutputGrid
        );
    }

    private UIElement createCreateSawOutputGrid() {
        return RecipeGridFactory.borderedGrid(2, 2, SLOT_SIZE, true, createSawOutputRows, (index, row, col) -> {
            var slot = createEditorSlot(SLOT_SIZE);
            configureCreateOutputSlot(slot, index);
            createSawOutputSlots[index] = slot;
            var cell = RecipeGridFactory.slotCell(slot, SLOT_SIZE);
            createSawOutputSlotCells[index] = cell;
            return cell;
        });
    }

    private UIElement createCreateBlockCuttingOutputGrid() {
        return RecipeGridFactory.borderedGrid(
                BLOCK_CUTTING_OUTPUT_COLUMNS,
                BLOCK_CUTTING_OUTPUT_ROWS,
                SLOT_SIZE,
                true,
                createBlockCuttingOutputRows,
                (index, row, col) -> {
                    var slot = createEditorSlot(SLOT_SIZE);
                    configureCreateOutputSlot(slot, index);
                    createBlockCuttingOutputSlots[index] = slot;
                    var cell = RecipeGridFactory.slotCell(slot, SLOT_SIZE);
                    createBlockCuttingOutputSlotCells[index] = cell;
                    return cell;
                }
        );
    }

    private UIElement createCreateAutoPackingCanvas() {
        configureCreateOutputSlot(createAutoPackingOutputSlot, 0);
        return CreateProcessingCanvasFactory.createAutoPackingCanvas(
                createCreateAutoPackingInputGrid(),
                createAutoPackingOutputSlot
        );
    }

    private UIElement createCreateSandpaperCanvas() {
        configureIngredientSlot(sandpaperIngredientSlot, 0);
        configureCreateOutputSlot(sandpaperOutputSlot, 0);
        return CreateProcessingCanvasFactory.createSandpaperCanvas(
                sandpaperIngredientSlot,
                sandpaperOutputSlot
        );
    }

    private UIElement createCreateAutoPackingInputGrid() {
        createAutoPackingInputGrid = RecipeGridFactory.borderedGrid(
                3,
                3,
                SLOT_SIZE,
                true,
                createAutoPackingIngredientRows,
                (index, row, col) -> {
                    var slot = createIngredientSlot(SLOT_SIZE);
                    configureAutoPackingIngredientSlot(slot);
                    createAutoPackingIngredientSlots[index] = slot;
                    var cell = RecipeGridFactory.slotCell(slot, SLOT_SIZE);
                    createAutoPackingIngredientSlotCells[index] = cell;
                    return cell;
                }
        ).style(style -> style.tooltips(Component.translatable("viscript_recipe.editor.create.auto_packing.input_grid")));
        createAutoPackingInputGrid.addEventListener(UIEvents.MOUSE_DOWN, event -> controller.selectIngredientSlot(0));
        return createAutoPackingInputGrid;
    }

    private UIElement createCreatePressBasinCanvas() {
        createPressHeatLabel.textStyle(style -> style
                .fontSize(16)
                .textColor(ColorPattern.WHITE.color)
                .textAlignHorizontal(Horizontal.CENTER)
                .textWrap(TextWrap.HOVER_ROLL));
        createPressHeatLabel.layout(layout -> {
            layout.widthPercent(100);
            layout.height(24);
        });
        createPressHeatPanel = CreateProcessingCanvasFactory.createPressHeatPanel(createPressHeatLabel);
        return CreateProcessingCanvasFactory.createPressBasinCanvas(
                createCreatePressInputSide(),
                createCreatePressMachineStack(),
                createCreatePressOutputSide(),
                createPressHeatPanel
        );
    }

    private UIElement createCreateAutomaticBrewingCanvas() {
        configureIngredientSlot(createAutomaticBrewingIngredientSlot, 0);
        configureCreateFluidInputSlot(createAutomaticBrewingFluidInputSlot, 0);
        configureCreateFluidOutputSlot(createAutomaticBrewingFluidOutputSlot, 0);
        createAutomaticBrewingHeatLabel.textStyle(style -> style
                .fontSize(16)
                .textColor(ColorPattern.WHITE.color)
                .textAlignHorizontal(Horizontal.CENTER)
                .textWrap(TextWrap.HOVER_ROLL));
        createAutomaticBrewingHeatLabel.layout(layout -> {
            layout.widthPercent(100);
            layout.height(24);
        });
        createAutomaticBrewingHeatPanel = CreateProcessingCanvasFactory.createPressHeatPanel(createAutomaticBrewingHeatLabel);
        return CreateProcessingCanvasFactory.createAutomaticBrewingCanvas(
                createAutomaticBrewingIngredientSlot,
                createAutomaticBrewingFluidInputSlot,
                createAutomaticBrewingFluidOutputSlot,
                createAutomaticBrewingHeatPanel
        );
    }

    private UIElement createCreatePressInputSide() {
        return CreateProcessingCanvasFactory.createPressInputSide(
                createCreatePressItemInputGrid(),
                createCreatePressFluidInputs()
        );
    }

    private UIElement createCreatePressItemInputGrid() {
        return RecipeGridFactory.borderedGrid(3, 3, SLOT_SIZE, false, createPressIngredientRows, (index, row, col) -> {
            var slot = createIngredientSlot(SLOT_SIZE);
            configureIngredientSlot(slot, index);
            createPressIngredientSlots[index] = slot;
            var cell = RecipeGridFactory.slotCell(slot, SLOT_SIZE);
            createPressIngredientSlotCells[index] = cell;
            return cell;
        });
    }

    private UIElement createCreatePressFluidInputs() {
        var row = RecipeEditorUi.row().layout(layout -> {
            layout.widthPercent(100);
            layout.height(40);
            layout.gapAll(6);
            layout.alignItems(AlignItems.CENTER);
            layout.justifyContent(AlignContent.CENTER);
        });
        createPressFluidInputRow = row;
        for (int i = 0; i < createPressFluidInputSlots.length; i++) {
            var slot = createFluidDisplaySlot();
            configureCreateFluidInputSlot(slot, i);
            createPressFluidInputSlots[i] = slot;
            var column = RecipeEditorUi.column().layout(layout -> {
                layout.width(46);
                layout.gapAll(2);
                layout.alignItems(AlignItems.CENTER);
            }).addChildren(
                    RecipeEditorUi.label(Component.translatable("viscript_recipe.editor.create.fluid_input_short")),
                    slot
            );
            createPressFluidInputColumns[i] = column;
            row.addChild(column);
        }
        return row;
    }

    private UIElement createCreatePressMachineStack() {
        return CreateProcessingCanvasFactory.createPressMachineStack(createBasinMachineIcon);
    }

    private UIElement createCreatePressOutputSide() {
        return CreateProcessingCanvasFactory.createPressOutputSide(
                createCreatePressItemOutputs(),
                createCreatePressFluidOutputs()
        );
    }

    private UIElement createCreatePressItemOutputs() {
        return RecipeGridFactory.borderedGrid(2, 2, SLOT_SIZE, true, createPressOutputRows, (index, row, col) -> {
            var slot = createEditorSlot(SLOT_SIZE);
            configureCreateOutputSlot(slot, index);
            createPressOutputSlots[index] = slot;
            var cell = RecipeGridFactory.slotCell(slot, SLOT_SIZE);
            createPressOutputSlotCells[index] = cell;
            return cell;
        });
    }

    private UIElement createCreatePressFluidOutputs() {
        var row = RecipeEditorUi.row().layout(layout -> {
            layout.widthPercent(100);
            layout.height(40);
            layout.gapAll(6);
            layout.alignItems(AlignItems.CENTER);
            layout.justifyContent(AlignContent.CENTER);
        });
        createPressFluidOutputRow = row;
        for (int i = 0; i < createPressFluidOutputSlots.length; i++) {
            var slot = createFluidSlot();
            configureCreateFluidOutputSlot(slot, i);
            createPressFluidOutputSlots[i] = slot;
            var column = RecipeEditorUi.column().layout(layout -> {
                layout.width(52);
                layout.gapAll(2);
                layout.alignItems(AlignItems.CENTER);
            }).addChildren(
                    RecipeEditorUi.label(Component.translatable("viscript_recipe.editor.create.fluid_output_short")),
                    slot
            );
            createPressFluidOutputColumns[i] = column;
            row.addChild(column);
        }
        return row;
    }

    private UIElement createCreatePressingCanvas() {
        configureIngredientSlot(createPressingIngredientSlot, 0);
        return CreateProcessingCanvasFactory.createPressingCanvas(
                createPressingIngredientSlot,
                createCreatePressingOutputRow()
        );
    }

    private UIElement createCreatePressingOutputRow() {
        return RecipeGridFactory.borderedRow(createPressingOutputSlots.length, SLOT_SIZE, i -> {
            var slot = createEditorSlot(SLOT_SIZE);
            configureCreateOutputSlot(slot, i);
            createPressingOutputSlots[i] = slot;
            var cell = RecipeGridFactory.slotCell(slot, SLOT_SIZE);
            createPressingOutputSlotCells[i] = cell;
            return cell;
        });
    }

    private UIElement createCreateDeployerCanvas() {
        configureIngredientSlot(createDeployerProcessedSlot, 0);
        configureIngredientSlot(createDeployerHeldSlot, 1);
        return CreateProcessingCanvasFactory.createDeployerCanvas(
                createDeployerHeldSlot,
                createDeployerProcessedSlot,
                createCreateDeployerOutputGrid()
        );
    }

    private UIElement createCreateDeployerOutputGrid() {
        return RecipeGridFactory.borderedGrid(2, 2, SLOT_SIZE, true, createDeployerOutputRows, (index, row, col) -> {
            var slot = createEditorSlot(SLOT_SIZE);
            configureCreateOutputSlot(slot, index);
            createDeployerOutputSlots[index] = slot;
            var cell = RecipeGridFactory.slotCell(slot, SLOT_SIZE);
            createDeployerOutputSlotCells[index] = cell;
            return cell;
        });
    }

    private UIElement createCreateManualApplicationCanvas() {
        configureIngredientSlot(createManualApplicationBlockSlot, 0);
        configureIngredientSlot(createManualApplicationHeldSlot, 1);
        return CreateProcessingCanvasFactory.createManualApplicationCanvas(
                createManualApplicationBlockSlot,
                createCreateManualApplicationProcessStack(),
                createCreateManualApplicationOutputGrid()
        );
    }

    private UIElement createCreateManualApplicationProcessStack() {
        createManualApplicationBlockScene
                .createScene(createManualApplicationPreviewWorld)
                .setRenderFacing(false)
                .setRenderSelect(false)
                .setDraggable(false)
                .setScalable(false)
                .setIntractable(false)
                .setTickWorld(false)
                .useOrtho()
                .useCacheBuffer()
                .setClipContext(ClipContext.Block.VISUAL, ClipContext.Fluid.NONE)
                .setRenderedCore(List.of(BlockPos.ZERO))
                .setZoom(1f)
                .layout(layout -> {
                    layout.widthPercent(100);
                    layout.maxWidth(174);
                    layout.minWidth(0);
                    layout.height(136);
                });
        return RecipeEditorUi.column().layout(layout -> {
            layout.widthPercent(100);
            layout.maxWidth(190);
            layout.minWidth(0);
            layout.height(184);
            layout.gapAll(0);
            layout.alignItems(AlignItems.CENTER);
            layout.justifyContent(AlignContent.CENTER);
        }).addChildren(
                createManualApplicationHeldSlot,
                createDownArrowElement(36, 24),
                createManualApplicationBlockScene
        );
    }

    private UIElement createCreateManualApplicationOutputGrid() {
        return RecipeGridFactory.borderedGrid(2, 2, SLOT_SIZE, true, createManualApplicationOutputRows, (index, row, col) -> {
            var slot = createEditorSlot(SLOT_SIZE);
            configureCreateOutputSlot(slot, index);
            createManualApplicationOutputSlots[index] = slot;
            var cell = RecipeGridFactory.slotCell(slot, SLOT_SIZE);
            createManualApplicationOutputSlotCells[index] = cell;
            return cell;
        });
    }

    private UIElement createCreateInputSide() {
        return CreateProcessingCanvasFactory.createInputSide(
                createCreateItemInputGrid(),
                createCreateFluidInputs()
        );
    }

    private UIElement createCreateItemInputGrid() {
        return RecipeGridFactory.borderedGrid(3, 3, SLOT_SIZE, (index, row, col) -> {
            var slot = createIngredientSlot(SLOT_SIZE);
            configureIngredientSlot(slot, index);
            createIngredientSlots[index] = slot;
            var cell = RecipeGridFactory.slotCell(slot, SLOT_SIZE);
            createIngredientSlotCells[index] = cell;
            return cell;
        });
    }

    private UIElement createCreateFluidInputs() {
        var row = RecipeEditorUi.row().layout(layout -> {
            layout.widthPercent(100);
            layout.height(42);
            layout.gapAll(6);
            layout.alignItems(AlignItems.CENTER);
            layout.justifyContent(AlignContent.CENTER);
        });
        for (int i = 0; i < createFluidInputSlots.length; i++) {
            var slot = createFluidDisplaySlot();
            configureCreateFluidInputSlot(slot, i);
            createFluidInputSlots[i] = slot;
            var column = RecipeEditorUi.column().layout(layout -> {
                layout.width(46);
                layout.gapAll(2);
                layout.alignItems(AlignItems.CENTER);
            }).addChildren(
                    RecipeEditorUi.label(Component.translatable("viscript_recipe.editor.create.fluid_input_short")),
                    slot
            );
            createFluidInputColumns[i] = column;
            row.addChild(column);
        }
        return row;
    }

    private UIElement createCreateMachineColumn() {
        createMachineIcon.layout(layout -> {
            layout.width(36);
            layout.height(36);
        });
        createMachineLabel.textStyle(style -> style
                .textAlignHorizontal(Horizontal.CENTER)
                .textColor(ColorPattern.WHITE.color)
                .textWrap(TextWrap.HOVER_ROLL));
        createMachineLabel.layout(layout -> {
            layout.width(86);
            layout.height(18);
        });
        return CreateProcessingCanvasFactory.createMachineColumn(createMachineIcon, createMachineLabel);
    }

    private UIElement createCreateOutputSide() {
        return CreateProcessingCanvasFactory.createOutputSide(
                createCreateItemOutputGrid(),
                createCreateFluidOutputs()
        );
    }

    private UIElement createCreateItemOutputGrid() {
        return RecipeGridFactory.borderedGrid(4, 3, SLOT_SIZE, (index, row, col) -> {
            var slot = createEditorSlot(SLOT_SIZE);
            configureCreateOutputSlot(slot, index);
            createOutputSlots[index] = slot;
            var cell = RecipeGridFactory.slotCell(slot, SLOT_SIZE);
            createOutputSlotCells[index] = cell;
            return cell;
        });
    }

    private UIElement createCreateFluidOutputs() {
        var row = RecipeEditorUi.row().layout(layout -> {
            layout.widthPercent(100);
            layout.height(42);
            layout.gapAll(6);
            layout.alignItems(AlignItems.CENTER);
            layout.justifyContent(AlignContent.CENTER);
        });
        for (int i = 0; i < createFluidOutputSlots.length; i++) {
            var slot = createFluidSlot();
            configureCreateFluidOutputSlot(slot, i);
            createFluidOutputSlots[i] = slot;
            var column = RecipeEditorUi.column().layout(layout -> {
                layout.width(52);
                layout.gapAll(2);
                layout.alignItems(AlignItems.CENTER);
            }).addChildren(
                    RecipeEditorUi.label(Component.translatable("viscript_recipe.editor.create.fluid_output_short")),
                    slot
            );
            createFluidOutputColumns[i] = column;
            row.addChild(column);
        }
        return row;
    }

    private UIElement createDragonBreathColumn() {
        dragonForgeBreathValueLabel.textStyle(style -> style
                .textColor(ColorPattern.WHITE.color)
                .textAlignHorizontal(Horizontal.CENTER)
                .textWrap(TextWrap.HOVER_ROLL));
        dragonForgeBreathValueLabel.layout(layout -> {
            layout.widthPercent(100);
            layout.height(24);
        });
        var panel = RecipeEditorUi.column().layout(layout -> {
            layout.width(76);
            layout.height(64);
            layout.paddingAll(5);
            layout.gapAll(4);
            layout.alignItems(AlignItems.CENTER);
            layout.justifyContent(AlignContent.CENTER);
        }).style(style -> style
                .backgroundTexture(Sprites.BORDER_DARK)
                .tooltips(Component.translatable("viscript_recipe.editor.dragon_forge.breath_tip"))
        ).addChildren(
                RecipeEditorUi.label(Component.translatable("viscript_recipe.editor.dragon_forge.breath"))
                        .textStyle(style -> style.textAlignHorizontal(Horizontal.CENTER).textColor(ColorPattern.LIGHT_GRAY.color))
                        .layout(layout -> layout.widthPercent(100).height(16)),
                dragonForgeBreathValueLabel
        );
        panel.addEventListener(UIEvents.MOUSE_DOWN, event -> controller.selectRecipeProperties());
        return panel;
    }

    private UIElement createDragonForgeInputColumn() {
        return RecipeEditorUi.column().layout(layout -> {
            layout.width(108);
            layout.height(72);
            layout.paddingAll(6);
            layout.gapAll(5);
            layout.alignItems(AlignItems.CENTER);
        }).style(style -> style.backgroundTexture(Sprites.BORDER_DARK))
                .addChildren(
                        RecipeEditorUi.label(Component.translatable("viscript_recipe.editor.dragon_forge.forge_inputs"))
                                .textStyle(style -> style.textAlignHorizontal(Horizontal.CENTER).textColor(ColorPattern.LIGHT_GRAY.color))
                                .layout(layout -> layout.widthPercent(100).height(16)),
                        RecipeEditorUi.row().layout(layout -> {
                            layout.widthPercent(100);
                            layout.height(SLOT_SIZE);
                            layout.gapAll(6);
                            layout.alignItems(AlignItems.CENTER);
                            layout.justifyContent(AlignContent.CENTER);
                        }).addChildren(
                                createDragonForgeInput("viscript_recipe.config.iceandfire.dragon_forge.input", 0),
                                createDragonForgeInput("viscript_recipe.config.iceandfire.dragon_forge.blood", 1)
                        )
                );
    }

    private UIElement createDragonForgeInput(String labelKey, int index) {
        var slot = createIngredientSlot(SLOT_SIZE);
        configureIngredientSlot(slot, index);
        dragonForgeIngredientSlots[index] = slot;
        return RecipeEditorUi.column().layout(layout -> {
            layout.width(42);
            layout.gapAll(2);
            layout.alignItems(AlignItems.CENTER);
        }).addChildren(
                RecipeEditorUi.label(Component.translatable(labelKey))
                        .textStyle(style -> style.textAlignHorizontal(Horizontal.CENTER).textColor(ColorPattern.LIGHT_GRAY.color))
                        .layout(layout -> layout.widthPercent(100).height(12)),
                slot
        );
    }

    private UIElement createFluidColumn(Label label, FluidSlot slot) {
        return RecipeEditorUi.column().layout(layout -> {
            layout.width(72);
            layout.gapAll(4);
            layout.alignItems(AlignItems.CENTER);
        }).addChildren(
                label,
                slot
        );
    }

    private IngredientDisplaySlot configureIngredientSlot(IngredientDisplaySlot slot, int index) {
        ingredientDragSlotIndices.put(slot, index);
        slot.registerValueListener(stack -> {
            if (!controller.isRefreshing()) {
                controller.setVisualIngredient(index, stack);
            }
        });
        slot.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            controller.selectIngredientSlot(index);
            if (event.button == 1) {
                controller.clearVisualIngredient(index);
                event.stopPropagation();
            }
        });
        slot.style(style -> style.tooltips(Component.translatable(
                "viscript_recipe.editor.ingredient_slot",
                index + 1
        )));
        return slot;
    }

    private ItemSlot configureResultSlot(ItemSlot slot) {
        slot.registerValueListener(stack -> {
            if (!controller.isRefreshing()) {
                controller.setVisualResult(stack);
            }
        });
        slot.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            controller.selectResultSlot();
            if (event.button == 1) {
                controller.clearVisualResult();
                event.stopPropagation();
            }
        });
        slot.style(style -> style.tooltips(Component.translatable("viscript_recipe.editor.result_slot")));
        return slot;
    }

    private ItemSlot configureContainerSlot(ItemSlot slot) {
        slot.registerValueListener(stack -> {
            if (!controller.isRefreshing()) {
                controller.setVisualContainer(stack);
            }
        });
        slot.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            controller.selectContainerSlot();
            if (event.button == 1) {
                controller.clearVisualContainer();
                event.stopPropagation();
            }
        });
        slot.style(style -> style.tooltips(Component.translatable("viscript_recipe.editor.farmersdelight.cooking.container_slot")));
        return slot;
    }

    private ItemSlot configureCuttingResultSlot(ItemSlot slot, int index) {
        slot.registerValueListener(stack -> {
            if (!controller.isRefreshing()) {
                controller.setVisualCuttingResult(index, stack);
            }
        });
        slot.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            controller.selectCuttingResultSlot(index);
            if (event.button == 1) {
                controller.clearVisualCuttingResult(index);
                event.stopPropagation();
            }
        });
        slot.style(style -> style.tooltips(Component.translatable(
                "viscript_recipe.editor.farmersdelight.cutting.result_slot",
                index + 1
        )));
        return slot;
    }

    private ItemSlot configureCreateOutputSlot(ItemSlot slot, int index) {
        slot.registerValueListener(stack -> {
            if (!controller.isRefreshing()) {
                controller.setVisualCreateOutput(index, stack);
            }
        });
        slot.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            controller.selectCreateOutputSlot(index);
            if (event.button == 1) {
                controller.clearVisualCreateOutput(index);
                event.stopPropagation();
            }
        });
        slot.style(style -> style.tooltips(Component.translatable(
                "viscript_recipe.editor.create.output_slot",
                index + 1
        )));
        return slot;
    }

    private ItemSlot configureArsNouveauOutputSlot(ItemSlot slot, int index) {
        slot.registerValueListener(stack -> {
            if (!controller.isRefreshing()) {
                controller.setVisualArsNouveauOutput(index, stack);
            }
        });
        slot.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            controller.selectArsNouveauOutputSlot(index);
            if (event.button == 1) {
                controller.clearVisualArsNouveauOutput(index);
                event.stopPropagation();
            }
        });
        slot.style(style -> style.tooltips(Component.translatable(
                "viscript_recipe.editor.ars_nouveau.output_slot",
                index + 1
        )));
        return slot;
    }

    private IngredientDisplaySlot configureAutoPackingIngredientSlot(IngredientDisplaySlot slot) {
        ingredientDragSlotIndices.put(slot, 0);
        slot.registerValueListener(stack -> {
            if (!controller.isRefreshing()) {
                controller.setVisualIngredient(0, stack);
            }
        });
        slot.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            controller.selectIngredientSlot(0);
            if (event.button == 1) {
                controller.clearVisualIngredient(0);
                event.stopPropagation();
            }
        });
        slot.style(style -> style.tooltips(Component.translatable("viscript_recipe.editor.create.auto_packing.input_grid")));
        return slot;
    }

    private FluidSlot configureFluidSlot(FluidSlot slot, int index) {
        slot.registerValueListener(stack -> {
            if (!controller.isRefreshing()) {
                controller.setVisualAlchemistFluid(index, stack);
            }
        });
        slot.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            controller.selectAlchemistFluidSlot(index);
            if (event.button == 1) {
                controller.clearVisualAlchemistFluid(index);
                event.stopPropagation();
            }
        });
        slot.style(style -> style.tooltips(Component.translatable("viscript_recipe.editor.fluid_slot")));
        return slot;
    }

    private FluidDisplaySlot configureCreateFluidInputSlot(FluidDisplaySlot slot, int index) {
        slot.registerValueListener(stack -> {
            if (!controller.isRefreshing()) {
                controller.setVisualCreateFluidInput(index, com.viscript_recipe.data.create.CreateFluidIngredientData.fluid(stack));
            }
        });
        slot.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            controller.selectCreateFluidInputSlot(index);
            if (event.button == 1) {
                controller.clearVisualCreateFluidInput(index);
                event.stopPropagation();
            }
        });
        slot.style(style -> style.tooltips(Component.translatable(
                "viscript_recipe.editor.create.fluid_input_slot",
                index + 1
        )));
        return slot;
    }

    private FluidSlot configureCreateFluidOutputSlot(FluidSlot slot, int index) {
        slot.registerValueListener(stack -> {
            if (!controller.isRefreshing()) {
                controller.setVisualCreateFluidOutput(index, stack);
            }
        });
        slot.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            controller.selectCreateFluidOutputSlot(index);
            if (event.button == 1) {
                controller.clearVisualCreateFluidOutput(index);
                event.stopPropagation();
            }
        });
        slot.style(style -> style.tooltips(Component.translatable(
                "viscript_recipe.editor.create.fluid_output_slot",
                index + 1
        )));
        return slot;
    }

    private ItemSlot configureCreateSequencedTransitionalSlot(ItemSlot slot) {
        slot.registerValueListener(stack -> {
            if (!controller.isRefreshing()) {
                controller.setVisualCreateSequencedTransitional(stack);
            }
        });
        slot.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            controller.selectCreateSequencedTransitionalSlot();
            if (event.button == 1) {
                controller.clearVisualCreateSequencedTransitional();
                event.stopPropagation();
            }
        });
        slot.style(style -> style.tooltips(Component.translatable("viscript_recipe.editor.create.sequenced_assembly.transitional_item_slot")));
        return slot;
    }

    private FluidDisplaySlot configureCreateSequencedStepFluidSlot(FluidDisplaySlot slot, int index) {
        slot.registerValueListener(stack -> {
            if (!controller.isRefreshing()) {
                controller.setCreateSequencedStepFluidIngredient(index, com.viscript_recipe.data.create.CreateFluidIngredientData.fluid(stack));
            }
        });
        slot.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            controller.selectCreateFluidInputSlot(index);
            if (event.button == 1) {
                controller.setCreateSequencedStepFluidIngredient(index, com.viscript_recipe.data.create.CreateFluidIngredientData.fluid(FluidStack.EMPTY));
            }
            event.stopPropagation();
        });
        slot.style(style -> style.tooltips(Component.translatable(
                "viscript_recipe.editor.create.sequenced_assembly.step_fluid_slot",
                index + 1
        )));
        return slot;
    }

    private UIElement configureCreateSequencedStepFluidCell(UIElement cell, int index) {
        cell.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            controller.selectCreateFluidInputSlot(index);
            if (event.button == 1) {
                controller.setCreateSequencedStepFluidIngredient(index, com.viscript_recipe.data.create.CreateFluidIngredientData.fluid(FluidStack.EMPTY));
            }
            event.stopPropagation();
        });
        return cell;
    }

    private IngredientDisplaySlot configureCreateSequencedStepIngredientSlot(IngredientDisplaySlot slot, int index) {
        ingredientDragSlotIndices.put(slot, index);
        slot.registerValueListener(stack -> {
            if (!controller.isRefreshing()) {
                controller.setVisualIngredient(index, stack);
            }
        });
        slot.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            controller.selectIngredientSlot(index);
            if (event.button == 1) {
                controller.clearVisualIngredient(index);
            }
            event.stopPropagation();
        });
        slot.style(style -> style.tooltips(Component.translatable(
                "viscript_recipe.editor.create.sequenced_assembly.step_ingredient_slot",
                index - CREATE_SEQUENCED_STEP_INGREDIENT_OFFSET + 1
        )));
        return slot;
    }

    private UIElement configureCreateSequencedStepIngredientCell(UIElement cell, int index) {
        cell.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            controller.selectIngredientSlot(index);
            if (event.button == 1) {
                controller.clearVisualIngredient(index);
            }
            event.stopPropagation();
        });
        return cell;
    }

    private void refresh() {
        titleLabel.setText(controller.selectedCategoryDisplayName());
        var singleInput = controller.isSelectedSingleInputLayout();
        var cooking = controller.isSelectedCookingLayout();
        var campfireCooking = cooking && isSelectedCampfireCooking();
        var smithing = controller.isSelectedSmithingLayout();
        var arcaneAnvil = useJeiArcaneAnvilCanvas && controller.isSelectedArcaneAnvilLayout();
        var alchemist = controller.isSelectedAlchemistCauldronLayout();
        var dragonForge = controller.isSelectedDragonForgeLayout();
        var cataclysmWeaponFusion = controller.isSelectedCataclysmWeaponFusionLayout();
        var cataclysmAmethystBless = controller.isSelectedCataclysmAmethystBlessLayout();
        var cataclysm = cataclysmWeaponFusion || cataclysmAmethystBless;
        var touhouLittleMaidAltar = controller.isSelectedTouhouLittleMaidAltarLayout();
        var sporeSurgery = controller.isSelectedSporeSurgeryLayout();
        var sporeGrafting = controller.isSelectedSporeGraftingLayout();
        var spore = sporeSurgery || sporeGrafting;
        var goetyCursedInfuser = controller.isSelectedGoetyCursedInfuserLayout();
        var goetyRitual = controller.isSelectedGoetyRitualLayout();
        var goetyBrazier = controller.isSelectedGoetyBrazierLayout();
        var goetyPulverize = controller.isSelectedGoetyPulverizeLayout();
        var goetyBrewing = controller.isSelectedGoetyBrewingLayout();
        var goety = goetyCursedInfuser || goetyRitual || goetyBrazier || goetyPulverize || goetyBrewing;
        var farmersCookingPot = controller.isSelectedFarmersCookingPotLayout();
        var farmersCuttingBoard = controller.isSelectedFarmersCuttingBoardLayout();
        var mechanicalCrafting = controller.isSelectedCreateMechanicalCraftingLayout();
        var largeCraftingGrid = controller.isSelectedLargeCraftingGridLayout();
        var avaritiaCompressor = controller.isSelectedAvaritiaCompressorLayout();
        var avaritiaExtremeSmithing = controller.isSelectedAvaritiaExtremeSmithingLayout();
        var extendedCombination = controller.isSelectedExtendedCraftingCombinationLayout();
        var extendedCompressor = controller.isSelectedExtendedCraftingCompressorLayout();
        var extendedFlux = controller.isSelectedExtendedCraftingFluxCrafterLayout();
        var createProcessing = controller.isSelectedCreateProcessingLayout();
        var createSequencedAssembly = controller.isSelectedCreateSequencedAssemblyLayout();
        var arsNouveauApparatus = controller.isSelectedArsNouveauApparatusLayout();
        var arsNouveauImbuement = controller.isSelectedArsNouveauImbuementLayout();
        var arsNouveauGlyph = controller.isSelectedArsNouveauGlyphLayout();
        var arsNouveauCrush = controller.isSelectedArsNouveauCrushLayout();
        var kaleidoscopePot = controller.isSelectedKaleidoscopePotLayout();
        var kaleidoscopeStockpot = controller.isSelectedKaleidoscopeStockpotLayout();
        var kaleidoscopeMillstone = controller.isSelectedKaleidoscopeMillstoneLayout();
        var kaleidoscopeChoppingBoard = controller.isSelectedKaleidoscopeChoppingBoardLayout();
        var kaleidoscopeSteamer = controller.isSelectedKaleidoscopeSteamerLayout();
        var kaleidoscopeTeapot = controller.isSelectedKaleidoscopeTeapotLayout();
        var kaleidoscope = kaleidoscopePot || kaleidoscopeStockpot || kaleidoscopeMillstone || kaleidoscopeChoppingBoard || kaleidoscopeSteamer || kaleidoscopeTeapot;
        if (craftingCanvas != null) {
            craftingCanvas.setDisplay(!singleInput && !smithing && !alchemist && !dragonForge && !cataclysm && !touhouLittleMaidAltar && !spore && !goety && !farmersCookingPot && !farmersCuttingBoard && !largeCraftingGrid && !avaritiaCompressor && !avaritiaExtremeSmithing && !extendedCombination && !extendedCompressor && !extendedFlux && !createProcessing && !createSequencedAssembly && !arsNouveauApparatus && !arsNouveauImbuement && !arsNouveauGlyph && !arsNouveauCrush && !kaleidoscope);
        }
        if (mechanicalCraftingCanvas != null) {
            mechanicalCraftingCanvas.setDisplay(largeCraftingGrid);
        }
        if (cookingCanvas != null) {
            cookingCanvas.setDisplay(cooking && !campfireCooking);
        }
        if (campfireCookingCanvas != null) {
            campfireCookingCanvas.setDisplay(campfireCooking);
        }
        if (singleInputCanvas != null) {
            singleInputCanvas.setDisplay(singleInput && !cooking);
        }
        if (farmersCookingPotCanvas != null) {
            farmersCookingPotCanvas.setDisplay(farmersCookingPot);
        }
        if (farmersCuttingBoardCanvas != null) {
            farmersCuttingBoardCanvas.setDisplay(farmersCuttingBoard);
        }
        if (smithingCanvas != null) {
            smithingCanvas.setDisplay(smithing && !arcaneAnvil && !alchemist && !dragonForge && !farmersCookingPot && !farmersCuttingBoard && !largeCraftingGrid && !avaritiaCompressor && !avaritiaExtremeSmithing && !createProcessing);
        }
        if (arcaneAnvilCanvas != null) {
            arcaneAnvilCanvas.setDisplay(smithing && arcaneAnvil);
        }
        if (avaritiaCompressorCanvas != null) {
            avaritiaCompressorCanvas.setDisplay(avaritiaCompressor);
        }
        if (avaritiaExtremeSmithingCanvas != null) {
            avaritiaExtremeSmithingCanvas.setDisplay(avaritiaExtremeSmithing);
        }
        if (extendedCombinationCanvas != null) {
            extendedCombinationCanvas.setDisplay(extendedCombination);
        }
        if (extendedCompressorCanvas != null) {
            extendedCompressorCanvas.setDisplay(extendedCompressor);
        }
        if (extendedFluxCanvas != null) {
            extendedFluxCanvas.setDisplay(extendedFlux);
        }
        if (alchemistCanvas != null) {
            alchemistCanvas.setDisplay(alchemist);
        }
        if (dragonForgeCanvas != null) {
            dragonForgeCanvas.setDisplay(dragonForge);
        }
        if (cataclysmWeaponFusionCanvas != null) {
            cataclysmWeaponFusionCanvas.setDisplay(cataclysmWeaponFusion);
        }
        if (cataclysmAmethystBlessCanvas != null) {
            cataclysmAmethystBlessCanvas.setDisplay(cataclysmAmethystBless);
        }
        if (touhouLittleMaidAltarCanvas != null) {
            touhouLittleMaidAltarCanvas.setDisplay(touhouLittleMaidAltar);
        }
        if (sporeSurgeryCanvas != null) {
            sporeSurgeryCanvas.setDisplay(sporeSurgery);
        }
        if (sporeGraftingCanvas != null) {
            sporeGraftingCanvas.setDisplay(sporeGrafting);
        }
        if (goetyCursedInfuserCanvas != null) {
            goetyCursedInfuserCanvas.setDisplay(goetyCursedInfuser);
        }
        if (goetyRitualCanvas != null) {
            goetyRitualCanvas.setDisplay(goetyRitual);
        }
        if (goetyBrazierCanvas != null) {
            goetyBrazierCanvas.setDisplay(goetyBrazier);
        }
        if (goetyPulverizeCanvas != null) {
            goetyPulverizeCanvas.setDisplay(goetyPulverize);
        }
        if (goetyBrewingCanvas != null) {
            goetyBrewingCanvas.setDisplay(goetyBrewing);
        }
        if (createProcessingCanvas != null) {
            createProcessingCanvas.setDisplay(createProcessing);
        }
        if (createSequencedAssemblyCanvas != null) {
            createSequencedAssemblyCanvas.setDisplay(createSequencedAssembly);
        }
        if (arsNouveauApparatusCanvas != null) {
            arsNouveauApparatusCanvas.setDisplay(arsNouveauApparatus);
        }
        if (arsNouveauImbuementCanvas != null) {
            arsNouveauImbuementCanvas.setDisplay(arsNouveauImbuement);
        }
        if (arsNouveauGlyphCanvas != null) {
            arsNouveauGlyphCanvas.setDisplay(arsNouveauGlyph);
        }
        if (arsNouveauCrushCanvas != null) {
            arsNouveauCrushCanvas.setDisplay(arsNouveauCrush);
        }
        if (kaleidoscopePotCanvas != null) {
            kaleidoscopePotCanvas.setDisplay(kaleidoscopePot);
        }
        if (kaleidoscopeStockpotCanvas != null) {
            kaleidoscopeStockpotCanvas.setDisplay(kaleidoscopeStockpot);
        }
        if (kaleidoscopeMillstoneCanvas != null) {
            kaleidoscopeMillstoneCanvas.setDisplay(kaleidoscopeMillstone);
        }
        if (kaleidoscopeChoppingBoardCanvas != null) {
            kaleidoscopeChoppingBoardCanvas.setDisplay(kaleidoscopeChoppingBoard);
        }
        if (kaleidoscopeSteamerCanvas != null) {
            kaleidoscopeSteamerCanvas.setDisplay(kaleidoscopeSteamer);
        }
        if (kaleidoscopeTeapotCanvas != null) {
            kaleidoscopeTeapotCanvas.setDisplay(kaleidoscopeTeapot);
        }
        if (alchemist) {
            var selectedEntry = controller.getSelectedEntry();
            var isFill = selectedEntry != null && controller.isIronAlchemistCauldronFillEntry(selectedEntry);
            var isBrew = selectedEntry != null && controller.isIronAlchemistCauldronBrewEntry(selectedEntry);
            alchemistInputPlusLabel.setDisplay(!isFill);
            alchemistInputArrow.setDisplay(isFill);
            alchemistOutputArrow.setDisplay(!isFill);
            alchemistOutputPlusLabel.setDisplay(isFill);
            if (alchemistResultFluidColumn != null) {
                alchemistResultFluidColumn.setDisplay(isBrew);
            }
            if (alchemistOutputItemColumn != null) {
                alchemistOutputItemColumn.setDisplay(!isBrew);
            }
            refreshIngredientSlot(alchemistIngredientSlot, 0);
            alchemistMiddleFluidLabel.setText(controller.alchemistMiddleFluidLabel());
            alchemistOutputFluidLabel.setText(Component.translatable("viscript_recipe.editor.alchemist_cauldron.result_fluid"));
            alchemistOutputLabel.setText(controller.alchemistResultLabel());
            var middleFluid = controller.getVisualAlchemistMiddleFluid();
            var resultFluid = controller.getVisualAlchemistResultFluid();
            if (useJeiAlchemistCanvas) {
                alchemistMiddleFluidSlot.setCapacity(Math.max(1, middleFluid.getAmount()));
                alchemistResultFluidSlot.setCapacity(Math.max(1, resultFluid.getAmount()));
            }
            setFluid(alchemistMiddleFluidSlot, middleFluid);
            setFluid(alchemistResultFluidSlot, resultFluid);
            alchemistResultFluidSlot.setDisplay(isBrew);
            alchemistOutputSlot.setDisplay(!isBrew);
            refreshAlchemistChance(isBrew);
            setSlot(alchemistOutputSlot, controller.getVisualResult());
        } else if (avaritiaCompressor) {
            refreshIngredientSlot(avaritiaCompressorIngredientSlot, 0);
            setSlot(avaritiaCompressorOutputSlot, controller.getVisualResult());
        } else if (avaritiaExtremeSmithing) {
            for (int i = 0; i < avaritiaSmithingIngredientSlots.length; i++) {
                refreshIngredientSlot(avaritiaSmithingIngredientSlots[i], i);
            }
            setSlot(avaritiaSmithingOutputSlot, controller.getVisualResult());
        } else if (extendedCombination) {
            refreshExtendedCombination();
        } else if (extendedCompressor) {
            refreshExtendedCompressor();
        } else if (extendedFlux) {
            refreshExtendedFlux();
        } else if (smithing) {
            if (arcaneAnvil) {
                for (int i = 0; i < arcaneAnvilIngredientSlots.length; i++) {
                    refreshIngredientSlot(arcaneAnvilIngredientSlots[i], i);
                }
                setSlot(arcaneAnvilOutputSlot, controller.getVisualResult());
            } else {
                updateSmithingInputLabels();
                var inputCount = controller.selectedSmithingInputCount();
                for (int i = 0; i < smithingIngredientSlots.length; i++) {
                    if (smithingInputColumns[i] != null) {
                        smithingInputColumns[i].setDisplay(i < inputCount);
                    }
                    if (i < inputCount) {
                        refreshIngredientSlot(smithingIngredientSlots[i], i);
                    }
                }
                setSlot(smithingOutputSlot, controller.getVisualResult());
            }
        } else if (farmersCookingPot) {
            for (int i = 0; i < farmerCookingIngredientSlots.length; i++) {
                refreshIngredientSlot(farmerCookingIngredientSlots[i], i);
            }
            setSlot(farmerCookingContainerSlot, controller.getVisualContainer());
            setSlot(farmerCookingPotPreviewSlot, controller.getVisualResult());
            setSlot(farmerCookingOutputSlot, controller.getVisualResult());
            refreshFarmerCookingInfo();
        } else if (farmersCuttingBoard) {
            for (int i = 0; i < farmerCuttingIngredientSlots.length; i++) {
                refreshIngredientSlot(farmerCuttingIngredientSlots[i], i);
            }
            var visibleResultCount = 1;
            var chanceResults = new boolean[farmerCuttingResultSlots.length];
            for (int i = 0; i < farmerCuttingResultSlots.length; i++) {
                var result = controller.getVisualCuttingResult(i);
                setSlot(farmerCuttingResultSlots[i], result);
                if (!result.isEmpty()) {
                    visibleResultCount = i + 1;
                    chanceResults[i] = controller.getVisualCuttingChance(i) < 1;
                }
            }
            if (useJeiFarmersCuttingBoardCanvas) {
                FarmersDelightCanvasFactory.updateJeiCuttingResultCells(
                        farmerCuttingResultCells,
                        visibleResultCount,
                        chanceResults
                );
            }
        } else if (dragonForge) {
            for (int i = 0; i < dragonForgeIngredientSlots.length; i++) {
                refreshIngredientSlot(dragonForgeIngredientSlots[i], i);
            }
            if (useJeiDragonForgeCanvas) {
                var selectedEntry = controller.getSelectedEntry();
                var dragonType = selectedEntry == null ? "fire" : controller.getDragonForgeDragonType(selectedEntry);
                DragonForgeCanvasFactory.updateDragonType(dragonForgeJeiTypeLayers, dragonType);
            } else {
                dragonForgeBreathValueLabel.setText(controller.selectedDragonForgeDragonTypeDisplayName());
            }
            setSlot(dragonForgeOutputSlot, controller.getVisualResult());
        } else if (cataclysmWeaponFusion) {
            refreshIngredientSlot(cataclysmWeaponFusionBaseSlot, 0);
            refreshIngredientSlot(cataclysmWeaponFusionAdditionSlot, 1);
            setSlot(cataclysmWeaponFusionResultSlot, controller.getVisualResult());
        } else if (cataclysmAmethystBless) {
            refreshIngredientSlot(cataclysmAmethystBlessIngredientSlot, 0);
            setSlot(cataclysmAmethystBlessResultSlot, controller.getVisualResult());
        } else if (touhouLittleMaidAltar) {
            refreshTouhouLittleMaidAltar();
        } else if (sporeSurgery) {
            for (int i = 0; i < sporeSurgeryIngredientSlots.length; i++) {
                refreshIngredientSlot(sporeSurgeryIngredientSlots[i], i);
            }
            setSlot(sporeSurgeryOutputSlot, controller.getVisualResult());
        } else if (sporeGrafting) {
            for (int i = 0; i < sporeGraftingIngredientSlots.length; i++) {
                refreshIngredientSlot(sporeGraftingIngredientSlots[i], i);
            }
            setSlot(sporeGraftingOutputSlot, controller.getVisualResult());
        } else if (goetyCursedInfuser) {
            refreshGoetyCursedInfuser();
        } else if (goetyRitual) {
            refreshGoetyRitual();
        } else if (goetyBrazier) {
            refreshGoetyBrazier();
        } else if (goetyPulverize) {
            refreshIngredientSlot(goetyPulverizeIngredientSlot, 0);
            setSlot(goetyPulverizeOutputSlot, controller.getVisualResult());
        } else if (goetyBrewing) {
            refreshGoetyBrewing();
        } else if (largeCraftingGrid) {
            refreshMechanicalCrafting();
        } else if (createSequencedAssembly) {
            refreshCreateSequencedAssembly();
        } else if (createProcessing) {
            refreshCreateProcessing();
        } else if (arsNouveauApparatus) {
            refreshArsNouveauApparatus();
        } else if (arsNouveauImbuement) {
            refreshArsNouveauImbuement();
        } else if (arsNouveauGlyph) {
            refreshArsNouveauGlyph();
        } else if (arsNouveauCrush) {
            refreshArsNouveauCrush();
        } else if (kaleidoscopePot) {
            refreshKaleidoscopePot();
        } else if (kaleidoscopeStockpot) {
            refreshKaleidoscopeStockpot();
        } else if (kaleidoscopeMillstone) {
            refreshKaleidoscopeSingleInput(kaleidoscopeMillstoneInputSlot, kaleidoscopeMillstoneResultSlot);
        } else if (kaleidoscopeChoppingBoard) {
            refreshKaleidoscopeSingleInput(kaleidoscopeChoppingBoardInputSlot, kaleidoscopeChoppingBoardResultSlot);
        } else if (kaleidoscopeSteamer) {
            refreshKaleidoscopeSingleInput(kaleidoscopeSteamerInputSlot, kaleidoscopeSteamerResultSlot);
        } else if (kaleidoscopeTeapot) {
            refreshKaleidoscopeTeapot();
        } else if (cooking) {
            refreshCookingCanvas(campfireCooking);
        } else if (singleInput) {
            refreshIngredientSlot(singleInputIngredientSlot, 0);
            setSlot(singleInputOutputSlot, controller.getVisualResult());
        } else {
            for (int i = 0; i < craftingIngredientSlots.length; i++) {
                refreshIngredientSlot(craftingIngredientSlots[i], i);
            }
            setSlot(craftingOutputSlot, controller.getVisualResult());
        }
        updateStatus();
        if (canvasViewport != null) {
            canvasViewport.requestReflow();
        }
    }

    private void refreshTouhouLittleMaidAltar() {
        for (int index = 0; index < touhouLittleMaidAltarIngredientSlots.length; index++) {
            refreshIngredientSlot(touhouLittleMaidAltarIngredientSlots[index], index);
        }
        setSlot(touhouLittleMaidAltarResultSlot, controller.getVisualResult());
        var entry = controller.getSelectedEntry();
        if (entry == null || !controller.isTouhouLittleMaidAltarEntry(entry)) {
            return;
        }
        var data = entry.getTouhouLittleMaidAltar();
        touhouLittleMaidAltarPowerLabel.setText(Component.literal(String.format(Locale.ROOT, "×%.2f", data.getPower())));
        var resultName = data.getLangKey() == null || data.getLangKey().isBlank()
                ? controller.getVisualResult().getHoverName()
                : Component.translatable(data.getLangKey());
        touhouLittleMaidAltarResultDescriptionLabel.setText(Component.translatable(
                "jei.touhou_little_maid.altar_craft.result",
                resultName
        ));
    }

    private void refreshGoetyCursedInfuser() {
        refreshIngredientSlot(goetyCursedInfuserIngredientSlot, 0);
        setSlot(goetyCursedInfuserOutputSlot, controller.getVisualResult());
        var entry = controller.getSelectedEntry();
        if (entry == null || !controller.isGoetyCursedInfuserEntry(entry)) {
            return;
        }
        var data = entry.getGoetyCursedInfuser();
        setItemTexture(goetyCursedInfuserMachineIcon, new ItemStack(itemFromRegistry(
                data.isGrim() ? "goety:grim_infuser" : "goety:cursed_infuser",
                Items.FURNACE
        )));
        goetyCursedInfuserTimeLabel.setText(Component.translatable(
                "viscript_recipe.editor.goety.time_seconds",
                Math.max(1, data.getCookingTime()) / 20
        ));
    }

    private void refreshGoetyRitual() {
/*        refreshIngredientSlot(goetyRitualActivationSlot, 0);
        for (int index = 0; index < goetyRitualIngredientSlots.length; index++) {
            refreshIngredientSlot(goetyRitualIngredientSlots[index], index + 1);
        }
        setSlot(goetyRitualOutputSlot, controller.getVisualResult());
        var entry = controller.getSelectedEntry();
        if (entry == null || !controller.isGoetyRitualEntry(entry)) {
            return;
        }
        var data = entry.getGoetyRitual();
        var type = data.getCraftType();
        var typeName = type == null ? "magic" : type.getSerializedName();
        setItemTexture(goetyRitualTypeIcon, GoetyRecipeUiSupport.ritualTypeIcon(typeName));
        var researchScroll = GoetyRecipeUiSupport.researchScroll(data.getResearch());
        goetyRitualResearchIcon.setDisplay(!researchScroll.isEmpty());
        setItemTexture(goetyRitualResearchIcon, researchScroll);
        goetyRitualInfoLabel.setText(Component.translatable(
                "viscript_recipe.editor.goety.ritual.info",
                Component.translatable("viscript_recipe.editor.goety.ritual.craft_type." + typeName),
                Math.max(0, data.getSoulCost()),
                Math.max(1, data.getDuration())
        ));*/
    }

    private void refreshGoetyBrazier() {
        for (int index = 0; index < goetyBrazierIngredientSlots.length; index++) {
            refreshIngredientSlot(goetyBrazierIngredientSlots[index], index);
        }
        setSlot(goetyBrazierOutputSlot, controller.getVisualResult());
        var entry = controller.getSelectedEntry();
        var soulCost = entry == null || !controller.isGoetyBrazierEntry(entry)
                ? 0
                : Math.max(0, entry.getGoetyBrazier().getSoulCost());
        goetyBrazierSoulLabel.setText(Component.translatable(
                "viscript_recipe.editor.goety.soul_cost",
                soulCost
        ));
    }

    private void refreshGoetyBrewing() {
/*        refreshIngredientSlot(goetyBrewingCatalystSlot, 0);
        var entry = controller.getSelectedEntry();
        if (entry == null || !controller.isGoetyBrewingEntry(entry)) {
            return;
        }
        var data = entry.getGoetyBrewing();
        setItemTexture(goetyBrewingOutputIcon, GoetyRecipeUiSupport.brewPreview(
                data.getEffect(),
                data.getDuration()
        ));
        goetyBrewingInfoLabel.setText(Component.translatable(
                "viscript_recipe.editor.goety.brewing.info",
                Math.max(0, data.getCapacityExtra()),
                Math.max(0, data.getSoulCost())
        ));*/
    }

    private boolean isSelectedCampfireCooking() {
        var entry = controller.getSelectedEntry();
        return entry == null
                ? RecipeEditorTypes.CAMPFIRE.equals(controller.getSelectedCategory())
                : entry.isType(RecipeEditorTypes.CAMPFIRE_COOKING);
    }

    private void refreshAlchemistChance(boolean isBrew) {
/*        if (!useJeiAlchemistCanvas) {
            return;
        }
        var input = controller.getVisualIngredient(0);
        var showChance = isBrew
                && !input.isEmpty()
                && IRON_SCROLL_ID.equals(BuiltInRegistries.ITEM.getKey(input.getItem()));
        alchemistChanceLabel.setDisplay(showChance);
        if (!showChance) {
            alchemistChanceLabel.setText(Component.empty());
            return;
        }
        var chance = IronSpellbooksRecipeUiSupport.scrollRecycleChancePercent();
        var color = chance >= 100 ? ChatFormatting.GREEN.getColor() : ChatFormatting.RED.getColor();
        alchemistChanceLabel.setText(Component.literal(chance + "%"));
        alchemistChanceLabel.textStyle(style -> style.textColor(color == null ? 0xFFFFFF : color));*/
    }

    private void refreshFarmerCookingInfo() {
        if (!useJeiFarmersCookingPotCanvas) {
            return;
        }
        var entry = controller.getSelectedEntry();
        if (entry == null || !controller.isFarmersCookingPotEntry(entry)) {
            farmerCookingTimeIcon.style(style -> style.tooltips(Component.translatable(
                    "viscript_recipe.config.cooking.cooking_time"
            )));
            farmerCookingExperienceIcon.setDisplay(false);
            return;
        }

        farmerCookingTimeIcon.style(style -> style.tooltips(Component.translatable(
                "viscript_recipe.editor.cooking.time_seconds",
                controller.getFarmersCookingTime(entry) / 20
        )));
        var experience = controller.getFarmersCookingExperience(entry);
        farmerCookingExperienceIcon.setDisplay(experience > 0);
        farmerCookingExperienceIcon.style(style -> style.tooltips(Component.translatable(
                "viscript_recipe.editor.cooking.experience_value",
                experience
        )));
    }

    private void refreshCookingCanvas(boolean campfireCooking) {
        if (!useJeiCookingCanvas) {
            var inputSlot = campfireCooking ? campfireCookingIngredientSlot : cookingIngredientSlot;
            var outputSlot = campfireCooking ? campfireCookingOutputSlot : cookingOutputSlot;
            refreshIngredientSlot(inputSlot, 0);
            setSlot(outputSlot, controller.getVisualResult());
            return;
        }
        var entry = controller.getSelectedEntry();
        var cookingTime = entry == null || !controller.isCookingEntry(entry)
                ? 0
                : controller.getCookingTime(entry);
        var timeText = cookingTime <= 0
                ? Component.empty()
                : Component.translatable("viscript_recipe.editor.cooking.time_seconds", cookingTime / 20);

        if (campfireCooking) {
            refreshIngredientSlot(campfireCookingIngredientSlot, 0);
            setSlot(campfireCookingOutputSlot, controller.getVisualResult());
            campfireCookingTimeLabel.setText(timeText);
            return;
        }

        refreshIngredientSlot(cookingIngredientSlot, 0);
        setSlot(cookingOutputSlot, controller.getVisualResult());
        cookingTimeLabel.setText(timeText);
        var experience = entry == null || !controller.isCookingEntry(entry)
                ? 0
                : controller.getCookingExperience(entry);
        cookingExperienceLabel.setDisplay(experience > 0);
        cookingExperienceLabel.setText(experience > 0
                ? Component.translatable("viscript_recipe.editor.cooking.experience_value", experience)
                : Component.empty());
    }

    private void refreshIngredientSlot(IngredientDisplaySlot slot, int index) {
        if (slot == null) {
            return;
        }
        var tagDisplayStacks = controller.getVisualIngredientTagStacks(index);
        if (tagDisplayStacks.length > 0) {
            slot.setTagDisplayStacks(tagDisplayStacks);
        } else {
            slot.clearTagDisplayStacks();
            setSlot(slot, controller.getVisualIngredient(index));
        }
        updateIngredientSlotTooltip(slot, index);
    }

    private void refreshExtendedCombination() {
        for (int i = 0; i < extendedCombinationIngredientSlots.length; i++) {
            refreshIngredientSlot(extendedCombinationIngredientSlots[i], i);
        }
        setSlot(extendedCombinationOutputSlot, controller.getVisualResult());
    }

    private void refreshExtendedCompressor() {
        refreshIngredientSlot(extendedCompressorCatalystSlot, 0);
        refreshIngredientSlot(extendedCompressorInputSlot, 1);
        setSlot(extendedCompressorOutputSlot, controller.getVisualResult());
    }

    private void refreshExtendedFlux() {
        for (int i = 0; i < extendedFluxIngredientSlots.length; i++) {
            refreshIngredientSlot(extendedFluxIngredientSlots[i], i);
        }
        setSlot(extendedFluxOutputSlot, controller.getVisualResult());
    }

    private void refreshKaleidoscopePot() {
        refreshKaleidoscopeGrid(kaleidoscopePotIngredientSlots);
        refreshIngredientSlot(kaleidoscopePotCarrierSlot, KALEIDOSCOPE_CARRIER_SLOT);
        setSlot(kaleidoscopePotResultSlot, controller.getVisualResult());
        kaleidoscopePotStirFryLabel.setText(controller.selectedKaleidoscopePotStirFryLabel());
    }

    private void refreshKaleidoscopeStockpot() {
        refreshKaleidoscopeGrid(kaleidoscopeStockpotIngredientSlots);
        refreshIngredientSlot(kaleidoscopeStockpotCarrierSlot, KALEIDOSCOPE_CARRIER_SLOT);
        setSlot(kaleidoscopeStockpotResultSlot, controller.getVisualResult());
    }

    private void refreshKaleidoscopeGrid(IngredientDisplaySlot[] slots) {
        for (int i = 0; i < slots.length; i++) {
            refreshIngredientSlot(slots[i], i);
        }
    }

    private void refreshKaleidoscopeSingleInput(IngredientDisplaySlot inputSlot, ItemSlot resultSlot) {
        refreshIngredientSlot(inputSlot, 0);
        setSlot(resultSlot, controller.getVisualResult());
    }

    private void refreshKaleidoscopeTeapot() {
        setSlot(kaleidoscopeTeapotFluidBucketSlot, controller.getVisualKaleidoscopeTeapotFluidBucket());
        refreshIngredientSlot(kaleidoscopeTeapotInputSlot, 0);
        if (controller.getVisualIngredientTagStacks(0).length == 0) {
            var ingredient = controller.getVisualIngredient(0);
            if (!ingredient.isEmpty()) {
                ingredient.setCount(Math.min(99, controller.selectedKaleidoscopeTeapotIngredientCount()));
            }
            setSlot(kaleidoscopeTeapotInputSlot, ingredient);
        }
        setSlot(kaleidoscopeTeapotResultSlot, controller.getVisualResult());
        kaleidoscopeTeapotTimeLabel.setText(controller.selectedKaleidoscopeTeapotTimeLabel());
    }

    private void refreshMechanicalCrafting() {
        var width = controller.selectedLargeCraftingGridWidth();
        var height = controller.selectedLargeCraftingGridHeight();
        var showJeiProcess = useJeiMechanicalCraftingCanvas
                && controller.isSelectedCreateMechanicalCraftingLayout();
        if (mechanicalCraftingJeiProcess != null) {
            mechanicalCraftingJeiProcess.setDisplay(showJeiProcess);
        }
        if (mechanicalCraftingFallbackProcess != null) {
            mechanicalCraftingFallbackProcess.setDisplay(!showJeiProcess);
        }
        updateMechanicalCraftingGridSkin(showJeiProcess);
        if (!showJeiProcess) {
            setItemTexture(mechanicalCraftingWorkstationIcon, controller.selectedLargeCraftingGridWorkstationStack());
        }
        if (mechanicalCraftingGrid != null) {
            mechanicalCraftingGrid.layout(layout -> {
                layout.width(mechanicalCraftingGridDimension(width));
                layout.height(mechanicalCraftingGridDimension(height));
            });
        }
        var ingredientCount = 0;
        for (int row = 0; row < MECHANICAL_CRAFTING_GRID_SIZE; row++) {
            var rowVisible = row < height;
            if (mechanicalCraftingIngredientRows[row] != null) {
                mechanicalCraftingIngredientRows[row].setDisplay(rowVisible);
                mechanicalCraftingIngredientRows[row].layout(layout -> {
                    layout.width(mechanicalCraftingGridInnerDimension(width));
                    layout.height(rowVisible ? MECHANICAL_CRAFTING_SLOT_SIZE : 0);
                });
            }
            for (int col = 0; col < MECHANICAL_CRAFTING_GRID_SIZE; col++) {
                var index = row * MECHANICAL_CRAFTING_GRID_SIZE + col;
                var visible = rowVisible && col < width;
                if (mechanicalCraftingIngredientSlotCells[index] != null) {
                    mechanicalCraftingIngredientSlotCells[index].setDisplay(visible);
                    mechanicalCraftingIngredientSlotCells[index].layout(layout -> {
                        layout.width(visible ? MECHANICAL_CRAFTING_SLOT_SIZE : 0);
                        layout.height(visible ? MECHANICAL_CRAFTING_SLOT_SIZE : 0);
                    });
                }
                if (visible) {
                    refreshIngredientSlot(mechanicalCraftingIngredientSlots[index], index);
                    if (!controller.getVisualIngredient(index).isEmpty()
                            || controller.getVisualIngredientTagStacks(index).length > 0) {
                        ingredientCount++;
                    }
                }
            }
        }
        mechanicalCraftingIngredientCountLabel.setText(Component.literal(Integer.toString(ingredientCount)));
        setSlot(mechanicalCraftingOutputSlot, controller.getVisualResult());
        setSlot(mechanicalCraftingJeiOutputSlot, controller.getVisualResult());
    }

    private void updateMechanicalCraftingGridSkin(boolean useJeiSkin) {
        if (mechanicalCraftingUsesJeiGrid == useJeiSkin) {
            return;
        }
        mechanicalCraftingUsesJeiGrid = useJeiSkin;
        mechanicalCraftingGrid.layout(layout -> {
            layout.paddingAll(mechanicalCraftingGridPadding());
            layout.gapAll(mechanicalCraftingGridGap());
        }).style(style -> style.backgroundTexture(useJeiSkin
                ? IGuiTexture.EMPTY
                : Sprites.BORDER_DARK));
        for (var row : mechanicalCraftingIngredientRows) {
            if (row != null) {
                row.layout(layout -> layout.gapAll(mechanicalCraftingGridGap()));
            }
        }
        for (int index = 0; index < mechanicalCraftingIngredientSlotCells.length; index++) {
            var cell = mechanicalCraftingIngredientSlotCells[index];
            var slot = mechanicalCraftingIngredientSlots[index];
            if (cell != null && slot != null) {
                CreateMechanicalCraftingCanvasFactory.configureGridCell(cell, slot, useJeiSkin);
            }
        }
    }

    private void refreshCreateSequencedAssembly() {
        refreshIngredientSlot(createSequencedInputSlot, 0);
        setSlot(createSequencedTransitionalSlot, controller.getVisualCreateSequencedTransitional());
        createSequencedLoopsLabel.setText(Component.translatable(
                "viscript_recipe.editor.create.sequenced_assembly.loops",
                controller.getSelectedEntry() == null ? 1 : controller.getCreateSequencedLoops(controller.getSelectedEntry())
        ));
        var stepCount = controller.selectedCreateSequencedStepCount();
        for (int i = 0; i < createSequencedStepCards.length; i++) {
            var visible = i < stepCount;
            if (createSequencedStepCards[i] != null) {
                createSequencedStepCards[i].setDisplay(visible);
            }
            if (visible) {
                refreshCreateSequencedStep(i);
            }
        }
        var outputCount = controller.selectedCreateSequencedOutputCount();
        if (createSequencedSecondaryOutputColumn != null) {
            createSequencedSecondaryOutputColumn.setDisplay(outputCount > 1);
        }
        for (int i = 0; i < createSequencedOutputSlots.length; i++) {
            if (createSequencedOutputSlotCells[i] != null) {
                createSequencedOutputSlotCells[i].setDisplay(i < outputCount);
            }
            if (i < outputCount) {
                setSlot(createSequencedOutputSlots[i], controller.getVisualCreateOutput(i));
            }
        }
    }

    private void refreshCreateSequencedStep(int index) {
        var entry = controller.getSelectedEntry();
        var kind = entry == null
                ? CreateSequencedAssemblyStepKind.DEPLOYING
                : controller.getCreateSequencedStepKind(entry, index);
        var selected = controller.isSelectedCreateSequencedStep(index);
        if (createSequencedStepLabels[index] != null) {
            createSequencedStepLabels[index].setText(Component.translatable(
                    "viscript_recipe.editor.create.sequenced_assembly.step_short",
                    index + 1
            ));
            createSequencedStepLabels[index].textStyle(style -> style
                    .textAlignHorizontal(Horizontal.CENTER)
                    .textColor(selected ? ColorPattern.WHITE.color : ColorPattern.LIGHT_GRAY.color)
                    .textWrap(TextWrap.HOVER_ROLL));
        }
        if (createSequencedStepIcons[index] != null) {
            createSequencedStepIcons[index].style(style -> style
                    .backgroundTexture(new ItemStackTexture(new ItemStack(itemFromRegistry(kind.machineItemId(), Items.CRAFTING_TABLE))))
                    .tooltips(controller.createSequencedStepKindDisplayName(kind)));
        }
        var deploying = kind == CreateSequencedAssemblyStepKind.DEPLOYING;
        var filling = kind == CreateSequencedAssemblyStepKind.FILLING;
        if (createSequencedStepIngredientCells[index] != null) {
            createSequencedStepIngredientCells[index].setDisplay(deploying);
        }
        if (deploying) {
            refreshIngredientSlot(createSequencedStepIngredientSlots[index], createSequencedIngredientSlotIndex(index));
        }
        if (createSequencedStepFluidCells[index] != null) {
            createSequencedStepFluidCells[index].setDisplay(filling);
        }
        if (filling) {
            refreshCreateSequencedStepFluidSlot(createSequencedStepFluidSlots[index], index);
        }
    }

    private void refreshCreateSequencedStepFluidSlot(FluidDisplaySlot slot, int index) {
        if (slot == null) {
            return;
        }
        var tagDisplayStacks = controller.getCreateSequencedStepFluidTagStacks(index);
        if (tagDisplayStacks.length > 0) {
            slot.setTagDisplayStacks(tagDisplayStacks);
        } else {
            slot.clearTagDisplayStacks();
            setFluid(slot, controller.getCreateSequencedStepFluidDisplay(index));
        }
        var tag = controller.getCreateSequencedStepFluidTag(index);
        if (tag == null) {
            slot.style(style -> style.tooltips(Component.translatable(
                    "viscript_recipe.editor.create.sequenced_assembly.step_fluid_slot",
                    index + 1
            )));
        } else {
            slot.style(style -> style.tooltips(Component.translatable(
                    "viscript_recipe.editor.create.sequenced_assembly.step_fluid_slot_tag",
                    index + 1,
                    "#" + tag
            )));
        }
    }

    private void refreshArsNouveauApparatus() {
        var inputCount = controller.selectedArsNouveauInputCount();
        var derivedPreview = controller.selectedArsNouveauApparatusHasDerivedPreview();
        for (int i = 0; i < arsNouveauIngredientSlots.length; i++) {
            var visible = i < inputCount && !(derivedPreview && i == 0);
            if (arsNouveauIngredientSlotCells[i] != null) {
                arsNouveauIngredientSlotCells[i].setDisplay(visible);
            }
            if (visible) {
                refreshIngredientSlot(arsNouveauIngredientSlots[i], i);
            }
        }
        arsNouveauApparatusCenterPreviewIcon.setDisplay(derivedPreview);
        arsNouveauApparatusOutputPreviewIcon.setDisplay(derivedPreview);
        if (arsNouveauApparatusCenterPreviewCell != null) {
            arsNouveauApparatusCenterPreviewCell.setDisplay(derivedPreview);
        }
        if (arsNouveauApparatusOutputPreviewCell != null) {
            arsNouveauApparatusOutputPreviewCell.setDisplay(derivedPreview);
        }
        arsNouveauResultSlot.setDisplay(!derivedPreview);
        if (derivedPreview) {
            setItemTexture(arsNouveauApparatusCenterPreviewIcon, controller.selectedArsNouveauApparatusCenterPreview());
            setItemTexture(arsNouveauApparatusOutputPreviewIcon, controller.selectedArsNouveauApparatusOutputPreview());
        }
        setSlot(arsNouveauResultSlot, controller.getVisualResult());
        arsNouveauApparatusSourceLabel.setText(controller.selectedArsNouveauApparatusSourceLabel());
        arsNouveauApparatusTierLabel.setText(controller.selectedArsNouveauApparatusTierLabel());
    }

    private void refreshArsNouveauImbuement() {
        var showDefaultCenter = controller.getSelectedEntry() == null;
        arsNouveauImbuementInputSlot.setDisplay(!showDefaultCenter);
        arsNouveauImbuementDefaultCenterIcon.setDisplay(showDefaultCenter);
        arsNouveauImbuementDefaultCenterIcon.style(style -> style
                .backgroundTexture(new ItemStackTexture(controller.selectedArsNouveauWorkstationStack()))
                .tooltips(Component.translatable("block.ars_nouveau.imbuement_chamber")));
        if (!showDefaultCenter) {
            refreshIngredientSlot(arsNouveauImbuementInputSlot, 0);
        }
        for (int i = 0; i < arsNouveauImbuementPedestalSlots.length; i++) {
            refreshIngredientSlot(arsNouveauImbuementPedestalSlots[i], i + 1);
            if (arsNouveauImbuementPedestalSlotCells[i] != null) {
                arsNouveauImbuementPedestalSlotCells[i].setDisplay(true);
            }
        }
        setSlot(arsNouveauImbuementResultSlot, controller.getVisualResult());
        var entry = controller.getSelectedEntry();
        var source = entry == null ? 0 : controller.getArsNouveauSourceCost(entry);
        arsNouveauImbuementSourceLabel.setText(Component.translatable("ars_nouveau.source", source));
    }

    private void refreshArsNouveauGlyph() {
        for (int i = 0; i < arsNouveauGlyphIngredientSlots.length; i++) {
            if (arsNouveauGlyphIngredientSlotCells[i] != null) {
                arsNouveauGlyphIngredientSlotCells[i].setDisplay(true);
            }
            refreshIngredientSlot(arsNouveauGlyphIngredientSlots[i], i);
        }
        arsNouveauGlyphWorkstationIcon.style(style -> style
                .backgroundTexture(new ItemStackTexture(controller.selectedArsNouveauWorkstationStack()))
                .tooltips(Component.translatable("block.ars_nouveau.scribes_table")));
        setSlot(arsNouveauGlyphResultSlot, controller.getVisualResult());
        var entry = controller.getSelectedEntry();
        arsNouveauGlyphExpLabel.setText(entry == null || !controller.isArsNouveauGlyphEntry(entry)
                ? Component.empty()
                : Component.translatable("viscript_recipe.editor.ars_nouveau.glyph_exp", controller.getArsNouveauGlyphExperience(entry)));
    }

    private void refreshArsNouveauCrush() {
        refreshIngredientSlot(arsNouveauCrushInputSlot, 0);
        var outputCount = controller.selectedArsNouveauCrushOutputCount();
        for (int i = 0; i < arsNouveauCrushOutputSlots.length; i++) {
            if (arsNouveauCrushOutputSlotCells[i] != null) {
                arsNouveauCrushOutputSlotCells[i].setDisplay(i < outputCount);
            }
            if (i < outputCount) {
                setSlot(arsNouveauCrushOutputSlots[i], controller.getVisualArsNouveauOutput(i));
            }
        }
    }

    private void refreshCreateProcessing() {
        var kind = controller.selectedCreateKind().orElse(null);
        var crushing = kind == CreateProcessingKind.CRUSHING;
        var milling = kind == CreateProcessingKind.MILLING;
        var cutting = kind == CreateProcessingKind.CUTTING;
        var blockCutting = kind == CreateProcessingKind.BLOCK_CUTTING;
        var sawing = cutting || blockCutting;
        var autoPacking = kind == CreateProcessingKind.AUTO_PACKING;
        var sandpaper = kind == CreateProcessingKind.SANDPAPER_POLISHING;
        var automaticBrewing = kind == CreateProcessingKind.AUTOMATIC_BREWING;
        var pressBasin = isCreateBasinKind(kind) && !automaticBrewing;
        var pressing = kind == CreateProcessingKind.PRESSING;
        var spout = kind == CreateProcessingKind.FILLING;
        var drain = kind == CreateProcessingKind.EMPTYING;
        var deployer = kind == CreateProcessingKind.DEPLOYING;
        var manualApplication = kind == CreateProcessingKind.ITEM_APPLICATION;
        var fan = isCreateFanKind(kind);
        var fanSingleOutput = isCreateFanSingleOutputKind(kind);
        if (genericCreateProcessingCanvas != null) {
            genericCreateProcessingCanvas.setDisplay(!spout && !drain && !deployer && !manualApplication && !fan && !crushing && !milling && !sawing && !autoPacking && !sandpaper && !pressBasin && !automaticBrewing && !pressing);
        }
        if (createSpoutCanvas != null) {
            createSpoutCanvas.setDisplay(spout);
        }
        if (createDrainCanvas != null) {
            createDrainCanvas.setDisplay(drain);
        }
        if (createDeployerCanvas != null) {
            createDeployerCanvas.setDisplay(deployer);
        }
        if (createManualApplicationCanvas != null) {
            createManualApplicationCanvas.setDisplay(manualApplication);
        }
        if (createFanCanvas != null) {
            createFanCanvas.setDisplay(fan);
        }
        if (createCrushingCanvas != null) {
            createCrushingCanvas.setDisplay(crushing);
        }
        if (createMillingCanvas != null) {
            createMillingCanvas.setDisplay(milling);
        }
        if (createSawCanvas != null) {
            createSawCanvas.setDisplay(sawing);
        }
        if (createSawOutputGrid != null) {
            createSawOutputGrid.setDisplay(cutting);
        }
        if (createBlockCuttingOutputGrid != null) {
            createBlockCuttingOutputGrid.setDisplay(blockCutting);
        }
        if (createAutoPackingCanvas != null) {
            createAutoPackingCanvas.setDisplay(autoPacking);
        }
        if (createSandpaperCanvas != null) {
            createSandpaperCanvas.setDisplay(sandpaper);
        }
        if (createPressBasinCanvas != null) {
            createPressBasinCanvas.setDisplay(pressBasin);
        }
        if (createAutomaticBrewingCanvas != null) {
            createAutomaticBrewingCanvas.setDisplay(automaticBrewing);
        }
        if (createPressingCanvas != null) {
            createPressingCanvas.setDisplay(pressing);
        }
        var itemInputCount = controller.selectedCreateItemInputCount();
        for (int i = 0; i < createIngredientSlots.length; i++) {
            if (createIngredientSlotCells[i] != null) {
                createIngredientSlotCells[i].setDisplay(i < itemInputCount);
            }
            if (i < itemInputCount) {
                refreshIngredientSlot(createIngredientSlots[i], i);
            }
        }
        var fluidInputCount = controller.selectedCreateFluidInputCount();
        for (int i = 0; i < createFluidInputSlots.length; i++) {
            if (createFluidInputColumns[i] != null) {
                createFluidInputColumns[i].setDisplay(i < fluidInputCount);
            }
            if (i < fluidInputCount) {
                refreshCreateFluidInputSlot(createFluidInputSlots[i], i);
            }
        }
        var itemOutputCount = controller.selectedCreateItemOutputCount();
        for (int i = 0; i < createOutputSlots.length; i++) {
            if (createOutputSlotCells[i] != null) {
                createOutputSlotCells[i].setDisplay(i < itemOutputCount);
            }
            if (i < itemOutputCount) {
                setSlot(createOutputSlots[i], controller.getVisualCreateOutput(i));
            }
        }
        var fluidOutputCount = controller.selectedCreateFluidOutputCount();
        for (int i = 0; i < createFluidOutputSlots.length; i++) {
            if (createFluidOutputColumns[i] != null) {
                createFluidOutputColumns[i].setDisplay(i < fluidOutputCount);
            }
            if (i < fluidOutputCount) {
                setFluid(createFluidOutputSlots[i], controller.getVisualCreateFluidOutput(i));
            }
        }
        if (kind != null) {
            createMachineLabel.setText(Component.translatable("viscript_recipe.editor.type.create." + kind.translationPath()));
            createMachineIcon.style(style -> style.backgroundTexture(new ItemStackTexture(new ItemStack(itemFromRegistry(kind.machineItemId(), Items.CRAFTING_TABLE)))));
        }
        if (crushing) {
            refreshIngredientSlot(createCrushingIngredientSlot, 0);
            refreshCreateOutputSlots(createCrushingOutputSlots, createCrushingOutputSlotCells, itemOutputCount);
        } else if (milling) {
            refreshIngredientSlot(createMillingIngredientSlot, 0);
            refreshCreateOutputSlots(createMillingOutputSlots, createMillingOutputSlotCells, itemOutputCount);
        } else if (sawing) {
            refreshIngredientSlot(createSawIngredientSlot, 0);
            if (cutting) {
                refreshCreateSawOutputRows(itemOutputCount);
                refreshCreateOutputSlots(createSawOutputSlots, createSawOutputSlotCells, itemOutputCount);
            } else {
                refreshCreateBlockCuttingOutputRows(itemOutputCount);
                refreshCreateOutputSlots(createBlockCuttingOutputSlots, createBlockCuttingOutputSlotCells, itemOutputCount);
            }
        } else if (autoPacking) {
            refreshCreateAutoPacking();
        } else if (sandpaper) {
            refreshCreateSandpaper();
        } else if (pressBasin) {
            refreshCreatePressBasin(kind, itemInputCount, fluidInputCount, itemOutputCount, fluidOutputCount);
        } else if (automaticBrewing) {
            refreshCreateAutomaticBrewing();
        } else if (pressing) {
            refreshIngredientSlot(createPressingIngredientSlot, 0);
            refreshCreateOutputSlots(createPressingOutputSlots, createPressingOutputSlotCells, itemOutputCount);
        } else if (spout) {
            refreshCreateSpout();
        } else if (drain) {
            refreshCreateDrain();
        } else if (deployer) {
            refreshCreateDeployer(itemOutputCount);
        } else if (manualApplication) {
            refreshCreateManualApplication(itemOutputCount);
        } else if (fan) {
            refreshCreateFan(kind, fanSingleOutput, itemOutputCount);
        }
    }

    private void refreshCreateSpout() {
        refreshIngredientSlot(createSpoutIngredientSlot, 0);
        refreshCreateFluidInputSlot(createSpoutFluidInputSlot, 0);
        setSlot(createSpoutOutputSlot, controller.getVisualCreateOutput(0));
    }

    private void refreshCreateDrain() {
        refreshIngredientSlot(createDrainIngredientSlot, 0);
        setFluid(createDrainFluidOutputSlot, controller.getVisualCreateFluidOutput(0));
        setSlot(createDrainOutputSlot, controller.getVisualCreateOutput(0));
    }

    private void refreshCreateDeployer(int itemOutputCount) {
        refreshIngredientSlot(createDeployerProcessedSlot, 0);
        refreshIngredientSlot(createDeployerHeldSlot, 1);
        refreshCreateDeployerOutputRows(itemOutputCount);
        refreshCreateOutputSlots(createDeployerOutputSlots, createDeployerOutputSlotCells, itemOutputCount);
    }

    private void refreshCreateManualApplication(int itemOutputCount) {
        refreshIngredientSlot(createManualApplicationBlockSlot, 0);
        refreshIngredientSlot(createManualApplicationHeldSlot, 1);
        updateCreateManualApplicationBlockPreview();
        refreshCreateManualApplicationOutputRows(itemOutputCount);
        refreshCreateOutputSlots(createManualApplicationOutputSlots, createManualApplicationOutputSlotCells, itemOutputCount);
    }

    private void refreshCreateFan(CreateProcessingKind kind, boolean singleOutput, int itemOutputCount) {
        refreshIngredientSlot(createFanIngredientSlot, 0);
        if (createFanSingleOutputPanel != null) {
            createFanSingleOutputPanel.setDisplay(singleOutput);
        }
        if (createFanMultiOutputPanel != null) {
            createFanMultiOutputPanel.setDisplay(!singleOutput);
        }
        updateCreateFanCatalyst(kind);
        if (singleOutput) {
            setSlot(createFanSingleOutputSlot, controller.getVisualCreateOutput(0));
        } else {
            refreshCreateOutputSlots(createFanOutputSlots, createFanOutputSlotCells, itemOutputCount);
        }
    }

    private void updateCreateFanCatalyst(CreateProcessingKind kind) {
        var catalystPath = createFanCatalystPath(kind);
        createFanCatalystLabel.setText(Component.translatable("viscript_recipe.editor.create.fan.catalyst." + catalystPath));
        createFanCatalystIcon.style(style -> style
                .backgroundTexture(createFanCatalystTexture(kind))
                .tooltips(Component.translatable("viscript_recipe.editor.create.fan.catalyst." + catalystPath)));
    }

    private static boolean isCreateFanKind(CreateProcessingKind kind) {
        return kind == CreateProcessingKind.BLASTING
                || kind == CreateProcessingKind.HAUNTING
                || kind == CreateProcessingKind.SMOKING
                || kind == CreateProcessingKind.SPLASHING;
    }

    private static boolean isCreateFanSingleOutputKind(CreateProcessingKind kind) {
        return kind == CreateProcessingKind.BLASTING || kind == CreateProcessingKind.SMOKING;
    }

    private static String createFanCatalystPath(CreateProcessingKind kind) {
        return switch (kind) {
            case BLASTING -> "blasting";
            case HAUNTING -> "haunting";
            case SMOKING -> "smoking";
            case SPLASHING -> "splashing";
            default -> "splashing";
        };
    }

    private static IGuiTexture createFanCatalystTexture(CreateProcessingKind kind) {
        return switch (kind) {
            case BLASTING -> new FluidStackTexture(new FluidStack(Fluids.LAVA, 1000));
            case HAUNTING -> new ItemStackTexture(new ItemStack(Items.SOUL_CAMPFIRE));
            case SMOKING -> new ItemStackTexture(new ItemStack(Items.CAMPFIRE));
            case SPLASHING -> new FluidStackTexture(new FluidStack(Fluids.WATER, 1000));
            default -> new FluidStackTexture(new FluidStack(Fluids.WATER, 1000));
        };
    }

    private void refreshCreateAutoPacking() {
        var gridSize = controller.getSelectedEntry() == null ? 3 : controller.getCreateAutoPackingGridSize(controller.getSelectedEntry());
        if (createAutoPackingInputGrid != null) {
            var size = autoPackingInputGridSize(gridSize);
            createAutoPackingInputGrid.layout(layout -> {
                layout.width(size);
                layout.height(size);
            });
        }
        for (int row = 0; row < createAutoPackingIngredientRows.length; row++) {
            if (createAutoPackingIngredientRows[row] != null) {
                var visible = row < gridSize;
                createAutoPackingIngredientRows[row].setDisplay(visible);
                createAutoPackingIngredientRows[row].layout(layout -> {
                    layout.width(SLOT_SIZE * gridSize + Math.max(0, gridSize - 1) * 2);
                    layout.height(visible ? SLOT_SIZE : 0);
                });
            }
        }
        for (int i = 0; i < createAutoPackingIngredientSlots.length; i++) {
            var row = i / 3;
            var col = i % 3;
            var visible = row < gridSize && col < gridSize;
            if (createAutoPackingIngredientSlotCells[i] != null) {
                createAutoPackingIngredientSlotCells[i].setDisplay(visible);
                createAutoPackingIngredientSlotCells[i].layout(layout -> {
                    layout.width(visible ? SLOT_SIZE : 0);
                    layout.height(visible ? SLOT_SIZE : 0);
                });
            }
            if (visible) {
                refreshIngredientSlot(createAutoPackingIngredientSlots[i], 0);
            }
        }
        setSlot(createAutoPackingOutputSlot, controller.getVisualCreateOutput(0));
    }

    private void refreshCreateSandpaper() {
        refreshIngredientSlot(sandpaperIngredientSlot, 0);
        setSlot(sandpaperOutputSlot, controller.getVisualCreateOutput(0));
    }

    private static int autoPackingInputGridSize(int gridSize) {
        var normalized = gridSize <= 2 ? 2 : 3;
        return SLOT_SIZE * normalized + Math.max(0, normalized - 1) * 2 + 8;
    }

    private int mechanicalCraftingGridDimension(int slots) {
        return mechanicalCraftingGridInnerDimension(slots) + mechanicalCraftingGridPadding() * 2;
    }

    private int mechanicalCraftingGridInnerDimension(int slots) {
        var normalized = Math.max(1, Math.min(MECHANICAL_CRAFTING_GRID_SIZE, slots));
        return MECHANICAL_CRAFTING_SLOT_SIZE * normalized
                + Math.max(0, normalized - 1) * mechanicalCraftingGridGap();
    }

    private int mechanicalCraftingGridPadding() {
        return mechanicalCraftingUsesJeiGrid ? 0 : 4;
    }

    private int mechanicalCraftingGridGap() {
        return mechanicalCraftingUsesJeiGrid ? 1 : 2;
    }

    private void refreshCreatePressBasin(CreateProcessingKind kind, int itemInputCount, int fluidInputCount, int itemOutputCount, int fluidOutputCount) {
        updateCreateBasinMachine(kind);
        for (int i = 0; i < createPressIngredientSlots.length; i++) {
            if (createPressIngredientSlotCells[i] != null) {
                createPressIngredientSlotCells[i].setDisplay(i < itemInputCount);
            }
            if (i < itemInputCount) {
                refreshIngredientSlot(createPressIngredientSlots[i], i);
            }
        }
        for (int row = 0; row < createPressIngredientRows.length; row++) {
            if (createPressIngredientRows[row] != null) {
                createPressIngredientRows[row].setDisplay(itemInputCount > row * 3);
            }
        }
        for (int i = 0; i < createPressFluidInputSlots.length; i++) {
            if (createPressFluidInputColumns[i] != null) {
                createPressFluidInputColumns[i].setDisplay(i < fluidInputCount);
            }
            if (i < fluidInputCount) {
                refreshCreateFluidInputSlot(createPressFluidInputSlots[i], i);
            }
        }
        if (createPressFluidInputRow != null) {
            createPressFluidInputRow.setDisplay(fluidInputCount > 0);
        }
        refreshCreateOutputSlots(createPressOutputSlots, createPressOutputSlotCells, itemOutputCount);
        refreshCreatePressOutputRows(itemOutputCount);
        for (int i = 0; i < createPressFluidOutputSlots.length; i++) {
            if (createPressFluidOutputColumns[i] != null) {
                createPressFluidOutputColumns[i].setDisplay(i < fluidOutputCount);
            }
            if (i < fluidOutputCount) {
                setFluid(createPressFluidOutputSlots[i], controller.getVisualCreateFluidOutput(i));
            }
        }
        if (createPressFluidOutputRow != null) {
            createPressFluidOutputRow.setDisplay(fluidOutputCount > 0);
        }
        var entry = controller.getSelectedEntry();
        var showHeat = controller.selectedCreateHeatAllowed() && entry != null;
        if (createPressHeatPanel != null) {
            createPressHeatPanel.setDisplay(showHeat);
        }
        if (showHeat) {
            var heat = controller.getCreateHeatRequirement(entry);
            createPressHeatLabel.setText(controller.createHeatDisplayName(heat));
        } else {
            createPressHeatLabel.setText(Component.empty());
        }
    }

    private void refreshCreateAutomaticBrewing() {
        refreshIngredientSlot(createAutomaticBrewingIngredientSlot, 0);
        refreshCreateFluidInputSlot(createAutomaticBrewingFluidInputSlot, 0);
        setFluid(createAutomaticBrewingFluidOutputSlot, controller.getVisualCreateFluidOutput(0));
        var entry = controller.getSelectedEntry();
        var showHeat = controller.selectedCreateHeatAllowed() && entry != null;
        if (createAutomaticBrewingHeatPanel != null) {
            createAutomaticBrewingHeatPanel.setDisplay(showHeat);
        }
        if (showHeat) {
            var heat = controller.getCreateHeatRequirement(entry);
            createAutomaticBrewingHeatLabel.setText(controller.createHeatDisplayName(heat));
        } else {
            createAutomaticBrewingHeatLabel.setText(Component.empty());
        }
    }

    private void updateCreateBasinMachine(CreateProcessingKind kind) {
        var machineItem = isCreateMixerKind(kind) ? "create:mechanical_mixer" : "create:mechanical_press";
        createBasinMachineIcon.style(style -> style.backgroundTexture(new ItemStackTexture(new ItemStack(itemFromRegistry(machineItem, Items.CRAFTING_TABLE)))));
    }

    private static boolean isCreateBasinKind(CreateProcessingKind kind) {
        return kind == CreateProcessingKind.COMPACTING || isCreateMixerKind(kind);
    }

    private static boolean isCreateMixerKind(CreateProcessingKind kind) {
        return kind == CreateProcessingKind.MIXING
                || kind == CreateProcessingKind.AUTOMATIC_SHAPELESS
                || kind == CreateProcessingKind.AUTOMATIC_BREWING;
    }

    private void refreshCreatePressOutputRows(int itemOutputCount) {
        if (createPressOutputRows[0] != null) {
            createPressOutputRows[0].setDisplay(itemOutputCount > 0);
        }
        if (createPressOutputRows[1] != null) {
            createPressOutputRows[1].setDisplay(itemOutputCount > 2);
        }
    }

    private void refreshCreateSawOutputRows(int itemOutputCount) {
        if (createSawOutputRows[0] != null) {
            createSawOutputRows[0].setDisplay(itemOutputCount > 0);
        }
        if (createSawOutputRows[1] != null) {
            createSawOutputRows[1].setDisplay(itemOutputCount > 2);
        }
    }

    private void refreshCreateDeployerOutputRows(int itemOutputCount) {
        if (createDeployerOutputRows[0] != null) {
            createDeployerOutputRows[0].setDisplay(itemOutputCount > 0);
        }
        if (createDeployerOutputRows[1] != null) {
            createDeployerOutputRows[1].setDisplay(itemOutputCount > 2);
        }
    }

    private void refreshCreateManualApplicationOutputRows(int itemOutputCount) {
        if (createManualApplicationOutputRows[0] != null) {
            createManualApplicationOutputRows[0].setDisplay(itemOutputCount > 0);
        }
        if (createManualApplicationOutputRows[1] != null) {
            createManualApplicationOutputRows[1].setDisplay(itemOutputCount > 2);
        }
    }

    private void updateCreateManualApplicationBlockPreview() {
        var block = manualApplicationPreviewBlockFromInput();
        if (block == createManualApplicationPreviewBlock) {
            return;
        }
        createManualApplicationPreviewBlock = block;
        createManualApplicationPreviewWorld.clear();
        if (block != Blocks.AIR) {
            createManualApplicationPreviewWorld.addBlock(BlockPos.ZERO, BlockInfo.fromBlock(block));
        }
        createManualApplicationBlockScene.setRenderedCore(List.of(BlockPos.ZERO)).setZoom(1.15f);
    }

    private Block manualApplicationPreviewBlockFromInput() {
        var tagStacks = controller.getVisualIngredientTagStacks(0);
        for (var stack : tagStacks) {
            var block = blockFromStack(stack);
            if (block != Blocks.AIR) {
                return block;
            }
        }
        return blockFromStack(controller.getVisualIngredient(0));
    }

    private static Block blockFromStack(ItemStack stack) {
        if (stack != null && stack.getItem() instanceof BlockItem blockItem) {
            return blockItem.getBlock();
        }
        return Blocks.AIR;
    }

    private void refreshCreateBlockCuttingOutputRows(int itemOutputCount) {
        for (int row = 0; row < createBlockCuttingOutputRows.length; row++) {
            if (createBlockCuttingOutputRows[row] != null) {
                createBlockCuttingOutputRows[row].setDisplay(itemOutputCount > row * BLOCK_CUTTING_OUTPUT_COLUMNS);
            }
        }
    }

    private void refreshCreateOutputSlots(ItemSlot[] slots, UIElement[] cells, int itemOutputCount) {
        var count = Math.min(slots.length, itemOutputCount);
        for (int i = 0; i < slots.length; i++) {
            if (cells[i] != null) {
                cells[i].setDisplay(i < count);
            }
            if (slots[i] != null && i < count) {
                setSlot(slots[i], controller.getVisualCreateOutput(i));
            }
        }
    }

    private void refreshCreateFluidInputSlot(FluidDisplaySlot slot, int index) {
        if (slot == null) {
            return;
        }
        var tagDisplayStacks = controller.getVisualCreateFluidInputTagStacks(index);
        if (tagDisplayStacks.length > 0) {
            slot.setTagDisplayStacks(tagDisplayStacks);
        } else {
            slot.clearTagDisplayStacks();
            setFluid(slot, controller.getVisualCreateFluidInputDisplay(index));
        }
        updateCreateFluidInputTooltip(slot, index);
    }

    private void updateCreateFluidInputTooltip(FluidSlot slot, int index) {
        var tag = controller.getVisualCreateFluidInputTag(index);
        if (tag == null) {
            slot.style(style -> style.tooltips(Component.translatable(
                    "viscript_recipe.editor.create.fluid_input_slot",
                    index + 1
            )));
            return;
        }
        slot.style(style -> style.tooltips(Component.translatable(
                "viscript_recipe.editor.create.fluid_input_slot_tag",
                index + 1,
                "#" + tag
        )));
    }

    private void updateSmithingInputLabels() {
        if (controller.isSelectedArcaneAnvilLayout()) {
            setSmithingInputLabel(0, "viscript_recipe.editor.arcane_anvil.input");
            setSmithingInputLabel(1, "viscript_recipe.editor.arcane_anvil.material");
            setSmithingInputLabel(2, "viscript_recipe.editor.smithing.addition");
            return;
        }
        setSmithingInputLabel(0, "viscript_recipe.editor.smithing.template");
        setSmithingInputLabel(1, "viscript_recipe.editor.smithing.base");
        setSmithingInputLabel(2, "viscript_recipe.editor.smithing.addition");
    }

    private void setSmithingInputLabel(int index, String key) {
        if (index < 0 || index >= smithingInputLabels.length || smithingInputLabels[index] == null) {
            return;
        }
        smithingInputLabels[index].setText(Component.translatable(key));
    }

    private void updateIngredientSlotTooltip(ItemSlot slot, int index) {
        var tag = controller.getVisualIngredientTag(index);
        var displayStacks = controller.getVisualIngredientTagStacks(index);
        if (controller.isSelectedCreateSequencedAssemblyLayout()) {
            updateCreateSequencedIngredientTooltip(slot, index, tag);
            return;
        }
        if (controller.isSelectedArsNouveauApparatusLayout()
                || controller.isSelectedArsNouveauGlyphLayout()
                || controller.isSelectedArsNouveauImbuementLayout()
                || controller.isSelectedArsNouveauCrushLayout()) {
            updateArsNouveauIngredientTooltip(slot, index, tag);
            return;
        }
        if (tag == null) {
            var itemAbility = controller.getVisualIngredientItemAbility(index);
            if (itemAbility != null) {
                slot.style(style -> style.tooltips(Component.translatable(
                        "viscript_recipe.editor.ingredient_slot_item_ability",
                        index + 1,
                        controller.itemAbilityDisplayName(itemAbility)
                )));
                return;
            }
            if (displayStacks.length > 1) {
                slot.style(style -> style.tooltips(Component.translatable(
                        "viscript_recipe.editor.ingredient_slot_multi",
                        index + 1,
                        displayStacks.length
                )));
                return;
            }
            slot.style(style -> style.tooltips(Component.translatable(
                    "viscript_recipe.editor.ingredient_slot",
                    index + 1
            )));
            return;
        }
        slot.style(style -> style.tooltips(Component.translatable(
                "viscript_recipe.editor.ingredient_slot_tag",
                index + 1,
                "#" + tag
        )));
    }

    private void updateArsNouveauIngredientTooltip(ItemSlot slot, int index, ResourceLocation tag) {
        var slotName = controller.arsNouveauInputSlotName(index);
        var displayStacks = controller.getVisualIngredientTagStacks(index);
        if (tag == null) {
            var itemAbility = controller.getVisualIngredientItemAbility(index);
            if (itemAbility != null) {
                slot.style(style -> style.tooltips(Component.translatable(
                        "viscript_recipe.editor.ars_nouveau.ingredient_slot_item_ability",
                        slotName,
                        controller.itemAbilityDisplayName(itemAbility)
                )));
                return;
            }
            if (displayStacks.length > 1) {
                slot.style(style -> style.tooltips(Component.translatable(
                        "viscript_recipe.editor.ars_nouveau.ingredient_slot_multi",
                        slotName,
                        displayStacks.length
                )));
                return;
            }
            slot.style(style -> style.tooltips(Component.translatable(
                    "viscript_recipe.editor.ars_nouveau.ingredient_slot",
                    slotName
            )));
            return;
        }
        slot.style(style -> style.tooltips(Component.translatable(
                "viscript_recipe.editor.ars_nouveau.ingredient_slot_tag",
                slotName,
                "#" + tag
        )));
    }

    private void updateCreateSequencedIngredientTooltip(ItemSlot slot, int index, ResourceLocation tag) {
        var key = index == 0
                ? "viscript_recipe.editor.create.sequenced_assembly.input_slot"
                : "viscript_recipe.editor.create.sequenced_assembly.step_ingredient_slot";
        if (tag == null) {
            if (index == 0) {
                slot.style(style -> style.tooltips(Component.translatable(key)));
            } else {
                slot.style(style -> style.tooltips(Component.translatable(key, index - CREATE_SEQUENCED_STEP_INGREDIENT_OFFSET + 1)));
            }
            return;
        }
        var tagKey = index == 0
                ? "viscript_recipe.editor.create.sequenced_assembly.input_slot_tag"
                : "viscript_recipe.editor.create.sequenced_assembly.step_ingredient_slot_tag";
        if (index == 0) {
            slot.style(style -> style.tooltips(Component.translatable(tagKey, "#" + tag)));
        } else {
            slot.style(style -> style.tooltips(Component.translatable(
                    tagKey,
                    index - CREATE_SEQUENCED_STEP_INGREDIENT_OFFSET + 1,
                    "#" + tag
            )));
        }
    }

    private void updateStatus() {
        var selectedEntry = controller.getSelectedEntry();
        if (selectedEntry == null) {
            statusLabel.setText(Component.empty());
            return;
        }
        var warningKey = controller.isSelectedContainsUnsupportedIngredients()
                ? "viscript_recipe.editor.status.unsupported_ingredient"
                : "viscript_recipe.editor.status.ready";
        statusLabel.setText(Component.translatable(
                "viscript_recipe.editor.status",
                controller.recipeFile().getEntries().size(),
                controller.recipeFile().getEntries().stream().filter(entry -> entry.isEnabled()).count(),
                Component.translatable(warningKey)
        ));
    }

    private void setSlot(ItemSlot slot, ItemStack stack) {
        slot.setItem(stack == null ? ItemStack.EMPTY : stack.copy(), false);
    }

    private void setItemTexture(UIElement element, ItemStack stack) {
        element.style(style -> style.backgroundTexture(new ItemStackTexture(stack == null ? ItemStack.EMPTY : stack.copy())));
    }

    private void setFluid(FluidSlot slot, FluidStack stack) {
        slot.setFluid(stack == null ? FluidStack.EMPTY : stack.copy(), false);
    }

    private ItemSlot createEditorSlot(int size) {
        return enableShiftDragCopy((ItemSlot) new ItemSlot()
                .xeiPhantom()
                .slotStyle(style -> style.showItemTooltips(true))
                .layout(layout -> {
                    layout.width(size);
                    layout.height(size);
                }));
    }

    private IngredientDisplaySlot createIngredientSlot(int size) {
        return enableShiftDragCopy((IngredientDisplaySlot) new IngredientDisplaySlot()
                .xeiPhantom()
                .slotStyle(style -> style.showItemTooltips(true))
                .layout(layout -> {
                    layout.width(size);
                    layout.height(size);
                }));
    }

    private <T extends ItemSlot> T enableShiftDragCopy(T slot) {
        slot.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            if (event.button != 0 || !Screen.hasShiftDown()) {
                return;
            }
            var stack = slot.getValue();
            if (stack.isEmpty()) {
                return;
            }

            var draggedStack = stack.copy();
            RecipeIngredient draggedIngredient = null;
            if (slot instanceof IngredientDisplaySlot ingredientSlot) {
                var ingredientIndex = ingredientDragSlotIndices.get(ingredientSlot);
                if (ingredientIndex != null) {
                    draggedIngredient = controller.snapshotVisualIngredient(ingredientIndex);
                }
            }
            var dragHandler = slot.startDrag(
                    new ItemSlotDragPayload(slot, draggedStack, draggedIngredient),
                    new ItemStackTexture(draggedStack)
            );
            dragHandler.setDragTexture(
                    -DRAGGED_ITEM_PREVIEW_SIZE / 2f,
                    -DRAGGED_ITEM_PREVIEW_SIZE / 2f,
                    DRAGGED_ITEM_PREVIEW_SIZE,
                    DRAGGED_ITEM_PREVIEW_SIZE
            );
            event.stopImmediatePropagation();
        });
        slot.addEventListener(UIEvents.DRAG_PERFORM, event -> {
            var draggingObject = event.dragHandler == null ? null : event.dragHandler.getDraggingObject();
            if (!(draggingObject instanceof ItemSlotDragPayload payload)) {
                return;
            }
            if (payload.source() == slot || payload.stack().isEmpty()) {
                event.stopPropagation();
                return;
            }

            if (slot instanceof IngredientDisplaySlot ingredientSlot && payload.ingredient() != null) {
                var ingredientIndex = ingredientDragSlotIndices.get(ingredientSlot);
                if (ingredientIndex != null) {
                    controller.setVisualIngredientFromDrag(ingredientIndex, payload.ingredient());
                    event.stopPropagation();
                    return;
                }
            }

            var copiedStack = payload.stack().copy();
            if (!slot.getSlot().mayPlace(copiedStack)) {
                event.stopPropagation();
                return;
            }
            if (slot instanceof IngredientDisplaySlot ingredientSlot) {
                ingredientSlot.clearTagDisplayStacks();
            }
            slot.setItem(copiedStack, true);
            event.stopPropagation();
        });
        return slot;
    }

    private static FluidSlot createFluidSlot() {
        return createFluidSlot(30);
    }

    private static FluidSlot createFluidSlot(int size) {
        return (FluidSlot) new FluidSlot()
                .xeiPhantom()
                .setAllowClickFilled(false)
                .setAllowClickDrained(false)
                .slotStyle(style -> style.showFluidTooltips(true))
                .layout(layout -> {
                    layout.width(size);
                    layout.height(size);
                });
    }

    private static FluidDisplaySlot createFluidDisplaySlot() {
        return (FluidDisplaySlot) new FluidDisplaySlot()
                .xeiPhantom()
                .setAllowClickFilled(false)
                .setAllowClickDrained(false)
                .slotStyle(style -> style.showFluidTooltips(true))
                .layout(layout -> {
                    layout.width(30);
                    layout.height(30);
                });
    }

    private record ItemSlotDragPayload(ItemSlot source, ItemStack stack, RecipeIngredient ingredient) {
    }

    private static UIElement createItemIcon(ItemStack stack, int size) {
        return new UIElement().layout(layout -> {
            layout.width(size);
            layout.height(size);
        }).style(style -> style.backgroundTexture(new ItemStackTexture(stack == null ? ItemStack.EMPTY : stack.copyWithCount(1))));
    }

    private static Label createOperatorPlusLabel() {
        Label label = RecipeEditorUi.label(Component.literal("+"));
        label.textStyle(style -> style.fontSize(22).textColor(ColorPattern.GRAY.color));
        label.layout(layout -> layout.width(22).height(24));
        return label;
    }

    private static UIElement createDownArrowElement(int width, int height) {
        return new UIElement().layout(layout -> {
            layout.width(width);
            layout.height(height);
        }).style(style -> style.backgroundTexture(Icons.DOWN_ARROW_NO_BAR));
    }

    private static UIElement createArrowElement() {
        return new UIElement().layout(layout -> {
            layout.width(28);
            layout.height(16);
        }).style(style -> style.backgroundTexture(Icons.ARROW_LEFT_RIGHT));
    }

    private static int createSequencedIngredientSlotIndex(int stepIndex) {
        return CREATE_SEQUENCED_STEP_INGREDIENT_OFFSET + stepIndex;
    }

    private static Item itemFromRegistry(String id, Item fallback) {
        var location = ResourceLocation.tryParse(id);
        if (location == null) {
            return fallback;
        }
        var item = BuiltInRegistries.ITEM.get(location);
        return item == Items.AIR ? fallback : item;
    }

}
