package com.viscript_recipe.compat.goety.canvas;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.viscript_recipe.compat.goety.data.GoetyCursedInfuserRecipeData;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.views.NavigationView;
import com.viscript_recipe.recipe.RecipeHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

import static com.viscript_recipe.compat.goety.canvas.BrazierCanvas.useJeiCanvas;

public class CursedInfuserCanvas extends RecipeCanvas<GoetyCursedInfuserRecipeData> {
    static final UIElement machine = new UIElement();
    static final Label timeLabel = emptyLabel();
    static {
        tooltip(machine, "viscript_recipe.editor.goety.cursed_infuser.machine");
        BrazierCanvas.centerLabel(timeLabel);
    }

    public CursedInfuserCanvas(NavigationView navigationView, RecipeEntry entry) {super(navigationView, entry);}

    @Override
    public void load() {
        var data = getData();
        loadIngredientSlot(0, data.getIngredient());
        setVisualOutput(0, data.getResult());
        updatePreview();
    }

    @Override
    public void save() {
        getData().setIngredient(getVisualIngredient(0)).setResult(getVisualOutput(0).getItem());
    }

    @Override
    public UIElement createCanvas() {
        var input = createIngredientSlot(0, useJeiCanvas ? JEI_SLOT_SIZE : SLOT_SIZE);
        var output = createOutputSlot(0, useJeiCanvas ? JEI_SLOT_SIZE : OUTPUT_SLOT_SIZE);
        return GoetyCanvasFactory.createCursedInfuserCanvas(input, output, machine, timeLabel, useJeiCanvas);
    }

    @Override
    public void buildRecipeProperties(UIElement content) {
        var data = getData();
        content.addChildren(sectionTitle("viscript_recipe.editor.properties.goety.cursed_infuser"),
                intField("viscript_recipe.config.goety.cursed_infuser.cooking_time",
                        data.getCookingTime(), 1, Integer.MAX_VALUE, data::setCookingTime, this::updatePreview),
                switchField("viscript_recipe.config.goety.cursed_infuser.grim", data.isGrim(), data::setGrim, this::updatePreview)
        );
    }

    private void updatePreview() {
        var data = getData();
        setTexture(machine, RecipeHelper.registryItem(
                data.isGrim() ? "goety:grim_infuser" : "goety:cursed_infuser", Items.FURNACE));
        timeLabel.setText(Component.translatable("viscript_recipe.editor.goety.time_seconds", data.getCookingTime() / 20));
    }
}
