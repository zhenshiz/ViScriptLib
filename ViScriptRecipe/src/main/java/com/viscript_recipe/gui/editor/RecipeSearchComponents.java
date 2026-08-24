package com.viscript_recipe.gui.editor;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.utils.UIElementProvider;
import com.lowdragmc.lowdraglib2.utils.search.IResultHandler;
import com.viscript_lib.gui.components.search.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class RecipeSearchComponents {
    private static final ResourceLocation DEFAULT_ENTITY_TAG = new ResourceLocation("undead");
    private static final ResourceLocation DEFAULT_FLUID_TAG = new ResourceLocation("c", "water");
    private static final ResourceLocation DEFAULT_STRUCTURE_TAG = new ResourceLocation("village");
    private static final ResourceLocation DEFAULT_BIOME_TAG = new ResourceLocation("is_overworld");
    private static final ResourceLocation DEFAULT_DIMENSION_TYPE = new ResourceLocation("overworld");
    private static final int MAX_RECIPE_ID_CANDIDATES = 200;

    private RecipeSearchComponents() {
    }

    public static UIElement entityType(
            String nameKey,
            Supplier<ResourceLocation> supplier,
            Consumer<ResourceLocation> consumer,
            Runnable onChanged,
            EntityType<?> defaultValue
    ) {
        var registry = BuiltInRegistries.ENTITY_TYPE;
        var current = registry.getOptional(Objects.requireNonNullElse(supplier.get(), registry.getKey(defaultValue)))
                .orElse(defaultValue);
        var searchBox = new EntityTypeSearchBox(current);
        return configure(nameKey, searchBox,
                value -> updateRegistryId(registry, value, supplier, consumer, onChanged));
    }

    public static UIElement mobEffect(
            String nameKey,
            Supplier<ResourceLocation> supplier,
            Consumer<ResourceLocation> consumer,
            Runnable onChanged,
            MobEffect defaultValue
    ) {
        var registry = BuiltInRegistries.MOB_EFFECT;
        var current = registry.getOptional(supplier.get()).orElse(defaultValue);
        var searchBox = new MobEffectSearchBox(current);
        return configure(nameKey, searchBox, value -> updateId(
                MobEffectSearchBox.getMobEffectId(value), supplier, consumer, onChanged));
    }

    public static UIElement enchantment(
            String nameKey,
            Supplier<ResourceLocation> supplier,
            Consumer<ResourceLocation> consumer,
            Runnable onChanged
    ) {
        var id = Objects.requireNonNullElse(supplier.get(), new ResourceLocation("sharpness"));
        var searchBox = new EnchantmentSearchBox(ResourceKey.create(Registries.ENCHANTMENT, id));
        return configure(nameKey, searchBox, value -> updateId(
                EnchantmentSearchBox.getEnchantmentId(value), supplier, consumer, onChanged));
    }

    public static UIElement block(
            String nameKey,
            Supplier<ResourceLocation> supplier,
            Consumer<ResourceLocation> consumer,
            Runnable onChanged,
            Block defaultValue
    ) {
        var registry = BuiltInRegistries.BLOCK;
        var current = registry.getOptional(Objects.requireNonNullElse(supplier.get(), registry.getKey(defaultValue)))
                .orElse(defaultValue);
        var searchBox = new BlockSearchBox(current);
        searchBox.setCandidateFilter(value -> value != Blocks.AIR
                && value != Blocks.CAVE_AIR
                && value != Blocks.VOID_AIR);
        return configure(nameKey, searchBox,
                value -> updateRegistryId(registry, value, supplier, consumer, onChanged));
    }

    public static UIElement fluid(
            String nameKey,
            Supplier<ResourceLocation> supplier,
            Consumer<ResourceLocation> consumer,
            Runnable onChanged,
            Fluid defaultValue
    ) {
        var registry = BuiltInRegistries.FLUID;
        var fallback = defaultValue == Fluids.EMPTY ? Fluids.WATER : defaultValue;
        var current = registry.getOptional(Objects.requireNonNullElse(supplier.get(), registry.getKey(fallback)))
                .filter(value -> value != Fluids.EMPTY)
                .orElse(fallback);
        var searchBox = new FluidSearchBox(current);
        searchBox.setCandidateFilter(value -> value != Fluids.EMPTY);
        return configure(nameKey, searchBox,
                value -> updateRegistryId(registry, value, supplier, consumer, onChanged));
    }

    public static UIElement entityTag(
            String nameKey,
            Supplier<ResourceLocation> supplier,
            Consumer<ResourceLocation> consumer,
            Runnable onChanged
    ) {
        var current = TagKey.create(Registries.ENTITY_TYPE,
                Objects.requireNonNullElse(supplier.get(), DEFAULT_ENTITY_TAG));
        return configure(nameKey, new EntityTypeTagSearchBox(current),
                value -> updateTagId(value, supplier, consumer, onChanged));
    }

    static UIElement itemTag(
            String nameKey,
            Supplier<ResourceLocation> supplier,
            Consumer<ResourceLocation> consumer,
            Runnable onChanged
    ) {
        var current = TagKey.create(Registries.ITEM,
                Objects.requireNonNullElse(supplier.get(), ItemTags.PLANKS.location()));
        return configure(nameKey, new ItemTagSearchBox(current),
                value -> updateTagId(value, supplier, consumer, onChanged));
    }

    public static UIElement fluidTag(
            String nameKey,
            Supplier<ResourceLocation> supplier,
            Consumer<ResourceLocation> consumer,
            Runnable onChanged
    ) {
        var current = TagKey.create(Registries.FLUID,
                Objects.requireNonNullElse(supplier.get(), DEFAULT_FLUID_TAG));
        return configure(nameKey, new FluidTagSearchBox(current),
                value -> updateTagId(value, supplier, consumer, onChanged));
    }

    public static UIElement blockTag(
            String nameKey,
            Supplier<ResourceLocation> supplier,
            Consumer<ResourceLocation> consumer,
            Runnable onChanged
    ) {
        var current = TagKey.create(Registries.BLOCK,
                Objects.requireNonNullElse(supplier.get(), new ResourceLocation("minecraft", "campfires")));
        return configure(nameKey, new BlockTagSearchBox(current),
                value -> updateTagId(value, supplier, consumer, onChanged));
    }

    public static UIElement structureTag(
            String nameKey,
            Supplier<ResourceLocation> supplier,
            Consumer<ResourceLocation> consumer,
            Runnable onChanged
    ) {
        var current = TagKey.create(Registries.STRUCTURE,
                Objects.requireNonNullElse(supplier.get(), DEFAULT_STRUCTURE_TAG));
        var structureTags = StructureTagClientData.tags();
        // Structure is a dynamic registry.  It is not guaranteed to exist in the
        // client built-in registry while the editor is being constructed (for
        // example, during the first tick after connecting to a server).  Always use
        // the server snapshot-backed search box here; an empty snapshot simply means
        // that there are no candidates yet and, importantly, does not throw the
        // registry's "Missing registry" exception.
        RegistrySearchBox<TagKey<Structure>> searchBox =
                new RecipeStructureTagSearchBox(current, structureTags);
        return configure(nameKey, searchBox,
                value -> updateTagId(value, supplier, consumer, onChanged));
    }

    public static UIElement biomeTag(
            String nameKey,
            Supplier<ResourceLocation> supplier,
            Consumer<ResourceLocation> consumer,
            Runnable onChanged
    ) {
        var candidates = RecipeRegistryClientData.biomeTags();
        if (candidates.isEmpty()) {
            var current = TagKey.create(Registries.BIOME,
                    Objects.requireNonNullElse(supplier.get(), BiomeTags.IS_OVERWORLD.location()));
            return configure(nameKey, new BiomeTagSearchBox(current),
                    value -> updateTagId(value, supplier, consumer, onChanged));
        }
        return catalog(nameKey, supplier, consumer, onChanged, candidates.keySet().stream().toList(),
                id -> {
                    var members = candidates.getOrDefault(id, List.of());
                    var sample = members.stream().limit(3).map(ResourceLocation::toString)
                            .collect(Collectors.joining(", "));
                    return sample.isEmpty() ? Component.literal(id.toString())
                            : Component.literal(id + " — " + sample);
                }, DEFAULT_BIOME_TAG);
    }

    public static UIElement dimensionType(
            String nameKey,
            Supplier<ResourceLocation> supplier,
            Consumer<ResourceLocation> consumer,
            Runnable onChanged
    ) {
        return catalog(nameKey, supplier, consumer, onChanged, RecipeRegistryClientData.dimensionTypes(),
                id -> Component.literal(id.toString()), DEFAULT_DIMENSION_TYPE);
    }

    static UIElement recipeId(
            String nameKey,
            Supplier<ResourceLocation> supplier,
            Consumer<ResourceLocation> consumer,
            Runnable onChanged
    ) {
        var searchBox = new RecipeIdSearchBox(supplier.get(),
                value -> updateId(value, supplier, consumer, onChanged));
        searchBox.textField.setResourceLocationOnly();
        searchBox.preview.setOverflowVisible(false);
        searchBox.textField.setOverflowVisible(false);
        return configure(nameKey, searchBox,
                value -> updateId(value, supplier, consumer, onChanged));
    }

/*    public static UIElement soupBase(
            String nameKey,
            Supplier<ResourceLocation> supplier,
            Consumer<ResourceLocation> consumer,
            Runnable onChanged
    ) {
        return catalog(nameKey, supplier, consumer, onChanged,
                KaleidoscopeSoupBaseUiSupport.ids(),
                KaleidoscopeSoupBaseUiSupport::displayName,
                KaleidoscopeSoupBaseUiSupport.DEFAULT_SOUP_BASE);
    }*/

    private static UIElement catalog(
            String nameKey,
            Supplier<ResourceLocation> supplier,
            Consumer<ResourceLocation> consumer,
            Runnable onChanged,
            List<ResourceLocation> candidates,
            Function<ResourceLocation, Component> display,
            ResourceLocation defaultValue
    ) {
        var current = Objects.requireNonNullElse(supplier.get(),
                candidates.isEmpty() ? defaultValue : candidates.get(0));
        var searchBox = new ResourceLocationCatalogSearchBox(current, candidates, display);
        return configure(nameKey, searchBox,
                value -> updateId(value, supplier, consumer, onChanged));
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
        return nameKey == null || nameKey.isBlank()
                ? searchBox
                : RecipeEditorUi.fieldGroup(nameKey, searchBox);
    }

    private static <T> void updateRegistryId(
            Registry<T> registry,
            T value,
            Supplier<ResourceLocation> supplier,
            Consumer<ResourceLocation> consumer,
            Runnable onChanged
    ) {
        updateId(registry.getKey(value), supplier, consumer, onChanged);
    }

    private static void updateId(
            ResourceLocation id,
            Supplier<ResourceLocation> supplier,
            Consumer<ResourceLocation> consumer,
            Runnable onChanged
    ) {
        if (id != null && !Objects.equals(id, supplier.get())) {
            consumer.accept(id);
            onChanged.run();
        }
    }

    private static <T> void updateTagId(
            TagKey<T> tag,
            Supplier<ResourceLocation> supplier,
            Consumer<ResourceLocation> consumer,
            Runnable onChanged
    ) {
        if (tag != null) {
            updateId(tag.location(), supplier, consumer, onChanged);
        }
    }

    private static String structureTagResultText(
            Map<ResourceLocation, List<ResourceLocation>> structureTags,
            TagKey<Structure> tag
    ) {
        var members = structureTags.getOrDefault(tag.location(), List.of());
        return members.size() == 1 ? members.get(0).toString() : tag.location().toString();
    }

    private static void searchStructureTags(
            Map<ResourceLocation, List<ResourceLocation>> structureTags,
            String word,
            IResultHandler<TagKey<Structure>> result
    ) {
        var query = word.toLowerCase(Locale.ROOT);
        for (var entry : structureTags.entrySet()) {
            if (Thread.currentThread().isInterrupted()) {
                return;
            }
            var matches = entry.getKey().toString().toLowerCase(Locale.ROOT).contains(query)
                    || entry.getValue().stream()
                    .anyMatch(id -> id.toString().toLowerCase(Locale.ROOT).contains(query));
            if (matches) {
                result.acceptResult(TagKey.create(Registries.STRUCTURE, entry.getKey()));
            }
        }
    }

    private static boolean matchesResourceLocation(ResourceLocation id, String word) {
        if (word == null || word.isBlank()) {
            return true;
        }
        var query = word.toLowerCase(Locale.ROOT);
        var searchText = String.join(" ",
                id.toString(),
                id.getNamespace(),
                id.getPath()
        ).toLowerCase(Locale.ROOT);
        for (var token : query.split("\\s+")) {
            if (!searchText.contains(token)) {
                return false;
            }
        }
        return true;
    }

    private static final class RecipeStructureTagSearchBox extends RegistrySearchBox<TagKey<Structure>> {
        private RecipeStructureTagSearchBox(
                TagKey<Structure> defaultValue,
                Map<ResourceLocation, List<ResourceLocation>> structureTags
        ) {
            super(
                    defaultValue,
                    () -> null,
                    TagKey::location,
                    tag -> structureTagResultText(structureTags, tag),
                    (word, result) -> searchStructureTags(structureTags, word, result),
                    UIElementProvider.text(tag -> Component.literal(structureTagResultText(structureTags, tag)))
            );
        }
    }

    private static final class RecipeIdSearchBox extends RegistrySearchBox<ResourceLocation> {
        private final Consumer<ResourceLocation> onTyped;

        private RecipeIdSearchBox(ResourceLocation defaultValue, Consumer<ResourceLocation> onTyped) {
            super(
                    defaultValue,
                    () -> null,
                    Function.identity(),
                    ResourceLocation::toString,
                    RecipeIdSearchBox::searchRecipeIds,
                    UIElementProvider.text(id -> Component.literal(id == null ? "" : id.toString()))
            );
            this.onTyped = onTyped;
        }

        @Override
        protected void onSearchWordChanged(String word) {
            // Keep the raw text while the player is typing. ResourceLocation.tryParse
            // treats an unqualified path as a minecraft ID (and an empty value as
            // minecraft:), so feeding that parsed value back through setSelected would
            // overwrite the field before the player can enter another namespace.
            if (word != null && !word.isBlank()) {
                var typedId = ResourceLocation.tryParse(word);
                if (typedId != null && !typedId.getPath().isEmpty() && onTyped != null) {
                    onTyped.accept(typedId);
                }
            }
            super.onSearchWordChanged(word);
        }

        private static void searchRecipeIds(String word, IResultHandler<ResourceLocation> result) {
            var minecraft = Minecraft.getInstance();
            if (minecraft.level == null) {
                return;
            }
            var accepted = 0;
            var recipeIds = minecraft.level.getRecipeManager().getRecipeIds()
                    .sorted(Comparator.comparing(ResourceLocation::toString))
                    .toList();
            for (var recipeId : recipeIds) {
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }
                if (matchesResourceLocation(recipeId, word)) {
                    result.acceptResult(recipeId);
                    if (++accepted >= MAX_RECIPE_ID_CANDIDATES) {
                        return;
                    }
                }
            }
        }
    }

    private static final class ResourceLocationCatalogSearchBox extends RegistrySearchBox<ResourceLocation> {
        private ResourceLocationCatalogSearchBox(
                ResourceLocation defaultValue,
                List<ResourceLocation> candidates,
                Function<ResourceLocation, Component> display
        ) {
            super(
                    defaultValue,
                    () -> null,
                    Function.identity(),
                    ResourceLocation::toString,
                    (word, result) -> search(candidates, display, word, result),
                    UIElementProvider.text(display)
            );
        }

        private static void search(
                List<ResourceLocation> candidates,
                Function<ResourceLocation, Component> display,
                String word,
                IResultHandler<ResourceLocation> result
        ) {
            var query = word.toLowerCase(Locale.ROOT);
            for (var id : candidates) {
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }
                if (id.toString().toLowerCase(Locale.ROOT).contains(query)
                        || display.apply(id).getString().toLowerCase(Locale.ROOT).contains(query)) {
                    result.acceptResult(id);
                }
            }
        }
    }
}
