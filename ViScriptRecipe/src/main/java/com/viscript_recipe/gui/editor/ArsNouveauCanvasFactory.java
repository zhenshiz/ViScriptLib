package com.viscript_recipe.gui.editor;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.Icons;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.styletemplate.Sprites;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.TaffyPosition;
import net.minecraft.network.chat.Component;

import java.util.function.IntFunction;

final class ArsNouveauCanvasFactory {
    private static final int SLOT_SIZE = 24;
    private static final int OUTPUT_SLOT_SIZE = 30;

    private ArsNouveauCanvasFactory() {
    }

    static ApparatusCanvas createApparatusCanvas(
            UIElement[] ingredientSlots,
            UIElement[] ingredientCells,
            UIElement resultSlot,
            UIElement centerPreviewIcon,
            UIElement outputPreviewIcon,
            Label sourceLabel,
            Label tierLabel,
            IntFunction<Component> ingredientTooltip
    ) {
        layoutFloatingLabel(sourceLabel, 20, 178, 176, 18);
        layoutFloatingLabel(tierLabel, 20, 8, 92, 18);

        var panel = createPanel(Sprites.BORDER_DARK);
        var centerPreviewCell = createFloatingCell(centerPreviewIcon, 98, 88, SLOT_SIZE, SLOT_SIZE);
        var outputPreviewCell = createFloatingCell(outputPreviewIcon, 170, 24, OUTPUT_SLOT_SIZE, OUTPUT_SLOT_SIZE);
        panel.addChildren(
                createIngredientCell(ingredientSlots[0], ingredientCells, 0, 0, 98, 88, ingredientTooltip),
                centerPreviewCell,
                createFloatingCell(resultSlot, 170, 24, OUTPUT_SLOT_SIZE, OUTPUT_SLOT_SIZE),
                outputPreviewCell,
                sourceLabel,
                tierLabel
        );

        var pedestalPositions = new int[][]{
                {98, 28},
                {53, 47},
                {38, 88},
                {53, 133},
                {98, 148},
                {143, 133},
                {158, 88},
                {143, 47}
        };
        for (int i = 0; i < pedestalPositions.length; i++) {
            var index = i + 1;
            var position = pedestalPositions[i];
            panel.addChild(createIngredientCell(
                    ingredientSlots[index],
                    ingredientCells,
                    index,
                    index,
                    position[0],
                    position[1],
                    ingredientTooltip
            ));
        }
        return new ApparatusCanvas(centerPanel(panel), centerPreviewCell, outputPreviewCell);
    }

    static UIElement createImbuementCanvas(
            UIElement inputSlot,
            UIElement[] pedestalSlots,
            UIElement[] pedestalCells,
            UIElement centerIcon,
            UIElement resultSlot,
            Label sourceLabel,
            IntFunction<Component> ingredientTooltip
    ) {
        layoutFloatingLabel(sourceLabel, 20, 178, 176, 18);
        var panel = createPanel(Sprites.BORDER_DARK);
        panel.addChildren(
                createIngredientCell(inputSlot, null, -1, 0, 98, 88, ingredientTooltip),
                createFloatingCell(centerIcon, 94, 84, 32, 32),
                createFloatingCell(resultSlot, 170, 24, OUTPUT_SLOT_SIZE, OUTPUT_SLOT_SIZE),
                sourceLabel
        );

        var pedestalPositions = new int[][]{
                {98, 28},
                {150, 122},
                {46, 122}
        };
        for (int i = 0; i < pedestalSlots.length; i++) {
            var position = pedestalPositions[i];
            panel.addChild(createIngredientCell(
                    pedestalSlots[i],
                    pedestalCells,
                    i,
                    i + 1,
                    position[0],
                    position[1],
                    ingredientTooltip
            ));
        }
        return centerPanel(panel);
    }

    static UIElement createGlyphCanvas(
            UIElement[] ingredientSlots,
            UIElement[] ingredientCells,
            UIElement workstationIcon,
            UIElement resultSlot,
            Label expLabel,
            IntFunction<Component> ingredientTooltip
    ) {
        layoutFloatingLabel(expLabel, 20, 178, 176, 18);
        var panel = createPanel(Sprites.BORDER);
        panel.addChildren(
                createFloatingCell(workstationIcon, 98, 88, 32, 32),
                createFloatingCell(resultSlot, 170, 24, OUTPUT_SLOT_SIZE, OUTPUT_SLOT_SIZE),
                expLabel
        );

        var inputPositions = new int[][]{
                {98, 28},
                {137, 42},
                {158, 78},
                {151, 122},
                {124, 148},
                {80, 148},
                {49, 122},
                {39, 78},
                {59, 42}
        };
        for (int i = 0; i < ingredientSlots.length; i++) {
            var position = inputPositions[i];
            panel.addChild(createIngredientCell(
                    ingredientSlots[i],
                    ingredientCells,
                    i,
                    i,
                    position[0],
                    position[1],
                    ingredientTooltip
            ));
        }
        return centerPanel(panel);
    }

