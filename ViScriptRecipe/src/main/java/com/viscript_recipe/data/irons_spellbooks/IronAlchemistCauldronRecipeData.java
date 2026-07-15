package com.viscript_recipe.data.irons_spellbooks;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.configurator.annotation.ConfigList;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.viscript_recipe.data.RecipeIngredient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class IronAlchemistCauldronRecipeData implements IPersistedSerializable, IConfigurable {
    @Configurable(name = "viscript_recipe.config.irons_spellbooks.alchemist_cauldron.input", subConfigurable = true)
    private RecipeIngredient input = RecipeIngredient.item(Items.WATER_BUCKET);

    @Configurable(name = "viscript_recipe.config.irons_spellbooks.alchemist_cauldron.result")
    private ItemStack result = new ItemStack(Items.BUCKET);

    @Configurable(name = "viscript_recipe.config.irons_spellbooks.alchemist_cauldron.fluid")
    private FluidStack fluid = new FluidStack(Fluids.WATER, 1000);

    @Configurable(name = "viscript_recipe.config.irons_spellbooks.alchemist_cauldron.base_fluid")
    private FluidStack baseFluid = new FluidStack(Fluids.WATER, 1000);

    @Configurable(name = "viscript_recipe.config.irons_spellbooks.alchemist_cauldron.result_fluids")
    @ConfigList(addDefaultMethod = "createDefaultResultFluid")
    private List<FluidStack> resultFluids = new ArrayList<>(List.of(new FluidStack(Fluids.WATER, 1000)));

    @Configurable(name = "viscript_recipe.config.irons_spellbooks.alchemist_cauldron.byproduct")
    private ItemStack byproduct = ItemStack.EMPTY;

    @Configurable(name = "viscript_recipe.config.irons_spellbooks.alchemist_cauldron.must_fit_all")
    private boolean mustFitAll = true;

    @Configurable(name = "viscript_recipe.config.irons_spellbooks.alchemist_cauldron.sound")
    private ResourceLocation sound = new ResourceLocation("item.bucket.empty");

    public FluidStack createDefaultResultFluid() {
        return new FluidStack(Fluids.WATER, 1000);
    }

/*    public Recipe<?> compileFill() {
        return IronSpellbooksRecipeFactory.compileFill(this);
    }

    public Recipe<?> compileEmpty() {
        return IronSpellbooksRecipeFactory.compileEmpty(this);
    }

    public Recipe<?> compileBrew() {
        return IronSpellbooksRecipeFactory.compileBrew(this);
    }*/

    public FluidStack firstResultFluid() {
        if (resultFluids == null || resultFluids.isEmpty()) {
            return FluidStack.EMPTY;
        }
        var stack = resultFluids.get(0);
        return stack == null ? FluidStack.EMPTY : stack.copy();
    }

    public FluidStack displayResultFluid() {
        if (resultFluids == null || resultFluids.isEmpty()) {
            return FluidStack.EMPTY;
        }
        for (var stack : resultFluids) {
            if (stack != null && !stack.isEmpty()) {
                return stack.copy();
            }
        }
        return FluidStack.EMPTY;
    }

    public void setFirstResultFluid(FluidStack stack) {
        if (resultFluids == null) {
            resultFluids = new ArrayList<>();
        }
        var copy = stack == null ? FluidStack.EMPTY : stack.copy();
        if (resultFluids.isEmpty()) {
            resultFluids.add(copy);
        } else {
            resultFluids.set(0, copy);
        }
    }

    public void replaceResultFluids(List<FluidStack> stacks) {
        resultFluids = new ArrayList<>();
        if (stacks == null) {
            return;
        }
        for (var stack : stacks) {
            resultFluids.add(stack == null ? FluidStack.EMPTY : stack.copy());
        }
    }
}
