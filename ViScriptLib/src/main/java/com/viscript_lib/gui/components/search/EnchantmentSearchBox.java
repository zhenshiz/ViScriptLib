package com.viscript_lib.gui.components.search;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib2.gui.ui.utils.UIElementProvider;
import com.lowdragmc.lowdraglib2.utils.search.IResultHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
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
public class EnchantmentSearchBox extends RegistrySearchBox<Holder<Enchantment>> {

    public EnchantmentSearchBox() {
        this(Enchantments.SHARPNESS);
    }

    public EnchantmentSearchBox(ResourceKey<Enchantment> defaultValue) {
        this(getEnchantmentHolder(defaultValue));
    }

    public EnchantmentSearchBox(@Nullable Holder<Enchantment> defaultValue) {
        super(
                defaultValue,
                EnchantmentSearchBox::getEnchantmentRegistry,
                EnchantmentSearchBox::getEnchantmentId,
                EnchantmentSearchBox::getEnchantmentIdString,
                EnchantmentSearchBox::searchEnchantments,
                UIElementProvider.iconText(
                        EnchantmentSearchBox::createEnchantmentIcon,
                        enchantment -> enchantment.value().description()
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
    public static Holder.Reference<Enchantment> getEnchantmentHolder(ResourceKey<Enchantment> key) {
        var registry = getEnchantmentRegistry();
        return registry == null ? null : registry.getHolder(key).orElse(null);
    }

    @Nullable
    public static ResourceLocation getEnchantmentId(@Nullable Holder<Enchantment> enchantment) {
        return enchantment == null ? null : enchantment.unwrapKey()
                .map(ResourceKey::location)
                .orElse(null);
    }

    public static String getEnchantmentIdString(@Nullable Holder<Enchantment> enchantment) {
        var id = getEnchantmentId(enchantment);
        return id == null ? "" : id.toString();
    }

    @Nullable
    static Registry<Enchantment> getEnchantmentRegistry() {
        var minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return null;
        }
        return minecraft.level.registryAccess().registry(Registries.ENCHANTMENT).orElse(null);
    }

    static IGuiTexture createEnchantmentIcon(Holder<Enchantment> enchantment) {
        return new ItemStackTexture(createEnchantedBook(enchantment));
    }

    static net.minecraft.world.item.ItemStack createEnchantedBook(Holder<Enchantment> enchantment) {
        return EnchantedBookItem.createForEnchantment(new EnchantmentInstance(
                enchantment,
                Math.max(1, enchantment.value().getMaxLevel())
        ));
    }

    private static void searchEnchantments(String word, IResultHandler<Holder<Enchantment>> searchHandler) {
        var registry = getEnchantmentRegistry();
        if (registry == null) {
            return;
        }

        var lowerWord = word.toLowerCase(java.util.Locale.ROOT);
        registry.holders()
                .sorted(Comparator.comparing(holder -> holder.key().location().toString()))
                .takeWhile(holder -> !Thread.currentThread().isInterrupted())
                .filter(holder -> matches(lowerWord, holder.key().location().toString())
                        || matches(lowerWord, holder.value().description().getString()))
                .forEach(searchHandler::acceptResult);
    }
}
