package com.viscript_recipe.recipe;

import com.simibubi.create.foundation.recipe.RecipeFinder;
import com.simibubi.create.foundation.recipe.trie.RecipeTrieFinder;
import com.viscript_recipe.Config;
import com.viscript_recipe.ViScriptRecipe;
import com.viscript_recipe.compat.create.CreateRecipeFactory;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.data.RecipeOperation;
import com.viscript_recipe.data.create.CreateProcessingKind;
import com.viscript_recipe.data.create.CreateRecipeEditorTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

public final class RecipeOverrideManager {
    private static final Object LOCK = new Object();
    @Nullable
    private static LinkedHashMap<ResourceLocation, Recipe<?>> baseRecipes;
    private static LinkedHashMap<ResourceLocation, ResourceLocation> lastAppliedRecipeTypes = new LinkedHashMap<>();
    private static ApplyResult lastResult = ApplyResult.empty();

    private RecipeOverrideManager() {
    }

    public static ApplyResult apply(RecipeManager recipeManager, HolderLookup.Provider provider) {
        return apply(recipeManager, provider, null);
    }

    public static ApplyResult apply(RecipeManager recipeManager, HolderLookup.Provider provider, @Nullable ResourceManager resourceManager) {
        synchronized (LOCK) {
            baseRecipes = snapshot(recipeManager.getRecipes());
            return applyOverrides(recipeManager, provider, baseRecipes, resourceManager);
        }
    }

    public static ApplyResult reload(RecipeManager recipeManager, HolderLookup.Provider provider) {
        return reload(recipeManager, provider, null);
    }

    public static ApplyResult reload(RecipeManager recipeManager, HolderLookup.Provider provider, @Nullable ResourceManager resourceManager) {
        synchronized (LOCK) {
            if (baseRecipes == null) {
                baseRecipes = snapshot(recipeManager.getRecipes());
            }
            return applyOverrides(recipeManager, provider, baseRecipes, resourceManager);
        }
    }

    public static ApplyResult getLastResult() {
        synchronized (LOCK) {
            return lastResult;
        }
    }

    public static List<ResourceLocation> recipeIdsForEditorType(ResourceLocation type) {
        synchronized (LOCK) {
            return lastAppliedRecipeTypes.entrySet()
                    .stream()
                    .filter(entry -> java.util.Objects.equals(entry.getValue(), type))
                    .map(java.util.Map.Entry::getKey)
                    .toList();
        }
    }

    private static ApplyResult applyOverrides(RecipeManager recipeManager, HolderLookup.Provider provider, LinkedHashMap<ResourceLocation, Recipe<?>> base, @Nullable ResourceManager resourceManager) {
        var loadedFiles = RecipeFileLoader.loadAll(provider);
        var showcaseOnly = Config.SHOWCASE_ONLY_VISCRIPT_RECIPES.get();
        var recipes = showcaseOnly ? new LinkedHashMap<ResourceLocation, Recipe<?>>() : new LinkedHashMap<>(base);
        var appliedRecipeTypes = new LinkedHashMap<ResourceLocation, ResourceLocation>();

        int entries = 0;
        int enabled = 0;
        int applied = 0;
        int skipped = 0;
        int failed = 0;
        for (var loaded : loadedFiles) {
            var file = loaded.file();
            if (file == null) {
                continue;
            }
            for (var entry : file.getEntries()) {
                entries++;
                if (!entry.isEnabled()) {
                    skipped++;
                    continue;
                }
                enabled++;
                var entryResult = applyEntry(loaded.relativePath(), entry, recipes, appliedRecipeTypes, showcaseOnly);
                switch (entryResult) {
                    case APPLIED -> applied++;
                    case SKIPPED -> skipped++;
                    case FAILED -> failed++;
                }
            }
        }

        recipeManager.replaceRecipes(recipes.values());
        invalidateCompatRecipeCaches(resourceManager);
        lastAppliedRecipeTypes = appliedRecipeTypes;
        lastResult = new ApplyResult(
                loadedFiles.size(),
                entries,
                enabled,
                applied,
                skipped,
                failed,
                base.size(),
                recipes.size()
        );
        ViScriptRecipe.LOGGER.info(
                "Reloaded ViScriptRecipe overrides: {} files, {} entries, {} enabled, {} applied, {} skipped, {} failed",
                lastResult.fileCount(),
                lastResult.entryCount(),
                lastResult.enabledEntryCount(),
                lastResult.appliedEntryCount(),
                lastResult.skippedEntryCount(),
                lastResult.failedEntryCount()
        );
        if (showcaseOnly) {
            ViScriptRecipe.LOGGER.info(
                    "ViScriptRecipe showcase recipe mode is enabled: cleared {} base recipes before applying .recipe files",
                    base.size()
            );
        }
        return lastResult;
    }

    private static void invalidateCompatRecipeCaches(@Nullable ResourceManager resourceManager) {
        if (ViScriptRecipe.isModLoaded(CreateRecipeEditorTypes.MOD_ID)) {
            RecipeFinder.LISTENER.onResourceManagerReload(resourceManager);
            RecipeTrieFinder.LISTENER.onResourceManagerReload(resourceManager);
        }
    }

    private static LinkedHashMap<ResourceLocation, Recipe<?>> snapshot(Collection<Recipe<?>> recipes) {
        var snapshot = new LinkedHashMap<ResourceLocation, Recipe<?>>();
        for (var holder : recipes) {
            snapshot.put(holder.getId(), holder);
        }
        return snapshot;
    }

