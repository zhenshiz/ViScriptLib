package com.viscript_recipe.compat.mekanism.canvas;

import com.lowdragmc.lowdraglib2.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.FluidSlot;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.gui.ui.rendering.GUIContext;
import com.lowdragmc.lowdraglib2.gui.ui.styletemplate.Sprites;
import com.viscript_recipe.ViScriptRecipe;
import com.viscript_recipe.compat.mekanism.data.MekanismChemicalIngredientData;
import com.viscript_recipe.compat.mekanism.data.MekanismChemicalStackData;
import com.viscript_recipe.compat.mekanism.data.MekanismRecipeKind;
import com.viscript_recipe.gui.editor.FluidDisplaySlot;
import com.viscript_recipe.gui.editor.IngredientDisplaySlot;
import com.viscript_recipe.gui.editor.RecipeEditorUi;
import com.viscript_recipe.gui.editor.SlotSelection;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.TaffyPosition;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/** Builds JEI-proportioned Mekanism recipe previews for every supported recipe codec. */
public final class MekanismCanvasFactory {
    private static final String MEKANISM_MOD_ID = "mekanism";
    private static final String JEI_MOD_ID = "jei";
    private static final int WIDTH = 190;
    private static final int HEIGHT = 100;
    private static final int CHEMICAL_MIXING_WIDTH = 178;
    private static final int CHEMICAL_MIXING_HEIGHT = 88;
    private static final int CHEMICAL_MIXING_OFFSET = 1;
    private static final int WASHING_WIDTH = 162;
    private static final int WASHING_HEIGHT = 60;
    private static final int WASHING_X_OFFSET = -7;
    private static final int WASHING_Y_OFFSET = -13;
    private static final int ITEM_CHEMICAL_WIDTH = 144;
    private static final int ITEM_CHEMICAL_HEIGHT = 54;
    private static final int METALLURGIC_INFUSING_WIDTH = 174;
    private static final int METALLURGIC_INFUSING_HEIGHT = 62;
    private static final int ROTARY_WIDTH = 178;
    private static final int ROTARY_HEIGHT = 72;
    private static final int ROTARY_X_OFFSET = 1;
    private static final int ROTARY_Y_OFFSET = -8;
    private static final int SLOT_SIZE = 18;
    static final int TANK_WIDTH = 18;
    static final int TANK_HEIGHT = 60;
    private static final int COMPACT_CHEMICAL_WIDTH = 6;
    private static final int COMPACT_CHEMICAL_HEIGHT = 12;
    private static final int NUCLEOSYNTHESIZER_HEIGHT = 88;
    private static final int SMALL_MED_GAUGE_WIDTH = 18;
    private static final int SMALL_MED_GAUGE_HEIGHT = 48;

    public static final int CHEMICAL_INPUT_SLOT = 0;
    public static final int EXTRA_CHEMICAL_INPUT_SLOT = 1;
    public static final int CHEMICAL_OUTPUT_SLOT = 2;
    public static final int SECONDARY_CHEMICAL_OUTPUT_SLOT = 3;
    public static final int FLUID_INPUT_SLOT = 0;
    public static final int FLUID_OUTPUT_SLOT = 2;

    public static UIElement createCanvas(
            MekanismCanvas canvas, IngredientDisplaySlot[] itemInputs, ItemSlot[] itemOutputs,
            FluidSlot fluidInput, FluidSlot fluidOutput
    ) {
        return new CanvasBuilder(canvas, itemInputs, itemOutputs, fluidInput, fluidOutput).root;
    }

