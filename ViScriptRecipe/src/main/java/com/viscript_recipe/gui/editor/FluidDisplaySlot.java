package com.viscript_recipe.gui.editor;

import com.lowdragmc.lowdraglib2.gui.ui.elements.FluidSlot;
import com.viscript_recipe.data.FluidIngredientData;
import lombok.Getter;
import net.minecraftforge.fluids.FluidStack;

import java.util.Arrays;

public class FluidDisplaySlot extends FluidSlot {
    @Getter
    private FluidIngredientData ingredient = FluidIngredientData.empty();
    private FluidStack[] tagDisplayStacks = new FluidStack[0];
    private int tagDisplayIndex;
    private int tagDisplayTicks;

    public void setFluidIngredient(FluidIngredientData ingredient) {
        this.ingredient = ingredient;
        setTagDisplayStacks(ingredient.getFluidStacks());
    }

    void setTagDisplayStacks(FluidStack[] stacks) {
        if (stacks == null || stacks.length == 0) {
            clearTagDisplayStacks();
            return;
        }
        var copies = new FluidStack[stacks.length];
        for (int i = 0; i < stacks.length; i++) {
            copies[i] = stacks[i] == null ? FluidStack.EMPTY : stacks[i].copy();
        }
        if (sameStacks(tagDisplayStacks, copies)) {
            return;
        }
        tagDisplayStacks = copies;
        tagDisplayIndex = 0;
        tagDisplayTicks = 0;
        setFluid(tagDisplayStacks[0].copy(), false);
    }

    void clearTagDisplayStacks() {
        setFluid(FluidStack.EMPTY, false);
        if (tagDisplayStacks.length == 0) {
            return;
        }
        tagDisplayStacks = new FluidStack[0];
        tagDisplayIndex = 0;
        tagDisplayTicks = 0;
    }

    @Override
    public void screenTick() {
        super.screenTick();
        if (tagDisplayStacks.length <= 1) {
            return;
        }
        tagDisplayTicks++;
        if (tagDisplayTicks < 20) {
            return;
        }
        tagDisplayTicks = 0;
        tagDisplayIndex = (tagDisplayIndex + 1) % tagDisplayStacks.length;
        setFluid(tagDisplayStacks[tagDisplayIndex].copy(), false);
    }

    private boolean sameStacks(FluidStack[] left, FluidStack[] right) {
        return left.length == right.length && Arrays.equals(stackKeys(left), stackKeys(right));
    }

    private String[] stackKeys(FluidStack[] stacks) {
        var keys = new String[stacks.length];
        for (int i = 0; i < stacks.length; i++) {
            var stack = stacks[i];
            keys[i] = stack == null || stack.isEmpty()
                    ? ""
                    : stack.getFluid().toString() + "#" + stack.getAmount();
        }
        return keys;
    }
}
