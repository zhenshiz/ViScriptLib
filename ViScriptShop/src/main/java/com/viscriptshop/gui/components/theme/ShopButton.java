package com.viscriptshop.gui.components.theme;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;

public class ShopButton extends Button {

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

    public static ShopButton other(ShopTheme theme) {
        return styled(theme.secondaryButtonBase(), theme.secondaryButtonHover(), theme.secondaryButtonPressed());
    }

    public ShopButton() {
    }
}
