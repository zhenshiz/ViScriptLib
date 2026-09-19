package com.viscript_recipe.compat.touhou_little_maid;

import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import com.viscript_recipe.IModModule;
import com.viscript_recipe.compat.touhou_little_maid.canvas.AltarCanvas;
import com.viscript_recipe.compat.touhou_little_maid.data.TouhouLittleMaidAltarRecipeData;
import com.viscript_recipe.data.RecipeEditorCategory;
import com.viscript_recipe.data.RecipeEditorType;
import com.viscript_recipe.recipe.importer.RecipeImportHandler;
import net.minecraft.resources.ResourceLocation;

@LDLRegister(registry = IModModule.ID, name = TouhouLittleMaidRecipeEditorTypes.MOD_ID, modID = TouhouLittleMaidRecipeEditorTypes.MOD_ID)
public final class TouhouLittleMaidRecipeEditorTypes implements IModModule{
    public static final String MOD_ID = "touhou_little_maid";

    public static final ResourceLocation ALTAR = touhouLittleMaid("altar");
    public static final ResourceLocation ALTAR_RECIPE = touhouLittleMaid("altar_recipe");

    private static boolean registered;

    @Override
    public RecipeImportHandler importHandler() {return TouhouLittleMaidRecipeImporter.INSTANCE;}

    @Override
    public void registerEditorTypes() {
        if (registered) return;
        registered = true;
        registerCategory(RecipeEditorCategory.of(
                ALTAR,
                "viscript_recipe.editor.category.touhou_little_maid.altar",
                MOD_ID, ALTAR_RECIPE, touhouLittleMaid("hakurei_gohei")
        ));
        registerEditorType(RecipeEditorType.of(
                ALTAR_RECIPE, ALTAR,
                "viscript_recipe.editor.type.touhou_little_maid.altar",
                TouhouLittleMaidAltarRecipeData.class, TouhouLittleMaidAltarRecipeData::new,
                AltarCanvas::new, MOD_ID
        ));
    }

    public static ResourceLocation touhouLittleMaid(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
