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
import java.util.Map;

import static com.viscript_recipe.compat.avaritia.AvaritiaRecipeEditorTypes.*;
import static com.viscript_recipe.recipe.RecipeHelper.itemFromRegistry;

@Getter
@Setter
@Accessors(chain = true)
public class AvaritiaSpecialShapelessRecipeData implements IVSRecipeData {
    @Persisted
    private String group = "default";
    @Persisted
    private List<RecipeIngredient> ingredients = new ArrayList<>(List.of(
            RecipeIngredient.item(Items.BEDROCK)
    ));
    @Persisted
    private int count = 1;

    static final Map<ResourceLocation, String> RESULTS = Map.of(
            INFINITY_CATALYST, "avaritia:infinity_catalyst",
            FULL_MATTER_CLUSTER, "avaritia:full_matter_cluster",
            ETERNAL_SINGULARITY, "avaritia:eternal_singularity"
    );
    public ItemStack result(ResourceLocation type) {
        return AvaritiaRecipeFactory.defaultItemStack(RESULTS.get(type), Items.NETHER_STAR);
    }

    @Override
    public String[] getCompatNames() {
        return new String[]{"avaritiaEternalSingularity", "avaritiaFullMatterCluster", "avaritiaInfinityCatalyst"};
    }

    @Override
    public Recipe<?> compile(ResourceLocation type) {
        if (type.equals(INFINITY_CATALYST)) {
            return AvaritiaRecipeFactory.compileInfinityCatalyst(this);
        } else if (type.equals(FULL_MATTER_CLUSTER)) {
            return AvaritiaRecipeFactory.compileFullMatterCluster(this);
        } else return AvaritiaRecipeFactory.compileEternalSingularity(this);
    }

    @Override
    public void applyDefaultData(ResourceLocation typeId) {
        if (typeId.equals(INFINITY_CATALYST)) setIngredients(new ArrayList<>(
                List.of(RecipeIngredient.item(itemFromRegistry("avaritia:neutron_ingot", Items.IRON_INGOT)))));
        else if (typeId.equals(ETERNAL_SINGULARITY)) setIngredients(new ArrayList<>(
                List.of(RecipeIngredient.item(itemFromRegistry("avaritia:singularity", Items.NETHER_STAR)))));
        else if (typeId.equals(FULL_MATTER_CLUSTER)) setIngredients(new ArrayList<>(List.of(RecipeIngredient.item(Items.CHEST))));
    }
}
