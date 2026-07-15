package com.viscript_recipe.data.goety;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.viscript_recipe.data.RecipeIngredient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Stores editable data for a Goety cursed infuser recipe.
 */
@Getter
@Setter
@Accessors(chain = true)
public class GoetyCursedInfuserRecipeData implements IPersistedSerializable, IConfigurable {
    @Configurable(name = "viscript_recipe.config.goety.cursed_infuser.ingredient", subConfigurable = true)
    private RecipeIngredient ingredient = RecipeIngredient.item(Items.EMERALD_BLOCK);

    @Configurable(name = "viscript_recipe.config.recipe.result")
    private ItemStack result = new ItemStack(Items.DIAMOND_BLOCK);

    @Configurable(name = "viscript_recipe.config.goety.cursed_infuser.cooking_time")
    private int cookingTime = 60;

    @Configurable(name = "viscript_recipe.config.goety.cursed_infuser.grim")
    private boolean grim;

    /**
     * Compiles this data into Goety's native cursed infuser recipe.
     *
     * @return the compiled cursed infuser recipe
     */
//    public Recipe<?> compile() {
//        return GoetyRecipeFactory.compileCursedInfuser(this);
//    }
}
