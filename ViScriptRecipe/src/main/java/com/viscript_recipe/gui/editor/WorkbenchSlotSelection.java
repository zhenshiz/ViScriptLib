package com.viscript_recipe.gui.editor;

public record WorkbenchSlotSelection(Kind kind, int index) {
    public static final WorkbenchSlotSelection RECIPE = new WorkbenchSlotSelection(Kind.RECIPE, -1);
    public static final WorkbenchSlotSelection RESULT = new WorkbenchSlotSelection(Kind.RESULT, -1);
    public static final WorkbenchSlotSelection CONTAINER = new WorkbenchSlotSelection(Kind.CONTAINER, -1);

    public static WorkbenchSlotSelection ingredient(int index) {
        return new WorkbenchSlotSelection(Kind.INGREDIENT, index);
    }

    public static WorkbenchSlotSelection fluid(int index) {
        return new WorkbenchSlotSelection(Kind.FLUID, index);
    }

    public static WorkbenchSlotSelection cuttingResult(int index) {
        return new WorkbenchSlotSelection(Kind.CUTTING_RESULT, index);
    }

    public static WorkbenchSlotSelection createResult(int index) {
        return new WorkbenchSlotSelection(Kind.CREATE_RESULT, index);
    }

    public static WorkbenchSlotSelection createTransitional() {
        return new WorkbenchSlotSelection(Kind.CREATE_TRANSITIONAL, -1);
    }

    public static WorkbenchSlotSelection createSequencedStep(int index) {
        return new WorkbenchSlotSelection(Kind.CREATE_SEQUENCED_STEP, index);
    }

    public static WorkbenchSlotSelection arsNouveauOutput(int index) {
        return new WorkbenchSlotSelection(Kind.ARS_NOUVEAU_OUTPUT, index);
    }

    public enum Kind {
        RECIPE,
        INGREDIENT,
        FLUID,
        RESULT,
        CONTAINER,
        CUTTING_RESULT,
        CREATE_RESULT,
        CREATE_TRANSITIONAL,
        CREATE_SEQUENCED_STEP,
        ARS_NOUVEAU_OUTPUT
    }
}
