package com.viscript_recipe.compat.create;

import com.simibubi.create.foundation.recipe.RecipeFinder;
import com.simibubi.create.foundation.recipe.trie.RecipeTrieFinder;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;

public final class CreateRecipeRuntimeSupport {
    private CreateRecipeRuntimeSupport() {
    }

    public static void invalidateRecipeCaches(@Nullable ResourceManager resourceManager) {
        RecipeFinder.LISTENER.onResourceManagerReload(resourceManager);
        RecipeTrieFinder.LISTENER.onResourceManagerReload(resourceManager);
    }
}
