package com.viscript_recipe.recipe;

import com.viscript_recipe.Config;
import com.viscript_recipe.ViScriptRecipe;
import com.viscript_recipe.compat.create.CreateRecipeEditorTypes;
import com.viscript_recipe.compat.create.CreateRecipeRuntimeSupport;
import com.viscript_recipe.compat.create.data.CreateProcessingKind;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.data.RecipeOperation;
import com.viscript_recipe.network.RecipeDeltaSnapshot;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class RecipeOverrideManager {
    private static final int MAX_DELTA_RECIPE_CHANGES = 256;
    private static final int DELTA_FULL_RELOAD_RATIO = 5;
    private static final Object LOCK = new Object();
    private static final Map<RecipeManager, ManagerState> MANAGER_STATES = new WeakHashMap<>();
    private static LinkedHashMap<ResourceLocation, ResourceLocation> lastAppliedRecipeTypes = new LinkedHashMap<>();
    private static ApplyResult lastResult = ApplyResult.empty();

    private RecipeOverrideManager() {
    }

    public static ApplyResult apply(RecipeManager recipeManager, HolderLookup.Provider provider) {
        return apply(recipeManager, provider, null);
    }

    public static ApplyResult apply(RecipeManager recipeManager, HolderLookup.Provider provider, @Nullable ResourceManager resourceManager) {
        synchronized (LOCK) {
            var state = stateFor(recipeManager);
            state.baseRecipes = snapshot(recipeManager.getRecipes());
            return applyOverrides(recipeManager, provider, state, resourceManager);
        }
    }

    public static ApplyResult reload(RecipeManager recipeManager, HolderLookup.Provider provider) {
        return reload(recipeManager, provider, null);
    }

    public static ApplyResult reload(RecipeManager recipeManager, HolderLookup.Provider provider, @Nullable ResourceManager resourceManager) {
        synchronized (LOCK) {
            var state = stateFor(recipeManager);
            if (state.baseRecipes == null) {
                state.baseRecipes = snapshot(recipeManager.getRecipes());
            }
            return applyOverrides(recipeManager, provider, state, resourceManager);
        }
    }

    public static DeltaReloadResult reloadDelta(RecipeManager recipeManager, HolderLookup.Provider provider) {
        return reloadDelta(recipeManager, provider, null);
    }

    public static DeltaReloadResult reloadDelta(
            RecipeManager recipeManager,
            HolderLookup.Provider provider,
            @Nullable ResourceManager resourceManager
    ) {
        synchronized (LOCK) {
            var state = stateFor(recipeManager);
            if (state.baseRecipes == null) {
                state.baseRecipes = snapshot(recipeManager.getRecipes());
            }

            var oldRecipes = snapshot(recipeManager.getRecipes());
            var oldManagedRecipeTypes = new LinkedHashMap<>(state.managedRecipeTypes);
//            var oldArcaneAnvilRecipes = state.arcaneAnvilRecipes;
            var oldShowcaseOnly = state.showcaseOnly;
            var baseRevision = state.revision;
            var result = applyOverrides(recipeManager, provider, state, resourceManager);

            RecipeDeltaSnapshot delta;
            try {
                delta = createDeltaSnapshot(
                        baseRevision,
                        recipeManager,
                        state,
                        oldRecipes,
                        oldManagedRecipeTypes/*,
                        oldArcaneAnvilRecipes*/
                );
            } catch (RuntimeException | LinkageError e) {
                ViScriptRecipe.LOGGER.warn("Failed to encode recipe delta; falling back to a full recipe sync", e);
                return new DeltaReloadResult(result, null, DeltaFallbackReason.ENCODING_FAILED);
            }

            if (oldShowcaseOnly || state.showcaseOnly) {
                return new DeltaReloadResult(result, delta, DeltaFallbackReason.SHOWCASE_MODE);
            }
            var changed = delta.changedRecipeCount();
            var tooManyAbsolute = changed > MAX_DELTA_RECIPE_CHANGES;
            var tooManyRelative = changed > 32
                    && changed * DELTA_FULL_RELOAD_RATIO > Math.max(1, result.resultRecipeCount());
            if (tooManyAbsolute || tooManyRelative) {
                return new DeltaReloadResult(result, delta, DeltaFallbackReason.TOO_MANY_CHANGES);
            }
            return new DeltaReloadResult(result, delta, null);
        }
    }

    public static RecipeDeltaSnapshot createBaseline(RecipeManager recipeManager) {
        synchronized (LOCK) {
            var state = stateFor(recipeManager);
            if (state.baseRecipes == null) {
                state.baseRecipes = snapshot(recipeManager.getRecipes());
            }
            return new RecipeDeltaSnapshot(
                    state.revision,
                    state.revision,
                    true,
                    state.showcaseOnly,
                    false,
                    List.of(),
                    List.of(),
                    state.managedRecipeTypes,
                    state.recipeTypeHints/*,
                    state.arcaneAnvilRecipes*/
            );
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
                    .filter(entry -> Objects.equals(entry.getValue(), type))
                    .map(Map.Entry::getKey)
                    .toList();
        }
    }

    private static ApplyResult applyOverrides(
            RecipeManager recipeManager,
            HolderLookup.Provider provider,
            ManagerState state,
            @Nullable ResourceManager resourceManager
    ) {
        var base = state.baseRecipes == null
                ? new LinkedHashMap<ResourceLocation, Recipe<?>>()
                : state.baseRecipes;
        var loadedFiles = RecipeFileLoader.loadAll(provider);
        boolean showcaseOnly = Config.SHOWCASE_ONLY_VISCRIPT_RECIPES.get();
        var recipes = showcaseOnly ? new LinkedHashMap<ResourceLocation, Recipe<?>>() : new LinkedHashMap<>(base);
        var appliedRecipeTypes = new LinkedHashMap<ResourceLocation, ResourceLocation>();
        var managedRecipeTypes = new LinkedHashMap<ResourceLocation, ResourceLocation>();
//        var arcaneAnvilRecipes = new LinkedHashMap<ResourceLocation, IronArcaneAnvilOverrideManager.CompiledRecipe>();
        var alchemistCauldronFluids = new ArrayList<FluidStack>();

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
                var entryResult = applyEntry(
                        loaded.relativePath(),
                        entry,
                        recipes,
                        appliedRecipeTypes,
                        managedRecipeTypes,
//                        arcaneAnvilRecipes,
                        showcaseOnly
                );
/*                if (entryResult == ApplyEntryResult.APPLIED) {
                    collectAlchemistCauldronRecipeFluids(entry, alchemistCauldronFluids);
                }*/
                switch (entryResult) {
                    case APPLIED -> applied++;
                    case SKIPPED -> skipped++;
                    case FAILED -> failed++;
                }
            }
        }

