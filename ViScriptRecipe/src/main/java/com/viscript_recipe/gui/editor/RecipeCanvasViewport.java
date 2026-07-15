package com.viscript_recipe.gui.editor;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.ScrollDisplay;
import com.lowdragmc.lowdraglib2.gui.ui.data.ScrollerMode;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import dev.vfyjxf.taffy.style.TaffyPosition;

/**
 * Provides a clipped, responsive scroll surface for a recipe canvas.
 *
 * <p>The surface measures visible descendant bounds after layout and expands its scrollable
 * extent when a fixed-size recipe diagram cannot fit. The canvas is scaled down only for compact
 * viewport sizes; its interactive coordinates continue to use the matching LDLib2 transform.
 */
final class RecipeCanvasViewport extends ScrollerView {
    private static final float RELAXED_MIN_WIDTH = 360;
    private static final float RELAXED_MIN_HEIGHT = 220;
    private static final float COMPACT_MIN_WIDTH = 260;
    private static final float COMPACT_MIN_HEIGHT = 170;
    private static final float COMPACT_SCALE = 0.84f;
    private static final float NARROW_SCALE = 0.75f;
    private static final float EPSILON = 0.5f;

    private final UIElement canvas;
    private final UIElement surface = new UIElement();
    private int pendingLayoutPasses = 3;
    private boolean resetToViewport = true;
    private boolean applyingLayout;
    private float logicalWidth;
    private float logicalHeight;
    private float contentScale = 1;
    private float observedWidth = -1;
    private float observedHeight = -1;
    private ScrollDisplay scrollDisplay = ScrollDisplay.AUTO;

    RecipeCanvasViewport(UIElement canvas) {
        this.canvas = canvas;
        layout(layout -> {
            layout.widthPercent(100);
            layout.heightPercent(100);
            layout.flex(1);
            layout.minWidth(0);
            layout.minHeight(0);
        });
        setOverflowVisible(false);
        scrollerStyle(style -> {
            style.mode(ScrollerMode.BOTH);
            style.horizontalScrollDisplay(ScrollDisplay.AUTO);
            style.verticalScrollDisplay(ScrollDisplay.AUTO);
            style.adaptiveWidth(false);
            style.adaptiveHeight(false);
        });
        viewPort.setOverflowVisible(false);
        viewPort.style(style -> style.backgroundTexture(IGuiTexture.EMPTY));
        viewPort.layout(layout -> {
            layout.paddingAll(0);
            layout.minWidth(0);
            layout.minHeight(0);
        });
        verticalContainer.layout(layout -> {
            layout.minWidth(0);
            layout.minHeight(0);
        });
        horizontalScroller.headButton.setDisplay(false);
        horizontalScroller.tailButton.setDisplay(false);
        verticalScroller.headButton.setDisplay(false);
        verticalScroller.tailButton.setDisplay(false);

        surface.layout(layout -> {
            layout.positionType(TaffyPosition.RELATIVE);
            layout.width(1);
            layout.height(1);
            layout.minWidth(0);
            layout.minHeight(0);
        });
        canvas.layout(layout -> {
            layout.positionType(TaffyPosition.ABSOLUTE);
            layout.left(0);
            layout.top(0);
            layout.width(1);
            layout.height(1);
            layout.minWidth(0);
            layout.minHeight(0);
        });
        surface.addChild(canvas);
        addScrollViewChild(surface);

        addEventListener(UIEvents.LAYOUT_CHANGED, event -> onViewportLayoutChanged());
        addEventListener(UIEvents.TICK, event -> applyPendingLayout());
    }

    /**
     * Recomputes the canvas extent after the selected recipe layout changes.
     *
     * <p>The next layout passes first return to the available viewport size, then expand only
     * when visible descendants still need additional room.
     */
    void requestReflow() {
        resetToViewport = true;
        pendingLayoutPasses = Math.max(pendingLayoutPasses, 3);
    }

