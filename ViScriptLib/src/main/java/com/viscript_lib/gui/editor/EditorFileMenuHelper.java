package com.viscript_lib.gui.editor;

import com.lowdragmc.lowdraglib2.editor.ui.menu.FileMenu;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.util.TreeNode;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Tuple;

/**
 * 复用 LDLib2 File 菜单的顺序，并替换默认打开目录。
 *
 * <p>LDLib2 的菜单扩展点默认只能追加节点；这个工具负责在替换 Open
 * 动作后把它移回 New 和 Save 之间，避免破坏原本菜单顺序。
 */
final class EditorFileMenuHelper {
    static final String OPEN_MENU_KEY = "ldlib.gui.editor.menu.open";

    /**
     * 替换 File 菜单的 Open 行为。
     *
     * @param fileMenu LDLib2 File 菜单
     * @param openAction 自定义打开动作
     */
    static void replaceOpen(FileMenu fileMenu, Runnable openAction) {
        fileMenu.registerMenuCreator((tab, menu) -> {
            Tuple<IGuiTexture, Component> key = null;
            for (var child : menu.peek().getChildren()) {
                var tuple = child.getKey();
                if (tuple.getB().equals(Component.translatable(OPEN_MENU_KEY))) {
                    key = tuple;
                    break;
                }
            }
            if (key != null && menu.peek() instanceof TreeNode<Tuple<IGuiTexture, Component>, Runnable> node)
                node.addContent(key, openAction);
        });
    }
}
