package com.viscript_lib.gui.editor;

import com.lowdragmc.lowdraglib2.editor.ui.menu.FileMenu;
import com.lowdragmc.lowdraglib2.gui.texture.Icons;
import com.lowdragmc.lowdraglib2.gui.util.ITreeNode;
import com.lowdragmc.lowdraglib2.gui.util.TreeBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Tuple;

import java.util.List;

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
            menu.remove(OPEN_MENU_KEY);
            menu.leaf(Icons.OPEN_FILE, OPEN_MENU_KEY, openAction);
            moveOpenMenuToDefaultPosition(menu);
        });
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void moveOpenMenuToDefaultPosition(TreeBuilder.Menu menu) {
        var openName = Component.translatable(OPEN_MENU_KEY);
        List children = (List) menu.peek().getChildren();
        for (var index = 0; index < children.size(); index++) {
            var child = children.get(index);
            if (child instanceof ITreeNode<?, ?> node
                    && node.getKey() instanceof Tuple<?, ?> key
                    && openName.equals(key.getB())) {
                children.remove(index);
                // LDLib2 的扩展点只能追加节点，这里把替换后的 Open 放回 New 之后。
                children.add(Math.min(1, children.size()), child);
                return;
            }
        }
    }
}
