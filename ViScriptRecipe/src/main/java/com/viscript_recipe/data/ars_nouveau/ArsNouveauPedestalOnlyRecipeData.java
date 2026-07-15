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
public class ArsNouveauPedestalOnlyRecipeData implements IPersistedSerializable, IConfigurable {
    @Configurable(name = "viscript_recipe.config.ars_nouveau.pedestal_items")
    @ConfigList(addDefaultMethod = "createDefaultPedestalItem")
    private List<RecipeIngredient> pedestalItems = new ArrayList<>(List.of(
            RecipeIngredient.item(Items.LAPIS_BLOCK),
            RecipeIngredient.item(Items.PAPER)
    ));

    @Configurable(name = "viscript_recipe.config.ars_nouveau.source_cost")
    private int sourceCost = 3000;

    public RecipeIngredient createDefaultPedestalItem() {
        return RecipeIngredient.item(Items.PAPER);
    }

    /*public Recipe<?> compile(ResourceLocation type) {
        return ArsNouveauRecipeFactory.compilePedestalOnly(type, this);
    }*/
}
