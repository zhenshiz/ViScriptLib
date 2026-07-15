package com.viscript_recipe.gui.editor;

import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.TextWrap;
import com.lowdragmc.lowdraglib2.gui.ui.data.Vertical;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.viscript_recipe.ViScriptRecipe;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.TaffyPosition;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

final class AlchemistCauldronCanvasFactory {
    private static final ResourceLocation JEI_BACKGROUND = new ResourceLocation(
            "irons_spellbooks",
            "textures/gui/jei_alchemist_cauldron.png"
    );
    private static final int JEI_WIDTH = 125;
    private static final int JEI_BACKGROUND_HEIGHT = 19;
    private static final int JEI_HEIGHT = 39;

    private AlchemistCauldronCanvasFactory() {
    }

    static UIElement createCanvas(
            UIElement itemInput,
            UIElement fluidInput,
            UIElement fluidOutput,
            UIElement itemOutput,
            UIElement cauldronIcon,
            Label chanceLabel
    ) {
        var background = positioned(new UIElement(), 0, 0, JEI_WIDTH, JEI_BACKGROUND_HEIGHT)
                .style(style -> style.backgroundTexture(
                        SpriteTexture.of(JEI_BACKGROUND).setSprite(0, 0, JEI_WIDTH, JEI_BACKGROUND_HEIGHT)
                ));
        chanceLabel.textStyle(style -> style
                .fontSize(9)
                .textAlignHorizontal(Horizontal.CENTER)
                .textAlignVertical(Vertical.CENTER)
                .textWrap(TextWrap.HOVER_ROLL));
        var panel = new UIElement().layout(layout -> {
            layout.width(JEI_WIDTH);
            layout.height(JEI_HEIGHT);
            layout.positionType(TaffyPosition.RELATIVE);
        }).addChildren(
                background,
                positioned(itemInput, 1, 1, 16, 16),
                positioned(fluidInput, 54, 1, 16, 16),
                positioned(fluidOutput, 108, 1, 16, 16),
                positioned(itemOutput, 108, 1, 16, 16),
                positioned(cauldronIcon, 51, 17, 23, 22),
                positioned(chanceLabel, 69, 19, 47, 10)
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
}
