package com.viscript_recipe.client;

import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacketDistributor;
import com.viscript_recipe.ViScriptRecipe;
import com.viscript_recipe.compat.jei.JeiShowcaseModeState;
import com.viscript_recipe.network.RecipeDeltaSnapshot;
import com.viscript_recipe.network.c2s.RecipeDeltaC2SPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RecipesUpdatedEvent;

import java.util.*;

/** Owns the client recipe revision and JEI reconciliation metadata for incremental reloads. */
public final class RecipeDeltaClientState {
    private static long revision = -1;
    private static boolean fullSyncRequested;
    private static boolean localJeiReload;
    private static final LinkedHashMap<ResourceLocation, ResourceLocation> MANAGED_EDITOR_TYPES = new LinkedHashMap<>();
    private static final LinkedHashMap<ResourceLocation, ResourceLocation> SERVER_RECIPE_TYPE_HINTS = new LinkedHashMap<>();
    private static final LinkedHashMap<ResourceLocation, LinkedHashSet<ResourceLocation>> RECIPE_TYPE_HINTS = new LinkedHashMap<>();
    private static final LinkedHashSet<ResourceLocation> DELTA_TOUCHED_IDS = new LinkedHashSet<>();

    private RecipeDeltaClientState() {
    }

    public static void apply(CompoundTag payload, boolean expectedBaseline) {
        var copy = payload == null ? new CompoundTag() : payload.copy();
        Minecraft.getInstance().execute(() -> applyOnClient(copy, expectedBaseline));
    }

    public static JeiSyncState jeiSyncState() {
        var hints = new LinkedHashMap<ResourceLocation, Set<ResourceLocation>>();
        RECIPE_TYPE_HINTS.forEach((id, values) -> hints.put(id, Set.copyOf(values)));
        return new JeiSyncState(
                revision,
                Set.copyOf(DELTA_TOUCHED_IDS),
                Map.copyOf(MANAGED_EDITOR_TYPES),
                Map.copyOf(hints)
        );
    }

    public static List<ResourceLocation> recipeIdsForEditorType(ResourceLocation editorType) {
        return MANAGED_EDITOR_TYPES.entrySet()
                .stream()
                .filter(entry -> entry.getValue().equals(editorType))
                .map(Map.Entry::getKey)
                .toList();
    }

    public static void runAsLocalJeiReload(Runnable reloadAction) {
        localJeiReload = true;
        try {
            reloadAction.run();
        } finally {
            localJeiReload = false;
        }
    }

    public static void onRecipesUpdated(RecipesUpdatedEvent event) {
        fullSyncRequested = false;
        DELTA_TOUCHED_IDS.clear();
        if (localJeiReload) {
            rebuildHistoricalHints();
            return;
        }
        revision = -1;
        MANAGED_EDITOR_TYPES.clear();
        SERVER_RECIPE_TYPE_HINTS.clear();
        RECIPE_TYPE_HINTS.clear();
    }

