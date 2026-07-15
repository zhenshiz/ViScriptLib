package com.viscript_recipe.data;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum IngredientValueKind implements StringRepresentable {
    ITEM("item"),
    TAG("tag"),
    ITEM_ABILITY("item_ability");

    private final String serializedName;

    IngredientValueKind(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public @NotNull String getSerializedName() {
        return serializedName;
    }
}
