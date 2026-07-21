package com.viscript_lib.gui.editor;

import com.lowdragmc.lowdraglib2.editor.ui.Editor;
import com.lowdragmc.lowdraglib2.editor.ui.EditorWindow;
import com.lowdragmc.lowdraglib2.gui.texture.DynamicTexture;
import com.lowdragmc.lowdraglib2.gui.texture.Icons;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.viscript_lib.mixin.EditorWindowAccessor;
import dev.vfyjxf.taffy.style.TaffyPosition;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.function.Supplier;

/**
 * ViScript Lib 对 LDLib2 {@link EditorWindow} 的轻量扩展。
 *
 * <p>这个窗口主要补充两个能力：把最小化按钮改成“缩到指定屏幕区域”，以及按需移除
 * LDLib2 默认的最大化/还原按钮。
 */
public class ViScriptEditorWindow extends EditorWindow {
    private static final String CUSTOM_MINIMIZE_BUTTON_CLASS = "__viscript_editor-window_minimize-button__";

    @Nullable
    private MinimizedBounds minimizedBounds;
    private boolean minimizedToBounds;
    private boolean defaultScaleButtonVisible = true;
    private boolean constructed;

    public ViScriptEditorWindow(Supplier<Editor> editorCreator) {
        super(editorCreator);
        constructed = true;
        applyWindowButtonPolicy();
    }

    public ViScriptEditorWindow(@Nullable ResourceLocation windowID, Supplier<Editor> editorCreator) {
        super(windowID, editorCreator);
        constructed = true;
        applyWindowButtonPolicy();
    }

    /**
     * 设置并启用最小化按钮。按钮按下后，窗口会缩到给定像素区域。
     */
    public ViScriptEditorWindow setMinimizedBounds(float left, float top, float width, float height) {
        return setMinimizedBounds(MinimizedBounds.pixels(left, top, width, height));
    }

    /**
     * 设置并启用最小化按钮。参数是屏幕百分比，<code>70</code> 表示屏幕宽/高的 70%。
     */
    public ViScriptEditorWindow setMinimizedBoundsPercent(float leftPercent, float topPercent,
                                                          float widthPercent, float heightPercent) {
        return setMinimizedBounds(MinimizedBounds.percent(leftPercent, topPercent, widthPercent, heightPercent));
    }

    /**
     * 设置并启用最小化按钮。可以用 {@link MinimizedBounds#of(BoundsValue, BoundsValue, BoundsValue, BoundsValue)}
     * 混合像素和百分比。
     */
    public ViScriptEditorWindow setMinimizedBounds(MinimizedBounds bounds) {
        this.minimizedBounds = bounds;
        applyWindowButtonPolicy();
        return this;
    }

    /**
     * 关闭自定义最小化按钮。
     */
    public ViScriptEditorWindow clearMinimizedBounds() {
        this.minimizedBounds = null;
        this.minimizedToBounds = false;
        applyWindowButtonPolicy();
        return this;
    }

    /**
     * 删除 LDLib2 默认的最大化/还原按钮。
     */
    public ViScriptEditorWindow removeDefaultScaleButton() {
        return setDefaultScaleButtonVisible(false);
    }

    /**
     * 设置是否保留 LDLib2 默认的最大化/还原按钮。
     */
    public ViScriptEditorWindow setDefaultScaleButtonVisible(boolean visible) {
        this.defaultScaleButtonVisible = visible;
        applyWindowButtonPolicy();
        return this;
    }

    @Override
    public Editor createNewEditor(Supplier<Editor> editorCreator) {
        var editor = super.createNewEditor(editorCreator);
        if (constructed) {
            applyWindowButtonPolicy(editor);
        }
        return editor;
    }

    @Override
    public void maximizeWindow() {
        useCenteredRoot();
        super.maximizeWindow();
        minimizedToBounds = false;
    }

    @Override
    public void retoreWindow() {
        super.retoreWindow();
        minimizedToBounds = false;
    }

    private void applyWindowButtonPolicy() {
        for (var editor : getEditors().keySet()) {
            applyWindowButtonPolicy(editor);
        }
    }

    private void applyWindowButtonPolicy(Editor editor) {
        removeCustomMinimizeButton(editor);

        if (minimizedBounds != null && windowID != null) {
            removeFirstWindowButtonBeforeClose(editor);
        }
        if (!defaultScaleButtonVisible) {
            removeWindowButtonsBeforeClose(editor);
        }
        if (minimizedBounds != null) {
            editor.buttonContainer.addChildAt(createMinimizeButton(), 0);
        }
    }

    private Button createMinimizeButton() {
        var button = new Button();
        button.noText()
                .addPreIcon(DynamicTexture.of(() -> minimizedToBounds && !isMaximized()
                        ? Icons.WINDOW_RESTORE
                        : Icons.WINDOW_MINIMIZE))
                .setOnClick(e -> {
                    if (minimizedToBounds && !isMaximized()) {
                        maximizeWindow();
                    } else {
                        minimizeToConfiguredBounds();
                    }
                });
        button.addClasses("__white_icon__", CUSTOM_MINIMIZE_BUTTON_CLASS);
        button.layout(layout -> layout.height(12));
        return button;
    }