    private void applyPendingLayout() {
        if (pendingLayoutPasses <= 0 || applyingLayout || !isDisplayed()) {
            return;
        }
        var availableWidth = viewPort.getContentWidth();
        var availableHeight = viewPort.getContentHeight();
        if (availableWidth <= 0 || availableHeight <= 0) {
            return;
        }

        pendingLayoutPasses--;
        var nextScale = scaleFor(availableWidth, availableHeight);
        var nextLogicalWidth = availableWidth / nextScale;
        var nextLogicalHeight = availableHeight / nextScale;
        CanvasBounds bounds = null;
        var contentOverflowsViewport = false;
        if (!resetToViewport) {
            bounds = measureCanvasBounds();
            contentOverflowsViewport = bounds.width() > nextLogicalWidth + EPSILON
                    || bounds.height() > nextLogicalHeight + EPSILON;
            nextLogicalWidth = Math.max(nextLogicalWidth, bounds.width());
            nextLogicalHeight = Math.max(nextLogicalHeight, bounds.height());
        }
        var mustReserveScrollbars = nextScale < 1
                || contentOverflowsViewport;
        updateScrollbarDisplay(mustReserveScrollbars);

        resetToViewport = false;
        if (nearlyEqual(nextScale, contentScale)
                && nearlyEqual(nextLogicalWidth, logicalWidth)
                && nearlyEqual(nextLogicalHeight, logicalHeight)) {
            return;
        }

        applyingLayout = true;
        try {
            contentScale = nextScale;
            logicalWidth = nextLogicalWidth;
            logicalHeight = nextLogicalHeight;
            canvas.layout(layout -> {
                layout.width(logicalWidth);
                layout.height(logicalHeight);
            });
            canvas.transform(transform -> transform
                    .setIdentity()
                    .pivot(0, 0)
                    .scale(contentScale));
            surface.layout(layout -> {
                layout.width(logicalWidth * contentScale);
                layout.height(logicalHeight * contentScale);
            });
            pendingLayoutPasses = Math.max(pendingLayoutPasses, 2);
        } finally {
            applyingLayout = false;
        }
    }

    private void onViewportLayoutChanged() {
        var width = getSizeWidth();
        var height = getSizeHeight();
        if (width <= 0 || height <= 0
                || nearlyEqual(width, observedWidth) && nearlyEqual(height, observedHeight)) {
            return;
        }
        observedWidth = width;
        observedHeight = height;
        requestReflow();
    }

    private void updateScrollbarDisplay(boolean reserveScrollbarTracks) {
        var nextDisplay = reserveScrollbarTracks ? ScrollDisplay.ALWAYS : ScrollDisplay.AUTO;
        if (nextDisplay == scrollDisplay) {
            return;
        }
        scrollDisplay = nextDisplay;
        scrollerStyle(style -> {
            style.horizontalScrollDisplay(nextDisplay);
            style.verticalScrollDisplay(nextDisplay);
        });
        pendingLayoutPasses = Math.max(pendingLayoutPasses, 2);
    }

    private CanvasBounds measureCanvasBounds() {
        var bounds = new CanvasBounds();
        var canvasX = canvas.getPositionX();
        var canvasY = canvas.getPositionY();
        for (var child : canvas.getChildren()) {
            includeBounds(bounds, child, canvasX, canvasY);
        }
        return bounds;
    }

    private void includeBounds(CanvasBounds bounds, UIElement element, float canvasX, float canvasY) {
        if (!element.isDisplayed()) {
            return;
        }
        var left = element.getPositionX() - canvasX;
        var top = element.getPositionY() - canvasY;
        bounds.include(left, top, left + element.getSizeWidth(), top + element.getSizeHeight());
        for (var child : element.getChildren()) {
            includeBounds(bounds, child, canvasX, canvasY);
        }
    }

    private static float scaleFor(float width, float height) {
        if (width >= RELAXED_MIN_WIDTH && height >= RELAXED_MIN_HEIGHT) {
            return 1;
        }
        if (width >= COMPACT_MIN_WIDTH && height >= COMPACT_MIN_HEIGHT) {
            return COMPACT_SCALE;
        }
        return NARROW_SCALE;
    }

    private static boolean nearlyEqual(float left, float right) {
        return Math.abs(left - right) < EPSILON;
    }

    private static final class CanvasBounds {
        private float minX = Float.POSITIVE_INFINITY;
        private float minY = Float.POSITIVE_INFINITY;
        private float maxX = Float.NEGATIVE_INFINITY;
        private float maxY = Float.NEGATIVE_INFINITY;

        void include(float left, float top, float right, float bottom) {
            minX = Math.min(minX, left);
            minY = Math.min(minY, top);
            maxX = Math.max(maxX, right);
            maxY = Math.max(maxY, bottom);
        }

        float width() {
            return minX == Float.POSITIVE_INFINITY ? 0 : Math.max(0, maxX - minX);
        }

        float height() {
            return minY == Float.POSITIVE_INFINITY ? 0 : Math.max(0, maxY - minY);
        }
    }
}
