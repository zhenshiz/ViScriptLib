package com.viscript_recipe.gui.editor;

import com.viscript_recipe.network.RecipeRegistrySnapshot;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;

/** Holds immutable dynamic-registry completion data supplied by the current server connection. */
public final class RecipeRegistryClientData {
    private static volatile Map<ResourceLocation, List<ResourceLocation>> biomeTags = Map.of();
    private static volatile List<ResourceLocation> dimensionTypes = List.of();

    private RecipeRegistryClientData() {
    }

    /** Replaces all client candidates with one authoritative server snapshot. */
    public static void updateFromServer(CompoundTag snapshot) {
        biomeTags = RecipeRegistrySnapshot.readBiomeTags(snapshot);
        dimensionTypes = RecipeRegistrySnapshot.readDimensionTypes(snapshot);
    }

    public static Map<ResourceLocation, List<ResourceLocation>> biomeTags() {
        return biomeTags;
    }

    public static List<ResourceLocation> dimensionTypes() {
        return dimensionTypes;
    }
}
