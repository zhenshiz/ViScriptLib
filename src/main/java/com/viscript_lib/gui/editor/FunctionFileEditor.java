package com.viscript_lib.gui.editor;

import com.lowdragmc.lowdraglib2.editor.project.ProjectType;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Dialog;

import javax.annotation.Nullable;
import java.io.File;

/**
 * 运行时文件和功能文件一体的编辑器基类。
 *
 * <p>这种编辑器没有单独工程文件，LDLib2 的保存、另存为和打开都直接作用于运行时文件。
 */
public abstract class FunctionFileEditor extends ViScriptEditor {
    @Override
    protected void initMenus() {
        super.initMenus();
        EditorFileMenuHelper.replaceOpen(fileMenu, this::openFunctionFile);
        fileMenu.registerMenuCreator((tab, menu) -> addExportLeaf(menu));
        var uploadMenu = new UploadMenu(this);
        menuContainer.addChild(uploadMenu.createMenuTab());
    }

    /**
     * 创建上传运行时文件的动作。
     *
     * @return 当前项目可上传时返回上传动作，否则返回 <code>null</code>
     */
    @Nullable
    protected EditorUploadAction createServerUploadAction() {
        return null;
    }

    /**
     * 从运行时文件目录打开文件。
     */
    public final void openFunctionFile() {
        if (projectTypes.isEmpty()) return;
        var suffixes = projectTypes.stream().map(ProjectType::getSuffix).toArray(String[]::new);
        var root = projectTypes.getFirst().getRootSavePath(null, null);
        Dialog.showFileDialog("ldlib.gui.editor.tips.load_project", root, true,
                Dialog.suffixFilter(suffixes), file -> {
                    if (file != null && file.isFile()) {
                        openFunctionFile(file);
                    }
                }).show(getModularUI());
    }

    /**
     * 上传当前运行时文件到服务端。
     */
    public final void uploadToServer() {
        EditorUploadActions.uploadToServer(this, createServerUploadAction());
    }

    private void openFunctionFile(File file) {
        var fileName = file.getName();
        projectTypes.stream()
                .filter(type -> fileName.endsWith(type.getSuffix()))
                .findFirst()
                .ifPresent(type -> loadProjectFileWithMissingItemWarning(type, file));
    }
}
