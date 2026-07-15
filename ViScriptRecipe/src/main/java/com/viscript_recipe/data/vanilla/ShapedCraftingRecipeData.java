package com.viscript_recipe.data.vanilla;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.configurator.annotation.ConfigList;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.viscript_recipe.ViScriptRecipe;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.recipe.vanilla.ShapedRecipePattern;
import com.viscript_recipe.recipe.vanilla.ViscriptShapedRecipe;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
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
public class ShapedCraftingRecipeData implements IPersistedSerializable, IConfigurable {
    @Configurable(name = "viscript_recipe.config.recipe.show_notification")
    private boolean showNotification = true;

    @Configurable(name = "viscript_recipe.config.shaped.pattern")
    @ConfigList(addDefaultMethod = "createDefaultPatternRow")
    private List<String> pattern = new ArrayList<>(List.of("A"));

    @Configurable(name = "viscript_recipe.config.shaped.key")
    @ConfigList(addDefaultMethod = "createDefaultKey")
    private List<ShapedKeyEntry> key = new ArrayList<>(List.of(
            ShapedKeyEntry.of("A", RecipeIngredient.item(Items.OAK_PLANKS))
    ));

    @Configurable(name = "viscript_recipe.config.shaped.remainders")
    @ConfigList(addDefaultMethod = "createDefaultRemainder")
    private List<CraftingRemainderRule> remainders = new ArrayList<>(List.of(CraftingRemainderRule.defaultRule()));

    @Configurable(name = "viscript_recipe.config.recipe.result")
    private ItemStack result = new ItemStack(Items.CRAFTING_TABLE);

    public String createDefaultPatternRow() {
        return "A";
    }

    public ShapedKeyEntry createDefaultKey() {
        return new ShapedKeyEntry();
    }

    public CraftingRemainderRule createDefaultRemainder() {
        return CraftingRemainderRule.defaultRule();
    }

    public Recipe<?> compile() {
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
