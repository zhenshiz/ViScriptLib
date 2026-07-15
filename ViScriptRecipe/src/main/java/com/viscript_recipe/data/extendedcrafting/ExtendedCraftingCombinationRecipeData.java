package com.viscript_recipe.data.extendedcrafting;

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

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class ExtendedCraftingCombinationRecipeData implements IPersistedSerializable, IConfigurable {
    @Configurable(name = "viscript_recipe.config.extendedcrafting.combination.input", subConfigurable = true)
    private RecipeIngredient input = RecipeIngredient.item(Items.DIAMOND);

    @Configurable(name = "viscript_recipe.config.extendedcrafting.combination.pedestal_items")
    @ConfigList(addDefaultMethod = "createDefaultPedestalItem")
    private List<RecipeIngredient> pedestalItems = new ArrayList<>(List.of(
            RecipeIngredient.item(Items.GOLD_INGOT)
    ));

    @Configurable(name = "viscript_recipe.config.recipe.result")
    private ItemStack result = new ItemStack(Items.NETHER_STAR);

    @Configurable(name = "viscript_recipe.config.extendedcrafting.power_cost")
    private int powerCost = 100000;

    @Configurable(name = "viscript_recipe.config.extendedcrafting.power_rate")
    private int powerRate = 500;

    public RecipeIngredient createDefaultPedestalItem() {
        return RecipeIngredient.item(Items.GOLD_INGOT);
    }

    /*public Recipe<?> compile() {
        return ExtendedCraftingRecipeFactory.compileCombination(this);
    }*/
}
