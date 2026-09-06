package com.viscript_recipe.compat.goety.data;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.compat.goety.GoetyRecipeFactory;
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

import static com.viscript_recipe.recipe.RecipeHelper.itemFromRegistry;

@Getter
@Setter
@Accessors(chain = true)
public class GoetyBrazierRecipeData implements IVSRecipeData {
    public static final int INPUT_COUNT = 3;

    @Persisted
    private List<RecipeIngredient> ingredients = new ArrayList<>();
    @Persisted
    private ItemStack result = new ItemStack(Items.IRON_INGOT);
    @Persisted
    private int soulCost = 500;

    @Override
    public Recipe<?> compile(ResourceLocation type) {
        return GoetyRecipeFactory.compileBrazier(this);
    }

    @Override
    public void applyDefaultData(ResourceLocation typeId) {
        setResult(new ItemStack(itemFromRegistry("goety:dark_ingot", Items.NETHERITE_INGOT)))
                .setIngredients(new ArrayList<>(List.of(
                        RecipeIngredient.item(Items.COAL),
                        RecipeIngredient.item(Items.SCULK),
                        RecipeIngredient.item(itemFromRegistry("goety:cursed_ingot", Items.IRON_INGOT))
                )));
    }
}
