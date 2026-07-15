package com.viscript_recipe.data.goety;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * Identifies Goety's built-in ritual environment categories.
 */
public enum GoetyRitualCraftType implements StringRepresentable {
    ANIMATION("animation"),
    NECROTURGY("necroturgy"),
    FORGE("forge"),
    MAGIC("magic"),
    ADEPT_NETHER("adept_nether"),
    EXPERT_NETHER("expert_nether"),
    SABBATH("sabbath"),
    END("end"),
    SKY("sky"),
    STORM("storm"),
    GEOTURGY("geoturgy"),
    FROST("frost"),
    DEEP("deep"),
    OVERGROWN("overgrown"),
    DIVINATION("divination");

    private final String serializedName;

    GoetyRitualCraftType(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public @NotNull String getSerializedName() {
        return serializedName;
    }

    /**
     * Resolves a stored Goety craft type, defaulting to magic for unknown values.
     *
     * @param  name the serialized craft type name
     * @return the matching built-in craft type
     */
    public static GoetyRitualCraftType byName(String name) {
        return Arrays.stream(values())
                .filter(value -> value.serializedName.equals(name))
                .findFirst()
                .orElse(MAGIC);
    }
}
