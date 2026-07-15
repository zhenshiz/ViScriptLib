package com.viscript_recipe.data.vanilla;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.recipe.vanilla.ViscriptSmithingTransformRecipe;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;

@Getter
@Setter
@Accessors(chain = true)
public class SmithingTransformRecipeData implements IPersistedSerializable, IConfigurable {
    @Configurable(name = "viscript_recipe.config.recipe.show_notification")
    private boolean showNotification = true;

    @Configurable(name = "viscript_recipe.config.smithing_transform.template", subConfigurable = true)
    private RecipeIngredient template = RecipeIngredient.item(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE);

    @Configurable(name = "viscript_recipe.config.smithing_transform.base", subConfigurable = true)
    private RecipeIngredient base = RecipeIngredient.item(Items.DIAMOND_SWORD);

    @Configurable(name = "viscript_recipe.config.smithing_transform.addition", subConfigurable = true)
    private RecipeIngredient addition = RecipeIngredient.item(Items.NETHERITE_INGOT);

    @Configurable(name = "viscript_recipe.config.recipe.result")
    private ItemStack result = new ItemStack(Items.NETHERITE_SWORD);

    public Recipe<?> compile() {
        var compiledTemplate = template == null ? net.minecraft.world.item.crafting.Ingredient.EMPTY : template.compile();
        var compiledBase = base == null ? net.minecraft.world.item.crafting.Ingredient.EMPTY : base.compile();
        var compiledAddition = addition == null ? net.minecraft.world.item.crafting.Ingredient.EMPTY : addition.compile();
        if (compiledTemplate.isEmpty()) {
            throw new IllegalArgumentException("Smithing transform template cannot be empty");
        }
        if (compiledBase.isEmpty()) {
            throw new IllegalArgumentException("Smithing transform base cannot be empty");
        }
        if (compiledAddition.isEmpty()) {
            throw new IllegalArgumentException("Smithing transform addition cannot be empty");
        }
        if (result.isEmpty()) {
            throw new IllegalArgumentException("Recipe result cannot be empty");
        }
        return new ViscriptSmithingTransformRecipe(compiledTemplate, compiledBase, compiledAddition, result.copy(), showNotification);
    }
}
