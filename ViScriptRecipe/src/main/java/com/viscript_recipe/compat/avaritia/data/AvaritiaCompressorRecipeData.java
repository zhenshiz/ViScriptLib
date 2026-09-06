package com.viscript_recipe.compat.avaritia.data;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.compat.avaritia.AvaritiaRecipeFactory;
import com.viscript_recipe.data.IVSRecipeData;
import com.viscript_recipe.data.RecipeIngredient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;

@Getter
@Setter
@Accessors(chain = true)
public class AvaritiaCompressorRecipeData implements IVSRecipeData {
    @Persisted
    private RecipeIngredient ingredient = RecipeIngredient.item(Items.COBBLESTONE);
    @Persisted
    private ItemStack result = new ItemStack(Items.BEDROCK);
    @Persisted
    private int timeCost = 240;

    @Override
    public Recipe<?> compile(ResourceLocation type) {
        return AvaritiaRecipeFactory.compileCompressor(this);
    }

    @Override
    public void applyDefaultData(ResourceLocation typeId) {
        setIngredient(RecipeIngredient.item(Items.COBBLESTONE).setCount(1000))
                .setResult(new ItemStack(com.viscript_recipe.recipe.RecipeHelper.itemFromRegistry
                        ("avaritia:singularity", Items.BEDROCK)));
    }
}
