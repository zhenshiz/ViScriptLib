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
public class ArsNouveauImbuementRecipeData implements IPersistedSerializable, IConfigurable {
    @Configurable(name = "viscript_recipe.config.ars_nouveau.imbuement.input", subConfigurable = true)
    private RecipeIngredient input = RecipeIngredient.item(Items.LAPIS_LAZULI);

    @Configurable(name = "viscript_recipe.config.ars_nouveau.pedestal_items")
    @ConfigList(addDefaultMethod = "createDefaultPedestalItem")
    private List<RecipeIngredient> pedestalItems = new ArrayList<>();

    @Configurable(name = "viscript_recipe.config.recipe.result")
    private ItemStack result = new ItemStack(Items.AMETHYST_SHARD);

    @Configurable(name = "viscript_recipe.config.ars_nouveau.source_cost")
    private int source = 500;

    public RecipeIngredient createDefaultPedestalItem() {
        return RecipeIngredient.item(Items.AMETHYST_SHARD);
    }

    /*public Recipe<?> compile() {
        return ArsNouveauRecipeFactory.compileImbuement(this);
    }*/
}
