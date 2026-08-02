package com.viscript_recipe.gui.canvas.farmersdelight;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Switch;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.data.RecipeOutputData;
import com.viscript_recipe.data.farmersdelight.FarmerCuttingRecipeData;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.editor.IngredientDisplaySlot;
import com.viscript_recipe.gui.editor.RecipeEditorUi;
import com.viscript_recipe.gui.editor.RecipeGridFactory;
import com.viscript_recipe.gui.views.NavigationView;
import com.viscript_recipe.gui.views.PropertiesView;

import java.util.ArrayList;

public class FarmerCuttingCanvas extends RecipeCanvas<FarmerCuttingRecipeData> {
    static final boolean useJeiCanvas = FarmersDelightCanvasFactory.hasJeiCuttingBoardSkin();
    static final IngredientDisplaySlot[] ingredientSlots = new IngredientDisplaySlot[2];
    static final ItemSlot[] resultSlots = new ItemSlot[4];

    public FarmerCuttingCanvas(NavigationView navigationView, RecipeEntry entry) {super(navigationView, entry);}

    @Override
    public void load() {
        var data = getData();
        loadIngredientSlot(0, data.getInput());
        loadIngredientSlot(1, data.getTool());
        var results = data.getResults();
        for (int i = 0; i < Math.min(4, results.size()); i++) {
            setVisualOutput(i, results.get(i));
        }
    }

    @Override
    public void save() {
        var data = getData();
        data.setInput(getVisualIngredient(0));
        data.setTool(getVisualIngredient(1));
        var results = new ArrayList<RecipeOutputData>();
        for (int i = 0; i < 4; i++) {
            var output = getVisualOutput(i);
            if (!output.isEmpty()) results.add(output);
        }
        data.setResults(results);
    }

    @Override
    public void buildRecipeProperties(UIElement content) {
        var data = getData();
        content.addChildren(
                RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.farmersdelight.cutting_board"),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.farmersdelight.cutting.custom_sound",
                        new Switch().setOn(data.isCustomSound(), false)
                                .setOnSwitchChanged(value -> {
                                    data.setCustomSound(value); navigationView.refreshPropertiesView();
                                }))
        );
        if (data.isCustomSound()) {
            content.addChild(RecipeEditorUi.fieldGroup("viscript_recipe.config.farmersdelight.cutting.sound",
                    RecipeEditorUi.resourceLocationField(data.getSound(), data::setSound)));
        }
    }

    @Override
    public void buildResultProperties(UIElement content) {
        content.addChildren(
            RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.farmersdelight.cutting_result"),
                PropertiesView.createItemStackConfigurator(
                    "viscript_recipe.config.farmersdelight.cutting.result_item",
                        () -> getSelectedOutput().getItem(), this::setSelectedOutput),
            RecipeEditorUi.fieldGroup("viscript_recipe.config.farmersdelight.cutting.chance",
                    RecipeEditorUi.floatField(getSelectedOutput().getChance(), 0, 1, this::setSelectedOutput)));
    }

    @Override
    public UIElement createCanvas() {
        if (useJeiCanvas) {
            for (int index = 0; index < 2; index++) {
                var slot = createIngredientSlot(index, JEI_SLOT_SIZE);
                configureJeiOverlaySlotVisual(slot);
                ingredientSlots[index] = slot;
            }
            for (int index = 0; index < 4; index++) {
                var slot = createOutputSlot(index, JEI_SLOT_SIZE);
                configureJeiOverlaySlotVisual(slot);
                resultSlots[index] = slot;
            }
            return FarmersDelightCanvasFactory.createJeiCuttingBoardCanvas(
                    ingredientSlots[0], ingredientSlots[1], resultSlots, new UIElement[4]
            );
        }
        return FarmersDelightCanvasFactory.createCuttingBoardCanvas(
                createFarmerCuttingInput("viscript_recipe.editor.farmersdelight.cutting.input", 0),
                createFarmerCuttingInput("viscript_recipe.editor.farmersdelight.cutting.tool", 1),
                createFarmerCuttingResultGrid()
        );
    }

    private UIElement createFarmerCuttingInput(String labelKey, int index) {
        var slot = createIngredientSlot(index, SLOT_SIZE);
        ingredientSlots[index] = slot;
        return FarmersDelightCanvasFactory.createCuttingInput(labelKey, slot);
    }

    private UIElement createFarmerCuttingResultGrid() {
        return RecipeGridFactory.borderedGrid(2, 2, SLOT_SIZE, (index, row, col) -> {
            var slot = createOutputSlot(index, SLOT_SIZE);
            resultSlots[index] = slot;
            return slot;
        });
    }
}
