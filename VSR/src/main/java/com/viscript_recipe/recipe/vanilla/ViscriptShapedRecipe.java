package com.viscript_recipe.recipe.vanilla;

import com.viscript_recipe.ViScriptRecipe;
import com.viscript_recipe.data.vanilla.CraftingRemainderRule;
import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.ShapedRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ViscriptShapedRecipe extends ShapedRecipe {
    private final List<CraftingRemainderRule> remainders;

    public ViscriptShapedRecipe(String group, CraftingBookCategory category, ShapedRecipePattern pattern, ItemStack result, boolean showNotification, List<CraftingRemainderRule> remainders) {
        super(ViScriptRecipe.placeholder, group, category, pattern.width(), pattern.height(), pattern.ingredients(), result, showNotification);
        this.remainders = remainders == null ? List.of() : remainders.stream().map(CraftingRemainderRule::copy).toList();
    }

    @Override
    public @NotNull NonNullList<ItemStack> getRemainingItems(CraftingContainer input) {
        var remaining = NonNullList.withSize(input.getContainerSize(), ItemStack.EMPTY);
        var mirrored = shouldUseMirroredRemainders(input);
        for (int index = 0; index < remaining.size(); index++) {
            var ruleIndex = mirrored ? mirroredIndex(index) : index;
            var rule = ruleIndex < remainders.size() ? remainders.get(ruleIndex) : CraftingRemainderRule.defaultRule();
            remaining.set(index, rule.apply(input.getItem(index)));
        }
        return remaining;
    }

    private boolean shouldUseMirroredRemainders(CraftingContainer input) {
        if (input.getWidth() != getWidth() || input.getHeight() != getHeight()) {
            return false;
        }
        return !matchesPattern(input, false) && matchesPattern(input, true);
    }

    private boolean matchesPattern(CraftingContainer input, boolean mirrored) {
        var ingredients = getIngredients();
        var width = getWidth();
        var height = getHeight();
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                var ingredientIndex = mirrored ? width - col - 1 + row * width : col + row * width;
                if (!ingredients.get(ingredientIndex).test(input.getItem(col + row * width))) {
                    return false;
                }
            }
        }
        return true;
    }

    private int mirroredIndex(int index) {
        var width = getWidth();
        var row = index / width;
        var col = index % width;
        return width - col - 1 + row * width;
    }
}
