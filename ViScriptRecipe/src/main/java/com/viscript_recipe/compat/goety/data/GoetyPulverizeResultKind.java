package com.viscript_recipe.compat.goety.data;

import com.viscript_recipe.data.ITranslated;

public enum GoetyPulverizeResultKind implements ITranslated {
    ITEM,
    BLOCK;

    @Override
    public String translatePrefix() {
        return "viscript_recipe.editor.goety.pulverize.result_kind.";
    }
}
