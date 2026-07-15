/*
package com.viscript_recipe.compat.jei.irons_spellbooks;

import com.viscript_recipe.compat.irons_spellbooks.IronArcaneAnvilOverrideManager;
import io.redspace.ironsspellbooks.jei.ArcaneAnvilJeiRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Arrays;
import java.util.List;

public class ViscriptArcaneAnvilJeiRecipe extends ArcaneAnvilJeiRecipe {
    private final ResourceLocation id;
    private final List<ItemStack> inputs;
    private final List<ItemStack> materials;
    private final List<ItemStack> results;

    public ViscriptArcaneAnvilJeiRecipe(IronArcaneAnvilOverrideManager.CompiledRecipe recipe) {
        super(Items.AIR, Items.AIR);
        this.id = recipe.id();
        this.inputs = copyStacks(recipe.input().getItems());
        this.materials = copyStacks(recipe.material().getItems());
        this.results = List.of(recipe.result());
    }

    public ResourceLocation id() {
        return id;
    }

    @Override
    public Tuple<List<ItemStack>, List<ItemStack>, List<ItemStack>> getRecipeItems() {
        return new Tuple<>(copyStacks(inputs), copyStacks(materials), copyStacks(results));
    }

    private static List<ItemStack> copyStacks(ItemStack[] stacks) {
        return Arrays.stream(stacks)
                .map(ItemStack::copy)
                .toList();
    }

    private static List<ItemStack> copyStacks(List<ItemStack> stacks) {
        return stacks.stream()
                .map(ItemStack::copy)
                .toList();
    }
}
*/
