package com.viscript_recipe.gui.canvas.create;

import com.lowdragmc.lowdraglib2.gui.ColorPattern;
import com.lowdragmc.lowdraglib2.gui.texture.Icons;
import com.lowdragmc.lowdraglib2.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.TextWrap;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Switch;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.data.RecipeOutputData;
import com.viscript_recipe.data.create.CreateSequencedAssemblyRecipeData;
import com.viscript_recipe.data.create.CreateSequencedAssemblyStepKind;
import com.viscript_recipe.data.create.FluidIngredientData;
import com.viscript_recipe.gui.canvas.FluidRecipeCanvas;
import com.viscript_recipe.gui.editor.FluidDisplaySlot;
import com.viscript_recipe.gui.editor.IngredientDisplaySlot;
import com.viscript_recipe.gui.editor.RecipeEditorUi;
import com.viscript_recipe.gui.editor.SlotSelection;
import com.viscript_recipe.gui.views.NavigationView;
import com.viscript_recipe.gui.views.PropertiesView;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public class SequencedAssemblyCanvas extends FluidRecipeCanvas<CreateSequencedAssemblyRecipeData> {
    static final int SEQUENCED_STEP_INGREDIENT_OFFSET = 1;
    static final int CREATE_SEQUENCED_MAX_OUTPUTS = 9;
    static final Label createSequencedLoopsLabel = createLoopsLabel();

    public SequencedAssemblyCanvas(NavigationView navigationView, RecipeEntry entry) {super(navigationView, entry);}

    @Override
    public void load() {
        var data = getData();
        loadIngredientSlot(0, data.getIngredient());
        setExtraItem(data.getTransitionalItem());
        var sequence = data.getSequence();
        for (int i = 0; i < sequence.size(); i++) {
            var step = sequence.get(i);
            if (step.isFluidIngredient()) setVisualFluidInput(i, step.getFluidIngredient());
            else loadIngredientSlot(i + SEQUENCED_STEP_INGREDIENT_OFFSET, step.getIngredient());
        }
        var outputs = data.getOutputs();
        for (int i = 0; i < Math.min(CREATE_SEQUENCED_MAX_OUTPUTS, outputs.size()); i++) {
            setVisualOutput(i, outputs.get(i));
        }
    }

    @Override
    public void save() {
        var data = getData();
        data.setIngredient(getVisualIngredient(0));
        var sequence = data.getSequence();
        for (int i = 0; i < sequence.size(); i++) {
            var step = sequence.get(i);
            int ingredientSlot = i + SEQUENCED_STEP_INGREDIENT_OFFSET;
            if (ingredientSlot < 81) {
                if (step.isFluidIngredient()) step.setFluidIngredient(getVisualFluidInput(i));
                else step.setIngredient(getVisualIngredient(ingredientSlot));
            }
        }
        var outputs = new ArrayList<RecipeOutputData>();
        for (int i = 0; i < CREATE_SEQUENCED_MAX_OUTPUTS; i++) {
            var output = getVisualOutput(i);
            if (!output.isEmpty()) outputs.add(output);
        }
        data.setOutputs(outputs);
    }

    @Override
    public void buildRecipeProperties(UIElement content) {
        var data = getData();
        content.addChildren(
                RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.create.sequenced_assembly"),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.create.sequenced_assembly.loops",
                        RecipeEditorUi.intField(data.getLoops(), 1, Integer.MAX_VALUE, i -> {
                            data.setLoops(i); updateLoopsLabel(i);
                        }))
        );
        content.addChild(RecipeEditorUi.textButton(
                Component.translatable("viscript_recipe.editor.create.sequenced_assembly.add_step"),
                Icons.ADD, event -> {
                    data.getSequence().add(data.createDefaultStep());
                    navigationView.reloadCanvas();
                    navigationView.setSlotSelection(SlotSelection.createSequencedStep(data.getSequence().size() - 1));
                }
        ).layout(layout -> layout.widthPercent(100).height(18)));
    }

    @Override
    public void buildExtraItemProperties(UIElement content) {
        content.addChildren(
                RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.create.sequenced_assembly.transitional_item"),
                PropertiesView.createItemStackConfigurator(
                        "viscript_recipe.config.create.sequenced_assembly.transitional_item",
                        this::getExtraItem, this::setExtraItem
                )
        );
    }

    @Override
    public void setExtraItem(int index, ItemStack item) {
        super.setExtraItem(index, item);
        getData().setTransitionalItem(item.copyWithCount(1));
    }

    public void buildSequencedStepProperties(UIElement content) {
        content.addChild(RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.create.sequenced_assembly.step"));
        int index = selectedSlotIndex();
        var data = getData();
        var step = data.getSequence().get(index);
        var kind = step.getKind();
        content.addChildren(createStepTitle(index),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.create.sequenced_assembly.step.kind",
                        RecipeEditorUi.selector(
                                List.of(CreateSequencedAssemblyStepKind.values()), kind,
                                CreateSequencedAssemblyStepKind::displayName, stepKind -> {
                                    step.setKind(stepKind);
                                    navigationView.reloadCanvas();
                                }
                        ))
        );
        if (kind == CreateSequencedAssemblyStepKind.DEPLOYING) {
            content.addChild(RecipeEditorUi.fieldGroup("viscript_recipe.config.create.keep_held_item",
                    new Switch().setOn(step.isKeepHeldItem(), false)
                            .setOnSwitchChanged(step::setKeepHeldItem)));
        }
        if (kind == CreateSequencedAssemblyStepKind.CUTTING) {
            content.addChild(RecipeEditorUi.fieldGroup("viscript_recipe.config.create.processing_time",
                    RecipeEditorUi.intField(step.getProcessingTime(), 0, Integer.MAX_VALUE, step::setProcessingTime)));
        }
        content.addChild(RecipeEditorUi.textButton(
                Component.translatable("viscript_recipe.editor.create.sequenced_assembly.remove_step"),
                Icons.DELETE, event -> {
                    data.getSequence().remove(index);
                    navigationView.reloadCanvas();
                    navigationView.setSlotSelection(SlotSelection.RECIPE);
                }
        ).layout(layout -> layout.widthPercent(100).height(18)));
    }

    private static UIElement createStepTitle(int index) {
        var label = RecipeEditorUi.label(Component.translatable("viscript_recipe.editor.create.sequenced_assembly.step", index + 1));
        label.textStyle(style -> style
                .textColor(ColorPattern.WHITE.color)
                .textWrap(TextWrap.HOVER_ROLL));
        label.layout(layout -> layout.height(16));
        return label;
    }

    @Override
    public void buildResultProperties(UIElement content) {
        content.addChildren(
                RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.create.output"),
                PropertiesView.createItemStackConfigurator(
                        "viscript_recipe.config.create.output.item",
                        () -> getSelectedOutput().getItem(), this::setSelectedOutput
                )
        );
        content.addChild(RecipeEditorUi.fieldGroup("viscript_recipe.config.create.output.weight",
                RecipeEditorUi.floatField(getSelectedOutput().getChance(), 0, Integer.MAX_VALUE, this::setSelectedOutput)));
    }

    @Override
    public UIElement createCanvas() {
        var transitionalSlot = configureExtraItemSlot(createEditorSlot(SLOT_SIZE),
                Component.translatable("viscript_recipe.editor.create.sequenced_assembly.transitional_item_slot"));
        extraItemSlots[0] = transitionalSlot;
        var inputSlot = createIngredientSlot(0, SLOT_SIZE);
        var outputSlots = new ItemSlot[9];
        for (int i = 0; i < CREATE_SEQUENCED_MAX_OUTPUTS; i++) {
            var slot = createOutputSlot(i, SLOT_SIZE);
            outputSlots[i] = slot;
        }
        var canvas = CreateSequencedAssemblyCanvasFactory.createCanvas(
                inputSlot, transitionalSlot,
                outputSlots, new UIElement[9],
                updateLoopsLabel(getData().getLoops())
        );
        for (int index = 0; index < getData().getSequence().size(); index++) {
            var kind = getData().getSequence().get(index).getKind();
            boolean bl = kind == CreateSequencedAssemblyStepKind.FILLING;
            var ingredientCell = bl ? null : createStepIngredientCell(index);
            var fluidCell = bl ? createStepFluidCell(index) : null;

            boolean selected = navigationView.getSlotSelection().kind() == SlotSelection.Kind.CREATE_SEQUENCED_STEP && selectedSlotIndex() == index;
            var stepLabel = RecipeEditorUi.label(Component.translatable(
                    "viscript_recipe.editor.create.sequenced_assembly.step_short", index + 1
            )).textStyle(style -> style.textAlignHorizontal(Horizontal.CENTER)
                    .textColor(selected ? ColorPattern.WHITE.color : ColorPattern.LIGHT_GRAY.color)
                    .textWrap(TextWrap.HOVER_ROLL));
            var stepIcon = new UIElement().style(style -> style
                    .backgroundTexture(new ItemStackTexture(new ItemStack(itemFromRegistry(kind.machineItemId(), Items.CRAFTING_TABLE))))
                    .tooltips(kind.displayName()));
            var card = CreateSequencedAssemblyCanvasFactory.createStepCard(index, stepLabel, stepIcon, ingredientCell, fluidCell,
                    i -> navigationView.setSlotSelection(SlotSelection.createSequencedStep(i)));
            canvas.stepRow().addChild(card);
        }
        return canvas.root();
    }

    private UIElement createStepIngredientCell(int index) {
        index += SEQUENCED_STEP_INGREDIENT_OFFSET;
        var ingredientSlot = configureStepIngredientSlot(createIngredientSlot(SLOT_SIZE), index);
        visualIngredientSlots[index] = ingredientSlot;
        return configureStepIngredientCell(CreateProcessingCanvasFactory.framedSlot(ingredientSlot, 36), index);
    }

    private UIElement createStepFluidCell(int index) {
        var fluidSlot = configureStepFluidSlot(createFluidDisplaySlot(), index);
        fluidInputSlots[index] = fluidSlot;
        return configureStepFluidCell(CreateProcessingCanvasFactory.framedSlot(fluidSlot, 36), index);
    }

    private static Label createLoopsLabel() {
        var label = RecipeEditorUi.label(Component.empty());
        label.textStyle(style -> style
                .textAlignHorizontal(Horizontal.CENTER)
                .textColor(ColorPattern.LIGHT_GRAY.color)
                .textWrap(TextWrap.HOVER_ROLL));
        label.layout(layout -> layout.width(62).height(16));
        return label;
    }

    private static Label updateLoopsLabel(int loops) {
        createSequencedLoopsLabel.setText(
                Component.translatable("viscript_recipe.editor.create.sequenced_assembly.loops", loops));
        return createSequencedLoopsLabel;
    }

    private FluidDisplaySlot configureStepFluidSlot(FluidDisplaySlot slot, int index) {
        slot.registerValueListener(stack -> setVisualFluidInput(index, FluidIngredientData.fluid(stack)));
        slot.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            if (event.button == 1) setVisualFluidInput(index, FluidIngredientData.empty());
            navigationView.setSlotSelection(SlotSelection.fluid(index));
            event.stopPropagation();
        });
        slot.style(style -> style.tooltips(Component.translatable(
                "viscript_recipe.editor.create.sequenced_assembly.step_fluid_slot",
                index + 1
        )));
        return slot;
    }

    private UIElement configureStepFluidCell(UIElement cell, int index) {
        cell.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            if (event.button == 1) setVisualFluidInput(index, FluidIngredientData.empty());
            navigationView.setSlotSelection(SlotSelection.fluid(index));
            event.stopPropagation();
        });
        return cell;
    }

    private IngredientDisplaySlot configureStepIngredientSlot(IngredientDisplaySlot slot, int index) {
        ingredientDragSlotIndices.put(slot, index);
        slot.registerValueListener(stack -> setVisualIngredient(index, RecipeIngredient.item(stack)));
        slot.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            if (event.button == 1) setVisualIngredient(index, RecipeIngredient.empty());
            navigationView.setSlotSelection(SlotSelection.ingredient(index));
            event.stopPropagation();
        });
        slot.style(style -> style.tooltips(Component.translatable(
                "viscript_recipe.editor.create.sequenced_assembly.step_ingredient_slot",
                index - SEQUENCED_STEP_INGREDIENT_OFFSET + 1
        )));
        return slot;
    }

    private UIElement configureStepIngredientCell(UIElement cell, int index) {
        cell.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            if (event.button == 1) setVisualIngredient(index, RecipeIngredient.empty());
            navigationView.setSlotSelection(SlotSelection.ingredient(index));
            event.stopPropagation();
        });
        return cell;
    }
}
