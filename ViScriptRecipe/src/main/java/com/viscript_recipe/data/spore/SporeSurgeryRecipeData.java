package com.viscript_recipe.data.spore;

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

@Getter
@Setter
@Accessors(chain = true)
public class SporeSurgeryRecipeData implements IPersistedSerializable, IConfigurable {
    public static final int INPUT_COUNT = 16;

    @Configurable(name = "viscript_recipe.config.spore.surgery.ingredients")
    @ConfigList(addDefaultMethod = "createDefaultIngredient")
    private List<RecipeIngredient> ingredients = emptyIngredients(INPUT_COUNT);

    @Configurable(name = "viscript_recipe.config.recipe.result")
    private ItemStack result = new ItemStack(Items.IRON_SWORD);

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

    public SporeSurgeryRecipeData setIngredient(int index, RecipeIngredient ingredient) {
        ingredients = normalizedIngredients(ingredients, INPUT_COUNT);
        if (index >= 0 && index < INPUT_COUNT) {
            ingredients.set(index, ingredient == null ? new RecipeIngredient() : ingredient);
        }
        return this;
    }

    public List<RecipeIngredient> normalizedIngredients() {
        return normalizedIngredients(ingredients, INPUT_COUNT);
    }

/*    public Recipe<?> compile() {
        return SporeRecipeFactory.compileSurgery(this);
    }*/

    static List<RecipeIngredient> normalizedIngredients(List<RecipeIngredient> source, int size) {
        var normalized = emptyIngredients(size);
        if (source != null) {
            for (int i = 0; i < Math.min(size, source.size()); i++) {
                var ingredient = source.get(i);
                normalized.set(i, ingredient == null ? new RecipeIngredient() : ingredient);
            }
        }
        return normalized;
    }

    private static List<RecipeIngredient> emptyIngredients(int size) {
        var ingredients = new ArrayList<RecipeIngredient>(size);
        for (int i = 0; i < size; i++) {
            ingredients.add(new RecipeIngredient());
        }
        return ingredients;
    }
}
