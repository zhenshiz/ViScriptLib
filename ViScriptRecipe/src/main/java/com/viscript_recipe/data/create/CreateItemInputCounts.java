package com.viscript_recipe.data.create;

import com.viscript_recipe.data.IngredientValueKind;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.data.RecipeIngredientValue;
import net.minecraft.world.item.ItemStack;

public final class CreateItemInputCounts {
    private CreateItemInputCounts() {
    }

    public static int slotWeight(RecipeIngredient ingredient) {
        if (isEmpty(ingredient)) {
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
        if (maxWeight <= 0 || isEmpty(ingredient)) {
            return new RecipeIngredient();
        }
        var copy = copyIngredient(ingredient);
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
            return new RecipeIngredient();
        }
        var copy = stack.copy();
        copy.setCount(Math.max(1, Math.min(maxWeight, copy.getCount())));
        var ingredient = new RecipeIngredient();
        ingredient.getValues().add(new RecipeIngredientValue()
                .setKind(IngredientValueKind.ITEM)
                .setItem(copy));
        return ingredient;
    }

    public static boolean isEmpty(RecipeIngredient ingredient) {
        if (ingredient == null || ingredient.getValues().isEmpty()) {
            return true;
        }
        for (var value : ingredient.getValues()) {
            if (value == null) {
                continue;
            }
            switch (value.getKind()) {
                case ITEM -> {
                    if (value.getItem() != null && !value.getItem().isEmpty()) {
                        return false;
                    }
                }
                case TAG -> {
                    if (value.getTag() != null) {
                        return false;
                    }
                }
                case ITEM_ABILITY -> {
                    if (value.getItemAbility() != null && !value.getItemAbility().isBlank()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static RecipeIngredientValue singleItemValue(RecipeIngredient ingredient) {
        if (ingredient == null || ingredient.getValues().size() != 1) {
            return null;
        }
        var value = ingredient.getValues().get(0);
        return value != null && value.getKind() == IngredientValueKind.ITEM ? value : null;
    }

    private static RecipeIngredient copyIngredient(RecipeIngredient original) {
        var copy = new RecipeIngredient();
        if (original == null) {
            return copy;
        }
        for (var value : original.getValues()) {
            if (value == null) {
                continue;
            }
            var valueCopy = new RecipeIngredientValue()
                    .setKind(value.getKind())
                    .setTag(value.getTag())
                    .setItemAbility(value.getItemAbility());
            if (value.getItem() != null) {
                valueCopy.setItem(value.getItem().copy());
            }
            copy.getValues().add(valueCopy);
        }
        return copy;
    }
}
