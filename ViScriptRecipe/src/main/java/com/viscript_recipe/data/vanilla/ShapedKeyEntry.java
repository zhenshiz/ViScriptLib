package com.viscript_recipe.data.vanilla;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
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
    @Persisted
    private String symbol = "A";
    @Persisted
    private RecipeIngredient ingredient = RecipeIngredient.item(Items.OAK_PLANKS);

    public static ShapedKeyEntry of(char symbol, RecipeIngredient ingredient) {
        return new ShapedKeyEntry().setSymbol(String.valueOf(symbol)).setIngredient(ingredient);
    }

    public char compileSymbol() {
        if (symbol.isEmpty() || symbol.charAt(0) == ' ') {
            throw new IllegalArgumentException("Shaped key symbol must be one non-space character");
        }
        return symbol.charAt(0);
    }

    public Ingredient compileIngredient() {
        return ingredient == null ? Ingredient.EMPTY : ingredient.compile();
    }
}
