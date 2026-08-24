package com.viscript_recipe.data;

public enum IngredientValueKind implements ITranslated {
    ITEM,
    TAG,
    ITEM_ABILITY;

    @Override
    public String translatePrefix() {return "viscript_recipe.editor.ingredient.kind.";}
}
