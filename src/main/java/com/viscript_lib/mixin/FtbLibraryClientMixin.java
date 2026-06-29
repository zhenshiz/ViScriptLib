package com.viscript_lib.mixin;

import com.lowdragmc.lowdraglib2.editor.ui.Editor;
import com.lowdragmc.lowdraglib2.editor.ui.EditorWindow;
import com.lowdragmc.lowdraglib2.gui.holder.ModularUIContainerScreen;
import com.lowdragmc.lowdraglib2.gui.holder.ModularUIScreen;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import dev.ftb.mods.ftblibrary.FTBLibraryClient;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(FTBLibraryClient.class)
public class FtbLibraryClientMixin {

    @Inject(method = "areButtonsVisible", at = @At("HEAD"), cancellable = true)
    private static void viscript_lib$hideSidebarButtonsInEditor(Screen screen, CallbackInfoReturnable<Boolean> cir) {
        if (viscript_lib$isEditorScreen(screen)) {
            cir.setReturnValue(false);
        }
    }

    @Unique
    private static boolean viscript_lib$isEditorScreen(Screen screen) {
        if (screen instanceof ModularUIContainerScreen containerScreen) {
            return viscript_lib$isEditorRoot(containerScreen.getMenu().getModularUI().ui.rootElement);
        }
        if (screen instanceof ModularUIScreen modularUIScreen) {
            return viscript_lib$isEditorRoot(modularUIScreen.getModularUI().ui.rootElement);
        }
        return false;
    }

    @Unique
    private static boolean viscript_lib$isEditorRoot(UIElement rootElement) {
        if (rootElement instanceof Editor) {
            return true;
        }
        return rootElement instanceof EditorWindow editorWindow
                && editorWindow.getCurrentEditor() instanceof Editor;
    }
}
