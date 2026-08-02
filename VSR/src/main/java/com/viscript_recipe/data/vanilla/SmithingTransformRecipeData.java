package com.viscript_recipe.data.vanilla;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.data.IVSRecipeData;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.recipe.vanilla.ViscriptSmithingTransformRecipe;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

@Getter
@Setter
@Accessors(chain = true)
public class SmithingTransformRecipeData implements IVSRecipeData {
    @Persisted
    private Boolean showNotification = true;
    @Persisted
    private RecipeIngredient template = RecipeIngredient.item(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE);
    @Persisted
    private RecipeIngredient base = RecipeIngredient.item(Items.DIAMOND_SWORD);
    @Persisted
    private RecipeIngredient addition = RecipeIngredient.item(Items.NETHERITE_INGOT);
    @Persisted
    private ItemStack result = new ItemStack(Items.NETHERITE_SWORD);

    @Override
    public Recipe<?> compile(ResourceLocation typeId) {
        var compiledTemplate = template == null ? Ingredient.EMPTY : template.compile();
        var compiledBase = base == null ? Ingredient.EMPTY : base.compile();
        var compiledAddition = addition == null ? Ingredient.EMPTY : addition.compile();
        if (compiledTemplate.isEmpty()) {
            throw new IllegalArgumentException("Smithing transform template cannot be empty");
        }
        if (compiledBase.isEmpty()) {
            throw new IllegalArgumentException("Smithing transform base cannot be empty");
        }
        if (compiledAddition.isEmpty()) {
            throw new IllegalArgumentException("Smithing transform addition cannot be empty");
        }
        if (result.isEmpty()) {
            throw new IllegalArgumentException("Recipe result cannot be empty");
        }
        return new ViscriptSmithingTransformRecipe(compiledTemplate, compiledBase, compiledAddition, result.copy(), showNotification);
    }
}
