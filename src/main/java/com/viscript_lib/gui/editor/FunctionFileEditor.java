package com.viscript_lib.gui.editor;

import com.lowdragmc.lowdraglib2.editor.project.ProjectType;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Dialog;

import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 运行时文件和功能文件一体的编辑器基类。
 *
 * <p>这种编辑器没有单独工程文件，LDLib2 的保存、另存为和打开都直接作用于运行时文件。
 */
public abstract class FunctionFileEditor extends ViScriptEditor {
    private final List<ProjectType> functionFileTypes = new ArrayList<>();

    @Override
    protected void initMenus() {
        super.initMenus();
        EditorFileMenuHelper.replaceOpen(fileMenu, this::openFunctionFile);
        fileMenu.registerMenuCreator((tab, menu) -> addExportLeaf(menu));
        var uploadMenu = new UploadMenu(this);
        menuContainer.addChild(uploadMenu.createMenuTab());
    }

    /**
     * 注册运行时文件项目类型。
     *
     * @param projectType 项目类型
     */
    protected final void registerFunctionFileType(ProjectType projectType) {
        fileMenu.addProjectProvider(projectType);
        functionFileTypes.add(projectType);
    }

    /**
     * 返回此编辑器支持的运行时文件类型。
     *
     * @return 不可修改的项目类型列表
     */
    protected final List<ProjectType> getFunctionFileTypes() {
        return Collections.unmodifiableList(functionFileTypes);
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
        if (functionFileTypes.isEmpty()) return;
        var suffixes = functionFileTypes.stream().map(ProjectType::getSuffix).toArray(String[]::new);
        var root = functionFileTypes.getFirst().getRootSavePath(null, null);
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
        functionFileTypes.stream()
                .filter(type -> fileName.endsWith(type.getSuffix()))
                .findFirst()
                .ifPresent(type -> {
                    try {
                        var project = type.loadProjectFromFile(file);
                        loadProject(project, file);
                    } catch (Exception e) {
                        Dialog.showNotification("editor.error", "editor.loading_failed", null).show(getModularUI());
                    }
                });
    }
}
