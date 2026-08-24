package com.viscript_recipe.compat.create.data;

import com.viscript_recipe.data.ITranslated;

public enum CreateSequencedAssemblyStepKind implements ITranslated {
    DEPLOYING("create:deployer"),
    PRESSING("create:mechanical_press"),
    CUTTING("create:mechanical_saw"),
    FILLING("create:spout");

    private final String machineItemId;

    CreateSequencedAssemblyStepKind(String machineItemId) {
        this.machineItemId = machineItemId;
    }

    @Override
    public String translatePrefix() {return "viscript_recipe.editor.create.sequenced_assembly.step.kind.";}

    public String machineItemId() {
        return machineItemId;
    }
}
