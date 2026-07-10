package com.vss_market.util;

import com.viscriptshop.gui.components.Message;

public record MarketResult(boolean success, Message.Type type, String messageKey) {
    public static MarketResult success(String messageKey) {
        return new MarketResult(true, Message.Type.SUCCESS, messageKey);
    }

    public static MarketResult error(String messageKey) {
        return new MarketResult(false, Message.Type.ERROR, messageKey);
    }
}
