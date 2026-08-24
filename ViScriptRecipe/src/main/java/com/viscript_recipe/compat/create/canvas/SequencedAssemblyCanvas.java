package com.viscript_recipe.compat.create.canvas;

import com.lowdragmc.lowdraglib2.gui.ColorPattern;
import com.lowdragmc.lowdraglib2.gui.texture.Icons;
import com.lowdragmc.lowdraglib2.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.TextWrap;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.viscript_recipe.compat.create.data.CreateSequencedAssemblyRecipeData;
import com.viscript_recipe.compat.create.data.CreateSequencedAssemblyStepData;
import com.viscript_recipe.compat.create.data.CreateSequencedAssemblyStepKind;
import com.viscript_recipe.data.FluidIngredientData;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.data.RecipeOutputData;
import com.viscript_recipe.gui.canvas.FluidRecipeCanvas;
import com.viscript_recipe.gui.editor.RecipeEditorUi;
import com.viscript_recipe.gui.editor.SlotSelection;
import com.viscript_recipe.gui.views.NavigationView;
import com.viscript_recipe.gui.views.PropertiesView;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

import static com.viscript_recipe.recipe.RecipeHelper.itemFromRegistry;

public class SequencedAssemblyCanvas extends FluidRecipeCanvas<CreateSequencedAssemblyRecipeData> {
    static final int SEQUENCED_STEP_INGREDIENT_OFFSET = 1;
    static final int CREATE_SEQUENCED_MAX_OUTPUTS = 9;
    static final Label loopsLabel = createLoopsLabel();

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
        updateLoopsLabel();
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
        content.addChildren(sectionTitle("viscript_recipe.editor.properties.create.sequenced_assembly"),
                intField("viscript_recipe.config.create.sequenced_assembly.loops",
                        data.getLoops(), 1, Integer.MAX_VALUE, data::setLoops, this::updateLoopsLabel)
        );
        content.addChild(RecipeEditorUi.textButton(
                Component.translatable("viscript_recipe.editor.create.sequenced_assembly.add_step"),
                Icons.ADD, event -> {
                    data.getSequence().add(new CreateSequencedAssemblyStepData());
                    reloadCanvas();
                    selectSlot(SlotSelection.createSequencedStep(data.getSequence().size() - 1));
                }
        ).layout(layout -> layout.widthPercent(100).height(18)));
    }

    @Override
    public void buildExtraItemProperties(UIElement content) {
        content.addChildren(sectionTitle("viscript_recipe.editor.properties.create.sequenced_assembly.transitional_item"),
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
        content.addChild(sectionTitle("viscript_recipe.editor.properties.create.sequenced_assembly.step"));
        int index = selectedSlotIndex();
        var data = getData();
        var step = data.getSequence().get(index);
        var kind = step.getKind();
        content.addChildren(createStepTitle(index),
                selector("viscript_recipe.config.create.sequenced_assembly.step.kind",
                        List.of(CreateSequencedAssemblyStepKind.values()), kind,
                        CreateSequencedAssemblyStepKind::displayName, stepKind -> {
                            step.setKind(stepKind); reloadCanvas(); reloadProperties();
                        }
                )
        );
        if (kind == CreateSequencedAssemblyStepKind.DEPLOYING) content.addChild(switchField("viscript_recipe.config.create.keep_held_item", step.isKeepHeldItem(), step::setKeepHeldItem));
        if (kind == CreateSequencedAssemblyStepKind.CUTTING) content.addChild(intField("viscript_recipe.config.create.processing_time", step.getProcessingTime(), 0, Integer.MAX_VALUE, step::setProcessingTime));
        content.addChild(RecipeEditorUi.textButton(
                Component.translatable("viscript_recipe.editor.create.sequenced_assembly.remove_step"),
                Icons.DELETE, event -> {
                    data.getSequence().remove(index);
                    reloadCanvas(); selectRecipe();
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
        content.addChildren(sectionTitle("viscript_recipe.editor.properties.create.output"),
                PropertiesView.createItemStackConfigurator("viscript_recipe.config.create.output.item",
                        () -> getSelectedOutput().getItem(), this::setSelectedOutput
                )
        );
        content.addChild(floatField("viscript_recipe.config.create.output.weight",
                getSelectedOutput().getChance(), 0, Integer.MAX_VALUE, this::setSelectedOutput));
    }

    @Override
    public UIElement createCanvas() {
        var transitionalSlot = createExtraItemSlot(SLOT_SIZE,
                Component.translatable("viscript_recipe.editor.create.sequenced_assembly.transitional_item_slot"));
        var inputSlot = createIngredientSlot(0, SLOT_SIZE);
        var outputSlots = new ItemSlot[9];
        for (int i = 0; i < CREATE_SEQUENCED_MAX_OUTPUTS; i++) {
            var slot = createOutputSlot(i, SLOT_SIZE);
            outputSlots[i] = slot;
        }
        var canvas = CreateSequencedAssemblyCanvasFactory.createCanvas(
                inputSlot, transitionalSlot,
                outputSlots, new UIElement[9], loopsLabel
        );
        for (int index = 0; index < getData().getSequence().size(); index++) {
            var kind = getData().getSequence().get(index).getKind();
            var ingredientCell = kind == CreateSequencedAssemblyStepKind.DEPLOYING ? createStepIngredientCell(index) : null;
            var fluidCell = kind == CreateSequencedAssemblyStepKind.FILLING ? createStepFluidCell(index) : null;

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
                    i -> selectSlot(SlotSelection.createSequencedStep(i)));
            canvas.stepRow().addChild(card);
        }
        return canvas.root();
    }

    private UIElement createStepIngredientCell(int index) {
        index += SEQUENCED_STEP_INGREDIENT_OFFSET;
        var ingredientSlot = createIngredientSlot(index, SLOT_SIZE);
        tooltip(ingredientSlot, Component.translatable(
                "viscript_recipe.editor.create.sequenced_assembly.step_ingredient_slot", index + 1));
        return configureStepIngredientCell(CreateProcessingCanvasFactory.framedSlot(ingredientSlot, 36), index);
    }

    private UIElement createStepFluidCell(int index) {
        var fluidSlot = createFluidInputSlot(index);
        tooltip(fluidSlot, Component.translatable(
                "viscript_recipe.editor.create.sequenced_assembly.step_fluid_slot", index + 1));
        return configureStepFluidCell(CreateProcessingCanvasFactory.framedSlot(fluidSlot, 36), index);
    }

    private static Label createLoopsLabel() {
        var label = emptyLabel();
        label.textStyle(style -> style
                .textAlignHorizontal(Horizontal.CENTER)
                .textColor(ColorPattern.LIGHT_GRAY.color)
                .textWrap(TextWrap.HOVER_ROLL));
        label.layout(layout -> layout.width(62).height(16));
        return label;
    }

    private void updateLoopsLabel() {
        loopsLabel.setText(Component.translatable(
                "viscript_recipe.editor.create.sequenced_assembly.loops", getData().getLoops()));
    }

    private UIElement configureStepFluidCell(UIElement cell, int index) {
        cell.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            if (event.button == 1) setVisualFluidInput(index, FluidIngredientData.empty());
            selectSlot(SlotSelection.fluid(index));
            event.stopPropagation();
        });
        return cell;
    }

    private UIElement configureStepIngredientCell(UIElement cell, int index) {
        cell.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            if (event.button == 1) setVisualIngredient(index, RecipeIngredient.empty());
            selectSlot(SlotSelection.ingredient(index));
            event.stopPropagation();
        });
        return cell;
    }
}
