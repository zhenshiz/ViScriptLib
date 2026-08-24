package com.viscript_recipe.compat.create.canvas;

import com.lowdragmc.lowdraglib2.gui.ColorPattern;
import com.lowdragmc.lowdraglib2.gui.texture.Icons;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.gui.ui.styletemplate.Sprites;
import com.viscript_recipe.gui.editor.RecipeEditorUi;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexWrap;
import net.minecraft.network.chat.Component;

import java.util.function.IntConsumer;

public final class CreateSequencedAssemblyCanvasFactory {
    private static final int SLOT_SIZE = 24;

    private CreateSequencedAssemblyCanvasFactory() {
    }

    public static Canvas createCanvas(
            UIElement inputSlot,
            UIElement transitionalSlot,
            UIElement[] outputSlots,
            UIElement[] outputSlotCells,
            Label loopsLabel
    ) {
        var secondaryOutputColumn = createSecondaryOutputColumn(outputSlots, outputSlotCells);
        var stepRow = createStepRow();
        var root = RecipeEditorUi.column().layout(layout -> {
            layout.widthPercent(100);
            layout.flex(1);
            layout.gapAll(12);
            layout.alignItems(AlignItems.CENTER);
            layout.justifyContent(AlignContent.CENTER);
        }).addChildren(
                stepRow,
                createFlowRow(inputSlot, transitionalSlot, loopsLabel, outputSlots[0], secondaryOutputColumn)
        );
        return new Canvas(root, secondaryOutputColumn, stepRow);
    }

    private static UIElement createStepRow() {
        return RecipeEditorUi.row().layout(layout -> {
            layout.widthPercent(100);
            layout.minHeight(112);
            layout.gapAll(6);
            layout.flexWrap(FlexWrap.WRAP);
            layout.alignItems(AlignItems.CENTER);
            layout.alignContent(AlignContent.CENTER);
            layout.justifyContent(AlignContent.CENTER);
        });
    }

    public static UIElement createStepCard(
            int index,
            UIElement label, UIElement icon,
            UIElement ingredientCell,
            UIElement fluidCell,
            IntConsumer selectStep
    ) {
        icon.layout(layout -> layout.width(36).height(36));
        label.layout(layout -> layout.widthPercent(100).height(14));

        var slotRow = RecipeEditorUi.row().layout(layout -> {
            layout.widthPercent(100);
            layout.height(38);
            layout.gapAll(4);
            layout.alignItems(AlignItems.CENTER);
            layout.justifyContent(AlignContent.CENTER);
        });
        if (ingredientCell != null) slotRow.addChild(ingredientCell);
        if (fluidCell != null) slotRow.addChild(fluidCell);

        var card = RecipeEditorUi.column().layout(layout -> {
            layout.width(66);
            layout.height(106);
            layout.paddingAll(4);
            layout.gapAll(4);
            layout.alignItems(AlignItems.CENTER);
        }).style(style -> style.backgroundTexture(Sprites.BORDER_DARK))
                .addChildren(label, icon, slotRow);
        card.addEventListener(UIEvents.MOUSE_DOWN, event -> selectStep.accept(index));
        return card;
    }

    private static UIElement createFlowRow(UIElement inputSlot, UIElement transitionalSlot, Label loopsLabel, UIElement mainOutputSlot, UIElement secondaryOutputColumn) {
        return RecipeEditorUi.row().layout(layout -> {
            layout.widthPercent(100);
            layout.height(96);
            layout.gapAll(10);
            layout.alignItems(AlignItems.CENTER);
            layout.justifyContent(AlignContent.CENTER);
        }).addChildren(
                CreateProcessingCanvasFactory.framedSlot(inputSlot, 42),
                rightArrow(30, 18),
                RecipeEditorUi.column().layout(layout -> {
                    layout.width(66);
                    layout.height(62);
                    layout.gapAll(4);
                    layout.alignItems(AlignItems.CENTER);
                    layout.justifyContent(AlignContent.CENTER);
                }).addChildren(
                        CreateProcessingCanvasFactory.framedSlot(transitionalSlot, 42),
                        loopsLabel
                ),
                rightArrow(30, 18),
                createOutputPanel(mainOutputSlot, secondaryOutputColumn)
        );
    }

