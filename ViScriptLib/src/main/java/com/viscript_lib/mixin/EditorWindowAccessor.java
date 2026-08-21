package com.viscript_lib.mixin;

import com.lowdragmc.lowdraglib2.editor.ui.EditorWindow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = EditorWindow.class, remap = false)
public interface EditorWindowAccessor {
    @Accessor("windowWidth")
    void viscript_lib$setWindowWidth(float windowWidth);

    @Accessor("windowHeight")
    void viscript_lib$setWindowHeight(float windowHeight);

    @Accessor("windowLeft")
    void viscript_lib$setWindowLeft(float windowLeft);

    @Accessor("windowTop")
    void viscript_lib$setWindowTop(float windowTop);
}
