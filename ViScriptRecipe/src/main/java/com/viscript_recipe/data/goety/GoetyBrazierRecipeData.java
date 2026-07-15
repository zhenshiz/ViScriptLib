package com.viscript_recipe.data.goety;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.configurator.annotation.ConfigList;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.viscript_recipe.data.RecipeIngredient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores the three-input layout used by Goety's necro brazier recipes.
 */
@Getter
@Setter
@Accessors(chain = true)
public class GoetyBrazierRecipeData implements IPersistedSerializable, IConfigurable {
    public static final int INPUT_COUNT = 3;

    @Configurable(name = "viscript_recipe.config.goety.brazier.ingredients")
    @ConfigList(addDefaultMethod = "createDefaultIngredient")
    private List<RecipeIngredient> ingredients = emptyIngredients();

    @Configurable(name = "viscript_recipe.config.recipe.result")
    private ItemStack result = new ItemStack(Items.IRON_INGOT);

    @Configurable(name = "viscript_recipe.config.goety.soul_cost")
    private int soulCost = 500;

    public RecipeIngredient createDefaultIngredient() {
        return new RecipeIngredient();
    }

    public RecipeIngredient ingredient(int index) {
        if (ingredients == null || index < 0 || index >= ingredients.size()) {
            return new RecipeIngredient();
        }
        var ingredient = ingredients.get(index);
        return ingredient == null ? new RecipeIngredient() : ingredient;
    }

    public GoetyBrazierRecipeData setIngredient(int index, RecipeIngredient ingredient) {
        ingredients = normalizedIngredients();
        if (index >= 0 && index < INPUT_COUNT) {
            ingredients.set(index, ingredient == null ? new RecipeIngredient() : ingredient);
        }
        return this;
    }

    public List<RecipeIngredient> normalizedIngredients() {
        var normalized = emptyIngredients();
        if (ingredients != null) {
            for (int i = 0; i < Math.min(INPUT_COUNT, ingredients.size()); i++) {
                var ingredient = ingredients.get(i);
                normalized.set(i, ingredient == null ? new RecipeIngredient() : ingredient);
            }
        }
        return normalized;
    }

    private static List<RecipeIngredient> emptyIngredients() {
        var result = new ArrayList<RecipeIngredient>(INPUT_COUNT);
        for (int i = 0; i < INPUT_COUNT; i++) {
            result.add(new RecipeIngredient());
        }
        return result;
    }

    /**
     * Compiles this data into Goety's native brazier recipe.
     *
     * @return the compiled brazier recipe
     */
//    public Recipe<?> compile() {
//        return GoetyRecipeFactory.compileBrazier(this);
//    }
}
