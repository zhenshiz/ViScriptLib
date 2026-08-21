package com.viscript_lib.gui.editor.dev;

import com.lowdragmc.lowdraglib2.editor.ui.Editor;
import com.viscript_lib.gui.editor.ProjectFileEditor;

import javax.annotation.Nonnull;

/**
 * 开发环境工程文件编辑器。
 */
public class DevProjectFileEditor extends ProjectFileEditor {
    public DevProjectFileEditor() {
        removeLeftWindow();
        removeBottomWindow();
        registerProjectType(DevProjectFileProjectType.TYPE);
    }

    @Override
    protected @Nonnull Editor createNewEditorInstance() {
        return new DevProjectFileEditor();
    }
}
