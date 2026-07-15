package com.viscript_recipe.data.create;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum CreateFluidIngredientKind implements StringRepresentable {
    FLUID("fluid"),
    TAG("tag");

    private final String serializedName;

    CreateFluidIngredientKind(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public @NotNull String getSerializedName() {
        return serializedName;
    }
}
