package com.viscript_recipe.gui.editor;

public record SlotSelection(Kind kind, int index) {
    public static final SlotSelection RECIPE = new SlotSelection(Kind.RECIPE, -1);
    public static final SlotSelection EXTRA_ITEM = new SlotSelection(Kind.EXTRA_ITEM, 0);

    public static SlotSelection ingredient(int index) {
        return new SlotSelection(Kind.INGREDIENT, index);
    }

    public static SlotSelection result(int index) {
        return new SlotSelection(Kind.RESULT, index);
    }

    public static SlotSelection fluid(int index) {
        return new SlotSelection(Kind.FLUID, index);
    }

    public static SlotSelection createSequencedStep(int index) {
        return new SlotSelection(Kind.CREATE_SEQUENCED_STEP, index);
    }

    public enum Kind {
        RECIPE,
        INGREDIENT,
        FLUID,
        RESULT,
        EXTRA_ITEM,
        CREATE_SEQUENCED_STEP
    }
}
