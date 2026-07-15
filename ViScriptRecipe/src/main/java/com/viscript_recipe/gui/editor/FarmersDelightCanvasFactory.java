package com.viscript_recipe.gui.editor;

import com.lowdragmc.lowdraglib2.gui.ColorPattern;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.Icons;
import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.styletemplate.Sprites;
import com.viscript_recipe.ViScriptRecipe;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.TaffyPosition;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

final class FarmersDelightCanvasFactory {
    private static final ResourceLocation JEI_COOKING_POT = new ResourceLocation(
            "farmersdelight",
            "textures/gui/jei/cooking_pot.png"
    );
    private static final ResourceLocation COOKING_POT_SCREEN = new ResourceLocation(
            "farmersdelight",
            "textures/gui/cooking_pot.png"
    );
    private static final ResourceLocation JEI_CUTTING_BOARD = new ResourceLocation(
            "farmersdelight",
            "textures/gui/jei/cutting_board.png"
    );

    private FarmersDelightCanvasFactory() {
    }

    static UIElement createJeiCookingPotCanvas(
            UIElement[] ingredientSlots,
            UIElement potPreviewSlot,
            UIElement containerSlot,
            UIElement outputSlot,
            UIElement timeIcon,
            UIElement experienceIcon
    ) {
        var panel = new UIElement().layout(layout -> {
            layout.width(116);
            layout.height(56);
            layout.positionType(TaffyPosition.RELATIVE);
        }).style(style -> style.backgroundTexture(sprite(JEI_COOKING_POT, 0, 0, 116, 56)));

        for (int index = 0; index < ingredientSlots.length; index++) {
            panel.addChild(positioned(ingredientSlots[index], index % 3 * 18 + 1, index / 3 * 18 + 1, 18, 18));
        }
        panel.addChildren(
                textureElement(sprite(COOKING_POT_SCREEN, 176, 15, 24, 17), 60, 9, 24, 17),
                textureElement(sprite(COOKING_POT_SCREEN, 176, 0, 17, 15), 18, 39, 17, 15)
                        .style(style -> style.tooltips(Component.translatable(
                                "viscript_recipe.editor.farmersdelight.cooking.heat_source"
                        ))),
                configureTextureElement(timeIcon, sprite(COOKING_POT_SCREEN, 176, 32, 8, 11), 64, 2, 8, 11),
                configureTextureElement(experienceIcon, sprite(COOKING_POT_SCREEN, 176, 43, 9, 9), 63, 21, 9, 9),
                positioned(potPreviewSlot, 95, 10, 18, 18),
                positioned(containerSlot, 63, 39, 18, 18),
                positioned(outputSlot, 95, 39, 18, 18)
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

    static boolean hasJeiCookingPotSkin() {
        if (!ViScriptRecipe.isModLoaded("jei") || !ViScriptRecipe.isModLoaded("farmersdelight")) {
            return false;
        }
        var resources = Minecraft.getInstance().getResourceManager();
        return resources.getResource(JEI_COOKING_POT).isPresent()
                && resources.getResource(COOKING_POT_SCREEN).isPresent();
    }

    static UIElement createJeiCuttingBoardCanvas(
            UIElement inputSlot,
            UIElement toolSlot,
            UIElement[] resultSlots,
            UIElement[] resultCells
    ) {
        var panel = new UIElement().layout(layout -> {
            layout.width(117);
            layout.height(57);
            layout.positionType(TaffyPosition.RELATIVE);
        }).style(style -> style.backgroundTexture(sprite(JEI_CUTTING_BOARD, 0, 0, 117, 57)));

        panel.addChildren(
                positioned(toolSlot, 16, 8, 18, 18),
                positioned(inputSlot, 16, 27, 18, 18)
        );
        for (int index = 0; index < resultSlots.length; index++) {
            var cell = new UIElement().addChild(resultSlots[index]);
            resultCells[index] = cell;
            panel.addChild(cell);
        }

        return RecipeEditorUi.row().layout(layout -> {
            layout.widthPercent(100);
            layout.flex(1);
            layout.minWidth(0);
            layout.minHeight(0);
            layout.alignItems(AlignItems.CENTER);
            layout.justifyContent(AlignContent.CENTER);
        }).addChild(panel);
    }

    static void updateJeiCuttingResultCells(UIElement[] resultCells, int visibleCount, boolean[] chanceResults) {
        var count = Math.max(1, Math.min(4, visibleCount));
        var offsetX = count > 1 ? 1 : 10;
        var offsetY = count > 2 ? 1 : 10;
        for (int index = 0; index < resultCells.length; index++) {
            var cell = resultCells[index];
            cell.setDisplay(index < count);
            if (index >= count) {
                continue;
            }
            var slotTexture = sprite(
                    JEI_CUTTING_BOARD,
                    chanceResults[index] ? 18 : 0,
                    58,
                    18,
                    18
            );
            positioned(
                    cell,
                    76 + offsetX + index % 2 * 19,
                    10 + offsetY + index / 2 * 19,
                    18,
                    18
            ).style(style -> style.backgroundTexture(slotTexture));
        }
    }

    static boolean hasJeiCuttingBoardSkin() {
        if (!ViScriptRecipe.isModLoaded("jei") || !ViScriptRecipe.isModLoaded("farmersdelight")) {
            return false;
        }
        return Minecraft.getInstance().getResourceManager().getResource(JEI_CUTTING_BOARD).isPresent();
    }

    static UIElement createCookingPotCanvas(UIElement ingredientGrid, UIElement heatSource, UIElement potPreview, UIElement servingRow) {
        return RecipeEditorUi.row().layout(layout -> {
            layout.widthPercent(100);
            layout.flex(1);
            layout.gapAll(18);
            layout.alignItems(AlignItems.CENTER);
            layout.justifyContent(AlignContent.CENTER);
        }).addChildren(
                RecipeEditorUi.column().layout(layout -> {
                    layout.width(116);
                    layout.gapAll(9);
                    layout.alignItems(AlignItems.CENTER);
                }).addChildren(ingredientGrid, heatSource),
                RecipeEditorUi.column().layout(layout -> {
                    layout.width(78);
                    layout.height(128);
                    layout.alignItems(AlignItems.CENTER);
                    layout.justifyContent(AlignContent.CENTER);
                }).addChild(rotatedArrow(Icons.UP_ARROW_NO_BAR, 30, 60)),
                RecipeEditorUi.column().layout(layout -> {
                    layout.width(134);
                    layout.height(138);
                    layout.gapAll(8);
                    layout.alignItems(AlignItems.CENTER);
                    layout.justifyContent(AlignContent.CENTER);
                }).addChildren(potPreview, servingRow)
        );
    }

    static UIElement createHeatSource(UIElement icon) {
        return RecipeEditorUi.row().layout(layout -> {
            layout.width(86);
            layout.height(30);
            layout.alignItems(AlignItems.CENTER);
            layout.justifyContent(AlignContent.CENTER);
        }).style(style -> style.tooltips(Component.translatable("viscript_recipe.editor.farmersdelight.cooking.heat_source")))
                .addChild(icon);
    }

    static UIElement createPotPreview(UIElement potIcon, UIElement previewSlot) {
        return RecipeEditorUi.column().layout(layout -> {
            layout.width(88);
            layout.height(72);
            layout.paddingAll(5);
            layout.gapAll(4);
            layout.alignItems(AlignItems.CENTER);
        }).style(style -> style
                .backgroundTexture(Sprites.BORDER_DARK)
                .tooltips(Component.translatable("viscript_recipe.editor.farmersdelight.cooking.pot_preview"))
        ).addChildren(
                RecipeEditorUi.row().layout(layout -> {
                    layout.widthPercent(100);
                    layout.height(12);
                    layout.alignItems(AlignItems.CENTER);
                    layout.justifyContent(AlignContent.CENTER);
                }).addChild(potIcon),
                previewSlot
        );
    }

    static UIElement createServingRow(UIElement containerSlot, UIElement outputSlot) {
        return RecipeEditorUi.row().layout(layout -> {
            layout.width(130);
            layout.height(34);
            layout.gapAll(8);
            layout.alignItems(AlignItems.CENTER);
            layout.justifyContent(AlignContent.CENTER);
        }).addChildren(
                containerSlot,
                rotatedArrow(Icons.RIGHT_ARROW_NO_BAR, 24, 16),
                outputSlot
        );
    }

    static UIElement createCuttingBoardCanvas(UIElement inputColumn, UIElement toolColumn, UIElement resultGrid) {
        return RecipeEditorUi.row().layout(layout -> {
            layout.widthPercent(100);
            layout.flex(1);
            layout.gapAll(16);
            layout.alignItems(AlignItems.CENTER);
            layout.justifyContent(AlignContent.CENTER);
        }).addChildren(
                inputColumn,
                operatorPlusLabel(),
                toolColumn,
                arrowElement(),
                RecipeEditorUi.column().layout(layout -> {
                    layout.width(86);
                    layout.gapAll(4);
                    layout.alignItems(AlignItems.CENTER);
                }).addChildren(
                        RecipeEditorUi.label(Component.translatable("viscript_recipe.editor.farmersdelight.cutting.results")),
                        resultGrid
                )
        );
    }

    static UIElement createCuttingInput(String labelKey, UIElement slot) {
        return RecipeEditorUi.column().layout(layout -> {
            layout.width(72);
            layout.gapAll(4);
            layout.alignItems(AlignItems.CENTER);
        }).addChildren(
                RecipeEditorUi.label(Component.translatable(labelKey)),
                slot
        );
    }

    private static Label operatorPlusLabel() {
        Label label = RecipeEditorUi.label(Component.literal("+"));
        label.textStyle(style -> style.fontSize(22).textColor(ColorPattern.GRAY.color));
        label.layout(layout -> layout.width(22).height(24));
        return label;
    }

    private static UIElement arrowElement() {
        return new UIElement().layout(layout -> {
            layout.width(28);
            layout.height(16);
        }).style(style -> style.backgroundTexture(Icons.ARROW_LEFT_RIGHT));
    }

    private static UIElement rotatedArrow(IGuiTexture texture, int width, int height) {
        return new UIElement().layout(layout -> {
            layout.width(width);
            layout.height(height);
        }).style(style -> style.backgroundTexture(texture.copy().rotate(-90)));
    }

    private static UIElement positioned(UIElement element, int left, int top, int width, int height) {
        return element.layout(layout -> {
            layout.positionType(TaffyPosition.ABSOLUTE);
            layout.left(left);
            layout.top(top);
            layout.width(width);
            layout.height(height);
        });
    }

    private static UIElement textureElement(IGuiTexture texture, int left, int top, int width, int height) {
        return configureTextureElement(new UIElement(), texture, left, top, width, height);
    }

    private static UIElement configureTextureElement(
            UIElement element,
            IGuiTexture texture,
            int left,
            int top,
            int width,
            int height
    ) {
        return positioned(element, left, top, width, height)
                .style(style -> style.backgroundTexture(texture));
    }

    private static SpriteTexture sprite(ResourceLocation texture, int left, int top, int width, int height) {
        return SpriteTexture.of(texture).setSprite(left, top, width, height);
    }
}
