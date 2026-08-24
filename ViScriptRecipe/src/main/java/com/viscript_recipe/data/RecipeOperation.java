package com.viscript_recipe.data;

public enum RecipeOperation implements ITranslated {
    ADD,
    REPLACE,
    REMOVE;

    @Override
    public String translatePrefix() {return "viscript_recipe.editor.operation.";}
}
