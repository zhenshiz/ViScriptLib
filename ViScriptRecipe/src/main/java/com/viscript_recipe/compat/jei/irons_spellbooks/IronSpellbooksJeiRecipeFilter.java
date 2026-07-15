/*
package com.viscript_recipe.compat.jei.irons_spellbooks;

import com.viscript_recipe.compat.irons_spellbooks.IronArcaneAnvilOverrideManager;
import io.redspace.ironsspellbooks.jei.*;
import io.redspace.ironsspellbooks.recipe_types.alchemist_cauldron.BrewAlchemistCauldronRecipe;
import io.redspace.ironsspellbooks.registries.RecipeRegistry;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class IronSpellbooksJeiRecipeFilter {
    private IronSpellbooksJeiRecipeFilter() {
    }

    public static void apply(IJeiRuntime jeiRuntime, boolean showcaseOnly) {
        var recipeManager = jeiRuntime.getRecipeManager();
        if (showcaseOnly) {
            applyAlchemistShowcaseFilter(recipeManager);
            applyArcaneAnvilShowcaseFilter(recipeManager);
            recipeManager.hideRecipeCategory(ScrollForgeRecipeCategory.SCROLL_FORGE_RECIPE_RECIPE_TYPE);
            return;
        }

        restoreAlchemistRecipes(recipeManager);
        restoreArcaneAnvilRecipes(recipeManager);
        recipeManager.unhideRecipeCategory(ArcaneAnvilRecipeCategory.ARCANE_ANVIL_RECIPE_RECIPE_TYPE);
        recipeManager.unhideRecipeCategory(ScrollForgeRecipeCategory.SCROLL_FORGE_RECIPE_RECIPE_TYPE);
    }

    private static void applyArcaneAnvilShowcaseFilter(IRecipeManager recipeManager) {
        var type = ArcaneAnvilRecipeCategory.ARCANE_ANVIL_RECIPE_RECIPE_TYPE;
        var allowed = currentArcaneAnvilRecipes();
        var allowedIds = arcaneAnvilRecipeIds(allowed);
        var existing = allJeiRecipes(recipeManager, type);
        var hidden = new ArrayList<ArcaneAnvilJeiRecipe>();
        var visible = new ArrayList<ArcaneAnvilJeiRecipe>();
        var visibleIds = new HashSet<ResourceLocation>();
        for (var recipe : existing) {
            if (recipe instanceof ViscriptArcaneAnvilJeiRecipe viscriptRecipe
                    && allowedIds.contains(viscriptRecipe.id())
                    && visibleIds.add(viscriptRecipe.id())) {
                visible.add(recipe);
            } else {
                hidden.add(recipe);
            }
        }
        var missing = allowed.stream()
                .filter(recipe -> !visibleIds.contains(recipe.id()))
                .toList();

        if (!hidden.isEmpty()) {
            recipeManager.hideRecipes(type, hidden);
        }
        if (!visible.isEmpty()) {
            recipeManager.unhideRecipes(type, visible);
        }
        if (!missing.isEmpty()) {
            recipeManager.addRecipes(type, new ArrayList<ArcaneAnvilJeiRecipe>(missing));
        }
        if (allowed.isEmpty()) {
            recipeManager.hideRecipeCategory(type);
        } else {
            recipeManager.unhideRecipeCategory(type);
        }
    }

    private static void restoreArcaneAnvilRecipes(IRecipeManager recipeManager) {
        var type = ArcaneAnvilRecipeCategory.ARCANE_ANVIL_RECIPE_RECIPE_TYPE;
        var customRecipes = currentArcaneAnvilRecipes();
        var customRecipeIds = arcaneAnvilRecipeIds(customRecipes);
        var existing = allJeiRecipes(recipeManager, type);
        var staleCustomRecipes = new ArrayList<ArcaneAnvilJeiRecipe>();
        var visibleRecipes = new ArrayList<ArcaneAnvilJeiRecipe>();
        var visibleCustomIds = new HashSet<ResourceLocation>();
        for (var recipe : existing) {
            if (recipe instanceof ViscriptArcaneAnvilJeiRecipe viscriptRecipe) {
                if (customRecipeIds.contains(viscriptRecipe.id()) && visibleCustomIds.add(viscriptRecipe.id())) {
                    visibleRecipes.add(recipe);
                } else {
                    staleCustomRecipes.add(recipe);
                }
                continue;
            }
            visibleRecipes.add(recipe);
        }
        var missing = customRecipes.stream()
                .filter(recipe -> !visibleCustomIds.contains(recipe.id()))
                .toList();

        if (!staleCustomRecipes.isEmpty()) {
            recipeManager.hideRecipes(type, staleCustomRecipes);
        }
        if (!visibleRecipes.isEmpty()) {
            recipeManager.unhideRecipes(type, visibleRecipes);
        }
        if (!missing.isEmpty()) {
            recipeManager.addRecipes(type, new ArrayList<ArcaneAnvilJeiRecipe>(missing));
        }
        recipeManager.unhideRecipeCategory(type);
    }

    private static void applyAlchemistShowcaseFilter(IRecipeManager recipeManager) {
        var type = AlchemistCauldronRecipeCategory.ALCHEMIST_CAULDRON_RECIPE_TYPE;
        var allowed = uniqueAlchemistRecipes(currentAlchemistBrewRecipes());
        var existing = allJeiRecipes(recipeManager, type);
        var hidden = new ArrayList<>(existing.stream()
                .filter(recipe -> !matchesAny(allowed, recipe))
                .toList());
        hidden.addAll(duplicateAlchemistRecipes(existing));
        var visible = uniqueAlchemistRecipes(existing.stream()
                .filter(recipe -> matchesAny(allowed, recipe))
                .toList());
        var missing = allowed.stream()
                .filter(recipe -> existing.stream().noneMatch(existingRecipe -> sameAlchemistRecipe(recipe, existingRecipe)))
                .toList();

        if (!hidden.isEmpty()) {
            recipeManager.hideRecipes(type, hidden);
        }
        if (!visible.isEmpty()) {
            recipeManager.unhideRecipes(type, visible);
        }
        if (!missing.isEmpty()) {
            recipeManager.addRecipes(type, missing);
        }
        if (allowed.isEmpty()) {
            recipeManager.hideRecipeCategory(type);
        } else {
            recipeManager.unhideRecipeCategory(type);
        }
        var advancedRecipes = new ArrayList<AlchemistCauldronJeiRecipe>(visible.size() + missing.size());
        advancedRecipes.addAll(visible);
        advancedRecipes.addAll(missing);
        AlchemistCauldronRecipeMaker.recipes = List.copyOf(advancedRecipes);
    }

    private static void restoreAlchemistRecipes(IRecipeManager recipeManager) {
        var type = AlchemistCauldronRecipeCategory.ALCHEMIST_CAULDRON_RECIPE_TYPE;
        var allRecipes = allJeiRecipes(recipeManager, type);
        var recipes = uniqueAlchemistRecipes(allRecipes);
        var duplicates = duplicateAlchemistRecipes(allRecipes);
        if (!duplicates.isEmpty()) {
            recipeManager.hideRecipes(type, duplicates);
        }
        if (!recipes.isEmpty()) {
            recipeManager.unhideRecipes(type, recipes);
        }
        recipeManager.unhideRecipeCategory(type);
        AlchemistCauldronRecipeMaker.recipes = List.copyOf(recipes);
    }

    private static List<AlchemistCauldronJeiRecipe> currentAlchemistBrewRecipes() {
        var level = Minecraft.getInstance().level;
        if (level == null) {
            return List.of();
        }
        return level.getRecipeManager()
                .getAllRecipesFor(RecipeRegistry.ALCHEMIST_CAULDRON_BREW_TYPE.get())
                .stream()
                .map(RecipeHolder::value)
                .map(IronSpellbooksJeiRecipeFilter::toJeiRecipe)
                .toList();
    }

    private static AlchemistCauldronJeiRecipe toJeiRecipe(BrewAlchemistCauldronRecipe recipe) {
        return new AlchemistCauldronJeiRecipe(
                recipe.reagent(),
                recipe.fluidIn(),
                recipe.results(),
                recipe.byproduct().orElse(ItemStack.EMPTY)
        );
    }

    private static List<ViscriptArcaneAnvilJeiRecipe> currentArcaneAnvilRecipes() {
        var seenIds = new HashSet<ResourceLocation>();
        var recipes = new ArrayList<ViscriptArcaneAnvilJeiRecipe>();
        for (var recipe : IronArcaneAnvilOverrideManager.recipes()) {
            if (seenIds.add(recipe.id())) {
                recipes.add(new ViscriptArcaneAnvilJeiRecipe(recipe));
            }
        }
        return recipes;
    }

    private static Set<ResourceLocation> arcaneAnvilRecipeIds(List<ViscriptArcaneAnvilJeiRecipe> recipes) {
        var ids = new HashSet<ResourceLocation>();
        for (var recipe : recipes) {
            ids.add(recipe.id());
        }
        return ids;
    }

    private static <T> List<T> allJeiRecipes(IRecipeManager recipeManager, RecipeType<T> type) {
        return recipeManager.createRecipeLookup(type)
                .includeHidden()
                .get()
                .toList();
    }

    private static List<AlchemistCauldronJeiRecipe> uniqueAlchemistRecipes(List<AlchemistCauldronJeiRecipe> recipes) {
        var unique = new ArrayList<AlchemistCauldronJeiRecipe>();
        for (var recipe : recipes) {
            if (!matchesAny(unique, recipe)) {
                unique.add(recipe);
            }
        }
        return unique;
    }

    private static List<AlchemistCauldronJeiRecipe> duplicateAlchemistRecipes(List<AlchemistCauldronJeiRecipe> recipes) {
        var unique = new ArrayList<AlchemistCauldronJeiRecipe>();
        var duplicates = new ArrayList<AlchemistCauldronJeiRecipe>();
        for (var recipe : recipes) {
            if (matchesAny(unique, recipe)) {
                duplicates.add(recipe);
            } else {
                unique.add(recipe);
            }
        }
        return duplicates;
    }

    private static boolean matchesAny(List<AlchemistCauldronJeiRecipe> recipes, AlchemistCauldronJeiRecipe recipe) {
        for (var candidate : recipes) {
            if (sameAlchemistRecipe(candidate, recipe)) {
                return true;
            }
        }
        return false;
    }

    private static boolean sameAlchemistRecipe(AlchemistCauldronJeiRecipe left, AlchemistCauldronJeiRecipe right) {
        return sameIngredient(left.itemIn(), right.itemIn())
                && sameFluidStack(left.fluidIn(), right.fluidIn())
                && sameFluidStacks(left.results(), right.results())
                && sameItemStack(left.resultByproduct(), right.resultByproduct());
    }

    private static boolean sameIngredient(Ingredient left, Ingredient right) {
        var leftItems = left.getItems();
        var rightItems = right.getItems();
        if (leftItems.length != rightItems.length) {
            return false;
        }
        var matched = new boolean[rightItems.length];
        for (var leftItem : leftItems) {
            var found = false;
            for (int i = 0; i < rightItems.length; i++) {
                if (!matched[i] && sameItemStack(leftItem, rightItems[i])) {
                    matched[i] = true;
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }

    private static boolean sameItemStack(ItemStack left, ItemStack right) {
        if (left.isEmpty() || right.isEmpty()) {
            return left.isEmpty() && right.isEmpty();
        }
        return left.getCount() == right.getCount() && ItemStack.isSameItemSameComponents(left, right);
    }

    private static boolean sameFluidStacks(List<FluidStack> left, List<FluidStack> right) {
        if (left.size() != right.size()) {
            return false;
        }
        for (int i = 0; i < left.size(); i++) {
            if (!sameFluidStack(left.get(i), right.get(i))) {
                return false;
            }
        }
        return true;
    }

    private static boolean sameFluidStack(FluidStack left, FluidStack right) {
        if (left.isEmpty() || right.isEmpty()) {
            return left.isEmpty() && right.isEmpty();
        }
        return left.getAmount() == right.getAmount() && FluidStack.isSameFluidSameComponents(left, right);
    }
}
*/
