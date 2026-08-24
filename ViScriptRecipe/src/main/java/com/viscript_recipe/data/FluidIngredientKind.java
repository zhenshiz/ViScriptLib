package com.viscript_recipe.data;

public enum FluidIngredientKind implements ITranslated {
    FLUID,
    TAG;

    @Override
    public String translatePrefix() {return "viscript_recipe.editor.fluid_ingredient.kind.";}
}
