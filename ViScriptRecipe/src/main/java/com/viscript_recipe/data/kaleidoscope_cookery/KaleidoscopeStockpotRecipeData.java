package com.viscript_recipe.data.kaleidoscope_cookery;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.configurator.annotation.ConfigList;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.viscript_recipe.data.RecipeIngredient;
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
public class KaleidoscopeStockpotRecipeData implements IPersistedSerializable, IConfigurable {
    @Configurable(name = "viscript_recipe.config.kaleidoscope_cookery.ingredients")
    @ConfigList(addDefaultMethod = "createDefaultIngredient")
    private List<RecipeIngredient> ingredients = new ArrayList<>(List.of(
            RecipeIngredient.item(Items.BEEF),
            RecipeIngredient.item(Items.CARROT),
            RecipeIngredient.item(Items.POTATO)
    ));

    @Configurable(name = "viscript_recipe.config.kaleidoscope_cookery.soup_base")
    private ResourceLocation soupBase = new ResourceLocation("water");

    @Configurable(name = "viscript_recipe.config.recipe.result")
    private ItemStack result = new ItemStack(Items.RABBIT_STEW);

    @Configurable(name = "viscript_recipe.config.kaleidoscope_cookery.time")
    private int time = 300;

    @Configurable(name = "viscript_recipe.config.kaleidoscope_cookery.carrier")
    private RecipeIngredient carrier = RecipeIngredient.item(Items.BOWL);

    @Configurable(name = "viscript_recipe.config.kaleidoscope_cookery.cooking_texture")
    private ResourceLocation cookingTexture = kaleidoscope("stockpot/default_cooking");

    @Configurable(name = "viscript_recipe.config.kaleidoscope_cookery.finished_texture")
    private ResourceLocation finishedTexture = kaleidoscope("stockpot/default_finished");

    @Configurable(name = "viscript_recipe.config.kaleidoscope_cookery.cooking_bubble_color")
    private int cookingBubbleColor = 0xFFECC3;

    @Configurable(name = "viscript_recipe.config.kaleidoscope_cookery.finished_bubble_color")
    private int finishedBubbleColor = 0xF4AA8B;

    public RecipeIngredient createDefaultIngredient() {
        return RecipeIngredient.item(Items.CARROT);
    }

/*    public Recipe<?> compile() {
        return KaleidoscopeCookeryRecipeFactory.compileStockpot(this);
    }*/

    private static ResourceLocation kaleidoscope(String path) {
        return new ResourceLocation(KaleidoscopeCookeryRecipeEditorTypes.MOD_ID, path);
    }
}