    static final class CanvasBuilder {
        private final MekanismCanvas canvas;
        private final UIElement root;
        private final UIElement panel;
        private final UIElement[] itemInputs = new UIElement[2];
        private final UIElement[] itemOutputs = new UIElement[2];
        private final ItemSlot[] itemOutputSlots = new ItemSlot[2];
        private final FluidSlot fluidInput;
        private final FluidSlot fluidOutput;
        private final ChemicalDisplay[] chemicalInputs = {
                chemicalGauge("red.png"), chemicalGauge("orange.png")
        };
        private final ChemicalDisplay[] chemicalOutputs = {
                chemicalGauge("blue.png"), chemicalGauge("aqua.png")
        };
        private final ChemicalDisplay nucleosynthesizerChemicalInput =
                new ChemicalDisplay("red.png", "small_med.png", 16, 46);
        private final ChemicalDisplay compactChemicalInput =
                chemicalBar(COMPACT_CHEMICAL_WIDTH, COMPACT_CHEMICAL_HEIGHT);
        private final ChemicalDisplay metallurgicChemicalInput = chemicalBar(4, 52);
        private final UIElement leftChemicalDrainSlot = chemicalControlSlot("input.png", "overlay_minus.png");
        private final UIElement rightChemicalDrainSlot = chemicalControlSlot("input_2.png", "overlay_minus.png");
        private final UIElement chemicalFillSlot = chemicalControlSlot("output.png", "overlay_plus.png");
        private final UIElement washingOutputDrainSlot = chemicalControlSlot("output.png", "overlay_minus.png");
        private final UIElement rotaryLeftInputSlot = machineControlSlot("input.png", "overlay_plus.png");
        private final UIElement rotaryLeftOutputSlot = machineControlSlot("output.png", "overlay_minus.png");
        private final UIElement rotaryRightInputSlot = machineControlSlot("input.png", null);
        private final UIElement rotaryRightOutputSlot = machineControlSlot("output.png", null);
        private final UIElement progressBar = progress("bar.png", 25, 9);
        private final UIElement largeRightProgress = progress("large_right.png", 48, 8);
        private final UIElement largeLeftProgress = progress("large_left.png", 48, 8);
        private final UIElement rightProgress = progress("right.png", 32, 8);
        private final UIElement smallRightProgress = progress("small_right.png", 28, 8);
        private final UIElement smallLeftProgress = progress("small_left.png", 28, 8);
        private final PigmentProgressTexture pigmentRightProgressTexture =
                new PigmentProgressTexture("small_right.png", false);
        private final PigmentProgressTexture pigmentLeftProgressTexture =
                new PigmentProgressTexture("small_left.png", true);
        private final UIElement pigmentRightProgress = new UIElement().style(
                style -> style.backgroundTexture(pigmentRightProgressTexture)
        );
        private final UIElement pigmentLeftProgress = new UIElement().style(
                style -> style.backgroundTexture(pigmentLeftProgressTexture)
        );
        private final UIElement chemicalMixingShapelessIcon = shapelessIcon();
        private final UIElement bidirectionalProgress = progress("bidirectional.png", 16, 6);
        private final UIElement upArrow = mekanismGui("up_arrow.png", 8, 10);
        private final UIElement downArrow = mekanismGui("down_arrow.png", 8, 9);
        private final UIElement sawmillOutputFrame = mekanismGui("slot/output_wide.png", 42, 26);
        private final UIElement powerSlot = powerSlot();
        private final IngredientDisplaySlot chemicalCatalystItem = new IngredientDisplaySlot();
        private final UIElement chemicalCatalystSlot = slotCell(chemicalCatalystItem, "extra.png");
        private final UIElement powerBar = bar(7, TANK_HEIGHT, 0xFF76EE93);
        private final UIElement itemChemicalPowerBar = verticalPowerBar();
        private final UIElement horizontalPowerBar = horizontalPowerBar();
        private final UIElement energyGauge = energyGauge("standard.png", 16, 58);
        private final UIElement nucleosynthesizerEnergyGauge = energyGauge("small_med.png", 16, 46);
        private final UIElement rateBar = dynamicRateBar();
        private final UIElement statusScreen = new UIElement().style(style -> style.backgroundTexture(
                mekanismTexture("inner_screen.png", 0, 0, 256, 256).setBorder(32)
        ));

        CanvasBuilder(MekanismCanvas canvas, IngredientDisplaySlot[] inputs, ItemSlot[] outputs,
                              FluidSlot inputFluid, FluidSlot outputFluid) {
            this.canvas = canvas;
            fluidInput = inputFluid;
            fluidOutput = outputFluid;
            for (int index = 0; index < itemInputs.length; index++) {
                itemInputs[index] = slotCell(inputs[index], index == 0 ? "input.png" : "extra.png");
            }
            itemOutputSlots[0] = outputs[0];
            itemOutputs[0] = slotCell(outputs[0], "output.png");
            itemOutputSlots[1] = outputs[1];
            itemOutputs[1] = slotCell(outputs[1], "output_2.png");
            canvas.bindChemicalDisplay(0, chemicalInputs[0]);
            configureChemicalSlot(chemicalInputs[0].element(), CHEMICAL_INPUT_SLOT);
            canvas.bindChemicalDisplay(1, chemicalInputs[1]);
            configureChemicalSlot(chemicalInputs[1].element(), EXTRA_CHEMICAL_INPUT_SLOT);
            canvas.bindChemicalDisplay(2, chemicalOutputs[0]);
            configureChemicalSlot(chemicalOutputs[0].element(), CHEMICAL_OUTPUT_SLOT);
            canvas.bindChemicalDisplay(3, chemicalOutputs[1]);
            configureChemicalSlot(chemicalOutputs[1].element(), SECONDARY_CHEMICAL_OUTPUT_SLOT);
            configureChemicalSlot(nucleosynthesizerChemicalInput.element(), CHEMICAL_INPUT_SLOT);
            configureChemicalSlot(compactChemicalInput.element(), CHEMICAL_INPUT_SLOT);
            configureChemicalSlot(metallurgicChemicalInput.element(), CHEMICAL_INPUT_SLOT);
            configureChemicalSlot(leftChemicalDrainSlot, CHEMICAL_INPUT_SLOT);
            configureChemicalSlot(rightChemicalDrainSlot, EXTRA_CHEMICAL_INPUT_SLOT);
            configureChemicalSlot(chemicalFillSlot, CHEMICAL_OUTPUT_SLOT);
            configureRotaryControlSlot(rotaryLeftInputSlot, true);
            configureRotaryControlSlot(rotaryLeftOutputSlot, true);
            configureRotaryControlSlot(rotaryRightInputSlot, false);
            configureRotaryControlSlot(rotaryRightOutputSlot, false);
            chemicalCatalystSlot.addEventListener(UIEvents.MOUSE_DOWN, event -> {
                canvas.selectChemicalSlot(0);
                event.stopPropagation();
            });

            panel = new UIElement().layout(layout -> {
                layout.positionType(TaffyPosition.RELATIVE);
                layout.width(WIDTH);
                layout.height(HEIGHT);
            }).style(style -> style.backgroundTexture(canvasBackground()));
            panel.addChildren(
                    itemInputs[0], itemInputs[1], sawmillOutputFrame, itemOutputs[0], itemOutputs[1],
                    fluidInput, fluidOutput,
                    chemicalInputs[0].element(), chemicalInputs[1].element(),
                    chemicalOutputs[0].element(), chemicalOutputs[1].element(),
                    nucleosynthesizerChemicalInput.element(), compactChemicalInput.element(),
                    metallurgicChemicalInput.element(), chemicalCatalystSlot,
                    leftChemicalDrainSlot, rightChemicalDrainSlot, chemicalFillSlot, washingOutputDrainSlot,
                    rotaryLeftInputSlot, rotaryLeftOutputSlot, rotaryRightInputSlot, rotaryRightOutputSlot,
                    progressBar, largeRightProgress, largeLeftProgress, rightProgress,
                    smallRightProgress, smallLeftProgress,
                    pigmentRightProgress, pigmentLeftProgress, chemicalMixingShapelessIcon,
                    bidirectionalProgress,
                    upArrow, downArrow, powerSlot, powerBar, itemChemicalPowerBar, horizontalPowerBar,
                    energyGauge, nucleosynthesizerEnergyGauge, rateBar, statusScreen
            );
            root = RecipeEditorUi.row().layout(layout -> {
                layout.widthPercent(100);
                layout.flex(1);
                layout.minWidth(0);
                layout.minHeight(0);
                layout.alignItems(AlignItems.CENTER);
                layout.justifyContent(AlignContent.CENTER);
            }).addChild(panel);
            canvas.refresh = this::refreshChemicalValues;
            refresh();
        }

