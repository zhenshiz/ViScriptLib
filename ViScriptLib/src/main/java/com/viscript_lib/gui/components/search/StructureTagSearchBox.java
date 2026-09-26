package com.viscript_lib.gui.components.search;

import com.lowdragmc.lowdraglib2.gui.ui.utils.UIElementProvider;
import com.lowdragmc.lowdraglib2.utils.search.IResultHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.StructureTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.structure.Structure;

import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * 结构标签自动补全框，值类型为 {@code TagKey<Structure>}。
 */
public class StructureTagSearchBox extends RegistrySearchBox<TagKey<Structure>> {
    private static final List<TagKey<Structure>> BUILTIN_STRUCTURE_TAGS = List.of(
            StructureTags.EYE_OF_ENDER_LOCATED,
            StructureTags.DOLPHIN_LOCATED,
            StructureTags.ON_WOODLAND_EXPLORER_MAPS,
            StructureTags.ON_OCEAN_EXPLORER_MAPS,
            StructureTags.ON_TREASURE_MAPS,
            StructureTags.CATS_SPAWN_IN,
            StructureTags.CATS_SPAWN_AS_BLACK,
            StructureTags.VILLAGE,
            StructureTags.MINESHAFT,
            StructureTags.SHIPWRECK,
            StructureTags.RUINED_PORTAL,
            StructureTags.OCEAN_RUIN
    );

    public StructureTagSearchBox() {
        this(StructureTags.VILLAGE);
    }

    public StructureTagSearchBox(TagKey<Structure> defaultValue) {
        super(
                defaultValue,
                StructureSearchBox::getStructureRegistry,
                TagKey::location,
                tag -> tag.location().toString(),
                StructureTagSearchBox::searchStructureTags,
                UIElementProvider.text(tag -> Component.literal(tag.location().toString()))
        );
    }

    @Nullable
    public ResourceLocation getSelectedStructureTagId() {
        return getSelectedId();
    }

    public String getSelectedStructureTagIdString() {
        return getSelectedIdString();
    }

    public String getSelectedStructureTagReferenceString() {
        return getStructureTagReferenceString(getValue());
    }

    @Nullable
    public static ResourceLocation getStructureTagId(@Nullable TagKey<Structure> tag) {
        return tag == null ? null : tag.location();
    }

    public static String getStructureTagIdString(@Nullable TagKey<Structure> tag) {
        var id = getStructureTagId(tag);
        return id == null ? "" : id.toString();
    }

    public static String getStructureTagReferenceString(@Nullable TagKey<Structure> tag) {
        var id = getStructureTagIdString(tag);
        return id.isEmpty() ? "" : "#" + id;
    }

    private static void searchStructureTags(String word, IResultHandler<TagKey<Structure>> searchHandler) {
        var registry = StructureSearchBox.getStructureRegistry();
        var tags = new ArrayList<TagKey<Structure>>();
        if (registry != null) {
            registry.getTagNames().forEach(tags::add);
        }
        if (tags.isEmpty()) {
            tags.addAll(BUILTIN_STRUCTURE_TAGS);
        }

        var lowerWord = word.toLowerCase(Locale.ROOT);
        tags.stream()
                .distinct()
                .sorted(Comparator.comparing(tag -> tag.location().toString()))
                .takeWhile(tag -> !Thread.currentThread().isInterrupted())
                .filter(tag -> matches(lowerWord, tag.location().toString())
                        || matches(lowerWord, tag.location().toString().replace('_', ' ')))
                .forEach(searchHandler::acceptResult);
    }
}
