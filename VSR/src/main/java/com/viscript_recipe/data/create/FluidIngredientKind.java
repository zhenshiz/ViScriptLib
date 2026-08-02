package com.viscript_recipe.data.create;

import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum FluidIngredientKind implements StringRepresentable {
    FLUID("fluid"),
    TAG("tag");

    private final String serializedName;

    FluidIngredientKind(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public @NotNull String getSerializedName() {
        return serializedName;
    }

    public Component displayName() {
        return Component.translatable("viscript_recipe.editor.create.fluid_ingredient.kind." + serializedName);
    }
}
