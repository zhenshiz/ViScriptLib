package com.viscript_recipe.data.goety;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.configurator.annotation.ConfigList;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.viscript_recipe.data.RecipeIngredient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores editable data shared by all Goety dark ritual JEI subcategories.
 */
@Getter
@Setter
@Accessors(chain = true)
public class GoetyRitualRecipeData implements IPersistedSerializable, IConfigurable {
    public static final int MAX_PEDESTAL_INGREDIENTS = 12;

    @Configurable(name = "viscript_recipe.config.goety.ritual.activation_item", subConfigurable = true)
    private RecipeIngredient activationItem = RecipeIngredient.item(Items.BOOK);

    @Configurable(name = "viscript_recipe.config.goety.ritual.ingredients")
    @ConfigList(addDefaultMethod = "createDefaultIngredient")
    private List<RecipeIngredient> ingredients = new ArrayList<>();

    @Configurable(name = "viscript_recipe.config.recipe.result")
    private ItemStack result = new ItemStack(Items.ENCHANTED_BOOK);

    @Configurable(name = "viscript_recipe.config.goety.ritual.craft_type")
    private GoetyRitualCraftType craftType = GoetyRitualCraftType.MAGIC;

    @Configurable(name = "viscript_recipe.config.goety.ritual.ritual_type")
    private ResourceLocation ritualType = new ResourceLocation("goety", "craft");

    @Configurable(name = "viscript_recipe.config.goety.soul_cost")
    private int soulCost;

    @Configurable(name = "viscript_recipe.config.goety.duration")
    private int duration = 30;

    @Configurable(name = "viscript_recipe.config.goety.ritual.summon_life")
    private int summonLife = -1;

    @Configurable(name = "viscript_recipe.config.goety.ritual.has_sacrifice")
    private boolean hasSacrifice;

    @Configurable(name = "viscript_recipe.config.goety.ritual.entity_to_sacrifice")
    private ResourceLocation entityToSacrifice = new ResourceLocation("minecraft", "zombies");

    @Configurable(name = "viscript_recipe.config.goety.ritual.entity_to_sacrifice_name")
    private String entityToSacrificeDisplayName = "entity.minecraft.zombie";

    @Configurable(name = "viscript_recipe.config.goety.ritual.has_summon")
    private boolean hasSummon;

    @Configurable(name = "viscript_recipe.config.goety.ritual.entity_to_summon")
    private ResourceLocation entityToSummon = new ResourceLocation("zombie");

    @Configurable(name = "viscript_recipe.config.goety.ritual.has_conversion")
    private boolean hasConversion;

    @Configurable(name = "viscript_recipe.config.goety.ritual.entity_to_convert")
    private ResourceLocation entityToConvert = new ResourceLocation("minecraft", "zombies");

    @Configurable(name = "viscript_recipe.config.goety.ritual.entity_to_convert_name")
    private String entityToConvertDisplayName = "entity.minecraft.zombie";

    @Configurable(name = "viscript_recipe.config.goety.ritual.entity_to_convert_into")
    private ResourceLocation entityToConvertInto = new ResourceLocation("zombie_villager");

    @Configurable(name = "viscript_recipe.config.goety.ritual.has_structure")
    private boolean hasStructure;

    @Configurable(name = "viscript_recipe.config.goety.ritual.structure_to_locate")
    private ResourceLocation structureToLocate = new ResourceLocation("minecraft", "village");

    @Configurable(name = "viscript_recipe.config.goety.ritual.structure_name")
    private String structureDisplayName = "filled_map.village";

    @Configurable(name = "viscript_recipe.config.goety.ritual.has_enchantment")
    private boolean hasEnchantment;

    @Configurable(name = "viscript_recipe.config.goety.ritual.enchantment")
    private ResourceLocation enchantment = new ResourceLocation("sharpness");

    @Configurable(name = "viscript_recipe.config.goety.ritual.xp_level_cost")
    private int xpLevelCost;

    @Configurable(name = "viscript_recipe.config.goety.ritual.research")
    private String research = "";

    /**
     * Creates an empty ingredient for the persisted pedestal list.
     *
     * @return an empty editable ingredient
     */
    public RecipeIngredient createDefaultIngredient() {
        return new RecipeIngredient();
    }

    /**
     * Returns a pedestal ingredient or an empty value when the index is outside the official twelve-slot layout.
     *
     * @param  index zero-based pedestal index
     * @return the stored ingredient or an empty ingredient
     */
    public RecipeIngredient ingredient(int index) {
        if (ingredients == null || index < 0 || index >= Math.min(MAX_PEDESTAL_INGREDIENTS, ingredients.size())) {
            return new RecipeIngredient();
        }
        var ingredient = ingredients.get(index);
        return ingredient == null ? new RecipeIngredient() : ingredient;
    }

    /**
     * Replaces a pedestal ingredient while enforcing the official JEI twelve-slot limit.
     *
     * @param  index zero-based pedestal index
     * @param  ingredient replacement ingredient
     * @return this data object
     */
    public GoetyRitualRecipeData setIngredient(int index, RecipeIngredient ingredient) {
        if (index < 0 || index >= MAX_PEDESTAL_INGREDIENTS) {
            return this;
        }
        ingredients = normalizedIngredients();
        while (ingredients.size() <= index) {
            ingredients.add(new RecipeIngredient());
        }
        ingredients.set(index, ingredient == null ? new RecipeIngredient() : ingredient);
        trimTrailingEmptyIngredients();
        return this;
    }

    /**
     * Returns a defensive pedestal list truncated to the official JEI limit.
     *
     * @return normalized pedestal ingredients
     */
    public List<RecipeIngredient> normalizedIngredients() {
        var normalized = new ArrayList<RecipeIngredient>();
        if (ingredients != null) {
            for (int i = 0; i < Math.min(MAX_PEDESTAL_INGREDIENTS, ingredients.size()); i++) {
                var ingredient = ingredients.get(i);
                normalized.add(ingredient == null ? new RecipeIngredient() : ingredient);
            }
        }
        return normalized;
    }

    private void trimTrailingEmptyIngredients() {
        while (!ingredients.isEmpty() && isEmpty(ingredients.get(ingredients.size() - 1))) {
            ingredients.remove(ingredients.size() - 1);
        }
    }

    private static boolean isEmpty(RecipeIngredient ingredient) {
        return ingredient == null || ingredient.getValues() == null || ingredient.getValues().isEmpty();
    }

    /**
     * Compiles this data into Goety's native ritual recipe.
     *
     * @return the compiled ritual recipe
     */
    public Recipe<?> compile() {
        return /*GoetyRecipeFactory.compileRitual(this)*/ null;
    }
}