    public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        revision = -1;
        fullSyncRequested = false;
        localJeiReload = false;
        MANAGED_EDITOR_TYPES.clear();
        SERVER_RECIPE_TYPE_HINTS.clear();
        RECIPE_TYPE_HINTS.clear();
        DELTA_TOUCHED_IDS.clear();
    }

    private static void applyOnClient(CompoundTag payload, boolean expectedBaseline) {
        var minecraft = Minecraft.getInstance();
        var level = minecraft.level;
        if (level == null) {
            return;
        }

        final RecipeDeltaSnapshot snapshot;
        try {
            snapshot = RecipeDeltaSnapshot.deserialize(payload);
        } catch (RuntimeException | LinkageError e) {
            ViScriptRecipe.LOGGER.warn("Failed to decode incremental recipe reload payload", e);
            if (!expectedBaseline) {
                requestFullSync();
            }
            return;
        }

        if (snapshot.baseline() || expectedBaseline) {
            applyBaseline(snapshot);
            return;
        }
        if (revision >= 0 && revision != snapshot.baseRevision()) {
            ViScriptRecipe.LOGGER.warn(
                    "Recipe delta revision mismatch: client={}, serverBase={}; requesting a full recipe sync",
                    revision,
                    snapshot.baseRevision()
            );
            requestFullSync();
            return;
        }

        var recipeManager = level.getRecipeManager();
        var recipes = snapshotRecipes(recipeManager);
        var affectedIds = new LinkedHashSet<>(snapshot.removedRecipeIds());
        snapshot.upsertedRecipes().forEach(holder -> affectedIds.add(holder.getId()));
        var oldRecipes = selectedRecipes(recipes, affectedIds);
        var oldEditorTypes = new LinkedHashMap<>(MANAGED_EDITOR_TYPES);

        if (!affectedIds.isEmpty()) {
            snapshot.removedRecipeIds().forEach(recipes::remove);
            snapshot.upsertedRecipes().forEach(holder -> recipes.put(holder.getId(), holder));
            recipeManager.replaceRecipes(recipes.values());
        }
        var newRecipes = selectedRecipes(recipes, affectedIds);

        revision = snapshot.revision();
        fullSyncRequested = false;
        MANAGED_EDITOR_TYPES.clear();
        MANAGED_EDITOR_TYPES.putAll(snapshot.managedEditorTypes());
        SERVER_RECIPE_TYPE_HINTS.clear();
        SERVER_RECIPE_TYPE_HINTS.putAll(snapshot.recipeTypeHints());
        addRecipeTypeHints(oldRecipes.values());
        addRecipeTypeHints(newRecipes.values());
        SERVER_RECIPE_TYPE_HINTS.forEach(RecipeDeltaClientState::addRecipeTypeHint);
        DELTA_TOUCHED_IDS.addAll(affectedIds);

        JeiShowcaseModeState.updateFromServer(snapshot.showcaseOnly());
/*        RecipeDeltaJeiBridge.applyDelta(
                snapshot.revision(),
                affectedIds,
                oldRecipes,
                newRecipes,
                oldEditorTypes,
                Map.copyOf(MANAGED_EDITOR_TYPES),
                snapshot.arcaneAnvilChanged()
        );*/
        ViScriptRecipe.LOGGER.info(
                "Applied recipe delta revision {}: {} removed, {} upserted",
                snapshot.revision(),
                snapshot.removedRecipeIds().size(),
                snapshot.upsertedRecipes().size()
        );
    }

    private static void applyBaseline(RecipeDeltaSnapshot snapshot) {
        revision = snapshot.revision();
        fullSyncRequested = false;
        MANAGED_EDITOR_TYPES.clear();
        MANAGED_EDITOR_TYPES.putAll(snapshot.managedEditorTypes());
        SERVER_RECIPE_TYPE_HINTS.clear();
        SERVER_RECIPE_TYPE_HINTS.putAll(snapshot.recipeTypeHints());
        DELTA_TOUCHED_IDS.clear();
        rebuildHistoricalHints();
        JeiShowcaseModeState.updateFromServer(snapshot.showcaseOnly());
//        RecipeDeltaJeiBridge.applyBaseline();
    }

    private static void requestFullSync() {
        if (fullSyncRequested) {
            return;
        }
        fullSyncRequested = true;
        RPCPacketDistributor.rpcToServer(RecipeDeltaC2SPayload.REQUEST_FULL_RECIPE_SYNC);
    }

    private static LinkedHashMap<ResourceLocation, Recipe<?>> snapshotRecipes(RecipeManager recipeManager) {
        var recipes = new LinkedHashMap<ResourceLocation, Recipe<?>>();
        for (var holder : recipeManager.getRecipes()) {
            recipes.put(holder.getId(), holder);
        }
        return recipes;
    }

    private static LinkedHashMap<ResourceLocation, Recipe<?>> selectedRecipes(
            Map<ResourceLocation, Recipe<?>> recipes,
            Collection<ResourceLocation> ids
    ) {
        var selected = new LinkedHashMap<ResourceLocation, Recipe<?>>();
        for (var id : ids) {
            var holder = recipes.get(id);
            if (holder != null) {
                selected.put(id, holder);
            }
        }
        return selected;
    }

    private static void addRecipeTypeHints(Collection<Recipe<?>> recipes) {
        for (var holder : recipes) {
            var typeId = BuiltInRegistries.RECIPE_TYPE.getKey(holder.getType());
            if (typeId != null) {
                addRecipeTypeHint(holder.getId(), typeId);
            }
        }
    }

    private static void addRecipeTypeHint(ResourceLocation recipeId, ResourceLocation typeId) {
        RECIPE_TYPE_HINTS.computeIfAbsent(recipeId, ignored -> new LinkedHashSet<>()).add(typeId);
    }

    private static void rebuildHistoricalHints() {
        RECIPE_TYPE_HINTS.clear();
        SERVER_RECIPE_TYPE_HINTS.forEach(RecipeDeltaClientState::addRecipeTypeHint);
    }

    public record JeiSyncState(
            long revision,
            Set<ResourceLocation> touchedRecipeIds,
            Map<ResourceLocation, ResourceLocation> managedEditorTypes,
            Map<ResourceLocation, Set<ResourceLocation>> recipeTypeHints
    ) {
    }
}
