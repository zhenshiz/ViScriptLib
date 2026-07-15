package com.viscript_recipe.gui.editor;

import com.lowdragmc.lowdraglib2.configurator.ui.RegistrySearchComponent;
import com.lowdragmc.lowdraglib2.configurator.ui.SearchComponentConfigurator;
import com.lowdragmc.lowdraglib2.configurator.ui.TagKeySearchComponent;
import com.lowdragmc.lowdraglib2.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.utils.UIElementProvider;
import com.lowdragmc.lowdraglib2.utils.search.IResultHandler;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Creates shared searchable inputs for recipe registry values and tags.
 *
 * <p>Registry identifiers remain the persisted values and searchable aliases, while candidate rows use
 * player-facing names where the registry exposes them.
 */
final class RecipeSearchComponents {
    private static final ResourceLocation DEFAULT_STRUCTURE_TAG = new ResourceLocation("village");

    private RecipeSearchComponents() {
    }

    /**
     * Creates an entity type input searchable by identifier, translation key, and localized name.
     *
     * @param  nameKey the translation key used for the input label
     * @param  supplier the current entity identifier supplier
     * @param  consumer the updated entity identifier consumer
     * @param  onChanged the callback invoked after a changed selection
     * @param  defaultValue the entity type used when the current identifier is unavailable
     * @return the configured entity type input
     */
    static RegistrySearchComponent.EntityType entityType(
            String nameKey,
            Supplier<ResourceLocation> supplier,
            Consumer<ResourceLocation> consumer,
            Runnable onChanged,
            EntityType<?> defaultValue
    ) {
        var registry = BuiltInRegistries.ENTITY_TYPE;
        var configurator = new RegistrySearchComponent.EntityType(
                nameKey,
                () -> {
                    var id = supplier.get();
                    return id == null ? defaultValue : registry.getOptional(id).orElse(defaultValue);
                },
                value -> updateRegistryId(registry, value, supplier, consumer, onChanged),
                defaultValue,
                true
        );
        configurator.setTranslator(value -> value.getDescriptionId() + " " + value.getDescription().getString());
        configurator.searchComponent.setCandidateUIProvider(UIElementProvider.text(EntityType::getDescription));
        return configure(configurator);
    }

    /**
     * Creates a mob effect input searchable by registry identifier, translation key, and localized name.
     *
     * @param  nameKey the translation key used for the input label
     * @param  supplier the current mob effect identifier supplier
     * @param  consumer the updated mob effect identifier consumer
     * @param  onChanged the callback invoked after a changed selection
     * @param  defaultValue the mob effect used when the current identifier is unavailable
     * @return the configured mob effect registry input
     */
    static RegistrySearchComponent<MobEffect> mobEffect(
            String nameKey,
            Supplier<ResourceLocation> supplier,
            Consumer<ResourceLocation> consumer,
            Runnable onChanged,
            MobEffect defaultValue
    ) {
        var registry = BuiltInRegistries.MOB_EFFECT;
        var configurator = new RegistrySearchComponent<>(
                nameKey,
                () -> {
                    var id = supplier.get();
                    return id == null ? defaultValue : registry.getOptional(id).orElse(defaultValue);
                },
                value -> updateRegistryId(registry, value, supplier, consumer, onChanged),
                defaultValue,
                true,
                registry,
                UIElementProvider.text(MobEffect::getDisplayName)
        );
        configurator.setTranslator(value -> value.getDescriptionId() + " " + value.getDisplayName().getString());
        return configure(configurator);
    }

