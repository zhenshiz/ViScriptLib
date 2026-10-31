package com.viscript_lib.mixin;

import com.lowdragmc.lowdraglib2.client.window.OsWindowHints;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** 使独立窗口使用主窗口实际创建的 OpenGL 版本，以满足共享上下文的要求。 */
@Mixin(value = OsWindowHints.class, remap = false)
public abstract class OsWindowHintsMixin {
    @Inject(method = "applyChildWindowHints", at = @At("RETURN"))
    private static void viscriptNpc$matchMainContext(boolean decorated, CallbackInfo callback) {
        long mainWindow = Minecraft.getInstance().getWindow().getWindow();
        // NeoForge 的早期窗口可能选择 4.1；macOS 无法与固定请求的 3.2 上下文共享资源。
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR,
                GLFW.glfwGetWindowAttrib(mainWindow, GLFW.GLFW_CONTEXT_VERSION_MAJOR));
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR,
                GLFW.glfwGetWindowAttrib(mainWindow, GLFW.GLFW_CONTEXT_VERSION_MINOR));
    }
}
