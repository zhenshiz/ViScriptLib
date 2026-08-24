package com.viscript_recipe.gui.editor;

import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.viscript_recipe.data.IngredientValueKind;
import com.viscript_recipe.data.RecipeIngredient;
import lombok.Getter;
import net.minecraft.world.item.ItemStack;

public class IngredientDisplaySlot extends ItemSlot {
    @Getter
    private RecipeIngredient ingredient = RecipeIngredient.empty();
    private ItemStack[] tagDisplayStacks = new ItemStack[0];
    private int tagDisplayIndex;
    private int tagDisplayTicks;

    public void setIngredient(RecipeIngredient ingredient) {
        // 因为内部调用setItem后会重复触发事件，通过判断新旧物品是否匹配来避免重复设置（为什么内部不把notify设置为false？因为有必要触发事件）
        if (ingredient.getKind() == IngredientValueKind.ITEM && tagDisplayStacks.length > 0
                && ItemStack.matches(tagDisplayStacks[tagDisplayIndex], ingredient.toStack())) return;
        this.ingredient = ingredient;
        setTagDisplayStacks(ingredient.getDisplayStacks());
    }

    public void setTagDisplayStacks(ItemStack[] stacks) {
        if (stacks == null || stacks.length == 0) {
            clearTagDisplayStacks();
            return;
        }
        var copies = new ItemStack[stacks.length];
        for (int i = 0; i < stacks.length; i++) {
            copies[i] = stacks[i] == null ? ItemStack.EMPTY : stacks[i].copy();
        }
        tagDisplayStacks = copies;
        tagDisplayIndex = 0;
        tagDisplayTicks = 0;
        setItem(tagDisplayStacks[0], true);
    }

    public void clearTagDisplayStacks() {
        ingredient = RecipeIngredient.empty();
        setItem(ItemStack.EMPTY, true);
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
        setItem(tagDisplayStacks[tagDisplayIndex], true);
    }
}
