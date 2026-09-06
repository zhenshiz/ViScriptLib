package com.viscript_recipe.compat.avaritia;

import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import com.viscript_lib.util.math.Clamp;
import com.viscript_recipe.IModModule;
import com.viscript_recipe.compat.avaritia.canvas.CompressorCanvas;
import com.viscript_recipe.compat.avaritia.canvas.ExtremeSmithingCanvas;
import com.viscript_recipe.compat.avaritia.canvas.SpecialShapelessCanvas;
import com.viscript_recipe.compat.avaritia.canvas.TableCanvas;
import com.viscript_recipe.compat.avaritia.data.AvaritiaCompressorRecipeData;
import com.viscript_recipe.compat.avaritia.data.AvaritiaExtremeSmithingRecipeData;
import com.viscript_recipe.compat.avaritia.data.AvaritiaSpecialShapelessRecipeData;
import com.viscript_recipe.compat.avaritia.data.AvaritiaTableRecipeData;
import com.viscript_recipe.data.RecipeEditorCategory;
import com.viscript_recipe.data.RecipeEditorType;
import com.viscript_recipe.recipe.importer.RecipeImportHandler;
import net.minecraft.resources.ResourceLocation;

@LDLRegister(registry = IModModule.ID, name = AvaritiaRecipeEditorTypes.MOD_ID, modID = AvaritiaRecipeEditorTypes.MOD_ID)
public final class AvaritiaRecipeEditorTypes implements IModModule{
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
    public static final ResourceLocation INFINITY_CATALYST = create("infinity_catalyst");
    public static final ResourceLocation ETERNAL_SINGULARITY = create("eternal_singularity");
    public static final ResourceLocation FULL_MATTER_CLUSTER = create("full_matter_cluster");

    private static boolean registered;

    @Override
    public RecipeImportHandler importHandler() {return AvaritiaRecipeImporter.INSTANCE;}

    @Override
    public void registerEditorTypes() {
        if (registered) return;
        registered = true;
        registerCategories();
        registerTypes();
    }

    private void registerCategories() {
        registerCategory(RecipeEditorCategory.of(
                AvaritiaRecipeEditorTypes.CRAFTING_TABLE,
                "viscript_recipe.editor.category.avaritia.crafting_table",
                MOD_ID,
                AvaritiaRecipeEditorTypes.SHAPED_TABLE,
                AvaritiaRecipeEditorTypes.SCULK_CRAFTING_TABLE
        ));
        registerCategory(RecipeEditorCategory.of(
                NEUTRON_COMPRESSOR,
                "viscript_recipe.editor.category.avaritia.neutron_compressor",
                MOD_ID, COMPRESSOR
        ));
        registerCategory(RecipeEditorCategory.of(
                EXTREME_SMITHING_TABLE,
                "viscript_recipe.editor.category.avaritia.extreme_smithing_table",
                MOD_ID, EXTREME_SMITHING
        ));
    }

    private void registerTypes() {
        registerTableType(SHAPED_TABLE, "viscript_recipe.editor.type.avaritia.shaped_table");
        registerTableType(SHAPELESS_TABLE, "viscript_recipe.editor.type.avaritia.shapeless_table");
        registerTableType(NO_CONSUME_CATALYST_SHAPED, "viscript_recipe.editor.type.avaritia.no_consume_catalyst_shaped");
        registerEditorType(RecipeEditorType.of(
                COMPRESSOR, NEUTRON_COMPRESSOR,
                "viscript_recipe.editor.type.avaritia.compressor",
                AvaritiaCompressorRecipeData.class, AvaritiaCompressorRecipeData::new,
                CompressorCanvas::new, MOD_ID
        ));
        registerEditorType(RecipeEditorType.of(
                EXTREME_SMITHING, EXTREME_SMITHING_TABLE,
                "viscript_recipe.editor.type.avaritia.extreme_smithing",
                AvaritiaExtremeSmithingRecipeData.class, AvaritiaExtremeSmithingRecipeData::new,
                ExtremeSmithingCanvas::new, MOD_ID
        ));
        registerSpecialType(INFINITY_CATALYST, "viscript_recipe.editor.type.avaritia.infinity_catalyst");
        registerSpecialType(ETERNAL_SINGULARITY, "viscript_recipe.editor.type.avaritia.eternal_singularity");
        registerSpecialType(FULL_MATTER_CLUSTER, "viscript_recipe.editor.type.avaritia.full_matter_cluster");
    }

    private void registerTableType(ResourceLocation type, String translationKey) {
        registerEditorType(RecipeEditorType.of(
                type, CRAFTING_TABLE, translationKey,
                AvaritiaTableRecipeData.class, AvaritiaTableRecipeData::new,
                TableCanvas::new, MOD_ID
        ));
    }

    private void registerSpecialType(ResourceLocation type, String translationKey) {
        registerEditorType(RecipeEditorType.of(
                type, CRAFTING_TABLE, translationKey,
                AvaritiaSpecialShapelessRecipeData.class, AvaritiaSpecialShapelessRecipeData::new,
                SpecialShapelessCanvas::new, MOD_ID
        ));
    }

    public static boolean isTableType(ResourceLocation type) {
        return isShapedTableType(type)
                || isShapelessTableType(type)
                || isNoConsumeCatalystType(type);
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
        return switch (Clamp.clamp(tier, 1, 4)) {
            case 1 -> 3;
            case 2 -> 5;
            case 3 -> 7;
            default -> 9;
        };
    }

    public static ResourceLocation tableItemForTier(int tier) {
        return switch (Clamp.clamp(tier, 1, 4)) {
            case 1 -> SCULK_CRAFTING_TABLE;
            case 2 -> NETHER_CRAFTING_TABLE;
            case 3 -> END_CRAFTING_TABLE;
            default -> EXTREME_CRAFTING_TABLE;
        };
    }

    public static ResourceLocation create(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
