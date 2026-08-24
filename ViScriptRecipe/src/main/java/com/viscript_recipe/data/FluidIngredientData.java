package com.viscript_recipe.data;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_lib.util.ISkipDefaultedSerialize;
import com.viscript_recipe.recipe.RecipeHelper;
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

    public int getAmount() {return kind == FluidIngredientKind.FLUID && fluid.isEmpty() ? 0 : amount;}

    public FluidIngredientData setAmount(int amount) {
        amount = Math.max(1, amount);
        this.amount = amount;
        if (!fluid.isEmpty()) fluid.setAmount(amount); // 为空抛异常，逆天forge
        return this;
    }

    /**请使用工厂方法*/
    @Deprecated
    public FluidIngredientData() {}

    public static FluidIngredientData of() {return new FluidIngredientData();}
    public static FluidIngredientData empty() {return fluid(FluidStack.EMPTY);}

    public static FluidIngredientData fluid(FluidStack stack) {
        var fluid = stack == null ? FluidStack.EMPTY : stack.copy();
        return of().setKind(FluidIngredientKind.FLUID).setFluid(fluid).setAmount(fluid.getAmount());
    }

    public static FluidIngredientData tag(ResourceLocation tag) {
        return of().setKind(FluidIngredientKind.TAG).setTag(tag);
    }

    public FluidIngredientData copy() {
        return of().setKind(kind).setFluid(fluid.copy()).setTag(tag).setAmount(getAmount());
    }

    public boolean isEmpty() {
        if (kind == FluidIngredientKind.TAG) return tag == null || amount <= 0;
        return fluid.isEmpty() || amount <= 0;
    }

    public FluidStack[] getFluidStacks() {
        return switch (kind) {
            case FLUID -> fluid.isEmpty() ? new FluidStack[0] : new FluidStack[]{fluid.copy()};
            case TAG -> RecipeHelper.fluidsFromTag(tag, getAmount());
        };
    }
}
