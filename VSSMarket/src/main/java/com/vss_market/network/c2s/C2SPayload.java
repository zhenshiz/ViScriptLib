package com.vss_market.network.c2s;

import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacket;
import com.lowdragmc.lowdraglib2.syncdata.rpc.RPCSender;
import com.vss_market.VSSMarket;
import com.vss_market.network.s2c.S2CPayload;
import com.vss_market.util.MarketServerUtil;
import net.minecraft.world.item.ItemStack;

public class C2SPayload {
    public static final String MOD_ID = VSSMarket.MOD_ID + ":";
    public static final String REQUEST_MARKET = MOD_ID + "request_market_c2s";
    public static final String REQUEST_MARKET_STATE = MOD_ID + "request_market_state_c2s";
    public static final String CREATE_SHOP = MOD_ID + "create_shop_c2s";
    public static final String RENAME_SHOP = MOD_ID + "rename_shop_c2s";
    public static final String UPLOAD_LISTING = MOD_ID + "upload_listing_c2s";
    public static final String BUY_LISTING = MOD_ID + "buy_listing_c2s";
    public static final String RESTOCK_LISTING = MOD_ID + "restock_listing_c2s";
    public static final String UPDATE_PRICE = MOD_ID + "update_price_c2s";
    public static final String REMOVE_LISTING = MOD_ID + "remove_listing_c2s";
    public static final String WITHDRAW = MOD_ID + "withdraw_c2s";
    public static final String DELETE_SHOP = MOD_ID + "delete_shop_c2s";

    @RPCPacket(REQUEST_MARKET)
    public static void requestMarket(RPCSender sender, String selectedShopId, String view) {
        var player = sender.asPlayer();
        if (player != null) {
            S2CPayload.open(player, selectedShopId, view, null);
        }
    }

    @RPCPacket(REQUEST_MARKET_STATE)
    public static void requestMarketState(RPCSender sender, String selectedShopId, String view, float shopListScroll, float manageListingScroll) {
        var player = sender.asPlayer();
        if (player != null) {
            S2CPayload.open(player, selectedShopId, "", view, shopListScroll, manageListingScroll, null);
        }
    }

    @RPCPacket(CREATE_SHOP)
    public static void createShop(RPCSender sender) {
        var player = sender.asPlayer();
        if (player != null) {
            S2CPayload.open(player, player.getUUID().toString(), "MANAGE", MarketServerUtil.createShop(player));
        }
    }

    @RPCPacket(RENAME_SHOP)
    public static void renameShop(RPCSender sender, String name) {
        var player = sender.asPlayer();
        if (player != null) {
            S2CPayload.open(player, player.getUUID().toString(), "MANAGE", MarketServerUtil.renameShop(player, name));
        }
    }

    @RPCPacket(UPLOAD_LISTING)
    public static void uploadListing(RPCSender sender, ItemStack stack, double price, int bundleSize, int stock, boolean purchaseOrder) {
        var player = sender.asPlayer();
        if (player != null) {
            var result = MarketServerUtil.uploadListing(player, stack, price, bundleSize, stock, purchaseOrder);
            if (result.success()) {
                S2CPayload.open(player, player.getUUID().toString(), "MANAGE", result);
            } else {
                S2CPayload.openUpload(player, result, stack, price, bundleSize, stock, purchaseOrder);
            }
        }
    }

    @RPCPacket(BUY_LISTING)
    public static void buyListing(RPCSender sender, String ownerId, String listingId, int count) {
        var player = sender.asPlayer();
        if (player != null) {
            S2CPayload.open(player, ownerId, listingId, "DETAIL", MarketServerUtil.buyListing(player, ownerId, listingId, count));
        }
    }

    @RPCPacket(RESTOCK_LISTING)
    public static void restockListing(RPCSender sender, String listingId, int count, float shopListScroll, float manageListingScroll) {
        var player = sender.asPlayer();
        if (player != null) {
            S2CPayload.open(player, player.getUUID().toString(), listingId, "DETAIL", shopListScroll, manageListingScroll, MarketServerUtil.restockListing(player, listingId, count));
        }
    }

    @RPCPacket(UPDATE_PRICE)
    public static void updatePrice(RPCSender sender, String listingId, double price, float shopListScroll, float manageListingScroll) {
        var player = sender.asPlayer();
        if (player != null) {
            S2CPayload.open(player, player.getUUID().toString(), listingId, "DETAIL", shopListScroll, manageListingScroll, MarketServerUtil.updatePrice(player, listingId, price));
        }
    }

    @RPCPacket(REMOVE_LISTING)
    public static void removeListing(RPCSender sender, String listingId, float shopListScroll, float manageListingScroll) {
        var player = sender.asPlayer();
        if (player != null) {
            S2CPayload.open(player, player.getUUID().toString(), "", "MANAGE", shopListScroll, manageListingScroll, MarketServerUtil.removeListing(player, listingId));
        }
    }

    @RPCPacket(WITHDRAW)
    public static void withdraw(RPCSender sender) {
        var player = sender.asPlayer();
        if (player != null) {
            S2CPayload.open(player, player.getUUID().toString(), "MANAGE", MarketServerUtil.withdraw(player));
        }
    }

    @RPCPacket(DELETE_SHOP)
    public static void deleteShop(RPCSender sender) {
        var player = sender.asPlayer();
        if (player != null) {
            S2CPayload.open(player, "", MarketServerUtil.deleteShop(player));
        }
    }
}
