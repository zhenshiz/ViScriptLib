package com.viscript_recipe.data.ars_nouveau;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.configurator.annotation.ConfigList;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.viscript_recipe.data.RecipeIngredient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class ArsNouveauGlyphRecipeData implements IPersistedSerializable, IConfigurable {
    @Configurable(name = "viscript_recipe.config.ars_nouveau.glyph.inputs")
    @ConfigList(addDefaultMethod = "createDefaultInput")
    private List<RecipeIngredient> inputs = new ArrayList<>(List.of(
            RecipeIngredient.item(Items.PAPER),
            RecipeIngredient.item(Items.AMETHYST_SHARD)
    ));

    @Configurable(name = "viscript_recipe.config.recipe.result")
    private ItemStack result = new ItemStack(Items.PAPER);

    @Configurable(name = "viscript_recipe.config.ars_nouveau.glyph.exp")
    private int exp = 27;

    public RecipeIngredient createDefaultInput() {
        return RecipeIngredient.item(Items.AMETHYST_SHARD);
    }

    /*public Recipe<?> compile() {
        return ArsNouveauRecipeFactory.compileGlyph(this);
    }*/
}
