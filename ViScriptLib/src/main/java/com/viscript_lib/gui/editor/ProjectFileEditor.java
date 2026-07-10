package com.viscript_lib.gui.editor;

import com.lowdragmc.lowdraglib2.Platform;
import com.lowdragmc.lowdraglib2.editor.project.ProjectType;
import com.lowdragmc.lowdraglib2.gui.texture.Icons;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Dialog;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import java.io.File;
import java.nio.file.Files;

/**
 * 工程文件和运行时文件分离的编辑器基类。
 *
 * <p>这种编辑器使用 LDLib2 默认的工程文件保存流程，并额外提供本地导出运行时文件、
 * 上传工程文件、上传运行时文件和同时上传两种文件的菜单动作。
 */
public abstract class ProjectFileEditor extends ViScriptEditor {
    @Override
    protected void initMenus() {
        super.initMenus();
        EditorFileMenuHelper.replaceOpen(fileMenu, this::openProjectFile);
        fileMenu.registerMenuCreator((tab, menu) -> {
            if (currentContext() != null) {
                menu.leaf(Icons.EXPORT, "viscript_lib.editor.menu.export_runtime_file", this::exportRuntimeFile);
            }
            addExportLeaf(menu);
        });
        var uploadMenu = new UploadMenu(this);
        menuContainer.addChild(uploadMenu.createMenuTab());
    }

    /**
     * 创建上传工程文件的动作。
     *
     * @return 当前项目可上传时返回上传动作，否则返回 <code>null</code>
     */
    @Nullable
    protected EditorUploadAction createUploadProjectAction() {
        var context = currentContext();
        if (context == null) return null;

        var format = context.format();
        return new ProjectUploadAction(
                "viscript_lib.editor.menu.upload_project_file",
                "viscript_lib.editor.dialog.upload_project_file",
                defaultBaseName(format),
                format.projectSuffix(),
                fileName -> EditorFileNames.normalizeFileName(fileName, format.projectSuffix()),
                fileName -> EditorServerUploads.uploadProjectToServer(format, fileName, serializeProjectFile(context))
        );
    }

    /**
     * 创建上传运行时文件的动作。
     *
     * @return 当前项目可上传时返回上传动作，否则返回 <code>null</code>
     */
    @Nullable
    protected EditorUploadAction createUploadRuntimeAction() {
        var context = currentContext();
        if (context == null) return null;

        var format = context.format();
        return new ProjectUploadAction(
                "viscript_lib.editor.menu.upload_runtime_file",
                "viscript_lib.editor.dialog.upload_runtime_file",
                defaultBaseName(format),
                format.runtimeSuffix(),
                fileName -> EditorFileNames.normalizeFileName(fileName, format.runtimeSuffix()),
                fileName -> EditorServerUploads.uploadToServer(format, fileName, serializeRuntimeFile(context))
        );
    }

    /**
     * 创建同时上传工程文件和运行时文件的动作。
     *
     * @return 当前项目可上传时返回上传动作，否则返回 <code>null</code>
     */
    @Nullable
    protected EditorUploadAction createUploadProjectAndRuntimeAction() {
        var context = currentContext();
        if (context == null) return null;

        var format = context.format();
        return new ProjectUploadAction(
                "viscript_lib.editor.menu.upload_project_and_runtime_file",
                "viscript_lib.editor.dialog.upload_project_and_runtime_file",
                defaultBaseName(format),
                "",
                fileName -> EditorFileNames.normalizeBaseName(fileName, format.projectSuffix(), format.runtimeSuffix()),
                fileName -> {
                    EditorServerUploads.uploadProjectToServer(format, fileName, serializeProjectFile(context));
                    EditorServerUploads.uploadToServer(format, fileName, serializeRuntimeFile(context));
                }
        );
    }

    /**
     * 从工程文件目录打开项目。
     */
    public final void openProjectFile() {
        if (projectTypes.isEmpty()) return;
        var suffixes = projectTypes.stream().map(ProjectType::getSuffix).toArray(String[]::new);
        var root = projectTypes.get(0).getRootSavePath(null, null);
        Dialog.showFileDialog("ldlib.gui.editor.tips.load_project", root, true,
                Dialog.suffixFilter(suffixes), file -> {
                    if (file != null && file.isFile()) {
                        openProjectFile(file);
                    }
                }).show(getModularUI());
    }

