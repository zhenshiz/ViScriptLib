package com.viscript_lib.gui.components.search;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib2.gui.ui.utils.UIElementProvider;
import com.lowdragmc.lowdraglib2.utils.search.IResultHandler;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;

/**
 * 附魔标签自动补全框，值类型为 {@code TagKey<Enchantment>}。
 */
public class EnchantmentTagSearchBox extends RegistrySearchBox<TagKey<Enchantment>> {

    public EnchantmentTagSearchBox() {
        this(EnchantmentTags.IN_ENCHANTING_TABLE);
    }

    public EnchantmentTagSearchBox(TagKey<Enchantment> defaultValue) {
        super(
                defaultValue,
                EnchantmentSearchBox::getEnchantmentRegistry,
                TagKey::location,
                tag -> tag.location().toString(),
                EnchantmentTagSearchBox::searchEnchantmentTags,
                UIElementProvider.optionalIconText(
                        EnchantmentTagSearchBox::createEnchantmentTagIcon,
                        tag -> Component.literal(tag.location().toString())
                )
        );
    }

    @Nullable
    public ResourceLocation getSelectedEnchantmentTagId() {
        return getSelectedId();
    }

    public String getSelectedEnchantmentTagIdString() {
        return getSelectedIdString();
    }

    public String getSelectedEnchantmentTagReferenceString() {
        return getEnchantmentTagReferenceString(getValue());
    }

    @Nullable
    public static ResourceLocation getEnchantmentTagId(@Nullable TagKey<Enchantment> tag) {
        return tag == null ? null : tag.location();
    }

    public static String getEnchantmentTagIdString(@Nullable TagKey<Enchantment> tag) {
        var id = getEnchantmentTagId(tag);
        return id == null ? "" : id.toString();
    }

    public static String getEnchantmentTagReferenceString(@Nullable TagKey<Enchantment> tag) {
        var id = getEnchantmentTagIdString(tag);
        return id.isEmpty() ? "" : "#" + id;
    }

    private static void searchEnchantmentTags(String word, IResultHandler<TagKey<Enchantment>> searchHandler) {
        var registry = EnchantmentSearchBox.getEnchantmentRegistry();
        if (registry == null) {
            return;
        }

        var lowerWord = word.toLowerCase(Locale.ROOT);
        registry.getTagNames()
                .sorted(Comparator.comparing(tag -> tag.location().toString()))
                .takeWhile(tag -> !Thread.currentThread().isInterrupted())
                .filter(tag -> matches(lowerWord, tag.location().toString()))
                .forEach(searchHandler::acceptResult);
    }

    private static IGuiTexture createEnchantmentTagIcon(TagKey<Enchantment> tag) {
        var registry = EnchantmentSearchBox.getEnchantmentRegistry();
        if (registry == null) {
            return IGuiTexture.EMPTY;
        }

        var itemStacks = new ArrayList<ItemStack>();
        for (Holder<Enchantment> holder : registry.getTagOrEmpty(tag)) {
            itemStacks.add(EnchantmentSearchBox.createEnchantedBook(holder));
            if (itemStacks.size() >= 64) {
                break;
            }
        }
        if (itemStacks.isEmpty()) {
            return IGuiTexture.EMPTY;
        }
        return new ItemStackTexture(itemStacks.toArray(ItemStack[]::new));
    }
}
