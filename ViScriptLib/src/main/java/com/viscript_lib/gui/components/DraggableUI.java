package com.viscript_lib.gui.components;

import com.lowdragmc.lowdraglib2.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Transform2D;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.gui.ui.style.PropertyRegistry;
import com.lowdragmc.lowdraglib2.gui.ui.style.StyleOrigin;
import com.lowdragmc.lowdraglib2.math.interpolate.Eases;
import com.lowdragmc.lowdraglib2.syncdata.ISubscription;
import dev.vfyjxf.taffy.style.FlexDirection;
import dev.vfyjxf.taffy.style.FlexWrap;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 可拖拽排序的容器组件
 */
@Getter
public class DraggableUI<T> extends UIElement {

    private List<T> dataList;
    private final Consumer<List<T>> onDataSync;
    private final Map<UIElement, T> uiToDataMap = new HashMap<>();

    private UIElement draggedCard = null;

    private final IGuiTexture ghostBg = new ColorRectTexture(0x804A4A4A);

    private boolean autoScrollEnabled = true;
    private ScrollerView autoScrollView = null;
    private float autoScrollMinPixelsPerTick = 2.0f;
    private float autoScrollMaxPixelsPerTick = 12.0f;
    private float autoScrollAccelerationDistance = 48.0f;
    private float lastDragMouseX = Float.NaN;
    private float lastDragMouseY = Float.NaN;

    private int lastTargetIndex = -1;

    private final Map<UIElement, float[]> lastKnownPositions = new HashMap<>();
    private final Map<UIElement, float[]> flipFromPositions = new HashMap<>();
    private int flipSnapshotTtlTicks = 0;
    private final Map<UIElement, ISubscription> activeFlipAnimations = new HashMap<>();

    private final Map<UIElement, IGuiTexture> originalBackgrounds = new HashMap<>();
    private final Map<UIElement, Float> originalOpacities = new HashMap<>();

    public DraggableUI(List<T> initialData, Consumer<List<T>> onDataSync) {
        this.dataList = new ArrayList<>(initialData);
        this.onDataSync = onDataSync;

        this.layout(layout -> {
            layout.flexDirection(FlexDirection.ROW);
            layout.wrap(FlexWrap.WRAP);
            layout.gapAll(10);
            layout.paddingAll(10);
        });

        this.addEventListener(UIEvents.MOUSE_UP, event -> stopDragging());

        this.addEventListener(UIEvents.TICK, e -> {
            if (draggedCard != null && !Float.isNaN(lastDragMouseX) && !Float.isNaN(lastDragMouseY)) {
                maybeAutoScrollAndReorder(lastDragMouseX, lastDragMouseY);
            }

            if (flipSnapshotTtlTicks > 0 && --flipSnapshotTtlTicks == 0) {
                flipFromPositions.clear();
            }
        });
    }

    public void addSortableCard(T data, UIElement card) {
        addSortableCard(data, card, null);
    }

    /**
     * @param dragHandle which element should start dragging; when null, the card itself is draggable
     */
    public void addSortableCard(T data, UIElement card, UIElement dragHandle) {
        uiToDataMap.put(card, data);

        bindDragEvents(card, dragHandle);
        this.addChild(card);

        card.addEventListener(UIEvents.LAYOUT_CHANGED, e -> onCardLayoutChanged(card));
    }

    public boolean isDragging() {
        return draggedCard != null;
    }

    public DraggableUI<T> setAutoScrollEnabled(boolean autoScrollEnabled) {
        this.autoScrollEnabled = autoScrollEnabled;
        return this;
    }

    /**
     * 手动指定要联动的滚动视图；不设置时会自动查找最近的 {@link ScrollerView} 父级。
     */
    public DraggableUI<T> setAutoScrollView(ScrollerView autoScrollView) {
        this.autoScrollView = autoScrollView;
        return this;
    }