//        IronArcaneAnvilOverrideManager.replaceAll(arcaneAnvilRecipes.values());
//        IronAlchemistCauldronFluidSupport.replaceAll(alchemistCauldronFluids);
        recipeManager.replaceRecipes(recipes.values());
        invalidateCompatRecipeCaches(resourceManager);
        lastAppliedRecipeTypes = appliedRecipeTypes;
        state.managedRecipeTypes = managedRecipeTypes;
        state.recipeTypeHints = buildRecipeTypeHints(base, recipes, managedRecipeTypes.keySet());
//        state.arcaneAnvilRecipes = List.copyOf(arcaneAnvilRecipes.values());
        state.showcaseOnly = showcaseOnly;
        state.revision++;
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
/*        if (!arcaneAnvilRecipes.isEmpty()) {
            ViScriptRecipe.LOGGER.info("Loaded {} Iron's Spells Arcane Anvil override recipes", arcaneAnvilRecipes.size());
        }
        if (IronAlchemistCauldronFluidSupport.allowedFluidCount() > 0) {
            ViScriptRecipe.LOGGER.info(
                    "Allowed {} Iron's Spells Alchemist Cauldron fluids referenced by ViScriptRecipe recipes",
                    IronAlchemistCauldronFluidSupport.allowedFluidCount()
            );
        }*/
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
            CreateRecipeRuntimeSupport.invalidateRecipeCaches(resourceManager);
        }
    }

    private static LinkedHashMap<ResourceLocation, Recipe<?>> snapshot(Collection<Recipe<?>> recipes) {
        var snapshot = new LinkedHashMap<ResourceLocation, Recipe<?>>();
        for (var holder : recipes) {
            snapshot.put(holder.getId(), holder);
        }
        return snapshot;
    }

    private static ManagerState stateFor(RecipeManager recipeManager) {
        return MANAGER_STATES.computeIfAbsent(recipeManager, ignored -> new ManagerState());
    }

    private static RecipeDeltaSnapshot createDeltaSnapshot(
            long baseRevision,
            RecipeManager recipeManager,
            ManagerState state,
            Map<ResourceLocation, Recipe<?>> oldRecipes,
            Map<ResourceLocation, ResourceLocation> oldManagedRecipeTypes/*,
            List<IronArcaneAnvilOverrideManager.CompiledRecipe> oldArcaneAnvilRecipes*/
    ) {
        var currentRecipes = snapshot(recipeManager.getRecipes());
        var affectedIds = new LinkedHashSet<ResourceLocation>();
        affectedIds.addAll(oldManagedRecipeTypes.keySet());
        affectedIds.addAll(state.managedRecipeTypes.keySet());

        var removed = new ArrayList<ResourceLocation>();
        var upserted = new ArrayList<Recipe<?>>();
        for (var id : affectedIds) {
            var oldHolder = oldRecipes.get(id);
            var newHolder = currentRecipes.get(id);
            if (oldHolder == null && newHolder == null) {
                continue;
            }
            if (newHolder == null) {
                removed.add(id);
                continue;
            }
            if (oldHolder == null || !sameRecipe(oldHolder, newHolder)) {
                upserted.add(newHolder);
            }
        }

//        var oldArcaneTag = RecipeDeltaSnapshot.encodeArcaneAnvilRecipes(oldArcaneAnvilRecipes);
//        var newArcaneTag = RecipeDeltaSnapshot.encodeArcaneAnvilRecipes(state.arcaneAnvilRecipes);
        return new RecipeDeltaSnapshot(
                baseRevision,
                state.revision,
                false,
                state.showcaseOnly,
                false, // !oldArcaneTag.equals(newArcaneTag),
                removed,
                upserted,
                state.managedRecipeTypes,
                state.recipeTypeHints/*,
                state.arcaneAnvilRecipes*/
        );
    }

    private static boolean sameRecipe(Recipe<?> left, Recipe<?> right) {
        Tag leftTag = RecipeDeltaSnapshot.encodeRecipe(left);
        Tag rightTag = RecipeDeltaSnapshot.encodeRecipe(right);
        return leftTag.equals(rightTag);
    }

    private static LinkedHashMap<ResourceLocation, ResourceLocation> buildRecipeTypeHints(
            Map<ResourceLocation, Recipe<?>> base,
            Map<ResourceLocation, Recipe<?>> recipes,
            Collection<ResourceLocation> managedRecipeIds
    ) {
        var hints = new LinkedHashMap<ResourceLocation, ResourceLocation>();
        for (var id : managedRecipeIds) {
            var holder = recipes.get(id);
            if (holder == null) {
                holder = base.get(id);
            }
            if (holder == null) {
                continue;
            }
            var recipeTypeId = BuiltInRegistries.RECIPE_TYPE.getKey(holder.getType());
            if (recipeTypeId != null) {
                hints.put(id, recipeTypeId);
            }
        }
        return hints;
    }

    private static ApplyEntryResult applyEntry(
            String source,
            RecipeEntry entry,
            LinkedHashMap<ResourceLocation, Recipe<?>> recipes,
            LinkedHashMap<ResourceLocation, ResourceLocation> appliedRecipeTypes,
            LinkedHashMap<ResourceLocation, ResourceLocation> managedRecipeTypes,
//            LinkedHashMap<ResourceLocation, IronArcaneAnvilOverrideManager.CompiledRecipe> arcaneAnvilRecipes,
            boolean showcaseOnly
    ) {
        if (entry.getRecipeId() == null) {
            ViScriptRecipe.LOGGER.warn("Skipping recipe entry with empty id in {}", source);
            return ApplyEntryResult.FAILED;
        }
        var id = entry.getRecipeId();
        try {
/*            if (IronArcaneAnvilOverrideManager.isArcaneAnvilEntry(entry)) {
                return applyArcaneAnvilEntry(source, entry, arcaneAnvilRecipes);
            }*/
            return switch (entry.getOperation()) {
                case REMOVE -> removeEntry(source, id, entry, recipes, managedRecipeTypes, showcaseOnly);
                case ADD, REPLACE -> upsertEntry(
                        source,
                        entry,
                        recipes,
                        appliedRecipeTypes,
                        managedRecipeTypes,
                        showcaseOnly
                );
            };
        } catch (Exception e) {
            ViScriptRecipe.LOGGER.error("Failed to apply recipe override {} from {}", id, source, e);
            return ApplyEntryResult.FAILED;
        }
    }

