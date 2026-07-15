package com.viscript_recipe.data.vanilla;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.viscript_recipe.data.RecipeIngredient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

@Getter
@Setter
@Accessors(chain = true)
public class ShapedKeyEntry implements IPersistedSerializable, IConfigurable {
    @Configurable(name = "viscript_recipe.config.shaped.key.symbol")
    private String symbol = "A";

    @Configurable(name = "viscript_recipe.config.shaped.key.ingredient", subConfigurable = true)
    private RecipeIngredient ingredient = RecipeIngredient.item(Items.OAK_PLANKS);

    public static ShapedKeyEntry of(String symbol, RecipeIngredient ingredient) {
        return new ShapedKeyEntry().setSymbol(symbol).setIngredient(ingredient);
    }

    public char compileSymbol() {
        if (symbol == null || symbol.length() != 1 || symbol.charAt(0) == ' ') {
            throw new IllegalArgumentException("Shaped key symbol must be one non-space character");
        }
        return symbol.charAt(0);
    }

    public Ingredient compileIngredient() {
        return ingredient == null ? Ingredient.EMPTY : ingredient.compile();
    }
}
