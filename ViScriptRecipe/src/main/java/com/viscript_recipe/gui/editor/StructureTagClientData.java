package com.viscript_recipe.gui.editor;

import com.viscript_recipe.network.StructureTagSnapshot;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;

/**
 * Holds the immutable structure tag catalog supplied by the server for the current client editor session.
 */
public final class StructureTagClientData {
    private static volatile Map<ResourceLocation, List<ResourceLocation>> tags = Map.of();

    private StructureTagClientData() {
    }

    /**
     * Replaces the client catalog with the latest authoritative server snapshot.
     *
     * @param  snapshot the compound tag containing structure tag data from the server
     */
    public static void updateFromServer(CompoundTag snapshot) {
        tags = StructureTagSnapshot.read(snapshot);
    }

    static Map<ResourceLocation, List<ResourceLocation>> tags() {
        return tags;
    }
}
