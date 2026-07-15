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
 * Persisted editor data for Cataclysm's mechanical fusion anvil recipe.
 */
@Getter
@Setter
@Accessors(chain = true)
public class CataclysmWeaponFusionRecipeData implements IPersistedSerializable, IConfigurable {
    @Configurable(name = "viscript_recipe.config.cataclysm.weapon_fusion.base", subConfigurable = true)
    private RecipeIngredient base = RecipeIngredient.item(Items.IRON_SWORD);

    @Configurable(name = "viscript_recipe.config.cataclysm.weapon_fusion.addition", subConfigurable = true)
    private RecipeIngredient addition = RecipeIngredient.item(Items.AMETHYST_SHARD);

    @Configurable(name = "viscript_recipe.config.recipe.result")
    private ItemStack result = new ItemStack(Items.DIAMOND_SWORD);

    /**
     * Compiles this data into Cataclysm's native recipe implementation.
     *
     * @return the compiled weapon fusion recipe
     */
    /*public Recipe<?> compile() {
        return CataclysmRecipeFactory.compileWeaponFusion(this);
    }*/
}
