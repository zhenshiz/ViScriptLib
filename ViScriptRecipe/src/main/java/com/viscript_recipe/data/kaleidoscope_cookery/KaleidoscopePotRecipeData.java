package com.viscript_recipe.data.kaleidoscope_cookery;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.configurator.annotation.ConfigList;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.viscript_recipe.data.RecipeIngredient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class KaleidoscopePotRecipeData implements IPersistedSerializable, IConfigurable {
    @Configurable(name = "viscript_recipe.config.kaleidoscope_cookery.ingredients")
    @ConfigList(addDefaultMethod = "createDefaultIngredient")
    private List<RecipeIngredient> ingredients = new ArrayList<>(List.of(
            RecipeIngredient.item(Items.EGG),
            RecipeIngredient.item(Items.CARROT),
            RecipeIngredient.item(Items.POTATO)
    ));

    @Configurable(name = "viscript_recipe.config.recipe.result")
    private ItemStack result = new ItemStack(Items.BAKED_POTATO);

    @Configurable(name = "viscript_recipe.config.kaleidoscope_cookery.carrier")
    private RecipeIngredient carrier = RecipeIngredient.item(Items.BOWL);

    @Configurable(name = "viscript_recipe.config.kaleidoscope_cookery.time")
    private int time = 200;

    @Configurable(name = "viscript_recipe.config.kaleidoscope_cookery.stir_fry_count")
    private int stirFryCount = 3;

    public RecipeIngredient createDefaultIngredient() {
        return RecipeIngredient.item(Items.CARROT);
    }

/*    public Recipe<?> compile() {
        return KaleidoscopeCookeryRecipeFactory.compilePot(this);
    }*/
}
