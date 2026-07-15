package com.viscript_recipe.data.irons_spellbooks;

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
public class IronNoAdditionSmithingRecipeData implements IPersistedSerializable, IConfigurable {
    @Configurable(name = "viscript_recipe.config.irons_spellbooks.smithing_no_addition.template", subConfigurable = true)
    private RecipeIngredient template = RecipeIngredient.item(Items.GOLD_INGOT);

    @Configurable(name = "viscript_recipe.config.irons_spellbooks.smithing_no_addition.base", subConfigurable = true)
    private RecipeIngredient base = RecipeIngredient.item(Items.IRON_SWORD);

    @Configurable(name = "viscript_recipe.config.recipe.result")
    private ItemStack result = new ItemStack(Items.GOLDEN_SWORD);

/*    public Recipe<?> compile() {
        return IronSpellbooksRecipeFactory.compileNoAdditionSmithing(this);
    }*/
}
