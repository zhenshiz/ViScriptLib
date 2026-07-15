package com.viscript_recipe.gui.editor;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.viscript_recipe.ViScriptRecipe;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.TaffyPosition;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

final class DragonForgeCanvasFactory {
    private static final String ICE_AND_FIRE_MOD_ID = "iceandfire";
    private static final String JEI_MOD_ID = "jei";
    private static final String[] DRAGON_TYPES = {"fire", "ice", "lightning"};
    private static final int JEI_WIDTH = 170;
    private static final int JEI_HEIGHT = 79;
    private static final int JEI_SLOT_SIZE = 18;

    private DragonForgeCanvasFactory() {
    }

    static UIElement createJeiCanvas(
            UIElement inputSlot,
            UIElement bloodSlot,
            UIElement outputSlot,
            UIElement[] dragonTypeLayers,
            Runnable onDragonBreathSelected
    ) {
        var panel = new UIElement().layout(layout -> {
            layout.width(JEI_WIDTH);
            layout.height(JEI_HEIGHT);
            layout.positionType(TaffyPosition.RELATIVE);
        });
        for (int index = 0; index < DRAGON_TYPES.length; index++) {
            var layer = createDragonTypeLayer(DRAGON_TYPES[index], onDragonBreathSelected);
            layer.setDisplay(index == 0);
            dragonTypeLayers[index] = layer;
            panel.addChild(layer);
        }
        panel.addChildren(
                positioned(inputSlot, 65, 30, JEI_SLOT_SIZE, JEI_SLOT_SIZE),
                positioned(bloodSlot, 83, 30, JEI_SLOT_SIZE, JEI_SLOT_SIZE),
                positioned(outputSlot, 145, 31, JEI_SLOT_SIZE, JEI_SLOT_SIZE)
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

    static void updateDragonType(UIElement[] dragonTypeLayers, String dragonType) {
        var activeIndex = switch (dragonType) {
            case "ice" -> 1;
            case "lightning" -> 2;
            default -> 0;
        };
        for (int index = 0; index < dragonTypeLayers.length; index++) {
            if (dragonTypeLayers[index] != null) {
                dragonTypeLayers[index].setDisplay(index == activeIndex);
            }
        }
    }

    static boolean hasJeiSkin() {
        if (!ViScriptRecipe.isModLoaded(JEI_MOD_ID) || !ViScriptRecipe.isModLoaded(ICE_AND_FIRE_MOD_ID)) {
            return false;
        }
        var resources = Minecraft.getInstance().getResourceManager();
        for (var dragonType : DRAGON_TYPES) {
            if (resources.getResource(textureLocation(dragonType)).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private static UIElement createDragonTypeLayer(String dragonType, Runnable onDragonBreathSelected) {
        var texture = textureLocation(dragonType);
        var layer = positioned(new UIElement(), 0, 0, JEI_WIDTH, JEI_HEIGHT)
                .style(style -> style.backgroundTexture(sprite(texture, 3, 4, JEI_WIDTH, JEI_HEIGHT)));
        var dragonBreath = positioned(new UIElement(), 9, 19, 126, 38)
                .style(style -> style
                        .backgroundTexture(sprite(texture, 0, 166, 126, 38))
                        .tooltips(Component.translatable("viscript_recipe.editor.dragon_forge.breath_tip")));
        dragonBreath.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            if (event.button == 0) {
                onDragonBreathSelected.run();
                event.stopPropagation();
            }
        });
        return layer.addChild(dragonBreath);
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

    private static IGuiTexture sprite(ResourceLocation texture, int left, int top, int width, int height) {
        return SpriteTexture.of(texture).setSprite(left, top, width, height);
    }

    private static ResourceLocation textureLocation(String dragonType) {
        return new ResourceLocation(
                ICE_AND_FIRE_MOD_ID,
                "textures/gui/dragonforge_" + dragonType + ".png"
        );
    }
}
