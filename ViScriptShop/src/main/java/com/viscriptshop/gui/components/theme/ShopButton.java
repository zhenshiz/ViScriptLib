package com.viscriptshop.gui.components.theme;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
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
        return styled(BUYING_BASE, BUYING_HOVER, BUYING_PRESSED);
    }

    public static ShopButton buying(ShopTheme theme) {
        return styled(theme.actionButtonBase(), theme.actionButtonHover(), theme.actionButtonPressed());
    }

    private static ShopButton styled(IGuiTexture base, IGuiTexture hover, IGuiTexture pressed) {
        ShopButton button = new ShopButton();
        button.buttonStyle(style -> {
            style.baseTexture(base);
            style.hoverTexture(hover);
            style.pressedTexture(pressed);
        });
        return button;
    }

    public static ShopButton other() {
        return styled(OTHER_BASE, OTHER_HOVER, OTHER_PRESSED);
    }

    public static ShopButton other(ShopTheme theme) {
        return styled(theme.secondaryButtonBase(), theme.secondaryButtonHover(), theme.secondaryButtonPressed());
    }

    public ShopButton() {
    }
}
