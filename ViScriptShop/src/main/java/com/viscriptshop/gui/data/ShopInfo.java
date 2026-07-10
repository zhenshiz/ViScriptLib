package com.viscriptshop.gui.data;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.lowdragmc.lowdraglib2.configurator.ui.BooleanConfigurator;
import com.lowdragmc.lowdraglib2.configurator.ui.ConfiguratorGroup;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib2.syncdata.annotation.SkipPersistedValue;
import com.lowdragmc.lowdraglib2.utils.PersistedParser;
import com.lowdragmc.lowdraglib2.utils.codec.StreamCodec;
import com.mojang.serialization.Codec;
import com.viscriptshop.ViscriptShop;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

//商店信息
@Data
public class ShopInfo implements IConfigurable, IPersistedSerializable {
    public static final StreamCodec<ByteBuf, ShopInfo> STREAM_CODEC;
    public static final Codec<ShopInfo> CODEC;

    @Configurable(name = "viscript_shop.data.shop.name", tips = "viscript_shop.data.shop.name.tip")
    private String name = "";
    @Persisted
    private boolean isQuickOpening = false;
    @Configurable(name = "viscript_shop.data.shop.lockedMerchantVisibility")
    private LockedMerchantVisibility lockedMerchantVisibility = LockedMerchantVisibility.SHOW_WITH_LOCK;
    @Persisted
    private List<CategoryInfo> categoryInfos = new ArrayList<>();

    static {
        CODEC = PersistedParser.createCodec(ShopInfo::new);
        STREAM_CODEC = PersistedParser.createStreamCodec(ShopInfo::new);
    }

    @Override
    public void buildConfigurator(ConfiguratorGroup father) {
        IConfigurable.super.buildConfigurator(father);
        if (ViscriptShop.isFtbLibraryLoaded()) {
            BooleanConfigurator isQuickOpeningConfigurator = new BooleanConfigurator("viscript_shop.data.shop.isQuickOpening", this::isQuickOpening, this::setQuickOpening, isQuickOpening, true);
            isQuickOpeningConfigurator.setTips("viscript_shop.data.shop.isQuickOpening.tip");
            father.addConfigurators(isQuickOpeningConfigurator);
        }
    }

    @SkipPersistedValue(field = "isQuickOpening")
    public boolean skipIsQuickOpening(boolean value) {
        return !ViscriptShop.isFtbLibraryLoaded();
    }

    @Getter
    @AllArgsConstructor
    public enum LockedMerchantVisibility implements StringRepresentable {
        SHOW_WITH_LOCK("viscript_shop.data.shop.lockedItemVisibility.show_with_lock"),
        HIDDEN("viscript_shop.data.shop.lockedItemVisibility.hidden");

        private final String name;

        @Override
        public @NotNull String getSerializedName() {
            return name;
        }
    }
}
