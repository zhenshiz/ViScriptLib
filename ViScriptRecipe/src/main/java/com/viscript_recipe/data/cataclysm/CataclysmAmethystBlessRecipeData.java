package com.viscript_recipe.data.cataclysm;

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
 * Persisted editor data for Cataclysm's Altar of Amethyst blessing recipe.
 */
@Getter
@Setter
@Accessors(chain = true)
public class CataclysmAmethystBlessRecipeData implements IPersistedSerializable, IConfigurable {
    @Configurable(name = "viscript_recipe.config.cataclysm.amethyst_bless.ingredient", subConfigurable = true)
    private RecipeIngredient ingredient = RecipeIngredient.item(Items.AMETHYST_SHARD);

    @Configurable(name = "viscript_recipe.config.recipe.result")
    private ItemStack result = new ItemStack(Items.AMETHYST_BLOCK);

    @Configurable(name = "viscript_recipe.config.cataclysm.amethyst_bless.time")
    private int time = 200;

    /**
     * Compiles this data into Cataclysm's native recipe implementation.
     *
     * @return the compiled amethyst blessing recipe
     */
    /*public Recipe<?> compile() {
        return CataclysmRecipeFactory.compileAmethystBless(this);
    }*/
}
