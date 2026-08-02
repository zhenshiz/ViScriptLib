package com.viscript_recipe.gui.canvas;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.FluidSlot;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.viscript_recipe.data.IVSRecipeData;
import com.viscript_recipe.data.RecipeEditorTypes;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.data.create.FluidIngredientData;
import com.viscript_recipe.data.create.FluidIngredientKind;
import com.viscript_recipe.gui.editor.FluidDisplaySlot;
import com.viscript_recipe.gui.editor.RecipeEditorUi;
import com.viscript_recipe.gui.editor.SlotSelection;
import com.viscript_recipe.gui.views.NavigationView;
import com.viscript_recipe.gui.views.PropertiesView;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

public abstract class FluidRecipeCanvas<D extends IVSRecipeData> extends RecipeCanvas<D> {
    protected static final int CREATE_MAX_FLUID_INPUTS = 2;
    protected static final int CREATE_MAX_FLUID_OUTPUTS = 2;

    protected static FluidIngredientData[] visualFluidInputs;
    protected static FluidStack[] visualFluidOutputs;
    public static final FluidDisplaySlot[] fluidInputSlots = new FluidDisplaySlot[81];
    public static final FluidSlot[] fluidOutputSlots = new FluidSlot[2];

    public FluidRecipeCanvas(NavigationView navigationView, RecipeEntry entry) {super(navigationView, entry);}

    @Override
    public void initVisualState() {
        super.initVisualState();
        visualFluidInputs = defaultedArrays(new FluidIngredientData[81], FluidIngredientData.empty());
        visualFluidOutputs = defaultedArrays(new FluidStack[2], FluidStack.EMPTY);
    }

    @Override
    public void buildFluidProperties(UIElement content) {
        if (navigationView.getSlotSelection().index() < 2 || entry.isType(RecipeEditorTypes.CREATE_SEQUENCED_ASSEMBLY)) {
            var ingredient = getSelectedFluidInput();
            var kind = ingredient.getKind();
            content.addChildren(
                    RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.create.fluid_ingredient"),
                    RecipeEditorUi.fieldGroup("viscript_recipe.config.create.fluid_ingredient.kind",
                            RecipeEditorUi.selector(
                                    List.of(FluidIngredientKind.values()),
                                    kind, FluidIngredientKind::displayName,
                                    this::setSelectedFluidIngredientKind
                            ))
            );
            if (kind == FluidIngredientKind.TAG) {
                content.addChildren(
                        PropertiesView.createFluidTagConfigurator(ingredient,
                                tag -> setSelectedFluidInput(FluidIngredientData.tag(tag.location()))),
                        RecipeEditorUi.fieldGroup("viscript_recipe.config.create.fluid_ingredient.amount",
                                RecipeEditorUi.intField(ingredient.getAmount(), 1, Integer.MAX_VALUE,
                                        value -> setSelectedFluidInput(ingredient.setAmount(value))))
                );
            } else {
                content.addChild(PropertiesView.createFluidStackConfigurator(
                        "viscript_recipe.config.create.fluid_ingredient.fluid",
                        () -> getSelectedFluidInput().getFluid(),
                        stack -> setSelectedFluidInput(FluidIngredientData.fluid(stack))
                ));
            }
        } else {
            content.addChildren(
                    RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.fluid"),
                    PropertiesView.createFluidStackConfigurator(
                            selectedFluidConfigNameKey(),
                            this::getSelectedFluidOutput, this::setSelectedFluidOutput
                    )
            );
        }
    }

    public void setSelectedFluidIngredientKind(FluidIngredientKind kind) {
        var ingredient = getSelectedFluidInput();
        ingredient.setKind(kind);
        if (kind == FluidIngredientKind.TAG) {
            if (ingredient.getTag() == null) ingredient.setTag(new ResourceLocation("c", "milk"));
            if (ingredient.getAmount() <= 0) ingredient.setAmount(1000);
        }
        setSelectedFluidInput(ingredient);
        navigationView.refreshPropertiesView();
    }

