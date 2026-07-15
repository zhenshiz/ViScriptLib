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
public class ExtendedCraftingEnderCrafterRecipeData implements IPersistedSerializable, IConfigurable {
    @Configurable(name = "viscript_recipe.config.shaped.pattern")
    @ConfigList(addDefaultMethod = "createDefaultPatternRow")
    private List<String> pattern = new ArrayList<>(List.of("A"));

    @Configurable(name = "viscript_recipe.config.shaped.key")
    @ConfigList(addDefaultMethod = "createDefaultKey")
    private List<ShapedKeyEntry> key = new ArrayList<>(List.of(
            ShapedKeyEntry.of("A", RecipeIngredient.item(Items.ENDER_EYE))
    ));

    @Configurable(name = "viscript_recipe.config.shapeless.ingredients")
    @ConfigList(addDefaultMethod = "createDefaultIngredient")
    private List<RecipeIngredient> shapelessIngredients = new ArrayList<>(List.of(
            RecipeIngredient.item(Items.ENDER_EYE)
    ));

    @Configurable(name = "viscript_recipe.config.recipe.result")
    private ItemStack result = new ItemStack(Items.ENDER_EYE);

    @Configurable(name = "viscript_recipe.config.extendedcrafting.ender_crafter.crafting_time")
    private int craftingTime = 200;

    public String createDefaultPatternRow() {
        return "A";
    }

    public ShapedKeyEntry createDefaultKey() {
        return new ShapedKeyEntry();
    }

    public RecipeIngredient createDefaultIngredient() {
        return RecipeIngredient.item(Items.ENDER_EYE);
    }

    /*public Recipe<?> compile(ResourceLocation type) {
        return ExtendedCraftingRecipeFactory.compileEnderCrafter(type, this);
    }*/
}
