package com.viscript_recipe.network;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

/** Transfers server-owned dynamic registry identifiers needed by client-side recipe completion fields. */
public final class RecipeRegistrySnapshot {
    private static final String BIOME_TAGS = "biome_tags";
    private static final String DIMENSION_TYPES = "dimension_types";
    private static final String ID = "id";
    private static final String MEMBERS = "members";

    private RecipeRegistrySnapshot() {
    }

    /** Creates the authoritative biome-tag and dimension-type catalog. */
    public static CompoundTag create(RegistryAccess access) {
        var result = new CompoundTag();
        var biomeTags = new ListTag();
        access.registry(Registries.BIOME).ifPresent(registry -> registry.getTags()
                .sorted(Comparator.comparing(pair -> pair.getFirst().location().toString()))
                .forEach(pair -> {
                    var row = new CompoundTag();
                    row.putString(ID, pair.getFirst().location().toString());
                    var members = new ListTag();
                    pair.getSecond().stream().flatMap(holder -> holder.unwrapKey().stream())
                            .map(ResourceKey::location).sorted(Comparator.comparing(ResourceLocation::toString))
                            .forEach(member -> members.add(StringTag.valueOf(member.toString())));
                    row.put(MEMBERS, members);
                    biomeTags.add(row);
                }));
        result.put(BIOME_TAGS, biomeTags);

        var dimensions = new ListTag();
        access.registry(Registries.DIMENSION_TYPE).ifPresent(registry -> registry.keySet().stream()
                .sorted(Comparator.comparing(ResourceLocation::toString))
                .forEach(id -> dimensions.add(StringTag.valueOf(id.toString()))));
        result.put(DIMENSION_TYPES, dimensions);
        return result;
    }

    /** Reads immutable biome-tag candidates from a snapshot. */
    public static Map<ResourceLocation, List<ResourceLocation>> readBiomeTags(CompoundTag snapshot) {
        if (snapshot == null) {
            return Map.of();
        }
        var result = new LinkedHashMap<ResourceLocation, List<ResourceLocation>>();
        var rows = snapshot.getList(BIOME_TAGS, Tag.TAG_COMPOUND);
        for (int index = 0; index < rows.size(); index++) {
            var row = rows.getCompound(index);
            var id = ResourceLocation.tryParse(row.getString(ID));
            if (id == null) {
                continue;
            }
            var members = new ArrayList<ResourceLocation>();
            var memberTags = row.getList(MEMBERS, Tag.TAG_STRING);
            for (int memberIndex = 0; memberIndex < memberTags.size(); memberIndex++) {
                var member = ResourceLocation.tryParse(memberTags.getString(memberIndex));
                if (member != null) {
                    members.add(member);
                }
            }
            result.put(id, List.copyOf(members));
        }
        return Collections.unmodifiableMap(result);
    }

    /** Reads immutable dimension-type identifiers from a snapshot. */
    public static List<ResourceLocation> readDimensionTypes(CompoundTag snapshot) {
        if (snapshot == null) {
            return List.of();
        }
        var result = new ArrayList<ResourceLocation>();
        var values = snapshot.getList(DIMENSION_TYPES, Tag.TAG_STRING);
        for (int index = 0; index < values.size(); index++) {
            var id = ResourceLocation.tryParse(values.getString(index));
            if (id != null) {
                result.add(id);
            }
        }
        return List.copyOf(result);
    }
}
