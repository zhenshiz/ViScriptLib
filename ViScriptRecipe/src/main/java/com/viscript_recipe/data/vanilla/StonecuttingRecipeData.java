package com.viscript_recipe.data.vanilla;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.recipe.vanilla.ViscriptStonecutterRecipe;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;

@Getter
@Setter
@Accessors(chain = true)
public class StonecuttingRecipeData implements IPersistedSerializable, IConfigurable {
    @Configurable(name = "viscript_recipe.config.recipe.show_notification")
    private boolean showNotification = true;

    @Configurable(name = "viscript_recipe.config.stonecutting.ingredient", subConfigurable = true)
    private RecipeIngredient ingredient = RecipeIngredient.item(Items.STONE);

    @Configurable(name = "viscript_recipe.config.recipe.result")
    private ItemStack result = new ItemStack(Items.STONE_SLAB, 2);

    public Recipe<?> compile() {
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
