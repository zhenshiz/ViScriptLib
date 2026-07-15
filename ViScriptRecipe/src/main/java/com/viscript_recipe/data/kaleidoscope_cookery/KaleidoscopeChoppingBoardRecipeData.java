package com.viscript_recipe.data.kaleidoscope_cookery;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.viscript_recipe.data.RecipeIngredient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@Getter
@Setter
@Accessors(chain = true)
public class KaleidoscopeChoppingBoardRecipeData implements IPersistedSerializable, IConfigurable {
    @Configurable(name = "viscript_recipe.config.kaleidoscope_cookery.ingredient")
    private RecipeIngredient ingredient = RecipeIngredient.item(Items.BEEF);

    @Configurable(name = "viscript_recipe.config.recipe.result")
    private ItemStack result = new ItemStack(Items.BEEF);

    @Configurable(name = "viscript_recipe.config.kaleidoscope_cookery.cut_count")
    private int cutCount = 4;

    @Configurable(name = "viscript_recipe.config.kaleidoscope_cookery.model_id")
    private ResourceLocation modelId = new ResourceLocation(KaleidoscopeCookeryRecipeEditorTypes.MOD_ID, "empty");

/*    public Recipe<?> compile() {
        return KaleidoscopeCookeryRecipeFactory.compileChoppingBoard(this);
    }*/
}