    private static ApplyEntryResult applyEntry(String source, RecipeEntry entry, LinkedHashMap<ResourceLocation, Recipe<?>> recipes, LinkedHashMap<ResourceLocation, ResourceLocation> appliedRecipeTypes, boolean showcaseOnly) {
        if (entry.getRecipeId() == null) {
            ViScriptRecipe.LOGGER.warn("Skipping recipe entry with empty id in {}", source);
            return ApplyEntryResult.FAILED;
        }
        var id = entry.getRecipeId();
        try {
            return switch (entry.getOperation()) {
                case REMOVE -> removeEntry(source, id, entry, recipes, showcaseOnly);
                case ADD, REPLACE -> upsertEntry(source, entry, recipes, appliedRecipeTypes, showcaseOnly);
            };
        } catch (Exception e) {
            ViScriptRecipe.LOGGER.error("Failed to apply recipe override {} from {}", id, source, e);
            return ApplyEntryResult.FAILED;
        }
    }

    private static ApplyEntryResult removeEntry(String source, ResourceLocation id, RecipeEntry entry, LinkedHashMap<ResourceLocation, Recipe<?>> recipes, boolean showcaseOnly) {
        var removed = false;
        for (var recipeId : removableRecipeIds(id, entry)) {
            removed |= recipes.remove(recipeId) != null;
        }
        if (!removed) {
            if (!showcaseOnly) {
                ViScriptRecipe.LOGGER.warn("Recipe override {} tried to remove missing recipe {}", source, id);
            }
            return ApplyEntryResult.SKIPPED;
        }
        return ApplyEntryResult.APPLIED;
    }

    private static ApplyEntryResult upsertEntry(String source, RecipeEntry entry, LinkedHashMap<ResourceLocation, Recipe<?>> recipes, LinkedHashMap<ResourceLocation, ResourceLocation> appliedRecipeTypes, boolean showcaseOnly) {
        var id = entry.getRecipeId();
        var holders = compileRecipeHolders(id, entry);
        if (holders.isEmpty()) {
            ViScriptRecipe.LOGGER.warn("Recipe override {} compiled no recipes for {}", source, id);
            return ApplyEntryResult.FAILED;
        }
        for (var holder : holders) {
            var exists = recipes.containsKey(holder.getId());
            if (entry.getOperation() == RecipeOperation.ADD && exists) {
                ViScriptRecipe.LOGGER.warn("Recipe override {} adds existing recipe {}; replacing it", source, holder.getId());
            } else if (entry.getOperation() == RecipeOperation.REPLACE && !exists && !showcaseOnly) {
                ViScriptRecipe.LOGGER.warn("Recipe override {} replaces missing recipe {}; adding it", source, holder.getId());
            }
            recipes.put(holder.getId(), holder);
            appliedRecipeTypes.put(holder.getId(), entry.getType());
        }
        return ApplyEntryResult.APPLIED;
    }

    private static List<Recipe<?>> compileRecipeHolders(ResourceLocation id, RecipeEntry entry) {
        var compiled = compileEntryRecipes(entry);
        var holders = new ArrayList<Recipe<?>>();
        for (int i = 0; i < compiled.size(); i++) {
            var recipeId = derivedRecipeId(id, i);
            Recipe<?> recipe = compiled.get(i);
            if (recipe instanceof RecipeIdSetter setter) {
                setter.setId(recipeId);
                holders.add(recipe);
            } else {
                ViScriptRecipe.LOGGER.error("Recipe type {} has no id setter, skipping!", recipe.getClass().getName());
            }
        }
        return holders;
    }

    private static List<Recipe<?>> compileEntryRecipes(RecipeEntry entry) {
        var createKind = CreateProcessingKind.byType(entry.getType()).orElse(null);
        if (createKind == CreateProcessingKind.BLOCK_CUTTING) {
            return CreateRecipeFactory.compileProcessingRecipes(entry.getType(), entry.getCreateProcessing());
        }
        return List.of(entry.compile());
    }

    private static List<ResourceLocation> removableRecipeIds(ResourceLocation id, RecipeEntry entry) {
        var createKind = CreateProcessingKind.byType(entry.getType()).orElse(null);
        if (createKind != CreateProcessingKind.BLOCK_CUTTING) {
            return List.of(id);
        }
        var ids = new ArrayList<ResourceLocation>();
        for (int i = 0; i < createKind.maxItemOutputs(); i++) {
            ids.add(derivedRecipeId(id, i));
        }
        return ids;
    }

    private static ResourceLocation derivedRecipeId(ResourceLocation baseId, int index) {
        if (index <= 0) {
            return baseId;
        }
        return new ResourceLocation(baseId.getNamespace(), baseId.getPath() + "_output_" + (index + 1));
    }

    private enum ApplyEntryResult {
        APPLIED,
        SKIPPED,
        FAILED
    }

    public record ApplyResult(
            int fileCount,
            int entryCount,
            int enabledEntryCount,
            int appliedEntryCount,
            int skippedEntryCount,
            int failedEntryCount,
            int baseRecipeCount,
            int resultRecipeCount
    ) {
        public static ApplyResult empty() {
            return new ApplyResult(0, 0, 0, 0, 0, 0, 0, 0);
        }
    }
}
