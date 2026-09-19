package com.viscript_recipe.compat.mekanism;

import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import com.viscript_recipe.IModModule;
import com.viscript_recipe.compat.mekanism.canvas.MekanismCanvas;
import com.viscript_recipe.compat.mekanism.data.MekanismRecipeData;
import com.viscript_recipe.compat.mekanism.data.MekanismRecipeKind;
import com.viscript_recipe.data.RecipeEditorCategory;
import com.viscript_recipe.data.RecipeEditorType;
import com.viscript_recipe.recipe.importer.RecipeImportHandler;

@LDLRegister(registry = IModModule.ID, name = MekanismRecipeEditorTypes.MOD_ID, modID = MekanismRecipeEditorTypes.MOD_ID)
public final class MekanismRecipeEditorTypes implements IModModule{
    public static final String MOD_ID = "mekanism";
    private static boolean registered;

    @Override
    public RecipeImportHandler importHandler() {return MekanismRecipeImporter.INSTANCE;}

    @Override
    public void registerEditorTypes() {
        if (registered) return;
        registered = true;
        for (var kind : MekanismRecipeKind.values()) {
            var path = kind.typeId().getPath();
            registerCategory(RecipeEditorCategory.of(
                    kind.typeId(), "viscript_recipe.editor.category.mekanism." + path,
                    MOD_ID, kind.typeId(), kind.workstationId()
            ));
            registerEditorType(RecipeEditorType.of(
                    kind.typeId(), kind.typeId(),
                    "viscript_recipe.editor.type.mekanism." + path,
                    MekanismRecipeData.class, MekanismRecipeData::new,
                    MekanismCanvas::new, MOD_ID
            ));
        }
    }
}
