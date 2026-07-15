package com.viscript_recipe.gui.editor;

import com.lowdragmc.lowdraglib2.gui.ColorPattern;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
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
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

final class VanillaCookingCanvasFactory {
    private static final int JEI_WIDTH = 82;
    private static final int FURNACE_HEIGHT = 54;
    private static final int CAMPFIRE_HEIGHT = 44;
    private static final int SLOT_SIZE = 18;
    private static final int OUTPUT_SLOT_SIZE = 26;

    private VanillaCookingCanvasFactory() {
    }

    static UIElement createFurnaceCanvas(
            UIElement inputSlot,
            UIElement outputSlot,
            Label experienceLabel,
            Label timeLabel
    ) {
        var skin = createJeiSkin();
        var panel = createPanel(FURNACE_HEIGHT);
        panel.addChildren(
                standardSlot(inputSlot, skin, 1, 1),
                readOnlyFuelSlot(skin, 1, 37),
                textureElement(skin.flame(), 1, 20, 14, 14),
                textureElement(skin.arrow(), 26, 17, 22, 16),
                outputSlot(outputSlot, skin, 61, 19),
                positionedLabel(experienceLabel, skin.textColor(), 0),
                positionedLabel(timeLabel, skin.textColor(), FURNACE_HEIGHT - 10)
        );
        return centerPanel(panel);
    }

    static UIElement createCampfireCanvas(UIElement inputSlot, UIElement outputSlot, Label timeLabel) {
        var skin = createJeiSkin();
        var panel = createPanel(CAMPFIRE_HEIGHT);
        panel.addChildren(
                standardSlot(inputSlot, skin, 1, 1),
                textureElement(skin.flame(), 1, 20, 14, 14),
                textureElement(skin.arrow(), 26, 7, 22, 16),
                outputSlot(outputSlot, skin, 61, 9),
                positionedLabel(timeLabel, skin.textColor(), CAMPFIRE_HEIGHT - 10)
        );
        return centerPanel(panel);
    }

    private static UIElement createPanel(int height) {
        return new UIElement().layout(layout -> {
            layout.width(JEI_WIDTH);
            layout.height(height);
            layout.positionType(TaffyPosition.RELATIVE);
        });
    }

    private static UIElement standardSlot(UIElement slot, CookingSkin skin, int left, int top) {
        return textureElement(skin.slot(), left, top, SLOT_SIZE, SLOT_SIZE).addChild(slot);
    }

    private static UIElement readOnlyFuelSlot(CookingSkin skin, int left, int top) {
        return textureElement(skin.slot(), left, top, SLOT_SIZE, SLOT_SIZE)
                .style(style -> style.tooltips(Component.translatable(
                        "viscript_recipe.editor.cooking.runtime_fuel_slot"
                )));
    }

    private static UIElement outputSlot(UIElement slot, CookingSkin skin, int left, int top) {
        var itemCell = new UIElement().layout(layout -> {
            layout.positionType(TaffyPosition.ABSOLUTE);
            layout.left(4);
            layout.top(4);
            layout.width(SLOT_SIZE);
            layout.height(SLOT_SIZE);
        }).addChild(slot);
        return textureElement(skin.outputSlot(), left, top, OUTPUT_SLOT_SIZE, OUTPUT_SLOT_SIZE)
                .addChild(itemCell);
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

    private static Label positionedLabel(Label label, int textColor, int top) {
        label.textStyle(style -> style
                .textAlignHorizontal(Horizontal.RIGHT)
                .textAlignVertical(Vertical.CENTER)
                .textWrap(TextWrap.HOVER_ROLL)
                .textColor(textColor));
        label.layout(layout -> {
            layout.positionType(TaffyPosition.ABSOLUTE);
            layout.left(20);
            layout.top(top);
            layout.width(JEI_WIDTH - 20);
            layout.height(10);
        });
        return label;
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

    static boolean hasJeiSkin() {
        if (!ViScriptRecipe.isModLoaded("jei")) {
            return false;
        }
        var resources = Minecraft.getInstance().getResourceManager();
        return resources.getResource(jeiTexture("slot.png")).isPresent()
                && resources.getResource(jeiTexture("output_slot.png")).isPresent()
                && resources.getResource(jeiTexture("recipe_arrow.png")).isPresent()
                && resources.getResource(jeiTexture("icons/flame.png")).isPresent();
    }

    private static CookingSkin createJeiSkin() {
        return new CookingSkin(
                sprite(jeiTexture("slot.png"), SLOT_SIZE, SLOT_SIZE),
                sprite(jeiTexture("output_slot.png"), OUTPUT_SLOT_SIZE, OUTPUT_SLOT_SIZE),
                sprite(jeiTexture("recipe_arrow.png"), 22, 16),
                sprite(jeiTexture("icons/flame.png"), 14, 14),
                ColorPattern.LIGHT_GRAY.color
        );
    }

    private static SpriteTexture sprite(ResourceLocation texture, int width, int height) {
        return SpriteTexture.of(texture).setSprite(0, 0, width, height);
    }

    private static ResourceLocation jeiTexture(String path) {
        return new ResourceLocation("jei", "textures/jei/atlas/gui/" + path);
    }

    private record CookingSkin(
            IGuiTexture slot,
            IGuiTexture outputSlot,
            IGuiTexture arrow,
            IGuiTexture flame,
            int textColor
    ) {
    }
}
