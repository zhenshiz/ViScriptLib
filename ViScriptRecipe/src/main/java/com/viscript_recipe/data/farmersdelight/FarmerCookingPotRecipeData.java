package com.viscript_recipe.data.farmersdelight;

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
public class FarmerCookingPotRecipeData implements IPersistedSerializable, IConfigurable {
    @Configurable(name = "viscript_recipe.config.farmersdelight.cooking.ingredients")
    @ConfigList(addDefaultMethod = "createDefaultIngredient")
    private List<RecipeIngredient> ingredients = new ArrayList<>(List.of(
            RecipeIngredient.item(Items.BEEF),
            RecipeIngredient.item(Items.CARROT),
            RecipeIngredient.item(Items.POTATO)
    ));

    @Configurable(name = "viscript_recipe.config.recipe.result")
    private ItemStack result = new ItemStack(Items.RABBIT_STEW);

    @Configurable(name = "viscript_recipe.config.farmersdelight.cooking.container")
    private ItemStack container = new ItemStack(Items.BOWL);

    @Configurable(name = "viscript_recipe.config.cooking.experience")
    private float experience = 1.0F;

    @Configurable(name = "viscript_recipe.config.cooking.cooking_time")
    private int cookingTime = 200;

    public RecipeIngredient createDefaultIngredient() {
        return RecipeIngredient.item(Items.CARROT);
    }

//    public Recipe<?> compile() {
//        return FarmersDelightRecipeFactory.compileCooking(this);
//    }
}
