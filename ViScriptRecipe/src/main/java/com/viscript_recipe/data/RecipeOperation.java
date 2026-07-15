package com.viscript_recipe.data;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum RecipeOperation implements StringRepresentable {
    ADD("add"),
    REPLACE("replace"),
    REMOVE("remove");

    private final String serializedName;

    RecipeOperation(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public @NotNull String getSerializedName() {
        return serializedName;
    }
}
