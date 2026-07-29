package com.viscript_lib.gui.editor;

import com.lowdragmc.lowdraglib2.LDLib2;
import com.lowdragmc.lowdraglib2.Platform;
import com.lowdragmc.lowdraglib2.editor.project.IProject;
import com.lowdragmc.lowdraglib2.editor.project.ProjectType;
import com.lowdragmc.lowdraglib2.editor.ui.Editor;
import com.lowdragmc.lowdraglib2.editor.ui.EditorLayout;
import com.lowdragmc.lowdraglib2.editor.ui.SplittableWindow;
import com.lowdragmc.lowdraglib2.editor.ui.View;
import com.lowdragmc.lowdraglib2.gui.ui.data.TextWrap;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Dialog;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.util.TreeBuilder;
import com.viscript_lib.util.NbtHelper;
import com.viscript_lib.util.item.MissingItemStackRecovery;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * ViScript Lib 编辑器公共基类。
 *
 * <p>这个类在 LDLib2 {@link Editor} 的基础上封装了常用窗口删除方法。实际编辑器
 * 如果不需要某块默认区域，可以在构造器里直接调用对应方法，例如
 * <code>removeBottomWindow()</code> 删除资源 View 所在的底部窗口。
 */
public abstract class ViScriptEditor extends Editor {
    final List<ProjectType> projectTypes = new ArrayList<>();
    private final Set<String> removedWindowAnchors = new HashSet<>();

    /**
     * 注册工程文件项目类型。
     *
     * @param projectType 项目类型
     */
    protected final void registerProjectType(ProjectType projectType) {
        fileMenu.addProjectProvider(projectType);
        projectTypes.add(projectType);
    }

    /**
     * 返回此编辑器支持的工程文件类型。
     *
     * @return 不可修改的项目类型列表
     */
    protected final List<ProjectType> getProjectTypes() {
        return Collections.unmodifiableList(projectTypes);
    }

    /**
     * 从文件反序列化项目，并在发现缺失物品时请求玩家确认恢复。
     *
     * <p>项目会先在回调作用域中载入到内存。没有缺失物品时立即打开；存在缺失物品时，
     * 玩家确认后会先复制原文件作为备份，再打开包含替代物品栈的项目。取消操作不会载入项目，
     * 也不会修改或备份文件。
     *
     * @param projectType 与文件后缀匹配的项目类型
     * @param file 需要打开的项目文件
     */
    protected final void loadProjectFileWithMissingItemRecovery(ProjectType projectType, File file) {
        var missingItemIds = new LinkedHashSet<ResourceLocation>();
        try {
            var project = MissingItemStackRecovery.withHandler(context -> {
                missingItemIds.add(context.itemId());
                return onMissingItemStack(context);
            }, () -> projectType.loadProjectFromFile(file));

            if (missingItemIds.isEmpty()) {
                loadProject(project, file);
            } else {
                showMissingItemsDialog(project, file, missingItemIds);
            }
        } catch (Exception e) {
            Dialog.showNotification("editor.error", "editor.loading_failed", null).show(getModularUI());
        }
    }

    /**
     * 处理项目反序列化期间发现的缺失物品。
     *
     * <p>默认实现返回名称为原物品 ID 的屏障占位符。编辑器子类可以重写本方法以适配自己的
     * 数据模型；返回 {@code null} 会拒绝恢复，并让本次项目载入按反序列化失败处理。
     * 本方法执行时只应转换数据，不应直接显示弹窗或修改原文件。
     *
     * @param context 缺失物品 ID 和原始物品栈 NBT
     * @return 用于继续反序列化的替代物品栈；返回 {@code null} 表示拒绝恢复
     */
    @Nullable
    protected ItemStack onMissingItemStack(MissingItemStackRecovery.MissingItemContext context) {
        return MissingItemStackRecovery.createBarrierPlaceholder(context);
    }

