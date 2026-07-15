package com.viscript_recipe.data.create;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum CreateHeatCondition implements StringRepresentable {
    NONE("none"),
    HEATED("heated"),
    SUPERHEATED("superheated");

    private final String serializedName;

    CreateHeatCondition(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public @NotNull String getSerializedName() {
        return serializedName;
    }
}
