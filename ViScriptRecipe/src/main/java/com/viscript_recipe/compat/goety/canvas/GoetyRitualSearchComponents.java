package com.viscript_recipe.compat.goety.canvas;

import com.Polarice3.Goety.common.research.ResearchList;
import com.Polarice3.Goety.common.ritual.ModRitualFactory;
import com.Polarice3.Goety.common.ritual.ModRituals;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.utils.UIElementProvider;
import com.viscript_lib.gui.components.search.RegistrySearchBox;
import com.viscript_recipe.gui.editor.RecipeEditorUi;
import com.viscript_recipe.gui.editor.RecipeSearchComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class GoetyRitualSearchComponents {
    private static final ResourceLocation DEFAULT_RITUAL_TYPE =
            new ResourceLocation("goety", "craft");
    static IForgeRegistry<ModRitualFactory> ritualRegistry() {return ModRituals.REGISTRY.get();}

    private GoetyRitualSearchComponents() {
    }

    public static UIElement ritualType(
            Supplier<ResourceLocation> supplier,
            Consumer<ResourceLocation> consumer,
            Runnable onChanged
    ) {
        var registry = ritualRegistry();
        var requestedId = supplier.get();
        // Goety can expose an empty ritual registry briefly while its client data
        // is loading.  Do not turn that normal lifecycle state into an editor crash.
        // RegistrySearchBox accepts a null value and will populate candidates once
        // the registry contains entries.
        var current = registry.getValue(Objects.requireNonNullElse(requestedId, DEFAULT_RITUAL_TYPE));
        var searchBox = new RitualTypeSearchBox(current);
        searchBox.setOnValueChanged(value -> {
            var id = registry.getKey(value);
            if (id != null && !Objects.equals(id, supplier.get())) {
                consumer.accept(id);
                onChanged.run();
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
        return RecipeEditorUi.fieldGroup("viscript_recipe.config.goety.ritual.ritual_type", searchBox);
    }

    public static UIElement enchantment(
            String nameKey,
            Supplier<ResourceLocation> supplier,
            Consumer<ResourceLocation> consumer,
            Runnable onChanged
    ) {
        return RecipeSearchComponents.enchantment(nameKey, supplier, consumer, onChanged);
    }

    public static UIElement research(Supplier<String> supplier, Consumer<String> consumer, Runnable onChanged) {
        var ids = new ArrayList<String>();
        ids.add("");
        ResearchList.getResearchList().keySet().stream().sorted().forEach(ids::add);
        var current = Objects.requireNonNullElse(supplier.get(), "");
        return RecipeEditorUi.fieldGroup("viscript_recipe.config.goety.ritual.research",
                RecipeEditorUi.selector(ids, current, GoetyRitualSearchComponents::researchName, value -> {
                    if (!Objects.equals(value, supplier.get())) {
                        consumer.accept(value);
                        onChanged.run();
                    }
                }));
    }

    private static Component researchName(String id) {
        if (id == null || id.isBlank()) {
            return Component.translatable("viscript_recipe.editor.goety.ritual.research.none");
        }
        return Component.translatableWithFallback("item.goety." + id + "_scroll", id)
                .append(Component.literal(" (" + id + ")"));
    }

    private static Component ritualTypeName(ResourceLocation id) {
        if (id == null) {
            return Component.translatable("viscript_recipe.editor.goety.ritual.behavior.unknown");
        }
        var key = "viscript_recipe.editor.goety.ritual.behavior."
                + id.getNamespace() + "." + id.getPath().replace('/', '.');
        return Component.translatableWithFallback(key, id.toString());
    }

    private static final class RitualTypeSearchBox extends RegistrySearchBox<ModRitualFactory> {
        private RitualTypeSearchBox(ModRitualFactory defaultValue) {
            super(
                    defaultValue,
                    ModRituals.REGISTRY::get,
                    id -> ritualRegistry().getKey(id),
                    value -> Objects.toString(ModRituals.REGISTRY.get().getKey(value), ""),
                    RitualTypeSearchBox::search,
                    UIElementProvider.text(value -> ritualTypeName(ritualRegistry().getKey(value)))
            );
        }

        private static void search(
                String word,
                com.lowdragmc.lowdraglib2.utils.search.IResultHandler<ModRitualFactory> result
        ) {
            var query = word.toLowerCase(Locale.ROOT);
            var registry = ritualRegistry();
            for (var value : registry) {
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }
                var id = registry.getKey(value);
                if (id != null && (id.toString().toLowerCase(Locale.ROOT).contains(query)
                        || ritualTypeName(id).getString().toLowerCase(Locale.ROOT).contains(query))) {
                    result.acceptResult(value);
                }
            }
        }
    }
}
