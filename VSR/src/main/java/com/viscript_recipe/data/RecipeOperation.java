package com.viscript_recipe.data;

import lombok.Getter;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;

public enum RecipeOperation implements StringRepresentable {
    ADD("add"),
    REPLACE("replace"),
    REMOVE("remove");

    @Getter
    private final String serializedName;

    RecipeOperation(String serializedName) {
        this.serializedName = serializedName;
    }

    public Component displayName() {
        return Component.translatable("viscript_recipe.editor.operation." + serializedName);
    }
}
