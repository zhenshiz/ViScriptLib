package com.viscript_recipe.gui.editor;

import com.lowdragmc.lowdraglib2.gui.ColorPattern;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.Icons;
import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.Vertical;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.viscript_recipe.ViScriptRecipe;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.TaffyPosition;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

final class CreateMechanicalCraftingCanvasFactory {
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

    static Canvas createCanvas(
            UIElement grid,
            UIElement fallbackMachineIcon,
            Label ingredientCountLabel,
            UIElement fallbackOutputSlot,
            UIElement jeiOutputSlot,
            boolean useJeiSkin
    ) {
        var fallbackProcess = createFallbackProcess(fallbackMachineIcon, fallbackOutputSlot);
        var jeiProcess = useJeiSkin
                ? createJeiProcess(ingredientCountLabel, jeiOutputSlot)
                : hiddenPlaceholder();
        fallbackProcess.setDisplay(!useJeiSkin);
        jeiProcess.setDisplay(useJeiSkin);

        var processStack = new UIElement().layout(layout -> {
            layout.width(PROCESS_WIDTH);
            layout.height(PROCESS_HEIGHT);
        }).addChildren(fallbackProcess, jeiProcess);
        var root = RecipeEditorUi.row().layout(layout -> {
            layout.widthPercent(100);
            layout.flex(1);
            layout.minWidth(0);
            layout.minHeight(0);
            layout.gapAll(18);
            layout.alignItems(AlignItems.CENTER);
            layout.justifyContent(AlignContent.CENTER);
        }).addChildren(grid, processStack);
        return new Canvas(root, fallbackProcess, jeiProcess);
    }

    static boolean hasJeiSkin() {
        return ViScriptRecipe.isModLoaded(CREATE_MOD_ID)
                &&ViScriptRecipe.isModLoaded(JEI_MOD_ID)
                && hasCreateWidgets();
    }

    static void configureJeiOutputCell(UIElement cell) {
        cell.style(style -> style.backgroundTexture(
                SpriteTexture.of(CREATE_JEI_WIDGETS).setSprite(0, 0, 18, 18)
        ));
    }

    static void configureGridCell(UIElement cell, ItemSlot slot, boolean useJeiSkin) {
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
        return ViScriptRecipe.isModLoaded(CREATE_MOD_ID)
                && Minecraft.getInstance().getResourceManager().getResource(CREATE_JEI_WIDGETS).isPresent();
    }

    private static UIElement hiddenPlaceholder() {
        var placeholder = processPanel();
        placeholder.setDisplay(false);
        return placeholder;
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

    record Canvas(UIElement root, UIElement fallbackProcess, UIElement jeiProcess) {
    }
}
