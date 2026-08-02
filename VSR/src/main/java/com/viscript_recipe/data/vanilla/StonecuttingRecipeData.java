package com.viscript_recipe.data.vanilla;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.data.IVSRecipeData;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.recipe.vanilla.ViscriptStonecutterRecipe;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;

@Getter
@Setter
@Accessors(chain = true)
public class StonecuttingRecipeData implements IVSRecipeData {
    @Persisted
    private Boolean showNotification = true;
    @Persisted
    private RecipeIngredient ingredient = RecipeIngredient.item(Items.STONE);
    @Persisted
    private ItemStack result = new ItemStack(Items.STONE_SLAB, 2);

    @Override
    public Recipe<?> compile(ResourceLocation typeId) {
        var compiledIngredient = ingredient == null ? net.minecraft.world.item.crafting.Ingredient.EMPTY : ingredient.compile();
        if (compiledIngredient.isEmpty()) {
            throw new IllegalArgumentException("Stonecutting recipe ingredient cannot be empty");
        }
        if (result.isEmpty()) {
            throw new IllegalArgumentException("Recipe result cannot be empty");
        }
        return new ViscriptStonecutterRecipe("", compiledIngredient, result.copy(), showNotification);
    }
}
