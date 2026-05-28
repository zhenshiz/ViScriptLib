package com.viscript_lib.gui.editor.dev;

import com.lowdragmc.lowdraglib2.editor.ui.Editor;
import com.viscript_lib.gui.editor.EditorUploadAction;
import com.viscript_lib.gui.editor.FunctionFileEditor;

import javax.annotation.Nonnull;

/**
 * 开发环境无工程文件编辑器。
 */
public class DevFunctionFileEditor extends FunctionFileEditor {
    public DevFunctionFileEditor() {
        registerFunctionFileType(DevFunctionFileProjectType.TYPE);
    }

    @Override
    protected @Nonnull Editor createNewEditorInstance() {
        return new DevFunctionFileEditor();
    }

    @Override
    protected EditorUploadAction createServerUploadAction() {
        if (getCurrentProject() instanceof DevFunctionFileProject project) {
            return new DevEditorServerUploadAction(
                    "viscript_lib.dev_editor.function_file.upload",
                    project::getContent
            );
        }
        return null;
    }
}
