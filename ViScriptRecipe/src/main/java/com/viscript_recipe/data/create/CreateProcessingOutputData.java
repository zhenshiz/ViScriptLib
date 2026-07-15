package com.viscript_recipe.data.create;

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
public class CreateProcessingOutputData implements IPersistedSerializable, IConfigurable {
    @Configurable(name = "viscript_recipe.config.create.output.item")
    private ItemStack item = new ItemStack(Items.IRON_NUGGET);

    @Configurable(name = "viscript_recipe.config.create.output.chance")
    private float chance = 1.0F;

    public CreateProcessingOutputData copy() {
        return new CreateProcessingOutputData()
                .setItem(item == null ? ItemStack.EMPTY : item.copy())
                .setChance(chance);
    }
}
