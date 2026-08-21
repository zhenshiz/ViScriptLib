package com.viscript_lib.gui.components.search;

import com.lowdragmc.lowdraglib2.gui.ui.utils.UIElementProvider;
import com.lowdragmc.lowdraglib2.utils.search.IResultHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;

import org.jetbrains.annotations.Nullable;
import java.util.Comparator;
import java.util.Locale;

/**
 * 伤害类型标签自动补全框，值类型为 {@code TagKey<DamageType>}。
 */
public class DamageTypeTagSearchBox extends RegistrySearchBox<TagKey<DamageType>> {

    public DamageTypeTagSearchBox() {
        this(DamageTypeTags.IS_FIRE);
    }

    public DamageTypeTagSearchBox(TagKey<DamageType> defaultValue) {
        super(
                defaultValue,
                DamageTypeSearchBox::getDamageTypeRegistry,
                TagKey::location,
                tag -> tag.location().toString(),
                DamageTypeTagSearchBox::searchDamageTypeTags,
                UIElementProvider.text(tag -> Component.literal(tag.location().toString()))
        );
    }

    @Nullable
    public ResourceLocation getSelectedDamageTypeTagId() {
        return getSelectedId();
    }

    public String getSelectedDamageTypeTagIdString() {
        return getSelectedIdString();
    }

    public String getSelectedDamageTypeTagReferenceString() {
        return getDamageTypeTagReferenceString(getValue());
    }

    @Nullable
    public static ResourceLocation getDamageTypeTagId(@Nullable TagKey<DamageType> tag) {
        return tag == null ? null : tag.location();
    }

    public static String getDamageTypeTagIdString(@Nullable TagKey<DamageType> tag) {
        var id = getDamageTypeTagId(tag);
        return id == null ? "" : id.toString();
    }

    public static String getDamageTypeTagReferenceString(@Nullable TagKey<DamageType> tag) {
        var id = getDamageTypeTagIdString(tag);
        return id.isEmpty() ? "" : "#" + id;
    }

    private static void searchDamageTypeTags(String word, IResultHandler<TagKey<DamageType>> searchHandler) {
        var registry = DamageTypeSearchBox.getDamageTypeRegistry();
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
