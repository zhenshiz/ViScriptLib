package com.viscript_recipe.data.farmersdelight;

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
    private RecipeIngredient tool = defaultKnifeTool();
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

    public static RecipeIngredient defaultKnifeTool() {
        return RecipeIngredient.itemAbility("knife_dig");
    }
}