    static UIElement createCrushCanvas(UIElement inputSlot, UIElement machineIcon, UIElement[] outputSlots, UIElement[] outputCells) {
        return RecipeEditorUi.row().layout(layout -> {
            layout.widthPercent(100);
            layout.flex(1);
            layout.gapAll(18);
            layout.alignItems(AlignItems.CENTER);
            layout.justifyContent(AlignContent.CENTER);
        }).addChildren(
                RecipeEditorUi.column().layout(layout -> {
                    layout.width(72);
                    layout.gapAll(4);
                    layout.alignItems(AlignItems.CENTER);
                }).addChildren(
                        RecipeEditorUi.label(Component.translatable("viscript_recipe.editor.ars_nouveau.input")),
                        inputSlot
                ),
                RecipeEditorUi.column().layout(layout -> {
                    layout.width(96);
                    layout.height(104);
                    layout.alignItems(AlignItems.CENTER);
                    layout.justifyContent(AlignContent.CENTER);
                }).addChild(machineIcon),
                createRightArrowElement(34, 20),
                RecipeEditorUi.column().layout(layout -> {
                    layout.width(SLOT_SIZE * 3 + 18);
                    layout.gapAll(4);
                    layout.alignItems(AlignItems.CENTER);
                }).addChildren(
                        RecipeEditorUi.label(Component.translatable("viscript_recipe.editor.ars_nouveau.outputs")),
                        createOutputGrid(outputSlots, outputCells)
                )
        );
    }

    private static UIElement createPanel(IGuiTexture background) {
        return new UIElement().layout(layout -> {
            layout.width(228);
            layout.height(216);
            layout.positionType(TaffyPosition.RELATIVE);
        }).style(style -> style.backgroundTexture(background));
    }

    private static UIElement createOutputGrid(UIElement[] outputSlots, UIElement[] outputCells) {
        var grid = RecipeEditorUi.column().layout(layout -> {
            layout.width(SLOT_SIZE * 3 + 14);
            layout.height(SLOT_SIZE * 2 + 12);
            layout.paddingAll(4);
            layout.gapAll(2);
            layout.alignItems(AlignItems.CENTER);
            layout.justifyContent(AlignContent.CENTER);
        }).style(style -> style.backgroundTexture(Sprites.BORDER_DARK));
        for (int row = 0; row < 2; row++) {
            var rowElement = RecipeEditorUi.row().layout(layout -> {
                layout.widthPercent(100);
                layout.height(SLOT_SIZE);
                layout.gapAll(2);
                layout.alignItems(AlignItems.CENTER);
                layout.justifyContent(AlignContent.CENTER);
            });
            for (int col = 0; col < 3; col++) {
                var index = row * 3 + col;
                var cell = new UIElement().layout(layout -> {
                    layout.width(SLOT_SIZE);
                    layout.height(SLOT_SIZE);
                }).addChild(outputSlots[index]);
                outputCells[index] = cell;
                rowElement.addChild(cell);
            }
            grid.addChild(rowElement);
        }
        return grid;
    }

    private static UIElement createIngredientCell(
            UIElement slot,
            UIElement[] cellStorage,
            int storageIndex,
            int recipeIndex,
            int left,
            int top,
            IntFunction<Component> tooltip
    ) {
        var cell = createFloatingCell(slot, left, top, SLOT_SIZE, SLOT_SIZE)
                .style(style -> style.tooltips(tooltip.apply(recipeIndex)));
        if (cellStorage != null && storageIndex >= 0 && storageIndex < cellStorage.length) {
            cellStorage[storageIndex] = cell;
        }
        return cell;
    }

    private static UIElement createFloatingCell(UIElement child, int left, int top, int width, int height) {
        child.layout(layout -> {
            layout.width(width);
            layout.height(height);
        });
        return new UIElement().layout(layout -> {
            layout.positionType(TaffyPosition.ABSOLUTE);
            layout.left(left);
            layout.top(top);
            layout.width(width);
            layout.height(height);
        }).addChild(child);
    }

    private static void layoutFloatingLabel(Label label, int left, int top, int width, int height) {
        label.layout(layout -> {
            layout.positionType(TaffyPosition.ABSOLUTE);
            layout.left(left);
            layout.top(top);
            layout.width(width);
            layout.height(height);
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

    private static UIElement createRightArrowElement(int width, int height) {
        return new UIElement().layout(layout -> {
            layout.width(width);
            layout.height(height);
        }).style(style -> style.backgroundTexture(Icons.DOWN_ARROW_NO_BAR.copy().rotate(-90)));
    }

    record ApparatusCanvas(UIElement root, UIElement centerPreviewCell, UIElement outputPreviewCell) {
    }
}
