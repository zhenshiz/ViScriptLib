package com.viscriptshop;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib2.utils.PersistedParser;
import com.lowdragmc.lowdraglib2.utils.codec.StreamCodec;
import com.mojang.serialization.Codec;
import com.viscriptshop.gui.data.MoneySavedData;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class ShopRegistries {
    @Getter @Setter
    static MoneySavedData moneySavedData;
    public static Money clientPlayerMoney = new Money();

    @Data
    public static class Money implements IPersistedSerializable {
        public static final Codec<Money> CODEC = PersistedParser.createCodec(Money::new);
        public static final StreamCodec<ByteBuf, Money> STREAM_CODEC = PersistedParser.createStreamCodec(Money::new);
        @Persisted
        private int money;
        @Persisted
        private List<String> flags = new ArrayList<>();
    }
}