    /**
     * 导出当前项目生成的运行时文件到本地。
     */
    public final void exportRuntimeFile() {
        var context = currentContext();
        if (context == null) return;

        var format = context.format();
        EditorLocalFileDialogs.showSaveFileDialog("viscript_lib.editor.dialog.export_runtime_file", format.functionDirectory(),
                defaultRuntimeFile(format),
                Dialog.suffixFilter(format.runtimeSuffix()), file -> {
                    if (validateLocalSaveFile(file, format.runtimeSuffix())) {
                        exportRuntimeFile(context, file);
                        return true;
                    }
                    return false;
                }).show(getModularUI());
    }

    /**
     * 上传当前工程文件到服务端。
     */
    public final void uploadProjectToServer() {
        EditorUploadActions.uploadToServer(this, createUploadProjectAction());
    }

    /**
     * 上传当前运行时文件到服务端。
     */
    public final void uploadRuntimeToServer() {
        EditorUploadActions.uploadToServer(this, createUploadRuntimeAction());
    }

    /**
     * 使用同一个基础文件名上传当前工程文件和运行时文件到服务端。
     */
    public final void uploadProjectAndRuntimeToServer() {
        EditorUploadActions.uploadToServer(this, createUploadProjectAndRuntimeAction());
    }

    private void openProjectFile(File file) {
        var fileName = file.getName();
        projectTypes.stream()
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

    private void exportRuntimeFile(EditorFileContext context, File file) {
        var suffix = context.format().runtimeSuffix();
        if (!file.getName().endsWith(suffix)) {
            file = new File(file.getParentFile(), file.getName() + suffix);
        }

        try {
            Files.createDirectories(file.getParentFile().toPath());
            var tag = serializeRuntimeFile(context);
            if (context.format().compressed()) {
                NbtIo.writeCompressed(tag, file);
            } else {
                NbtIo.write(tag, file);
            }
            Dialog.showNotification("viscript_lib.editor.runtime_file_export_success", 2)
                    .show(getModularUI());
        } catch (Exception e) {
            Dialog.showNotification("editor.error", "viscript_lib.editor.runtime_file_export_failed", null)
                    .show(getModularUI());
        }
    }

    private CompoundTag serializeProjectFile(EditorFileContext context) {
        return context.project().serializeNBT(Platform.getFrozenRegistry());
    }

    private CompoundTag serializeRuntimeFile(EditorFileContext context) {
        return context.project().serializeRuntimeFile(Platform.getFrozenRegistry());
    }

    @Nullable
    private EditorFileContext currentContext() {
        if (getCurrentProject() instanceof IRuntimeFileProject project
                && project.getProjectType() instanceof ProjectFileProjectType projectType) {
            return new EditorFileContext(project, projectType.getFormat());
        }
        return null;
    }

    private File defaultRuntimeFile(EditorFileFormat format) {
        return new File(format.functionDirectory(), defaultBaseName(format) + format.runtimeSuffix());
    }

    private String defaultBaseName(EditorFileFormat format) {
        var currentFile = getCurrentProjectFile();
        if (currentFile != null) {
            return EditorFileNames.normalizeBaseName(currentFile.getName(), format.projectSuffix(), format.runtimeSuffix());
        }
        return "test";
    }

    private record EditorFileContext(IRuntimeFileProject project, EditorFileFormat format) {
    }

    @FunctionalInterface
    public interface FileNameNormalizer {
        String normalize(String fileName);
    }

    @FunctionalInterface
    public interface UploadHandler {
        void upload(String fileName) throws Exception;
    }

    public record ProjectUploadAction(String displayKey, String dialogTitleKey, String defaultFileName,
                                       String suffix, FileNameNormalizer normalizer,
                                       UploadHandler uploadHandler) implements EditorUploadAction {
        @Override
        public Component getDisplayName() {
            return Component.translatable(displayKey);
        }

        @Override
        public String getDialogTitleKey() {
            return dialogTitleKey;
        }

        @Override
        public String getDefaultFileName() {
            return defaultFileName;
        }

        @Override
        public String getSuffix() {
            return suffix;
        }

        @Override
        public String normalizeFileName(String fileName) {
            return normalizer.normalize(fileName);
        }

        @Override
        public void uploadToServer(String fileName) throws Exception {
            uploadHandler.upload(fileName);
        }
    }
}
