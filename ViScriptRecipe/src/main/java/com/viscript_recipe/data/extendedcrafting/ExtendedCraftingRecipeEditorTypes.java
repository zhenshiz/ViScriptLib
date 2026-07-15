package com.viscript_recipe.data.extendedcrafting;

import com.viscript_recipe.data.RecipeEditorCategory;
import com.viscript_recipe.data.RecipeEditorLayout;
import com.viscript_recipe.data.RecipeEditorTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class ExtendedCraftingRecipeEditorTypes {
    public static final String MOD_ID = "extendedcrafting";

    public static final ResourceLocation CRAFTING_CORE = create("crafting_core");
    public static final ResourceLocation CRAFTING_TABLE = create("crafting_table");
    public static final ResourceLocation BASIC_TABLE = create("basic_table");
    public static final ResourceLocation ADVANCED_TABLE = create("advanced_table");
    public static final ResourceLocation ELITE_TABLE = create("elite_table");
    public static final ResourceLocation ULTIMATE_TABLE = create("ultimate_table");
    public static final ResourceLocation COMPRESSOR = create("compressor");
    public static final ResourceLocation ENDER_CRAFTER = create("ender_crafter");
    public static final ResourceLocation FLUX_CRAFTER = create("flux_crafter");

    public static final ResourceLocation COMBINATION = create("combination");
    public static final ResourceLocation SHAPED_TABLE = create("shaped_table");
    public static final ResourceLocation SHAPELESS_TABLE = create("shapeless_table");
    public static final ResourceLocation BASIC_SHAPED_TABLE = create("basic_shaped_table");
    public static final ResourceLocation BASIC_SHAPELESS_TABLE = create("basic_shapeless_table");
    public static final ResourceLocation ADVANCED_SHAPED_TABLE = create("advanced_shaped_table");
    public static final ResourceLocation ADVANCED_SHAPELESS_TABLE = create("advanced_shapeless_table");
    public static final ResourceLocation ELITE_SHAPED_TABLE = create("elite_shaped_table");
    public static final ResourceLocation ELITE_SHAPELESS_TABLE = create("elite_shapeless_table");
    public static final ResourceLocation ULTIMATE_SHAPED_TABLE = create("ultimate_shaped_table");
    public static final ResourceLocation ULTIMATE_SHAPELESS_TABLE = create("ultimate_shapeless_table");
    public static final ResourceLocation ULTIMATE_SINGULARITY = create("ultimate_singularity");
    public static final ResourceLocation COMPRESSOR_RECIPE = create("compressor_recipe");
    public static final ResourceLocation SHAPED_ENDER_CRAFTER = create("shaped_ender_crafter");
    public static final ResourceLocation SHAPELESS_ENDER_CRAFTER = create("shapeless_ender_crafter");
    public static final ResourceLocation SHAPED_FLUX_CRAFTER = create("shaped_flux_crafter");
    public static final ResourceLocation SHAPELESS_FLUX_CRAFTER = create("shapeless_flux_crafter");

    private static final List<String> REQUIRED_MODS = List.of(MOD_ID);
    private static boolean registered;

    private ExtendedCraftingRecipeEditorTypes() {
    }

    public static synchronized void registerAll() {
        if (registered) {
            return;
        }
        registered = true;
        registerCategories();
        registerTypes();
    }

    private static void registerCategories() {
        RecipeEditorTypes.registerCategory(new RecipeEditorCategory(
                CRAFTING_CORE,
                "viscript_recipe.editor.category.extendedcrafting.crafting_core",
                MOD_ID,
                REQUIRED_MODS,
                COMBINATION,
                RecipeEditorLayout.CRAFTING_GRID
        ));
        registerTableCategory(CRAFTING_TABLE, SHAPED_TABLE, "viscript_recipe.editor.category.extendedcrafting.crafting_table");
        RecipeEditorTypes.registerCategory(new RecipeEditorCategory(
                COMPRESSOR,
                "viscript_recipe.editor.category.extendedcrafting.compressor",
                MOD_ID,
                REQUIRED_MODS,
                COMPRESSOR_RECIPE,
                RecipeEditorLayout.CRAFTING_GRID
        ));
        RecipeEditorTypes.registerCategory(new RecipeEditorCategory(
                ENDER_CRAFTER,
                "viscript_recipe.editor.category.extendedcrafting.ender_crafter",
                MOD_ID,
                REQUIRED_MODS,
                SHAPED_ENDER_CRAFTER,
                RecipeEditorLayout.CRAFTING_GRID
        ));
        RecipeEditorTypes.registerCategory(new RecipeEditorCategory(
                FLUX_CRAFTER,
                "viscript_recipe.editor.category.extendedcrafting.flux_crafter",
                MOD_ID,
                REQUIRED_MODS,
                SHAPED_FLUX_CRAFTER,
                RecipeEditorLayout.CRAFTING_GRID
        ));
    }

    private static void registerTableCategory(ResourceLocation category, ResourceLocation defaultType, String translationKey) {
        RecipeEditorTypes.registerCategory(new RecipeEditorCategory(
                category,
                translationKey,
                MOD_ID,
                REQUIRED_MODS,
                defaultType,
                RecipeEditorLayout.EXTENDED_CRAFTING_TABLE
        ));
    }

    private static void registerTypes() {
/*        RecipeEditorTypes.register(new RecipeEditorType(
                COMBINATION,
                CRAFTING_CORE,
                "viscript_recipe.editor.type.extendedcrafting.combination",
                REQUIRED_MODS,
                false,
                entry -> entry.getExtendedCraftingCombination().compile(),
                entry -> false,
                (entry, value) -> {
                },
                entry -> entry.getExtendedCraftingCombination().getResult(),
                (entry, stack) -> entry.getExtendedCraftingCombination().setResult(copy(stack))
        ));
        registerTableType(SHAPED_TABLE, CRAFTING_TABLE, "viscript_recipe.editor.type.extendedcrafting.shaped_table");
        registerTableType(SHAPELESS_TABLE, CRAFTING_TABLE, "viscript_recipe.editor.type.extendedcrafting.shapeless_table");
        RecipeEditorTypes.register(new RecipeEditorType(
                ULTIMATE_SINGULARITY,
                CRAFTING_TABLE,
                "viscript_recipe.editor.type.extendedcrafting.ultimate_singularity",
                REQUIRED_MODS,
                false,
                entry -> entry.getExtendedCraftingUltimateSingularity().compile(),
                entry -> false,
                (entry, value) -> {
                },
                entry -> entry.getExtendedCraftingUltimateSingularity().getResult(),
                (entry, stack) -> entry.getExtendedCraftingUltimateSingularity().setResult(copy(stack))
        ));
        RecipeEditorTypes.register(new RecipeEditorType(
                COMPRESSOR_RECIPE,
                COMPRESSOR,
                "viscript_recipe.editor.type.extendedcrafting.compressor",
                REQUIRED_MODS,
                false,
                entry -> entry.getExtendedCraftingCompressor().compile(),
                entry -> false,
                (entry, value) -> {
                },
                entry -> entry.getExtendedCraftingCompressor().getResult(),
                (entry, stack) -> entry.getExtendedCraftingCompressor().setResult(copy(stack))
        ));
        registerEnderType(SHAPED_ENDER_CRAFTER, "viscript_recipe.editor.type.extendedcrafting.shaped_ender_crafter");
        registerEnderType(SHAPELESS_ENDER_CRAFTER, "viscript_recipe.editor.type.extendedcrafting.shapeless_ender_crafter");
        registerFluxType(SHAPED_FLUX_CRAFTER, "viscript_recipe.editor.type.extendedcrafting.shaped_flux_crafter");
        registerFluxType(SHAPELESS_FLUX_CRAFTER, "viscript_recipe.editor.type.extendedcrafting.shapeless_flux_crafter");
    }

    private static void registerTableType(ResourceLocation type, ResourceLocation category, String translationKey) {
        RecipeEditorTypes.register(new RecipeEditorType(
                type,
                category,
                translationKey,
                REQUIRED_MODS,
                false,
                entry -> entry.getExtendedCraftingTable().compile(entry.getType()),
                entry -> false,
                (entry, value) -> {
                },
                entry -> entry.getExtendedCraftingTable().getResult(),
                (entry, stack) -> entry.getExtendedCraftingTable().setResult(copy(stack))
        ));
    }

    private static void registerEnderType(ResourceLocation type, String translationKey) {
        RecipeEditorTypes.register(new RecipeEditorType(
                type,
                ENDER_CRAFTER,
                translationKey,
                REQUIRED_MODS,
                false,
                entry -> entry.getExtendedCraftingEnderCrafter().compile(entry.getType()),
                entry -> false,
                (entry, value) -> {
                },
                entry -> entry.getExtendedCraftingEnderCrafter().getResult(),
                (entry, stack) -> entry.getExtendedCraftingEnderCrafter().setResult(copy(stack))
        ));
    }

    private static void registerFluxType(ResourceLocation type, String translationKey) {
        RecipeEditorTypes.register(new RecipeEditorType(
                type,
                FLUX_CRAFTER,
                translationKey,
                REQUIRED_MODS,
                false,
                entry -> entry.getExtendedCraftingFluxCrafter().compile(entry.getType()),
                entry -> false,
                (entry, value) -> {
                },
                entry -> entry.getExtendedCraftingFluxCrafter().getResult(),
                (entry, stack) -> entry.getExtendedCraftingFluxCrafter().setResult(copy(stack))
        ));*/
    }

    public static boolean isTableType(ResourceLocation type) {
        return isShapedTableType(type) || isShapelessTableType(type);
    }

    public static boolean isShapedTableType(ResourceLocation type) {
        return SHAPED_TABLE.equals(type)
                || BASIC_SHAPED_TABLE.equals(type)
                || ADVANCED_SHAPED_TABLE.equals(type)
                || ELITE_SHAPED_TABLE.equals(type)
                || ULTIMATE_SHAPED_TABLE.equals(type);
    }

    public static boolean isShapelessTableType(ResourceLocation type) {
        return SHAPELESS_TABLE.equals(type)
                || BASIC_SHAPELESS_TABLE.equals(type)
                || ADVANCED_SHAPELESS_TABLE.equals(type)
                || ELITE_SHAPELESS_TABLE.equals(type)
                || ULTIMATE_SHAPELESS_TABLE.equals(type);
    }

    public static boolean isShapedEnderType(ResourceLocation type) {
        return SHAPED_ENDER_CRAFTER.equals(type);
    }

    public static boolean isShapedFluxType(ResourceLocation type) {
        return SHAPED_FLUX_CRAFTER.equals(type);
    }

    public static int tableTierForType(ResourceLocation type) {
        if (SHAPED_TABLE.equals(type)
                || SHAPELESS_TABLE.equals(type)
                || BASIC_SHAPED_TABLE.equals(type)
                || BASIC_SHAPELESS_TABLE.equals(type)) {
            return 1;
        }
        if (ADVANCED_SHAPED_TABLE.equals(type) || ADVANCED_SHAPELESS_TABLE.equals(type)) {
            return 2;
        }
        if (ELITE_SHAPED_TABLE.equals(type) || ELITE_SHAPELESS_TABLE.equals(type)) {
            return 3;
        }
        if (ULTIMATE_SHAPED_TABLE.equals(type) || ULTIMATE_SHAPELESS_TABLE.equals(type) || ULTIMATE_SINGULARITY.equals(type)) {
            return 4;
        }
        return 1;
    }

    public static ResourceLocation normalizeAlias(ResourceLocation type) {
        if (BASIC_SHAPED_TABLE.equals(type)
                || ADVANCED_SHAPED_TABLE.equals(type)
                || ELITE_SHAPED_TABLE.equals(type)
                || ULTIMATE_SHAPED_TABLE.equals(type)) {
            return SHAPED_TABLE;
        }
        if (BASIC_SHAPELESS_TABLE.equals(type)
                || ADVANCED_SHAPELESS_TABLE.equals(type)
                || ELITE_SHAPELESS_TABLE.equals(type)
                || ULTIMATE_SHAPELESS_TABLE.equals(type)) {
            return SHAPELESS_TABLE;
        }
        return type;
    }

    public static int tableGridSizeForTier(int tier) {
        return switch (Math.max(1, Math.min(4, tier))) {
            case 1 -> 3;
            case 2 -> 5;
            case 3 -> 7;
            default -> 9;
        };
    }

    public static ResourceLocation tableItemForTier(int tier) {
        return switch (Math.max(1, Math.min(4, tier))) {
            case 1 -> BASIC_TABLE;
            case 2 -> ADVANCED_TABLE;
            case 3 -> ELITE_TABLE;
            default -> ULTIMATE_TABLE;
        };
    }

    public static ResourceLocation create(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    private static ItemStack copy(ItemStack stack) {
        return stack == null ? ItemStack.EMPTY : stack.copy();
    }
}
