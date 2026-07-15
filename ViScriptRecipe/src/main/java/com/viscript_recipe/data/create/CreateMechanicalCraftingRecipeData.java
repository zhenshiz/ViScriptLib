package com.viscript_recipe.data.create;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.configurator.annotation.ConfigList;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.simibubi.create.content.kinetics.crafter.MechanicalCraftingRecipe;
import com.viscript_recipe.ViScriptRecipe;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.data.vanilla.ShapedKeyEntry;
import com.viscript_recipe.recipe.vanilla.ShapedRecipePattern;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class CreateMechanicalCraftingRecipeData implements IPersistedSerializable, IConfigurable {
    public static final int MIN_SIZE = 1;
    public static final int MAX_SIZE = 9;

    @Configurable(name = "viscript_recipe.config.create.mechanical_crafting.width")
    private int width = 3;

    @Configurable(name = "viscript_recipe.config.create.mechanical_crafting.height")
    private int height = 3;

    @Configurable(name = "viscript_recipe.config.create.mechanical_crafting.accept_mirrored")
    private boolean acceptMirrored = true;

    @Configurable(name = "viscript_recipe.config.shaped.pattern")
    @ConfigList(addDefaultMethod = "createDefaultPatternRow")
    private List<String> pattern = new ArrayList<>(List.of("A"));

    @Configurable(name = "viscript_recipe.config.shaped.key")
    @ConfigList(addDefaultMethod = "createDefaultKey")
    private List<ShapedKeyEntry> key = new ArrayList<>(List.of(
            ShapedKeyEntry.of("A", RecipeIngredient.item(Items.IRON_INGOT))
    ));

    @Configurable(name = "viscript_recipe.config.recipe.result")
    private ItemStack result = new ItemStack(Items.CRAFTING_TABLE);

    public String createDefaultPatternRow() {
        return "A";
    }

    public ShapedKeyEntry createDefaultKey() {
        return new ShapedKeyEntry();
    }

    public int normalizedWidth() {
        return clampSize(width);
    }

    public int normalizedHeight() {
        return clampSize(height);
    }

    public CreateMechanicalCraftingRecipeData setWidth(int width) {
        this.width = clampSize(width);
        return this;
    }

    public CreateMechanicalCraftingRecipeData setHeight(int height) {
        this.height = clampSize(height);
        return this;
    }

    public Recipe<?> compile() {
        var normalizedPattern = normalizedPattern();
        if (normalizedPattern.stream().allMatch(String::isBlank)) {
            throw new IllegalArgumentException("Mechanical crafting recipe pattern cannot be empty");
        }
        var compiledKey = new LinkedHashMap<Character, net.minecraft.world.item.crafting.Ingredient>();
        for (var entry : key) {
            compiledKey.put(entry.compileSymbol(), entry.compileIngredient());
        }
        if (result == null || result.isEmpty()) {
            throw new IllegalArgumentException("Mechanical crafting recipe result cannot be empty");
        }
        ShapedRecipePattern pattern1 = ShapedRecipePattern.of(compiledKey, normalizedPattern);
        return new MechanicalCraftingRecipe(
                ViScriptRecipe.placeholder, "",
                pattern1.width(), pattern1.height(), pattern1.ingredients(),
                result.copy(),
                acceptMirrored
        );
    }

    private List<String> normalizedPattern() {
        var normalized = new ArrayList<String>();
        var normalizedWidth = normalizedWidth();
        var normalizedHeight = normalizedHeight();
        for (int row = 0; row < normalizedHeight; row++) {
            var line = pattern != null && row < pattern.size() && pattern.get(row) != null ? pattern.get(row) : "";
            if (line.length() > normalizedWidth) {
                line = line.substring(0, normalizedWidth);
            }
            normalized.add(line + " ".repeat(normalizedWidth - line.length()));
        }
        return normalized;
    }

    private static int clampSize(int value) {
        return Math.max(MIN_SIZE, Math.min(MAX_SIZE, value));
    }
}
