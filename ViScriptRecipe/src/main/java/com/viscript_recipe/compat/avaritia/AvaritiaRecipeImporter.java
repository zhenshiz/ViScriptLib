package com.viscript_recipe.compat.avaritia;

import com.viscript_recipe.compat.avaritia.data.AvaritiaCompressorRecipeData;
import com.viscript_recipe.compat.avaritia.data.AvaritiaExtremeSmithingRecipeData;
import com.viscript_recipe.compat.avaritia.data.AvaritiaSpecialShapelessRecipeData;
import com.viscript_recipe.compat.avaritia.data.AvaritiaTableRecipeData;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.recipe.importer.RecipeImportException;
import com.viscript_recipe.recipe.importer.RecipeImportHandler;
import com.viscript_recipe.recipe.importer.RecipeImportResult;
import com.viscript_recipe.recipe.importer.RecipeImporter;
import committee.nova.mods.avaritia.common.crafting.recipe.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.common.crafting.CompoundIngredient;

import java.util.ArrayList;
import java.util.List;

public final class AvaritiaRecipeImporter implements RecipeImportHandler {
    public static final AvaritiaRecipeImporter INSTANCE = new AvaritiaRecipeImporter();

    private AvaritiaRecipeImporter() {
    }

    @Override
    public boolean canImport(Recipe<?> recipe) {
        if (recipe == null) {
            return false;
        }
        return recipe instanceof ShapedTableCraftingRecipe
                || recipe instanceof InfinityCatalystCraftRecipe
                || recipe instanceof EternalSingularityCraftRecipe
                || recipe instanceof FullMatterClusterRecipe
                || recipe instanceof ShapelessTableCraftingRecipe && !isSpecialShapeless(recipe)
                || recipe instanceof CompressorRecipe
                || recipe instanceof ExtremeSmithingRecipe;
    }

