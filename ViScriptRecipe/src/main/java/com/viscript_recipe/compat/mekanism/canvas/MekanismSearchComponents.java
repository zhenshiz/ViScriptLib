package com.viscript_recipe.compat.mekanism.canvas;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.utils.UIElementProvider;
import com.lowdragmc.lowdraglib2.utils.search.IResultHandler;
import com.viscript_lib.gui.components.search.RegistrySearchBox;
import com.viscript_recipe.gui.editor.RecipeEditorUi;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalType;
import mekanism.common.util.ChemicalUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings({"unchecked", "rawtypes"})
public class MekanismSearchComponents {
    private MekanismSearchComponents() {
    }

    static <T extends Chemical> IForgeRegistry<T> getRegistry(ChemicalType chemicalType) {
        return (IForgeRegistry<T>) switch (chemicalType) {
            case GAS -> MekanismAPI.gasRegistry();
            case INFUSION -> MekanismAPI.infuseTypeRegistry();
            case PIGMENT -> MekanismAPI.pigmentRegistry();
            case SLURRY -> MekanismAPI.slurryRegistry();
        };
    }

    static UIElement chemical(
            String nameKey,
            ChemicalType type,
            Supplier<ResourceLocation> supplier,
            Consumer<ResourceLocation> consumer,
            Runnable onChanged
    ) {
        var registry = getRegistry(type);
        var fallback = fallbackChemical(type);
        var current = Objects.requireNonNullElse(registry.getValue(supplier.get()), fallback);
        var searchBox = new ChemicalSearchBox<>(registry, current);
        searchBox.setCandidateFilter(MekanismSearchComponents::isUsableChemical);
        return configure(nameKey, searchBox, value -> {
            var id = registry.getKey(value);
            if (!Objects.equals(id, supplier.get())) { consumer.accept(id); onChanged.run(); }
        });
    }

    static UIElement chemicalTag(
            String nameKey,
            ChemicalType type,
            Supplier<ResourceLocation> supplier,
            Consumer<ResourceLocation> consumer,
            Runnable onChanged
    ) {
        var current = switch (type) {
            case GAS -> TagKey.create(MekanismAPI.GAS_REGISTRY_NAME,
                    Objects.requireNonNullElse(supplier.get(), new ResourceLocation("mekanism", "water_vapor")));
            case INFUSION -> TagKey.create(MekanismAPI.INFUSE_TYPE_REGISTRY_NAME,
                    Objects.requireNonNullElse(supplier.get(), new ResourceLocation("mekanism", "carbon")));
            case PIGMENT -> TagKey.create(MekanismAPI.PIGMENT_REGISTRY_NAME, // mek没有注册这玩意的标签
                    Objects.requireNonNullElse(supplier.get(), new ResourceLocation("mekanism", "clean")));
            case SLURRY -> TagKey.create(MekanismAPI.SLURRY_REGISTRY_NAME,
                    Objects.requireNonNullElse(supplier.get(), new ResourceLocation("mekanism", "clean")));
        };
        return configure(nameKey, new ChemicalTagSearchBox(getRegistry(type), current), tag -> {
            if (!Objects.equals(tag.location(), supplier.get())) {
                consumer.accept(tag.location());
                onChanged.run();
            }
        });
    }

    private static <T> UIElement configure(
            String nameKey,
            RegistrySearchBox<T> searchBox,
            Consumer<T> onSelected
    ) {
        searchBox.setOnValueChanged(value -> {
            if (value != null) {
                onSelected.accept(value);
            }
        });
        searchBox.searchStyle(style -> {
            style.maxItemCount(8);
            style.scrollerViewHeight(120);
            style.closeAfterSelect(true);
        });
        searchBox.layout(layout -> {
            layout.widthPercent(100);
            layout.height(18);
        });
        return RecipeEditorUi.fieldGroup(nameKey, searchBox);
    }

    private static Chemical fallbackChemical(ChemicalType type) {
        var registry = getRegistry(type);
        for (var chemical : registry) {
            if (isUsableChemical(chemical)) return chemical;
        }
        return registry.getValue(registry.getDefaultKey());
    }

    private static boolean isUsableChemical(Chemical chemical) {
        return chemical != null && ChemicalUtil.getEmptyStack(ChemicalType.getTypeFor(chemical)).getRaw() != chemical;
    }

    private static class ChemicalSearchBox<T extends Chemical> extends RegistrySearchBox<T> {
        private ChemicalSearchBox(IForgeRegistry<T> registry, T defaultValue) {
            super(defaultValue, () -> registry, registry::getKey,
                    value -> Objects.toString(registry.getKey(value), ""),
                    (word, searchHandler) -> searchForgeRegistry(registry, word, searchHandler,
                            value -> value.getTranslationKey() + " " + value.getTextComponent().getString()),
                    UIElementProvider.text(T::getTextComponent));
        }

        protected static <V extends Chemical> void searchForgeRegistry(IForgeRegistry<V> registry, String word, IResultHandler<V> searchHandler, Function<V, String> extraSearchText) {
            var lowerWord = word.toLowerCase(Locale.ROOT);
            for (var key : registry.getKeys()) {
                if (Thread.currentThread().isInterrupted()) return;
                V value = registry.getValue(key);
                if (matches(lowerWord, key.toString()) || matches(lowerWord, extraSearchText.apply(value))) {
                    searchHandler.acceptResult(value);
                }
            }
        }
    }

    private static final class ChemicalTagSearchBox extends RegistrySearchBox<TagKey<?>> {
        static IForgeRegistry<?> registry;

        private ChemicalTagSearchBox(IForgeRegistry<?> registry, TagKey<?> defaultValue) {
            super(
                    defaultValue,
                    () -> registry,
                    TagKey::location,
                    tag -> tag.location().toString(),
                    ChemicalTagSearchBox::search,
                    UIElementProvider.text(tag -> Component.literal("#" + tag.location()))
            );
            ChemicalTagSearchBox.registry = registry;
        }

        private static void search(String word, IResultHandler<TagKey<?>> result) {
            var query = word.toLowerCase(Locale.ROOT);
            Objects.requireNonNull(registry.tags()).getTagNames()
                    .sorted(Comparator.comparing(tag -> tag.location().toString()))
                    .takeWhile(tag -> !Thread.currentThread().isInterrupted())
                    .filter(tag -> tag.location().toString().toLowerCase(Locale.ROOT).contains(query))
                    .forEach(result::acceptResult);
        }
    }
}
