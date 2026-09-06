package com.viscript_recipe.compat.avaritia.canvas;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.viscript_recipe.gui.editor.RecipeEditorUi;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.TaffyPosition;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class AvaritiaCanvasFactory {
    private static final int JEI_SLOT_SIZE = 18;

    private AvaritiaCanvasFactory() {
    }

    public static UIElement createCompressorCanvas(UIElement ingredientSlot, UIElement outputSlot) {
        var panel = createJeiPanel("compressor", 170, 63);
        panel.addChildren(
                createFloatingCell(ingredientSlot, 37, 21, JEI_SLOT_SIZE, JEI_SLOT_SIZE),
                createFloatingCell(outputSlot, 117, 21, JEI_SLOT_SIZE, JEI_SLOT_SIZE)
        );
        return centerPanel(panel);
    }

    public static UIElement createExtremeSmithingCanvas(UIElement[] ingredientSlots, UIElement outputSlot) {
        var panel = createJeiPanel("extreme_smithing_jei", 170, 64);
        panel.addChildren(
                createSmithingIngredientCell(ingredientSlots[0], 0, 27, 23),
                createSmithingIngredientCell(ingredientSlots[1], 1, 45, 23),
                createSmithingIngredientCell(ingredientSlots[2], 2, 45, 5),
                createSmithingIngredientCell(ingredientSlots[3], 3, 63, 23),
                createSmithingIngredientCell(ingredientSlots[4], 4, 45, 41),
                createFloatingCell(outputSlot, 117, 23, JEI_SLOT_SIZE, JEI_SLOT_SIZE)
                        .style(style -> style.tooltips(Component.translatable("viscript_recipe.editor.result_slot")))
        );
        return centerPanel(panel);
    }

    private static UIElement createJeiPanel(String texturePath, int width, int height) {
        return new UIElement().layout(layout -> {
            layout.width(width);
            layout.height(height);
            layout.positionType(TaffyPosition.RELATIVE);
        }).style(style -> style.backgroundTexture(jeiTexture(texturePath, width, height)));
    }

    private static IGuiTexture jeiTexture(String path, int width, int height) {
        return SpriteTexture.of(new ResourceLocation(
                "avaritia",
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

    private static UIElement createSmithingIngredientCell(UIElement slot, int index, int left, int top) {
        return createFloatingCell(slot, left, top, JEI_SLOT_SIZE, JEI_SLOT_SIZE)
                .style(style -> style.tooltips(Component.translatable(
                        "viscript_recipe.editor.avaritia.extreme_smithing.slot." + index
                )));
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
}
