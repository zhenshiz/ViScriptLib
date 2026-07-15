package com.viscript_recipe.data.avaritia;

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
public class AvaritiaEternalSingularityRecipeData implements IPersistedSerializable, IConfigurable {
    @Configurable(name = "viscript_recipe.config.shapeless.ingredients")
    @ConfigList(addDefaultMethod = "createDefaultIngredient")
    private List<RecipeIngredient> ingredients = new ArrayList<>();

    @Configurable(name = "viscript_recipe.config.avaritia.count")
    private int count = 1;

    public RecipeIngredient createDefaultIngredient() {
        return RecipeIngredient.item(Items.NETHER_STAR);
    }

    /*public ItemStack result() {
        return AvaritiaRecipeFactory.defaultItemStack("avaritia:eternal_singularity", Items.NETHER_STAR);
    }

    public Recipe<?> compile() {
        return AvaritiaRecipeFactory.compileEternalSingularity(this);
    }*/
}
