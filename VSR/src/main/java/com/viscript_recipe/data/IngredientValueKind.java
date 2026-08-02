package com.viscript_recipe.data;

import lombok.Getter;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;

public enum IngredientValueKind implements StringRepresentable {
    ITEM("item"),
    TAG("tag"),
    ITEM_ABILITY("item_ability");

    @Getter
    private final String serializedName;

    IngredientValueKind(String serializedName) {
        this.serializedName = serializedName;
    }

    public Component displayName() {
        return Component.translatable("viscript_recipe.editor.ingredient.kind." + serializedName);
    }
}
