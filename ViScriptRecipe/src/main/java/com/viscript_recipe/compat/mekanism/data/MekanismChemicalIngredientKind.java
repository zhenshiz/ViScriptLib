package com.viscript_recipe.compat.mekanism.data;

import com.viscript_recipe.data.ITranslated;

public enum MekanismChemicalIngredientKind implements ITranslated {
    CHEMICAL,
    TAG;

    @Override
    public String translatePrefix() {return "viscript_recipe.editor.mekanism.chemical_kind.";}
}
