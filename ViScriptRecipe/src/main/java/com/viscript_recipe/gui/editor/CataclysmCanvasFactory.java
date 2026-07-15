package com.viscript_recipe.gui.editor;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.TaffyPosition;
import net.minecraft.resources.ResourceLocation;

/**
 * Builds Cataclysm recipe canvases using the same textures and coordinates as its JEI categories.
 */
final class CataclysmCanvasFactory {
    private static final String MOD_ID = "cataclysm";
    private static final int JEI_SLOT_SIZE = 18;

    private CataclysmCanvasFactory() {
    }

    static UIElement createWeaponFusionCanvas(UIElement baseSlot, UIElement additionSlot, UIElement resultSlot) {
        var panel = createPanel(weaponFusionTexture(), 125, 18);
        panel.addChildren(
                createCell(baseSlot, 1, 1),
                createCell(additionSlot, 50, 1),
                createCell(resultSlot, 108, 1)
        );
        return centerPanel(panel);
    }

    static UIElement createAmethystBlessCanvas(UIElement ingredientSlot, UIElement resultSlot) {
        var panel = createPanel(amethystBlessTexture(), 125, 59);
        panel.addChildren(
                createCell(ingredientSlot, 21, 23),
                createCell(resultSlot, 94, 23)
        );
        return centerPanel(panel);
    }

    private static UIElement createPanel(IGuiTexture texture, int width, int height) {
        return new UIElement().layout(layout -> {
            layout.width(width);
            layout.height(height);
            layout.positionType(TaffyPosition.RELATIVE);
        }).style(style -> style.backgroundTexture(texture));
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

    private static UIElement centerPanel(UIElement panel) {
        return RecipeEditorUi.row().layout(layout -> {
            layout.widthPercent(100);
            layout.flex(1);
            layout.minWidth(0);
            layout.minHeight(0);
            layout.alignItems(AlignItems.CENTER);
            layout.justifyContent(AlignContent.CENTER);
        }).addChild(panel);
    }

    private static IGuiTexture weaponFusionTexture() {
        return SpriteTexture.of(new ResourceLocation(
                MOD_ID,
                "textures/gui/fusion.png"
        )).setSprite(26, 46, 125, 18);
    }

    private static IGuiTexture amethystBlessTexture() {
        return SpriteTexture.of(new ResourceLocation(
                MOD_ID,
                "textures/gui/altar_of_amethyst_jei.png"
        )).setSprite(0, 0, 125, 59);
    }

}
