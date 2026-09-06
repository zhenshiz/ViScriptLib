package com.viscript_recipe;

import com.lowdragmc.lowdraglib2.registry.AutoRegistry;
import com.lowdragmc.lowdraglib2.registry.ILDLRegister;
import com.viscript_recipe.data.RecipeEditorCategory;
import com.viscript_recipe.data.RecipeEditorType;
import com.viscript_recipe.data.RecipeEditorTypes;
import com.viscript_recipe.recipe.importer.RecipeImportHandler;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

public interface IModModule extends ILDLRegister<IModModule, Supplier<IModModule>> {
    String ID = ViScriptRecipe.MOD_ID + ":mod_module";
    AutoRegistry.LDLibRegister<IModModule, Supplier<IModModule>> MODULES =
            AutoRegistry.LDLibRegister.create(new ResourceLocation(ID), IModModule.class, AutoRegistry::noArgsCreator);

    RecipeImportHandler importHandler();

    void registerEditorTypes();

    default void registerCategory(RecipeEditorCategory category) {RecipeEditorTypes.registerCategory(category);}

    default void registerEditorType(RecipeEditorType editorType) {RecipeEditorTypes.register(editorType);}
}
