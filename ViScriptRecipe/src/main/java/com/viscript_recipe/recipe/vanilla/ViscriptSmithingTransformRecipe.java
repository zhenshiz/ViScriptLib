package com.viscript_recipe.recipe.vanilla;

import com.viscript_recipe.ViScriptRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;

public class ViscriptSmithingTransformRecipe extends SmithingTransformRecipe {
    private final boolean showNotification;

    public ViscriptSmithingTransformRecipe(Ingredient template, Ingredient base, Ingredient addition, ItemStack result, boolean showNotification) {
        super(ViScriptRecipe.placeholder, template, base, addition, result);
        this.showNotification = showNotification;
    }

    @Override
    public boolean showNotification() {
        return showNotification;
    }
}
