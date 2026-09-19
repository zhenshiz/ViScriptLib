package com.viscript_recipe.compat.touhou_little_maid;

import com.github.tartaricacid.touhoulittlemaid.crafting.AltarRecipe;
import com.viscript_recipe.compat.touhou_little_maid.data.TouhouLittleMaidAltarRecipeData;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.recipe.importer.RecipeImportException;
import com.viscript_recipe.recipe.importer.RecipeImportHandler;
import com.viscript_recipe.recipe.importer.RecipeImportResult;
import com.viscript_recipe.recipe.importer.RecipeImporter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.Recipe;

import java.util.ArrayList;

public final class TouhouLittleMaidRecipeImporter implements RecipeImportHandler {
    public static final TouhouLittleMaidRecipeImporter INSTANCE = new TouhouLittleMaidRecipeImporter();

    private TouhouLittleMaidRecipeImporter() {
    }

    @Override
    public boolean canImport(Recipe<?> holder) {
        return holder instanceof AltarRecipe;
    }

    @Override
    public RecipeImportResult tryImport(Recipe<?> holder, HolderLookup.Provider provider) throws RecipeImportException {
        if (!(holder instanceof AltarRecipe recipe)) {
            return null;
        }
        var ingredients = new ArrayList<RecipeIngredient>();
        for (var ingredient : recipe.getIngredients()) {
            if (ingredient != null && !ingredient.isEmpty()) {
                ingredients.add(RecipeImporter.importIngredient(ingredient));
            }
        }
        if (ingredients.isEmpty()) {
            throw new RecipeImportException("viscript_recipe.editor.import_recipe.error.empty_ingredient");
        }
        if (ingredients.size() > TouhouLittleMaidAltarRecipeData.INPUT_COUNT) {
            throw new RecipeImportException(
                    "viscript_recipe.editor.import_recipe.error.too_many_ingredients",
                    ingredients.size(),
                    TouhouLittleMaidAltarRecipeData.INPUT_COUNT
            );
        }
        var data = new TouhouLittleMaidAltarRecipeData()
                .setIngredients(ingredients)
                .setResult(RecipeImporter.copyResult(recipe, provider))
                .setPower(recipe.getPowerCost())
                .setEntityType(BuiltInRegistries.ENTITY_TYPE.getKey(recipe.getEntityType()))
                .setExtraData(recipe.getExtraData());
        return RecipeImporter.success(RecipeImporter.baseEntry(recipe.getId(), TouhouLittleMaidRecipeEditorTypes.ALTAR_RECIPE)
                .setData(data));
    }
}
