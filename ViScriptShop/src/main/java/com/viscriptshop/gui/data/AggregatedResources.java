package com.viscriptshop.gui.data;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib2.utils.PersistedParser;
import com.lowdragmc.lowdraglib2.utils.codec.StreamCodec;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.*;

/**
 * 汇总购物车中所需支付或获得的物品、货币和经验值。
 * 使用 LDLib2 原生的 @Persisted + MapAccessor 进行序列化，包括 Map<ItemStack, Integer>。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AggregatedResources implements IPersistedSerializable {
    public static final StreamCodec<ByteBuf, AggregatedResources> STREAM_CODEC;
    public static final Codec<AggregatedResources> CODEC;

    @Persisted
    private Map<ItemStack, Integer> items = new HashMap<>();
    @Persisted
    private List<ItemEntry> itemEntries = new ArrayList<>();
    @Persisted
    private List<String> commands = new ArrayList<>();
    @Persisted
    private int totalMoney = 0;
    @Persisted
    private int totalXp = 0;
    @Persisted
    private List<PurchaseEntry> purchaseEntries = new ArrayList<>();

    static {
        CODEC = PersistedParser.createCodec(AggregatedResources::new);
        STREAM_CODEC = PersistedParser.createStreamCodec(AggregatedResources::new);
    }

    /**
     * 购买条目，记录具体购买了哪个商品多少数量
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PurchaseEntry implements IPersistedSerializable {
        public static final StreamCodec<ByteBuf, PurchaseEntry> STREAM_CODEC;
        public static final Codec<PurchaseEntry> CODEC;

        @Persisted
        private String categoryId;
        @Persisted
        private String merchantId;
        @Persisted
        private int buyCount;

        static {
            CODEC = PersistedParser.createCodec(PurchaseEntry::new);
            STREAM_CODEC = PersistedParser.createStreamCodec(PurchaseEntry::new);
        }
    }

    /**
     * 物品消耗条目，除了物品和数量外还保存组件比较规则。
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ItemEntry implements IPersistedSerializable {
        public static final StreamCodec<ByteBuf, ItemEntry> STREAM_CODEC;
        public static final Codec<ItemEntry> CODEC;

        @Persisted
        private ItemStack itemStack = ItemStack.EMPTY;
        @Persisted
        private int count = 0;
        @Persisted
        private ItemMatchRule matchRule = new ItemMatchRule();

        static {
            CODEC = PersistedParser.createCodec(ItemEntry::new);
            STREAM_CODEC = PersistedParser.createStreamCodec(ItemEntry::new);
        }

        public boolean canMerge(ItemStack stack, ItemMatchRule rule) {
            return hasSameRule(rule) && safeRule().matches(itemStack, stack);
        }

        public boolean hasSameRule(ItemMatchRule rule) {
            ItemMatchRule otherRule = rule == null ? new ItemMatchRule() : rule;
            return safeRule().resolvedCompareMode() == otherRule.resolvedCompareMode()
                    && componentSet(safeRule()).equals(componentSet(otherRule));
        }

        public ItemEntry copyWithCount(int count) {
            ItemStack stack = itemStack.copy();
            stack.setCount(1);
            return new ItemEntry(stack, count, safeRule().copy());
        }

        public int getItemForPlayerCount(ServerPlayer player) {
            return safeRule().getItemForPlayerCount(player, itemStack);
        }

        public void removeItemForPlayer(ServerPlayer player) {
            safeRule().removeItemForPlayer(player, itemStack, count);
        }

        private ItemMatchRule safeRule() {
            return matchRule == null ? new ItemMatchRule() : matchRule;
        }

        private static Set<String> componentSet(ItemMatchRule rule) {
            return new HashSet<>(rule.resolvedComponents());
        }
    }

    public boolean isEmpty() {
        return purchaseEntries.isEmpty();
    }

    public long getTotalItemCount() {
        long total = 0L;
        for (int count : items.values()) {
            total += count;
        }
        return total;
    }

    /**
     * 将一个 ItemStack 合并到汇总中。
     *
     * @param stack 要合并的物品（通常数量为1，但也可以是任意数量）
     * @param count 购买数量 (buyCount)
     */
    public void addItem(ItemStack stack, int count) {
        if (stack.isEmpty() || count <= 0) return;

        // 计算总数量
        int totalQuantity = stack.getCount() * count;

        // 尝试找到已存在的相同物品
        ItemStack foundKey = null;
        for (ItemStack key : items.keySet()) {
            if (ItemStack.isSameItemSameTags(stack, key)) {
                foundKey = key;
                break;
            }
        }

        if (foundKey != null) {
            // 更新累计数量
            items.put(foundKey, items.get(foundKey) + totalQuantity);
        } else {
            ItemStack newKey = stack.copy();
            newKey.setCount(1);
            items.put(newKey, totalQuantity);
        }
    }

    public void addItemEntry(ItemStack stack, int count, ItemMatchRule matchRule) {
        if (stack.isEmpty() || count <= 0) return;

        int totalQuantity = stack.getCount() * count;
        ItemMatchRule rule = matchRule == null ? new ItemMatchRule() : matchRule;

        for (ItemEntry entry : itemEntries) {
            if (entry.canMerge(stack, rule)) {
                entry.setCount(entry.getCount() + totalQuantity);
                addItem(stack, count);
                return;
            }
        }

        ItemStack newStack = stack.copy();
        newStack.setCount(1);
        itemEntries.add(new ItemEntry(newStack, totalQuantity, rule.copy()));
        addItem(stack, count);
    }

    /**
     * 合并货币花费。
     *
     * @param money 花费的货币值
     * @param count 购买数量
     */
    public void addMoney(int money, int count) {
        if (money > 0 && count > 0) {
            this.totalMoney += money * count;
        }
    }

    /**
     * 合并经验值。
     *
     * @param xp    获得的经验值
     * @param count 购买数量
     */
    public void addXp(int xp, int count) {
        if (xp > 0 && count > 0) {
            this.totalXp += xp * count;
        }
    }

    /**
     * 合并指令
     *
     * @param command 指令
     */
    public void addCommand(String command) {
        if (!command.isEmpty()) {
            commands.add(command);
        }
    }

    /**
     * 计算购物车中所有商品的成本（玩家需要支付的）。
     *
     * @param shopInfo 商店信息，包括各个分类里所有的购物车列表
     * @return 购物车中所有商品的成本
     */
    public static AggregatedResources getCostSummary(ShopInfo shopInfo) {
        AggregatedResources cost = new AggregatedResources();
        for (CategoryInfo categoryInfo : shopInfo.getCategoryInfos()) {
            for (MerchantInfo merchant : categoryInfo.getMerchants()) {
                int count = (int) merchant.getBuyCount();
                if (count <= 0) continue;

                // 记录购买条目
                cost.getPurchaseEntries().add(new PurchaseEntry(categoryInfo.getId(), merchant.getId(), count));

                switch (categoryInfo.getShopType()) {
                    case ITEM_FOR_ITEM -> {
                        // 以物换物商店：成本是 itemA 和 itemB
                        cost.addItemEntry(merchant.getItemA(), count, merchant.getItemAMatchRule());
                        cost.addItemEntry(merchant.getItemB(), count, merchant.getItemBMatchRule());
                    }
                    case CURRENCY -> {
                        switch (merchant.getTradeType()) {
                            case BUY -> // 购买物品：成本是货币
                                    cost.addMoney(merchant.getMoney(), count);
                            case SELL -> // 出售物品：成本是玩家出售的物品 (itemResult)
                                    cost.addItemEntry(merchant.getItemResult(), count, null);
                        }
                    }
                }
            }
        }
        return cost;
    }

    /**
     * 计算购物车中所有商品的收益（玩家可以获得的）。
     *
     * @param shopInfo 商店信息，包括各个分类里所有的购物车列表
     * @return 购物车中所有商品的收益
     */
    public static AggregatedResources getGainSummary(ShopInfo shopInfo) {
        AggregatedResources gain = new AggregatedResources();
        for (CategoryInfo categoryInfo : shopInfo.getCategoryInfos()) {
            for (MerchantInfo merchant : categoryInfo.getMerchants()) {
                int count = (int) merchant.getBuyCount();
                if (count <= 0) continue;

                // 记录购买条目（只需要记录一次即可）
                gain.getPurchaseEntries().add(new PurchaseEntry(categoryInfo.getId(), merchant.getId(), count));

                //通用收益
                gain.addXp(merchant.getXp(), count);
                gain.addCommand(merchant.getCommand());
                switch (categoryInfo.getShopType()) {
                    case ITEM_FOR_ITEM -> {
                        // 以物换物商店：收益是 itemResult
                        gain.addItem(merchant.getItemResult(), count);
                    }
                    case CURRENCY -> {
                        // 通用货币商店：根据 TradeType 决定收益
                        switch (merchant.getTradeType()) {
                            case BUY -> {
                                // 购买物品：收益是 itemResult
                                gain.addItem(merchant.getItemResult(), count);
                            }
                            case SELL -> {
                                // 出售物品：收益是货币
                                gain.addMoney(merchant.getMoney(), count);
                            }
                        }
                    }
                }
            }
        }
        return gain;
    }
}
