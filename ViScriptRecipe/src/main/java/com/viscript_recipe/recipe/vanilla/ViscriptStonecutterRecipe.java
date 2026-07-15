package com.viscript_recipe.recipe.vanilla;

import com.viscript_recipe.ViScriptRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.StonecutterRecipe;

public class ViscriptStonecutterRecipe extends StonecutterRecipe {
    private final boolean showNotification;

    public ViscriptStonecutterRecipe(String group, Ingredient ingredient, ItemStack result, boolean showNotification) {
        super(ViScriptRecipe.placeholder, group, ingredient, result);
        this.showNotification = showNotification;
    }

    @Override
    public boolean showNotification() {
        return showNotification;
    }
}
