package com.viscript_recipe.data.create;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum CreateSequencedAssemblyStepKind implements StringRepresentable {
    DEPLOYING("deploying", "create:deployer"),
    PRESSING("pressing", "create:mechanical_press"),
    CUTTING("cutting", "create:mechanical_saw"),
    FILLING("filling", "create:spout");

    private final String serializedName;
    private final String machineItemId;

    CreateSequencedAssemblyStepKind(String serializedName, String machineItemId) {
        this.serializedName = serializedName;
        this.machineItemId = machineItemId;
    }

    @Override
    public @NotNull String getSerializedName() {
        return serializedName;
    }

    public String machineItemId() {
        return machineItemId;
    }
}
