package com.viscript_lib.gui.components.search;

import com.lowdragmc.lowdraglib2.gui.ui.utils.UIElementProvider;
import com.lowdragmc.lowdraglib2.utils.LocalizationUtils;
import com.lowdragmc.lowdraglib2.utils.search.IResultHandler;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

import org.jetbrains.annotations.Nullable;
import java.util.Comparator;
import java.util.Locale;

/**
 * 生物群系自动补全框，值类型为 {@code Holder<Biome>}。
 */
public class BiomeSearchBox extends RegistrySearchBox<Holder<Biome>> {

    public BiomeSearchBox() {
        this(Biomes.PLAINS);
    }

    public BiomeSearchBox(ResourceKey<Biome> defaultValue) {
        this(getBiomeHolder(defaultValue));
    }

    public BiomeSearchBox(@Nullable Holder<Biome> defaultValue) {
        super(
                defaultValue,
                BiomeSearchBox::getBiomeRegistry,
                BiomeSearchBox::getBiomeId,
                BiomeSearchBox::getBiomeIdString,
                BiomeSearchBox::searchBiomes,
                UIElementProvider.text(BiomeSearchBox::getBiomeDisplayName)
        );
    }

    @Nullable
    public ResourceLocation getSelectedBiomeId() {
        return getSelectedId();
    }

    public String getSelectedBiomeIdString() {
        return getSelectedIdString();
    }

    @Nullable
    public static Holder.Reference<Biome> getBiomeHolder(ResourceKey<Biome> key) {
        var registry = getBiomeRegistry();
        return registry == null ? null : registry.getHolder(key).orElse(null);
    }

    @Nullable
    public static ResourceLocation getBiomeId(@Nullable Holder<Biome> biome) {
        return biome == null ? null : biome.unwrapKey()
                .map(ResourceKey::location)
                .orElse(null);
    }

    public static String getBiomeIdString(@Nullable Holder<Biome> biome) {
        var id = getBiomeId(biome);
        return id == null ? "" : id.toString();
    }

    public static String getBiomeTranslationKey(@Nullable Holder<Biome> biome) {
        return getBiomeTranslationKey(getBiomeId(biome));
    }

    public static String getBiomeTranslationKey(@Nullable ResourceLocation id) {
        return id == null ? "" : Util.makeDescriptionId("biome", id);
    }

    public static Component getBiomeDisplayName(@Nullable Holder<Biome> biome) {
        var translationKey = getBiomeTranslationKey(biome);
        return translationKey.isEmpty() ? Component.empty() : Component.translatable(translationKey);
    }

    @Nullable
    static Registry<Biome> getBiomeRegistry() {
        var minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return null;
        }
        return minecraft.level.registryAccess().registry(Registries.BIOME).orElse(null);
    }

    private static void searchBiomes(String word, IResultHandler<Holder<Biome>> searchHandler) {
        var registry = getBiomeRegistry();
        if (registry == null) {
            return;
        }

        var lowerWord = word.toLowerCase(Locale.ROOT);
        registry.holders()
                .sorted(Comparator.comparing(holder -> holder.key().location().toString()))
                .takeWhile(holder -> !Thread.currentThread().isInterrupted())
                .filter(holder -> matches(lowerWord, holder.key().location().toString())
                        || matches(lowerWord, LocalizationUtils.format(getBiomeTranslationKey(holder))))
                .forEach(searchHandler::acceptResult);
    }
}
