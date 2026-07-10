package com.viscriptshop.event;

import com.viscriptshop.event.kubejs.ShopClientEventJS;
import com.viscriptshop.event.kubejs.ShopServerEventJS;
import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;

public interface ViScriptShopEventsJS {
    EventGroup GROUP = EventGroup.of("ViScriptShopEvents");

    EventHandler OPENING = GROUP.client("opening", () -> ShopClientEventJS.Opening.class);
    EventHandler CLOSING = GROUP.client("closing", () -> ShopClientEventJS.Closing.class);
    EventHandler TICK = GROUP.client("tick", () -> ShopClientEventJS.Tick.class);
    EventHandler BUY_PRE = GROUP.server("buyPre", () -> ShopServerEventJS.BuyPre.class).hasResult();
    EventHandler BUY_FAIL = GROUP.server("buyFail", () -> ShopServerEventJS.BuyFail.class);
    EventHandler BUY_SUCCESS = GROUP.server("buySuccess", () -> ShopServerEventJS.BuySuccess.class);
}
