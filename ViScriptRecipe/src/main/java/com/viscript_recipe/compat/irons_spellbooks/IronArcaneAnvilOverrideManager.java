package com.viscript_recipe.compat.irons_spellbooks;

import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.data.irons_spellbooks.IronArcaneAnvilRecipeData;
import com.viscript_recipe.data.irons_spellbooks.IronSpellbooksRecipeEditorTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public final class IronArcaneAnvilOverrideManager {
    private static volatile List<CompiledRecipe> recipes = List.of();

    private IronArcaneAnvilOverrideManager() {
    }

    public static boolean isArcaneAnvilEntry(RecipeEntry entry) {
        return entry != null && entry.isType(IronSpellbooksRecipeEditorTypes.ARCANE_ANVIL_TRANSFORM);
    }

    public static CompiledRecipe compile(ResourceLocation id, IronArcaneAnvilRecipeData data) {
        if (data == null) {
            throw new IllegalArgumentException("Arcane anvil recipe data cannot be empty");
        }
        var input = compileIngredient(data.getInput(), "Arcane anvil input cannot be empty");
        var material = compileIngredient(data.getMaterial(), "Arcane anvil material cannot be empty");
        var result = data.getResult() == null ? ItemStack.EMPTY : data.getResult().copy();
        if (result.isEmpty()) {
            throw new IllegalArgumentException("Arcane anvil result cannot be empty");
        }
        return new CompiledRecipe(id, input, material, result);
    }

    public static void replaceAll(Collection<CompiledRecipe> compiledRecipes) {
        recipes = compiledRecipes == null ? List.of() : List.copyOf(compiledRecipes);
    }

    public static List<CompiledRecipe> recipes() {
        return recipes;
    }

    public static Optional<ItemStack> findResult(ItemStack input, ItemStack material) {
        if (input == null || material == null || input.isEmpty() || material.isEmpty()) {
            return Optional.empty();
        }
        for (var recipe : recipes) {
            if (recipe.matches(input, material)) {
                return Optional.of(recipe.result());
            }
        }
        return Optional.empty();
    }

    public static int recipeCount() {
        return recipes.size();
    }

    private static Ingredient compileIngredient(@Nullable RecipeIngredient ingredient, String message) {
        var compiled = ingredient == null ? Ingredient.EMPTY : ingredient.compile();
        if (compiled.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return compiled;
    }

    public record CompiledRecipe(ResourceLocation id, Ingredient input, Ingredient material, ItemStack result) {
        public boolean matches(ItemStack inputStack, ItemStack materialStack) {
            return input.test(inputStack) && material.test(materialStack);
        }

        public ItemStack result() {
            return result.copy();
        }
    }
}
