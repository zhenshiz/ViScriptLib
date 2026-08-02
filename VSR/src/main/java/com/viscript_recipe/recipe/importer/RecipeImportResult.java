package com.viscript_recipe.recipe.importer;

import com.viscript_recipe.data.RecipeEntry;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public record RecipeImportResult(boolean successful, @Nullable RecipeEntry entry, Component message) {
    public static RecipeImportResult success(RecipeEntry entry, Component message) {
        return new RecipeImportResult(true, entry, message);
    }

    public static RecipeImportResult failure(Component message) {
        return new RecipeImportResult(false, null, message);
    }

    public static RecipeImportResult failure(String key, Object... args) {
        return failure(Component.translatable(key, args));
    }
}
