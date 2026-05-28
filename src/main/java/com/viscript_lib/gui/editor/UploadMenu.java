package com.viscript_lib.gui.editor;

import com.lowdragmc.lowdraglib2.editor.ui.Editor;
import com.lowdragmc.lowdraglib2.editor.ui.menu.MenuTab;
import com.lowdragmc.lowdraglib2.gui.util.TreeBuilder;
import net.minecraft.network.chat.Component;

/**
 * 项目型编辑器和纯功能文件编辑器共用的上传菜单。
 *
 * <p>纯功能文件编辑器只显示一个上传动作；工程文件编辑器显示工程文件、
 * 运行时文件和两者一起上传三个动作。
 */
public class UploadMenu extends MenuTab {
    /**
     * 创建上传菜单。
     *
     * @param editor 所属编辑器
     */
    public UploadMenu(Editor editor) {
        super(editor);
    }

    @Override
    protected TreeBuilder.Menu createDefaultMenu() {
        var menu = TreeBuilder.Menu.start();
        if (editor instanceof ProjectFileEditor projectFileEditor) {
            addServerUpload(menu, projectFileEditor.createUploadProjectAction(), projectFileEditor::uploadProjectToServer);
            addServerUpload(menu, projectFileEditor.createUploadRuntimeAction(), projectFileEditor::uploadRuntimeToServer);
            addServerUpload(menu, projectFileEditor.createUploadProjectAndRuntimeAction(), projectFileEditor::uploadProjectAndRuntimeToServer);
        } else if (editor instanceof FunctionFileEditor functionFileEditor) {
            addServerUpload(menu, functionFileEditor.createServerUploadAction(), functionFileEditor::uploadToServer);
        }
        return menu;
    }

    private void addServerUpload(TreeBuilder.Menu menu, EditorUploadAction action, Runnable uploadAction) {
        if (action != null) {
            menu.leaf(action.getIcon(), action.getDisplayName(), uploadAction);
        }
    }

    @Override
    protected Component getComponent() {
        return Component.translatable("viscript_lib.editor.menu.upload");
    }
}
