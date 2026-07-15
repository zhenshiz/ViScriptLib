package com.viscript_recipe.data.create;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.configurator.annotation.ConfigRL;
import com.lowdragmc.lowdraglib2.configurator.annotation.ConfigSelector;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;

@Getter
@Setter
@Accessors(chain = true)
public class CreateFluidIngredientData implements IPersistedSerializable, IConfigurable {
    @Configurable(name = "viscript_recipe.config.create.fluid_ingredient.kind")
    @ConfigSelector(candidate = {"fluid", "tag"})
    private CreateFluidIngredientKind kind = CreateFluidIngredientKind.FLUID;

    @Configurable(name = "viscript_recipe.config.create.fluid_ingredient.fluid")
    private FluidStack fluid = new FluidStack(Fluids.WATER, 1000);

    @Configurable(name = "viscript_recipe.config.create.fluid_ingredient.tag")
    @ConfigRL(ConfigRL.Type.FLUID_TAG_KEY)
    private ResourceLocation tag = new ResourceLocation("c", "milk");

    @Configurable(name = "viscript_recipe.config.create.fluid_ingredient.amount")
    private int amount = 1000;

    public static CreateFluidIngredientData empty() {
        return fluid(FluidStack.EMPTY);
    }

    public static CreateFluidIngredientData fluid(FluidStack stack) {
        var fluid = stack == null ? FluidStack.EMPTY : stack.copy();
        return new CreateFluidIngredientData()
                .setKind(CreateFluidIngredientKind.FLUID)
                .setFluid(fluid)
                .setAmount(fluid.isEmpty() ? 0 : Math.max(1, fluid.getAmount()));
    }

    public CreateFluidIngredientData copy() {
        return new CreateFluidIngredientData()
                .setKind(kind == null ? CreateFluidIngredientKind.FLUID : kind)
                .setFluid(fluid == null ? FluidStack.EMPTY : fluid.copy())
                .setTag(tag)
                .setAmount(amount);
    }
}
