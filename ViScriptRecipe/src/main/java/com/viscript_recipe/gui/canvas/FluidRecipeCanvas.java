package com.viscript_recipe.gui.canvas;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.FluidSlot;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.viscript_recipe.data.*;
import com.viscript_recipe.gui.editor.FluidDisplaySlot;
import com.viscript_recipe.gui.editor.SlotSelection;
import com.viscript_recipe.gui.views.NavigationView;
import com.viscript_recipe.gui.views.PropertiesView;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import java.util.Arrays;
import java.util.List;

/**
 * 目前所有包含流体的配方中，除了机械动力模组的序列装配配方，都没有需要超过2个流体输入和两个流体输出的。因此流体槽位的选择索引没有区分输入和输出。 <p>
 * 当前的流体属性配置（不包括序列装配）：索引0-1为输入，索引2-3为输出。万一以后遇到需要更多输入槽位的配方，根据实际需要调整即可。
 */
public abstract class FluidRecipeCanvas<D extends IVSRecipeData> extends RecipeCanvas<D> {
    protected static final int CREATE_MAX_FLUID_INPUTS = 2;
    protected static final int CREATE_MAX_FLUID_OUTPUTS = 2;

    public static final FluidDisplaySlot[] fluidInputSlots = new FluidDisplaySlot[81];
    public static final FluidSlot[] fluidOutputSlots = new FluidSlot[2];

    public FluidRecipeCanvas(NavigationView navigationView, RecipeEntry entry) {super(navigationView, entry);}

    @Override
    public void initVisualState() {
        Arrays.fill(fluidInputSlots, null);
        Arrays.fill(fluidOutputSlots, null);
        super.initVisualState();
    }

    @Override
    public void buildFluidProperties(UIElement content) {
        content.addChild(sectionTitle("viscript_recipe.editor.properties.fluid"));
        if (selectedSlotIndex() < 2 || entry.isType(RecipeEditorTypes.CREATE_SEQUENCED_ASSEMBLY)) {
            var ingredient = getSelectedFluidInput();
            var kind = ingredient.getKind();
            content.addChild(selector("viscript_recipe.config.create.fluid_ingredient.kind",
                    List.of(FluidIngredientKind.values()), kind,
                    FluidIngredientKind::displayName, this::setSelectedFluidIngredientKind)
            );
            if (kind == FluidIngredientKind.TAG) content.addChild(PropertiesView.createFluidTagConfigurator(ingredient,
                    tag -> setSelectedFluidInput(ingredient.setTag(tag.location())))
            );
            else content.addChild(PropertiesView.removeCountConfig(PropertiesView.createFluidStackConfigurator(
                    selectedFluidConfigNameKey(),
                    ingredient::getFluid, stack -> setSelectedFluidInput(ingredient.setFluid(stack))
            )));
            content.addChild(intField("viscript_recipe.config.create.fluid_ingredient.amount",
                    ingredient.getAmount(), 1, Integer.MAX_VALUE,
                            value -> setSelectedFluidInput(ingredient.setAmount(value))));
        } else content.addChild(PropertiesView.createFluidStackConfigurator(selectedFluidConfigNameKey(),
                this::getSelectedFluidOutput, this::setSelectedFluidOutput)
        );
    }

    public void setSelectedFluidIngredientKind(FluidIngredientKind kind) {
        var ingredient = getSelectedFluidInput();
        ingredient.setKind(kind);
        if (kind == FluidIngredientKind.TAG) {
            if (ingredient.getTag() == null) ingredient.setTag(new ResourceLocation("c", "milk"));
            if (ingredient.getAmount() <= 0) ingredient.setAmount(1000);
        }
        setSelectedFluidInput(ingredient);
        reloadProperties();
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
            return fluidInputSlots[index].getIngredient().copy();
        } catch (Exception e) {
            return FluidIngredientData.empty();
        }
    }
    public void setVisualFluidInput(int index, FluidIngredientData input) {
        try {
            fluidInputSlots[index].setFluidIngredient(input);
        } catch (Exception ignored) {}
    }
    public FluidIngredientData getSelectedFluidInput() {return getVisualFluidInput(selectedSlotIndex());}
    public void setSelectedFluidInput(FluidIngredientData input) {setVisualFluidInput(selectedSlotIndex(), input);}

    public FluidStack getVisualFluidOutput(int index) {
        try {
            return fluidOutputSlots[index].getFluid().copy();
        } catch (Exception e) {
            return FluidStack.EMPTY;
        }
    }
    public void setVisualFluidOutput(int index, FluidStack stack) {
        try {
            fluidOutputSlots[index].setFluid(stack.copy(), false);
        } catch (Exception ignored) {}
    }
    public FluidStack getSelectedFluidOutput() {return getVisualFluidOutput(selectedSlotIndex() - 2);}
    public void setSelectedFluidOutput(FluidStack stack) {setVisualFluidOutput(selectedSlotIndex() - 2, stack);}

    public FluidDisplaySlot createFluidInputSlot(int index) {
        fluidInputSlots[index] = createFluidDisplaySlot();
        return configureFluidInputSlot(index);
    }

    public FluidSlot createFluidOutputSlot(int index) {return createFluidOutputSlot(index, 30);}
    public FluidSlot createFluidOutputSlot(int index, int size) {
        fluidOutputSlots[index] = createFluidSlot(size);
        return configureFluidOutputSlot(index);
    }

    public static FluidSlot createFluidSlot(int size) {
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
        slot.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            if (event.button == 1) {
                setVisualFluidInput(index, FluidIngredientData.empty());
                event.stopPropagation();
            }
            selectSlot(SlotSelection.fluid(index));
        });
        slot.style(style -> style.tooltips(Component.translatable(
                "viscript_recipe.editor.create.fluid_input_slot", index + 1
        )));
        return slot;
    }

    public FluidSlot configureFluidOutputSlot(int index) {
        var slot = fluidOutputSlots[index];
        slot.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            if (event.button == 1) {
                setVisualFluidOutput(index, FluidStack.EMPTY);
                event.stopPropagation();
            }
            selectSlot(SlotSelection.fluid(index + 2));
        });
        slot.style(style -> style.tooltips(Component.translatable(
                "viscript_recipe.editor.create.fluid_output_slot", index + 1
        )));
        return slot;
    }

    public static void configureJeiOverlayFluidSlotVisual(FluidSlot slot) {
        slot.style(style -> style.backgroundTexture(IGuiTexture.EMPTY));
        slot.slotStyle(style -> style.slotOverlay(IGuiTexture.EMPTY));
        slot.amountLabel.setDisplay(false);
    }
}
