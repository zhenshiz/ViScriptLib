package com.viscript_recipe.gui.editor;

import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;

class IngredientDisplaySlot extends ItemSlot {
    private ItemStack[] tagDisplayStacks = new ItemStack[0];
    private int tagDisplayIndex;
    private int tagDisplayTicks;

    void setTagDisplayStacks(ItemStack[] stacks) {
        if (stacks == null || stacks.length == 0) {
            clearTagDisplayStacks();
            return;
        }
        var copies = new ItemStack[stacks.length];
        for (int i = 0; i < stacks.length; i++) {
            copies[i] = stacks[i] == null ? ItemStack.EMPTY : stacks[i].copy();
        }
        if (sameStacks(tagDisplayStacks, copies)) {
            return;
        }
        tagDisplayStacks = copies;
        tagDisplayIndex = 0;
        tagDisplayTicks = 0;
        setItem(tagDisplayStacks[0].copy(), false);
    }

    void clearTagDisplayStacks() {
        if (tagDisplayStacks.length == 0) {
            return;
        }
        tagDisplayStacks = new ItemStack[0];
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
        setItem(tagDisplayStacks[tagDisplayIndex].copy(), false);
    }

    private boolean sameStacks(ItemStack[] left, ItemStack[] right) {
        return left.length == right.length && Arrays.equals(stackKeys(left), stackKeys(right));
    }

    private String[] stackKeys(ItemStack[] stacks) {
        var keys = new String[stacks.length];
        for (int i = 0; i < stacks.length; i++) {
            var stack = stacks[i];
            keys[i] = stack == null || stack.isEmpty()
                    ? ""
                    : stack.getItemHolder().unwrapKey().map(Object::toString).orElse(stack.getItem().toString()) + "x" + stack.getCount();
        }
        return keys;
    }
}
