package com.viscript_recipe.data.extendedcrafting;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@Getter
@Setter
@Accessors(chain = true)
public class ExtendedCraftingUltimateSingularityRecipeData implements IPersistedSerializable, IConfigurable {
    @Configurable(name = "viscript_recipe.config.recipe.result")
    private ItemStack result = new ItemStack(Items.NETHER_STAR);

/*    public Recipe<?> compile() {
        return ExtendedCraftingRecipeFactory.compileUltimateSingularity(this);
    }*/
}
