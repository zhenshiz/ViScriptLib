package com.viscript_recipe.data.ars_nouveau;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.configurator.annotation.ConfigList;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.viscript_recipe.data.RecipeIngredient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class ArsNouveauCrushRecipeData implements IPersistedSerializable, IConfigurable {
    @Configurable(name = "viscript_recipe.config.ars_nouveau.crush.input", subConfigurable = true)
    private RecipeIngredient input = RecipeIngredient.item(Items.COBBLESTONE);

    @Configurable(name = "viscript_recipe.config.ars_nouveau.crush.outputs")
    @ConfigList(addDefaultMethod = "createDefaultOutput")
    private List<ArsNouveauCrushOutputData> outputs = new ArrayList<>(List.of(new ArsNouveauCrushOutputData()));

    @Configurable(name = "viscript_recipe.config.ars_nouveau.crush.skip_block_place")
    private boolean skipBlockPlace;

    public ArsNouveauCrushOutputData createDefaultOutput() {
        return new ArsNouveauCrushOutputData();
    }

    /*public Recipe<?> compile() {
        return ArsNouveauRecipeFactory.compileCrush(this);
    }*/
}
