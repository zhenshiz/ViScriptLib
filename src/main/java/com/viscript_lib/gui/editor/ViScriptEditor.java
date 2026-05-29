package com.viscript_lib.gui.editor;

import com.lowdragmc.lowdraglib2.editor.ui.Editor;
import com.lowdragmc.lowdraglib2.editor.ui.SplittableWindow;
import com.lowdragmc.lowdraglib2.editor.ui.View;

import javax.annotation.Nullable;
import java.util.ArrayList;

/**
 * ViScript Lib 编辑器公共基类。
 *
 * <p>这个类在 LDLib2 {@link Editor} 的基础上封装了常用窗口删除方法。实际编辑器
 * 如果不需要某块默认区域，可以在构造器里直接调用对应方法，例如
 * <code>removeBottomWindow()</code> 删除资源 View 所在的底部窗口。
 */
public abstract class ViScriptEditor extends Editor {
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
