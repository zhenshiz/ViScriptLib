package com.viscript_recipe.recipe.importer;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.Nullable;

public interface RecipeImportHandler {
    boolean canImport(Recipe<?> holder);

    @Nullable
    RecipeImportResult tryImport(Recipe<?> holder, HolderLookup.Provider provider) throws RecipeImportException;
}
