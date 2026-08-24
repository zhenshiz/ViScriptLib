package com.viscript_recipe.data.vanilla;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.data.IVSRecipeData;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.recipe.vanilla.ViscriptShapelessRecipe;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class ShapelessCraftingRecipeData implements IVSRecipeData {
    @Persisted
    private Boolean showNotification = true;
    @Persisted
    private List<RecipeIngredient> ingredients = new ArrayList<>(List.of(
            RecipeIngredient.item(Items.OAK_PLANKS)
    ));
    @Persisted
    private ItemStack result = new ItemStack(Items.STICK, 4);

    @Override
    public String getDataName() {return "shapeless";}

    @Override
    public Recipe<?> compile(ResourceLocation typeId) {
        if (ingredients.isEmpty()) {
            throw new IllegalArgumentException("Shapeless recipe must have at least one ingredient");
        }
        var compiledIngredients = NonNullList.<Ingredient>create();
        for (var ingredient : ingredients) {
            var compiled = ingredient.compile();
            if (!compiled.isEmpty()) {
                compiledIngredients.add(compiled);
            }
        }
        if (compiledIngredients.isEmpty()) {
            throw new IllegalArgumentException("Shapeless recipe must have at least one non-empty ingredient");
        }
        if (result.isEmpty()) {
            throw new IllegalArgumentException("Recipe result cannot be empty");
        }
        return new ViscriptShapelessRecipe("", CraftingBookCategory.MISC, result.copy(), compiledIngredients, showNotification);
    }
}
