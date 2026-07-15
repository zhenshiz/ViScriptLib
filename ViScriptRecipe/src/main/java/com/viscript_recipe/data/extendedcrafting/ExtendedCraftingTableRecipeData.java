package com.viscript_recipe.data.extendedcrafting;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.configurator.annotation.ConfigList;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.data.vanilla.ShapedKeyEntry;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class ExtendedCraftingTableRecipeData implements IPersistedSerializable, IConfigurable {
    public static final int MIN_SIZE = 1;
    public static final int MAX_SIZE = 9;

    @Configurable(name = "viscript_recipe.config.extendedcrafting.table.width")
    private int width = 3;

    @Configurable(name = "viscript_recipe.config.extendedcrafting.table.height")
    private int height = 3;

    @Configurable(name = "viscript_recipe.config.extendedcrafting.table.tier")
    private int tier = 1;

    @Configurable(name = "viscript_recipe.config.shaped.pattern")
    @ConfigList(addDefaultMethod = "createDefaultPatternRow")
    private List<String> pattern = new ArrayList<>(List.of("A"));

    @Configurable(name = "viscript_recipe.config.shaped.key")
    @ConfigList(addDefaultMethod = "createDefaultKey")
    private List<ShapedKeyEntry> key = new ArrayList<>(List.of(
            ShapedKeyEntry.of("A", RecipeIngredient.item(Items.IRON_INGOT))
    ));

    @Configurable(name = "viscript_recipe.config.shapeless.ingredients")
    @ConfigList(addDefaultMethod = "createDefaultIngredient")
    private List<RecipeIngredient> shapelessIngredients = new ArrayList<>(List.of(
            RecipeIngredient.item(Items.IRON_INGOT)
    ));

    @Configurable(name = "viscript_recipe.config.recipe.result")
    private ItemStack result = new ItemStack(Items.CRAFTING_TABLE);

    public String createDefaultPatternRow() {
        return "A";
    }

    public ShapedKeyEntry createDefaultKey() {
        return new ShapedKeyEntry();
    }

    public RecipeIngredient createDefaultIngredient() {
        return RecipeIngredient.item(Items.IRON_INGOT);
    }

    public int normalizedWidth() {
        return clampSize(width);
    }

    public int normalizedHeight() {
        return clampSize(height);
    }

    public int normalizedTier() {
        return Math.max(0, Math.min(4, tier));
    }

    public ExtendedCraftingTableRecipeData setWidth(int width) {
        this.width = clampSize(width);
        return this;
    }

    public ExtendedCraftingTableRecipeData setHeight(int height) {
        this.height = clampSize(height);
        return this;
    }

    public ExtendedCraftingTableRecipeData setTier(int tier) {
        this.tier = Math.max(0, Math.min(4, tier));
        return this;
    }

/*    public Recipe<?> compile(ResourceLocation type) {
        return ExtendedCraftingRecipeFactory.compileTable(type, this);
    }*/

    private static int clampSize(int value) {
        return Math.max(MIN_SIZE, Math.min(MAX_SIZE, value));
    }
}
