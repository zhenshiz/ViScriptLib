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

import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class SporeGraftingRecipeData implements IPersistedSerializable, IConfigurable {
    public static final int INPUT_COUNT = 3;

    @Configurable(name = "viscript_recipe.config.spore.grafting.ingredients")
    @ConfigList(addDefaultMethod = "createDefaultIngredient")
    private List<RecipeIngredient> ingredients = SporeSurgeryRecipeData.normalizedIngredients(null, INPUT_COUNT);

    @Configurable(name = "viscript_recipe.config.recipe.result")
    private ItemStack result = new ItemStack(Items.IRON_HELMET);

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

    public SporeGraftingRecipeData setIngredient(int index, RecipeIngredient ingredient) {
        ingredients = normalizedIngredients();
        if (index >= 0 && index < INPUT_COUNT) {
            ingredients.set(index, ingredient == null ? new RecipeIngredient() : ingredient);
        }
        return this;
    }

    public List<RecipeIngredient> normalizedIngredients() {
        return SporeSurgeryRecipeData.normalizedIngredients(ingredients, INPUT_COUNT);
    }

/*    public Recipe<?> compile() {
        return SporeRecipeFactory.compileGrafting(this);
    }*/
}
