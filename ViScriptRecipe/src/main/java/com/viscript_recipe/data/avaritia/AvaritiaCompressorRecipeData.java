package com.viscript_recipe.data.avaritia;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.viscript_recipe.data.RecipeIngredient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@Getter
@Setter
@Accessors(chain = true)
public class AvaritiaCompressorRecipeData implements IPersistedSerializable, IConfigurable {
    @Configurable(name = "viscript_recipe.config.avaritia.compressor.ingredient", subConfigurable = true)
    private RecipeIngredient ingredient = RecipeIngredient.item(Items.COBBLESTONE);

    @Configurable(name = "viscript_recipe.config.recipe.result")
    private ItemStack result = new ItemStack(Items.BEDROCK);

    @Configurable(name = "viscript_recipe.config.avaritia.compressor.input_count")
    private int inputCount = 1000;

    @Configurable(name = "viscript_recipe.config.avaritia.compressor.time_cost")
    private int timeCost = 240;

    /*public Recipe<?> compile() {
        return AvaritiaRecipeFactory.compileCompressor(this);
    }*/
}