        private void refresh() {
            var kind = canvas.kind();
            for (var element : panel.getChildren()) element.setDisplay(false);
            resizePanel(WIDTH, HEIGHT);
            boolean sawing = kind.hasSecondaryChance();
            configureItemOutputSlot(itemOutputSlots[0], sawing ? null : "output.png", SLOT_SIZE);
            configureItemOutputSlot(itemOutputSlots[1], sawing ? null : "output_2.png", SLOT_SIZE);
            switch (kind) {
                case CRUSHING, ENRICHING, SMELTING -> layoutStandardItem();
                case CHEMICAL_INFUSING -> layoutChemicalMixing(false);
                case PIGMENT_MIXING -> layoutChemicalMixing(true);
                case COMBINING -> layoutCombiner();
                case SEPARATING -> layoutSeparating();
                case WASHING -> layoutWashing();
                case EVAPORATING -> layoutEvaporating();
                case ACTIVATING, CENTRIFUGING -> layoutChemicalConversion();
                case CRYSTALLIZING -> layoutCrystallizing();
                case DISSOLUTION -> layoutDissolution();
                case COMPRESSING, PURIFYING, INJECTING -> layoutItemChemicalMachine();
                case NUCLEOSYNTHESIZING -> layoutNucleosynthesizing();
                case ENERGY_CONVERSION -> layoutEnergyConversion();
                case GAS_CONVERSION, INFUSION_CONVERSION, OXIDIZING, PIGMENT_EXTRACTING -> layoutItemToChemical();
                case METALLURGIC_INFUSING -> layoutMetallurgicInfusing();
                case PAINTING -> layoutPainting();
                case REACTION -> layoutReaction();
                case CONDENSENTRATING -> layoutRotary(true);
                case DECONDENSENTRATING -> layoutRotary(false);
                case SAWING -> layoutSawmill();
            }
        }

        private void refreshChemicalValues() {
            var kind = canvas.kind(); var data = canvas.getData();
            nucleosynthesizerChemicalInput.setInput(
                    kind == MekanismRecipeKind.NUCLEOSYNTHESIZING ? data.getChemicalInput() : null
            );
            compactChemicalInput.setInput(data.getChemicalInput());
            metallurgicChemicalInput.setInput(data.getChemicalInput());
            if (kind == MekanismRecipeKind.PIGMENT_MIXING) {
                int outputColor = chemicalColor(data.getChemicalOutput());
                pigmentRightProgressTexture.setColors(chemicalColor(data.getChemicalInput()), outputColor);
                pigmentLeftProgressTexture.setColors(chemicalColor(data.getExtraChemicalInput()), outputColor);
            }
            var showChemicalCatalyst = switch (kind) {
                case COMPRESSING, PURIFYING, INJECTING, NUCLEOSYNTHESIZING, METALLURGIC_INFUSING -> true;
                default -> false;
            };
            setChemicalCatalysts(chemicalCatalystItem, showChemicalCatalyst ? data.getChemicalInput() : null);
        }

        private void layoutStandardItem() {
            place(itemInputs[0], 64, 17);
            place(itemOutputs[0], 116, 35);
            place(upArrow, 68, 38, 8, 10);
            place(progressBar, 86, 38, 25, 9);
            place(powerSlot, 64, 53);
            place(powerBar, 164, 15, 7, TANK_HEIGHT);
        }

        private void layoutCombiner() {
            place(itemInputs[0], 64, 17);
            place(itemInputs[1], 64, 53);
            place(itemOutputs[0], 116, 35);
            place(upArrow, 68, 38, 8, 10);
            place(progressBar, 86, 38, 25, 9);
            place(powerSlot, 39, 35);
            place(powerBar, 164, 15, 7, TANK_HEIGHT);
        }

