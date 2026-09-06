package com.viscript_recipe.compat.goety.canvas;

import com.google.common.util.concurrent.Runnables;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.viscript_recipe.compat.goety.data.GoetyPulverizeRecipeData;
import com.viscript_recipe.compat.goety.data.GoetyPulverizeResultKind;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.editor.RecipeSearchComponents;
import com.viscript_recipe.gui.views.NavigationView;
import com.viscript_recipe.gui.views.PropertiesView;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

import static com.viscript_recipe.compat.goety.canvas.BrazierCanvas.useJeiCanvas;

public class PulverizeCanvas extends RecipeCanvas<GoetyPulverizeRecipeData> {
    public PulverizeCanvas(NavigationView navigationView, RecipeEntry entry) {super(navigationView, entry);}

    @Override
    public void load() {
        var data = getData();
        loadIngredientSlot(0, data.getIngredient());
        setVisualOutput(0, data.result());
    }

    @Override
    public void save() {
        getData().setIngredient(getVisualIngredient(0)).setResult(getVisualOutput(0).getItem());
    }

    @Override
    public UIElement createCanvas() {
        var input = createIngredientSlot(0, useJeiCanvas ? JEI_SLOT_SIZE : SLOT_SIZE);
        var output = createOutputSlot(0, useJeiCanvas ? JEI_SLOT_SIZE : OUTPUT_SLOT_SIZE);
        if (useJeiCanvas) {
            configureJeiOverlaySlotVisual(input);
            configureJeiOverlaySlotVisual(output);
        }
        return GoetyCanvasFactory.createPulverizeCanvas(input, output, useJeiCanvas);
    }

    @Override
    public void buildResultProperties(UIElement content) {
        var data = getData();
        content.addChildren(sectionTitle("viscript_recipe.editor.properties.goety.pulverize"),
                selector("viscript_recipe.config.goety.pulverize.result_kind",
                        List.of(GoetyPulverizeResultKind.values()), data.getResultKind(),
                        GoetyPulverizeResultKind::displayName, data::setResultKind, RecipeCanvas::reloadProperties
                )
        );
        if (data.getResultKind() == GoetyPulverizeResultKind.BLOCK) {
            content.addChild(RecipeSearchComponents.block("viscript_recipe.config.goety.pulverize.block_result",
                    data::getBlockResult, id -> {
                        data.setBlockResult(id);
                        setVisualOutput(0, data.result());
                    }, Runnables.doNothing(), Blocks.COBBLESTONE
            ));
        } else content.addChild(PropertiesView.createItemStackConfigurator("viscript_recipe.config.recipe.result",
                () -> getSelectedOutput().getItem(), this::setSelectedOutput
        ));
    }
}
