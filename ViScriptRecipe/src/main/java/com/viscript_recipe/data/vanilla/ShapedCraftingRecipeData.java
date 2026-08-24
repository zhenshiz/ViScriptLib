package com.viscript_recipe.data.vanilla;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.ViScriptRecipe;
import com.viscript_recipe.data.IVSRecipeData;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.recipe.vanilla.ShapedRecipePattern;
import com.viscript_recipe.recipe.vanilla.ViscriptShapedRecipe;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class ShapedCraftingRecipeData implements IVSRecipeData {
    @Persisted
    private Boolean showNotification = true;
    @Persisted
    private List<String> pattern = new ArrayList<>(List.of("A"));
    @Persisted
    private List<ShapedKeyEntry> key = new ArrayList<>(List.of(
            ShapedKeyEntry.of('A', RecipeIngredient.item(Items.OAK_PLANKS))
    ));
    @Persisted
    private List<CraftingRemainderRule> remainders = new ArrayList<>(List.of(CraftingRemainderRule.defaultRule()));
    @Persisted
    private ItemStack result = new ItemStack(Items.CRAFTING_TABLE);

    @Override
    public String getDataName() {return "shaped";}

    @Override
    public Recipe<?> compile(ResourceLocation typeId) {
        if (pattern.isEmpty()) {
            throw new IllegalArgumentException("Shaped recipe pattern cannot be empty");
        }
        var compiledKey = new LinkedHashMap<Character, net.minecraft.world.item.crafting.Ingredient>();
        for (var entry : key) {
            compiledKey.put(entry.compileSymbol(), entry.compileIngredient());
        }
        var pattern1 = ShapedRecipePattern.of(compiledKey, pattern);
        if (result.isEmpty()) {
            throw new IllegalArgumentException("Recipe result cannot be empty");
        }
        var compiledRemainders = remainders == null ? List.<CraftingRemainderRule>of() : remainders;
        if (compiledRemainders.stream().anyMatch(rule -> !rule.isDefault())) {
            return new ViscriptShapedRecipe("", CraftingBookCategory.MISC, pattern1, result.copy(), showNotification, compiledRemainders);
        }
        return new ShapedRecipe(ViScriptRecipe.placeholder, "", CraftingBookCategory.MISC, pattern1.width(), pattern1.height(), pattern1.ingredients(), result.copy(), showNotification);
    }
}
