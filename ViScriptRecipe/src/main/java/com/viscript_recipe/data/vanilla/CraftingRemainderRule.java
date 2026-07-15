package com.viscript_recipe.data.vanilla;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.configurator.annotation.ConfigSelector;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.world.item.ItemStack;

@Getter
@Setter
@Accessors(chain = true)
public class CraftingRemainderRule implements IPersistedSerializable, IConfigurable {
    @Configurable(name = "viscript_recipe.config.remainder.mode")
    @ConfigSelector(candidate = {"default", "consume", "replace"})
    private CraftingRemainderMode mode = CraftingRemainderMode.DEFAULT;

    @Configurable(name = "viscript_recipe.config.remainder.item")
    private ItemStack item = ItemStack.EMPTY;

    public static CraftingRemainderRule defaultRule() {
        return new CraftingRemainderRule();
    }

    public CraftingRemainderRule copy() {
        return new CraftingRemainderRule()
                .setMode(modeOrDefault())
                .setItem(item == null ? ItemStack.EMPTY : item.copy());
    }

    public boolean isDefault() {
        return modeOrDefault() == CraftingRemainderMode.DEFAULT;
    }

    public ItemStack apply(ItemStack input) {
        if (input == null || input.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return switch (modeOrDefault()) {
            case DEFAULT -> input.hasCraftingRemainingItem() ? input.getCraftingRemainingItem() : ItemStack.EMPTY;
            case CONSUME -> ItemStack.EMPTY;
            case REPLACE -> item == null || item.isEmpty() ? ItemStack.EMPTY : item.copy();
        };
    }

    private CraftingRemainderMode modeOrDefault() {
        return mode == null ? CraftingRemainderMode.DEFAULT : mode;
    }
}