    public String selectedFluidConfigNameKey() {
        if (entry.isType(RecipeEditorTypes.CREATE_SEQUENCED_ASSEMBLY)) {
            return "viscript_recipe.config.create.sequenced_assembly.step.fluid_ingredient";
        }
        return selectedSlotIndex() >= CREATE_MAX_FLUID_INPUTS
                ? "viscript_recipe.config.create.fluid_output"
                : "viscript_recipe.config.create.fluid_ingredient.fluid";
    }

    public FluidIngredientData getVisualFluidInput(int index) {
        try {
            return visualFluidInputs[index].copy();
        } catch (Exception e) {
            return FluidIngredientData.empty();
        }
    }
    public void setVisualFluidInput(int index, FluidIngredientData input) {
        try {
            visualFluidInputs[index] = input.copy();
            fluidInputSlots[index].setFluidIngredient(input);
        } catch (Exception ignored) {}
    }
    public FluidIngredientData getSelectedFluidInput() {return getVisualFluidInput(selectedSlotIndex());}
    public void setSelectedFluidInput(FluidIngredientData input) {setVisualFluidInput(selectedSlotIndex(), input);}

    public FluidStack getVisualFluidOutput(int index) {
        try {
            return visualFluidOutputs[index].copy();
        } catch (Exception e) {
            return FluidStack.EMPTY;
        }
    }
    public void setVisualFluidOutput(int index, FluidStack stack) {
        try {
            visualFluidOutputs[index] = stack.copy();
            fluidOutputSlots[index].setFluid(stack.copy(), false);
        } catch (Exception ignored) {}
    }
    public FluidStack getSelectedFluidOutput() {return getVisualFluidOutput(selectedSlotIndex() - 2);}
    public void setSelectedFluidOutput(FluidStack stack) {setVisualFluidOutput(selectedSlotIndex() - 2, stack);}

    public FluidDisplaySlot createFluidInputSlot(int index) {
        fluidInputSlots[index] = createFluidDisplaySlot();
        return configureFluidInputSlot(index);
    }

    public FluidSlot createFluidOutputSlot(int index) {
        fluidOutputSlots[index] = createFluidSlot();
        return configureFluidOutputSlot(index);
    }

    public static FluidSlot createFluidSlot() {
        return (FluidSlot) new FluidSlot()
                .xeiPhantom()
                .setAllowClickFilled(false)
                .setAllowClickDrained(false)
                .slotStyle(style -> style.showFluidTooltips(true))
                .layout(layout -> {
                    layout.width(30);
                    layout.height(30);
                });
    }

    public static FluidDisplaySlot createFluidDisplaySlot() {
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

    public FluidDisplaySlot configureFluidInputSlot(int index) {
        var slot = fluidInputSlots[index];
        slot.registerValueListener(stack -> setVisualFluidInput(index, FluidIngredientData.fluid(stack)));
        slot.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            if (event.button == 1) {
                setVisualFluidInput(index, FluidIngredientData.empty());
                event.stopPropagation();
            }
            navigationView.setSlotSelection(SlotSelection.fluid(index));
        });
        slot.style(style -> style.tooltips(Component.translatable(
                "viscript_recipe.editor.create.fluid_input_slot", index + 1
        )));
        return slot;
    }

    public FluidSlot configureFluidOutputSlot(int index) {
        var slot = fluidOutputSlots[index];
        slot.registerValueListener(stack -> setVisualFluidOutput(index, stack));
        slot.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            if (event.button == 1) {
                setVisualFluidOutput(index, FluidStack.EMPTY);
                event.stopPropagation();
            } // todo 注意索引
            navigationView.setSlotSelection(SlotSelection.fluid(index + 2));
        });
        slot.style(style -> style.tooltips(Component.translatable(
                "viscript_recipe.editor.create.fluid_output_slot", index + 1
        )));
        return slot;
    }
}