    private void showMissingItemsDialog(IProject project, File file, Set<ResourceLocation> missingItemIds) {
        var backupFile = findAvailableBackupFile(file);
        var itemList = String.join("\n", missingItemIds.stream().map(ResourceLocation::toString).toList());
        var dialog = new Dialog().setTitle("viscript_lib.editor.missing_items.title");
        dialog.overlay.layout(layout -> layout.width(230));
        dialog.addContent(new Label()
                .setText(Component.translatable(
                        "viscript_lib.editor.missing_items.warning",
                        itemList,
                        backupFile.getAbsolutePath()
                ))
                .textStyle(style -> style
                        .textWrap(TextWrap.WRAP)
                        .adaptiveWidth(false)
                        .adaptiveHeight(true))
                .layout(layout -> layout.widthPercent(100).minWidth(0)));
        dialog.addButton(new Button()
                .setOnClick(event -> {
                    try {
                        Files.copy(file.toPath(), backupFile.toPath());
                        dialog.close();
                        loadProject(project, file);
                    } catch (Exception e) {
                        Dialog.showNotification(
                                "editor.error",
                                "viscript_lib.editor.missing_items.backup_failed",
                                null
                        ).show(getModularUI());
                    }
                })
                .setText("viscript_lib.editor.missing_items.force_open")
                .addClass("__confirm-button__"));
        dialog.addButton(new Button()
                .setOnClick(event -> dialog.close())
                .setText("ldlib.gui.tips.cancel")
                .addClass("__cancel-button__"));
        dialog.show(getModularUI());
    }

    private static File findAvailableBackupFile(File file) {
        var absoluteFile = file.getAbsoluteFile();
        var parent = absoluteFile.getParentFile();
        var fileName = absoluteFile.getName();
        var extensionIndex = fileName.lastIndexOf('.');
        var baseName = extensionIndex < 0 ? fileName : fileName.substring(0, extensionIndex);
        var extension = extensionIndex < 0 ? "" : fileName.substring(extensionIndex);
        var backup = new File(parent, baseName + ".missing-items-backup" + extension);
        for (int index = 1; backup.exists(); index++) {
            backup = new File(parent, baseName + ".missing-items-backup." + index + extension);
        }
        return backup;
    }

    @Override
    public void applyLayout(EditorLayout layout) {
        if (!removedWindowAnchors.isEmpty()) {
            var prunedConfig = pruneRemovedAnchors(layout.layoutConfig());
            if (prunedConfig == null) {
                return;
            }
            layout = new EditorLayout(prunedConfig, layout.slots());
        }
        super.applyLayout(layout);
    }

    @Nullable
    private SplittableWindow.LayoutConfig pruneRemovedAnchors(@Nullable SplittableWindow.LayoutConfig config) {
        if (config == null || removedWindowAnchors.contains(config.anchorId())) {
            return null;
        }

        var first = pruneRemovedAnchors(config.first());
        var second = pruneRemovedAnchors(config.second());
        if (config.first() != null && first == null) {
            return second;
        }
        if (config.second() != null && second == null) {
            return first;
        }
        if (first != config.first() || second != config.second()) {
            return new SplittableWindow.LayoutConfig(config.anchorId(), config.vertical(), config.percentage(), first, second);
        }
        return config;
    }

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
        var project = getProjectTypes().getFirst().newEmptyProject();
        try {
            var tag = NbtHelper.tagFromString(Minecraft.getInstance().keyboardHandler.getClipboard());
            project.deserializeNBT(Platform.getFrozenRegistry(), tag);
            if (getCurrentProject() != null) closeCurrentProject(false, null);
            loadNewProject(project, null);
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
     * <p>这里会先移除窗口里的所有 View 和布局回退记录，再从 LDLib2 的拆分树里
     * 摘掉窗口。只有没有父窗口的根窗口本身才会回退为隐藏窗口。
     *
     * @param window 要删除的窗口
     */
    protected final void removeEditorWindow(@Nullable SplittableWindow window) {
        if (window == null || window == rootWindow) return;

        var anchorId = window.getAnchorId();
        if (anchorId != null) {
            removedWindowAnchors.add(anchorId);
            window.setAnchorId(null);
            if (window == leftWindow) {
                leftWindow = rootWindow;
            } else if (window == rightWindow) {
                rightWindow = rootWindow;
            } else if (window == centerWindow) {
                centerWindow = rootWindow;
            } else if (window == bottomWindow) {
                bottomWindow = rootWindow;
            }
        }

        for (View view : new ArrayList<>(window.getAllViews())) {
            view.removeSelf();
            viewFallbacks.remove(view);
        }

        var parent = window.getParentWindow();
        if (parent != null) {
            parent.removeSplitWindow(window);
        } else {
            window.setDisplay(false);
        }
    }

}
