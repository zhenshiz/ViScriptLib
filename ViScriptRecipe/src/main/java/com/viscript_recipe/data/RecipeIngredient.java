package com.viscript_recipe.data;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.configurator.annotation.ConfigList;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.CompoundIngredient;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class RecipeIngredient implements IPersistedSerializable, IConfigurable {
    @Configurable(name = "viscript_recipe.config.ingredient.values")
    @ConfigList(addDefaultMethod = "createDefaultValue")
    private List<RecipeIngredientValue> values = new ArrayList<>();

    public static RecipeIngredient item(Item item) {
        return item(new ItemStack(item));
    }

    public static RecipeIngredient item(ItemStack stack) {
        var ingredient = new RecipeIngredient();
        ingredient.values.add(RecipeIngredientValue.item(stack));
        return ingredient;
    }

    public static RecipeIngredient itemAbility(String itemAbility) {
        var ingredient = new RecipeIngredient();
        ingredient.values.add(RecipeIngredientValue.itemAbility(itemAbility));
        return ingredient;
    }

    public RecipeIngredientValue createDefaultValue() {
        return new RecipeIngredientValue();
    }

    public Ingredient compile() {
        if (values.isEmpty()) {
            return Ingredient.EMPTY;
        }
        var ingredients = values.stream()
                .map(RecipeIngredientValue::compile)
                .filter(ingredient -> !ingredient.isEmpty())
                .toArray(Ingredient[]::new);
        return CompoundIngredient.of(ingredients);
    }
}
