package com.viscript_recipe.gui.canvas.vanilla;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.viscript_recipe.ViScriptRecipe;
import com.viscript_recipe.gui.editor.RecipeEditorUi;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.TaffyPosition;
import net.minecraft.resources.ResourceLocation;

public final class VanillaStonecuttingCanvasFactory {
    private static final int JEI_WIDTH = 82;
    private static final int JEI_HEIGHT = 34;
    private static final int SLOT_SIZE = 18;
    private static final int OUTPUT_SLOT_SIZE = 26;

    private VanillaStonecuttingCanvasFactory() {
    }

    public static UIElement createCanvas(UIElement inputSlot, UIElement outputSlot) {
        var panel = new UIElement().layout(layout -> {
            layout.width(JEI_WIDTH);
            layout.height(JEI_HEIGHT);
            layout.positionType(TaffyPosition.RELATIVE);
        }).addChildren(
                standardSlot(inputSlot, 1, 9),
                textureElement(sprite(jeiTexture("recipe_arrow.png"), 22, 16), 26, 9, 22, 16),
                outputSlot(outputSlot, 61, 9)
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

    public static boolean hasJeiSkin() {
        if (!ViScriptRecipe.isModLoaded("jei")) return false;
        return ViScriptRecipe.isPresentResource(jeiTexture("slot.png"))
                && ViScriptRecipe.isPresentResource(jeiTexture("output_slot.png"))
                && ViScriptRecipe.isPresentResource(jeiTexture("recipe_arrow.png"));
    }

    private static UIElement standardSlot(UIElement slot, int left, int top) {
        return textureElement(sprite(jeiTexture("slot.png"), SLOT_SIZE, SLOT_SIZE), left, top, SLOT_SIZE, SLOT_SIZE)
                .addChild(slot);
    }

    private static UIElement outputSlot(UIElement slot, int left, int top) {
        var itemCell = new UIElement().layout(layout -> {
            layout.positionType(TaffyPosition.ABSOLUTE);
            layout.left(4);
            layout.top(4);
            layout.width(SLOT_SIZE);
            layout.height(SLOT_SIZE);
        }).addChild(slot);
        return textureElement(
                sprite(jeiTexture("output_slot.png"), OUTPUT_SLOT_SIZE, OUTPUT_SLOT_SIZE),
                left,
                top,
                OUTPUT_SLOT_SIZE,
                OUTPUT_SLOT_SIZE
        ).addChild(itemCell);
    }

    private static UIElement textureElement(IGuiTexture texture, int left, int top, int width, int height) {
        return new UIElement().layout(layout -> {
            layout.positionType(TaffyPosition.ABSOLUTE);
            layout.left(left);
            layout.top(top);
            layout.width(width);
            layout.height(height);
        }).style(style -> style.backgroundTexture(texture));
    }

    private static SpriteTexture sprite(ResourceLocation texture, int width, int height) {
        return SpriteTexture.of(texture).setSprite(0, 0, width, height);
    }

    private static ResourceLocation jeiTexture(String path) {
        return new ResourceLocation("jei", "textures/jei/atlas/gui/" + path);
    }
}
