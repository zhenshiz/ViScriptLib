package com.viscript_recipe.data.vanilla;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.configurator.annotation.ConfigList;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.recipe.vanilla.ViscriptShapelessRecipe;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.core.NonNullList;
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
public class ShapelessCraftingRecipeData implements IPersistedSerializable, IConfigurable {
    @Configurable(name = "viscript_recipe.config.recipe.show_notification")
    private boolean showNotification = true;

    @Configurable(name = "viscript_recipe.config.shapeless.ingredients")
    @ConfigList(addDefaultMethod = "createDefaultIngredient")
    private List<RecipeIngredient> ingredients = new ArrayList<>(List.of(
            RecipeIngredient.item(Items.OAK_PLANKS)
    ));

    @Configurable(name = "viscript_recipe.config.recipe.result")
    private ItemStack result = new ItemStack(Items.STICK, 4);

    public RecipeIngredient createDefaultIngredient() {
        return RecipeIngredient.item(Items.OAK_PLANKS);
    }

    public Recipe<?> compile() {
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
