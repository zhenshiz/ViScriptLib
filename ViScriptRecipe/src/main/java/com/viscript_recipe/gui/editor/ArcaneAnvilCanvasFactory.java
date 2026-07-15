package com.viscript_recipe.gui.editor;

import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.viscript_recipe.ViScriptRecipe;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.TaffyPosition;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

final class ArcaneAnvilCanvasFactory {
    private static final ResourceLocation JEI_BACKGROUND = new ResourceLocation(
            "irons_spellbooks",
            "textures/gui/gui_vanilla.png"
    );
    private static final int JEI_WIDTH = 125;
    private static final int JEI_BACKGROUND_HEIGHT = 18;
    private static final int JEI_HEIGHT = 33;
    private static final int SLOT_SIZE = 18;

    private ArcaneAnvilCanvasFactory() {
    }

    static UIElement createCanvas(UIElement leftInput, UIElement rightInput, UIElement output) {
        var background = new UIElement().layout(layout -> {
            layout.positionType(TaffyPosition.ABSOLUTE);
            layout.left(0);
            layout.top(0);
            layout.width(JEI_WIDTH);
            layout.height(JEI_BACKGROUND_HEIGHT);
        }).style(style -> style.backgroundTexture(
                SpriteTexture.of(JEI_BACKGROUND).setSprite(0, 168, JEI_WIDTH, JEI_BACKGROUND_HEIGHT)
        ));
        var panel = new UIElement().layout(layout -> {
            layout.width(JEI_WIDTH);
            layout.height(JEI_HEIGHT);
            layout.positionType(TaffyPosition.RELATIVE);
        }).addChildren(
                background,
                positionedSlot(leftInput, 0),
                positionedSlot(rightInput, 49),
                positionedSlot(output, 107)
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

    static boolean hasJeiSkin() {
        return ViScriptRecipe.isModLoaded("jei")
                && ViScriptRecipe.isModLoaded("irons_spellbooks")
                && Minecraft.getInstance().getResourceManager().getResource(JEI_BACKGROUND).isPresent();
    }

    private static UIElement positionedSlot(UIElement slot, int left) {
        return new UIElement().layout(layout -> {
            layout.positionType(TaffyPosition.ABSOLUTE);
            layout.left(left);
            layout.top(0);
            layout.width(SLOT_SIZE);
            layout.height(SLOT_SIZE);
        }).addChild(slot);
    }
}
