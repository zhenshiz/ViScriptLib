package com.viscript_lib.gui.editor;

import com.lowdragmc.lowdraglib2.gui.texture.Icons;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Dialog;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextField;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TreeList;
import com.lowdragmc.lowdraglib2.gui.util.FileNode;
import dev.vfyjxf.taffy.style.FlexDirection;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.List;
import java.util.function.Predicate;

/**
 * 本地文件弹窗工具。
 *
 * <p>LDLib2 默认保存弹窗在没有选中目录时不会触发确认回调，因此这里提供一个
 * 保存专用版本：没有选中节点时默认使用传入根目录，让调用方可以统一校验空文件名。
 */
final class EditorLocalFileDialogs {
    private EditorLocalFileDialogs() {
    }

    static Dialog showSaveFileDialog(String title, File dir, @Nullable File defaultValue,
                                     @Nullable Predicate<FileNode> valid, SaveFileHandler handler) {
        var dialog = new Dialog();
        var textField = new TextField();
        var treeList = new TreeList<FileNode>();
        if (!dir.isDirectory() && !dir.mkdirs()) {
            return dialog;
        }
        var root = new FileNode(dir).setValid(valid);

        dialog.overlay.layout(layout -> layout.width(200));
        dialog.setTitle(title);
        dialog.addContent(new UIElement().layout(layout -> {
            layout.widthPercent(100);
            layout.flexDirection(FlexDirection.ROW);
            layout.gapAll(2);
        }).addChildren(textField.layout(layout -> layout.flex(1)), new Button().setOnClick(e -> {
            Util.getPlatform().openFile(dir.isDirectory() ? dir : dir.getParentFile());
        }).noText().layout(layout -> {
            layout.width(14);
            layout.height(14);
            layout.paddingAll(3);
        }).addChild(new UIElement()
                .layout(layout -> layout.widthPercent(100))
                .style(style -> style.backgroundTexture(Icons.FOLDER)))));
        dialog.addContent(new ScrollerView().addScrollViewChild(treeList.setOnSelectedChanged(selected -> {
                    if (selected.isEmpty()) return;
                    var first = selected.stream().findFirst().get();
                    if (first.getKey().isFile()) {
                        textField.setText(first.getKey().getName(), false);
                    } else {
                        textField.setText("", false);
                    }
                }).setNodeUISupplier(TreeList.iconTextTemplate(
                        node -> node.getKey().isDirectory() ?
                                Icons.FOLDER :
                                Icons.getIcon(node.getKey().getName()
                                        .substring(node.getKey().getName().lastIndexOf('.') + 1)),
                        node -> Component.translatable(node.getKey().getName())))
                        .setRoot(root)
                ).layout(layout -> {
                    layout.widthPercent(100);
                    layout.height(180);
                })
        );
        applyDefault(treeList, textField, root, defaultValue);
        dialog.addButton(new Button()
                .setOnClick(e -> {
                    var targetDir = dir;
                    var nodes = treeList.getSelected();
                    if (!nodes.isEmpty()) {
                        var selectedFile = nodes.stream().findFirst().get().getKey();
                        targetDir = selectedFile.isFile() ? selectedFile.getParentFile() : selectedFile;
                    }
                    if (targetDir == null) {
                        targetDir = dir;
                    }

                    if (handler.confirm(new File(targetDir, textField.getText()))) {
                        dialog.close();
                    }
                })
                .setText("ldlib.gui.tips.confirm")
                .addClass("__confirm-button__"));
        dialog.addButton(new Button()
                .setOnClick(e -> dialog.close())
                .setText("ldlib.gui.tips.cancel")
                .addClass("__cancel-button__"));
        return dialog;
    }

    private static void applyDefault(TreeList<FileNode> treeList, TextField textField, FileNode root,
                                     @Nullable File defaultValue) {
        if (defaultValue == null) return;

        var selectedFile = defaultValue.isDirectory() ? defaultValue : defaultValue.getParentFile();
        var selectedNode = findVisibleFileNode(root, selectedFile);
        if (selectedNode != null) {
            treeList.expandNodeAlongPath(selectedNode);
            treeList.setSelected(List.of(selectedNode), false);
        }
        textField.setText(defaultValue.isDirectory() ? "" : defaultValue.getName(), false);
    }

    @Nullable
    private static FileNode findVisibleFileNode(FileNode root, @Nullable File file) {
        if (file == null) return null;
        var rootFile = normalizeFile(root.getKey());
        var targetFile = normalizeFile(file);
        var path = new ArrayDeque<File>();
        var currentFile = targetFile;
        while (currentFile != null) {
            path.addFirst(currentFile);
            if (rootFile.equals(currentFile)) {
                break;
            }
            currentFile = currentFile.getParentFile();
        }
        if (path.isEmpty() || !rootFile.equals(path.peekFirst())) {
            return null;
        }

        var currentNode = root;
        path.removeFirst();
        while (!path.isEmpty()) {
            var nextFile = path.removeFirst();
            FileNode nextNode = null;
            for (var child : currentNode.getChildren()) {
                if (normalizeFile(child.getKey()).equals(nextFile)) {
                    nextNode = child;
                    break;
                }
            }
            if (nextNode == null) {
                return null;
            }
            currentNode = nextNode;
        }
        return currentNode;
    }

    private static File normalizeFile(File file) {
        try {
            return file.getCanonicalFile();
        } catch (IOException ignored) {
            return file.getAbsoluteFile();
        }
    }

    @FunctionalInterface
    interface SaveFileHandler {
        boolean confirm(File file);
    }
}
