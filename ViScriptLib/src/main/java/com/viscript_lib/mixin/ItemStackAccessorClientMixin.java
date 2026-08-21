package com.viscript_lib.mixin;

import com.lowdragmc.lowdraglib2.configurator.accessors.ItemStackAccessor;
import com.lowdragmc.lowdraglib2.gui.ColorPattern;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Dialog;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvent;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEventDispatcher;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ItemStackAccessor.class, remap = false)
public class ItemStackAccessorClientMixin {

    @Inject(
            method = "createPickerSlot(Lnet/minecraft/world/item/ItemStack;[Lcom/lowdragmc/lowdraglib2/gui/ui/elements/ItemSlot;[Lnet/minecraft/world/item/ItemStack;)Lcom/lowdragmc/lowdraglib2/gui/ui/elements/ItemSlot;",
            at = @At("RETURN")
    )
    private static void viscript_lib$selectInventoryItemOnDoubleClick(
            ItemStack stack,
            ItemSlot[] selected,
            ItemStack[] selectedStack,
            CallbackInfoReturnable<ItemSlot> cir
    ) {
        var itemSlot = cir.getReturnValue();
        itemSlot.addEventListener(UIEvents.DOUBLE_CLICK, event -> {
            if (event.button != 0 || itemSlot.getValue().isEmpty()) {
                return;
            }

            viscript_lib$markSelected(itemSlot, selected, selectedStack);
            var dialog = viscript_lib$findDialog(itemSlot);
            var confirmButton = dialog == null ? null : viscript_lib$findConfirmButton(dialog);
            if (confirmButton == null) {
                return;
            }

            event.stopImmediatePropagation();
            var confirmEvent = UIEvent.create(UIEvents.MOUSE_DOWN);
            confirmEvent.x = event.x;
            confirmEvent.y = event.y;
            confirmEvent.button = event.button;
            confirmEvent.target = confirmButton;
            UIEventDispatcher.dispatchEvent(confirmEvent, true, true, false);
        });
    }

    @Unique
    private static void viscript_lib$markSelected(ItemSlot itemSlot, ItemSlot[] selected, ItemStack[] selectedStack) {
        if (selected[0] == itemSlot) {
            return;
        }
        if (selected[0] != null) {
            selected[0].getStyle().overlayTexture(IGuiTexture.EMPTY);
        }
        selected[0] = itemSlot;
        selectedStack[0] = itemSlot.getValue();
        itemSlot.getStyle().overlayTexture(ColorPattern.T_BLUE.rectTexture());
    }

    @Unique
    @Nullable
    private static Dialog viscript_lib$findDialog(UIElement element) {
        var current = element;
        while (current != null) {
            if (current instanceof Dialog dialog) {
                return dialog;
            }
            current = current.getParent();
        }
        return null;
    }

    @Unique
    @Nullable
    private static Button viscript_lib$findConfirmButton(UIElement element) {
        if (element instanceof Button button && button.hasClass("__confirm-button__")) {
            return button;
        }
        for (var child : element.getChildren()) {
            var button = viscript_lib$findConfirmButton(child);
            if (button != null) {
                return button;
            }
        }
        return null;
    }
}
