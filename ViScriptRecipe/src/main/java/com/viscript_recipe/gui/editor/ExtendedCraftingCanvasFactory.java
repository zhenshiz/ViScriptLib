package com.viscript_recipe.gui.editor;

import com.lowdragmc.lowdraglib2.gui.texture.Icons;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.styletemplate.Sprites;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.TaffyPosition;
import net.minecraft.network.chat.Component;

final class ExtendedCraftingCanvasFactory {
    private static final int SLOT_SIZE = 24;
    private static final int JEI_SLOT_SIZE = 18;

    private ExtendedCraftingCanvasFactory() {
    }

    static UIElement createCombinationCanvas(UIElement[] ingredientSlots, UIElement outputSlot) {
        var panel = createPanel(220, 108);
        panel.addChild(createSlotCell(ingredientSlots[0], 88, 52));
        var pedestalPositions = new int[][]{
                {88, 16},
                {116, 28},
                {130, 52},
                {116, 76},
                {88, 88},
                {60, 76},
                {46, 52},
                {60, 28}
        };
        for (int i = 0; i < pedestalPositions.length; i++) {
            panel.addChild(createSlotCell(
                    ingredientSlots[i + 1],
                    pedestalPositions[i][0],
                    pedestalPositions[i][1]
            ));
        }
        panel.addChild(createFloatingCell(createRightArrowElement(30, 18), 160, 52, 30, 18));
        panel.addChild(createSlotCell(outputSlot, 196, 52));
        return centerPanel(panel);
    }

    static UIElement createCompressorCanvas(UIElement catalystSlot, UIElement inputSlot, UIElement outputSlot) {
        var panel = createPanel(150, 54);
        panel.addChildren(
                createSlotCell(catalystSlot, 10, 18),
                createSlotCell(inputSlot, 42, 18),
                createFloatingCell(createRightArrowElement(30, 18), 78, 18, 30, 18),
                createSlotCell(outputSlot, 122, 18)
        );
        return centerPanel(panel);
    }

    static UIElement createFluxCanvas(UIElement[] ingredientSlots, UIElement outputSlot) {
        var grid = RecipeEditorUi.column().layout(layout -> {
            layout.width(SLOT_SIZE * 3 + 12);
            layout.height(SLOT_SIZE * 3 + 12);
            layout.paddingAll(4);
            layout.gapAll(2);
        }).style(style -> style.backgroundTexture(Sprites.BORDER_DARK));

        for (int row = 0; row < 3; row++) {
            var rowElement = RecipeEditorUi.row().layout(layout -> {
                layout.widthPercent(100);
                layout.height(SLOT_SIZE);
                layout.gapAll(2);
            });
            for (int col = 0; col < 3; col++) {
                rowElement.addChild(ingredientSlots[row * 3 + col]);
            }
            grid.addChild(rowElement);
        }

        return RecipeEditorUi.row().layout(layout -> {
            layout.widthPercent(100);
            layout.flex(1);
            layout.gapAll(24);
            layout.alignItems(AlignItems.CENTER);
            layout.justifyContent(AlignContent.CENTER);
        }).addChildren(
                grid,
                new UIElement().layout(layout -> {
                    layout.width(28);
                    layout.height(16);
                }).style(style -> style.backgroundTexture(Icons.ARROW_LEFT_RIGHT)),
                RecipeEditorUi.column().layout(layout -> {
                    layout.width(72);
                    layout.gapAll(4);
                    layout.alignItems(AlignItems.CENTER);
                }).addChildren(
                        RecipeEditorUi.label(Component.translatable("viscript_recipe.editor.result")),
                        outputSlot
                )
        );
    }

    private static UIElement createPanel(int width, int height) {
        return new UIElement().layout(layout -> {
            layout.width(width);
            layout.height(height);
            layout.positionType(TaffyPosition.RELATIVE);
        });
    }

    private static UIElement centerPanel(UIElement panel) {
        return RecipeEditorUi.row().layout(layout -> {
            layout.widthPercent(100);
            layout.flex(1);
            layout.alignItems(AlignItems.CENTER);
            layout.justifyContent(AlignContent.CENTER);
        }).addChild(panel);
    }

    private static UIElement createSlotCell(UIElement child, int left, int top) {
        return createFloatingCell(child, left, top, JEI_SLOT_SIZE, JEI_SLOT_SIZE)
                .style(style -> style.backgroundTexture(ItemSlot.ITEM_SLOT_TEXTURE));
    }

    private static UIElement createFloatingCell(UIElement child, int left, int top, int width, int height) {
        return new UIElement().layout(layout -> {
            layout.positionType(TaffyPosition.ABSOLUTE);
            layout.left(left);
            layout.top(top);
            layout.width(width);
            layout.height(height);
        }).addChild(child);
    }

    private static UIElement createRightArrowElement(int width, int height) {
        return new UIElement().layout(layout -> {
            layout.width(width);
            layout.height(height);
        }).style(style -> style.backgroundTexture(Icons.DOWN_ARROW_NO_BAR.copy().rotate(-90)));
    }
}
