package com.viscriptshop.gui.components.theme;

import com.lowdragmc.lowdraglib2.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.SDFRectTexture;
import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.viscriptshop.Config;
import com.viscriptshop.ViscriptShop;

/**
 * 商店界面的主题纹理集合。
 */
public record ShopTheme(
        String styleClass,
        float centerPanelGap,
        IGuiTexture categoryHeader,
        IGuiTexture categoryPanel,
        IGuiTexture topBar,
        IGuiTexture merchantPanel,
        IGuiTexture titleHeader,
        IGuiTexture summaryPanel,
        IGuiTexture shoppingCartPanel,
        IGuiTexture consumptionPanel,
        IGuiTexture searchIconBackground,
        IGuiTexture searchField,
        IGuiTexture merchantList,
        IGuiTexture merchantGrid,
        IGuiTexture actionButtonBase,
        IGuiTexture actionButtonHover,
        IGuiTexture actionButtonPressed,
        IGuiTexture secondaryButtonBase,
        IGuiTexture secondaryButtonHover,
        IGuiTexture secondaryButtonPressed,
        IGuiTexture categoryDefault,
        IGuiTexture categorySelected
) {
    public static ShopTheme current() {
        return switch (Config.shopUiTheme.get()) {
            case CLASSIC -> classic();
            case GLASS_DARK -> glassDark();
        };
    }

    private static ShopTheme classic() {
        return new ShopTheme(
                "shop-theme-classic",
                0,
                sprite("shop_ui_1.png"),
                sprite("shop_ui_2.png"),
                sprite("shop_top_bar.png"),
                sprite("shop_ui_bottom.png"),
                sprite("shop_ui_4.png"),
                sprite("shop_ui_5.png"),
                sprite("shopping_cart_background.png"),
                sprite("consumption_background.png"),
                new ColorRectTexture(0xFF454049),
                sprite("search_bar.png"),
                sprite("trade_bar.png"),
                sprite("verticle_trade_bar.png"),
                sprite("button/buying_botton.png"),
                sprite("button/buying_botton_hover.png"),
                sprite("button/buying_botton_hold.png"),
                sprite("button/other_button.png"),
                sprite("button/other_button_hover.png"),
                sprite("button/other_button_hold.png"),
                rounded(0x50505070, 0x00000000, 0, 3),
                rounded(0xC05A5362, 0xD0988EA2, 1, 3)
        );
    }

    private static ShopTheme glassDark() {
        return glass(
                "shop-theme-glass-dark",
                0xA0202934,
                0x78111922,
                0x6E17212B,
                0x8A26313E,
                0xB02F3E4D,
                0x70D6E5F2
        );
    }

    private static ShopTheme glass(String styleClass, int header, int panel, int inset,
                                   int card, int selected, int border) {
        return new ShopTheme(
                styleClass,
                3,
                rounded(header, border, 1, 4),
                rounded(panel, border, 1, 4),
                rounded(header, border, 1, 5),
                rounded(panel, border, 1, 5),
                rounded(header, border, 1, 4),
                rounded(panel, border, 1, 4),
                rounded(inset, border, 1, 4),
                rounded(inset, border, 1, 4),
                IGuiTexture.EMPTY,
                rounded(inset, border, 1, 5),
                rounded(card, border, 1, 4),
                rounded(card, border, 1, 4),
                rounded(0xA83B4652, 0x70D6E5F2, 1, 3),
                rounded(0xC0506270, 0x90EAF7FF, 1, 3),
                rounded(0xB02A333E, 0x70D6E5F2, 1, 3),
                rounded(0xA83B4652, 0x70D6E5F2, 1, 3),
                rounded(0xC0506270, 0x90EAF7FF, 1, 3),
                rounded(0xB02A333E, 0x70D6E5F2, 1, 3),
                rounded(0x50333F4B, border, 1, 3),
                rounded(selected, 0xC0F2FAFF, 1, 3)
        );
    }

    private static SpriteTexture sprite(String fileName) {
        return SpriteTexture.of(ViscriptShop.formattedMod("textures/gui/" + fileName));
    }

    private static SDFRectTexture rounded(int fill, int border, float stroke, float radius) {
        return SDFRectTexture.of(fill)
                .setBorderColor(border)
                .setStroke(stroke)
                .setRadius(radius);
    }
}