    private static UIElement createOutputPanel(UIElement mainOutputSlot, UIElement secondaryOutputColumn) {
        return RecipeEditorUi.row().layout(layout -> {
            layout.width(184);
            layout.height(86);
            layout.gapAll(8);
            layout.alignItems(AlignItems.CENTER);
            layout.justifyContent(AlignContent.CENTER);
        }).addChildren(
                RecipeEditorUi.column().layout(layout -> {
                    layout.width(50);
                    layout.height(76);
                    layout.gapAll(4);
                    layout.alignItems(AlignItems.CENTER);
                    layout.justifyContent(AlignContent.CENTER);
                }).addChildren(
                        sectionLabel("viscript_recipe.editor.create.sequenced_assembly.main_output"),
                        CreateProcessingCanvasFactory.framedSlot(mainOutputSlot, 42)
                ),
                secondaryOutputColumn
        );
    }

    private static UIElement createSecondaryOutputColumn(UIElement[] outputSlots, UIElement[] outputSlotCells) {
        var column = RecipeEditorUi.column().layout(layout -> {
            layout.width(126);
            layout.height(76);
            layout.gapAll(4);
            layout.alignItems(AlignItems.CENTER);
            layout.justifyContent(AlignContent.CENTER);
        });
        column.addChildren(
                sectionLabel("viscript_recipe.editor.create.sequenced_assembly.extra_outputs"),
                createSecondaryOutputGrid(outputSlots, outputSlotCells)
        );
        return column;
    }

    private static UIElement createSecondaryOutputGrid(UIElement[] outputSlots, UIElement[] outputSlotCells) {
        var grid = RecipeEditorUi.column().layout(layout -> {
            layout.width(126);
            layout.height(62);
            layout.gapAll(2);
            layout.alignItems(AlignItems.CENTER);
            layout.justifyContent(AlignContent.CENTER);
        });
        for (int row = 0; row < 2; row++) {
            var rowElement = RecipeEditorUi.row().layout(layout -> {
                layout.widthPercent(100);
                layout.height(SLOT_SIZE);
                layout.gapAll(2);
                layout.alignItems(AlignItems.CENTER);
                layout.justifyContent(AlignContent.CENTER);
            });
            for (int col = 0; col < 4; col++) {
                var index = row * 4 + col + 1;
                var cell = RecipeEditorUi.row().layout(layout -> {
                    layout.width(SLOT_SIZE);
                    layout.height(SLOT_SIZE);
                    layout.alignItems(AlignItems.CENTER);
                    layout.justifyContent(AlignContent.CENTER);
                }).addChild(outputSlots[index]);
                outputSlotCells[index] = cell;
                rowElement.addChild(cell);
            }
            grid.addChild(rowElement);
        }
        return grid;
    }

    private static Label sectionLabel(String key) {
        var label = RecipeEditorUi.label(Component.translatable(key));
        label.layout(layout -> {
            layout.widthPercent(100);
            layout.height(16);
        });
        label.textStyle(style -> style
                .textAlignHorizontal(Horizontal.CENTER)
                .textColor(ColorPattern.LIGHT_GRAY.color)
                .textWrap(com.lowdragmc.lowdraglib2.gui.ui.data.TextWrap.HOVER_ROLL));
        return label;
    }

    private static UIElement rightArrow(int width, int height) {
        return new UIElement().layout(layout -> {
            layout.width(width);
            layout.height(height);
        }).style(style -> style.backgroundTexture(Icons.DOWN_ARROW_NO_BAR.copy().rotate(-90)));
    }

    public record Canvas(UIElement root, UIElement secondaryOutputColumn, UIElement stepRow) {
    }
}
