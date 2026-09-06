package com.viscript_recipe.compat.goety.data;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.compat.goety.GoetyRecipeFactory;
import com.viscript_recipe.data.IVSRecipeData;
import com.viscript_recipe.data.RecipeIngredient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;

@Getter
@Setter
@Accessors(chain = true)
public class GoetyBrewingRecipeData implements IVSRecipeData {
    @Persisted
    private RecipeIngredient ingredient = RecipeIngredient.item(Items.SPIDER_EYE);
    @Persisted
    private ResourceLocation effect = new ResourceLocation("poison");
    @Persisted
    private int soulCost = 25;
    @Persisted
    private int capacityExtra = 1;
    @Persisted
    private int duration = 600;
    @Persisted
    private GoetyBrewingEntityKind entityKind = GoetyBrewingEntityKind.NONE;
    @Persisted
    private ResourceLocation entity = new ResourceLocation("zombie");

    @Override
    public Recipe<?> compile(ResourceLocation type) {
        return GoetyRecipeFactory.compileBrewing(this);
    }
}
