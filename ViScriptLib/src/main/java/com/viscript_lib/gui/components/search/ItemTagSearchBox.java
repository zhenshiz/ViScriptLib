package com.viscript_lib.gui.components.search;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib2.gui.ui.utils.UIElementProvider;
import com.lowdragmc.lowdraglib2.utils.search.IResultHandler;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;

/**
 * 物品标签自动补全框，值类型为 {@code TagKey<Item>}。
 */
public class ItemTagSearchBox extends RegistrySearchBox<TagKey<Item>> {

    public ItemTagSearchBox() {
        this(ItemTags.PLANKS);
    }

    public ItemTagSearchBox(TagKey<Item> defaultValue) {
        super(
                defaultValue,
                () -> BuiltInRegistries.ITEM,
                TagKey::location,
                tag -> tag.location().toString(),
                ItemTagSearchBox::searchItemTags,
                UIElementProvider.optionalIconText(
                        ItemTagSearchBox::createItemTagIcon,
                        tag -> Component.literal(tag.location().toString())
                )
        );
    }

    @Nullable
    public ResourceLocation getSelectedItemTagId() {
        return getSelectedId();
    }

    public String getSelectedItemTagIdString() {
        return getSelectedIdString();
    }

    public String getSelectedItemTagReferenceString() {
        return getItemTagReferenceString(getValue());
    }

    @Nullable
    public static ResourceLocation getItemTagId(@Nullable TagKey<Item> tag) {
        return tag == null ? null : tag.location();
    }

    public static String getItemTagIdString(@Nullable TagKey<Item> tag) {
        var id = getItemTagId(tag);
        return id == null ? "" : id.toString();
    }

    public static String getItemTagReferenceString(@Nullable TagKey<Item> tag) {
        var id = getItemTagIdString(tag);
        return id.isEmpty() ? "" : "#" + id;
    }

    private static void searchItemTags(String word, IResultHandler<TagKey<Item>> searchHandler) {
        var lowerWord = word.toLowerCase(Locale.ROOT);
        BuiltInRegistries.ITEM.getTagNames()
                .sorted(Comparator.comparing(tag -> tag.location().toString()))
                .takeWhile(tag -> !Thread.currentThread().isInterrupted())
                .filter(tag -> matches(lowerWord, tag.location().toString()))
                .forEach(searchHandler::acceptResult);
    }

    private static IGuiTexture createItemTagIcon(TagKey<Item> tag) {
        var items = new ArrayList<Item>();
        for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(tag)) {
            items.add(holder.value());
            if (items.size() >= 64) {
                break;
            }
        }
        if (items.isEmpty()) {
            return IGuiTexture.EMPTY;
        }
        return new ItemStackTexture(items.toArray(Item[]::new));
    }
}
