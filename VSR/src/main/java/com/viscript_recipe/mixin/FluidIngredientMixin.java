package com.viscript_recipe.mixin;

import com.simibubi.create.foundation.fluid.FluidIngredient;
import com.viscript_recipe.compat.create.FluidIngredientAccessor;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = FluidIngredient.class, remap = false)
public class FluidIngredientMixin implements FluidIngredientAccessor {
    @Shadow protected int amountRequired;

    @Override
    public int amountRequired() {
        return amountRequired;
    }
}

@Mixin(value = FluidIngredient.FluidStackIngredient.class, remap = false)
class FluidStack extends FluidIngredientMixin implements FluidIngredientAccessor.Stack {
    @Shadow protected Fluid fluid;

    @Override
    public Fluid fluid() {
        return fluid;
    }
}

@Mixin(value = FluidIngredient.FluidTagIngredient.class, remap = false)
class FluidTag extends FluidIngredientMixin implements FluidIngredientAccessor.Tag {
    @Shadow protected TagKey<Fluid> tag;

    @Override
    public TagKey<Fluid> tag() {
        return tag;
    }
}
