package com.viscript_recipe.data.create;

import com.viscript_recipe.data.IngredientValueKind;
import com.viscript_recipe.data.RecipeIngredient;
import net.minecraft.world.item.ItemStack;

public final class CreateItemInputCounts {
    private CreateItemInputCounts() {
    }

    public static int slotWeight(RecipeIngredient ingredient) {
        if (ingredient.isEmpty()) {
            return 0;
        }
        var value = singleItemValue(ingredient);
        if (value == null) {
            return 1;
        }
        var stack = value.getItem();
        return stack == null || stack.isEmpty() ? 0 : Math.max(1, stack.getCount());
    }

    public static RecipeIngredient copyWithClampedWeight(RecipeIngredient ingredient, int maxWeight) {
        if (maxWeight <= 0 || ingredient.isEmpty()) {
            return RecipeIngredient.empty();
        }
        var copy = ingredient.copy();
        var value = singleItemValue(copy);
        if (value != null && value.getItem() != null && !value.getItem().isEmpty()) {
            var stack = value.getItem().copy();
            stack.setCount(Math.max(1, Math.min(maxWeight, stack.getCount())));
            value.setItem(stack);
        }
        return copy;
    }

    public static RecipeIngredient item(ItemStack stack, int maxWeight) {
        if (stack == null || stack.isEmpty() || maxWeight <= 0) {
            return RecipeIngredient.empty();
        }
        var copy = stack.copy();
        copy.setCount(Math.max(1, Math.min(maxWeight, copy.getCount())));
        return RecipeIngredient.item(copy);
    }

    private static RecipeIngredient singleItemValue(RecipeIngredient ingredient) {
        return ingredient != null && ingredient.getKind() == IngredientValueKind.ITEM ? ingredient : null;
    }
}
