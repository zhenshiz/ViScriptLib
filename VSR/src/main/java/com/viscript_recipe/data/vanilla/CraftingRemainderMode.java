package com.viscript_recipe.data.vanilla;

import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum CraftingRemainderMode implements StringRepresentable {
    DEFAULT("default"),
    CONSUME("consume"),
    REPLACE("replace");

    private final String serializedName;

    CraftingRemainderMode(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public @NotNull String getSerializedName() {
        return serializedName;
    }

    public Component displayName() {
        return Component.translatable("viscript_recipe.editor.remainder.mode." + serializedName);
    }
}
