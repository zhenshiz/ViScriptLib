package com.vss_market.util;

import com.vss_market.Config;
import com.vss_market.data.MarketListing;
import com.vss_market.data.MarketSavedData;
import com.vss_market.data.PlayerShopData;
import com.viscript_lib.util.item.ItemUtil;
import com.viscriptshop.util.ViScriptShopServerUtil;
import lombok.experimental.UtilityClass;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;

@UtilityClass
public class MarketServerUtil {
    public static MarketSavedData data(ServerPlayer player) {
        return MarketSavedData.get(player.server.overworld());
    }

    public static PlayerShopData getOrCreateShop(ServerPlayer player) {
        return data(player).getOrCreateShop(player.getGameProfile());
    }

    public static MarketResult createShop(ServerPlayer player) {
        var savedData = data(player);
        if (savedData.findShop(player.getUUID()).isPresent()) {
            return MarketResult.error("vss_market.message.shop_already_exists");
        }
        savedData.getOrCreateShop(player.getGameProfile());
        return MarketResult.success("vss_market.message.shop_created");
    }

    public static MarketResult renameShop(ServerPlayer player, String name) {
        var normalized = name == null ? "" : name.trim();
        if (normalized.isBlank() || normalized.length() > Config.MAX_SHOP_NAME_LENGTH.get()) {
            return MarketResult.error("vss_market.message.invalid_shop_name");
        }
        var savedData = data(player);
        var shop = savedData.getOrCreateShop(player.getGameProfile());
        shop.setName(normalized).setUpdatedTime(System.currentTimeMillis());
        savedData.setDirty();
        return MarketResult.success("vss_market.message.shop_saved");
    }

    public static MarketResult uploadListing(ServerPlayer player, ItemStack stack, int price, int bundleSize, int stock) {
        if (!validatePrice(price)) {
            return MarketResult.error("vss_market.message.invalid_price");
        }
        if (!validateCount(bundleSize) || !validateCount(stock)) {
            return MarketResult.error("vss_market.message.invalid_count");
        }
        if (stack == null || stack.isEmpty()) {
            return MarketResult.error("vss_market.message.no_item_selected");
        }
        var requiredItems = totalItems(bundleSize, stock);
        if (requiredItems <= 0 || requiredItems > Config.MAX_STACKS_PER_OPERATION.get()) {
            return MarketResult.error("vss_market.message.invalid_count");
        }
        var savedData = data(player);
        var shop = savedData.getOrCreateShop(player.getGameProfile());
        if (shop.getListings().size() >= Config.MAX_LISTINGS_PER_SHOP.get()) {
            return MarketResult.error("vss_market.message.too_many_listings");
        }
        var unitStack = stack.copyWithCount(1);
        if (ItemUtil.getItemForPlayerCount(player, unitStack) < requiredItems) {
            return MarketResult.error("vss_market.message.not_enough_item");
        }

        ItemUtil.removeItemForPlayer(player, unitStack, requiredItems);
        long now = System.currentTimeMillis();
        var listing = new MarketListing()
                .setItem(unitStack)
                .setPrice(price)
                .setBundleSize(bundleSize)
                .setStock(stock)
                .setCreatedTime(now)
                .setUpdatedTime(now);
        shop.getListings().add(listing);
        shop.setUpdatedTime(now);
        savedData.setDirty();
        return MarketResult.success("vss_market.message.uploaded");
    }

    public static MarketResult buyListing(ServerPlayer buyer, String ownerId, String listingId, int count) {
        if (!validateCount(count)) {
            return MarketResult.error("vss_market.message.invalid_count");
        }
        var savedData = data(buyer);
        var shop = savedData.findShop(ownerId).orElse(null);
        if (shop == null) {
            return MarketResult.error("vss_market.message.shop_not_found");
        }
        if (shop.getOwnerId().equals(buyer.getUUID())) {
            return MarketResult.error("vss_market.message.cannot_buy_self");
        }
        var listing = shop.findListing(listingId).orElse(null);
        if (listing == null || listing.getItem().isEmpty()) {
            return MarketResult.error("vss_market.message.listing_not_found");
        }
        if (listing.getStock() < count) {
            return MarketResult.error("vss_market.message.out_of_stock");
        }
        int itemCount = totalItems(listing.getBundleSize(), count);
        if (itemCount <= 0) {
            return MarketResult.error("vss_market.message.invalid_count");
        }
        long total = (long) listing.getPrice() * count;
        if (total > Integer.MAX_VALUE) {
            return MarketResult.error("vss_market.message.price_too_large");
        }
        if (ViScriptShopServerUtil.getMoney(buyer) < total) {
            return MarketResult.error("vss_market.message.not_enough_money");
        }
        if ((long) shop.getBalance() + total > Integer.MAX_VALUE) {
            return MarketResult.error("vss_market.message.price_too_large");
        }

        ViScriptShopServerUtil.removeMoney(buyer, (int) total);
        giveItem(buyer, listing.unitStack(), itemCount);
        listing.setStock(listing.getStock() - count).setUpdatedTime(System.currentTimeMillis());
        shop.setBalance(shop.getBalance() + (int) total).setUpdatedTime(System.currentTimeMillis());
        savedData.setDirty();
        return MarketResult.success("vss_market.message.buy_success");
    }

