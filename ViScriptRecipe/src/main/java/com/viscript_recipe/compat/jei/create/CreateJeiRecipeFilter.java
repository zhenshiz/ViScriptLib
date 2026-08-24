package com.viscript_recipe.compat.jei.create;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.kinetics.mixer.MixingRecipe;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import com.viscript_recipe.compat.create.data.CreateProcessingKind;
import com.viscript_recipe.recipe.RecipeOverrideManager;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class CreateJeiRecipeFilter {
    private static final RecipeType<BasinRecipe> AUTOMATIC_BREWING =
            RecipeType.create("create", "automatic_brewing", BasinRecipe.class);
    private static final Set<ResourceLocation> ADDED_AUTOMATIC_BREWING_IDS = new HashSet<>();

    private CreateJeiRecipeFilter() {
    }

    public static void apply(IJeiRuntime jeiRuntime, boolean showcaseOnly) {
        var recipeManager = jeiRuntime.getRecipeManager();
        if (showcaseOnly) {
            applyAutomaticBrewingShowcaseFilter(recipeManager);
            return;
        }
        restoreAutomaticBrewingRecipes(recipeManager);
    }

    private static void applyAutomaticBrewingShowcaseFilter(IRecipeManager recipeManager) {
        var allowed = currentAutomaticBrewingRecipes();
        var allowedIds = recipeIds(allowed);
        var existing = allJeiRecipes(recipeManager, AUTOMATIC_BREWING);
        var hidden = new ArrayList<BasinRecipe>();
        var visible = new ArrayList<BasinRecipe>();
        var visibleIds = new HashSet<ResourceLocation>();

        for (var recipe : existing) {
            if (ADDED_AUTOMATIC_BREWING_IDS.contains(recipe.getId())
                    && allowedIds.contains(recipe.getId())
                    && visibleIds.add(recipe.getId())) {
                visible.add(recipe);
            } else {
                hidden.add(recipe);
            }
        }
        var missing = allowed.stream()
                .filter(recipe -> !visibleIds.contains(recipe.getId()))
                .toList();

        if (!hidden.isEmpty()) {
            recipeManager.hideRecipes(AUTOMATIC_BREWING, hidden);
        }
        if (!visible.isEmpty()) {
            recipeManager.unhideRecipes(AUTOMATIC_BREWING, visible);
        }
        addAutomaticBrewingRecipes(recipeManager, missing);
        if (allowed.isEmpty()) {
            recipeManager.hideRecipeCategory(AUTOMATIC_BREWING);
        } else {
            recipeManager.unhideRecipeCategory(AUTOMATIC_BREWING);
        }
    }

    private static void restoreAutomaticBrewingRecipes(IRecipeManager recipeManager) {
        var allowed = currentAutomaticBrewingRecipes();
        var allowedIds = recipeIds(allowed);
        var existing = allJeiRecipes(recipeManager, AUTOMATIC_BREWING);
        var existingIds = recipeIds(existing);
        var staleAdded = existing.stream()
                .filter(recipe -> ADDED_AUTOMATIC_BREWING_IDS.contains(recipe.getId()) && !allowedIds.contains(recipe.getId()))
                .toList();
        var staleAddedIds = recipeIds(staleAdded);
        var visible = existing.stream()
                .filter(recipe -> !staleAddedIds.contains(recipe.getId()))
                .toList();
        var missing = allowed.stream()
                .filter(recipe -> !existingIds.contains(recipe.getId()))
                .toList();

        if (!staleAdded.isEmpty()) {
            recipeManager.hideRecipes(AUTOMATIC_BREWING, staleAdded);
        }
        if (!visible.isEmpty()) {
            recipeManager.unhideRecipes(AUTOMATIC_BREWING, visible);
        }
        addAutomaticBrewingRecipes(recipeManager, missing);
        recipeManager.unhideRecipeCategory(AUTOMATIC_BREWING);
    }

    private static void addAutomaticBrewingRecipes(IRecipeManager recipeManager, List<BasinRecipe> recipes) {
        if (recipes.isEmpty()) {
            return;
        }
        recipeManager.addRecipes(AUTOMATIC_BREWING, recipes);
        recipes.forEach(recipe -> ADDED_AUTOMATIC_BREWING_IDS.add(recipe.getId()));
    }

    private static List<BasinRecipe> currentAutomaticBrewingRecipes() {
        var level = Minecraft.getInstance().level;
        if (level == null) {
            return List.of();
        }
        var ids = Set.copyOf(RecipeOverrideManager.recipeIdsForEditorType(CreateProcessingKind.AUTOMATIC_BREWING.typeId()));
        if (ids.isEmpty()) {
            return List.of();
        }

        net.minecraft.world.item.crafting.RecipeType<MixingRecipe> mixingType = AllRecipeTypes.MIXING.getType();
        var recipes = new ArrayList<BasinRecipe>();
        for (var recipe : level.getRecipeManager().getAllRecipesFor(mixingType)) {
            if (ids.contains(recipe.getId())) {
                recipes.add(recipe);
            }
        }
        return recipes;
    }

    private static Set<ResourceLocation> recipeIds(List<BasinRecipe> recipes) {
        var ids = new HashSet<ResourceLocation>();
        for (var recipe : recipes) {
            ids.add(recipe.getId());
        }
        return ids;
    }

    private static <T> List<T> allJeiRecipes(IRecipeManager recipeManager, RecipeType<T> type) {
        return recipeManager.createRecipeLookup(type)
                .includeHidden()
                .get()
                .toList();
    }
}
