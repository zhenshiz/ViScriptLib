package com.viscript_recipe.gui.editor;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.Icons;
import com.lowdragmc.lowdraglib2.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.viscript_recipe.ViScriptRecipe;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.TaffyPosition;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Builds Goety recipe canvases from its official JEI coordinates with an LDLib2 fallback skin.
 */
final class GoetyCanvasFactory {
    private static final int SLOT_SIZE = 18;
    private static final ResourceLocation JEI_GUI = texture("jei_gui.png");
    private static final ResourceLocation ARROW = texture("arrow.png");
    private static final ResourceLocation BRAZIER = texture("brazier.png");
    private static final ResourceLocation CIRCLE = texture("circle.png");
    private static final ResourceLocation DOWN_ARROW = texture("down_arrow.png");

    private static final int[][] RITUAL_PEDESTALS = {
            {56, 42}, {86, 72}, {56, 102}, {26, 72},
            {71, 42}, {86, 42}, {41, 102}, {26, 102},
            {41, 42}, {86, 102}, {71, 102}, {26, 42}
    };

    private GoetyCanvasFactory() {
    }

    static boolean hasJeiSkin() {
        if (!ViScriptRecipe.isModLoaded("goety") || !ViScriptRecipe.isModLoaded("jei")) {
            return false;
        }
        var resources = Minecraft.getInstance().getResourceManager();
        return resources.getResource(JEI_GUI).isPresent()
                && resources.getResource(ARROW).isPresent()
                && resources.getResource(BRAZIER).isPresent()
                && resources.getResource(CIRCLE).isPresent()
                && resources.getResource(DOWN_ARROW).isPresent();
    }

    static UIElement createCursedInfuserCanvas(UIElement input, UIElement output, UIElement machine, Label timeLabel,
                                                boolean useJei) {
        if (!useJei) {
            return fallbackProcess(input, machine, output, timeLabel);
        }
        var panel = panel(82, 56, IGuiTexture.EMPTY);
        panel.addChildren(
                positioned(input, 1, 2, SLOT_SIZE, SLOT_SIZE),
                positioned(arrow(28, 16), 27, 3, 28, 16),
                positioned(output, 63, 2, SLOT_SIZE, SLOT_SIZE),
                positioned(machine, 30, 36, 16, 16),
                positioned(timeLabel, 35, 39, 46, 9)
        );
        return centered(panel);
    }

    static UIElement createPulverizeCanvas(UIElement input, UIElement output, boolean useJei) {
        if (!useJei) {
            return fallbackProcess(input, arrow(28, 16), output, null);
        }
        var panel = panel(82, 34, sprite(JEI_GUI, 0, 220, 82, 34));
        panel.addChildren(
                positioned(input, 1, 9, SLOT_SIZE, SLOT_SIZE),
                positioned(output, 61, 9, SLOT_SIZE, SLOT_SIZE)
        );
        return centered(panel);
    }

