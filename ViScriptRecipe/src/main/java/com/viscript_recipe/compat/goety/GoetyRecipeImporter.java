package com.viscript_recipe.compat.goety;

import com.Polarice3.Goety.common.crafting.*;
import com.viscript_recipe.compat.goety.data.*;
import com.viscript_recipe.recipe.importer.RecipeImportException;
import com.viscript_recipe.recipe.importer.RecipeImportHandler;
import com.viscript_recipe.recipe.importer.RecipeImportResult;
import com.viscript_recipe.recipe.importer.RecipeImporter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Blocks;

/**
 * Imports Goety's five JEI-backed recipe classes into editor-owned data.
 */
public final class GoetyRecipeImporter implements RecipeImportHandler {
    public static final GoetyRecipeImporter INSTANCE = new GoetyRecipeImporter();

    private GoetyRecipeImporter() {
    }

    @Override
    public boolean canImport(Recipe<?> holder) {
        return (holder instanceof CursedInfuserRecipes
                || holder instanceof RitualRecipe
                || holder instanceof BrazierRecipe
                || holder instanceof PulverizeRecipe
                || holder instanceof BrewingRecipe);
    }

    @Override
    public RecipeImportResult tryImport(Recipe<?> holder, HolderLookup.Provider provider) throws RecipeImportException {
        if (holder instanceof CursedInfuserRecipes recipe) {
            var data = new GoetyCursedInfuserRecipeData()
                    .setIngredient(RecipeImporter.importIngredient(recipe.getIngredients().get(0)))
                    .setResult(RecipeImporter.copyResult(recipe, provider))
                    .setCookingTime(Math.max(1, recipe.getCookingTime()))
                    .setGrim(recipe.isGrim());
            return success(holder, GoetyRecipeEditorTypes.CURSED_INFUSER_RECIPE, entry -> entry.setData(data));
        }
        if (holder instanceof RitualRecipe recipe) {
            var enchantmentId = recipe.getEnchantment() == null
                    ? null
                    : BuiltInRegistries.ENCHANTMENT.getKey(recipe.getEnchantment());
            var data = new GoetyRitualRecipeData()
                    .setActivationItem(RecipeImporter.importIngredient(recipe.getActivationItem()))
                    .setIngredients(RecipeImporter.importIngredientList(recipe.getIngredients(), GoetyRitualRecipeData.MAX_PEDESTAL_INGREDIENTS))
                    .setResult(RecipeImporter.copyResult(recipe, provider))
                    .setCraftType(GoetyRitualCraftType.byName(recipe.getCraftType()))
                    .setRitualType(recipe.getRitualType())
                    .setSoulCost(Math.max(0, recipe.getSoulCost()))
                    .setDuration(Math.max(1, recipe.getDuration()))
                    .setSummonLife(recipe.getSummonLife())
                    .setHasSacrifice(recipe.getEntityToSacrifice() != null)
                    .setEntityToSacrifice(recipe.getEntityToSacrifice() == null ? null : recipe.getEntityToSacrifice().location())
                    .setEntityToSacrificeDisplayName(recipe.getEntityToSacrificeDisplayName())
                    .setHasSummon(recipe.getEntityToSummon() != null)
                    .setEntityToSummon(entityId(recipe.getEntityToSummon()))
                    .setHasConversion(recipe.getEntityToConvert() != null || recipe.getEntityToConvertInto() != null)
                    .setEntityToConvert(recipe.getEntityToConvert() == null ? null : recipe.getEntityToConvert().location())
                    .setEntityToConvertDisplayName(recipe.getEntityToConvertDisplayName())
                    .setEntityToConvertInto(entityId(recipe.getEntityToConvertInto()))
                    .setHasStructure(recipe.getStructureTag() != null)
                    .setStructureToLocate(recipe.getStructureTag() == null ? null : recipe.getStructureTag().location())
                    .setStructureDisplayName(recipe.getStructureName())
                    .setHasEnchantment(enchantmentId != null)
                    .setEnchantment(enchantmentId)
                    .setXpLevelCost(Math.max(0, recipe.getXPLevelCost()))
                    .setResearch(recipe.getResearch());
            return success(holder, GoetyRecipeEditorTypes.RITUAL, entry -> entry.setData(data));
        }
        if (holder instanceof BrazierRecipe recipe) {
            var data = new GoetyBrazierRecipeData()
                    .setIngredients(RecipeImporter.importIngredientList(recipe.getIngredients(), GoetyBrazierRecipeData.INPUT_COUNT))
                    .setResult(RecipeImporter.copyResult(recipe, provider))
                    .setSoulCost(Math.max(0, recipe.getSoulCost()));
            return success(holder, GoetyRecipeEditorTypes.BRAZIER, entry -> entry.setData(data));
        }
        if (holder instanceof PulverizeRecipe recipe) {
            var itemResult = RecipeImporter.copyResult(recipe, provider);
            var blockResult = recipe.getBlockResult();
            var itemMode = !itemResult.isEmpty();
            var data = new GoetyPulverizeRecipeData()
                    .setIngredient(RecipeImporter.importIngredient(recipe.getIngredients().get(0)))
                    .setResultKind(itemMode ? GoetyPulverizeResultKind.ITEM : GoetyPulverizeResultKind.BLOCK)
                    .setItemResult(itemResult)
                    .setBlockResult(blockResult == null || blockResult == Blocks.CAVE_AIR
                            ? new ResourceLocation("cobblestone")
                            : BuiltInRegistries.BLOCK.getKey(blockResult));
            return success(holder, GoetyRecipeEditorTypes.PULVERIZE, entry -> entry.setData(data));
        }
        if (holder instanceof BrewingRecipe recipe) {
            var entityKind = recipe.getEntityTypeTag() != null
                    ? GoetyBrewingEntityKind.TAG
                    : recipe.getEntityType() != null ? GoetyBrewingEntityKind.ENTITY : GoetyBrewingEntityKind.NONE;
            var entity = recipe.getEntityTypeTag() != null
                    ? recipe.getEntityTypeTag().location()
                    : entityId(recipe.getEntityType());
            var data = new GoetyBrewingRecipeData()
                    .setIngredient(RecipeImporter.importIngredient(recipe.getInput()))
                    .setEffect(BuiltInRegistries.MOB_EFFECT.getKey(recipe.getOutput()))
                    .setSoulCost(Math.max(0, recipe.getSoulCost()))
                    .setCapacityExtra(Math.max(0, recipe.getCapacityExtra()))
                    .setDuration(Math.max(1, recipe.getDuration()))
                    .setEntityKind(entityKind)
                    .setEntity(entity);
            return success(holder, GoetyRecipeEditorTypes.BREWING, entry -> entry.setData(data));
        }
        return null;
    }

    private static RecipeImportResult success(Recipe<?> holder, ResourceLocation type,
                                              java.util.function.Consumer<com.viscript_recipe.data.RecipeEntry> consumer) {
        var entry = RecipeImporter.baseEntry(holder.getId(), type);
        consumer.accept(entry);
        return RecipeImporter.success(entry);
    }

    private static ResourceLocation entityId(net.minecraft.world.entity.EntityType<?> entityType) {
        return entityType == null ? null : BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
    }
}
