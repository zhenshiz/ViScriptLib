package com.viscript_recipe.data.ars_nouveau;

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
public class ArsNouveauApparatusRecipeData implements IPersistedSerializable, IConfigurable {
    @Configurable(name = "viscript_recipe.config.ars_nouveau.apparatus.reagent", subConfigurable = true)
    private RecipeIngredient reagent = RecipeIngredient.item(Items.DIAMOND);

    @Configurable(name = "viscript_recipe.config.ars_nouveau.pedestal_items")
    @ConfigList(addDefaultMethod = "createDefaultPedestalItem")
    private List<RecipeIngredient> pedestalItems = new ArrayList<>(List.of(
            RecipeIngredient.item(Items.GOLD_INGOT),
            RecipeIngredient.item(Items.AMETHYST_SHARD)
    ));

    @Configurable(name = "viscript_recipe.config.recipe.result")
    private ItemStack result = new ItemStack(Items.ENCHANTED_BOOK);

    @Configurable(name = "viscript_recipe.config.ars_nouveau.source_cost")
    private int sourceCost;

    @Configurable(name = "viscript_recipe.config.ars_nouveau.apparatus.keep_nbt_of_reagent")
    private boolean keepNbtOfReagent;

    public RecipeIngredient createDefaultPedestalItem() {
        return RecipeIngredient.item(Items.AMETHYST_SHARD);
    }

    /*public Recipe<?> compile() {
        return ArsNouveauRecipeFactory.compileApparatus(this);
    }*/
}