    static UIElement createRitualCanvas(UIElement activation, UIElement[] ingredients, UIElement output,
                                        UIElement typeIcon, UIElement researchIcon, Label infoLabel, boolean useJei) {
        if (!useJei) {
            var pedestalGrid = RecipeEditorUi.row().layout(layout -> {
                layout.width(128);
                layout.gapAll(2);
                layout.flexWrap(dev.vfyjxf.taffy.style.FlexWrap.WRAP);
                layout.alignItems(AlignItems.CENTER);
                layout.justifyContent(AlignContent.CENTER);
            });
            for (var ingredient : ingredients) {
                pedestalGrid.addChild(ingredient);
            }
            var ritualCore = RecipeEditorUi.column().layout(layout -> {
                layout.width(136);
                layout.gapAll(4);
                layout.alignItems(AlignItems.CENTER);
            }).addChildren(activation, readOnlyItem("goety:dark_altar",
                    "viscript_recipe.editor.goety.ritual.dark_altar", 24), pedestalGrid, infoLabel);
            return RecipeEditorUi.row().layout(layout -> {
                layout.widthPercent(100);
                layout.flex(1);
                layout.gapAll(12);
                layout.alignItems(AlignItems.CENTER);
                layout.justifyContent(AlignContent.CENTER);
            }).addChildren(typeIcon, ritualCore, arrow(28, 16), output);
        }

        var panel = panel(176, 140, IGuiTexture.EMPTY);
        panel.addChildren(
                textureElement(sprite(ARROW, 0, 0, 64, 46), 111, 72, 64, 46),
                positioned(typeIcon, 0, 0, 16, 16),
                positioned(researchIcon, 0, 16, 16, 16),
                positioned(readOnlyItem("goety:dark_altar", "viscript_recipe.editor.goety.ritual.dark_altar", 16),
                        57, 73, 16, 16),
                positioned(readOnlyItem("goety:dark_altar", "viscript_recipe.editor.goety.ritual.result_altar", 16),
                        132, 73, 16, 16),
                positioned(infoLabel, 18, 120, 140, 18)
        );
        for (int index = 0; index < ingredients.length; index++) {
            var position = RITUAL_PEDESTALS[index];
            panel.addChildren(
                    positioned(readOnlyItem("goety:pedestal_dummy", "viscript_recipe.editor.goety.ritual.pedestal", 16),
                            position[0] + 1, position[1] + 1, 16, 16),
                    positioned(ingredients[index], position[0], position[1] - 5, SLOT_SIZE, SLOT_SIZE)
            );
        }
        panel.addChildren(
                positioned(activation, 56, 57, SLOT_SIZE, SLOT_SIZE),
                positioned(output, 131, 57, SLOT_SIZE, SLOT_SIZE)
        );
        return centered(panel);
    }

    static UIElement createBrazierCanvas(UIElement[] inputs, UIElement output, Label soulLabel, boolean useJei) {
        var machine = readOnlyItem("goety:necro_brazier", "viscript_recipe.editor.goety.brazier.catalyst", 18);
        if (!useJei) {
            var inputRow = RecipeEditorUi.row().layout(layout -> {
                layout.gapAll(3);
                layout.alignItems(AlignItems.CENTER);
            }).addChildren(inputs);
            var body = RecipeEditorUi.column().layout(layout -> {
                layout.gapAll(5);
                layout.alignItems(AlignItems.CENTER);
            }).addChildren(inputRow, machine, soulLabel);
            return RecipeEditorUi.row().layout(layout -> {
                layout.widthPercent(100);
                layout.flex(1);
                layout.gapAll(12);
                layout.alignItems(AlignItems.CENTER);
                layout.justifyContent(AlignContent.CENTER);
            }).addChildren(body, arrow(28, 16), output);
        }

        var panel = panel(134, 80, IGuiTexture.EMPTY);
        panel.addChildren(
                textureElement(sprite(BRAZIER, 0, 0, 31, 31), 40, 20, 31, 31),
                textureElement(sprite(CIRCLE, 0, 0, 31, 31), 91, 27, 31, 31),
                textureElement(sprite(ARROW, 0, 0, 64, 46), 79, 35, 64, 46),
                textureElement(sprite(DOWN_ARROW, 0, 0, 46, 64), 48, 22, 46, 64),
                positioned(machine, 48, 35, SLOT_SIZE, SLOT_SIZE),
                positioned(output, 99, 35, SLOT_SIZE, SLOT_SIZE),
                positioned(soulLabel, 2, 68, 90, 10)
        );
        for (int index = 0; index < inputs.length; index++) {
            panel.addChild(positioned(inputs[index], 10 + index * 18, 6, SLOT_SIZE, SLOT_SIZE));
        }
        return centered(panel);
    }

