package com.viscript_recipe.compat.farmersdelight.data;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.compat.farmersdelight.FarmersDelightRecipeFactory;
import com.viscript_recipe.data.IVSRecipeData;
import com.viscript_recipe.data.RecipeIngredient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class FarmerCookingPotRecipeData implements IVSRecipeData {
    @Persisted
    private List<RecipeIngredient> ingredients = new ArrayList<>(List.of(
            RecipeIngredient.item(Items.BEEF),
            RecipeIngredient.item(Items.CARROT),
            RecipeIngredient.item(Items.POTATO)
    ));
    @Persisted
    private ItemStack result = new ItemStack(Items.RABBIT_STEW);
    @Persisted
    private ItemStack container = new ItemStack(Items.BOWL);
    @Persisted
    private float experience = 1.0F;
    @Persisted
    private int cookingTime = 200;

    @Override
    public Recipe<?> compile(ResourceLocation typeId) {
        return FarmersDelightRecipeFactory.compileCooking(this);
    }

    @Override
    public void applyDefaultData(ResourceLocation typeId) {
        setResult(new ItemStack(com.viscript_recipe.recipe.RecipeHelper.itemFromRegistry
                ("farmersdelight:beef_stew", Items.RABBIT_STEW)));
    }
}
