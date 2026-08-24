package com.viscript_recipe.data;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_lib.util.ISkipDefaultedSerialize;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@Getter
@Setter
@Accessors(chain = true)
public class RecipeOutputData implements ISkipDefaultedSerialize, IConfigurable {
    @Persisted
    private ItemStack item = new ItemStack(Items.GRASS_BLOCK);
    @Persisted
    private float chance = 1.0F;

    /**请使用工厂方法*/
    @Deprecated
    public RecipeOutputData() {}

    public static RecipeOutputData of() {return new RecipeOutputData();}

    public static RecipeOutputData of(ItemStack item, float chance) {
        return of().setItem(item.copy()).setChance(chance);
    }
    public static RecipeOutputData of(ItemStack item) {return of(item, 1.0F);}

    public static RecipeOutputData empty() {return of(ItemStack.EMPTY);}

    public RecipeOutputData copy() {return of().setItem(item.copy()).setChance(chance);}

    public boolean isEmpty() {return item.isEmpty();}
}
