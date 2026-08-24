package com.viscript_recipe.compat.create.canvas;

import com.lowdragmc.lowdraglib2.gui.ColorPattern;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.Icons;
import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.Vertical;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.rendering.GUIContext;
import com.simibubi.create.compat.jei.category.animations.AnimatedCrafter;
import com.viscript_recipe.ViScriptRecipe;
import com.viscript_recipe.gui.editor.RecipeEditorUi;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.TaffyPosition;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public final class CreateMechanicalCraftingCanvasFactory {
    private static final String CREATE_MOD_ID = "create";
    private static final String JEI_MOD_ID = "jei";
    private static final ResourceLocation CREATE_JEI_WIDGETS = new ResourceLocation(
            CREATE_MOD_ID,
            "textures/gui/jei/widgets.png"
    );
    private static final int PROCESS_WIDTH = 72;
    private static final int PROCESS_HEIGHT = 107;

    private CreateMechanicalCraftingCanvasFactory() {
    }

    public static UIElement createCanvas(
            UIElement grid,
            UIElement fallbackMachineIcon,
            Label ingredientCountLabel,
            UIElement fallbackOutputSlot,
            UIElement jeiOutputSlot
    ) {
        UIElement fallbackProcess = null; UIElement jeiProcess = null;
        if (fallbackOutputSlot != null) fallbackProcess = createFallbackProcess(fallbackMachineIcon, fallbackOutputSlot);
        if (jeiOutputSlot != null) jeiProcess = createJeiProcess(ingredientCountLabel, jeiOutputSlot);

        var processStack = new UIElement().layout(layout -> {
            layout.width(PROCESS_WIDTH);
            layout.height(PROCESS_HEIGHT);
        });
        if (fallbackProcess != null) processStack.addChild(fallbackProcess);
        if (jeiProcess != null) processStack.addChild(jeiProcess);

        return RecipeEditorUi.row().layout(layout -> {
            layout.widthPercent(100);
            layout.flex(1);
            layout.minWidth(0);
            layout.minHeight(0);
            layout.gapAll(18);
            layout.alignItems(AlignItems.CENTER);
            layout.justifyContent(AlignContent.CENTER);
        }).addChildren(grid, processStack);
    }

    public static boolean hasJeiSkin() {
        return ViScriptRecipe.isModLoaded(CREATE_MOD_ID)
                && ViScriptRecipe.isModLoaded(JEI_MOD_ID)
                && hasCreateWidgets();
    }

    static void configureJeiOutputCell(UIElement cell) {
        cell.style(style -> style.backgroundTexture(
                SpriteTexture.of(CREATE_JEI_WIDGETS).setSprite(0, 0, 18, 18)
        ));
    }

    public static void configureGridCell(UIElement cell, ItemSlot slot, boolean useJeiSkin) {
        cell.style(style -> style.backgroundTexture(useJeiSkin
                ? SpriteTexture.of(CREATE_JEI_WIDGETS).setSprite(0, 0, 18, 18)
                : IGuiTexture.EMPTY));
        slot.style(style -> style.backgroundTexture(useJeiSkin
                ? IGuiTexture.EMPTY
                : ItemSlot.ITEM_SLOT_TEXTURE));
        slot.slotStyle(style -> style.slotOverlay(IGuiTexture.EMPTY));
    }

    private static UIElement createJeiProcess(Label ingredientCountLabel, UIElement outputSlot) {
        ingredientCountLabel.textStyle(style -> style
                .fontSize(9)
                .textColor(ColorPattern.WHITE.color)
                .textAlignHorizontal(Horizontal.CENTER)
                .textAlignVertical(Vertical.CENTER));
        var outputCell = positioned(new UIElement(), 21, 80, 18, 18).addChild(outputSlot);
        configureJeiOutputCell(outputCell);
        var process = processPanel().addChildren(
                positioned(new CreateMechanicalCrafterElement(), 0, 0, PROCESS_WIDTH, 59),
                positioned(ingredientCountLabel, 24, 2, 24, 10),
                createDownArrow(16, 59),
                outputCell
        );
        process.setOverflowVisible(true);
        return process;
    }

    private static UIElement createFallbackProcess(UIElement machineIcon, UIElement outputSlot) {
        return processPanel().addChildren(
                positioned(machineIcon, 0, 1, PROCESS_WIDTH, 55),
                createDownArrow(27, 59),
                positioned(outputSlot, 21, 77, 30, 30)
        );
    }

    private static UIElement processPanel() {
        return new UIElement().layout(layout -> {
            layout.positionType(TaffyPosition.ABSOLUTE);
            layout.left(0);
            layout.top(0);
            layout.width(PROCESS_WIDTH);
            layout.height(PROCESS_HEIGHT);
        });
    }

    private static UIElement createDownArrow(int left, int top) {
        if (hasCreateWidgets()) {
            return positioned(new UIElement(), left, top, 18, 14)
                    .style(style -> style.backgroundTexture(
                            SpriteTexture.of(CREATE_JEI_WIDGETS).setSprite(0, 21, 18, 14)
                    ));
        }
        return positioned(new UIElement(), left - 3, top - 2, 24, 18)
                .style(style -> style.backgroundTexture(Icons.DOWN_ARROW_NO_BAR));
    }

    private static boolean hasCreateWidgets() {
        return ViScriptRecipe.isModLoaded(CREATE_MOD_ID) && ViScriptRecipe.isPresentResource(CREATE_JEI_WIDGETS);
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

    static class CreateMechanicalCrafterElement extends UIElement {
        private final AnimatedCrafter crafter = new AnimatedCrafter();

        CreateMechanicalCrafterElement() {setOverflowVisible(true);}

        @Override
        public void drawBackgroundAdditional(@NotNull GUIContext guiContext) {
            super.drawBackgroundAdditional(guiContext);
            guiContext.graphics.flush();
            crafter.draw(
                    guiContext.graphics,
                    Math.round(getPositionX() + 17),
                    Math.round(getPositionY() + 25)
            );
            guiContext.graphics.flush();
        }
    }
}
