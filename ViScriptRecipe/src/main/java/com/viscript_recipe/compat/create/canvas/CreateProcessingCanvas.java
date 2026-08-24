package com.viscript_recipe.compat.create.canvas;

import com.lowdragmc.lowdraglib2.gui.ColorPattern;
import com.lowdragmc.lowdraglib2.gui.texture.FluidStackTexture;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.TextWrap;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Scene;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.utils.data.BlockInfo;
import com.lowdragmc.lowdraglib2.utils.virtuallevel.TrackedDummyWorld;
import com.viscript_recipe.compat.create.data.CreateHeatCondition;
import com.viscript_recipe.compat.create.data.CreateProcessingKind;
import com.viscript_recipe.compat.create.data.CreateProcessingRecipeData;
import com.viscript_recipe.data.FluidIngredientData;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.data.RecipeOutputData;
import com.viscript_recipe.gui.canvas.FluidRecipeCanvas;
import com.viscript_recipe.gui.editor.IngredientDisplaySlot;
import com.viscript_recipe.gui.editor.RecipeEditorUi;
import com.viscript_recipe.gui.editor.RecipeGridFactory;
import com.viscript_recipe.gui.editor.SlotSelection;
import com.viscript_recipe.gui.views.NavigationView;
import com.viscript_recipe.gui.views.PropertiesView;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

import static com.viscript_recipe.recipe.RecipeHelper.itemFromRegistry;

public class CreateProcessingCanvas extends FluidRecipeCanvas<CreateProcessingRecipeData> {
    static final int CREATE_MAX_ITEM_OUTPUTS = 15;
    static final int BLOCK_CUTTING_OUTPUT_COLUMNS = 5;
    static final int BLOCK_CUTTING_OUTPUT_ROWS = 3;
    static final TrackedDummyWorld manualApplicationPreviewWorld = new TrackedDummyWorld();
    static final Scene manualApplicationBlockScene = new Scene();

    public CreateProcessingCanvas(NavigationView navigationView, RecipeEntry entry) {super(navigationView, entry);}

    protected static CreateProcessingKind getCreateProcessingKind() {
        return CreateProcessingKind.byType(entry.getType()).orElse(null);
    }

    @Override
    public boolean ingredientHasCount(int slotIndex) {
        var kind = getCreateProcessingKind();
        return kind == CreateProcessingKind.MIXING || kind == CreateProcessingKind.COMPACTING
                || kind == CreateProcessingKind.AUTOMATIC_SHAPELESS;
    }

    @Override
    public void load() {
        var kind = getCreateProcessingKind();
        var data = getData();
        if (kind == CreateProcessingKind.AUTO_PACKING) {
            loadCreateAutoPacking(data);
            return;
        }
        var ingredients = data.getIngredients();
        for (int i = 0; i < ingredients.size(); i++) loadIngredientSlot(i, ingredients.get(i));

        var fluidInputs = data.getFluidIngredients();
        for (int i = 0; i < fluidInputs.size(); i++) setVisualFluidInput(i, fluidInputs.get(i).copy());

        var outputs = data.getOutputs();
        for (int i = 0; i < outputs.size(); i++) setVisualOutput(i, outputs.get(i).copy());

        var fluidOutputs = data.getFluidOutputs();
        for (int i = 0; i < fluidOutputs.size(); i++) setVisualFluidOutput(i, fluidOutputs.get(i).copy());
    }

    private void loadCreateAutoPacking(CreateProcessingRecipeData data) {
        var gridSize = autoPackingGridSize(data);
        var ingredient = data.getIngredients().isEmpty() ? RecipeIngredient.empty() : data.getIngredients().get(0);
        if (containsUnsupportedIngredientValue(ingredient)) containsUnsupportedIngredients = true;

        for (int i = 0; i < gridSize * gridSize; i++) setVisualIngredient(i, ingredient);
        var outputs = data.getOutputs();
        if (!outputs.isEmpty()) setVisualOutput(0, outputs.get(0));
    }

