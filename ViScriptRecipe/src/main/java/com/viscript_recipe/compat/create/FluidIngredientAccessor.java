package com.viscript_recipe.compat.create;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

public interface FluidIngredientAccessor {
    int amountRequired();

    interface Tag extends FluidIngredientAccessor {
        TagKey<Fluid> tag();
    }

    interface Stack extends FluidIngredientAccessor {
        Fluid fluid();
    }
}
