package com.viscript_recipe.mixin;

import com.viscript_recipe.compat.irons_spellbooks.IronArcaneAnvilOverrideManager;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "io.redspace.ironsspellbooks.gui.arcane_anvil.ArcaneAnvilMenu", remap = false)
public abstract class IronArcaneAnvilMenuMixin extends ItemCombinerMenu {
    public IronArcaneAnvilMenuMixin(@Nullable MenuType<?> type, int containerId, Inventory playerInventory, ContainerLevelAccess access) {
        super(type, containerId, playerInventory, access);
    }

    @Inject(method = "createResult", at = @At("HEAD"), cancellable = true, remap = false)
    private void viscriptRecipe$createArcaneAnvilOverrideResult(CallbackInfo ci) {
        var result = IronArcaneAnvilOverrideManager.findResult(inputSlots.getItem(0), inputSlots.getItem(1));
        if (result.isEmpty()) {
            return;
        }
        resultSlots.setItem(0, result.get());
        ci.cancel();
    }
}
