package com.viscript_recipe.gui.editor;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.viscript_recipe.ViScriptRecipe;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.TaffyPosition;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

final class VanillaSmithingCanvasFactory {
    private static final int JEI_WIDTH = 108;
    private static final int JEI_HEIGHT = 28;
    private static final int SLOT_SIZE = 18;

    private VanillaSmithingCanvasFactory() {
    }

    static UIElement createCanvas(
            UIElement templateSlot,
            UIElement baseSlot,
            UIElement additionSlot,
            UIElement outputSlot
    ) {
        var panel = new UIElement().layout(layout -> {
            layout.width(JEI_WIDTH);
            layout.height(JEI_HEIGHT);
            layout.positionType(TaffyPosition.RELATIVE);
        }).addChildren(
                positioned(templateSlot, 1, 6, SLOT_SIZE, SLOT_SIZE),
                positioned(baseSlot, 19, 6, SLOT_SIZE, SLOT_SIZE),
                positioned(additionSlot, 37, 6, SLOT_SIZE, SLOT_SIZE),
                textureElement(jeiTexture("recipe_arrow.png"), 61, 6, 22, 16),
                positioned(outputSlot, 91, 6, SLOT_SIZE, SLOT_SIZE)
        );
        return RecipeEditorUi.row().layout(layout -> {
            layout.widthPercent(100);
            layout.flex(1);
            layout.minWidth(0);
            layout.minHeight(0);
            layout.alignItems(AlignItems.CENTER);
            layout.justifyContent(AlignContent.CENTER);
        }).addChild(panel);
    }

    static UIElement createSlotCell(UIElement slot) {
        return new UIElement().layout(layout -> {
            layout.width(SLOT_SIZE);
            layout.height(SLOT_SIZE);
        }).style(style -> style.backgroundTexture(sprite(jeiTexture("slot.png"), SLOT_SIZE, SLOT_SIZE)))
                .addChild(slot);
    }

    static boolean hasJeiSkin() {
        if (!ViScriptRecipe.isModLoaded("jei")) {
            return false;
        }
        var resources = Minecraft.getInstance().getResourceManager();
        return resources.getResource(jeiTexture("slot.png")).isPresent()
                && resources.getResource(jeiTexture("recipe_arrow.png")).isPresent();
    }

    private static UIElement positioned(UIElement element, int left, int top, int width, int height) {
        element.layout(layout -> {
            layout.positionType(TaffyPosition.ABSOLUTE);
            layout.left(left);
            layout.top(top);
            layout.width(width);
            layout.height(height);
        });
        return element;
    }

    private static UIElement textureElement(ResourceLocation texture, int left, int top, int width, int height) {
        return positioned(new UIElement(), left, top, width, height)
                .style(style -> style.backgroundTexture(sprite(texture, width, height)));
    }

    private static IGuiTexture sprite(ResourceLocation texture, int width, int height) {
        return SpriteTexture.of(texture).setSprite(0, 0, width, height);
    }

    private static ResourceLocation jeiTexture(String path) {
        return new ResourceLocation("jei", "textures/jei/atlas/gui/" + path);
    }
}
