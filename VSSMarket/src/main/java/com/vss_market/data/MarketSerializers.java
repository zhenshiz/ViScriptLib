package com.vss_market.data;

import com.lowdragmc.lowdraglib2.syncdata.AccessorRegistries;
import com.lowdragmc.lowdraglib2.syncdata.accessor.direct.CustomDirectAccessor;
import com.lowdragmc.lowdraglib2.utils.codec.StreamCodec;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import lombok.experimental.UtilityClass;

@UtilityClass
public class MarketSerializers {
    private boolean registered;

    public synchronized void register() {
        if (registered) {
            return;
        }
        register(MarketListing.class, MarketListing.CODEC, MarketListing.STREAM_CODEC);
        register(PlayerShopData.class, PlayerShopData.CODEC, PlayerShopData.STREAM_CODEC);
        register(MarketSavedData.class, MarketSavedData.CODEC, MarketSavedData.STREAM_CODEC);
        register(MarketScreenPayload.class, MarketScreenPayload.CODEC, MarketScreenPayload.STREAM_CODEC);
        registered = true;
    }

    private <T> void register(Class<T> type, Codec<T> codec, StreamCodec<? super ByteBuf, T> streamCodec) {
        AccessorRegistries.registerAccessor(CustomDirectAccessor.builder(type)
                .codec(codec)
                .streamCodec(streamCodec)
                .codecMark()
                .build(), 900);
    }
}
