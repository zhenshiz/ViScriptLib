package com.vss_market.network;

import com.vss_market.data.MarketScreenPayload;
import com.vss_market.gui.MarketClientScreen;
import net.minecraftforge.fml.loading.FMLEnvironment;

public class MarketClientBridge {
    public static void openMarket(MarketScreenPayload payload) {
        if (FMLEnvironment.dist.isClient()) {
            MarketClientScreen.open(payload);
        }
    }
}
