package com.viscript_recipe.gui.editor;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.styletemplate.Sprites;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexWrap;

import java.util.function.IntFunction;

final class RecipeGridFactory {
    private static final int GRID_PADDING = 4;
    private static final int GRID_GAP = 2;

    private RecipeGridFactory() {
    }

    static UIElement borderedGrid(int columns, int rows, int slotSize, GridCellFactory cellFactory) {
        return borderedGrid(columns, rows, slotSize, false, null, cellFactory);
    }

    static UIElement borderedGrid(int columns, int rows, int slotSize, boolean centered, UIElement[] rowStorage, GridCellFactory cellFactory) {
        var grid = RecipeEditorUi.column().layout(layout -> {
            layout.width(gridDimension(columns, slotSize));
            layout.height(gridDimension(rows, slotSize));
            layout.paddingAll(GRID_PADDING);
            layout.gapAll(GRID_GAP);
            if (centered) {
                layout.alignItems(AlignItems.CENTER);
                layout.justifyContent(AlignContent.CENTER);
            }
        }).style(style -> style.backgroundTexture(Sprites.BORDER_DARK));
        for (int row = 0; row < rows; row++) {
            var rowIndex = row;
            var rowElement = RecipeEditorUi.row().layout(layout -> {
                layout.widthPercent(100);
                layout.height(slotSize);
                layout.gapAll(GRID_GAP);
                if (centered) {
                    layout.alignItems(AlignItems.CENTER);
                    layout.justifyContent(AlignContent.CENTER);
                }
            });
            if (rowStorage != null && rowIndex < rowStorage.length) {
                rowStorage[rowIndex] = rowElement;
            }
            for (int col = 0; col < columns; col++) {
                rowElement.addChild(cellFactory.create(row * columns + col, row, col));
            }
            grid.addChild(rowElement);
        }
        return grid;
    }

    static UIElement borderedRow(int slotCount, int slotSize, IntFunction<UIElement> cellFactory) {
        var row = RecipeEditorUi.row().layout(layout -> {
            layout.widthPercent(100);
            layout.maxWidth(gridDimension(slotCount, slotSize));
            layout.minWidth(0);
            layout.minHeight(slotSize + GRID_PADDING * 2);
            layout.paddingAll(GRID_PADDING);
            layout.gapAll(GRID_GAP);
            layout.flexWrap(FlexWrap.WRAP);
            layout.alignItems(AlignItems.CENTER);
            layout.alignContent(AlignContent.CENTER);
            layout.justifyContent(AlignContent.CENTER);
        }).style(style -> style.backgroundTexture(Sprites.BORDER_DARK));
        for (int i = 0; i < slotCount; i++) {
            row.addChild(cellFactory.apply(i));
        }
        return row;
    }

    static UIElement slotCell(UIElement slot, int slotSize) {
        return new UIElement().layout(layout -> {
            layout.width(slotSize);
            layout.height(slotSize);
        }).addChild(slot);
    }

    private static int gridDimension(int cells, int slotSize) {
        return cells * slotSize + GRID_PADDING * 2 + Math.max(0, cells - 1) * GRID_GAP;
    }

    @FunctionalInterface
    interface GridCellFactory {
        UIElement create(int index, int row, int col);
    }
}
