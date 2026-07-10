package com.vss_market.data;

import com.lowdragmc.lowdraglib2.Platform;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib2.utils.PersistedParser;
import com.lowdragmc.lowdraglib2.utils.codec.StreamCodec;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.serialization.Codec;
import com.vss_market.VSSMarket;
import lombok.Getter;
import net.minecraft.nbt.CompoundTag;
import io.netty.buffer.ByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

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
        return data;
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
