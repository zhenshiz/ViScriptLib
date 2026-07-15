package com.viscript_recipe.data.iceandfire;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.configurator.annotation.ConfigSelector;
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
public class DragonForgeRecipeData implements IPersistedSerializable, IConfigurable {
    @Configurable(name = "viscript_recipe.config.iceandfire.dragon_forge.input", subConfigurable = true)
    private RecipeIngredient input = RecipeIngredient.item(Items.IRON_INGOT);

    @Configurable(name = "viscript_recipe.config.iceandfire.dragon_forge.blood", subConfigurable = true)
    private RecipeIngredient blood = RecipeIngredient.item(Items.GLASS_BOTTLE);

    @Configurable(name = "viscript_recipe.config.recipe.result")
    private ItemStack result = new ItemStack(Items.IRON_INGOT);

    @Configurable(name = "viscript_recipe.config.iceandfire.dragon_forge.dragon_type")
    @ConfigSelector(candidate = {"fire", "ice", "lightning"})
    private String dragonType = "fire";

    @Configurable(name = "viscript_recipe.config.iceandfire.dragon_forge.cook_time")
    private int cookTime = 1000;

//    public Recipe<?> compile() {
//        return IceAndFireRecipeFactory.compileDragonForge(this);
//    }
}
