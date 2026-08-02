package com.viscript_recipe.data;

import com.viscript_recipe.compat.RecipeCompatModules;
import com.viscript_recipe.data.create.CreateRecipeEditorTypes;
import com.viscript_recipe.data.farmersdelight.FarmersDelightRecipeEditorTypes;
import com.viscript_recipe.data.vanilla.VanillaRecipeEditorTypes;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

public final class RecipeEditorTypes {
    public static final ResourceLocation CRAFTING_TABLE = VanillaRecipeEditorTypes.CRAFTING_TABLE;
    public static final ResourceLocation FURNACE = VanillaRecipeEditorTypes.FURNACE;
    public static final ResourceLocation BLAST_FURNACE = VanillaRecipeEditorTypes.BLAST_FURNACE;
    public static final ResourceLocation SMOKER = VanillaRecipeEditorTypes.SMOKER;
    public static final ResourceLocation CAMPFIRE = VanillaRecipeEditorTypes.CAMPFIRE;
    public static final ResourceLocation STONECUTTER = VanillaRecipeEditorTypes.STONECUTTER;
    public static final ResourceLocation SMITHING_TABLE = VanillaRecipeEditorTypes.SMITHING_TABLE;
    public static final ResourceLocation CRAFTING_SHAPED = VanillaRecipeEditorTypes.CRAFTING_SHAPED;
    public static final ResourceLocation CRAFTING_SHAPELESS = VanillaRecipeEditorTypes.CRAFTING_SHAPELESS;
    public static final ResourceLocation SMELTING = VanillaRecipeEditorTypes.SMELTING;
    public static final ResourceLocation BLASTING = VanillaRecipeEditorTypes.BLASTING;
    public static final ResourceLocation SMOKING = VanillaRecipeEditorTypes.SMOKING;
    public static final ResourceLocation CAMPFIRE_COOKING = VanillaRecipeEditorTypes.CAMPFIRE_COOKING;
    public static final ResourceLocation STONECUTTING = VanillaRecipeEditorTypes.STONECUTTING;
    public static final ResourceLocation SMITHING_TRANSFORM = VanillaRecipeEditorTypes.SMITHING_TRANSFORM;

    public static final ResourceLocation FARMERSDELIGHT_COOKING_POT = FarmersDelightRecipeEditorTypes.COOKING_POT;
    public static final ResourceLocation FARMERSDELIGHT_CUTTING_BOARD = FarmersDelightRecipeEditorTypes.CUTTING_BOARD;
    public static final ResourceLocation FARMERSDELIGHT_COOKING = FarmersDelightRecipeEditorTypes.COOKING;
    public static final ResourceLocation FARMERSDELIGHT_CUTTING = FarmersDelightRecipeEditorTypes.CUTTING;

    public static final ResourceLocation CREATE_CRUSHING = CreateRecipeEditorTypes.create("crushing");
    public static final ResourceLocation CREATE_MIXING = CreateRecipeEditorTypes.create("mixing");
    public static final ResourceLocation CREATE_MECHANICAL_CRAFTING = CreateRecipeEditorTypes.MECHANICAL_CRAFTING;
    public static final ResourceLocation CREATE_SEQUENCED_ASSEMBLY = CreateRecipeEditorTypes.SEQUENCED_ASSEMBLY;

    private static final LinkedHashMap<ResourceLocation, RecipeEditorCategory> CATEGORIES = new LinkedHashMap<>();
    private static final LinkedHashMap<ResourceLocation, RecipeEditorType> TYPES = new LinkedHashMap<>();

    static {
        RecipeCompatModules.registerEditorTypes();
    }

    private RecipeEditorTypes() {
    }

    public static void registerCategory(RecipeEditorCategory category) {
        CATEGORIES.put(category.id(), category);
    }

    public static void register(RecipeEditorType type) {
        TYPES.put(type.id(), type);
    }

    public static Collection<RecipeEditorCategory> allCategories() {
        return List.copyOf(CATEGORIES.values());
    }

    public static Collection<RecipeEditorType> all() {
        return List.copyOf(TYPES.values());
    }

    public static Optional<RecipeEditorCategory> getCategory(@Nullable ResourceLocation id) {
        return Optional.ofNullable(id == null ? null : CATEGORIES.get(id));
    }

    public static Optional<RecipeEditorType> get(@Nullable ResourceLocation id) {
        return Optional.ofNullable(id == null ? null : TYPES.get(id));
    }

    public static RecipeEditorCategory requireCategory(ResourceLocation id) {
        return getCategory(id).orElseThrow(() -> new IllegalArgumentException("Unknown recipe editor category: " + id));
    }

    public static RecipeEditorType require(ResourceLocation id) {
        return get(id).orElseThrow(() -> new IllegalArgumentException("Unknown recipe editor type: " + id));
    }

    public static List<RecipeEditorCategory> availableCategories() {
        return CATEGORIES.values().stream()
                .filter(RecipeEditorCategory::isAvailable)
                .filter(category -> !availableInCategory(category.id()).isEmpty())
                .toList();
    }

    public static List<RecipeEditorType> availableInCategory(ResourceLocation category) {
        return TYPES.values().stream()
                .filter(type -> type.category().equals(category))
                .filter(RecipeEditorType::isAvailable)
                .toList();
    }

    public static ResourceLocation defaultTypeForCategory(ResourceLocation category) {
        var configuredDefault = getCategory(category).map(RecipeEditorCategory::defaultType).orElse(CRAFTING_SHAPED);
        if (get(configuredDefault).filter(RecipeEditorType::isAvailable).isPresent()) {
            return configuredDefault;
        }
        return availableInCategory(category).stream()
                .findFirst()
                .map(RecipeEditorType::id)
                .orElse(CRAFTING_SHAPED);
    }

    public static boolean isInCategory(@Nullable ResourceLocation id, ResourceLocation category) {
        return get(id)
                .map(type -> type.category().equals(category))
                .orElse(false);
    }
}
