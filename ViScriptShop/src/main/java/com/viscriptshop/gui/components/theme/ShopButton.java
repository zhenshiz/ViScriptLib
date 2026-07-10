package com.viscriptshop.gui.components.theme;

import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.viscriptshop.ViscriptShop;

public class ShopButton extends Button {

    private static final SpriteTexture BUYING_BASE = SpriteTexture.of(ViscriptShop.formattedMod("textures/gui/button/buying_botton.png"));
    private static final SpriteTexture BUYING_HOVER = SpriteTexture.of(ViscriptShop.formattedMod("textures/gui/button/buying_botton_hover.png"));
    private static final SpriteTexture BUYING_PRESSED = SpriteTexture.of(ViscriptShop.formattedMod("textures/gui/button/buying_botton_hold.png"));

    private static final SpriteTexture OTHER_BASE = SpriteTexture.of(ViscriptShop.formattedMod("textures/gui/button/other_button.png"));
    private static final SpriteTexture OTHER_HOVER = SpriteTexture.of(ViscriptShop.formattedMod("textures/gui/button/other_button_hover.png"));
    private static final SpriteTexture OTHER_PRESSED = SpriteTexture.of(ViscriptShop.formattedMod("textures/gui/button/other_button_hold.png"));

    public static ShopButton buying() {
        ShopButton button = new ShopButton();
        button.buttonStyle(style -> {
            style.baseTexture(BUYING_BASE);
            style.hoverTexture(BUYING_HOVER);
            style.pressedTexture(BUYING_PRESSED);
        });
        return button;
    }

    public static ShopButton other() {
        ShopButton button = new ShopButton();
        button.buttonStyle(style -> {
            style.baseTexture(OTHER_BASE);
            style.hoverTexture(OTHER_HOVER);
            style.pressedTexture(OTHER_PRESSED);
        });
        return button;
    }

    public ShopButton() {
    }
}
