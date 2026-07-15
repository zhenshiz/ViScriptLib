package com.viscript_recipe.data.farmersdelight;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.configurator.annotation.ConfigList;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.data.RecipeIngredientValue;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class FarmerCuttingRecipeData implements IPersistedSerializable, IConfigurable {
    @Configurable(name = "viscript_recipe.config.farmersdelight.cutting.input", subConfigurable = true)
    private RecipeIngredient input = RecipeIngredient.item(Items.BEEF);

    @Configurable(name = "viscript_recipe.config.farmersdelight.cutting.tool", subConfigurable = true)
    private RecipeIngredient tool = defaultKnifeTool();

    @Configurable(name = "viscript_recipe.config.farmersdelight.cutting.results")
    @ConfigList(addDefaultMethod = "createDefaultResult")
    private List<FarmerCuttingResultData> results = new ArrayList<>(List.of(new FarmerCuttingResultData()
            .setItem(new ItemStack(Items.BEEF))
            .setChance(1.0F)));

    @Configurable(name = "viscript_recipe.config.farmersdelight.cutting.custom_sound")
    private boolean customSound;

    @Configurable(name = "viscript_recipe.config.farmersdelight.cutting.sound")
    private ResourceLocation sound = new ResourceLocation("item.axe.strip");

    public FarmerCuttingResultData createDefaultResult() {
        return new FarmerCuttingResultData();
    }

//    public Recipe<?> compile() {
//        return FarmersDelightRecipeFactory.compileCutting(this);
//    }

    public static RecipeIngredient defaultKnifeTool() {
        var ingredient = new RecipeIngredient();
        ingredient.getValues().add(RecipeIngredientValue.itemAbility("knife_dig"));
        return ingredient;
    }
}
