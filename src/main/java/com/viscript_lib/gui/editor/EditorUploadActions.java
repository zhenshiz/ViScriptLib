package com.viscript_lib.gui.editor;

import com.lowdragmc.lowdraglib2.editor.ui.Editor;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Dialog;
import com.viscript_lib.ViScriptLib;

import javax.annotation.Nullable;

/**
 * 负责显示上传文件名输入框并触发上传动作。
 */
final class EditorUploadActions {

    static void uploadToServer(Editor editor, @Nullable EditorUploadAction action) {
        if (action == null) return;

        Dialog.stringEditorDialog(action.getDialogTitleKey(), action.getDefaultFileName(),
                fileName -> fileName != null && !fileName.isBlank(),
                fileName -> uploadToServer(editor, action, fileName)
        ).show(editor.getModularUI());
    }

    private static void uploadToServer(Editor editor, EditorUploadAction action, String fileName) {
        try {
            var normalizedFileName = action.normalizeFileName(fileName);
            action.uploadToServer(normalizedFileName);
            Dialog.showNotification("viscript_lib.editor.server_upload_sent", 2)
                    .show(editor.getModularUI());
        } catch (Exception e) {
            ViScriptLib.LOGGER.error("Failed to upload editor data to server", e);
            Dialog.showNotification("editor.error", "viscript_lib.editor.server_upload_failed", null)
                    .show(editor.getModularUI());
        }
    }
}
