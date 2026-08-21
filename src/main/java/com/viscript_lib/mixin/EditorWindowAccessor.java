package com.viscript_lib.mixin;

import com.lowdragmc.lowdraglib2.editor.ui.EditorWindow;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(EditorWindow.class)
public interface EditorWindowAccessor {
    @Accessor("MINIMIZED_WINDOWS")
    static Map<ResourceLocation, EditorWindow> viscript_lib$getMinimizedWindows() {
        throw new AssertionError();
    }

    @Accessor("windowWidth")
    void viscript_lib$setWindowWidth(float windowWidth);

    @Accessor("windowHeight")
    void viscript_lib$setWindowHeight(float windowHeight);

    @Accessor("windowLeft")
    void viscript_lib$setWindowLeft(float windowLeft);

    @Accessor("windowTop")
    void viscript_lib$setWindowTop(float windowTop);
}
