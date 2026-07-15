package com.viscript_recipe.data.avaritia;

import com.viscript_recipe.data.RecipeEditorCategory;
import com.viscript_recipe.data.RecipeEditorLayout;
import com.viscript_recipe.data.RecipeEditorTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class AvaritiaRecipeEditorTypes {
    public static final String MOD_ID = "avaritia";

    public static final ResourceLocation CRAFTING_TABLE = create("crafting_table");
    public static final ResourceLocation SCULK_CRAFTING_TABLE = create("sculk_crafting_table");
    public static final ResourceLocation NETHER_CRAFTING_TABLE = create("nether_crafting_table");
    public static final ResourceLocation END_CRAFTING_TABLE = create("end_crafting_table");
    public static final ResourceLocation EXTREME_CRAFTING_TABLE = create("extreme_crafting_table");
    public static final ResourceLocation NEUTRON_COMPRESSOR = create("neutron_compressor");
    public static final ResourceLocation EXTREME_SMITHING_TABLE = create("extreme_smithing_table");

    public static final ResourceLocation SHAPED_TABLE = create("shaped_table");
    public static final ResourceLocation SHAPELESS_TABLE = create("shapeless_table");
    public static final ResourceLocation SCULK_SHAPED_TABLE = create("sculk_shaped_table");
    public static final ResourceLocation SCULK_SHAPELESS_TABLE = create("sculk_shapeless_table");
    public static final ResourceLocation NETHER_SHAPED_TABLE = create("nether_shaped_table");
    public static final ResourceLocation NETHER_SHAPELESS_TABLE = create("nether_shapeless_table");
    public static final ResourceLocation END_SHAPED_TABLE = create("end_shaped_table");
    public static final ResourceLocation END_SHAPELESS_TABLE = create("end_shapeless_table");
    public static final ResourceLocation EXTREME_SHAPED_TABLE = create("extreme_shaped_table");
    public static final ResourceLocation EXTREME_SHAPELESS_TABLE = create("extreme_shapeless_table");
    public static final ResourceLocation NO_CONSUME_CATALYST_SHAPED = create("no_consume_catalyst_shaped");
    public static final ResourceLocation COMPRESSOR = create("compressor");
    public static final ResourceLocation EXTREME_SMITHING = create("extreme_smithing");

    private static final List<String> REQUIRED_MODS = List.of(MOD_ID);
    private static boolean registered;

    private AvaritiaRecipeEditorTypes() {
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
        registerTableCategory(CRAFTING_TABLE, SHAPED_TABLE, "viscript_recipe.editor.category.avaritia.crafting_table");
        RecipeEditorTypes.registerCategory(new RecipeEditorCategory(
                NEUTRON_COMPRESSOR,
                "viscript_recipe.editor.category.avaritia.neutron_compressor",
                MOD_ID,
                REQUIRED_MODS,
                COMPRESSOR,
                RecipeEditorLayout.AVARITIA_COMPRESSOR
        ));
        RecipeEditorTypes.registerCategory(new RecipeEditorCategory(
                EXTREME_SMITHING_TABLE,
                "viscript_recipe.editor.category.avaritia.extreme_smithing_table",
                MOD_ID,
                REQUIRED_MODS,
                EXTREME_SMITHING,
                RecipeEditorLayout.AVARITIA_EXTREME_SMITHING
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
/*        registerTableType(SHAPED_TABLE, CRAFTING_TABLE, "viscript_recipe.editor.type.avaritia.shaped_table");
        registerTableType(SHAPELESS_TABLE, CRAFTING_TABLE, "viscript_recipe.editor.type.avaritia.shapeless_table");
        registerTableType(NO_CONSUME_CATALYST_SHAPED, CRAFTING_TABLE, "viscript_recipe.editor.type.avaritia.no_consume_catalyst_shaped");
        RecipeEditorTypes.register(new RecipeEditorType(
                COMPRESSOR,
                NEUTRON_COMPRESSOR,
                "viscript_recipe.editor.type.avaritia.compressor",
                REQUIRED_MODS,
                false,
                entry -> entry.getAvaritiaCompressor().compile(),
                entry -> false,
                (entry, value) -> {
                },
                entry -> entry.getAvaritiaCompressor().getResult(),
                (entry, stack) -> entry.getAvaritiaCompressor().setResult(copy(stack))
        ));
        RecipeEditorTypes.register(new RecipeEditorType(
                EXTREME_SMITHING,
                EXTREME_SMITHING_TABLE,
                "viscript_recipe.editor.type.avaritia.extreme_smithing",
                REQUIRED_MODS,
                false,
                entry -> entry.getAvaritiaExtremeSmithing().compile(),
                entry -> false,
                (entry, value) -> {
                },
                entry -> entry.getAvaritiaExtremeSmithing().getResult(),
                (entry, stack) -> entry.getAvaritiaExtremeSmithing().setResult(copy(stack))
        ));*/
    }

/*    private static void registerTableType(ResourceLocation type, ResourceLocation category, String translationKey) {
        RecipeEditorTypes.register(new RecipeEditorType(
                type,
                category,
                translationKey,
                REQUIRED_MODS,
                false,
                entry -> entry.getAvaritiaTable().compile(entry.getType()),
                entry -> false,
                (entry, value) -> {
                },
                entry -> entry.getAvaritiaTable().getResult(),
                (entry, stack) -> entry.getAvaritiaTable().setResult(copy(stack))
        ));
    }*/

    public static boolean isTableType(ResourceLocation type) {
        return isShapedTableType(type)
                || isShapelessTableType(type)
                || isNoConsumeCatalystType(type);
    }

    public static boolean isNormalTableType(ResourceLocation type) {
        return isShapedTableType(type) || isShapelessTableType(type) || isNoConsumeCatalystType(type);
    }

    public static boolean isShapedTableType(ResourceLocation type) {
        return SHAPED_TABLE.equals(type)
                || SCULK_SHAPED_TABLE.equals(type)
                || NETHER_SHAPED_TABLE.equals(type)
                || END_SHAPED_TABLE.equals(type)
                || EXTREME_SHAPED_TABLE.equals(type);
    }

    public static boolean isShapelessTableType(ResourceLocation type) {
        return SHAPELESS_TABLE.equals(type)
                || SCULK_SHAPELESS_TABLE.equals(type)
                || NETHER_SHAPELESS_TABLE.equals(type)
                || END_SHAPELESS_TABLE.equals(type)
                || EXTREME_SHAPELESS_TABLE.equals(type);
    }

    public static boolean isNoConsumeCatalystType(ResourceLocation type) {
        return NO_CONSUME_CATALYST_SHAPED.equals(type);
    }

    public static int tableTierForType(ResourceLocation type) {
        if (SHAPED_TABLE.equals(type) || SHAPELESS_TABLE.equals(type)) {
            return 1;
        }
        if (SCULK_SHAPED_TABLE.equals(type) || SCULK_SHAPELESS_TABLE.equals(type)) {
            return 1;
        }
        if (NETHER_SHAPED_TABLE.equals(type) || NETHER_SHAPELESS_TABLE.equals(type)) {
            return 2;
        }
        if (END_SHAPED_TABLE.equals(type) || END_SHAPELESS_TABLE.equals(type)) {
            return 3;
        }
        return 4;
    }

    public static ResourceLocation normalizeAlias(ResourceLocation type) {
        if (SCULK_SHAPED_TABLE.equals(type)
                || NETHER_SHAPED_TABLE.equals(type)
                || END_SHAPED_TABLE.equals(type)
                || EXTREME_SHAPED_TABLE.equals(type)) {
            return SHAPED_TABLE;
        }
        if (SCULK_SHAPELESS_TABLE.equals(type)
                || NETHER_SHAPELESS_TABLE.equals(type)
                || END_SHAPELESS_TABLE.equals(type)
                || EXTREME_SHAPELESS_TABLE.equals(type)) {
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
            case 1 -> SCULK_CRAFTING_TABLE;
            case 2 -> NETHER_CRAFTING_TABLE;
            case 3 -> END_CRAFTING_TABLE;
            default -> EXTREME_CRAFTING_TABLE;
        };
    }

    public static ResourceLocation create(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    private static ItemStack copy(ItemStack stack) {
        return stack == null ? ItemStack.EMPTY : stack.copy();
    }
}
