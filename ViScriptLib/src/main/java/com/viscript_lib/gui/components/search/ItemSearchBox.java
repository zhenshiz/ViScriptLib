package com.viscript_lib.gui.components.search;

import com.lowdragmc.lowdraglib2.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib2.gui.ui.utils.UIElementProvider;
import com.lowdragmc.lowdraglib2.utils.LocalizationUtils;
import com.lowdragmc.lowdraglib2.utils.search.IResultHandler;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import org.jetbrains.annotations.Nullable;

/**
 * 只选择物品类型本身的自动补全框，不包含数量和组件信息。
 */
public class ItemSearchBox extends RegistrySearchBox<Item> {

    public ItemSearchBox() {
        this(Items.AIR);
    }

    public ItemSearchBox(Item defaultValue) {
        super(
                defaultValue,
                () -> BuiltInRegistries.ITEM,
                BuiltInRegistries.ITEM::getKey,
                item -> idString(BuiltInRegistries.ITEM.getKey(item)),
                ItemSearchBox::searchItems,
                UIElementProvider.iconText(
                        ItemStackTexture::new,
                        item -> Component.translatable(item.getDescriptionId())
                )
        );
    }

    @Nullable
    public ResourceLocation getSelectedItemId() {
        return getSelectedId();
    }

    public String getSelectedItemIdString() {
        return getSelectedIdString();
    }

    @Nullable
    public static ResourceLocation getItemId(@Nullable Item item) {
        return item == null ? null : BuiltInRegistries.ITEM.getKey(item);
    }

    public static String getItemIdString(@Nullable Item item) {
        var id = getItemId(item);
        return id == null ? "" : id.toString();
    }

    private static void searchItems(String word, IResultHandler<Item> searchHandler) {
        searchRegistry(
                BuiltInRegistries.ITEM,
                word,
                searchHandler,
                item -> LocalizationUtils.format(item.getDescriptionId())
        );
    }
}
