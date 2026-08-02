package com.viscript_recipe.data;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.ViScriptRecipe;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class RecipeFile implements IPersistedSerializable, IConfigurable {
    public static final int CURRENT_FORMAT_VERSION = 1;

    @Persisted
    private int formatVersion = CURRENT_FORMAT_VERSION;
    @Persisted
    private String packId = "";
    @Persisted
    private String recipeNamespace = ViScriptRecipe.MOD_ID;
    @Persisted
    private List<RecipeEntry> entries = new ArrayList<>();

    public static boolean isValidRecipeNamespace(String namespace) {
        return namespace != null && !namespace.isEmpty() && ResourceLocation.tryBuild(namespace, "recipe") != null;
    }

    public String getRecipeNamespace() {
        return isValidRecipeNamespace(recipeNamespace) ? recipeNamespace : ViScriptRecipe.MOD_ID;
    }

    public RecipeFile setRecipeNamespace(String recipeNamespace) {
        if (isValidRecipeNamespace(recipeNamespace)) {
            this.recipeNamespace = recipeNamespace;
        }
        return this;
    }
}