    static UIElement createBrewingCanvas(UIElement catalyst, UIElement output, Label infoLabel, boolean useJei) {
        var bottle = readOnlyItem("minecraft:glass_bottle", "viscript_recipe.editor.goety.brewing.glass_bottle", SLOT_SIZE);
        var wart = readOnlyItem("minecraft:nether_wart", "viscript_recipe.editor.goety.brewing.nether_wart", SLOT_SIZE);
        var leftCauldron = readOnlyItem("goety:witch_cauldron", "viscript_recipe.editor.goety.brewing.cauldron", 22);
        var rightCauldron = readOnlyItem("goety:witch_cauldron", "viscript_recipe.editor.goety.brewing.cauldron", 22);
        if (!useJei) {
            return RecipeEditorUi.column().layout(layout -> {
                layout.widthPercent(100);
                layout.flex(1);
                layout.gapAll(6);
                layout.alignItems(AlignItems.CENTER);
                layout.justifyContent(AlignContent.CENTER);
            }).addChildren(
                    infoLabel,
                    RecipeEditorUi.row().layout(layout -> {
                        layout.gapAll(8);
                        layout.alignItems(AlignItems.CENTER);
                    }).addChildren(bottle, arrow(20, 12), wart, leftCauldron, arrow(20, 12), catalyst,
                            rightCauldron, arrow(20, 12), output)
            );
        }

        var panel = panel(125, 60, IGuiTexture.EMPTY);
        panel.addChildren(
                positioned(infoLabel, 0, 0, 125, 18),
                positioned(bottle, 1, 20, SLOT_SIZE, SLOT_SIZE),
                positioned(wart, 36, 20, SLOT_SIZE, SLOT_SIZE),
                positioned(catalyst, 72, 20, SLOT_SIZE, SLOT_SIZE),
                positioned(output, 108, 20, SLOT_SIZE, SLOT_SIZE),
                positioned(leftCauldron, 33, 38, 22, 22),
                positioned(rightCauldron, 69, 38, 22, 22)
        );
        return centered(panel);
    }

    static UIElement readOnlyItem(String itemId, String tooltipKey, int size) {
        return new UIElement().layout(layout -> {
            layout.width(size);
            layout.height(size);
        }).style(style -> style
                .backgroundTexture(new ItemStackTexture(itemStack(itemId)))
                .tooltips(Component.translatable(tooltipKey)));
    }

    private static UIElement fallbackProcess(UIElement input, UIElement machine, UIElement output, Label label) {
        var row = RecipeEditorUi.row().layout(layout -> {
            layout.widthPercent(100);
            layout.flex(1);
            layout.gapAll(12);
            layout.alignItems(AlignItems.CENTER);
            layout.justifyContent(AlignContent.CENTER);
        }).addChildren(input, machine, arrow(28, 16), output);
        if (label == null) {
            return row;
        }
        return RecipeEditorUi.column().layout(layout -> {
            layout.widthPercent(100);
            layout.flex(1);
            layout.gapAll(5);
            layout.alignItems(AlignItems.CENTER);
            layout.justifyContent(AlignContent.CENTER);
        }).addChildren(row, label);
    }

    private static UIElement arrow(int width, int height) {
        return new UIElement().layout(layout -> {
            layout.width(width);
            layout.height(height);
        }).style(style -> style.backgroundTexture(Icons.DOWN_ARROW_NO_BAR.copy().rotate(-90)));
    }

    private static UIElement centered(UIElement panel) {
        return RecipeEditorUi.row().layout(layout -> {
            layout.widthPercent(100);
            layout.flex(1);
            layout.minWidth(0);
            layout.minHeight(0);
            layout.alignItems(AlignItems.CENTER);
            layout.justifyContent(AlignContent.CENTER);
        }).addChild(panel);
    }

    private static UIElement panel(int width, int height, IGuiTexture background) {
        return new UIElement().layout(layout -> {
            layout.width(width);
            layout.height(height);
            layout.positionType(TaffyPosition.RELATIVE);
        }).style(style -> style.backgroundTexture(background));
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
        return positioned(new UIElement(), left, top, width, height)
                .style(style -> style.backgroundTexture(texture));
    }

    private static SpriteTexture sprite(ResourceLocation texture, int left, int top, int width, int height) {
        return SpriteTexture.of(texture).setSprite(left, top, width, height);
    }

    private static ItemStack itemStack(String id) {
        var location = ResourceLocation.tryParse(id);
        if (location == null || !BuiltInRegistries.ITEM.containsKey(location)) {
            return ItemStack.EMPTY;
        }
        var item = BuiltInRegistries.ITEM.get(location);
        if (item == Items.AIR) {
            return ItemStack.EMPTY;
        }
        var stack = new ItemStack(item);
        if (id.equals("goety:dark_altar") || id.equals("goety:pedestal_dummy") || id.equals("goety:necro_brazier")) {
            CompoundTag tag = stack.getOrCreateTag();
            tag.putBoolean("RenderFull", true);
            stack.setTag(tag);
        }
        return stack;
    }

    private static ResourceLocation texture(String name) {
        return new ResourceLocation("goety", "textures/gui/jei/" + name);
    }
}
