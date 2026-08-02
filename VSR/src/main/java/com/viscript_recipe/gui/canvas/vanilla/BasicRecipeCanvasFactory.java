package com.viscript_recipe.gui.canvas.vanilla;

import com.lowdragmc.lowdraglib2.gui.ColorPattern;
import com.lowdragmc.lowdraglib2.gui.texture.Icons;
import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.viscript_recipe.ViScriptRecipe;
import com.viscript_recipe.gui.editor.RecipeEditorUi;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class BasicRecipeCanvasFactory {
    private static final String JEI_MOD_ID = "jei";
    private static final ResourceLocation JEI_RECIPE_ARROW = new ResourceLocation(
            JEI_MOD_ID,
            "textures/jei/atlas/gui/recipe_arrow.png"
    );

    private BasicRecipeCanvasFactory() {
    }

    public static UIElement createCraftingCanvas(UIElement grid, UIElement outputSlot) {
        return createGridResultCanvas(grid, 24, outputSlot);
    }

    public static UIElement createCookingCanvas(UIElement inputSlot, UIElement outputSlot) {
        return RecipeEditorUi.row().layout(layout -> {
            layout.widthPercent(100);
            layout.flex(1);
            layout.gapAll(22);
            layout.alignItems(AlignItems.CENTER);
            layout.justifyContent(AlignContent.CENTER);
        }).addChildren(
                labeledSlot("viscript_recipe.editor.cooking.ingredient", inputSlot),
                rightLeftArrow(28, 16),
                resultColumn(outputSlot)
        );
    }

    public static UIElement createSmithingCanvas(UIElement templateInput, UIElement baseInput, UIElement additionInput, UIElement outputSlot) {
        return RecipeEditorUi.row().layout(layout -> {
            layout.widthPercent(100);
            layout.flex(1);
            layout.gapAll(12);
            layout.alignItems(AlignItems.CENTER);
            layout.justifyContent(AlignContent.CENTER);
        }).addChildren(
                templateInput,
                baseInput,
                additionInput,
                rightLeftArrow(28, 16),
                resultColumn(outputSlot)
        );
    }

    private static UIElement createGridResultCanvas(UIElement grid, int gap, UIElement outputSlot) {
        return RecipeEditorUi.row().layout(layout -> {
            layout.widthPercent(100);
            layout.flex(1);
            layout.gapAll(gap);
            layout.alignItems(AlignItems.CENTER);
            layout.justifyContent(AlignContent.CENTER);
        }).addChildren(
                grid,
                craftingArrow(),
                resultColumn(outputSlot)
        );
    }

    private static UIElement craftingArrow() {
        if (!ViScriptRecipe.isModLoaded(JEI_MOD_ID) || !ViScriptRecipe.isPresentResource(JEI_RECIPE_ARROW)) {
            return rightLeftArrow(28, 16);
        }
        return new UIElement().layout(layout -> {
            layout.width(22);
            layout.height(16);
        }).style(style -> style.backgroundTexture(
                SpriteTexture.of(JEI_RECIPE_ARROW).setSprite(0, 0, 22, 16)
        ));
    }

    private static UIElement labeledSlot(String labelKey, UIElement slot) {
        return RecipeEditorUi.column().layout(layout -> {
            layout.width(72);
            layout.gapAll(4);
            layout.alignItems(AlignItems.CENTER);
        }).addChildren(
                RecipeEditorUi.label(Component.translatable(labelKey)),
                slot
        );
    }

    private static UIElement resultColumn(UIElement outputSlot) {
        return RecipeEditorUi.column().layout(layout -> {
            layout.width(72);
            layout.gapAll(4);
            layout.alignItems(AlignItems.CENTER);
        }).addChildren(
                RecipeEditorUi.label(Component.translatable("viscript_recipe.editor.result")),
                outputSlot
        );
    }

    private static UIElement rightLeftArrow(int width, int height) {
        return new UIElement().layout(layout -> {
            layout.width(width);
            layout.height(height);
        }).style(style -> style.backgroundTexture(Icons.ARROW_LEFT_RIGHT));
    }

    private static UIElement rightArrow(int width, int height) {
        return new UIElement().layout(layout -> {
            layout.width(width);
            layout.height(height);
        }).style(style -> style.backgroundTexture(Icons.DOWN_ARROW_NO_BAR.copy().rotate(-90)));
    }

    private static Label operatorPlusLabel() {
        Label label = RecipeEditorUi.label(Component.literal("+"));
        label.textStyle(style -> style.fontSize(22).textColor(ColorPattern.GRAY.color));
        label.layout(layout -> layout.width(22).height(24));
        return label;
    }
}
