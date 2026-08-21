package com.viscript_lib.gui.components.search;

import com.lowdragmc.lowdraglib2.gui.ui.utils.UIElementProvider;
import com.lowdragmc.lowdraglib2.utils.LocalizationUtils;
import com.lowdragmc.lowdraglib2.utils.search.IResultHandler;
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
public class AttributeSearchBox extends RegistrySearchBox<Attribute> {

    public AttributeSearchBox() {
        this(Attributes.MAX_HEALTH);
    }

    public AttributeSearchBox(ResourceKey<Attribute> defaultValue) {
        this(getAttributeHolder(defaultValue));
    }

    public AttributeSearchBox(@Nullable Attribute defaultValue) {
        super(
                defaultValue,
                () -> BuiltInRegistries.ATTRIBUTE,
                AttributeSearchBox::getAttributeId,
                AttributeSearchBox::getAttributeIdString,
                AttributeSearchBox::searchAttributes,
                UIElementProvider.text(attribute -> Component.translatable(attribute.getDescriptionId()))
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
    public static Attribute getAttributeHolder(ResourceKey<Attribute> key) {
        return BuiltInRegistries.ATTRIBUTE.get(key);
    }

    @Nullable
    public static ResourceLocation getAttributeId(@Nullable Attribute attribute) {
        return attribute == null ? null : BuiltInRegistries.ATTRIBUTE.getKey(attribute);
    }

    public static String getAttributeIdString(@Nullable Attribute attribute) {
        var id = getAttributeId(attribute);
        return id == null ? "" : id.toString();
    }

    public static String getAttributeDescriptionId(@Nullable Attribute attribute) {
        return attribute == null ? "" : attribute.getDescriptionId();
    }

    public static double getAttributeDefaultValue(@Nullable Attribute attribute) {
        return attribute == null ? 0.0 : attribute.getDefaultValue();
    }

    private static void searchAttributes(String word, IResultHandler<Attribute> searchHandler) {
        var lowerWord = word.toLowerCase(Locale.ROOT);
        BuiltInRegistries.ATTRIBUTE.stream()
                .sorted(Comparator.comparing(AttributeSearchBox::getAttributeIdString))
                .takeWhile(holder -> !Thread.currentThread().isInterrupted())
                .filter(holder -> matches(lowerWord, getAttributeIdString(holder))
                        || matches(lowerWord, LocalizationUtils.format(getAttributeDescriptionId(holder))))
                .forEach(searchHandler::acceptResult);
    }
}
