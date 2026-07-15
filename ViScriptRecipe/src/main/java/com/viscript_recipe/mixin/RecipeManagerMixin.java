package com.viscript_recipe.mixin;

import com.google.gson.JsonElement;
import com.lowdragmc.lowdraglib2.Platform;
import com.viscript_recipe.recipe.RecipeOverrideManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(value = RecipeManager.class, priority = 900)
public abstract class RecipeManagerMixin {
    @Inject(
            method = "apply*",
            at = @At("TAIL")
    )
    private void viscriptRecipe$applyOverrides(Map<?, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler, CallbackInfo ci) {
        RecipeOverrideManager.apply((RecipeManager) (Object) this, Platform.getFrozenRegistry(), resourceManager);
    }
}
