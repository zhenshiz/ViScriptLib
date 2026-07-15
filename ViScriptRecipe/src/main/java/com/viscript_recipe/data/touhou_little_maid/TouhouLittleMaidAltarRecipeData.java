package com.viscript_recipe.data.touhou_little_maid;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.configurator.annotation.ConfigList;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.viscript_recipe.data.RecipeIngredient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores the editable fields of a Touhou Little Maid altar recipe.
 */
@Getter
@Setter
@Accessors(chain = true)
public class TouhouLittleMaidAltarRecipeData implements IPersistedSerializable, IConfigurable {
    public static final int INPUT_COUNT = 6;

    @Configurable(name = "viscript_recipe.config.touhou_little_maid.altar.ingredients")
    @ConfigList(addDefaultMethod = "createDefaultIngredient")
    private List<RecipeIngredient> ingredients = emptyIngredients();

    @Configurable(name = "viscript_recipe.config.recipe.result")
    private ItemStack result = new ItemStack(Items.STICK);

    @Configurable(name = "viscript_recipe.config.touhou_little_maid.altar.power")
    private float power = 0.2F;

    @Configurable(name = "viscript_recipe.config.touhou_little_maid.altar.entity")
    private ResourceLocation entityType = new ResourceLocation("item");

    @Configurable(name = "viscript_recipe.config.touhou_little_maid.altar.lang")
    private String langKey = "jei.touhou_little_maid.altar_craft.item_craft.result";

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

    public TouhouLittleMaidAltarRecipeData setIngredient(int index, RecipeIngredient ingredient) {
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

/*    public Recipe<?> compile() {
        return TouhouLittleMaidRecipeFactory.compileAltar(this);
    }*/

    private static List<RecipeIngredient> emptyIngredients() {
        var result = new ArrayList<RecipeIngredient>(INPUT_COUNT);
        for (int i = 0; i < INPUT_COUNT; i++) {
            result.add(new RecipeIngredient());
        }
        return result;
    }
}