        private void layoutChemicalMixing(boolean pigmentMixing) {
            resizePanel(CHEMICAL_MIXING_WIDTH, CHEMICAL_MIXING_HEIGHT);
            // JEI's category offset (-3, -3) plus the four-pixel recipe background padding.
            placeWithChemicalOffset(chemicalInputs[0].element(), 25, 13, TANK_WIDTH, TANK_HEIGHT);
            placeWithChemicalOffset(chemicalInputs[1].element(), 133, 13, TANK_WIDTH, TANK_HEIGHT);
            placeWithChemicalOffset(chemicalOutputs[0].element(), 79, 4, TANK_WIDTH, TANK_HEIGHT);
            placeWithChemicalOffset(leftChemicalDrainSlot, 5, 55, SLOT_SIZE, SLOT_SIZE);
            placeWithChemicalOffset(rightChemicalDrainSlot, 153, 55, SLOT_SIZE, SLOT_SIZE);
            placeWithChemicalOffset(chemicalFillSlot, 79, 64, SLOT_SIZE, SLOT_SIZE);
            placeWithChemicalOffset(pigmentMixing ? pigmentRightProgress : smallRightProgress,
                    47, 39, 28, 8);
            placeWithChemicalOffset(pigmentMixing ? pigmentLeftProgress : smallLeftProgress,
                    101, 39, 28, 8);
            placeWithChemicalOffset(powerSlot, 153, 13, SLOT_SIZE, SLOT_SIZE);
            placeWithChemicalOffset(horizontalPowerBar, 115, 75, 54, 6);
            placeWithChemicalOffset(chemicalMixingShapelessIcon, 162, 0, 8, 8);
        }

        private void layoutSeparating() {
            place(fluidInput, 7, 4, TANK_WIDTH, TANK_HEIGHT + 10);
            place(chemicalOutputs[0].element(), 58, 13, TANK_WIDTH, TANK_HEIGHT);
            place(chemicalOutputs[1].element(), 110, 13, TANK_WIDTH, TANK_HEIGHT);
            place(bidirectionalProgress, 80, 30, 16, 6);
            place(powerSlot, 143, 35);
            place(powerBar, 164, 15, 7, TANK_HEIGHT);
        }

        private void layoutWashing() {
            resizePanel(WASHING_WIDTH, WASHING_HEIGHT);
            placeWithWashingOffset(fluidInput, 7, 13, TANK_WIDTH, TANK_HEIGHT);
            placeWithWashingOffset(chemicalInputs[0].element(), 28, 13, TANK_WIDTH, TANK_HEIGHT);
            placeWithWashingOffset(chemicalOutputs[0].element(), 131, 13, TANK_WIDTH, TANK_HEIGHT);
            placeWithWashingOffset(largeRightProgress, 64, 39, 48, 8);
            placeWithWashingOffset(powerSlot, 151, 13, SLOT_SIZE, SLOT_SIZE);
            placeWithWashingOffset(washingOutputDrainSlot, 151, 55, SLOT_SIZE, SLOT_SIZE);
        }

        private void layoutEvaporating() {
            place(fluidInput, 25, 13, TANK_WIDTH, TANK_HEIGHT);
            place(fluidOutput, 133, 13, TANK_WIDTH, TANK_HEIGHT);
            place(largeRightProgress, 64, 39, 48, 8);
        }

        private void layoutChemicalConversion() {
            place(chemicalInputs[0].element(), 25, 13, TANK_WIDTH, TANK_HEIGHT);
            place(chemicalOutputs[0].element(), 133, 13, TANK_WIDTH, TANK_HEIGHT);
            place(largeRightProgress, 64, 39, 48, 8);
        }

        private void layoutCrystallizing() {
            place(chemicalInputs[0].element(), 7, 4, TANK_WIDTH, TANK_HEIGHT + 10);
            place(statusScreen, 31, 13, 94, 42);
            place(itemOutputs[0], 129, 57);
            place(largeRightProgress, 53, 61, 48, 8);
        }

        private void layoutDissolution() {
            place(chemicalInputs[0].element(), 7, 4, TANK_WIDTH, TANK_HEIGHT + 10);
            place(itemInputs[0], 29, 36);
            place(chemicalOutputs[0].element(), 131, 13, TANK_WIDTH, TANK_HEIGHT);
            place(largeRightProgress, 64, 40, 48, 8);
            place(powerSlot, 152, 14);
            place(powerBar, 115, 76, 55, 6);
        }

        private void layoutItemChemicalMachine() {
            resizePanel(ITEM_CHEMICAL_WIDTH, ITEM_CHEMICAL_HEIGHT);
            place(itemInputs[0], 35, 0);
            place(chemicalCatalystSlot, 35, 36);
            place(compactChemicalInput.element(), 40, 20,
                    COMPACT_CHEMICAL_WIDTH + 2, COMPACT_CHEMICAL_HEIGHT + 2);
            place(itemOutputs[0], 87, 18);
            place(progressBar, 58, 22, 25, 9);
            place(powerSlot, 10, 18);
            place(itemChemicalPowerBar, 136, 0, 6, 54);
        }

