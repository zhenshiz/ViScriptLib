package com.viscript_lib.gui.components.search;

import com.lowdragmc.lowdraglib2.gui.ui.utils.UIElementProvider;
import com.lowdragmc.lowdraglib2.utils.LocalizationUtils;
import com.lowdragmc.lowdraglib2.utils.search.IResultHandler;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;

import org.jetbrains.annotations.Nullable;
import java.util.Comparator;
import java.util.Locale;

/**
 * 实体属性自动补全框，值类型为 {@code Holder<Attribute>}。
 */
public class AttributeSearchBox extends RegistrySearchBox<Holder<Attribute>> {

    public AttributeSearchBox() {
        this(Attributes.MAX_HEALTH);
    }

    public AttributeSearchBox(ResourceKey<Attribute> defaultValue) {
        this(getAttributeHolder(defaultValue));
    }

    public AttributeSearchBox(@Nullable Holder<Attribute> defaultValue) {
        super(
                defaultValue,
                () -> BuiltInRegistries.ATTRIBUTE,
                AttributeSearchBox::getAttributeId,
                AttributeSearchBox::getAttributeIdString,
                AttributeSearchBox::searchAttributes,
                UIElementProvider.text(attribute -> Component.translatable(attribute.value().getDescriptionId()))
        );
    }

    @Nullable
    public ResourceLocation getSelectedAttributeId() {
        return getSelectedId();
    }

    public String getSelectedAttributeIdString() {
        return getSelectedIdString();
    }

    public String getSelectedAttributeDescriptionId() {
        return getAttributeDescriptionId(getValue());
    }

    public double getSelectedAttributeDefaultValue() {
        return getAttributeDefaultValue(getValue());
    }

    @Nullable
    public static Holder.Reference<Attribute> getAttributeHolder(ResourceKey<Attribute> key) {
        return BuiltInRegistries.ATTRIBUTE.getHolder(key).orElse(null);
    }

    @Nullable
    public static ResourceLocation getAttributeId(@Nullable Holder<Attribute> attribute) {
        return attribute == null ? null : attribute.unwrapKey()
                .map(ResourceKey::location)
                .orElse(null);
    }

    public static String getAttributeIdString(@Nullable Holder<Attribute> attribute) {
        var id = getAttributeId(attribute);
        return id == null ? "" : id.toString();
    }

    public static String getAttributeDescriptionId(@Nullable Holder<Attribute> attribute) {
        return attribute == null ? "" : attribute.value().getDescriptionId();
    }

    public static double getAttributeDefaultValue(@Nullable Holder<Attribute> attribute) {
        return attribute == null ? 0.0 : attribute.value().getDefaultValue();
    }

    private static void searchAttributes(String word, IResultHandler<Holder<Attribute>> searchHandler) {
        var lowerWord = word.toLowerCase(Locale.ROOT);
        BuiltInRegistries.ATTRIBUTE.holders()
                .sorted(Comparator.comparing(holder -> holder.key().location().toString()))
                .takeWhile(holder -> !Thread.currentThread().isInterrupted())
                .filter(holder -> matches(lowerWord, holder.key().location().toString())
                        || matches(lowerWord, LocalizationUtils.format(holder.value().getDescriptionId())))
                .forEach(searchHandler::acceptResult);
    }
}