    public static int autoPackingGridSize(CreateProcessingRecipeData data) {
        var count = 0;
        for (var ingredient : data.getIngredients()) {
            if (!ingredient.isEmpty()) count++;
        }
        return count <= 4 ? 2 : 3;
    }

    @Override
    public void save() {
        var kind = getCreateProcessingKind();
        var data = getData();
        if (kind == CreateProcessingKind.AUTO_PACKING) {
            writeCreateAutoPackingRecipe(data);
            return;
        }
        var ingredients = new ArrayList<RecipeIngredient>();
        for (int i = 0; i < Math.min(9, kind.maxItemInputs()); i++) {
            var ingredient = getVisualIngredient(i);
            if (!ingredient.isEmpty()) ingredients.add(ingredient);
        }
        var fluidIngredients = new ArrayList<FluidIngredientData>();
        for (int i = 0; i < Math.min(CREATE_MAX_FLUID_INPUTS, kind.maxFluidInputs()); i++) {
            var input = getVisualFluidInput(i);
            if (!input.isEmpty()) fluidIngredients.add(input);
        }
        var outputs = new ArrayList<RecipeOutputData>();
        for (int i = 0; i < Math.min(CREATE_MAX_ITEM_OUTPUTS, kind.maxItemOutputs()); i++) {
            var output = getVisualOutput(i);
            if (!output.isEmpty()) outputs.add(output);
        }
        var fluidOutputs = new ArrayList<FluidStack>();
        for (int i = 0; i < Math.min(CREATE_MAX_FLUID_OUTPUTS, kind.maxFluidOutputs()); i++) {
            var stack = getVisualFluidOutput(i);
            if (!stack.isEmpty()) fluidOutputs.add(stack);
        }
        data.setIngredients(ingredients).setOutputs(outputs);
        data.setFluidIngredients(fluidIngredients).setFluidOutputs(fluidOutputs);
    }

    private void writeCreateAutoPackingRecipe(CreateProcessingRecipeData data) {
        int gridSize = autoPackingGridSize(data);
        fillAutoPackingInput(gridSize);
        var outputs = new ArrayList<>(List.of(getVisualOutput(0)));
        data.setFluidIngredients(new ArrayList<>());
        data.setOutputs(outputs);
        data.setFluidOutputs(new ArrayList<>());
    }

    @Override
    public void setVisualIngredient(int index, RecipeIngredient ingredient) {
        if (getCreateProcessingKind() == CreateProcessingKind.AUTO_PACKING && index == 0) {
            var gridSize = autoPackingGridSize(getData());
            for (int i = 0; i < gridSize * gridSize; i++) super.setVisualIngredient(i, ingredient);
        } else super.setVisualIngredient(index, ingredient);
    }

    @Override
    public UIElement createCanvas() {
        // var generic = createGenericCreateProcessingCanvas();
        var variant = switch (getCreateProcessingKind()) {
            case CRUSHING -> createCreateCrushingCanvas();
            case MILLING -> createCreateMillingCanvas();
            case CUTTING, BLOCK_CUTTING -> createCreateSawCanvas();
            case AUTO_PACKING -> createCreateAutoPackingCanvas();
            case SANDPAPER_POLISHING -> createCreateSandpaperCanvas();
            case AUTOMATIC_BREWING -> createCreateAutomaticBrewingCanvas();
            case MIXING, AUTOMATIC_SHAPELESS, COMPACTING -> createCreatePressBasinCanvas();
            case PRESSING -> createCreatePressingCanvas();
            case FILLING -> createCreateSpoutCanvas();
            case EMPTYING -> createCreateDrainCanvas();
            case DEPLOYING -> createCreateDeployerCanvas();
            case ITEM_APPLICATION -> createCreateManualApplicationCanvas();
            case BLASTING, SMOKING, HAUNTING, SPLASHING -> createCreateFanCanvas();
        };
        return CreateProcessingCanvasFactory.createProcessingStack(/*generic, */variant);
    }

