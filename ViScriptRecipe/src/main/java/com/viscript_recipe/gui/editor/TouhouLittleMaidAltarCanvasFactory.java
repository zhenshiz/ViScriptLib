package com.viscript_recipe.gui.editor;

import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.TextWrap;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.TaffyPosition;
import net.minecraft.resources.ResourceLocation;

/**
 * Builds the altar editor canvas from Touhou Little Maid's JEI coordinates.
 */
final class TouhouLittleMaidAltarCanvasFactory {
    private static final int WIDTH = 160;
    private static final int HEIGHT = 125;
    private static final int SLOT_SIZE = 18;
    private static final int[][] INPUT_POSITIONS = {
            {40, 35},
            {40, 55},
            {60, 15},
            {80, 15},
            {100, 35},
            {100, 55}
    };

    private TouhouLittleMaidAltarCanvasFactory() {
    }

    static UIElement createCanvas(
            UIElement[] inputSlots,
            UIElement outputSlot,
            Label powerLabel,
            Label resultDescriptionLabel,
            Runnable selectRecipeProperties
    ) {
        if (inputSlots.length != INPUT_POSITIONS.length) {
            throw new IllegalArgumentException("Touhou Little Maid altar requires exactly six input slots");
        }
        var panel = new UIElement().layout(layout -> {
            layout.positionType(TaffyPosition.RELATIVE);
            layout.width(WIDTH);
            layout.height(HEIGHT);
        });
        for (int i = 0; i < inputSlots.length; i++) {
            panel.addChild(createCell(inputSlots[i], INPUT_POSITIONS[i][0], INPUT_POSITIONS[i][1], SLOT_SIZE, SLOT_SIZE));
        }
        panel.addChild(createCell(outputSlot, 140, 5, SLOT_SIZE, SLOT_SIZE));

        var powerIcon = new UIElement().layout(layout -> {
            layout.positionType(TaffyPosition.ABSOLUTE);
            layout.left(72);
            layout.top(40);
            layout.width(13);
            layout.height(13);
        }).style(style -> style.backgroundTexture(SpriteTexture.of(new ResourceLocation(
                "touhou_little_maid",
                "textures/entity/power_point.png"
        )).setSprite(32, 0, 16, 16)));
        powerIcon.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            selectRecipeProperties.run();
            event.stopPropagation();
        });

        powerLabel.textStyle(style -> style
                .textAlignHorizontal(Horizontal.CENTER)
                .textWrap(TextWrap.HOVER_ROLL));
        powerLabel.layout(layout -> {
            layout.positionType(TaffyPosition.ABSOLUTE);
            layout.left(55);
            layout.top(53);
            layout.width(50);
            layout.height(12);
        });
        powerLabel.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            selectRecipeProperties.run();
            event.stopPropagation();
        });

        resultDescriptionLabel.textStyle(style -> style
                .textAlignHorizontal(Horizontal.CENTER)
                .textWrap(TextWrap.HOVER_ROLL));
        resultDescriptionLabel.layout(layout -> {
            layout.positionType(TaffyPosition.ABSOLUTE);
            layout.left(0);
            layout.top(82);
            layout.width(WIDTH);
            layout.height(18);
        });
        resultDescriptionLabel.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            selectRecipeProperties.run();
            event.stopPropagation();
        });

        panel.addChildren(powerIcon, powerLabel, resultDescriptionLabel);
        return RecipeEditorUi.row().layout(layout -> {
            layout.widthPercent(100);
            layout.flex(1);
            layout.minWidth(0);
            layout.minHeight(0);
            layout.alignItems(AlignItems.CENTER);
            layout.justifyContent(AlignContent.CENTER);
        }).addChild(panel);
    }

    private static UIElement createCell(UIElement child, int left, int top, int width, int height) {
        return new UIElement().layout(layout -> {
            layout.positionType(TaffyPosition.ABSOLUTE);
            layout.left(left);
            layout.top(top);
            layout.width(width);
            layout.height(height);
        }).addChild(child);
    }
}
