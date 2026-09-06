package com.vss_market.data;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib2.utils.PersistedParser;
import com.lowdragmc.lowdraglib2.utils.codec.StreamCodec;
import com.mojang.serialization.Codec;
import com.viscript_lib.util.item.ViScriptItemStack;
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
public class MarketPurchaseRecord implements IPersistedSerializable {
    public static final Codec<MarketPurchaseRecord> CODEC = PersistedParser.createCodec(MarketPurchaseRecord::new);
    public static final StreamCodec<ByteBuf, MarketPurchaseRecord> STREAM_CODEC = PersistedParser.createStreamCodec(MarketPurchaseRecord::new);

    @Persisted
    private UUID buyerId = new UUID(0L, 0L);
    @Persisted
    private String buyerName = "";
    @Persisted
    private ViScriptItemStack item = new ViScriptItemStack();
    @Persisted
    private int quantity;
    @Persisted
    private double moneySpent;
    @Persisted
    private boolean purchaseOrder;
    @Persisted
    private long purchasedTime;

    public ItemStack displayStack() {
        if (item == null) {
            return ItemStack.EMPTY;
        }
        var stack = item.toItemStack();
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return stack.copyWithCount(1);
    }

    public String buyerDisplayName() {
        if (buyerName != null && !buyerName.isBlank()) {
            return buyerName;
        }
        return buyerId.toString();
    }
}
