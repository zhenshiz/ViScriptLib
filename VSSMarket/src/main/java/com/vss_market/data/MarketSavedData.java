package com.vss_market.data;

import com.lowdragmc.lowdraglib2.Platform;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib2.utils.PersistedParser;
import com.lowdragmc.lowdraglib2.utils.codec.StreamCodec;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.serialization.Codec;
import com.viscript_lib.util.item.ViScriptItemStack;
import com.viscriptshop.util.MoneyUtil;
import com.vss_market.VSSMarket;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class MarketSavedData extends SavedData implements IPersistedSerializable {
    private static final String DATA_NAME = VSSMarket.MOD_ID + "_market";
    private static final String TEXTURES_PROPERTY = "textures";

    public static final Codec<MarketSavedData> CODEC = PersistedParser.createCodec(MarketSavedData::new);
    public static final StreamCodec<ByteBuf, MarketSavedData> STREAM_CODEC = PersistedParser.createStreamCodec(MarketSavedData::new);

    @Getter
    @Persisted
    private final List<PlayerShopData> shops = new ArrayList<>();

    public static MarketSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(MarketSavedData::load, MarketSavedData::new, DATA_NAME);
    }

    public static MarketSavedData load(CompoundTag tag) {
        var data = new MarketSavedData();
        data.deserializeNBT(Platform.getFrozenRegistry(), tag);
        var cleanup = data.removeInvalidItemEntries();
        if (cleanup.hasChanges()) {
            data.setDirty();
            VSSMarket.LOGGER.warn("市场数据中发现 {} 个失效商品和 {} 条失效交易记录，已自动清理，{} 枚收购预付款已转入对应店铺的待领取结算。", cleanup.listings(), cleanup.records(), MoneyUtil.format(cleanup.refund()));
        }
        return data;
    }

    private CleanupResult removeInvalidItemEntries() {
        int removedListings = 0;
        int removedRecords = 0;
        double totalRefund = 0;
        for (var shop : shops) {
            double refund = 0;
            for (int index = shop.getListings().size() - 1; index >= 0; index--) {
                var listing = shop.getListings().get(index);
                if (!hasInvalidItem(listing.getItem(), listing.unitStack())) {
                    continue;
                }
                if (listing.isPurchaseOrder()) {
                    refund = MoneyUtil.add(refund, MoneyUtil.multiply(listing.getPrice(), listing.getStock()));
                }
                shop.getListings().remove(index);
                removedListings++;
            }
            if (MoneyUtil.isPositive(refund)) {
                shop.setBalance(MoneyUtil.add(shop.getBalance(), refund));
                totalRefund = MoneyUtil.add(totalRefund, refund);
            }

            for (int index = shop.getPurchaseRecords().size() - 1; index >= 0; index--) {
                var record = shop.getPurchaseRecords().get(index);
                if (hasInvalidItem(record.getItem(), record.displayStack())) {
                    shop.getPurchaseRecords().remove(index);
                    removedRecords++;
                }
            }
        }
        return new CleanupResult(removedListings, removedRecords, totalRefund);
    }

    private static boolean hasInvalidItem(ViScriptItemStack item, ItemStack stack) {
        return item == null || item.isUnavailable() || stack.isEmpty();
    }

    private record CleanupResult(int listings, int records, double refund) {
        private boolean hasChanges() {
            return listings > 0 || records > 0;
        }
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag tag) {
        tag.merge(serializeNBT(Platform.getFrozenRegistry()));
        return tag;
    }

    public Optional<PlayerShopData> findShop(UUID ownerId) {
        return shops.stream().filter(shop -> shop.getOwnerId().equals(ownerId)).findFirst();
    }

    public Optional<PlayerShopData> findShop(String ownerId) {
        try {
            return findShop(UUID.fromString(ownerId));
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }

    public Optional<PlayerShopData> findShopByOwnerName(String ownerName) {
        if (ownerName == null || ownerName.isBlank()) {
            return Optional.empty();
        }
        var normalized = ownerName.toLowerCase(Locale.ROOT);
        return shops.stream()
                .filter(shop -> shop.getOwnerName().toLowerCase(Locale.ROOT).equals(normalized))
                .findFirst();
    }

    public Optional<PlayerShopData> findShopByCommandKey(String key) {
        var byOwnerName = findShopByOwnerName(key);
        return byOwnerName.isPresent() ? byOwnerName : findShop(key);
    }

    public PlayerShopData getOrCreateShop(UUID ownerId, String ownerName) {
        var shop = findShop(ownerId).orElse(null);
        if (shop != null) {
            var normalizedOwnerName = ownerName == null ? "" : ownerName;
            if (!normalizedOwnerName.isBlank() && !normalizedOwnerName.equals(shop.getOwnerName())) {
                shop.setOwnerName(normalizedOwnerName);
                setDirty();
            }
            return shop;
        }
        long now = System.currentTimeMillis();
        var normalizedOwnerName = ownerName == null ? "" : ownerName;
        shop = new PlayerShopData()
                .setOwnerId(ownerId)
                .setOwnerName(normalizedOwnerName)
                .setName(normalizedOwnerName)
                .setCreatedTime(now)
                .setUpdatedTime(now);
        shops.add(shop);
        setDirty();
        return shop;
    }

    public PlayerShopData getOrCreateShop(GameProfile ownerProfile) {
        var shop = getOrCreateShop(ownerProfile.getId(), ownerProfile.getName());
        if (updateShopOwnerProfile(shop, ownerProfile)) {
            setDirty();
        }
        return shop;
    }

    public boolean refreshShopOwnerProfile(GameProfile ownerProfile) {
        var shop = findShop(ownerProfile.getId()).orElse(null);
        if (shop == null || !updateShopOwnerProfile(shop, ownerProfile)) {
            return false;
        }
        setDirty();
        return true;
    }

    private static boolean updateShopOwnerProfile(PlayerShopData shop, GameProfile ownerProfile) {
        boolean changed = false;
        var ownerName = ownerProfile.getName();
        if (ownerName != null && !ownerName.isBlank() && !ownerName.equals(shop.getOwnerName())) {
            shop.setOwnerName(ownerName);
            changed = true;
        }
        Property texture = ownerProfile.getProperties().get(TEXTURES_PROPERTY).stream().findFirst().orElse(null);
        if (texture != null) {
            var signature = texture.getSignature() == null ? "" : texture.getSignature();
            if (!texture.getValue().equals(shop.getOwnerTexture()) || !signature.equals(shop.getOwnerTextureSignature())) {
                shop.setOwnerTexture(texture.getValue());
                shop.setOwnerTextureSignature(signature);
                changed = true;
            }
        }
        return changed;
    }

    public boolean removeShop(UUID ownerId) {
        boolean removed = shops.removeIf(shop -> shop.getOwnerId().equals(ownerId));
        if (removed) {
            setDirty();
        }
        return removed;
    }

}
