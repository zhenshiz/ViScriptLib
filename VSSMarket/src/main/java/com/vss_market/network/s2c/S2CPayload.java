package com.vss_market.network.s2c;

import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacket;
import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacketDistributor;
import com.lowdragmc.lowdraglib2.syncdata.rpc.RPCSender;
import com.viscriptshop.gui.components.Message;
import com.viscriptshop.util.ViScriptShopServerUtil;
import com.vss_market.VSSMarket;
import com.vss_market.data.MarketScreenPayload;
import com.vss_market.data.MarketSerializers;
import com.vss_market.network.MarketClientBridge;
import com.vss_market.util.MarketResult;
import com.vss_market.util.MarketServerUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class S2CPayload {
    public static final String MOD_ID = VSSMarket.MOD_ID + ":";
    public static final String OPEN_MARKET_SCREEN = MOD_ID + "open_market_screen_s2c";

    static {
        MarketSerializers.register();
    }

    @RPCPacket(OPEN_MARKET_SCREEN)
    public static void openMarketScreen(RPCSender sender, MarketScreenPayload payload) {
        if (sender.isServer()) {
            MarketClientBridge.openMarket(payload);
        }
    }

    public static void open(ServerPlayer player, String selectedShopId, MarketResult result) {
        open(player, selectedShopId, "", "MARKET", result);
    }

    public static void open(ServerPlayer player, String selectedShopId, String view, MarketResult result) {
        open(player, selectedShopId, "", view, result);
    }

    public static void open(ServerPlayer player, String selectedShopId, String selectedListingId, String view, MarketResult result) {
        open(player, selectedShopId, selectedListingId, view, 0f, result);
    }

    public static void open(ServerPlayer player, String selectedShopId, String selectedListingId, String view, float shopListScroll, MarketResult result) {
        open(player, selectedShopId, selectedListingId, view, shopListScroll, 0f, result);
    }

    public static void open(ServerPlayer player, String selectedShopId, String selectedListingId, String view, float shopListScroll, float manageListingScroll, MarketResult result) {
        RPCPacketDistributor.rpcToPlayer(player, OPEN_MARKET_SCREEN, createPayload(player, selectedShopId, selectedListingId, view, shopListScroll, manageListingScroll, result));
    }

    public static void openUpload(ServerPlayer player, MarketResult result, ItemStack stack, int price, int bundleSize, int stock, boolean purchaseOrder) {
        var payload = createPayload(player, player.getUUID().toString(), "", "UPLOAD", 0f, 0f, result);
        payload.setUploadStack(stack)
                .setUploadPrice(price)
                .setUploadBundleSize(bundleSize)
                .setUploadStock(stock)
                .setUploadPurchaseOrder(purchaseOrder);
        RPCPacketDistributor.rpcToPlayer(player, OPEN_MARKET_SCREEN, payload);
    }

    private static MarketScreenPayload createPayload(ServerPlayer player, String selectedShopId, String selectedListingId, String view, float shopListScroll, float manageListingScroll, MarketResult result) {
        var marketData = MarketServerUtil.data(player);
        for (var onlinePlayer : player.server.getPlayerList().getPlayers()) {
            marketData.refreshShopOwnerProfile(onlinePlayer.getGameProfile());
        }
        var payload = new MarketScreenPayload()
                .setMarket(marketData)
                .setViewer(player.getUUID())
                .setSelectedShop(selectedShopId)
                .setSelectedListing(selectedListingId)
                .setView(view)
                .setMoney(ViScriptShopServerUtil.getMoney(player))
                .setShopListScroll(shopListScroll)
                .setManageListingScroll(manageListingScroll);
        if (result != null) {
            payload.setMessageKey(result.messageKey())
                    .setMessageType(result.type().name());
        } else {
            payload.setMessageType(Message.Type.INFO.name());
        }
        return payload;
    }
}
