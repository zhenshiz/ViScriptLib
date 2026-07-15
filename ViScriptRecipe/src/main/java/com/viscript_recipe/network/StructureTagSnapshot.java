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

/**
 * Encodes the server-owned structure tag catalog needed by client editor search fields.
 */
public final class StructureTagSnapshot {
    private static final String TAGS_KEY = "tags";
    private static final String TAG_ID_KEY = "id";
    private static final String MEMBERS_KEY = "members";

    private StructureTagSnapshot() {
    }

    /**
     * Creates a compact snapshot containing structure tag identifiers and their member identifiers.
     *
     * @param  registryAccess the authoritative server registry access
     * @return the encoded structure tag snapshot
     */
    public static CompoundTag create(RegistryAccess registryAccess) {
        var snapshot = new CompoundTag();
        var entries = new ListTag();
        registryAccess.registry(Registries.STRUCTURE).ifPresent(registry -> registry.getTags()
                .sorted(Comparator.comparing(pair -> pair.getFirst().location().toString()))
                .forEach(pair -> {
                    var entry = new CompoundTag();
                    entry.putString(TAG_ID_KEY, pair.getFirst().location().toString());

                    var members = new ListTag();
                    pair.getSecond().stream()
                            .flatMap(holder -> holder.unwrapKey().stream())
                            .map(ResourceKey::location)
                            .sorted(Comparator.comparing(ResourceLocation::toString))
                            .forEach(id -> members.add(StringTag.valueOf(id.toString())));
                    entry.put(MEMBERS_KEY, members);
                    entries.add(entry);
                }));
        snapshot.put(TAGS_KEY, entries);
        return snapshot;
    }

    /**
     * Decodes a structure tag snapshot into immutable client search data.
     *
     * @param  snapshot the compound tag containing the encoded server snapshot
     * @return structure tag identifiers mapped to their member structure identifiers
     */
    public static Map<ResourceLocation, List<ResourceLocation>> read(CompoundTag snapshot) {
        if (snapshot == null) {
            return Map.of();
        }
        var result = new LinkedHashMap<ResourceLocation, List<ResourceLocation>>();
        var entries = snapshot.getList(TAGS_KEY, Tag.TAG_COMPOUND);
        for (int i = 0; i < entries.size(); i++) {
            var entry = entries.getCompound(i);
            var tagId = ResourceLocation.tryParse(entry.getString(TAG_ID_KEY));
            if (tagId == null) {
                continue;
            }

            var members = entry.getList(MEMBERS_KEY, Tag.TAG_STRING);
            var memberIds = new java.util.ArrayList<ResourceLocation>(members.size());
            for (int memberIndex = 0; memberIndex < members.size(); memberIndex++) {
                var memberId = ResourceLocation.tryParse(members.getString(memberIndex));
                if (memberId != null) {
                    memberIds.add(memberId);
                }
            }
            result.put(tagId, List.copyOf(memberIds));
        }
        return Collections.unmodifiableMap(result);
    }
}
