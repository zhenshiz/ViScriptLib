package com.viscript_recipe.data.create;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.configurator.annotation.ConfigSelector;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
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
public class CreateSequencedAssemblyStepData implements IPersistedSerializable, IConfigurable {
    @Configurable(name = "viscript_recipe.config.create.sequenced_assembly.step.kind")
    @ConfigSelector(candidate = {"deploying", "pressing", "cutting", "filling"})
    private CreateSequencedAssemblyStepKind kind = CreateSequencedAssemblyStepKind.DEPLOYING;

    @Configurable(name = "viscript_recipe.config.create.sequenced_assembly.step.ingredient", subConfigurable = true)
    private RecipeIngredient ingredient = RecipeIngredient.item(Items.IRON_NUGGET);

    @Configurable(name = "viscript_recipe.config.create.sequenced_assembly.step.fluid_ingredient", subConfigurable = true)
    private CreateFluidIngredientData fluidIngredient = CreateFluidIngredientData.fluid(new FluidStack(Fluids.WATER, 250));

    @Configurable(name = "viscript_recipe.config.create.processing_time")
    private int processingTime = 100;

    @Configurable(name = "viscript_recipe.config.create.keep_held_item")
    private boolean keepHeldItem;

    public CreateSequencedAssemblyStepData copy() {
        return new CreateSequencedAssemblyStepData()
                .setKind(kind == null ? CreateSequencedAssemblyStepKind.DEPLOYING : kind)
                .setIngredient(copyIngredient(ingredient))
                .setFluidIngredient(fluidIngredient == null ? CreateFluidIngredientData.empty() : fluidIngredient.copy())
                .setProcessingTime(processingTime)
                .setKeepHeldItem(keepHeldItem);
    }

    private static RecipeIngredient copyIngredient(RecipeIngredient original) {
        if (original == null) {
            return new RecipeIngredient();
        }
        var copy = new RecipeIngredient();
        for (var value : original.getValues()) {
            var valueCopy = new com.viscript_recipe.data.RecipeIngredientValue()
                    .setKind(value.getKind())
                    .setTag(value.getTag())
                    .setItemAbility(value.getItemAbility());
            if (value.getItem() != null) {
                valueCopy.setItem(value.getItem().copy());
            }
            copy.getValues().add(valueCopy);
        }
        return copy;
    }
}