        private void layoutNucleosynthesizing() {
            resizePanel(WIDTH, NUCLEOSYNTHESIZER_HEIGHT);
            // Mekanism JEI uses xOffset=-6 and yOffset=-18; the four-pixel canvas padding is included here.
            place(nucleosynthesizerChemicalInput.element(), 3, 4,
                    SMALL_MED_GAUGE_WIDTH, SMALL_MED_GAUGE_HEIGHT);
            place(statusScreen, 43, 4, 104, 68);
            place(itemInputs[0], 23, 25);
            place(itemOutputs[0], 149, 25);
            place(chemicalCatalystSlot, 3, 54);
            place(nucleosynthesizerEnergyGauge, 170, 4,
                    SMALL_MED_GAUGE_WIDTH, SMALL_MED_GAUGE_HEIGHT);
            place(powerSlot, 170, 54);
            place(rateBar, 3, 74, 185, 10);
        }

        private void layoutEnergyConversion() {
            place(itemInputs[0], 26, 36);
            place(energyGauge, 131, 13, TANK_WIDTH, TANK_HEIGHT);
            place(largeRightProgress, 64, 40, 48, 8);
        }

        private void layoutItemToChemical() {
            place(itemInputs[0], 26, 36);
            place(chemicalOutputs[0].element(), 131, 13, TANK_WIDTH, TANK_HEIGHT);
            place(largeRightProgress, 64, 40, 48, 8);
        }

        private void layoutMetallurgicInfusing() {
            resizePanel(METALLURGIC_INFUSING_WIDTH, METALLURGIC_INFUSING_HEIGHT);
            // Category offset (-5, -16) plus the four-pixel JEI recipe background padding.
            place(metallurgicChemicalInput.element(), 6, 3, 6, 54);
            place(chemicalCatalystSlot, 15, 22);
            place(itemInputs[0], 49, 30);
            place(itemOutputs[0], 107, 30);
            place(rightProgress, 71, 35, 32, 8);
            place(powerSlot, 141, 22);
            place(itemChemicalPowerBar, 163, 3, 6, 54);
        }

        private void layoutPainting() {
            place(chemicalInputs[0].element(), 25, 13, TANK_WIDTH, TANK_HEIGHT);
            place(itemInputs[0], 45, 35);
            place(itemOutputs[0], 116, 35);
            place(largeRightProgress, 64, 39, 48, 8);
            place(powerSlot, 144, 35);
            place(powerBar, 164, 15, 7, TANK_HEIGHT);
        }

        private void layoutReaction() {
            place(fluidInput, 5, 15, TANK_WIDTH, TANK_HEIGHT);
            place(chemicalInputs[0].element(), 28, 15, TANK_WIDTH, TANK_HEIGHT);
            place(itemInputs[0], 54, 40);
            place(itemOutputs[0], 116, 40);
            place(chemicalOutputs[0].element(), 140, 45, 18, 32);
            place(rightProgress, 77, 43, 32, 8);
            place(powerSlot, 141, 22);
            place(powerBar, 164, 21, 7, TANK_HEIGHT - 6);
        }

        private void layoutRotary(boolean condensentrating) {
            resizePanel(ROTARY_WIDTH, ROTARY_HEIGHT);
            placeWithRotaryOffset(
                    condensentrating ? chemicalInputs[0].element() : chemicalOutputs[0].element(),
                    25, 13, TANK_WIDTH, TANK_HEIGHT
            );
            placeWithRotaryOffset(
                    condensentrating ? fluidOutput : fluidInput,
                    133, 13, TANK_WIDTH, TANK_HEIGHT
            );
            placeWithRotaryOffset(rotaryLeftInputSlot, 4, 24, SLOT_SIZE, SLOT_SIZE);
            placeWithRotaryOffset(rotaryLeftOutputSlot, 4, 55, SLOT_SIZE, SLOT_SIZE);
            placeWithRotaryOffset(rotaryRightInputSlot, 154, 24, SLOT_SIZE, SLOT_SIZE);
            placeWithRotaryOffset(rotaryRightOutputSlot, 154, 55, SLOT_SIZE, SLOT_SIZE);
            placeWithRotaryOffset(downArrow, 159, 44, 8, 9);
            placeWithRotaryOffset(condensentrating ? largeRightProgress : largeLeftProgress,
                    64, 39, 48, 8);
        }

        private void layoutSawmill() {
            place(itemInputs[0], 56, 17);
            place(sawmillOutputFrame, 112, 31, 42, 26);
            place(itemOutputs[0], 116, 35);
            place(itemOutputs[1], 132, 35);
            place(upArrow, 60, 38, 8, 10);
            place(progressBar, 78, 38, 25, 9);
            place(powerSlot, 56, 53);
            place(powerBar, 164, 15, 7, TANK_HEIGHT);
        }

        private void configureChemicalSlot(UIElement display, int selectionIndex) {
            display.addEventListener(UIEvents.MOUSE_DOWN, event -> {
                canvas.selectChemicalSlot(selectionIndex);
                event.stopPropagation();
            });
        }

