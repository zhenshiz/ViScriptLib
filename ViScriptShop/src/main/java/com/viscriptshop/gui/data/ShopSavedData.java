package com.viscriptshop.gui.data;

import com.lowdragmc.lowdraglib2.Platform;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class ShopSavedData extends SavedData {
    public static final String GLOBAL_STOCK_OWNER = "__global__";
    private static final String MERCHANT_STOCKS_TAG = "__viscriptShopMerchantStocks";
    private static final String LEGACY_PERSONAL_STOCK_TAG = "__viscriptShopPersonalStock";
    private static final String STOCK_KEY_SEPARATOR = "\u001F";

    public final Map<String, ShopInfo> shopInfoMap = new HashMap<>();
    public final Map<String, Map<String, Map<String, Integer>>> merchantStocks = new HashMap<>();

    public void addShopMerchant(String shop, int categoryIndex, MerchantInfo merchantInfo) {
        ShopInfo shopInfo = shopInfoMap.get(shop);
        shopInfo.getCategoryInfos().get(categoryIndex).getMerchants().add(merchantInfo);
        setDirty();
    }

    public ShopInfo getShopInfo(String shop) {
        setDirty();
        return shopInfoMap.get(shop);
    }

    public void setShopInfo(String shop, ShopInfo shopInfo) {
        shopInfoMap.put(shop, shopInfo);
        setDirty();
    }

    public void resetShopInfo(String shop) {
        shopInfoMap.remove(shop);
        merchantStocks.remove(shop);
        setDirty();
    }

    public void reset() {
        shopInfoMap.clear();
        merchantStocks.clear();
        setDirty();
    }

    public int getMerchantStock(String shop, String stockOwner, String categoryId, String merchantId, int fallbackStock) {
        Map<String, Map<String, Integer>> shopStocks = merchantStocks.get(shop);
        if (shopStocks == null) {
            return fallbackStock;
        }
        Map<String, Integer> ownerStocks = shopStocks.get(stockOwner);
        if (ownerStocks == null) {
            return fallbackStock;
        }
        return ownerStocks.getOrDefault(getMerchantStockKey(categoryId, merchantId), fallbackStock);
    }

    public void setMerchantStock(String shop, String stockOwner, String categoryId, String merchantId, int stock) {
        merchantStocks.computeIfAbsent(shop, ignored -> new HashMap<>())
                .computeIfAbsent(stockOwner, ignored -> new HashMap<>())
                .put(getMerchantStockKey(categoryId, merchantId), stock);
        setDirty();
    }

    public void clearMerchantStock(String shop, String categoryId, String merchantId) {
        Map<String, Map<String, Integer>> shopStocks = merchantStocks.get(shop);
        if (shopStocks == null) {
            return;
        }
        String merchantStockKey = getMerchantStockKey(categoryId, merchantId);
        shopStocks.values().forEach(ownerStocks -> ownerStocks.remove(merchantStockKey));
        shopStocks.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        if (shopStocks.isEmpty()) {
            merchantStocks.remove(shop);
        }
        setDirty();
    }

    public static ShopSavedData fromNbt(CompoundTag nbt, HolderLookup.@NotNull Provider provider) {
        ShopSavedData shopSavedData = new ShopSavedData();
        CompoundTag merchantStocksTag = nbt.getCompound(MERCHANT_STOCKS_TAG);
        for (String shop : merchantStocksTag.getAllKeys()) {
            CompoundTag shopStocksTag = merchantStocksTag.getCompound(shop);
            Map<String, Map<String, Integer>> shopStocks = new HashMap<>();
            for (String stockOwner : shopStocksTag.getAllKeys()) {
                CompoundTag ownerStocksTag = shopStocksTag.getCompound(stockOwner);
                Map<String, Integer> ownerStocks = new HashMap<>();
                for (String stockKey : ownerStocksTag.getAllKeys()) {
                    ownerStocks.put(stockKey, ownerStocksTag.getInt(stockKey));
                }
                shopStocks.put(stockOwner, ownerStocks);
            }
            shopSavedData.merchantStocks.put(shop, shopStocks);
        }

        for (String shop : nbt.getAllKeys()) {
            if (shop.equals(MERCHANT_STOCKS_TAG) || shop.equals(LEGACY_PERSONAL_STOCK_TAG)) {
                continue;
            }
            ShopInfo shopInfo = Shop.deserializeRuntimeInfo(provider, nbt.getCompound(shop), true);
            shopSavedData.shopInfoMap.put(shop, shopInfo);
        }
        return shopSavedData;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag compoundTag) {
        for (Map.Entry<String, ShopInfo> entry : shopInfoMap.entrySet()) {
            compoundTag.put(entry.getKey(), Shop.serializeRuntimeNBT(Platform.getFrozenRegistry(), entry.getValue()));
        }
        if (!merchantStocks.isEmpty()) {
            CompoundTag merchantStocksTag = new CompoundTag();
            for (Map.Entry<String, Map<String, Map<String, Integer>>> shopEntry : merchantStocks.entrySet()) {
                CompoundTag shopStocksTag = new CompoundTag();
                for (Map.Entry<String, Map<String, Integer>> ownerEntry : shopEntry.getValue().entrySet()) {
                    CompoundTag ownerStocksTag = new CompoundTag();
                    for (Map.Entry<String, Integer> stockEntry : ownerEntry.getValue().entrySet()) {
                        ownerStocksTag.putInt(stockEntry.getKey(), stockEntry.getValue());
                    }
                    shopStocksTag.put(ownerEntry.getKey(), ownerStocksTag);
                }
                merchantStocksTag.put(shopEntry.getKey(), shopStocksTag);
            }
            compoundTag.put(MERCHANT_STOCKS_TAG, merchantStocksTag);
        }
        return compoundTag;
    }

    private static String getMerchantStockKey(String categoryId, String merchantId) {
        return categoryId + STOCK_KEY_SEPARATOR + merchantId;
    }
}
