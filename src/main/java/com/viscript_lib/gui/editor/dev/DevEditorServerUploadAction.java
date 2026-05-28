package com.viscript_lib.gui.editor.dev;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.Icons;
import com.viscript_lib.gui.editor.EditorUploadAction;
import com.viscript_lib.gui.editor.EditorServerUploads;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import java.util.function.Supplier;

/**
 * 开发环境无工程文件编辑器的上传动作。
 */
record DevEditorServerUploadAction(String displayKey, Supplier<String> contentSupplier) implements EditorUploadAction {
    @Override
    public IGuiTexture getIcon() {
        return Icons.EXPORT;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(displayKey);
    }

    @Override
    public String getDefaultFileName() {
        return "test";
    }

    @Override
    public String getSuffix() {
        return DevFunctionFileProjectType.FORMAT.runtimeSuffix();
    }

    @Override
    public void uploadToServer(String fileName) {
        var tag = new CompoundTag();
        tag.putString("type", "function_file");
        tag.putString("content", contentSupplier.get());
        EditorServerUploads.uploadToServer(DevFunctionFileProjectType.FORMAT, fileName, tag);
    }
}
