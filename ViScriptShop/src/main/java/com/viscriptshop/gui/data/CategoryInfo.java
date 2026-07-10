package com.viscriptshop.gui.data;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.configurator.accessors.ItemStackAccessor;
import com.lowdragmc.lowdraglib2.configurator.annotation.ConfigSelector;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.lowdragmc.lowdraglib2.configurator.ui.ConfiguratorGroup;
import com.lowdragmc.lowdraglib2.configurator.ui.StringConfigurator;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib2.utils.PersistedParser;
import com.lowdragmc.lowdraglib2.utils.codec.StreamCodec;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import lombok.*;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

//分类信息
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryInfo implements IConfigurable, IPersistedSerializable {
    public static final StreamCodec<ByteBuf, CategoryInfo> STREAM_CODEC;
    public static final Codec<CategoryInfo> CODEC;

    @Configurable(name = "viscript_shop.data.category.id")
    private String id = UUID.randomUUID().toString();
    @Configurable(name = "viscript_shop.data.category.shopType")
    private ShopType shopType = ShopType.ITEM_FOR_ITEM;
    @Configurable(name = "viscript_shop.data.category.iconType")
    @ConfigSelector(subConfiguratorBuilder = "iconTypeSubConfiguratorBuilder")
    private IconType iconType = IconType.ITEM;
    @Persisted
    private ItemStack iconItem = ItemStack.EMPTY;
    @Persisted
    private String iconTexture = "";
    @Configurable(name = "viscript_shop.data.category.name")
    private String name = "";
    @Persisted
    private List<MerchantInfo> merchants = new ArrayList<>();

    static {
        CODEC = PersistedParser.createCodec(CategoryInfo::new);
        STREAM_CODEC = PersistedParser.createStreamCodec(CategoryInfo::new);
    }

    @SneakyThrows
    private void iconTypeSubConfiguratorBuilder(IconType value, ConfiguratorGroup group) {
        switch (value) {
            case ITEM -> {
                group.addConfigurator(new ItemStackAccessor().create("viscript_shop.data.category.iconItem", this::getIconItem, this::setIconItem, true, this.getClass().getDeclaredField("iconItem"), this));
            }
            case TEXTURE -> {
                group.addConfigurator(new StringConfigurator("viscript_shop.data.category.iconTexture", this::getIconTexture, this::setIconTexture, iconTexture, true).setResourceLocation(true));
            }
        }
    }

    @Getter
    @AllArgsConstructor
    public enum IconType implements StringRepresentable {
        ITEM("viscript_shop.data.category.iconType.item"),
        TEXTURE("viscript_shop.data.category.iconType.texture");

        private final String name;

        @Override
        public @NotNull String getSerializedName() {
            return name;
        }
    }

    @Getter
    @AllArgsConstructor
    public enum ShopType implements StringRepresentable {
        ITEM_FOR_ITEM("viscript_shop.data.category.shopType.item_for_item"),
        CURRENCY("viscript_shop.data.category.shopType.currency");

        private final String name;

        @Override
        public @NotNull String getSerializedName() {
            return name;
        }
    }
}
