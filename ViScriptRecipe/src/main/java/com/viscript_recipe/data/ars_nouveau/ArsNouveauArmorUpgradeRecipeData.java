package com.viscript_recipe.data.ars_nouveau;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.configurator.annotation.ConfigList;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.viscript_recipe.data.RecipeIngredient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class ArsNouveauArmorUpgradeRecipeData implements IPersistedSerializable, IConfigurable {
    @Configurable(name = "viscript_recipe.config.ars_nouveau.pedestal_items")
    @ConfigList(addDefaultMethod = "createDefaultPedestalItem")
    private List<RecipeIngredient> pedestalItems = new ArrayList<>(List.of(
            RecipeIngredient.item(Items.BLAZE_ROD),
            RecipeIngredient.item(Items.BLAZE_ROD)
    ));

    @Configurable(name = "viscript_recipe.config.ars_nouveau.source_cost")
    private int sourceCost = 2500;

    @Configurable(name = "viscript_recipe.config.ars_nouveau.armor_upgrade.tier")
    private int tier = 1;

    public RecipeIngredient createDefaultPedestalItem() {
        return RecipeIngredient.item(Items.BLAZE_ROD);
    }

    /*public Recipe<?> compile() {
        return ArsNouveauRecipeFactory.compileArmorUpgrade(this);
    }*/
}
