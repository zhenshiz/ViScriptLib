package com.viscript_recipe.compat.goety.data;

import com.viscript_recipe.data.ITranslated;

import java.util.Arrays;

public enum GoetyRitualCraftType implements ITranslated {
    ANIMATION,
    NECROTURGY,
    FORGE,
    MAGIC,
    ADEPT_NETHER,
    EXPERT_NETHER,
    SABBATH,
    END,
    SKY,
    STORM,
    GEOTURGY,
    FROST,
    DEEP,
    OVERGROWN,
    DIVINATION;

    public static GoetyRitualCraftType byName(String name) {
        return Arrays.stream(values())
                .filter(value -> value.getSerializedName().equals(name))
                .findFirst()
                .orElse(MAGIC);
    }

    @Override
    public String translatePrefix() {return "viscript_recipe.editor.goety.ritual.craft_type.";}
}
