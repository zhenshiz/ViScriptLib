package com.viscript_recipe.compat.touhou_little_maid;

import com.github.tartaricacid.touhoulittlemaid.crafting.AltarRecipe;
import com.lowdragmc.lowdraglib2.utils.LDLibExtraCodecs;
import com.lowdragmc.lowdraglib2.utils.TagBuilder;
import com.viscript_recipe.ViScriptRecipe;
import com.viscript_recipe.compat.touhou_little_maid.data.TouhouLittleMaidAltarRecipeData;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

/**
 * Creates Touhou Little Maid's native altar recipes from editor data.
 */
public final class TouhouLittleMaidRecipeFactory {
    private TouhouLittleMaidRecipeFactory() {
    }

    public static Recipe<?> compileAltar(TouhouLittleMaidAltarRecipeData data) {
        var ingredients = NonNullList.<Ingredient>create();
        for (var ingredientData : data.getIngredients()) {
            var ingredient = ingredientData == null ? Ingredient.EMPTY : ingredientData.compile();
            if (!ingredient.isEmpty()) {
                ingredients.add(ingredient);
            }
        }
        if (ingredients.isEmpty()) {
            throw new IllegalArgumentException("Touhou Little Maid altar recipe must contain at least one ingredient");
        }
        if (ingredients.size() > TouhouLittleMaidAltarRecipeData.INPUT_COUNT) {
            throw new IllegalArgumentException("Touhou Little Maid altar recipe accepts at most six ingredients");
        }
        var result = requireResult(data.getResult());
        var entityType = data.getEntityType();
        if (entityType == null || !BuiltInRegistries.ENTITY_TYPE.containsKey(entityType)) {
            throw new IllegalArgumentException("Unknown altar output entity type: " + entityType);
        }
        if (!data.isItemCraft()) result = data.getExtraData();
        var power = data.getPower();
        if (!Float.isFinite(power) || power < 0) {
            throw new IllegalArgumentException("Altar power cost must be a finite non-negative number");
        }
        return new AltarRecipe(
                ViScriptRecipe.placeholder,
                BuiltInRegistries.ENTITY_TYPE.get(entityType),
                result,
                power,
                ingredients.toArray(Ingredient[]::new)
        );
    }

    private static CompoundTag requireResult(ItemStack stack) {
        var tag = LDLibExtraCodecs.getOrThrow(ItemStack.CODEC.encodeStart(NbtOps.INSTANCE, stack));
        return TagBuilder.compound().add("Item", tag).build();
    }
}