    /**
     * Creates a block input searchable by registry identifier, translation key, and localized name.
     *
     * @param  nameKey the translation key used for the input label
     * @param  supplier the current block identifier supplier
     * @param  consumer the updated block identifier consumer
     * @param  onChanged the callback invoked after a changed selection
     * @param  defaultValue the block used when the current identifier is unavailable
     * @return the configured block registry input
     */
    static RegistrySearchComponent.Block block(
            String nameKey,
            Supplier<ResourceLocation> supplier,
            Consumer<ResourceLocation> consumer,
            Runnable onChanged,
            Block defaultValue
    ) {
        var registry = BuiltInRegistries.BLOCK;
        var configurator = new RegistrySearchComponent.Block(
                nameKey,
                () -> {
                    var id = supplier.get();
                    return id == null ? defaultValue : registry.getOptional(id).orElse(defaultValue);
                },
                value -> updateRegistryId(registry, value, supplier, consumer, onChanged),
                defaultValue,
                true
        );
        configurator.setFilter(value -> value != Blocks.AIR
                && value != Blocks.CAVE_AIR
                && value != Blocks.VOID_AIR);
        configurator.setTranslator(value -> value.getDescriptionId() + " "
                + Component.translatable(value.getDescriptionId()).getString());
        configurator.searchComponent.setCandidateUIProvider(UIElementProvider.iconText(
                value -> new ItemStackTexture(value.asItem()),
                value -> Component.translatable(value.getDescriptionId())
                        .append(Component.literal(" (" + registry.getKey(value) + ")"))
        ));
        return configure(configurator);
    }

    /**
     * Creates an entity type tag input searchable by tag and member identifiers and entity names.
     *
     * @param  nameKey the translation key used for the input label
     * @param  supplier the current entity tag identifier supplier
     * @param  consumer the updated entity tag identifier consumer
     * @param  onChanged the callback invoked after a changed selection
     * @return the configured entity type tag input
     */
    static TagKeySearchComponent.EntityType entityTag(
            String nameKey,
            Supplier<ResourceLocation> supplier,
            Consumer<ResourceLocation> consumer,
            Runnable onChanged
    ) {
        var defaultTag = TagKey.create(Registries.ENTITY_TYPE, supplier.get());
        var configurator = new EntityTagSearchComponent(
                nameKey,
                () -> TagKey.create(Registries.ENTITY_TYPE, supplier.get()),
                tag -> updateTagId(tag, supplier, consumer, onChanged),
                defaultTag,
                true
        );
        configurator.searchComponent.setCandidateUIProvider(UIElementProvider.text(
                RecipeSearchComponents::entityTagName));
        return configure(configurator);
    }

    /**
     * Creates a structure tag input searchable by tag and member structure identifiers.
     *
     * @param  nameKey the translation key used for the input label
     * @param  supplier the current structure tag identifier supplier
     * @param  consumer the updated structure tag identifier consumer
     * @param  onChanged the callback invoked after a changed selection
     * @return the configured structure tag input, or an identifier input when no server snapshot is available
     */
    static UIElement structureTag(
            String nameKey,
            Supplier<ResourceLocation> supplier,
            Consumer<ResourceLocation> consumer,
            Runnable onChanged
    ) {
        var structureTags = StructureTagClientData.tags();
        if (structureTags.isEmpty()) {
            return RecipeEditorUi.fieldGroup(nameKey, RecipeEditorUi.resourceLocationField(supplier.get(), value -> {
                if (!Objects.equals(value, supplier.get())) {
                    consumer.accept(value);
                    onChanged.run();
                }
            }));
        }
        var defaultTag = TagKey.create(Registries.STRUCTURE,
                Objects.requireNonNullElse(supplier.get(), DEFAULT_STRUCTURE_TAG));
        var configurator = new SearchComponentConfigurator<>(
                nameKey,
                () -> TagKey.create(Registries.STRUCTURE,
                        Objects.requireNonNullElse(supplier.get(), DEFAULT_STRUCTURE_TAG)),
                tag -> updateTagId(tag, supplier, consumer, onChanged),
                defaultTag,
                true,
                (word, result) -> searchStructureTags(structureTags, word, result),
                tag -> structureTagResultText(structureTags, tag),
                UIElementProvider.text(tag -> structureTagName(structureTags, tag))
        );
        return configure(configurator);
    }