        private void configureRotaryControlSlot(UIElement display, boolean leftSide) {
            display.addEventListener(UIEvents.MOUSE_DOWN, event -> {
                var kind = canvas.kind();
                if (leftSide && kind == MekanismRecipeKind.CONDENSENTRATING) {
                    canvas.selectChemicalSlot(CHEMICAL_INPUT_SLOT);
                } else if (leftSide && kind == MekanismRecipeKind.DECONDENSENTRATING) {
                    canvas.selectChemicalSlot(CHEMICAL_OUTPUT_SLOT);
                } else if (!leftSide && kind == MekanismRecipeKind.CONDENSENTRATING) {
                    MekanismCanvas.selectSlot(SlotSelection.fluid(FLUID_OUTPUT_SLOT));
                } else if (!leftSide && kind == MekanismRecipeKind.DECONDENSENTRATING) {
                    MekanismCanvas.selectSlot(SlotSelection.fluid(FLUID_INPUT_SLOT));
                }
                event.stopPropagation();
            });
        }

        private void resizePanel(int width, int height) {
            panel.layout(layout -> {
                layout.width(width);
                layout.height(height);
            });
        }

        private static void placeWithChemicalOffset(
                UIElement element,
                int left,
                int top,
                int width,
                int height
        ) {
            place(
                    element,
                    left + CHEMICAL_MIXING_OFFSET,
                    top + CHEMICAL_MIXING_OFFSET,
                    width,
                    height
            );
        }

        private static void placeWithWashingOffset(
                UIElement element,
                int left,
                int top,
                int width,
                int height
        ) {
            place(
                    element,
                    left + WASHING_X_OFFSET,
                    top + WASHING_Y_OFFSET,
                    width,
                    height
            );
        }

        private static void placeWithRotaryOffset(
                UIElement element,
                int left,
                int top,
                int width,
                int height
        ) {
            place(element, left + ROTARY_X_OFFSET, top + ROTARY_Y_OFFSET, width, height);
        }

        private static void place(UIElement element, int left, int top) {
            place(element, left, top, SLOT_SIZE, SLOT_SIZE);
        }

        private static void place(UIElement element, int left, int top, int width, int height) {
            element.setDisplay(true);
            element.layout(layout -> {
                layout.positionType(TaffyPosition.ABSOLUTE);
                layout.left(left);
                layout.top(top);
                layout.width(width);
                layout.height(height);
            });
        }
    }

    private static UIElement slotCell(UIElement slot, String slotTexture) {
        slot.layout(layout -> {
            layout.width(SLOT_SIZE);
            layout.height(SLOT_SIZE);
        });
        slot.style(style -> style.backgroundTexture(
                mekanismTexture("slot/" + slotTexture, SLOT_SIZE, SLOT_SIZE)
        ));
        return new UIElement().layout(layout -> {
            layout.positionType(TaffyPosition.ABSOLUTE);
            layout.width(SLOT_SIZE);
            layout.height(SLOT_SIZE);
        }).addChild(slot);
    }

    private static void configureItemOutputSlot(ItemSlot slot, String slotTexture, int size) {
        slot.layout(layout -> {
            layout.width(size);
            layout.height(size);
        });
        slot.style(style -> style.backgroundTexture(slotTexture == null
                ? IGuiTexture.EMPTY
                : mekanismTexture("slot/" + slotTexture, SLOT_SIZE, SLOT_SIZE)));
    }

    public static FluidDisplaySlot createFluidInputGauge() {
        return new MekanismFluidGauge("red.png");
    }

    public static FluidDisplaySlot createFluidOutputGauge() {
        return new MekanismFluidGauge("blue.png");
    }

    private static ChemicalDisplay chemicalGauge(String gaugeFrame) {
        return new ChemicalDisplay(gaugeFrame);
    }

    private static ChemicalDisplay chemicalBar(int width, int height) {
        return new ChemicalDisplay(width, height);
    }

    private static UIElement progress(String textureName, int width, int height) {
        return new UIElement().style(style -> style.backgroundTexture(
                mekanismTexture("progress/" + textureName, 0, height, width, height)
        ));
    }

    private static UIElement chemicalControlSlot(String slotTexture, String overlayTexture) {
        return new UIElement().style(style -> style.backgroundTexture(IGuiTexture.group(
                mekanismTexture("slot/" + slotTexture, SLOT_SIZE, SLOT_SIZE),
                mekanismTexture("slot/" + overlayTexture, SLOT_SIZE, SLOT_SIZE)
        )));
    }

    private static UIElement machineControlSlot(String slotTexture, String overlayTexture) {
        if (overlayTexture == null) {
            return new UIElement().style(style -> style.backgroundTexture(
                    mekanismTexture("slot/" + slotTexture, SLOT_SIZE, SLOT_SIZE)
            ));
        }
        return chemicalControlSlot(slotTexture, overlayTexture);
    }

    private static UIElement powerSlot() {
        return new UIElement().style(style -> style.backgroundTexture(IGuiTexture.group(
                mekanismTexture("slot/power.png", SLOT_SIZE, SLOT_SIZE),
                mekanismTexture("slot/overlay_power.png", SLOT_SIZE, SLOT_SIZE)
        )));
    }

    private static UIElement mekanismGui(String textureName, int width, int height) {
        return new UIElement().style(style -> style.backgroundTexture(mekanismTexture(textureName, width, height)));
    }

    private static SpriteTexture mekanismTexture(String textureName, int width, int height) {
        return mekanismTexture(textureName, 0, 0, width, height);
    }

