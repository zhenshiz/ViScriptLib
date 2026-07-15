package com.viscript_recipe.gui.editor;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.rendering.GUIContext;
import com.simibubi.create.compat.jei.category.animations.AnimatedCrafter;

final class CreateMechanicalCrafterElement extends UIElement {
    private final AnimatedCrafter crafter = new AnimatedCrafter();

    CreateMechanicalCrafterElement() {
        setOverflowVisible(true);
    }

    @Override
    public void drawBackgroundAdditional(GUIContext guiContext) {
        super.drawBackgroundAdditional(guiContext);
        guiContext.graphics.flush();
        crafter.draw(
                guiContext.graphics,
                Math.round(getPositionX() + 17),
                Math.round(getPositionY() + 25)
        );
        guiContext.graphics.flush();
    }
}