    @Override
    public void buildRecipeProperties(UIElement content) {
        var kind = getCreateProcessingKind();
        content.addChild(sectionTitle("viscript_recipe.editor.properties.create.processing"));
        var data = getData();
        if (kind == CreateProcessingKind.AUTO_PACKING) {
            content.addChild(selector("viscript_recipe.config.create.auto_packing.grid_size",
                    List.of(2, 3), autoPackingGridSize(data), value ->
                            Component.translatable("viscript_recipe.editor.create.auto_packing.grid_" + (value <= 2 ? 2 : 3)),
                    this::fillAutoPackingInput, CreateProcessingCanvas::reloadCanvas
            ));
        }
        if (kind.durationAllowed()) {
            content.addChild(intField("viscript_recipe.config.create.processing_time",
                    data.getProcessingTime(), 0, Integer.MAX_VALUE, data::setProcessingTime));
        }
        if (kind.heatAllowed()) {
            content.addChild(selector("viscript_recipe.config.create.heat_requirement",
                    List.of(CreateHeatCondition.values()), data.getHeatRequirement(),
                    CreateHeatCondition::displayName, data::setHeatRequirement, CreateProcessingCanvas::reloadCanvas
            ));
        }
        if (kind.keepHeldItemAllowed()) {
            content.addChild(switchField("viscript_recipe.config.create.keep_held_item",
                    data.isKeepHeldItem(), data::setKeepHeldItem));
        }
    }

    private void fillAutoPackingInput(int gridSize) {
        var data = getData();
        data.getIngredients().clear();
        gridSize = gridSize <= 2 ? 2 : 3;
        var ingredient = getVisualIngredient(0);
        for (int i = 0; i < gridSize * gridSize; i++) data.getIngredients().add(ingredient);
    }

    @Override
    public void buildResultProperties(UIElement content) {
        content.addChildren(sectionTitle("viscript_recipe.editor.properties.create.output"),
                PropertiesView.createItemStackConfigurator("viscript_recipe.config.create.output.item",
                        () -> getSelectedOutput().getItem(), this::setSelectedOutput
                )
        );
        if (outputWithChance()) content.addChild(floatField("viscript_recipe.config.create.output.chance",
                getSelectedOutput().getChance(), 0, 1, this::setSelectedOutput));
    }

    public boolean outputWithChance() {
        var kind = getCreateProcessingKind();
        return kind != CreateProcessingKind.BLOCK_CUTTING
                && kind != CreateProcessingKind.BLASTING && kind != CreateProcessingKind.SMOKING;
    }

    private UIElement createGenericCreateProcessingCanvas() {
        return CreateProcessingCanvasFactory.createGenericProcessingCanvas(
                createCreateInputSide(), createCreateMachineColumn(), createCreateOutputSide()
        );
    }

    private UIElement createCreateSpoutCanvas() {
        return CreateProcessingCanvasFactory.createSpoutCanvas(
                createFluidInputSlot(0),
                createIngredientSlot(0, SLOT_SIZE),
                createOutputSlot(0, OUTPUT_SLOT_SIZE)
        );
    }

    private UIElement createCreateDrainCanvas() {
        return CreateProcessingCanvasFactory.createDrainCanvas(
                createIngredientSlot(0, SLOT_SIZE),
                createFluidOutputSlot(0),
                createOutputSlot(0, OUTPUT_SLOT_SIZE)
        );
    }

