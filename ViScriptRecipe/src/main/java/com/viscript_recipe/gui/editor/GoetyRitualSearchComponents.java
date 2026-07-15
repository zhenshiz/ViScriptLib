package com.viscript_recipe.gui.editor;

import com.lowdragmc.lowdraglib2.configurator.ui.RegistrySearchComponent;
import com.lowdragmc.lowdraglib2.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.utils.UIElementProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Creates searchable registry and tag inputs for Goety dark ritual properties.
 */
final class GoetyRitualSearchComponents {
    private static final ResourceLocation DEFAULT_RITUAL_TYPE = new ResourceLocation("goety", "craft");

    private GoetyRitualSearchComponents() {
    }

    /**
     * Creates an input backed by Goety's ritual behavior registry.
     *
     * @param  supplier the current ritual behavior identifier supplier
     * @param  consumer the updated ritual behavior identifier consumer
     * @param  onChanged the callback invoked after a changed selection
     * @return the searchable ritual behavior input
     */
/*    static UIElement ritualType(Supplier<ResourceLocation> supplier, Consumer<ResourceLocation> consumer,
                                Runnable onChanged) {
        var registry = ModRituals.REGISTRY;
        var defaultValue = registry.getOptional(DEFAULT_RITUAL_TYPE)
                .orElseGet(() -> registry.iterator().next());
        var configurator = new RegistrySearchComponent<>(
                "viscript_recipe.config.goety.ritual.ritual_type",
                () -> registry.getOptional(supplier.get()).orElse(defaultValue),
                value -> updateRegistryId(registry, value, supplier, consumer, onChanged),
                defaultValue,
                true,
                registry,
                UIElementProvider.text(value -> ritualTypeName(registry.getKey(value)))
        );
        configurator.setTranslator(value -> ritualTypeName(registry.getKey(value)).getString());
        return configure(configurator);
    }*/

    /**
     * Creates an enchantment input searchable by identifier and localized enchantment name.
     *
     * @param  nameKey the input label translation key
     * @param  supplier the current enchantment identifier supplier
     * @param  consumer the updated enchantment identifier consumer
     * @param  onChanged the callback invoked after a changed selection
     * @return the searchable enchantment input, or a plain identifier input when no client registry is available
     */
    static UIElement enchantment(String nameKey, Supplier<ResourceLocation> supplier,
                                 Consumer<ResourceLocation> consumer, Runnable onChanged) {
        var level = Minecraft.getInstance().level;
        if (level == null) {
            return RecipeEditorUi.fieldGroup(nameKey, RecipeEditorUi.resourceLocationField(supplier.get(), value -> {
                if (!Objects.equals(value, supplier.get())) {
                    consumer.accept(value);
                    onChanged.run();
                }
            }));
        }
        var registry = level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        var defaultValue = registry.getOptional(new ResourceLocation("sharpness"))
                .orElseGet(() -> registry.getRandom(RandomSource.create()).orElseThrow().value());
        var configurator = new RegistrySearchComponent<>(
                nameKey,
                () -> registry.getOptional(supplier.get()).orElse(defaultValue),
                value -> updateRegistryId(registry, value, supplier, consumer, onChanged),
                defaultValue,
                true,
                registry,
                UIElementProvider.iconText(
                        value -> new ItemStackTexture(new ItemStack(Items.ENCHANTED_BOOK)),
                        e -> Component.translatable(e.getDescriptionId())
                )
        );
        configurator.setTranslator(Enchantment::getDescriptionId);
        return configure(configurator);
    }

    private static UIElement configure(RegistrySearchComponent<?> configurator) {
        configurator.layout(layout -> layout.widthPercent(100));
        configurator.searchComponent.searchStyle(style -> {
            style.maxItemCount(8);
            style.scrollerViewHeight(120);
        });
        return configurator;
    }

    private static <T> void updateRegistryId(Registry<T> registry, T value, Supplier<ResourceLocation> supplier,
                                             Consumer<ResourceLocation> consumer, Runnable onChanged) {
        var id = registry.getKey(value);
        if (id != null && !Objects.equals(id, supplier.get())) {
            consumer.accept(id);
            onChanged.run();
        }
    }

    private static Component ritualTypeName(ResourceLocation id) {
        if (id == null) {
            return Component.translatable("viscript_recipe.editor.goety.ritual.behavior.unknown");
        }
        var key = "viscript_recipe.editor.goety.ritual.behavior." + id.getNamespace() + "." + id.getPath().replace('/', '.');
        return Component.translatableWithFallback(key, id.toString());
    }

}
