package com.viscript_recipe.data.kaleidoscope_cookery;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.viscript_recipe.data.RecipeIngredient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@Getter
@Setter
@Accessors(chain = true)
public class KaleidoscopeTeapotRecipeData implements IPersistedSerializable, IConfigurable {
    @Configurable(name = "viscript_recipe.config.kaleidoscope_cookery.tea_fluid")
    private ResourceLocation teaFluid = new ResourceLocation("water");

    @Configurable(name = "viscript_recipe.config.kaleidoscope_cookery.ingredient")
    private RecipeIngredient ingredient = RecipeIngredient.item(Items.WHEAT_SEEDS);

    @Configurable(name = "viscript_recipe.config.kaleidoscope_cookery.ingredient_count")
    private int ingredientCount = 12;

    @Configurable(name = "viscript_recipe.config.kaleidoscope_cookery.time")
    private int time = 240;

    @Configurable(name = "viscript_recipe.config.recipe.result")
    private ItemStack result = new ItemStack(Items.POTION);

/*    public Recipe<?> compile() {
        return KaleidoscopeCookeryRecipeFactory.compileTeapot(this);
    }*/
}