    private UIElement createCreateFanCanvas() {
        var fanCatalystIcon = new UIElement();
        var fanCatalystLabel = RecipeEditorUi.label(Component.empty());
        fanCatalystLabel.textStyle(style -> style
                .textAlignHorizontal(Horizontal.CENTER)
                .textColor(ColorPattern.WHITE.color)
                .textWrap(TextWrap.HOVER_ROLL));
        fanCatalystLabel.layout(layout -> {
            layout.widthPercent(100);
            layout.height(18);
        });
        var kind = getCreateProcessingKind();
        updateCreateFanCatalyst(kind, fanCatalystIcon, fanCatalystLabel);
        boolean bl = isCreateFanSingleOutputKind(kind);
        var singleSlot = bl ? createOutputSlot(0, OUTPUT_SLOT_SIZE) : null;
        var multiSlots = bl ? null : createCreateFanOutputGrid();
        return CreateProcessingCanvasFactory.createFanCanvas(
                createIngredientSlot(0, SLOT_SIZE),
                fanCatalystIcon, fanCatalystLabel,
                singleSlot, multiSlots
        );
    }

    private void updateCreateFanCatalyst(CreateProcessingKind kind, UIElement icon, Label label) {
        var catalystPath = createFanCatalystPath(kind);
        label.setText(Component.translatable("viscript_recipe.editor.create.fan.catalyst." + catalystPath));
        icon.style(style -> style
                .backgroundTexture(createFanCatalystTexture(kind))
                .tooltips(Component.translatable("viscript_recipe.editor.create.fan.catalyst." + catalystPath)));
    }

    private static boolean isCreateFanSingleOutputKind(CreateProcessingKind kind) {
        return kind == CreateProcessingKind.BLASTING || kind == CreateProcessingKind.SMOKING;
    }

    private static String createFanCatalystPath(CreateProcessingKind kind) {
        return switch (kind) {
            case BLASTING -> "blasting";
            case HAUNTING -> "haunting";
            case SMOKING -> "smoking";
            default -> "splashing";
        };
    }

    private static IGuiTexture createFanCatalystTexture(CreateProcessingKind kind) {
        return switch (kind) {
            case BLASTING -> new FluidStackTexture(new FluidStack(Fluids.LAVA, 1000));
            case HAUNTING -> new ItemStackTexture(new ItemStack(Items.SOUL_CAMPFIRE));
            case SMOKING -> new ItemStackTexture(new ItemStack(Items.CAMPFIRE));
            default -> new FluidStackTexture(new FluidStack(Fluids.WATER, 1000));
        };
    }

    private UIElement createCreateFanOutputGrid() {
        return RecipeGridFactory.borderedGrid(3, 4, SLOT_SIZE, true, null,
                (index, row, col) -> RecipeGridFactory.slotCell(createOutputSlot(index, SLOT_SIZE), SLOT_SIZE));
    }

    private UIElement createCreateCrushingCanvas() {
        return CreateProcessingCanvasFactory.createCrushingCanvas(
                createIngredientSlot(0, SLOT_SIZE), createCreateCrushingOutputRow()
        );
    }

    private UIElement createCreateCrushingOutputRow() {
        return RecipeGridFactory.borderedRow(/*createCrushingOutputSlots.length*/7, SLOT_SIZE, i -> {
            var slot = createOutputSlot(i, SLOT_SIZE);
            return RecipeGridFactory.slotCell(slot, SLOT_SIZE);
        });
    }

    private UIElement createCreateMillingCanvas() {
        return CreateProcessingCanvasFactory.createMillingCanvas(
                createIngredientSlot(0, SLOT_SIZE), createCreateMillingOutputRow()
        );
    }

    private UIElement createCreateMillingOutputRow() {
        return RecipeGridFactory.borderedRow(/*createMillingOutputSlots.length*/ 4, SLOT_SIZE, i -> {
            var slot = createOutputSlot(i, SLOT_SIZE);
            return RecipeGridFactory.slotCell(slot, SLOT_SIZE);
        });
    }

