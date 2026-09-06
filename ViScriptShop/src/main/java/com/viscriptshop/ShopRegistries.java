package com.viscriptshop;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib2.utils.PersistedParser;
import com.lowdragmc.lowdraglib2.utils.codec.StreamCodec;
import com.mojang.serialization.Codec;
import com.viscript_lib.util.item.ItemOutputTargets;
import com.viscriptshop.gui.data.MoneySavedData;
import com.viscriptshop.util.MoneyUtil;
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
        private double money;
        @Persisted
        private List<String> flags = new ArrayList<>();
        @Persisted
        private String outputTargetId = ItemOutputTargets.PLAYER_INVENTORY;
        /** 玩家偏好的虚拟货币商品布局；旧存档未配置时仍使用列表。 */
        @Persisted
        private boolean currencyGridLayout;

        /**
         * 设置玩家持有的 VSS 货币余额。
         *
         * <p>负数、非数字和无穷值均会被规范化为零。
         *
         * @param money 新的货币余额
         */
        public void setMoney(double money) {
            this.money = MoneyUtil.normalize(money);
        }
    }
}
