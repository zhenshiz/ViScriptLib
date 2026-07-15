package com.viscript_recipe.recipe.vanilla;

import com.viscript_recipe.ViScriptRecipe;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapelessRecipe;

public class ViscriptShapelessRecipe extends ShapelessRecipe {
    private final boolean showNotification;

    public ViscriptShapelessRecipe(String group, CraftingBookCategory category, ItemStack result, NonNullList<Ingredient> ingredients, boolean showNotification) {
        super(ViScriptRecipe.placeholder, group, category, result, ingredients);
        this.showNotification = showNotification;
    }

    @Override
    public boolean showNotification() {
        return showNotification;
    }
}