    private UIElement createCreateSawCanvas() {
        boolean blockCutting = getCreateProcessingKind() == CreateProcessingKind.BLOCK_CUTTING;
        int columns = blockCutting ? BLOCK_CUTTING_OUTPUT_COLUMNS : 2;
        int rows = blockCutting ? BLOCK_CUTTING_OUTPUT_ROWS : 2;
        var outputGrid = RecipeGridFactory.borderedGrid(columns, rows, SLOT_SIZE, true, null,
                (index, row, col) -> RecipeGridFactory.slotCell(createOutputSlot(index, SLOT_SIZE), SLOT_SIZE));
        return CreateProcessingCanvasFactory.createSawCanvas(createIngredientSlot(0, SLOT_SIZE), outputGrid);
    }

    private UIElement createCreateAutoPackingCanvas() {
        return CreateProcessingCanvasFactory.createAutoPackingCanvas(
                createCreateAutoPackingInputGrid(),
                createOutputSlot(0, OUTPUT_SLOT_SIZE)
        );
    }

    private UIElement createCreateAutoPackingInputGrid() {
        int size = autoPackingGridSize(getData());
        return RecipeGridFactory.borderedGrid(
                size, size, SLOT_SIZE, true, null,
                (index, row, col) -> {
                    var slot = createIngredientSlot(index, SLOT_SIZE);
                    if (index != 0) { // 全部操作第一个格子
                        removeUIFirstEvent(slot, UIEvents.MOUSE_DOWN, false);
                        slot.addEventListener(UIEvents.MOUSE_DOWN, event -> {
                            if (event.button == 1) {
                                setVisualIngredient(0, RecipeIngredient.empty());
                                event.stopPropagation();
                            }
                            selectSlot(SlotSelection.ingredient(0));
                        });
                    }
                    return RecipeGridFactory.slotCell(slot, SLOT_SIZE);
                }
        );
    }

    private UIElement createCreateSandpaperCanvas() {
        return CreateProcessingCanvasFactory.createSandpaperCanvas(
                createIngredientSlot(0, SLOT_SIZE), createOutputSlot(0, OUTPUT_SLOT_SIZE)
        );
    }

    private UIElement createCreatePressBasinCanvas() {
        var createPressHeatLabel = RecipeEditorUi.label(getData().getHeatRequirement().displayName());
        createPressHeatLabel.textStyle(style -> style
                .fontSize(16)
                .textColor(ColorPattern.WHITE.color)
                .textAlignHorizontal(Horizontal.CENTER)
                .textWrap(TextWrap.HOVER_ROLL));
        createPressHeatLabel.layout(layout -> {
            layout.widthPercent(100);
            layout.height(24);
        });
        var createPressHeatPanel = CreateProcessingCanvasFactory.createPressHeatPanel(createPressHeatLabel);
        return CreateProcessingCanvasFactory.createPressBasinCanvas(
                createCreatePressInputSide(),
                createCreatePressMachineStack(),
                createCreatePressOutputSide(),
                createPressHeatPanel
        );
    }

    private UIElement createCreateAutomaticBrewingCanvas() {
        var createAutomaticBrewingHeatLabel = RecipeEditorUi.label(getData().getHeatRequirement().displayName());
        createAutomaticBrewingHeatLabel.textStyle(style -> style
                .fontSize(16)
                .textColor(ColorPattern.WHITE.color)
                .textAlignHorizontal(Horizontal.CENTER)
                .textWrap(TextWrap.HOVER_ROLL));
        createAutomaticBrewingHeatLabel.layout(layout -> {
            layout.widthPercent(100);
            layout.height(24);
        });
        var createAutomaticBrewingHeatPanel = CreateProcessingCanvasFactory.createPressHeatPanel(createAutomaticBrewingHeatLabel);
        return CreateProcessingCanvasFactory.createAutomaticBrewingCanvas(
                createIngredientSlot(0, SLOT_SIZE),
                createFluidInputSlot(0),
                createFluidOutputSlot(0),
                createAutomaticBrewingHeatPanel
        );
    }

    private UIElement createCreatePressInputSide() {
        return CreateProcessingCanvasFactory.createPressInputSide(
                createCreatePressItemInputGrid(), createCreatePressFluidInputs()
        );
    }

