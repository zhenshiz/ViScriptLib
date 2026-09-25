package com.viscript_lib.gui.components.search;

import com.lowdragmc.lowdraglib2.gui.ui.utils.UIElementProvider;
import com.lowdragmc.lowdraglib2.utils.search.IResultHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

import org.jetbrains.annotations.Nullable;
import java.util.Comparator;
import java.util.Locale;

/**
 * 生物群系标签自动补全框，值类型为 {@code TagKey<Biome>}。
 */
public class BiomeTagSearchBox extends RegistrySearchBox<TagKey<Biome>> {

    public BiomeTagSearchBox() {
        this(BiomeTags.IS_OVERWORLD);
    }

    public BiomeTagSearchBox(TagKey<Biome> defaultValue) {
        super(
                defaultValue,
                BiomeSearchBox::getBiomeRegistry,
                TagKey::location,
                tag -> tag.location().toString(),
                BiomeTagSearchBox::searchBiomeTags,
                UIElementProvider.text(tag -> Component.literal(tag.location().toString()))
        );
    }

    @Nullable
    public ResourceLocation getSelectedBiomeTagId() {
        return getSelectedId();
    }

    public String getSelectedBiomeTagIdString() {
        return getSelectedIdString();
    }

    public String getSelectedBiomeTagReferenceString() {
        return getBiomeTagReferenceString(getValue());
    }

    @Nullable
    public static ResourceLocation getBiomeTagId(@Nullable TagKey<Biome> tag) {
        return tag == null ? null : tag.location();
    }

    public static String getBiomeTagIdString(@Nullable TagKey<Biome> tag) {
        var id = getBiomeTagId(tag);
        return id == null ? "" : id.toString();
    }

    public static String getBiomeTagReferenceString(@Nullable TagKey<Biome> tag) {
        var id = getBiomeTagIdString(tag);
        return id.isEmpty() ? "" : "#" + id;
    }

    private static void searchBiomeTags(String word, IResultHandler<TagKey<Biome>> searchHandler) {
        var registry = BiomeSearchBox.getBiomeRegistry();
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
}
