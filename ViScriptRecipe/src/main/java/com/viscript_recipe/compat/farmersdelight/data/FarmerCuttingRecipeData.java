package com.viscript_recipe.compat.farmersdelight.data;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.compat.farmersdelight.FarmersDelightRecipeFactory;
import com.viscript_recipe.data.IVSRecipeData;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.data.RecipeOutputData;
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
public class FarmerCuttingRecipeData implements IVSRecipeData {
    @Persisted
    private RecipeIngredient input = RecipeIngredient.item(Items.BEEF);
    @Persisted
    private RecipeIngredient tool = RecipeIngredient.itemAbility("knife_dig");
    @Persisted
    private List<RecipeOutputData> results = new ArrayList<>(List.of(RecipeOutputData.of(new ItemStack(Items.BEEF))));
    @Persisted
    private boolean customSound;
    @Persisted
    private ResourceLocation sound = new ResourceLocation("item.axe.strip");

    @Override
    public Recipe<?> compile(ResourceLocation typeId) {
        return FarmersDelightRecipeFactory.compileCutting(this);
    }

    @Override
    public void applyDefaultData(ResourceLocation typeId) {
        setResults(new ArrayList<>(List.of(RecipeOutputData.of(new ItemStack(
                com.viscript_recipe.recipe.RecipeHelper.itemFromRegistry("farmersdelight:minced_beef", Items.BEEF))))));
    }
}
