package com.viscript_lib.mixin;

import com.lowdragmc.lowdraglib2.editor.ui.Editor;
import com.lowdragmc.lowdraglib2.editor.ui.EditorWindow;
import com.lowdragmc.lowdraglib2.gui.holder.ModularUIContainerScreen;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;

@Pseudo
@Mixin(targets = "dev.ftb.mods.ftblibrary.FTBLibraryClient", remap = false)
public class FtbLibraryClientMixin {

    @Unique
    private static boolean viscript_shop$isShopEditorOpened(Screen screen) {
        if (screen instanceof ModularUIContainerScreen containerScreen) {
            var rootElement = containerScreen.getMenu().getModularUI().ui.rootElement;
            if (rootElement instanceof Editor) {
                return true;
            }
            if (rootElement instanceof EditorWindow editorWindow) {
                return editorWindow.getCurrentEditor() instanceof Editor;
            }
        }
        return false;
    }
}
