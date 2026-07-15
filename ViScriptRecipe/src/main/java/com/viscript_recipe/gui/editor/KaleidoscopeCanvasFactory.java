package com.viscript_recipe.gui.editor;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.TaffyPosition;
import net.minecraft.resources.ResourceLocation;

final class KaleidoscopeCanvasFactory {
    private static final int JEI_SLOT_SIZE = 18;

    private KaleidoscopeCanvasFactory() {
    }

    static UIElement createPotCanvas(UIElement[] ingredientSlots, UIElement carrierSlot, UIElement resultSlot, Label stirFryLabel) {
        var panel = createPanel("pot", 176, 102);
        addGridSlots(panel, ingredientSlots, 15, 24);
        panel.addChild(createCell(carrierSlot, 133, 18));
        panel.addChild(createCell(resultSlot, 143, 60));
        panel.addChild(createLabelCell(stirFryLabel, 0, 85, 176, 12));
        return centerPanel(panel);
    }

    static UIElement createStockpotCanvas(UIElement[] ingredientSlots, UIElement carrierSlot, UIElement resultSlot) {
        var panel = createPanel("stockpot", 176, 102);
        addGridSlots(panel, ingredientSlots, 15, 25);
        panel.addChild(createCell(carrierSlot, 133, 18));
        panel.addChild(createCell(resultSlot, 143, 60));
        return centerPanel(panel);
    }

    static UIElement createMillstoneCanvas(UIElement inputSlot, UIElement resultSlot) {
        var panel = createPanel("millstone", 176, 95);
        panel.addChild(createCell(inputSlot, 69, 39));
        panel.addChild(createCell(resultSlot, 146, 47));
        return centerPanel(panel);
    }

    static UIElement createChoppingBoardCanvas(UIElement inputSlot, UIElement resultSlot) {
        return createSingleInputCanvas("chopping_board", inputSlot, resultSlot);
    }

    static UIElement createSteamerCanvas(UIElement inputSlot, UIElement resultSlot) {
        return createSingleInputCanvas("steamer", inputSlot, resultSlot);
    }

    static UIElement createTeapotCanvas(UIElement fluidBucketSlot, UIElement inputSlot, UIElement resultSlot, Label timeLabel) {
        var panel = createPanel("teapot", 176, 78);
        panel.addChild(createSlotBackgroundCell(fluidBucketSlot, 65, 3));
        panel.addChild(createSlotBackgroundCell(inputSlot, 83, 3));
        panel.addChild(createCell(resultSlot, 128, 30));
        panel.addChild(createLabelCell(timeLabel, 0, 70, 176, 10));
        return centerPanel(panel);
    }

    private static UIElement createSingleInputCanvas(String texturePath, UIElement inputSlot, UIElement resultSlot) {
        var panel = createPanel(texturePath, 176, 78);
        panel.addChild(createCell(inputSlot, 38, 27));
        panel.addChild(createCell(resultSlot, 128, 30));
        return centerPanel(panel);
    }

    private static UIElement createPanel(String texturePath, int width, int height) {
        return new UIElement().layout(layout -> {
            layout.width(width);
            layout.height(height);
            layout.positionType(TaffyPosition.RELATIVE);
        }).style(style -> style.backgroundTexture(jeiTexture(texturePath, width, height)));
    }

    private static IGuiTexture jeiTexture(String path, int width, int height) {
        return SpriteTexture.of(new ResourceLocation(
                "kaleidoscope_cookery",
                "textures/gui/jei/" + path + ".png"
        )).setSprite(0, 0, width, height);
    }

    private static UIElement centerPanel(UIElement panel) {
        return RecipeEditorUi.row().layout(layout -> {
            layout.widthPercent(100);
            layout.flex(1);
            layout.alignItems(AlignItems.CENTER);
            layout.justifyContent(AlignContent.CENTER);
        }).addChild(panel);
    }

    private static void addGridSlots(UIElement panel, UIElement[] slots, int left, int top) {
        for (int i = 0; i < slots.length; i++) {
            panel.addChild(createCell(
                    slots[i],
                    left + (i % 3) * JEI_SLOT_SIZE,
                    top + (i / 3) * JEI_SLOT_SIZE
            ));
        }
    }

    private static UIElement createCell(UIElement slot, int left, int top) {
        return new UIElement().layout(layout -> {
            layout.positionType(TaffyPosition.ABSOLUTE);
            layout.left(left);
            layout.top(top);
            layout.width(JEI_SLOT_SIZE);
            layout.height(JEI_SLOT_SIZE);
        }).addChild(slot);
    }

    private static UIElement createSlotBackgroundCell(UIElement slot, int left, int top) {
        return createCell(slot, left, top)
                .style(style -> style.backgroundTexture(ItemSlot.ITEM_SLOT_TEXTURE));
    }

    private static UIElement createLabelCell(Label label, int left, int top, int width, int height) {
        label.layout(layout -> {
            layout.width(width);
            layout.height(height);
        });
        return new UIElement().layout(layout -> {
            layout.positionType(TaffyPosition.ABSOLUTE);
            layout.left(left);
            layout.top(top);
            layout.width(width);
            layout.height(height);
        }).addChild(label);
    }
}