    private static SpriteTexture mekanismTexture(
            String textureName,
            int x,
            int y,
            int width,
            int height
    ) {
        return SpriteTexture.of(new ResourceLocation(MEKANISM_MOD_ID, "gui/" + textureName))
                .setSprite(x, y, width, height);
    }

    private static IGuiTexture canvasBackground() {
        var background = new ResourceLocation(
                JEI_MOD_ID, "textures/jei/atlas/gui/single_recipe_background.png"
        );
        if (!ViScriptRecipe.isPresentResource(background)) {
            return Sprites.BORDER;
        }
        return SpriteTexture.of(background).setSprite(0, 0, 64, 64).setBorder(16);
    }

    private static UIElement shapelessIcon() {
        var texture = new ResourceLocation(
                JEI_MOD_ID, "textures/jei/atlas/gui/icons/shapeless_icon.png"
        );
        boolean present = ViScriptRecipe.isPresentResource(texture);
        return new UIElement().style(style -> {
            style.backgroundTexture(present
                    ? SpriteTexture.of(texture).setSprite(0, 0, 32, 32)
                    : IGuiTexture.EMPTY);
            if (present) {
                style.tooltips(Component.translatable("jei.tooltip.shapeless.recipe"));
            }
        });
    }

    private static UIElement horizontalPowerBar() {
        var fill = new UIElement().layout(layout -> {
            layout.widthPercent(100);
            layout.heightPercent(100);
        }).style(style -> style.backgroundTexture(
                mekanismTexture("bar/horizontal_power.png", 52, 4)
        ));
        return new UIElement().layout(layout -> {
            layout.positionType(TaffyPosition.ABSOLUTE);
            layout.width(54);
            layout.height(6);
            layout.paddingAll(1);
        }).style(style -> style.backgroundTexture(
                mekanismTexture("bar/base.png", 5, 5).setBorder(2)
        )).addChild(fill);
    }

    private static UIElement verticalPowerBar() {
        var fill = new UIElement().layout(layout -> {
            layout.widthPercent(100);
            layout.heightPercent(100);
        }).style(style -> style.backgroundTexture(
                mekanismTexture("bar/vertical_power.png", 4, 52)
        ));
        return new UIElement().layout(layout -> {
            layout.positionType(TaffyPosition.ABSOLUTE);
            layout.width(6);
            layout.height(54);
            layout.paddingAll(1);
        }).style(style -> style.backgroundTexture(
                mekanismTexture("bar/base.png", 5, 5).setBorder(2)
        )).addChild(fill);
    }

    private static UIElement energyGauge(String overlayTexture, int overlayWidth, int overlayHeight) {
        var fill = new UIElement().layout(layout -> {
            layout.positionType(TaffyPosition.ABSOLUTE);
            layout.left(1);
            layout.top(1);
            layout.width(overlayWidth);
            layout.height(overlayHeight);
        }).style(style -> style.backgroundTexture(new ChemicalDisplay.EnergyTexture()));
        var overlay = new UIElement().layout(layout -> {
            layout.positionType(TaffyPosition.ABSOLUTE);
            layout.left(1);
            layout.top(1);
            layout.width(overlayWidth);
            layout.height(overlayHeight);
        }).style(style -> style.backgroundTexture(
                mekanismTexture("gauge/" + overlayTexture, overlayWidth, overlayHeight)
        ));
        return new UIElement().layout(layout -> {
            layout.positionType(TaffyPosition.ABSOLUTE);
            layout.width(overlayWidth + 2);
            layout.height(overlayHeight + 2);
        }).style(style -> style.backgroundTexture(
                mekanismTexture("gauge/normal.png", 0, 0, 5, 5).setBorder(2)
        )).addChildren(fill, overlay);
    }

    private static UIElement dynamicRateBar() {
        var fill = new UIElement().layout(layout -> {
            layout.positionType(TaffyPosition.ABSOLUTE);
            layout.left(1);
            layout.top(1);
            layout.width(183);
            layout.height(8);
        }).style(style -> style.backgroundTexture(new DynamicRateTexture()));
        return new UIElement().layout(layout -> {
            layout.positionType(TaffyPosition.ABSOLUTE);
            layout.width(185);
            layout.height(10);
        }).style(style -> style.backgroundTexture(
                mekanismTexture("bar/base.png", 0, 0, 5, 5).setBorder(2)
        )).addChild(fill);
    }

    private static UIElement bar(int width, int height, int color) {
        var fill = new UIElement().layout(layout -> {
            layout.widthPercent(100);
            layout.heightPercent(100);
        }).style(style -> style.backgroundTexture(new ColorRectTexture(color)));
        return new UIElement().layout(layout -> {
            layout.positionType(TaffyPosition.ABSOLUTE);
            layout.width(width);
            layout.height(height);
            layout.paddingAll(1);
        }).style(style -> style.backgroundTexture(Sprites.BORDER_DARK)).addChild(fill);
    }

    private static void setChemicalCatalysts(IngredientDisplaySlot slot, MekanismChemicalIngredientData data) {
        var stacks = ChemicalDisplay.catalystStacks(data);
        slot.setTagDisplayStacks(stacks);
    }

    private static int chemicalColor(MekanismChemicalIngredientData data) {
        return ChemicalDisplay.colorRepresentation(data);
    }

