package com.viscriptshop.gui.data;

import com.lowdragmc.lowdraglib2.Platform;
import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.configurator.annotation.ConfigNumber;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.lowdragmc.lowdraglib2.configurator.ui.Configurator;
import com.lowdragmc.lowdraglib2.configurator.ui.ConfiguratorGroup;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib2.utils.PersistedParser;
import com.lowdragmc.lowdraglib2.utils.codec.StreamCodec;
import com.mojang.serialization.Codec;
import com.viscript_lib.util.CodecUtil;
import com.viscriptshop.gui.components.MerchantFlagGroupsConfigurator;
import dev.vfyjxf.taffy.style.TaffyDisplay;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.minecraft.nbt.Tag;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MerchantInfo implements IConfigurable, IPersistedSerializable {
    public static final StreamCodec<ByteBuf, MerchantInfo> STREAM_CODEC;
    public static final Codec<MerchantInfo> CODEC;

    //以物换物商店
    @Configurable(name = "viscript_shop.data.merchant.itemA")
    private ItemStack itemA = ItemStack.EMPTY;
    @Configurable(name = "viscript_shop.data.merchant.itemA.matchRule", subConfigurable = true)
    private ItemMatchRule itemAMatchRule = new ItemMatchRule();
    @Configurable(name = "viscript_shop.data.merchant.itemB")
    private ItemStack itemB = ItemStack.EMPTY;
    @Configurable(name = "viscript_shop.data.merchant.itemB.matchRule", subConfigurable = true)
    private ItemMatchRule itemBMatchRule = new ItemMatchRule();
    //通用货币商店
    @Configurable(name = "viscript_shop.data.merchant.money")
    @ConfigNumber(range = {0, Integer.MAX_VALUE})
    private int money = 0;
    @Configurable(name = "viscript_shop.data.merchant.tradeType")
    private TradeType tradeType = TradeType.BUY;
    //通用参数
    @Configurable(name = "viscript_shop.data.merchant.id")
    private String id = UUID.randomUUID().toString();
    @Configurable(name = "viscript_shop.data.merchant.itemResult")
    private ItemStack itemResult = ItemStack.EMPTY;
    @Configurable(name = "viscript_shop.data.merchant.stock", tips = "viscript_shop.data.merchant.stock.tips")
    @ConfigNumber(range = {-1, Integer.MAX_VALUE}, wheel = 1)
    private int stock = -1;
    @Configurable(name = "viscript_shop.data.merchant.xp")
    @ConfigNumber(range = {0, Integer.MAX_VALUE})
    private int xp = 0;
    @Configurable(name = "viscript_shop.data.merchant.command", tips = "viscript_shop.data.merchant.command.tip")
    private String command = "";
    @Persisted
    private MerchantFlagGroup.GroupMatchMode flagGroupMode = MerchantFlagGroup.GroupMatchMode.OR;
    @Persisted
    private List<MerchantFlagGroup> flagGroups = new ArrayList<>();
    //ui用参数
    private Number buyCount = 0;

    static {
        CODEC = PersistedParser.createCodec(MerchantInfo::new);
        STREAM_CODEC = PersistedParser.createStreamCodec(MerchantInfo::new);
    }

    public Configurator createConfigurator(CategoryInfo.ShopType shopType) {
        ConfiguratorGroup group = new ConfiguratorGroup();
        group.setCanCollapse(false);
        group.setCollapse(false);
        group.lineContainer.setDisplay(TaffyDisplay.NONE);
        buildConfigurator(group);
        List<Configurator> configurators = new ArrayList<>(group.getConfigurators());
        group.removeAllConfigurators();
        //以物换物商店
        List<Configurator> barterConfigurators = configurators.subList(0, 4);
        //通用货币商店
        List<Configurator> currencyConfigurators = configurators.subList(4, 6);
        //商品唯一标识
        Configurator idConfigurator = configurators.get(6);
        //通用参数
        List<Configurator> commonConfigurators = configurators.subList(7, configurators.size());
        group.addConfigurator(idConfigurator);
        switch (shopType) {
            case ITEM_FOR_ITEM -> barterConfigurators.forEach(group::addConfigurator);
            case CURRENCY -> currencyConfigurators.forEach(group::addConfigurator);
        }
        commonConfigurators.forEach(group::addConfigurator);
        group.addConfigurator(new MerchantFlagGroupsConfigurator(this));
        return group;
    }

    @Deprecated
    public List<String> getFlags() {
        if (flagGroups.size() == 1 && flagGroups.get(0).getMode() == MerchantFlagGroup.MatchMode.AND) {
            return flagGroups.get(0).getFlags();
        }
        List<String> flags = new ArrayList<>();
        for (MerchantFlagGroup group : flagGroups) {
            flags.addAll(group.normalizedFlags());
        }
        return flags;
    }

    @Deprecated
    public void setFlags(List<String> flags) {
        flagGroups.clear();
        if (flags != null && !flags.isEmpty()) {
            flagGroups.add(new MerchantFlagGroup(MerchantFlagGroup.MatchMode.AND, new ArrayList<>(flags)));
        }
    }

    public MerchantInfo copy() {
        Tag tag = CodecUtil.serializeNBT(MerchantInfo.CODEC, this, Platform.getFrozenRegistry());
        MerchantInfo copy = CodecUtil.deserializeNBT(MerchantInfo.CODEC, tag, Platform.getFrozenRegistry());
        // 生成新的UUID，确保ID唯一性
        copy.setId(UUID.randomUUID().toString());
        return copy;
    }

    public ItemMatchRule getItemAMatchRule() {
        if (itemAMatchRule == null) {
            itemAMatchRule = new ItemMatchRule();
        }
        return itemAMatchRule;
    }

    public ItemMatchRule getItemBMatchRule() {
        if (itemBMatchRule == null) {
            itemBMatchRule = new ItemMatchRule();
        }
        return itemBMatchRule;
    }

    @Getter
    @AllArgsConstructor
    public enum TradeType implements StringRepresentable {
        BUY("viscript_shop.data.merchant.tradeType.buy"),
        SELL("viscript_shop.data.merchant.tradeType.sell");

        private final String name;

        @Override
        public @NotNull String getSerializedName() {
            return name;
        }
    }
}