    private void minimizeToConfiguredBounds() {
        var bounds = minimizedBounds;
        if (bounds == null) return;

        var screenWidth = getCurrentScreenWidth();
        var screenHeight = getCurrentScreenHeight();
        var resolved = toCenteredRootLocalBounds(bounds.resolve(screenWidth, screenHeight), screenWidth, screenHeight);
        var accessor = (EditorWindowAccessor) this;
        accessor.viscript_lib$setWindowLeft(resolved.left());
        accessor.viscript_lib$setWindowTop(resolved.top());
        accessor.viscript_lib$setWindowWidth(resolved.width());
        accessor.viscript_lib$setWindowHeight(resolved.height());

        useCenteredRoot();
        if (isMaximized()) {
            super.retoreWindow();
        } else {
            applyResolvedBounds(resolved);
            reinitScreen();
        }
        minimizedToBounds = true;
    }

    private void applyResolvedBounds(ResolvedBounds bounds) {
        useCenteredRoot();
        layout(layout -> layout.width(1).height(1));
        window.layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .paddingAll(3)
                .left(bounds.left())
                .top(bounds.top())
                .width(Math.max(1, bounds.width()))
                .height(Math.max(1, bounds.height())));
    }

    private void useCenteredRoot() {
        layout(layout -> layout
                .positionType(TaffyPosition.RELATIVE)
                .leftAuto()
                .topAuto()
                .rightAuto()
                .bottomAuto());
    }

    private ResolvedBounds toCenteredRootLocalBounds(ResolvedBounds screenBounds, float screenWidth, float screenHeight) {
        return new ResolvedBounds(
                screenBounds.left() - centeredRootOrigin(screenWidth),
                screenBounds.top() - centeredRootOrigin(screenHeight),
                screenBounds.width(),
                screenBounds.height()
        );
    }

    private static float centeredRootOrigin(float screenSize) {
        return Math.round((screenSize - 1f) / 2f);
    }

    private int getCurrentScreenWidth() {
        var mui = getModularUI();
        if (mui != null && mui.getScreenWidth() > 0) {
            return mui.getScreenWidth();
        }
        return Minecraft.getInstance().getWindow().getGuiScaledWidth();
    }

    private int getCurrentScreenHeight() {
        var mui = getModularUI();
        if (mui != null && mui.getScreenHeight() > 0) {
            return mui.getScreenHeight();
        }
        return Minecraft.getInstance().getWindow().getGuiScaledHeight();
    }

    private void reinitScreen() {
        var mui = getModularUI();
        var minecraft = Minecraft.getInstance();
        if (mui != null && mui.getScreen() != null) {
            mui.getScreen().init(minecraft, minecraft.getWindow().getGuiScaledWidth(), minecraft.getWindow().getGuiScaledHeight());
        }
    }

    private void removeCustomMinimizeButton(Editor editor) {
        for (var child : new ArrayList<>(editor.buttonContainer.getChildren())) {
            if (child.hasClass(CUSTOM_MINIMIZE_BUTTON_CLASS)) {
                editor.buttonContainer.removeChild(child);
            }
        }
    }

    private void removeFirstWindowButtonBeforeClose(Editor editor) {
        for (var child : new ArrayList<>(editor.buttonContainer.getChildren())) {
            if (child == editor.closeButton) {
                return;
            }
            editor.buttonContainer.removeChild(child);
            return;
        }
    }

    private void removeWindowButtonsBeforeClose(Editor editor) {
        for (var child : new ArrayList<>(editor.buttonContainer.getChildren())) {
            if (child == editor.closeButton) {
                return;
            }
            editor.buttonContainer.removeChild(child);
        }
    }

    public record MinimizedBounds(BoundsValue left, BoundsValue top, BoundsValue width, BoundsValue height) {
        public static MinimizedBounds of(BoundsValue left, BoundsValue top, BoundsValue width, BoundsValue height) {
            return new MinimizedBounds(left, top, width, height);
        }

        public static MinimizedBounds pixels(float left, float top, float width, float height) {
            return of(BoundsValue.pixels(left), BoundsValue.pixels(top),
                    BoundsValue.pixels(width), BoundsValue.pixels(height));
        }

        public static MinimizedBounds percent(float leftPercent, float topPercent,
                                              float widthPercent, float heightPercent) {
            return of(BoundsValue.percent(leftPercent), BoundsValue.percent(topPercent),
                    BoundsValue.percent(widthPercent), BoundsValue.percent(heightPercent));
        }

        private ResolvedBounds resolve(float screenWidth, float screenHeight) {
            return new ResolvedBounds(
                    left.resolve(screenWidth),
                    top.resolve(screenHeight),
                    Math.max(1, width.resolve(screenWidth)),
                    Math.max(1, height.resolve(screenHeight))
            );
        }
    }

    public record BoundsValue(float value, boolean percent) {
        public static BoundsValue pixels(float value) {
            return new BoundsValue(value, false);
        }

        public static BoundsValue percent(float value) {
            return new BoundsValue(value, true);
        }

        private float resolve(float fullSize) {
            return percent ? fullSize * value / 100f : value;
        }
    }

    private record ResolvedBounds(float left, float top, float width, float height) {
    }
}
