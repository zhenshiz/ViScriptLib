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

import java.util.ArrayList;
import java.util.List;

import static com.viscript_recipe.recipe.RecipeHelper.itemFromRegistry;

@Getter
@Setter
@Accessors(chain = true)
public class AvaritiaExtremeSmithingRecipeData implements IVSRecipeData {
    @Persisted
    private RecipeIngredient template = RecipeIngredient.item(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE);
    @Persisted
    private RecipeIngredient base = RecipeIngredient.item(Items.NETHERITE_CHESTPLATE);
    @Persisted
    private List<RecipeIngredient> additions = new ArrayList<>(List.of(
            RecipeIngredient.item(Items.NETHERITE_INGOT),
            RecipeIngredient.item(Items.NETHERITE_INGOT),
            RecipeIngredient.item(Items.NETHERITE_INGOT)
    ));
    @Persisted
    private ItemStack result = new ItemStack(Items.NETHERITE_SWORD);

    @Override
    public Recipe<?> compile(ResourceLocation type) {
        return AvaritiaRecipeFactory.compileExtremeSmithing(this);
    }

    @Override
    public void applyDefaultData(ResourceLocation typeId) {
        setTemplate(RecipeIngredient.item(itemFromRegistry("avaritia:infinity_catalyst", Items.NETHER_STAR)))
                .setAdditions(new ArrayList<>(List.of(
                        RecipeIngredient.item(itemFromRegistry("avaritia:neutron_ingot", Items.IRON_INGOT)),
                        RecipeIngredient.item(itemFromRegistry("avaritia:enhancement_core", Items.NETHER_STAR)),
                        RecipeIngredient.item(itemFromRegistry("avaritia:infinity_catalyst", Items.NETHER_STAR))
                )))
                .setResult(new ItemStack(itemFromRegistry("avaritia:infinity_chestplate", Items.NETHERITE_CHESTPLATE)));
    }
}
