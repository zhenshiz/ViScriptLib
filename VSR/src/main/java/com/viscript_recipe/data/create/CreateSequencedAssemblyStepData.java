package com.viscript_recipe.data.create;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_lib.util.ISkipDefaultedSerialize;
import com.viscript_recipe.data.RecipeIngredient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;

@Getter
@Setter
@Accessors(chain = true)
public class CreateSequencedAssemblyStepData implements ISkipDefaultedSerialize, IConfigurable {
    @Persisted
    private CreateSequencedAssemblyStepKind kind = CreateSequencedAssemblyStepKind.DEPLOYING;
    @Persisted
    private RecipeIngredient ingredient = RecipeIngredient.item(Items.IRON_NUGGET);
    @Persisted
    private FluidIngredientData fluidIngredient = FluidIngredientData.fluid(new FluidStack(Fluids.WATER, 250));
    @Persisted
    private int processingTime = 100;
    @Persisted
    private boolean keepHeldItem;

    public boolean isFluidIngredient() {return kind == CreateSequencedAssemblyStepKind.FILLING;}

    public CreateSequencedAssemblyStepData copy() {
        return new CreateSequencedAssemblyStepData()
                .setKind(kind).setIngredient(ingredient.copy())
                .setFluidIngredient(fluidIngredient == null ? FluidIngredientData.empty() : fluidIngredient.copy())
                .setProcessingTime(processingTime)
                .setKeepHeldItem(keepHeldItem);
    }
}