    public static MarketResult restockListing(ServerPlayer owner, String listingId, int count) {
        if (!validateCount(count)) {
            return MarketResult.error("vss_market.message.invalid_count");
        }
        var savedData = data(owner);
        var shop = savedData.findShop(owner.getUUID()).orElse(null);
        if (shop == null) {
            return MarketResult.error("vss_market.message.shop_not_found");
        }
        var listing = shop.findListing(listingId).orElse(null);
        if (listing == null || listing.getItem().isEmpty()) {
            return MarketResult.error("vss_market.message.listing_not_found");
        }
        int itemCount = totalItems(listing.getBundleSize(), count);
        if (itemCount <= 0) {
            return MarketResult.error("vss_market.message.invalid_count");
        }
        if (ItemUtil.getItemForPlayerCount(owner, listing.unitStack()) < itemCount) {
            return MarketResult.error("vss_market.message.not_enough_item");
        }
        if ((long) listing.getStock() + count > Integer.MAX_VALUE) {
            return MarketResult.error("vss_market.message.invalid_count");
        }

        ItemUtil.removeItemForPlayer(owner, listing.unitStack(), itemCount);
        listing.setStock(listing.getStock() + count).setUpdatedTime(System.currentTimeMillis());
        shop.setUpdatedTime(System.currentTimeMillis());
        savedData.setDirty();
        return MarketResult.success("vss_market.message.restocked");
    }

    public static MarketResult updatePrice(ServerPlayer owner, String listingId, int price) {
        if (!validatePrice(price)) {
            return MarketResult.error("vss_market.message.invalid_price");
        }
        var savedData = data(owner);
        var shop = savedData.findShop(owner.getUUID()).orElse(null);
        if (shop == null) {
            return MarketResult.error("vss_market.message.shop_not_found");
        }
        var listing = shop.findListing(listingId).orElse(null);
        if (listing == null) {
            return MarketResult.error("vss_market.message.listing_not_found");
        }
        listing.setPrice(price).setUpdatedTime(System.currentTimeMillis());
        shop.setUpdatedTime(System.currentTimeMillis());
        savedData.setDirty();
        return MarketResult.success("vss_market.message.price_updated");
    }

    public static MarketResult removeListing(ServerPlayer owner, String listingId) {
        var savedData = data(owner);
        var shop = savedData.findShop(owner.getUUID()).orElse(null);
        if (shop == null) {
            return MarketResult.error("vss_market.message.shop_not_found");
        }
        var listing = shop.findListing(listingId).orElse(null);
        if (listing == null) {
            return MarketResult.error("vss_market.message.listing_not_found");
        }
        if (listing.getStock() > 0) {
            giveItem(owner, listing.unitStack(), totalItems(listing.getBundleSize(), listing.getStock()));
        }
        shop.removeListing(listingId);
        shop.setUpdatedTime(System.currentTimeMillis());
        savedData.setDirty();
        return MarketResult.success("vss_market.message.removed");
    }

    public static MarketResult withdraw(ServerPlayer owner) {
        var savedData = data(owner);
        var shop = savedData.findShop(owner.getUUID()).orElse(null);
        if (shop == null) {
            return MarketResult.error("vss_market.message.shop_not_found");
        }
        if (shop.getBalance() <= 0) {
            return MarketResult.error("vss_market.message.no_balance");
        }
        if (!canAddMoney(owner, shop.getBalance())) {
            return MarketResult.error("vss_market.message.money_too_large");
        }
        ViScriptShopServerUtil.addMoney(owner, shop.getBalance());
        shop.setBalance(0).setUpdatedTime(System.currentTimeMillis());
        savedData.setDirty();
        return MarketResult.success("vss_market.message.withdrawn");
    }

    public static MarketResult deleteShop(ServerPlayer owner) {
        var savedData = data(owner);
        var shop = savedData.findShop(owner.getUUID()).orElse(null);
        if (shop == null) {
            return MarketResult.error("vss_market.message.shop_not_found");
        }
        if (shop.getBalance() > 0 && !canAddMoney(owner, shop.getBalance())) {
            return MarketResult.error("vss_market.message.money_too_large");
        }
        for (var listing : shop.getListings()) {
            if (listing.getStock() > 0 && !listing.getItem().isEmpty()) {
                giveItem(owner, listing.unitStack(), totalItems(listing.getBundleSize(), listing.getStock()));
            }
        }
        if (shop.getBalance() > 0) {
            ViScriptShopServerUtil.addMoney(owner, shop.getBalance());
        }
        savedData.removeShop(owner.getUUID());
        return MarketResult.success("vss_market.message.shop_deleted");
    }

    private static boolean validatePrice(int price) {
        return price >= Config.MIN_PRICE.get() && price <= Config.MAX_PRICE.get();
    }

    private static boolean validateCount(int count) {
        return count > 0 && count <= Config.MAX_STACKS_PER_OPERATION.get();
    }

    private static int totalItems(int bundleSize, int stock) {
        long total = (long) bundleSize * stock;
        return total > 0 && total <= Integer.MAX_VALUE ? (int) total : -1;
    }

    private static boolean canAddMoney(ServerPlayer player, int amount) {
        return amount >= 0 && (long) ViScriptShopServerUtil.getMoney(player) + amount <= Integer.MAX_VALUE;
    }

    private static void giveItem(ServerPlayer player, ItemStack unitStack, int count) {
        if (unitStack.isEmpty() || count <= 0) {
            return;
        }
        int remaining = count;
        int max = Math.max(1, unitStack.getMaxStackSize());
        while (remaining > 0) {
            int giveCount = Math.min(max, remaining);
            ItemHandlerHelper.giveItemToPlayer(player, unitStack.copyWithCount(giveCount));
            remaining -= giveCount;
        }
    }
}