    @Override
    public RecipeImportResult tryImport(Recipe<?> recipe, HolderLookup.Provider provider) throws RecipeImportException {
        if (recipe instanceof NoConsumeCatalystShapedRecipe noConsume) {
            return RecipeImporter.success(importShapedTable(recipe, provider, noConsume, AvaritiaRecipeEditorTypes.NO_CONSUME_CATALYST_SHAPED, false));
        }
        if (recipe instanceof ShapedTableCraftingRecipe shaped) {
            return RecipeImporter.success(importShapedTable(recipe, provider, shaped, AvaritiaRecipeEditorTypes.SHAPED_TABLE, shaped.isCompatible()));
        }
        if (recipe instanceof ShapelessTableCraftingRecipe shapeless && !isSpecialShapeless(shapeless)) {
            var tier = Math.max(1, shapeless.getTier());
            var gridSize = AvaritiaRecipeEditorTypes.tableGridSizeForTier(tier);
            var data = new AvaritiaTableRecipeData()
                    .setTier(tier)
                    .setWidth(gridSize)
                    .setHeight(gridSize)
                    .setShapelessIngredients(new ArrayList<>(RecipeImporter.importIngredientList(shapeless.getIngredients(), 81)))
                    .setResult(RecipeImporter.copyResult(shapeless, provider));
            return RecipeImporter.success(RecipeImporter.baseEntry(recipe.getId(), AvaritiaRecipeEditorTypes.SHAPELESS_TABLE).setData(data));
        }
        if (recipe instanceof InfinityCatalystCraftRecipe catalyst) {
            var result = RecipeImporter.copyResult(catalyst, provider);
            var data = new AvaritiaSpecialShapelessRecipeData()
                    .setGroup(catalyst.getGroup())
                    .setIngredients(new ArrayList<>(RecipeImporter.importIngredientList(catalyst.getIngredients(), 81)))
                    .setCount(Math.max(1, result.getCount()));
            return RecipeImporter.success(RecipeImporter.baseEntry(recipe.getId(), AvaritiaRecipeEditorTypes.INFINITY_CATALYST)
                    .setData(data));
        }
        if (recipe instanceof EternalSingularityCraftRecipe eternalSingularity) {
            var result = RecipeImporter.copyResult(eternalSingularity, provider);
            var data = new AvaritiaSpecialShapelessRecipeData()
                    .setIngredients(new ArrayList<>(RecipeImporter.importIngredientList(eternalSingularity.originalInputs, 81)))
                    .setCount(Math.max(1, result.getCount()));
            return RecipeImporter.success(RecipeImporter.baseEntry(recipe.getId(), AvaritiaRecipeEditorTypes.ETERNAL_SINGULARITY)
                    .setData(data));
        }
        if (recipe instanceof FullMatterClusterRecipe fullMatterCluster) {
            var result = RecipeImporter.copyResult(fullMatterCluster, provider);
            var data = new AvaritiaSpecialShapelessRecipeData()
                    .setGroup(fullMatterCluster.getGroup())
                    .setIngredients(new ArrayList<>(RecipeImporter.importIngredientList(fullMatterCluster.getIngredients(), 81)))
                    .setCount(Math.max(1, result.getCount()));
            return RecipeImporter.success(RecipeImporter.baseEntry(recipe.getId(), AvaritiaRecipeEditorTypes.FULL_MATTER_CLUSTER)
                    .setData(data));
        }
        if (recipe instanceof CompressorRecipe compressor) {
            var data = new AvaritiaCompressorRecipeData()
                    .setIngredient(RecipeImporter.importIngredient(compressor.getInput()).setCount(compressor.getInputCount()))
                    .setResult(RecipeImporter.copyResult(compressor, provider))
                    .setTimeCost(compressor.getTimeCost());
            return RecipeImporter.success(RecipeImporter.baseEntry(recipe.getId(), AvaritiaRecipeEditorTypes.COMPRESSOR).setData(data));
        }
        if (recipe instanceof ExtremeSmithingRecipe smithing) {
            var data = new AvaritiaExtremeSmithingRecipeData()
                    .setTemplate(RecipeImporter.importIngredient(smithing.template))
                    .setBase(RecipeImporter.importIngredient(smithing.base))
                    .setAdditions(importAdditions(smithing.additions))
                    .setResult(RecipeImporter.copyResult(smithing, provider));
            return RecipeImporter.success(RecipeImporter.baseEntry(recipe.getId(), AvaritiaRecipeEditorTypes.EXTREME_SMITHING).setData(data));
        }
        return null;
    }

    private static RecipeEntry importShapedTable(Recipe<?> holder, HolderLookup.Provider provider, ShapedTableCraftingRecipe recipe, ResourceLocation type, boolean compatible) throws RecipeImportException {
        var pattern = RecipeImporter.importShapedPattern(recipe.getIngredients(), recipe.getWidth(), recipe.getHeight());
        var data = new AvaritiaTableRecipeData()
                .setWidth(recipe.getWidth())
                .setHeight(recipe.getHeight())
                .setTier(Math.max(1, recipe.getTier()))
                .setCompatible(compatible)
                .setPattern(pattern.pattern())
                .setKey(pattern.key())
                .setResult(RecipeImporter.copyResult(recipe, provider));
        return RecipeImporter.baseEntry(holder.getId(), type).setData(data);
    }

    private static List<RecipeIngredient> importAdditions(Ingredient additions) throws RecipeImportException {
        var imported = new ArrayList<RecipeIngredient>();
        if (additions instanceof CompoundIngredient compound) {
            for (var child : compound.getChildren()) {
                if (child != null && !child.isEmpty()) {
                    imported.add(RecipeImporter.importIngredient(child));
                }
            }
        } else if (additions != null && !additions.isEmpty()) {
            imported.add(RecipeImporter.importIngredient(additions));
        }
        while (imported.size() < 3) {
            imported.add(RecipeIngredient.empty());
        }
        return imported.stream().limit(3).toList();
    }

    private static boolean isSpecialShapeless(Object recipe) {
        return recipe instanceof InfinityCatalystCraftRecipe
                || recipe instanceof EternalSingularityCraftRecipe
                || recipe instanceof FullMatterClusterRecipe;
    }
}
