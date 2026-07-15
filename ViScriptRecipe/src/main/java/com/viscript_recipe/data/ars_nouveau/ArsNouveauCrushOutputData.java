package com.viscript_recipe.data.ars_nouveau;

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
public class ArsNouveauCrushOutputData implements IPersistedSerializable, IConfigurable {
    @Configurable(name = "viscript_recipe.config.ars_nouveau.crush.output_item")
    private ItemStack item = new ItemStack(Items.GRAVEL);

    @Configurable(name = "viscript_recipe.config.ars_nouveau.crush.chance")
    private float chance = 1.0F;

    @Configurable(name = "viscript_recipe.config.ars_nouveau.crush.max_range")
    private int maxRange = 1;

    public ArsNouveauCrushOutputData copy() {
        return new ArsNouveauCrushOutputData()
                .setItem(item == null ? ItemStack.EMPTY : item.copy())
                .setChance(chance)
                .setMaxRange(maxRange);
    }
}
