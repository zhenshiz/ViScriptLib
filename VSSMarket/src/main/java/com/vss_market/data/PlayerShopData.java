package com.vss_market.data;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib2.utils.PersistedParser;
import com.lowdragmc.lowdraglib2.utils.codec.StreamCodec;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class PlayerShopData implements IPersistedSerializable {
    public static final Codec<PlayerShopData> CODEC = PersistedParser.createCodec(PlayerShopData::new);
    public static final StreamCodec<ByteBuf, PlayerShopData> STREAM_CODEC = PersistedParser.createStreamCodec(PlayerShopData::new);

    @Persisted
    private UUID ownerId = new UUID(0L, 0L);
    @Persisted
    private String ownerName = "";
    @Persisted
    private String ownerTexture = "";
    @Persisted
    private String ownerTextureSignature = "";
    @Persisted
    private String name = "";
    @Persisted
    private long balance;
    @Persisted
    private long createdTime;
    @Persisted
    private long updatedTime;
    @Persisted
    private final List<MarketListing> listings = new ArrayList<>();
    @Persisted
    private final List<MarketPurchaseRecord> purchaseRecords = new ArrayList<>();

    public Optional<MarketListing> findListing(String listingId) {
        return listings.stream().filter(listing -> listing.getId().equals(listingId)).findFirst();
    }

    public boolean removeListing(String listingId) {
        return listings.removeIf(listing -> listing.getId().equals(listingId));
    }

    public int availableListingCount() {
        int count = 0;
        for (var listing : listings) {
            if (listing.getStock() > 0) {
                count++;
            }
        }
        return count;
    }

    public void addPurchaseRecord(MarketPurchaseRecord record, int limit) {
        if (record == null || limit <= 0) {
            return;
        }
        purchaseRecords.add(0, record);
        while (purchaseRecords.size() > limit) {
            purchaseRecords.remove(purchaseRecords.size() - 1);
        }
    }
}
