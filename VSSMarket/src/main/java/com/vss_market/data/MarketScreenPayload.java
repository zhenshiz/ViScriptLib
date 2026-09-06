package com.vss_market.data;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib2.utils.PersistedParser;
import com.lowdragmc.lowdraglib2.utils.codec.StreamCodec;
import com.mojang.serialization.Codec;
import com.viscriptshop.gui.components.Message;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class MarketScreenPayload implements IPersistedSerializable {
    public static final Codec<MarketScreenPayload> CODEC = PersistedParser.createCodec(MarketScreenPayload::new);
    public static final StreamCodec<ByteBuf, MarketScreenPayload> STREAM_CODEC = PersistedParser.createStreamCodec(MarketScreenPayload::new);

    @Persisted
    private MarketSavedData market = new MarketSavedData();
    @Persisted
    private UUID viewer = new UUID(0L, 0L);
    @Persisted
    private String selectedShop = "";
    @Persisted
    private String selectedListing = "";
    @Persisted
    private String view = "MARKET";
    @Persisted
    private double money;
    @Persisted
    private String messageKey = "";
    @Persisted
    private String messageType = Message.Type.INFO.name();
    @Persisted
    private ItemStack uploadStack = ItemStack.EMPTY;
    @Persisted
    private double uploadPrice = 1;
    @Persisted
    private int uploadBundleSize = 1;
    @Persisted
    private int uploadStock = 1;
    @Persisted
    private boolean uploadPurchaseOrder;
    @Persisted
    private float shopListScroll;
    @Persisted
    private float manageListingScroll;

    public Message.Type parsedMessageType() {
        try {
            return Message.Type.valueOf(messageType);
        } catch (Exception ignored) {
            return Message.Type.INFO;
        }
    }
}
