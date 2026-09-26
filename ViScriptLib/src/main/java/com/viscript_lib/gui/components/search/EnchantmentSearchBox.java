package com.viscript_lib.gui.components.search;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib2.gui.ui.utils.UIElementProvider;
import com.lowdragmc.lowdraglib2.utils.search.IResultHandler;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;

import org.jetbrains.annotations.Nullable;
import java.util.Comparator;

/**
 * 附魔自动补全框，值类型为 {@code Holder<Enchantment>}。
 */
public class EnchantmentSearchBox extends RegistrySearchBox<Enchantment> {

    public EnchantmentSearchBox() {
        this(Enchantments.SHARPNESS);
    }

    public EnchantmentSearchBox(ResourceKey<Enchantment> defaultValue) {
        this(getEnchantmentHolder(defaultValue));
    }

    public EnchantmentSearchBox(@Nullable Enchantment defaultValue) {
        super(
                defaultValue,
                EnchantmentSearchBox::getEnchantmentRegistry,
                EnchantmentSearchBox::getEnchantmentId,
                EnchantmentSearchBox::getEnchantmentIdString,
                EnchantmentSearchBox::searchEnchantments,
                UIElementProvider.iconText(
                        EnchantmentSearchBox::createEnchantmentIcon,
                        enchantment -> enchantment.getFullname(1)
                )
        );
    }

    @Nullable
    public ResourceLocation getSelectedEnchantmentId() {
        return getSelectedId();
    }

    public String getSelectedEnchantmentIdString() {
        return getSelectedIdString();
    }

    @Nullable
    public static Enchantment getEnchantmentHolder(ResourceKey<Enchantment> key) {
        var registry = getEnchantmentRegistry();
        return registry == null ? null : registry.get(key);
    }

    @Nullable
    public static ResourceLocation getEnchantmentId(@Nullable Enchantment enchantment) {
        return enchantment == null ? null : getEnchantmentRegistry().getKey(enchantment);
    }

    public static String getEnchantmentIdString(@Nullable Enchantment enchantment) {
        var id = getEnchantmentId(enchantment);
        return id == null ? "" : id.toString();
    }

    static Registry<Enchantment> getEnchantmentRegistry() {
        return BuiltInRegistries.ENCHANTMENT;
    }

    static IGuiTexture createEnchantmentIcon(Enchantment enchantment) {
        return new ItemStackTexture(createEnchantedBook(enchantment));
    }

    static net.minecraft.world.item.ItemStack createEnchantedBook(Enchantment enchantment) {
        return EnchantedBookItem.createForEnchantment(new EnchantmentInstance(
                enchantment,
                Math.max(1, enchantment.getMaxLevel())
        ));
    }

    private static void searchEnchantments(String word, IResultHandler<Enchantment> searchHandler) {
        var registry = getEnchantmentRegistry();
        if (registry == null) {
            return;
        }

        var lowerWord = word.toLowerCase(java.util.Locale.ROOT);
        registry.stream()
                .sorted(Comparator.comparing(holder -> registry.getKey(holder).toString()))
                .takeWhile(holder -> !Thread.currentThread().isInterrupted())
                .filter(holder -> matches(lowerWord, registry.getKey(holder).toString())
                        || matches(lowerWord, holder.getFullname(1).getString()))
                .forEach(searchHandler::acceptResult);
    }
}
