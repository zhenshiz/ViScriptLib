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
 * <p>这个窗口主要补充两个能力：把缩放按钮接管为“缩到指定屏幕区域”，以及按需移除
 * LDLib2 默认的最大化/还原按钮。
 */
public class ViScriptEditorWindow extends EditorWindow {
    private static final String CUSTOM_SCALE_BUTTON_CLASS = "__viscript_editor-window_scale-button__";

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
     * 设置并接管缩放按钮。按钮按下后，窗口会缩到给定像素区域。
     */
    public ViScriptEditorWindow setMinimizedBounds(float left, float top, float width, float height) {
        return setMinimizedBounds(MinimizedBounds.pixels(left, top, width, height));
    }

    /**
     * 设置并接管缩放按钮。参数是屏幕百分比，<code>70</code> 表示屏幕宽/高的 70%。
     */
    public ViScriptEditorWindow setMinimizedBoundsPercent(float leftPercent, float topPercent,
                                                          float widthPercent, float heightPercent) {
        return setMinimizedBounds(MinimizedBounds.percent(leftPercent, topPercent, widthPercent, heightPercent));
    }

    /**
     * 设置并接管缩放按钮。可以用 {@link MinimizedBounds#of(BoundsValue, BoundsValue, BoundsValue, BoundsValue)}
     * 混合像素和百分比。
     */
    public ViScriptEditorWindow setMinimizedBounds(MinimizedBounds bounds) {
        this.minimizedBounds = bounds;
        applyWindowButtonPolicy();
        return this;
    }

    /**
     * 取消缩放按钮的指定区域行为，恢复为普通最大化/还原。
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
        var buttonIndex = removeCustomScaleButton(editor);
        var defaultScaleIndex = findDefaultScaleButtonIndex(editor);
        if (defaultScaleIndex >= 0) {
            var button = editor.buttonContainer.getChildren().get(defaultScaleIndex);
            editor.buttonContainer.removeChild(button);
            if (buttonIndex < 0) {
                buttonIndex = defaultScaleIndex;
            }
        }
        if (!defaultScaleButtonVisible) {
            return;
        }

        if (buttonIndex < 0) {
            buttonIndex = findScaleButtonInsertionIndex(editor);
        }
        editor.buttonContainer.addChildAt(createScaleButton(), Math.min(buttonIndex, editor.buttonContainer.getChildren().size()));
    }

    private Button createScaleButton() {
        var button = new Button();
        button.noText()
                .addPreIcon(DynamicTexture.of(() -> isMaximized() ? Icons.WINDOW_RESTORE : Icons.WINDOW_MAXIMIZE))
                .setOnClick(e -> handleScaleButtonClick());
        button.addClasses("__white_icon__", CUSTOM_SCALE_BUTTON_CLASS);
        button.layout(layout -> layout.height(12));
        return button;
    }

    private void handleScaleButtonClick() {
        if (isMaximized()) {
            if (minimizedBounds != null) {
                minimizeToConfiguredBounds();
            } else {
                retoreWindow();
            }
            return;
        }
        maximizeWindow();
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

    private int removeCustomScaleButton(Editor editor) {
        var index = -1;
        for (var child : new ArrayList<>(editor.buttonContainer.getChildren())) {
            if (child.hasClass(CUSTOM_SCALE_BUTTON_CLASS)) {
                var childIndex = editor.buttonContainer.getChildren().indexOf(child);
                if (index < 0) {
                    index = childIndex;
                }
                editor.buttonContainer.removeChild(child);
            }
        }
        return index;
    }

    private int findDefaultScaleButtonIndex(Editor editor) {
        var closeIndex = editor.buttonContainer.getChildren().indexOf(editor.closeButton);
        if (closeIndex < 0) {
            return -1;
        }
        if (windowID != null) {
            return closeIndex >= 2 ? closeIndex - 1 : -1;
        }
        return closeIndex >= 1 ? closeIndex - 1 : -1;
    }

    private int findScaleButtonInsertionIndex(Editor editor) {
        var closeIndex = editor.buttonContainer.getChildren().indexOf(editor.closeButton);
        if (closeIndex < 0) {
            return editor.buttonContainer.getChildren().size();
        }
        if (windowID != null && closeIndex >= 1) {
            return closeIndex;
        }
        return Math.max(0, closeIndex);
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
