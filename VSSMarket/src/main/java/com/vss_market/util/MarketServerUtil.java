package com.vss_market.util;

import com.viscript_lib.util.item.ItemUtil;
import com.viscriptshop.util.ViScriptShopServerUtil;
import com.vss_market.Config;
import com.vss_market.data.MarketListing;
import com.vss_market.data.MarketPurchaseRecord;
import com.vss_market.data.MarketSavedData;
import com.vss_market.data.PlayerShopData;
import lombok.experimental.UtilityClass;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;

@UtilityClass
public class MarketServerUtil {
    private static final int MAX_PURCHASE_RECORDS = 200;

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

    public static MarketResult uploadListing(ServerPlayer player, ItemStack stack, int price, int bundleSize, int stock, boolean purchaseOrder) {
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
        if (purchaseOrder) {
            long total = totalPrice(price, stock);
            if (total <= 0 || total > Integer.MAX_VALUE) {
                return MarketResult.error("vss_market.message.price_too_large");
            }
            if (ViScriptShopServerUtil.getMoney(player) < total) {
                return MarketResult.error("vss_market.message.not_enough_money");
            }
            ViScriptShopServerUtil.removeMoney(player, (int) total);
        } else {
            if (ItemUtil.getItemForPlayerCount(player, unitStack) < requiredItems) {
                return MarketResult.error("vss_market.message.not_enough_item");
            }
            ItemUtil.removeItemForPlayer(player, unitStack, requiredItems);
        }

        long now = System.currentTimeMillis();
        var listing = new MarketListing()
                .setItem(unitStack)
                .setPrice(price)
                .setBundleSize(bundleSize)
                .setStock(stock)
                .setPurchaseOrder(purchaseOrder)
                .setCreatedTime(now)
                .setUpdatedTime(now);
        shop.getListings().add(listing);
        shop.setUpdatedTime(now);
        savedData.setDirty();
        return MarketResult.success(purchaseOrder
                ? "vss_market.message.purchase_uploaded"
                : "vss_market.message.uploaded");
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
        long now = System.currentTimeMillis();
        int moneySpent = (int) total;
        if (listing.isPurchaseOrder()) {
            if (ItemUtil.getItemForPlayerCount(buyer, listing.unitStack()) < itemCount) {
                return MarketResult.error("vss_market.message.not_enough_item");
            }
            if (!canAddMoney(buyer, moneySpent)) {
                return MarketResult.error("vss_market.message.money_too_large");
            }
            if ((long) listing.getCollectedStock() + count > Integer.MAX_VALUE) {
                return MarketResult.error("vss_market.message.invalid_count");
            }
            ItemUtil.removeItemForPlayer(buyer, listing.unitStack(), itemCount);
            ViScriptShopServerUtil.addMoney(buyer, moneySpent);
            listing.setStock(listing.getStock() - count)
                    .setCollectedStock(listing.getCollectedStock() + count)
                    .setUpdatedTime(now);
        } else {
            if (ViScriptShopServerUtil.getMoney(buyer) < total) {
                return MarketResult.error("vss_market.message.not_enough_money");
            }
            if (shop.getBalance() > Long.MAX_VALUE - total) {
                return MarketResult.error("vss_market.message.price_too_large");
            }
            ViScriptShopServerUtil.removeMoney(buyer, moneySpent);
            giveItem(buyer, listing.unitStack(), itemCount);
            listing.setStock(listing.getStock() - count).setUpdatedTime(now);
            shop.setBalance(shop.getBalance() + moneySpent).setUpdatedTime(now);
        }
        shop.addPurchaseRecord(new MarketPurchaseRecord()
                .setBuyerId(buyer.getUUID())
                .setBuyerName(buyer.getGameProfile().getName())
                .setItem(listing.unitStack())
                .setQuantity(itemCount)
                .setMoneySpent(moneySpent)
                .setPurchaseOrder(listing.isPurchaseOrder())
                .setPurchasedTime(now), MAX_PURCHASE_RECORDS);
        savedData.setDirty();
        return MarketResult.success(listing.isPurchaseOrder()
                ? "vss_market.message.sell_success"
                : "vss_market.message.buy_success");
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
        if ((long) listing.getStock() + count > Integer.MAX_VALUE) {
            return MarketResult.error("vss_market.message.invalid_count");
        }

        if (listing.isPurchaseOrder()) {
            long total = totalPrice(listing.getPrice(), count);
            if (total <= 0 || total > Integer.MAX_VALUE) {
                return MarketResult.error("vss_market.message.price_too_large");
            }
            if (ViScriptShopServerUtil.getMoney(owner) < total) {
                return MarketResult.error("vss_market.message.not_enough_money");
            }
            ViScriptShopServerUtil.removeMoney(owner, (int) total);
        } else {
            int itemCount = totalItems(listing.getBundleSize(), count);
            if (itemCount <= 0) {
                return MarketResult.error("vss_market.message.invalid_count");
            }
            if (ItemUtil.getItemForPlayerCount(owner, listing.unitStack()) < itemCount) {
                return MarketResult.error("vss_market.message.not_enough_item");
            }
            ItemUtil.removeItemForPlayer(owner, listing.unitStack(), itemCount);
        }
        listing.setStock(listing.getStock() + count).setUpdatedTime(System.currentTimeMillis());
        shop.setUpdatedTime(System.currentTimeMillis());
        savedData.setDirty();
        return MarketResult.success(listing.isPurchaseOrder()
                ? "vss_market.message.purchase_restocked"
                : "vss_market.message.restocked");
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
        if (listing.isPurchaseOrder()) {
            long oldTotal = totalPrice(listing.getPrice(), listing.getStock());
            long newTotal = totalPrice(price, listing.getStock());
            long difference = newTotal - oldTotal;
            if (difference > 0) {
                if (ViScriptShopServerUtil.getMoney(owner) < difference) {
                    return MarketResult.error("vss_market.message.not_enough_money");
                }
                ViScriptShopServerUtil.removeMoney(owner, (int) difference);
            } else if (difference < 0) {
                long refund = -difference;
                if (refund > Integer.MAX_VALUE || !canAddMoney(owner, (int) refund)) {
                    return MarketResult.error("vss_market.message.money_too_large");
                }
                ViScriptShopServerUtil.addMoney(owner, (int) refund);
            }
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
        if (listing.isPurchaseOrder()) {
            long refund = totalPrice(listing.getPrice(), listing.getStock());
            if (refund > Integer.MAX_VALUE || !canAddMoney(owner, (int) refund)) {
                return MarketResult.error("vss_market.message.money_too_large");
            }
            if (listing.getCollectedStock() > 0) {
                giveItem(owner, listing.unitStack(), totalItems(listing.getBundleSize(), listing.getCollectedStock()));
            }
            if (refund > 0) {
                ViScriptShopServerUtil.addMoney(owner, (int) refund);
            }
        } else if (listing.getStock() > 0) {
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
        int amount = (int) Math.min(shop.getBalance(), remainingMoneyCapacity(owner));
        if (amount <= 0) {
            return MarketResult.error("vss_market.message.money_too_large");
        }
        ViScriptShopServerUtil.addMoney(owner, amount);
        shop.setBalance(shop.getBalance() - amount).setUpdatedTime(System.currentTimeMillis());
        savedData.setDirty();
        return MarketResult.success("vss_market.message.withdrawn");
    }

    public static MarketResult deleteShop(ServerPlayer owner) {
        var savedData = data(owner);
        var shop = savedData.findShop(owner.getUUID()).orElse(null);
        if (shop == null) {
            return MarketResult.error("vss_market.message.shop_not_found");
        }
        long refund = shop.getBalance();
        for (var listing : shop.getListings()) {
            if (listing.isPurchaseOrder()) {
                long listingRefund = totalPrice(listing.getPrice(), listing.getStock());
                if (listingRefund < 0 || refund > Long.MAX_VALUE - listingRefund) {
                    return MarketResult.error("vss_market.message.money_too_large");
                }
                refund += listingRefund;
            }
        }
        if (refund > Integer.MAX_VALUE || !canAddMoney(owner, (int) refund)) {
            return MarketResult.error("vss_market.message.money_too_large");
        }
        for (var listing : shop.getListings()) {
            if (listing.isPurchaseOrder()) {
                if (listing.getCollectedStock() > 0 && !listing.getItem().isEmpty()) {
                    giveItem(owner, listing.unitStack(), totalItems(listing.getBundleSize(), listing.getCollectedStock()));
                }
            } else if (listing.getStock() > 0 && !listing.getItem().isEmpty()) {
                giveItem(owner, listing.unitStack(), totalItems(listing.getBundleSize(), listing.getStock()));
            }
        }
        if (refund > 0) {
            ViScriptShopServerUtil.addMoney(owner, (int) refund);
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

    private static long totalPrice(int price, int stock) {
        return (long) price * stock;
    }

    private static boolean canAddMoney(ServerPlayer player, int amount) {
        return amount >= 0 && amount <= remainingMoneyCapacity(player);
    }

    private static long remainingMoneyCapacity(ServerPlayer player) {
        return (long) Integer.MAX_VALUE - ViScriptShopServerUtil.getMoney(player);
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
