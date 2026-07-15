package com.viscript_recipe.data.avaritia;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.configurator.annotation.ConfigList;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.viscript_recipe.data.RecipeIngredient;
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
public class AvaritiaExtremeSmithingRecipeData implements IPersistedSerializable, IConfigurable {
    @Configurable(name = "viscript_recipe.config.smithing_transform.template", subConfigurable = true)
    private RecipeIngredient template = RecipeIngredient.item(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE);

    @Configurable(name = "viscript_recipe.config.smithing_transform.base", subConfigurable = true)
    private RecipeIngredient base = RecipeIngredient.item(Items.DIAMOND_SWORD);

    @Configurable(name = "viscript_recipe.config.avaritia.extreme_smithing.additions")
    @ConfigList(addDefaultMethod = "createDefaultAddition")
    private List<RecipeIngredient> additions = new ArrayList<>(List.of(
            RecipeIngredient.item(Items.NETHERITE_INGOT),
            RecipeIngredient.item(Items.NETHERITE_INGOT),
            RecipeIngredient.item(Items.NETHERITE_INGOT)
    ));

    @Configurable(name = "viscript_recipe.config.recipe.result")
    private ItemStack result = new ItemStack(Items.NETHERITE_SWORD);

    public RecipeIngredient createDefaultAddition() {
        return RecipeIngredient.item(Items.NETHERITE_INGOT);
    }

    public RecipeIngredient addition(int index) {
        if (additions == null || index < 0 || index >= additions.size()) {
            return new RecipeIngredient();
        }
        var addition = additions.get(index);
        return addition == null ? new RecipeIngredient() : addition;
    }

    public AvaritiaExtremeSmithingRecipeData setAddition(int index, RecipeIngredient ingredient) {
        ensureAdditionSize();
        additions.set(Math.max(0, Math.min(2, index)), ingredient == null ? new RecipeIngredient() : ingredient);
        return this;
    }

    public List<RecipeIngredient> normalizedAdditions() {
        ensureAdditionSize();
        return additions.stream().limit(3).toList();
    }

    private void ensureAdditionSize() {
        if (additions == null) {
            additions = new ArrayList<>();
        }
        while (additions.size() < 3) {
            additions.add(createDefaultAddition());
        }
    }

    /*public Recipe<?> compile() {
        return AvaritiaRecipeFactory.compileExtremeSmithing(this);
    }*/
}