/*    private static ApplyEntryResult applyArcaneAnvilEntry(String source, RecipeEntry entry, LinkedHashMap<ResourceLocation, IronArcaneAnvilOverrideManager.CompiledRecipe> arcaneAnvilRecipes) {
        if (!ViScriptRecipe.isModLoaded(IronSpellbooksRecipeEditorTypes.MOD_ID)) {
            ViScriptRecipe.LOGGER.warn("Skipping Iron's Spells Arcane Anvil override {} because irons_spellbooks is not loaded", entry.getRecipeId());
            return ApplyEntryResult.SKIPPED;
        }
        var id = entry.getRecipeId();
        return switch (entry.getOperation()) {
            case REMOVE -> arcaneAnvilRecipes.remove(id) == null ? ApplyEntryResult.SKIPPED : ApplyEntryResult.APPLIED;
            case ADD, REPLACE -> {
                var exists = arcaneAnvilRecipes.containsKey(id);
                if (entry.getOperation() == RecipeOperation.ADD && exists) {
                    ViScriptRecipe.LOGGER.warn("Arcane Anvil override {} adds existing recipe {}; replacing it", source, id);
                }
                arcaneAnvilRecipes.put(id, IronArcaneAnvilOverrideManager.compile(id, entry.getData()));
                yield ApplyEntryResult.APPLIED;
            }
        };
    }

    private static void collectAlchemistCauldronRecipeFluids(RecipeEntry entry, Collection<FluidStack> fluids) {
        if (entry.getOperation() == RecipeOperation.REMOVE || !isAlchemistCauldronRecipe(entry)) {
            return;
        }
        var data = (IronAlchemistCauldronRecipeData) entry.getData();
        if (entry.isType(IronSpellbooksRecipeEditorTypes.ALCHEMIST_CAULDRON_BREW)) {
            addFluid(fluids, data.getBaseFluid());
            if (data.getResultFluids() != null) {
                data.getResultFluids().forEach(stack -> addFluid(fluids, stack));
            }
            return;
        }
        addFluid(fluids, data.getFluid());
    }

    private static boolean isAlchemistCauldronRecipe(RecipeEntry entry) {
        return entry.isType(IronSpellbooksRecipeEditorTypes.ALCHEMIST_CAULDRON_FILL)
                || entry.isType(IronSpellbooksRecipeEditorTypes.ALCHEMIST_CAULDRON_EMPTY)
                || entry.isType(IronSpellbooksRecipeEditorTypes.ALCHEMIST_CAULDRON_BREW);
    }*/

    private static void addFluid(Collection<FluidStack> fluids, FluidStack stack) {
        if (stack != null && !stack.isEmpty()) {
            fluids.add(stack.copy());
        }
    }

    private static ApplyEntryResult removeEntry(
            String source,
            ResourceLocation id,
            RecipeEntry entry,
            LinkedHashMap<ResourceLocation, Recipe<?>> recipes,
            LinkedHashMap<ResourceLocation, ResourceLocation> managedRecipeTypes,
            boolean showcaseOnly
    ) {
        var removed = false;
        for (var recipeId : removableRecipeIds(id, entry)) {
            managedRecipeTypes.put(recipeId, entry.getType());
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

    private static ApplyEntryResult upsertEntry(
            String source,
            RecipeEntry entry,
            LinkedHashMap<ResourceLocation, Recipe<?>> recipes,
            LinkedHashMap<ResourceLocation, ResourceLocation> appliedRecipeTypes,
            LinkedHashMap<ResourceLocation, ResourceLocation> managedRecipeTypes,
            boolean showcaseOnly
    ) {
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
            managedRecipeTypes.put(holder.getId(), entry.getType());
        }
        return ApplyEntryResult.APPLIED;
    }

    private static List<Recipe<?>> compileRecipeHolders(ResourceLocation id, RecipeEntry entry) {
        var compiled = List.of(entry.compile());
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

    public enum DeltaFallbackReason {
        SHOWCASE_MODE("commands.viscript_recipe.reload.delta.fallback.showcase_mode"),
        TOO_MANY_CHANGES("commands.viscript_recipe.reload.delta.fallback.too_many_changes"),
        ENCODING_FAILED("commands.viscript_recipe.reload.delta.fallback.encoding_failed");

        private final String translationKey;

        DeltaFallbackReason(String translationKey) {
            this.translationKey = translationKey;
        }

        public String translationKey() {
            return translationKey;
        }
    }

    public record DeltaReloadResult(
            ApplyResult applyResult,
            @Nullable RecipeDeltaSnapshot delta,
            @Nullable DeltaFallbackReason fallbackReason
    ) {
        public boolean requiresFullSync() {
            return fallbackReason != null || delta == null;
        }
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

    private static final class ManagerState {
        @Nullable
        private LinkedHashMap<ResourceLocation, Recipe<?>> baseRecipes;
        private LinkedHashMap<ResourceLocation, ResourceLocation> managedRecipeTypes = new LinkedHashMap<>();
        private LinkedHashMap<ResourceLocation, ResourceLocation> recipeTypeHints = new LinkedHashMap<>();
//        private List<IronArcaneAnvilOverrideManager.CompiledRecipe> arcaneAnvilRecipes = List.of();
        private boolean showcaseOnly;
        private long revision;
    }
}
