package com.viscript_recipe.data.vanilla;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.ViScriptRecipe;
import com.viscript_recipe.data.IVSRecipeData;
import com.viscript_recipe.data.RecipeEditorTypes;
import com.viscript_recipe.data.RecipeIngredient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;

import java.util.Map;

@Getter
@Setter
@Accessors(chain = true)
public class CookingRecipeData implements IVSRecipeData {
    @Persisted
    private RecipeIngredient ingredient = RecipeIngredient.item(Items.RAW_IRON);
    @Persisted
    private ItemStack result = new ItemStack(Items.IRON_INGOT);
    @Persisted
    private float experience = 0.7F;
    @Persisted
    private int cookingTime = 200;

    @Override
    public Recipe<?> compile(ResourceLocation typeId) {
        var compiledIngredient = ingredient == null ? net.minecraft.world.item.crafting.Ingredient.EMPTY : ingredient.compile();
        if (compiledIngredient.isEmpty()) {
            throw new IllegalArgumentException("Cooking recipe ingredient cannot be empty");
        }
        if (result.isEmpty()) {
            throw new IllegalArgumentException("Recipe result cannot be empty");
        }
        Factory factory = factories.get(typeId);
        if (factory == null) return null;
        return factory.create(ViScriptRecipe.placeholder, "", CookingBookCategory.MISC, compiledIngredient, result.copy(), Math.max(0, Math.min(Integer.MAX_VALUE, experience)), Math.max(1, cookingTime));
    }
    
    static final Map<ResourceLocation, Factory> factories = Map.of(
            RecipeEditorTypes.SMELTING, SmeltingRecipe::new,
            RecipeEditorTypes.BLASTING, BlastingRecipe::new,
            RecipeEditorTypes.SMOKING, SmokingRecipe::new,
            RecipeEditorTypes.CAMPFIRE_COOKING, CampfireCookingRecipe::new
    );

    public interface Factory {
        AbstractCookingRecipe create(ResourceLocation id, String group, CookingBookCategory category, Ingredient ingredient, ItemStack result, float experience, int cookingTime);
    }

    @Override
    public void applyDefaultData(ResourceLocation typeId) {
        if (typeId.equals(RecipeEditorTypes.BLASTING)) setCookingTime(100);
        else if (typeId.equals(RecipeEditorTypes.SMOKING)) setCookingTime(100);
        else if (typeId.equals(RecipeEditorTypes.CAMPFIRE_COOKING)) setCookingTime(600);
    }
}
