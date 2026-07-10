package com.viscriptshop.event;

import com.lowdragmc.lowdraglib2.gui.holder.ModularUIScreen;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.mojang.blaze3d.platform.Window;
import com.viscriptshop.ViscriptShop;
import com.viscriptshop.compat.JeiHelper;
import com.viscriptshop.gui.ShopUI;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.awt.*;
import java.util.List;

@Mod.EventBusSubscriber(modid = ViscriptShop.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ShopClientEvent {
    @SubscribeEvent
    public static void shopUiOpening(ScreenEvent.Opening event) {
        if (event.getScreen() instanceof ModularUIScreen screen && screen.modularUI.ui.rootElement instanceof ShopUI shopUI) {
            MinecraftForge.EVENT_BUS.post(new com.viscriptshop.event.neoforge.ShopClientEvent.Opening(shopUI));
        }
    }

    @SubscribeEvent
    public static void shopUiClosing(ScreenEvent.Closing event) {
        if (event.getScreen() instanceof ModularUIScreen screen && screen.modularUI.ui.rootElement instanceof ShopUI shopUI) {
            MinecraftForge.EVENT_BUS.post(new com.viscriptshop.event.neoforge.ShopClientEvent.Closing(shopUI));
        }
    }

    @SubscribeEvent
    public static void shopUiKeyDown(ScreenEvent.KeyPressed.Post event) {
        if (ViscriptShop.isJEILoaded() && event.getScreen() instanceof ModularUIScreen screen && screen.modularUI.ui.rootElement instanceof ShopUI shopUI) {
            Point point = getMousePos();

            List<UIElement> children = shopUI.merchantsView.viewContainer.getChildren();
            for (int i = 0; i < children.size(); i++) {
                UIElement itemA = screen.modularUI.getElementById("itemA" + i);
                UIElement itemB = screen.modularUI.getElementById("itemB" + i);
                UIElement itemResult = screen.modularUI.getElementById("itemResult" + i);
                addJeiSearch(event, itemA, point);
                addJeiSearch(event, itemB, point);
                addJeiSearch(event, itemResult, point);
            }
        }
    }

    public static void addJeiSearch(ScreenEvent.KeyPressed.Post event, UIElement element, Point point) {
        if (element instanceof ItemSlot itemSlot && itemSlot.isMouseOverElement(point.x, point.y) && !itemSlot.getValue().isEmpty()) {
            if (event.getKeyCode() == JeiHelper.getShowRecipeKey()) {
                JeiHelper.showRecipes(itemSlot.getValue());
            } else if (event.getKeyCode() == JeiHelper.getShowUsesKey()) {
                JeiHelper.showUses(itemSlot.getValue());
            }
        }
    }

    public static Point getMousePos() {
        Minecraft minecraft = Minecraft.getInstance();
        Window window = minecraft.getWindow();
        int w1 = window.getWidth();
        int w2 = minecraft.getWindow().getGuiScaledWidth();
        int h1 = window.getHeight();
        int h2 = minecraft.getWindow().getGuiScaledHeight();
        double rW = (double) w2 / (double) w1;
        double rH = (double) h2 / (double) h1;
        return new Point((int) (rW * minecraft.mouseHandler.xpos()), (int) (rH * minecraft.mouseHandler.ypos()));
    }
}