    private static int chemicalColor(MekanismChemicalStackData data) {
        return ChemicalDisplay.colorRepresentation(data);
    }

    private static final class MekanismFluidGauge extends FluidDisplaySlot {
        private final IGuiTexture gaugeOverlay;

        private MekanismFluidGauge(String gaugeFrame) {
            xeiPhantom()
                    .setAllowClickFilled(false)
                    .setAllowClickDrained(false)
                    .slotStyle(style -> style.showFluidTooltips(true));
            layout(layout -> {
                layout.positionType(TaffyPosition.ABSOLUTE);
                layout.width(TANK_WIDTH);
                layout.height(TANK_HEIGHT);
                layout.paddingAll(1);
            });
            amountLabel.setDisplay(false);
            style(style -> style.backgroundTexture(
                    mekanismTexture("gauge/" + gaugeFrame, 5, 5).setBorder(2)
            ));
            gaugeOverlay = mekanismTexture("gauge/standard.png", 16, 58);
        }

        @Override
        public void drawBackgroundAdditional(@NotNull GUIContext guiContext) {
            super.drawBackgroundAdditional(guiContext);
            guiContext.drawTexture(gaugeOverlay, getContentX(), getContentY(), getContentWidth(), getContentHeight());
        }
    }

    private static final class PigmentProgressTexture implements IGuiTexture {
        private static final int WIDTH = 28;
        private static final int HEIGHT = 8;
        private final SpriteTexture[] columns = new SpriteTexture[WIDTH];
        private final boolean reverse;
        private int fromColor;
        private int toColor;

        private PigmentProgressTexture(String textureName, boolean reverse) {
            this.reverse = reverse;
            for (int column = 0; column < columns.length; column++) {
                columns[column] = mekanismTexture("progress/" + textureName, column, HEIGHT, 1, HEIGHT);
            }
            setColors(0xFFFFFFFF, 0xFFFFFFFF);
        }

        private void setColors(int fromColor, int toColor) {
            if (this.fromColor == fromColor && this.toColor == toColor) {
                return;
            }
            this.fromColor = fromColor;
            this.toColor = toColor;
            for (int column = 0; column < columns.length; column++) {
                float progress = column / (float) (columns.length - 1);
                columns[column].setColor(lerpColor(fromColor, toColor, reverse ? 1 - progress : progress));
            }
        }

        @Override
        public void draw(
                GuiGraphics graphics,
                float mouseX,
                float mouseY,
                float x,
                float y,
                float width,
                float height,
                float partialTicks
        ) {
            int pixelWidth = Math.max(1, Math.round(width));
            for (int column = 0; column < pixelWidth; column++) {
                int textureIndex = pixelWidth == 1
                        ? columns.length - 1
                        : Math.round(column * (columns.length - 1F) / (pixelWidth - 1F));
                columns[textureIndex].draw(graphics, mouseX, mouseY, x + column, y, 1, height, partialTicks);
            }
        }

        private static int lerpColor(int from, int to, float progress) {
            int alpha = lerpChannel(from >>> 24, to >>> 24, progress);
            int red = lerpChannel(from >>> 16, to >>> 16, progress);
            int green = lerpChannel(from >>> 8, to >>> 8, progress);
            int blue = lerpChannel(from, to, progress);
            return alpha << 24 | red << 16 | green << 8 | blue;
        }

        private static int lerpChannel(int from, int to, float progress) {
            return Math.round((from & 0xFF) + ((to & 0xFF) - (from & 0xFF)) * progress);
        }
    }

    static final class DynamicRateTexture implements IGuiTexture {
        private static final int START_RED = 60;
        private static final int START_GREEN = 45;
        private static final int START_BLUE = 74;
        private static final int END_RED = 100;
        private static final int END_GREEN = 30;
        private static final int END_BLUE = 170;
        private static final int TEXTURE_WIDTH = 183;
        private final SpriteTexture[] columns = createColumns();

        @Override
        public void draw(
                GuiGraphics graphics,
                float mouseX,
                float mouseY,
                float x,
                float y,
                float width,
                float height,
                float partialTicks
        ) {
            int pixelWidth = Math.max(1, Math.round(width));
            for (int column = 0; column < pixelWidth; column++) {
                int textureIndex = pixelWidth == 1
                        ? TEXTURE_WIDTH - 1
                        : Math.round(column * (TEXTURE_WIDTH - 1F) / (pixelWidth - 1F));
                columns[textureIndex].draw(graphics, mouseX, mouseY, x + column, y, 1, height, partialTicks);
            }
        }

        private static SpriteTexture[] createColumns() {
            var textures = new SpriteTexture[TEXTURE_WIDTH];
            for (int column = 0; column < textures.length; column++) {
                float progress = column / (float) (textures.length - 1);
                int red = Math.round(START_RED + (END_RED - START_RED) * progress);
                int green = Math.round(START_GREEN + (END_GREEN - START_GREEN) * progress);
                int blue = Math.round(START_BLUE + (END_BLUE - START_BLUE) * progress);
                int spriteX = column == 0 ? 0 : column == textures.length - 1 ? 2 : 1;
                textures[column] = mekanismTexture("bar/dynamic_rate.png", spriteX, 0, 1, 8)
                        .setColor(0xFF000000 | red << 16 | green << 8 | blue);
            }
            return textures;
        }
    }
}
