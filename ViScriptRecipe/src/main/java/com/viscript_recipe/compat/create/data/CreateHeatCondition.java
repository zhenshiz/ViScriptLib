package com.viscript_recipe.compat.create.data;

import com.viscript_recipe.data.ITranslated;

public enum CreateHeatCondition implements ITranslated {
    NONE,
    HEATED,
    SUPERHEATED;

    @Override
    public String translatePrefix() {return "viscript_recipe.editor.create.heat.";}
}
