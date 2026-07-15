package com.viscript_recipe.compat.irons_spellbooks;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public final class IronAlchemistCauldronFluidSupport {
    private static volatile Set<ResourceLocation> allowedRecipeFluids = Set.of();

    private IronAlchemistCauldronFluidSupport() {
    }

    public static void replaceAll(Collection<FluidStack> fluids) {
        if (fluids == null || fluids.isEmpty()) {
            allowedRecipeFluids = Set.of();
            return;
        }
        var ids = new HashSet<ResourceLocation>();
        for (var stack : fluids) {
            if (stack == null || stack.isEmpty() || stack.getFluid() == Fluids.EMPTY) {
                continue;
            }
            ids.add(BuiltInRegistries.FLUID.getKey(stack.getFluid()));
        }
        allowedRecipeFluids = Set.copyOf(ids);
    }

    public static boolean allows(FluidStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        return allowedRecipeFluids.contains(BuiltInRegistries.FLUID.getKey(stack.getFluid()));
    }

    public static int allowedFluidCount() {
        return allowedRecipeFluids.size();
    }
}
