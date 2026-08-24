package com.viscript_recipe.compat.farmersdelight;

import com.viscript_recipe.compat.farmersdelight.data.FarmerCookingPotRecipeData;
import com.viscript_recipe.compat.farmersdelight.data.FarmerCuttingRecipeData;
import com.viscript_recipe.data.RecipeEditorTypes;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.data.RecipeOutputData;
import com.viscript_recipe.recipe.importer.RecipeImportException;
import com.viscript_recipe.recipe.importer.RecipeImportHandler;
import com.viscript_recipe.recipe.importer.RecipeImportResult;
import com.viscript_recipe.recipe.importer.RecipeImporter;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;
import vectorwing.farmersdelight.common.crafting.CuttingBoardRecipe;
import vectorwing.farmersdelight.common.crafting.ingredient.ToolActionIngredient;

import java.util.ArrayList;

public final class FarmersDelightRecipeImporter implements RecipeImportHandler {
    public static final FarmersDelightRecipeImporter INSTANCE = new FarmersDelightRecipeImporter();

    private FarmersDelightRecipeImporter() {
    }

    @Override
    public boolean canImport(Recipe<?> recipe) {
        if (recipe == null) {
            return false;
        }
        return recipe instanceof CookingPotRecipe || recipe instanceof CuttingBoardRecipe;
    }

    @Override
    public RecipeImportResult tryImport(Recipe<?> recipe, HolderLookup.Provider provider) throws RecipeImportException {
        if (recipe instanceof CookingPotRecipe cooking) {
            return RecipeImporter.success(importCooking(recipe.getId(), cooking, provider));
        }
        if (recipe instanceof CuttingBoardRecipe cutting) {
            return RecipeImporter.success(importCutting(recipe.getId(), cutting, provider));
        }
        return null;
    }

    private static RecipeEntry importCooking(ResourceLocation id, CookingPotRecipe recipe, HolderLookup.Provider provider) throws RecipeImportException {
        var data = new FarmerCookingPotRecipeData()
                .setIngredients(new ArrayList<>(RecipeImporter.importIngredientList(recipe.getIngredients(), CookingPotRecipe.INPUT_SLOTS)))
                .setResult(RecipeImporter.copyResult(recipe, provider))
                .setContainer(RecipeImporter.copyStack(recipe.getOutputContainer()))
                .setExperience(recipe.getExperience())
                .setCookingTime(Math.max(1, recipe.getCookTime()));
        return RecipeImporter.baseEntry(id, RecipeEditorTypes.FARMERSDELIGHT_COOKING).setData(data);
    }

    private static RecipeEntry importCutting(ResourceLocation id, CuttingBoardRecipe recipe, HolderLookup.Provider provider) throws RecipeImportException {
        var ingredients = recipe.getIngredients();
        if (ingredients.isEmpty()) {
            throw new RecipeImportException("viscript_recipe.editor.import_recipe.error.empty_ingredient");
        }
        var results = new ArrayList<RecipeOutputData>();
        for (var result : recipe.getRollableResults()) {
            if (result != null && result.getStack() != null && !result.getStack().isEmpty()) {
                results.add(RecipeOutputData.of(result.getStack(), result.getChance()));
            }
        }
        var sound = new ResourceLocation(recipe.getSoundEventID());
        var data = new FarmerCuttingRecipeData()
                .setInput(RecipeImporter.importIngredient(ingredients.get(0)))
                .setTool(importTool(recipe.getTool()))
                .setResults(results)
                .setCustomSound(!recipe.getSoundEventID().isBlank())
                .setSound(sound);
        if (data.getResults().isEmpty()) {
            data.getResults().add(RecipeOutputData.of(RecipeImporter.copyResult(recipe, provider)));
        }
        return RecipeImporter.baseEntry(id, RecipeEditorTypes.FARMERSDELIGHT_CUTTING).setData(data);
    }

    private static RecipeIngredient importTool(Ingredient ingredient) throws RecipeImportException {
        if (ingredient instanceof ToolActionIngredient itemAbility) {
            return RecipeIngredient.itemAbility(itemAbility.toolAction.name());
        }
        return RecipeImporter.importIngredient(ingredient);
    }
}