    /**
     * @param minPixelsPerTick 刚越过边界时每 tick 自动滚动的像素
     * @param maxPixelsPerTick 越过边界较远时每 tick 自动滚动的像素上限
     * @param accelerationDistance 从最小速度加速到最大速度所需的越界距离
     */
    public DraggableUI<T> setAutoScrollSpeed(float minPixelsPerTick, float maxPixelsPerTick, float accelerationDistance) {
        this.autoScrollMinPixelsPerTick = Math.max(0, minPixelsPerTick);
        this.autoScrollMaxPixelsPerTick = Math.max(this.autoScrollMinPixelsPerTick, maxPixelsPerTick);
        this.autoScrollAccelerationDistance = Math.max(1, accelerationDistance);
        return this;
    }

    private void bindDragEvents(UIElement card, UIElement dragHandle) {
        UIElement handle = dragHandle == null ? card : dragHandle;

        handle.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            if (event.button == 0) {
                startDragging(card);
                event.stopPropagation();
            }
        });

        card.addEventListener(UIEvents.DRAG_SOURCE_UPDATE, event -> {
            if (draggedCard != card) return;
            lastDragMouseX = event.x;
            lastDragMouseY = event.y;

            if (!maybeAutoScrollAndReorder(event.x, event.y, false)) {
                maybeReorderAt(event.x, event.y);
            }
        });

        card.addEventListener(UIEvents.DRAG_END, event -> stopDragging());
        card.addEventListener(UIEvents.MOUSE_UP, event -> stopDragging());
    }

    private void onCardLayoutChanged(UIElement card) {
        float newX = card.getPositionX();
        float newY = card.getPositionY();

        lastKnownPositions.put(card, new float[]{newX, newY});

        if (card == draggedCard) {
            flipFromPositions.remove(card);
            return;
        }

        float[] oldPos = flipFromPositions.remove(card);
        if (oldPos == null) {
            return;
        }

        float dx = oldPos[0] - newX;
        float dy = oldPos[1] - newY;
        if (Math.abs(dx) < 0.5f && Math.abs(dy) < 0.5f) {
            return;
        }

        var prev = activeFlipAnimations.remove(card);
        if (prev != null) {
            prev.unsubscribe();
        }

        card.getStyleBag().removeCandidates(PropertyRegistry.TRANSFORM_2D, slot ->
                slot.origin() == StyleOrigin.ANIMATION && slot.specificity() == 999 && slot.sourceOrder() == 0);

        card.getStyle().transform2D(Transform2D.identity().translate(dx, dy));

        var sub = card.animation()
                .duration(0.30f)
                .ease(Eases.QUART_OUT)
                .style(PropertyRegistry.TRANSFORM_2D, Transform2D.identity())
                .onFinished(activeFlipAnimations::remove)
                .start();
        activeFlipAnimations.put(card, sub);
    }

    private void startDragging(UIElement card) {
        if (draggedCard != null && draggedCard != card) {
            stopDragging();
        }

        draggedCard = card;
        lastTargetIndex = -1;
        lastDragMouseX = Float.NaN;
        lastDragMouseY = Float.NaN;

        originalBackgrounds.put(card, card.getStyle().backgroundTexture());
        originalOpacities.put(card, card.getStyle().opacity());

        card.getStyle().backgroundTexture(ghostBg);
        card.getStyle().opacity(0.4f);

        var dragTexture = originalBackgrounds.get(card);
        if (dragTexture == null) {
            dragTexture = ghostBg;
        }
        var handler = card.startDrag(uiToDataMap.get(card), dragTexture);
        float w = card.getSizeWidth();
        float h = card.getSizeHeight();
        if (w > 0.1f && h > 0.1f) {
            handler.setDragTexture(-w / 2f, -h / 2f, w, h);
        }
    }

    private boolean maybeAutoScrollAndReorder(float mouseX, float mouseY) {
        return maybeAutoScrollAndReorder(mouseX, mouseY, true);
    }

    private boolean maybeAutoScrollAndReorder(float mouseX, float mouseY, boolean scroll) {
        if (!autoScrollEnabled || draggedCard == null) {
            return false;
        }

        ScrollerView scrollView = resolveAutoScrollView();
        if (scrollView == null || !scrollView.isDisplayed()) {
            return false;
        }

        float top = scrollView.viewPort.getContentY();
        float bottom = top + scrollView.viewPort.getContentHeight();
        if (bottom <= top) {
            return false;
        }

        int direction = 0;
        float overflowDistance = 0;
        if (mouseY < top) {
            direction = -1;
            overflowDistance = top - mouseY;
        } else if (mouseY > bottom) {
            direction = 1;
            overflowDistance = mouseY - bottom;
        }

        if (direction == 0) {
            return false;
        }

        if (scroll) {
            scrollVertically(scrollView, direction, overflowDistance);
        }

        float reorderMouseX = clampMouseXForAutoScroll(scrollView, mouseX);
        float reorderMouseY = direction < 0 ? top + 1.0f : bottom - 1.0f;
        maybeReorderAt(reorderMouseX, reorderMouseY);
        return true;
    }

    private ScrollerView resolveAutoScrollView() {
        if (autoScrollView != null) {
            return autoScrollView;
        }
        return getFirstAncestorOfType(ScrollerView.class);
    }

    private boolean scrollVertically(ScrollerView scrollView, int direction, float overflowDistance) {
        var scroller = scrollView.verticalScroller;
        if (!scroller.isDisplayed()) {
            return false;
        }

        float minValue = scroller.getMinValue();
        float maxValue = scroller.getMaxValue();
        if (maxValue <= minValue) {
            return false;
        }

        float value = scroller.getValue();
        if ((direction < 0 && value <= minValue + 0.0001f)
                || (direction > 0 && value >= maxValue - 0.0001f)) {
            return false;
        }

        float scrollablePixels = scrollView.getContainerHeight() - scrollView.viewPort.getContentHeight();
        if (scrollablePixels <= 0.1f) {
            return false;
        }

        float speedProgress = clamp(overflowDistance / autoScrollAccelerationDistance, 0.0f, 1.0f);
        float pixels = autoScrollMinPixelsPerTick
                + (autoScrollMaxPixelsPerTick - autoScrollMinPixelsPerTick) * speedProgress;
        float valueDelta = direction * pixels / scrollablePixels * (maxValue - minValue);

        scroller.setValue(value + valueDelta);
        return true;
    }

    private float clampMouseXForAutoScroll(ScrollerView scrollView, float mouseX) {
        float left = Math.max(getContentX(), scrollView.viewPort.getContentX());
        float right = Math.min(getContentX() + getContentWidth(),
                scrollView.viewPort.getContentX() + scrollView.viewPort.getContentWidth());
        if (right - left <= 2.0f) {
            return mouseX;
        }
        return clamp(mouseX, left + 1.0f, right - 1.0f);
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private void maybeReorderAt(float mouseX, float mouseY) {
        if (draggedCard == null) return;

        List<UIElement> children = new ArrayList<>(this.getChildren());
        int currentIndex = children.indexOf(draggedCard);
        if (currentIndex < 0) return;

        for (int targetIndex = 0; targetIndex < children.size(); targetIndex++) {
            UIElement sibling = children.get(targetIndex);
            if (sibling == draggedCard) continue;

            float siblingX = sibling.getPositionX();
            float siblingY = sibling.getPositionY();
            float siblingW = sibling.getSizeWidth();
            float siblingH = sibling.getSizeHeight();

            if (!UIElement.isMouseOverRect(siblingX, siblingY, siblingW, siblingH, mouseX, mouseY)) {
                continue;
            }

            float centerX = siblingX + siblingW / 2f;
            float centerY = siblingY + siblingH / 2f;
            float offsetX = mouseX - centerX;
            float offsetY = mouseY - centerY;
            boolean insertBefore = (Math.abs(offsetY) > Math.abs(offsetX)) ? (offsetY < 0) : (offsetX < 0);

            int actualInsertIndex = insertBefore
                    ? (currentIndex < targetIndex ? targetIndex - 1 : targetIndex)
                    : (currentIndex < targetIndex ? targetIndex : targetIndex + 1);

            int maxIndex = Math.max(0, children.size() - 1);
            if (actualInsertIndex > maxIndex) actualInsertIndex = maxIndex;

            moveDraggedCardTo(currentIndex, actualInsertIndex);
            return;
        }

        maybeMoveToLastPosition(children, currentIndex, mouseX, mouseY);
    }

    /**
     * 兼容拖到最后一个元素后方的空白区域。
     *
     * <p>普通换位依赖鼠标命中某个 sibling；最后一个元素下面或右侧没有 sibling，
     * 所以需要额外把这块区域视为“插入到末尾”。
     */
    private void maybeMoveToLastPosition(List<UIElement> children, int currentIndex, float mouseX, float mouseY) {
        int lastIndex = children.size() - 1;
        if (currentIndex == lastIndex) return;

        UIElement lastSibling = null;
        for (int i = children.size() - 1; i >= 0; i--) {
            UIElement child = children.get(i);
            if (child != draggedCard) {
                lastSibling = child;
                break;
            }
        }
        if (lastSibling == null) return;

        float lastX = lastSibling.getPositionX();
        float lastY = lastSibling.getPositionY();
        float lastW = lastSibling.getSizeWidth();
        float lastH = lastSibling.getSizeHeight();
        float centerX = lastX + lastW / 2f;
        float centerY = lastY + lastH / 2f;

        boolean afterLastRow = mouseY > centerY;
        boolean afterLastColumn = mouseY >= lastY && mouseY <= lastY + lastH && mouseX > centerX;
        if (afterLastRow || afterLastColumn) {
            moveDraggedCardTo(currentIndex, lastIndex);
        }
    }

    private void moveDraggedCardTo(int currentIndex, int targetIndex) {
        if (lastTargetIndex == targetIndex || targetIndex == currentIndex) {
            return;
        }
        lastTargetIndex = targetIndex;

        snapshotPositionsForFlip();

        this.removeChild(draggedCard);
        this.addChildAt(draggedCard, targetIndex);

        this.markTaffyStyleDirty();
    }

    private void snapshotPositionsForFlip() {
        flipFromPositions.clear();
        flipSnapshotTtlTicks = 2;

        for (UIElement child : this.getChildren()) {
            if (child == draggedCard) continue;
            var last = lastKnownPositions.get(child);
            if (last != null) {
                flipFromPositions.put(child, new float[]{last[0], last[1]});
            } else {
                flipFromPositions.put(child, new float[]{child.getPositionX(), child.getPositionY()});
            }
        }
    }

    private void stopDragging() {
        if (draggedCard != null) {
            var bg = originalBackgrounds.remove(draggedCard);
            if (bg != null) {
                draggedCard.getStyle().backgroundTexture(bg);
            }
            var opacity = originalOpacities.remove(draggedCard);
            draggedCard.getStyle().opacity(opacity == null ? 1.0f : opacity);
            draggedCard = null;
            lastTargetIndex = -1;

            flipFromPositions.clear();
            flipSnapshotTtlTicks = 0;
            lastDragMouseX = Float.NaN;
            lastDragMouseY = Float.NaN;

            syncDataFromDOM();
        }
    }

    private void syncDataFromDOM() {
        List<T> newDataList = new ArrayList<>();
        for (UIElement child : this.getChildren()) {
            if (uiToDataMap.containsKey(child)) {
                newDataList.add(uiToDataMap.get(child));
            }
        }
        this.dataList = newDataList;

        if (onDataSync != null) {
            onDataSync.accept(newDataList);
        }
    }
}
