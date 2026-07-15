package com.viscript_recipe.data.farmersdelight;

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
public class FarmerCuttingResultData implements IPersistedSerializable, IConfigurable {
    @Configurable(name = "viscript_recipe.config.farmersdelight.cutting.result_item")
    private ItemStack item = new ItemStack(Items.APPLE);

    @Configurable(name = "viscript_recipe.config.farmersdelight.cutting.chance")
    private float chance = 1.0F;

    public FarmerCuttingResultData copy() {
        return new FarmerCuttingResultData()
                .setItem(item == null ? ItemStack.EMPTY : item.copy())
                .setChance(chance);
    }
}
