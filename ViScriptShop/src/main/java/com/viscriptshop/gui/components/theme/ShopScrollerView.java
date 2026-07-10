package com.viscriptshop.gui.components.theme;

import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import com.viscriptshop.ViscriptShop;

public class ShopScrollerView extends ScrollerView {

    private static final SpriteTexture SCROLL_TOP = SpriteTexture.of(ViscriptShop.formattedMod("textures/gui/scroll/scroll_top.png"));
    private static final SpriteTexture SCROLL_BOTTOM = SpriteTexture.of(ViscriptShop.formattedMod("textures/gui/scroll/scroll_bottom.png"));
    private static final SpriteTexture SCROLL_BACKGROUND = SpriteTexture.of(ViscriptShop.formattedMod("textures/gui/scroll/scroll_bar_background.png"));
    private static final SpriteTexture SCROLL_BAR = SpriteTexture.of(ViscriptShop.formattedMod("textures/gui/scroll/scroll_bar.png"));
    private static final SpriteTexture SCROLL_BAR_HOVER = SpriteTexture.of(ViscriptShop.formattedMod("textures/gui/scroll/scroll_bar_hover.png"));
    private static final SpriteTexture SCROLL_BAR_HOLD = SpriteTexture.of(ViscriptShop.formattedMod("textures/gui/scroll/scroll_bar_hold.png"));


    public ShopScrollerView() {
        verticalScroller(scroller -> {
            scroller.headButton(btn -> btn.buttonStyle(style -> style
                    .baseTexture(SCROLL_TOP)
            ));
            scroller.tailButton(btn -> btn.buttonStyle(style -> style
                    .baseTexture(SCROLL_BOTTOM)
            ));
            scroller.scrollContainer(container -> container.style(style -> style
                    .backgroundTexture(SCROLL_BACKGROUND)
            ));
            scroller.scrollBar(bar -> bar.buttonStyle(style -> {
                style.baseTexture(SCROLL_BAR);
                style.hoverTexture(SCROLL_BAR_HOVER);
                style.pressedTexture(SCROLL_BAR_HOLD);
            }));
        });
    }
}
