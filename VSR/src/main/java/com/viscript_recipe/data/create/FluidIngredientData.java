package com.viscript_recipe.data.create;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_lib.util.ISkipDefaultedSerialize;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;

@Getter
@Setter
@Accessors(chain = true)
public class FluidIngredientData implements ISkipDefaultedSerialize, IConfigurable {
    @Persisted
    private FluidIngredientKind kind = FluidIngredientKind.FLUID;
    @Persisted
    private FluidStack fluid = new FluidStack(Fluids.WATER, 1000);
    @Persisted
    private ResourceLocation tag = new ResourceLocation("c", "milk");
    @Persisted
    private int amount = 1000;

    /**请使用工厂方法*/
    @Deprecated
    public FluidIngredientData() {}

    public static FluidIngredientData empty() {return fluid(FluidStack.EMPTY);}

    public static FluidIngredientData fluid(FluidStack stack) {
        var fluid = stack == null ? FluidStack.EMPTY : stack.copy();
        return new FluidIngredientData()
                .setKind(FluidIngredientKind.FLUID)
                .setFluid(fluid)
                .setAmount(fluid.isEmpty() ? 0 : Math.max(1, fluid.getAmount()));
    }

    public static FluidIngredientData tag(ResourceLocation tag) {
        return new FluidIngredientData().setKind(FluidIngredientKind.TAG).setTag(tag);
    }

    public FluidIngredientData copy() {
        return new FluidIngredientData().setKind(kind).setFluid(fluid.copy()).setTag(tag).setAmount(amount);
    }

    public boolean isEmpty() {
        if (kind == FluidIngredientKind.TAG) return tag == null || amount <= 0;
        return fluid.isEmpty() || amount <= 0;
    }

    public FluidStack[] getFluidStacks() {
        return switch (kind) {
            case FLUID -> fluid.isEmpty() ? new FluidStack[0] : new FluidStack[]{fluid};
            case TAG -> RecipeCanvas.fluidsFromTag(tag, amount);
        };
    }
}
