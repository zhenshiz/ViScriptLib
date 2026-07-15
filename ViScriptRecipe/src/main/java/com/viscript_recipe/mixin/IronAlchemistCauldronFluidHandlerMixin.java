package com.viscript_recipe.mixin;

import com.viscript_recipe.compat.irons_spellbooks.IronAlchemistCauldronFluidSupport;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "io.redspace.ironsspellbooks.block.alchemist_cauldron.AlchemistCauldronTile$AlchemistCauldronFluidHandler", remap = false)
public abstract class IronAlchemistCauldronFluidHandlerMixin {
    @Redirect(
            method = "fill",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/fluids/FluidStack;is(Lnet/minecraft/tags/TagKey;)Z"
            ),
            remap = false
    )
    private boolean viscriptRecipe$allowRecipeFluidInAlchemistCauldron(FluidStack stack, TagKey<Fluid> tag) {
        return stack.getFluid().is(tag) && !IronAlchemistCauldronFluidSupport.allows(stack);
    }
}
