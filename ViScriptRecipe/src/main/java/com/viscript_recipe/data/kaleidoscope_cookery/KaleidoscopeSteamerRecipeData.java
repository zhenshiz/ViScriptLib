package com.viscript_recipe.data.kaleidoscope_cookery;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.viscript_recipe.data.RecipeIngredient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@Getter
@Setter
@Accessors(chain = true)
public class KaleidoscopeSteamerRecipeData implements IPersistedSerializable, IConfigurable {
    @Configurable(name = "viscript_recipe.config.kaleidoscope_cookery.ingredient")
    private RecipeIngredient ingredient = RecipeIngredient.item(Items.WHEAT);

    @Configurable(name = "viscript_recipe.config.recipe.result")
    private ItemStack result = new ItemStack(Items.BREAD);

    @Configurable(name = "viscript_recipe.config.kaleidoscope_cookery.cook_tick")
    private int cookTick = 1200;

/*    public Recipe<?> compile() {
        return KaleidoscopeCookeryRecipeFactory.compileSteamer(this);
    }*/
}