    private UIElement createCreatePressItemInputGrid() {
        return RecipeGridFactory.borderedGrid(3, 3, SLOT_SIZE, false, null, (index, row, col) -> {
            var slot = createIngredientSlot(index, SLOT_SIZE);
            return RecipeGridFactory.slotCell(slot, SLOT_SIZE);
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
        for (int i = 0; i < getCreateProcessingKind().maxFluidInputs(); i++) {
            var slot = createFluidInputSlot(i);
            var column = RecipeEditorUi.column().layout(layout -> {
                layout.width(46);
                layout.gapAll(2);
                layout.alignItems(AlignItems.CENTER);
            }).addChildren(
                    RecipeEditorUi.label(Component.translatable("viscript_recipe.editor.create.fluid_input_short")),
                    slot
            );
            row.addChild(column);
        }
        return row;
    }

    private UIElement createCreatePressMachineStack() {
        return CreateProcessingCanvasFactory.createPressMachineStack(getCreateBasinMachine(getCreateProcessingKind()));
    }

    private static UIElement getCreateBasinMachine(CreateProcessingKind kind) {
        var machineItem = isCreateMixerKind(kind) ? "create:mechanical_mixer" : "create:mechanical_press";
        return new UIElement().style(style -> style.backgroundTexture(new ItemStackTexture(new ItemStack(itemFromRegistry(machineItem, Items.CRAFTING_TABLE)))));
    }

    private static boolean isCreateMixerKind(CreateProcessingKind kind) {
        return kind == CreateProcessingKind.MIXING
                || kind == CreateProcessingKind.AUTOMATIC_SHAPELESS
                || kind == CreateProcessingKind.AUTOMATIC_BREWING;
    }

    private UIElement createCreatePressOutputSide() {
        return CreateProcessingCanvasFactory.createPressOutputSide(
                createCreatePressItemOutputs(), createCreatePressFluidOutputs()
        );
    }

    private UIElement createCreatePressItemOutputs() {
        return RecipeGridFactory.borderedGrid(2, 2, SLOT_SIZE, true, null, (index, row, col) -> {
            var slot = createOutputSlot(index, SLOT_SIZE);
            return RecipeGridFactory.slotCell(slot, SLOT_SIZE);
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
        for (int i = 0; i < getCreateProcessingKind().maxFluidOutputs(); i++) {
            var slot = createFluidOutputSlot(i);
            var column = RecipeEditorUi.column().layout(layout -> {
                layout.width(52);
                layout.gapAll(2);
                layout.alignItems(AlignItems.CENTER);
            }).addChildren(
                    RecipeEditorUi.label(Component.translatable("viscript_recipe.editor.create.fluid_output_short")),
                    slot
            );
            row.addChild(column);
        }
        return row;
    }

    private UIElement createCreatePressingCanvas() {
        return CreateProcessingCanvasFactory.createPressingCanvas(
                createIngredientSlot(0, SLOT_SIZE), createCreatePressingOutputRow()
        );
    }

    private UIElement createCreatePressingOutputRow() {
        return RecipeGridFactory.borderedRow(/*createPressingOutputSlots.length*/2, SLOT_SIZE, i -> {
            var slot = createOutputSlot(i, SLOT_SIZE);
            return RecipeGridFactory.slotCell(slot, SLOT_SIZE);
        });
    }

    private UIElement createCreateDeployerCanvas() {
        return CreateProcessingCanvasFactory.createDeployerCanvas(
                createIngredientSlot(1, SLOT_SIZE),
                createIngredientSlot(0, SLOT_SIZE),
                createCreateDeployerOutputGrid()
        );
    }

    private UIElement createCreateDeployerOutputGrid() {
        return RecipeGridFactory.borderedGrid(2, 2, SLOT_SIZE, true, null, (index, row, col) -> {
            var slot = createOutputSlot(index, SLOT_SIZE);
            return RecipeGridFactory.slotCell(slot, SLOT_SIZE);
        });
    }

    private UIElement createCreateManualApplicationCanvas() {
        var blockSlot = createIngredientSlot(0, SLOT_SIZE);
        var heldSlot = createIngredientSlot(1, SLOT_SIZE);
        blockSlot.registerValueListener(stack -> {
            Block block = stack.getItem() instanceof BlockItem item ? item.getBlock() : Blocks.AIR;
            manualApplicationPreviewWorld.clear();
            manualApplicationPreviewWorld.addBlock(BlockPos.ZERO, BlockInfo.fromBlock(block));
            manualApplicationBlockScene.setRenderedCore(List.of(BlockPos.ZERO)).setZoom(1.15f);
        });
        return CreateProcessingCanvasFactory.createManualApplicationCanvas(
                blockSlot,
                createCreateManualApplicationProcessStack(heldSlot),
                createCreateManualApplicationOutputGrid()
        );
    }

    private UIElement createCreateManualApplicationProcessStack(IngredientDisplaySlot heldSlot) {
        manualApplicationBlockScene
                .createScene(manualApplicationPreviewWorld)
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
                heldSlot, createDownArrowElement(36, 24), manualApplicationBlockScene
        );
    }

    private UIElement createCreateManualApplicationOutputGrid() {
        return RecipeGridFactory.borderedGrid(2, 2, SLOT_SIZE, true, null, (index, row, col) -> {
            var slot = createOutputSlot(index, SLOT_SIZE);
            return RecipeGridFactory.slotCell(slot, SLOT_SIZE);
        });
    }

    private UIElement createCreateInputSide() {
        return CreateProcessingCanvasFactory.createInputSide(
                createCreateItemInputGrid(), createCreateFluidInputs()
        );
    }

    private UIElement createCreateItemInputGrid() {
        return RecipeGridFactory.borderedGrid(3, 3, SLOT_SIZE, (index, row, col) -> {
            var slot = createIngredientSlot(index, SLOT_SIZE);
            return RecipeGridFactory.slotCell(slot, SLOT_SIZE);
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
        for (int i = 0; i < /*fluidInputSlots.length*/2; i++) {
            var slot = createFluidInputSlot(i);
            var column = RecipeEditorUi.column().layout(layout -> {
                layout.width(46);
                layout.gapAll(2);
                layout.alignItems(AlignItems.CENTER);
            }).addChildren(
                    RecipeEditorUi.label(Component.translatable("viscript_recipe.editor.create.fluid_input_short")),
                    slot
            );
            row.addChild(column);
        }
        return row;
    }

    private UIElement createCreateMachineColumn() {
        CreateProcessingKind kind = getCreateProcessingKind();
        var createMachineLabel = RecipeEditorUi.label(Component.translatable("viscript_recipe.editor.type.create." + kind.translationPath()));
        var createMachineIcon = new UIElement().style(style -> style.backgroundTexture(new ItemStackTexture(new ItemStack(itemFromRegistry(kind.machineItemId(), Items.CRAFTING_TABLE)))));
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
                createCreateItemOutputGrid(), createCreateFluidOutputs()
        );
    }

    private UIElement createCreateItemOutputGrid() {
        return RecipeGridFactory.borderedGrid(4, 3, SLOT_SIZE, (index, row, col) -> {
            var slot = createOutputSlot(index, SLOT_SIZE);
            return RecipeGridFactory.slotCell(slot, SLOT_SIZE);
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
        for (int i = 0; i < /*fluidOutputSlots.length*/2; i++) {
            var slot = createFluidOutputSlot(i);
            var column = RecipeEditorUi.column().layout(layout -> {
                layout.width(52);
                layout.gapAll(2);
                layout.alignItems(AlignItems.CENTER);
            }).addChildren(
                    RecipeEditorUi.label(Component.translatable("viscript_recipe.editor.create.fluid_output_short")),
                    slot
            );
            row.addChild(column);
        }
        return row;
    }
}
