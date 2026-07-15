package com.viscript_recipe.data.vanilla;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.viscript_recipe.ViScriptRecipe;
import com.viscript_recipe.data.RecipeIngredient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

@Getter
@Setter
@Accessors(chain = true)
public class CookingRecipeData implements IPersistedSerializable, IConfigurable {
    @Configurable(name = "viscript_recipe.config.cooking.ingredient", subConfigurable = true)
    private RecipeIngredient ingredient = RecipeIngredient.item(Items.RAW_IRON);

    @Configurable(name = "viscript_recipe.config.recipe.result")
    private ItemStack result = new ItemStack(Items.IRON_INGOT);

    @Configurable(name = "viscript_recipe.config.cooking.experience")
    private float experience = 0.7F;

    @Configurable(name = "viscript_recipe.config.cooking.cooking_time")
    private int cookingTime = 200;

    public Recipe<?> compile(Factory factory) {
        var compiledIngredient = ingredient == null ? net.minecraft.world.item.crafting.Ingredient.EMPTY : ingredient.compile();
        if (compiledIngredient.isEmpty()) {
            throw new IllegalArgumentException("Cooking recipe ingredient cannot be empty");
        }
        if (result.isEmpty()) {
            throw new IllegalArgumentException("Recipe result cannot be empty");
        }
        return factory.create(ViScriptRecipe.placeholder, "", CookingBookCategory.MISC, compiledIngredient, result.copy(), Math.max(0, Math.min(Integer.MAX_VALUE, experience)), Math.max(1, cookingTime));
    }

    public interface Factory {
        AbstractCookingRecipe create(ResourceLocation id, String group, CookingBookCategory category, Ingredient ingredient, ItemStack result, float experience, int cookingTime);
    }
}
