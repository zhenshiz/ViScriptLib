package com.viscript_lib.gui.editor;

import com.lowdragmc.lowdraglib2.LDLib2;
import com.lowdragmc.lowdraglib2.Platform;
import com.lowdragmc.lowdraglib2.editor.project.IProject;
import com.lowdragmc.lowdraglib2.editor.ui.Editor;
import com.lowdragmc.lowdraglib2.editor.ui.SplittableWindow;
import com.lowdragmc.lowdraglib2.editor.ui.View;
import com.lowdragmc.lowdraglib2.gui.ui.data.TextWrap;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Dialog;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.util.TreeBuilder;
import com.viscript_lib.util.NbtHelper;
import net.minecraft.client.Minecraft;

import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;

/**
 * ViScript Lib 编辑器公共基类。
 *
 * <p>这个类在 LDLib2 {@link Editor} 的基础上封装了常用窗口删除方法。实际编辑器
 * 如果不需要某块默认区域，可以在构造器里直接调用对应方法，例如
 * <code>removeBottomWindow()</code> 删除资源 View 所在的底部窗口。
 */
public abstract class ViScriptEditor extends Editor {
    @Override
    public void saveAsProject(@Nullable Runnable onFinish) {
        var project = getCurrentProject();
        if (project == null) return;

        var projectType = project.getProjectType();
        var projectRoot = LDLib2.getAssetsDir();
        var defaultSaveFile = currentProjectFile == null
                ? projectType.getDefaultSaveFile(project, projectRoot)
                : currentProjectFile;
        saveAsProject(defaultSaveFile, onFinish);
    }

    @Override
    public void saveAsProject(@Nullable File defaultSaveFile, @Nullable Runnable onFinish) {
        var project = getCurrentProject();
        if (project == null) return;

        var projectType = project.getProjectType();
        var projectRoot = LDLib2.getAssetsDir();
        var suffix = project.getSuffix();
        EditorLocalFileDialogs.showSaveFileDialog("ldlib.gui.editor.tips.save_as",
                projectType.getRootSavePath(project, projectRoot),
                defaultSaveFile,
                Dialog.suffixFilter(suffix), file -> saveProjectAsLocalFile(project, suffix, file, onFinish)
        ).show(getModularUI());
    }

    private boolean saveProjectAsLocalFile(IProject project, String suffix, @Nullable File file,
                                           @Nullable Runnable onFinish) {
        if (!validateLocalSaveFile(file, suffix)) {
            return false;
        }

        if (!file.getName().endsWith(suffix)) {
            file = new File(file.getParentFile(), file.getName() + suffix);
        }
        try {
            project.getProjectType().saveProjectToFile(project, file);
            currentProjectFile = file;
        } catch (Exception ignored) {
        }
        if (onFinish != null) {
            onFinish.run();
        }
        return true;
    }

    /**
     * 校验本地保存目标是否有有效文件名。
     *
     * @param file 玩家确认的文件
     * @param suffix 当前文件类型后缀
     * @return 可以继续写文件时返回 <code>true</code>
     */
    protected final boolean validateLocalSaveFile(@Nullable File file, String suffix) {
        if (file == null) {
            return false;
        }
        if (file.isDirectory() || EditorFileNames.isBlankFileName(file.getName(), suffix)) {
            Dialog.showNotification("editor.error", "viscript_lib.editor.file_name_empty", null)
                    .show(getModularUI());
            return false;
        }
        return true;
    }

    protected void addExportLeaf(TreeBuilder.Menu menu) {
        menu.leaf("viscript_lib.editor.export", // 导出数据到剪贴板
                () -> Dialog.showNotification(exportToClipboard(), 3).show(getModularUI()));
        menu.leaf("viscript_lib.editor.import", () -> { // 从剪贴板导入数据
            var dialog = new Dialog()
                    .setTitle("viscript_lib.editor.import.title") // 导入数据须知
                    // 若操作成功，当前项目的数据会被覆盖，请确保当前项目的内容已经保存。若导入的数据有误，可能会导致游戏崩溃，你确定要继续吗？
                    .addContent(new Label().setText("viscript_lib.editor.import.tip")
                            .textStyle(style -> style.textWrap(TextWrap.WRAP).adaptiveHeight(true))
                            .layout(layout -> layout.width(150)));
            dialog.addButton(new Button()
                    .setOnClick(e -> {
                        Dialog.showNotification(loadFromClipboard(), 3).show(getModularUI());
                        dialog.close();
                    })
                    .setText("ldlib.gui.tips.confirm")
                    .addClass("__confirm-button__"));
            dialog.addButton(new Button()
                    .setOnClick(e -> dialog.close())
                    .setText("ldlib.gui.tips.cancel")
                    .addClass("__cancel-button__"));
            dialog.show(getModularUI());
        });
    }

    protected String exportToClipboard() {
        var project = getCurrentProject();
        if (project == null) return "viscript_lib.editor.no_project"; // 请先打开一个项目

        var tag = project.serializeNBT(Platform.getFrozenRegistry());
        Minecraft.getInstance().keyboardHandler.setClipboard(NbtHelper.tagToString(tag));
        return "viscript_lib.editor.exported"; // 已将当前项目数据复制到剪贴板
    }

    protected String loadFromClipboard() {
        var project = getCurrentProject();
        if (project == null) return "viscript_lib.editor.no_project"; // 请先打开一个项目

        try {
            var tag = NbtHelper.tagFromString(Minecraft.getInstance().keyboardHandler.getClipboard());
            project.deserializeNBT(Platform.getFrozenRegistry(), tag);
            closeCurrentProject(false, () -> loadNewProject(project, null));
            return "viscript_lib.editor.import.success"; // 已从剪贴板导入数据并覆盖当前项目
        } catch (Exception e) {
            return "viscript_lib.editor.import.fail"; // 剪贴板内容解析出错！
        }
    }

    /**
     * 删除左侧窗口。
     */
    protected final void removeLeftWindow() {
        removeEditorWindow(leftWindow);
    }

    /**
     * 删除中间窗口。
     */
    protected final void removeCenterWindow() {
        removeEditorWindow(centerWindow);
    }

    /**
     * 删除底部窗口。
     *
     * <p>LDLib2 默认会把资源 View 放在这里，不需要资源 View 的编辑器通常调用这个方法。
     */
    protected final void removeBottomWindow() {
        removeEditorWindow(bottomWindow);
    }

    /**
     * 删除右侧窗口。
     *
     * <p>LDLib2 默认会把检查器和历史记录 View 放在这里。
     */
    protected final void removeRightWindow() {
        removeEditorWindow(rightWindow);
    }

    /**
     * 一次删除多个编辑器窗口。
     *
     * @param windows 要删除的窗口
     */
    protected final void removeEditorWindows(SplittableWindow... windows) {
        for (var window : windows) {
            removeEditorWindow(window);
        }
    }

    /**
     * 删除指定编辑器窗口。
     *
     * <p>这里会先移除窗口里的所有 View 和布局回退记录，再优先从 LDLib2 的拆分树里
     * 摘掉窗口。根节点直属窗口无法直接提升兄弟窗口时，会回退为隐藏窗口本身。
     *
     * @param window 要删除的窗口
     */
    protected final void removeEditorWindow(@Nullable SplittableWindow window) {
        if (window == null || window == rootWindow) return;

        for (View view : new ArrayList<>(window.getAllViews())) {
            view.removeSelf();
            viewFallbacks.remove(view);
        }

        var parent = window.getParentWindow();
        if (parent != null && parent.getParentWindow() != null) {
            parent.removeSplitWindow(window);
        } else {
            window.setDisplay(false);
        }
    }

}
