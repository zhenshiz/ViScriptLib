package com.viscript_recipe.recipe.importer;

import net.minecraft.network.chat.Component;

public class RecipeImportException extends Exception {
    private final Component component;

    public RecipeImportException(String key, Object... args) {
        this(Component.translatable(key, args));
    }

    public RecipeImportException(Component component) {
        super(component.getString());
        this.component = component;
    }

    public Component component() {
        return component;
    }
}
