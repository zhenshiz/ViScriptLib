package com.viscript_recipe.compat.goety.data;

import com.viscript_recipe.data.ITranslated;

public enum GoetyBrewingEntityKind implements ITranslated {
    NONE,
    TAG,
    ENTITY;

    @Override
    public String translatePrefix() {return "viscript_recipe.editor.goety.brewing.entity_kind.";}
}
