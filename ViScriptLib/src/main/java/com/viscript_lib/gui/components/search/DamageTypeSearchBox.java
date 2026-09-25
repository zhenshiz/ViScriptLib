package com.viscript_lib.gui.components.search;

import com.lowdragmc.lowdraglib2.gui.ui.utils.UIElementProvider;
import com.lowdragmc.lowdraglib2.utils.search.IResultHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;

import org.jetbrains.annotations.Nullable;
import java.util.Comparator;
import java.util.Locale;

/**
 * 伤害类型自动补全框，值类型为 {@code Holder<DamageType>}。
 */
public class DamageTypeSearchBox extends RegistrySearchBox<Holder<DamageType>> {

    public DamageTypeSearchBox() {
        this(DamageTypes.GENERIC);
    }

    public DamageTypeSearchBox(ResourceKey<DamageType> defaultValue) {
        this(getDamageTypeHolder(defaultValue));
    }

    public DamageTypeSearchBox(@Nullable Holder<DamageType> defaultValue) {
        super(
                defaultValue,
                DamageTypeSearchBox::getDamageTypeRegistry,
                DamageTypeSearchBox::getDamageTypeId,
                DamageTypeSearchBox::getDamageTypeIdString,
                DamageTypeSearchBox::searchDamageTypes,
                UIElementProvider.text(damageType -> Component.literal(getDamageTypeIdString(damageType)))
        );
    }

    @Nullable
    public ResourceLocation getSelectedDamageTypeId() {
        return getSelectedId();
    }

    public String getSelectedDamageTypeIdString() {
        return getSelectedIdString();
    }

    public String getSelectedDamageTypeMessageId() {
        return getDamageTypeMessageId(getValue());
    }

    @Nullable
    public static Holder.Reference<DamageType> getDamageTypeHolder(ResourceKey<DamageType> key) {
        var registry = getDamageTypeRegistry();
        return registry == null ? null : registry.getHolder(key).orElse(null);
    }

    @Nullable
    public static ResourceLocation getDamageTypeId(@Nullable Holder<DamageType> damageType) {
        return damageType == null ? null : damageType.unwrapKey()
                .map(ResourceKey::location)
                .orElse(null);
    }

    public static String getDamageTypeIdString(@Nullable Holder<DamageType> damageType) {
        var id = getDamageTypeId(damageType);
        return id == null ? "" : id.toString();
    }

    public static String getDamageTypeMessageId(@Nullable Holder<DamageType> damageType) {
        return damageType == null ? "" : damageType.value().msgId();
    }

    public static String getDamageTypeDeathMessageKey(@Nullable Holder<DamageType> damageType) {
        var messageId = getDamageTypeMessageId(damageType);
        return messageId.isEmpty() ? "" : "death.attack." + messageId;
    }

    @Nullable
    static Registry<DamageType> getDamageTypeRegistry() {
        var minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return null;
        }
        return minecraft.level.registryAccess().registry(Registries.DAMAGE_TYPE).orElse(null);
    }

    private static void searchDamageTypes(String word, IResultHandler<Holder<DamageType>> searchHandler) {
        var registry = getDamageTypeRegistry();
        if (registry == null) {
            return;
        }

        var lowerWord = word.toLowerCase(Locale.ROOT);
        registry.holders()
                .sorted(Comparator.comparing(holder -> holder.key().location().toString()))
                .takeWhile(holder -> !Thread.currentThread().isInterrupted())
                .filter(holder -> matches(lowerWord, holder.key().location().toString())
                        || matches(lowerWord, holder.value().msgId())
                        || matches(lowerWord, Language.getInstance().getOrDefault(getDamageTypeDeathMessageKey(holder))))
                .forEach(searchHandler::acceptResult);
    }
}
