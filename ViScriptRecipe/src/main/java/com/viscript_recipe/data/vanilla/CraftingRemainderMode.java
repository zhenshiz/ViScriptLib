package com.viscript_recipe.data.vanilla;

import com.viscript_recipe.data.ITranslated;

public enum CraftingRemainderMode implements ITranslated {
    DEFAULT,
    CONSUME,
    REPLACE;

    @Override
    public String translatePrefix() {return "viscript_recipe.editor.remainder.mode.";}
}