    private static <T extends SearchComponentConfigurator<?>> T configure(T configurator) {
        configurator.layout(layout -> layout.widthPercent(100));
        configurator.searchComponent.searchStyle(style -> {
            style.maxItemCount(8);
            style.scrollerViewHeight(120);
        });
        return configurator;
    }

    private static <T> void updateRegistryId(
            Registry<T> registry,
            T value,
            Supplier<ResourceLocation> supplier,
            Consumer<ResourceLocation> consumer,
            Runnable onChanged
    ) {
        var id = registry.getKey(value);
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
        if (tag != null && !Objects.equals(tag.location(), supplier.get())) {
            consumer.accept(tag.location());
            onChanged.run();
        }
    }

    private static Component entityTagName(TagKey<EntityType<?>> tag) {
        var names = BuiltInRegistries.ENTITY_TYPE.getTag(tag)
                .stream()
                .flatMap(HolderSet.ListBacked::stream)
                .map(holder -> holder.value().getDescription().getString())
                .distinct()
                .limit(3)
                .collect(Collectors.joining(", "));
        return names.isEmpty()
                ? Component.literal(tag.location().toString())
                : Component.literal(tag.location() + " — " + names);
    }

    private static Component structureTagName(Map<ResourceLocation, List<ResourceLocation>> structureTags,
                                              TagKey<Structure> tag) {
        return Component.literal(structureTagResultText(structureTags, tag));
    }

    private static String structureTagResultText(Map<ResourceLocation, List<ResourceLocation>> structureTags,
                                                 TagKey<Structure> tag) {
        var members = structureTags.getOrDefault(tag.location(), List.of());
        return members.size() == 1 ? members.get(0).toString() : tag.location().toString();
    }

    private static void searchStructureTags(Map<ResourceLocation, List<ResourceLocation>> structureTags,
                                            String word, Consumer<TagKey<Structure>> result) {
        var query = word.toLowerCase(Locale.ROOT);
        for (var entry : structureTags.entrySet()) {
            if (Thread.currentThread().isInterrupted()) {
                return;
            }
            var matches = entry.getKey().toString().toLowerCase(Locale.ROOT).contains(query)
                    || entry.getValue().stream()
                    .anyMatch(id -> id.toString().toLowerCase(Locale.ROOT).contains(query));
            if (matches) {
                result.accept(TagKey.create(Registries.STRUCTURE, entry.getKey()));
            }
        }
    }

    private static final class EntityTagSearchComponent extends TagKeySearchComponent.EntityType {
        private EntityTagSearchComponent(
                String name,
                Supplier<TagKey<net.minecraft.world.entity.EntityType<?>>> supplier,
                Consumer<TagKey<net.minecraft.world.entity.EntityType<?>>> onUpdate,
                TagKey<net.minecraft.world.entity.EntityType<?>> defaultValue,
                boolean forceUpdate
        ) {
            super(name, supplier, onUpdate, defaultValue, forceUpdate);
        }

        @Override
        public void search(
                String word,
                IResultHandler<TagKey<net.minecraft.world.entity.EntityType<?>>> searchHandler
        ) {
            var query = word.toLowerCase(Locale.ROOT);
            for (var pair : BuiltInRegistries.ENTITY_TYPE.getTags().toList()) {
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }
                var tag = pair.getFirst();
                var matches = tag.location().toString().toLowerCase(Locale.ROOT).contains(query)
                        || pair.getSecond().stream().anyMatch(holder -> {
                    var entity = holder.value();
                    var id = BuiltInRegistries.ENTITY_TYPE.getKey(entity);
                    return (id != null && id.toString().toLowerCase(Locale.ROOT).contains(query))
                            || entity.getDescriptionId().toLowerCase(Locale.ROOT).contains(query)
                            || entity.getDescription().getString().toLowerCase(Locale.ROOT).contains(query);
                });
                if (matches) {
                    searchHandler.acceptResult(tag);
                }
            }
        }
    }

}
