package com.viscript_recipe.gui.editor;

import com.lowdragmc.lowdraglib2.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.TaffyPosition;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Arrays;

final class SporeCanvasFactory {
    private static final String MOD_ID = "spore";
    private static final int JEI_WIDTH = 176;
    private static final int JEI_HEIGHT = 82;
    private static final int SLOT_SIZE = 18;

    private SporeCanvasFactory() {
    }

    static UIElement createSurgeryCanvas(UIElement[] inputSlots, UIElement outputSlot) {
        var panel = createPanel("textures/gui/surgery_table_gui.png");
        for (int index = 0; index < inputSlots.length; index++) {
            var column = index / 4;
            var row = index % 4;
            panel.addChild(positionedSlot(inputSlots[index], 7 + column * SLOT_SIZE, 8 + row * SLOT_SIZE));
        }
        panel.addChildren(
                positionedSlot(readOnlySlot(
                        Component.translatable("viscript_recipe.editor.spore.surgery.stitches_slot"),
                        itemStack("minecraft:string"),
                        itemStack("spore:tendons"),
                        itemStack("spore:nerves")
                ), 97, 8),
                positionedSlot(readOnlyAgentSlot("spore:hardening_agent"), 115, 8),
                positionedSlot(readOnlyAgentSlot("spore:sharpening_agent"), 133, 8),
                positionedSlot(readOnlyAgentSlot("spore:integrating_agent"), 151, 8),
                positionedSlot(outputSlot, 124, 53)
        );
        return centerPanel(panel);
    }

    static UIElement createGraftingCanvas(UIElement[] inputSlots, UIElement outputSlot) {
        var panel = createPanel("textures/gui/grafting_gui.png");
        panel.addChildren(
                positionedSlot(inputSlots[0], 25, 8),
                positionedSlot(inputSlots[1], 25, 35),
                positionedSlot(inputSlots[2], 25, 62),
                positionedSlot(outputSlot, 88, 35)
        );
        return centerPanel(panel);
    }

    private static UIElement createPanel(String texturePath) {
        return new UIElement().layout(layout -> {
            layout.width(JEI_WIDTH);
            layout.height(JEI_HEIGHT);
            layout.positionType(TaffyPosition.RELATIVE);
        }).style(style -> style.backgroundTexture(SpriteTexture.of(
                new ResourceLocation(MOD_ID, texturePath)
        ).setSprite(0, 0, JEI_WIDTH, JEI_HEIGHT)));
    }

    private static UIElement positionedSlot(UIElement slot, int left, int top) {
        return new UIElement().layout(layout -> {
            layout.positionType(TaffyPosition.ABSOLUTE);
            layout.left(left);
            layout.top(top);
            layout.width(SLOT_SIZE);
            layout.height(SLOT_SIZE);
        }).addChild(slot);
    }

    private static UIElement readOnlyAgentSlot(String itemId) {
        var stack = itemStack(itemId);
        return readOnlySlot(Component.translatable(
                "viscript_recipe.editor.spore.surgery.agent_slot",
                stack.getHoverName()
        ), stack);
    }

    private static UIElement readOnlySlot(Component tooltip, ItemStack... candidates) {
        var stacks = Arrays.stream(candidates)
                .filter(stack -> stack != null && !stack.isEmpty())
                .map(ItemStack::copy)
                .toArray(ItemStack[]::new);
        var icon = new UIElement().layout(layout -> {
            layout.positionType(TaffyPosition.ABSOLUTE);
            layout.left(1);
            layout.top(1);
            layout.width(16);
            layout.height(16);
        }).style(style -> style.backgroundTexture(new ItemStackTexture(stacks)));
        return new UIElement().layout(layout -> {
            layout.width(SLOT_SIZE);
            layout.height(SLOT_SIZE);
            layout.positionType(TaffyPosition.RELATIVE);
        }).style(style -> style.tooltips(tooltip)).addChild(icon);
    }

    private static ItemStack itemStack(String itemId) {
        var id = ResourceLocation.tryParse(itemId);
        if (id == null || !BuiltInRegistries.ITEM.containsKey(id)) {
            return ItemStack.EMPTY;
        }
        var item = BuiltInRegistries.ITEM.get(id);
        return item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item);
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
}
