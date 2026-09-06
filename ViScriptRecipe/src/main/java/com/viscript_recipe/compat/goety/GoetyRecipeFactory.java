package com.viscript_recipe.compat.goety;

import com.Polarice3.Goety.common.crafting.*;
import com.Polarice3.Goety.common.ritual.ModRituals;
import com.viscript_recipe.compat.goety.data.*;
import com.viscript_recipe.data.RecipeIngredient;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.Structure;

/**
 * Creates Goety's native recipe objects from editor-owned persisted data.
 */
public final class GoetyRecipeFactory {
    private static final ResourceLocation FALLBACK_ID = new ResourceLocation("viscript_recipe", "goety_editor");

    private GoetyRecipeFactory() {
    }

    /**
     * Compiles a cursed infuser recipe.
     *
     * @param  data editor recipe data
     * @return the native Goety recipe
     */
    public static Recipe<?> compileCursedInfuser(GoetyCursedInfuserRecipeData data) {
        return new CursedInfuserRecipes(
                FALLBACK_ID,
                "",
                requireIngredient(data.getIngredient(), "Goety cursed infuser ingredient cannot be empty"),
                requireItem(data.getResult(), "Goety cursed infuser result cannot be empty"),
                0,
                Math.max(1, data.getCookingTime()),
                data.isGrim()
        );
    }

    /**
     * Compiles a dark ritual recipe with at most twelve pedestal ingredients.
     *
     * @param  data editor recipe data
     * @return the native Goety ritual recipe
     */
    public static Recipe<?> compileRitual(GoetyRitualRecipeData data) {
        var ritualType = data.getRitualType();
        if (ritualType == null || ModRituals.REGISTRY.get().getValue(ritualType) == null) {
            throw new IllegalArgumentException("Unknown Goety ritual type: " + ritualType);
        }
        var ingredients = NonNullList.<Ingredient>create();
        for (var ingredient : data.getIngredients()) {
            var compiled = compileIngredient(ingredient);
            if (!compiled.isEmpty()) {
                ingredients.add(compiled);
            }
        }
        var sacrifice = data.isHasSacrifice()
                ? TagKey.create(Registries.ENTITY_TYPE, requireLocation(data.getEntityToSacrifice(), "Goety sacrifice entity tag cannot be empty"))
                : null;
        var conversion = data.isHasConversion()
                ? TagKey.create(Registries.ENTITY_TYPE, requireLocation(data.getEntityToConvert(), "Goety conversion entity tag cannot be empty"))
                : null;
        TagKey<Structure> structure = data.isHasStructure()
                ? TagKey.create(Registries.STRUCTURE, requireLocation(data.getStructureToLocate(), "Goety structure tag cannot be empty"))
                : null;
        var summon = data.isHasSummon() ? requireEntity(data.getEntityToSummon(), "Goety summon entity cannot be empty") : null;
        var convertInto = data.isHasConversion()
                ? requireEntity(data.getEntityToConvertInto(), "Goety conversion target cannot be empty")
                : null;
        var enchantmentId = data.isHasEnchantment()
                ? requireLocation(data.getEnchantment(), "Goety enchantment cannot be empty")
                : null;
        return new RitualRecipe(
                FALLBACK_ID,
                "",
                data.getCraftType() == null ? "magic" : data.getCraftType().getSerializedName(),
                ritualType,
                requireItem(data.getResult(), "Goety ritual result cannot be empty"),
                summon,
                convertInto,
                requireIngredient(data.getActivationItem(), "Goety ritual activation item cannot be empty"),
                ingredients,
                Math.max(1, data.getDuration()),
                data.getSummonLife(),
                Math.max(0, data.getSoulCost()),
                sacrifice,
                data.isHasSacrifice() ? safeString(data.getEntityToSacrificeDisplayName()) : "",
                conversion,
                data.isHasConversion() ? safeString(data.getEntityToConvertDisplayName()) : "",
                structure,
                data.isHasStructure() ? safeString(data.getStructureDisplayName()) : "",
                BuiltInRegistries.ENCHANTMENT.get(enchantmentId),
                data.isHasEnchantment() ? Math.max(0, data.getXpLevelCost()) : 0,
                safeString(data.getResearch())
        );
    }

    /**
     * Compiles a three-input necro brazier recipe.
     *
     * @param  data editor recipe data
     * @return the native Goety brazier recipe
     */
    public static Recipe<?> compileBrazier(GoetyBrazierRecipeData data) {
        var ingredients = NonNullList.<Ingredient>create();
        for (var ingredient : data.getIngredients()) {
            ingredients.add(requireIngredient(ingredient, "Goety brazier ingredients cannot be empty"));
        }
        return new BrazierRecipe(
                FALLBACK_ID,
                requireItem(data.getResult(), "Goety brazier result cannot be empty"),
                ingredients,
                Math.max(0, data.getSoulCost())
        );
    }

    /**
     * Compiles a pulverize recipe using only the selected result field.
     *
     * @param  data editor recipe data
     * @return the native Goety pulverize recipe
     */
    public static Recipe<?> compilePulverize(GoetyPulverizeRecipeData data) {
        var itemResult = data.getResultKind() == GoetyPulverizeResultKind.ITEM
                ? requireItem(data.getItemResult(), "Goety pulverize item result cannot be empty")
                : ItemStack.EMPTY;
        var blockResult = data.getResultKind() == GoetyPulverizeResultKind.BLOCK
                ? requireBlock(data.getBlockResult())
                : Blocks.CAVE_AIR;
        return new PulverizeRecipe(
                FALLBACK_ID,
                requireIngredient(data.getIngredient(), "Goety pulverize ingredient cannot be empty"),
                itemResult,
                blockResult
        );
    }

    /**
     * Compiles a witch cauldron brewing recipe.
     *
     * @param  data editor recipe data
     * @return the native Goety brewing recipe
     */
    public static Recipe<?> compileBrewing(GoetyBrewingRecipeData data) {
        var kind = data.getEntityKind() == null ? GoetyBrewingEntityKind.NONE : data.getEntityKind();
        TagKey<EntityType<?>> entityTag = kind == GoetyBrewingEntityKind.TAG
                ? TagKey.create(Registries.ENTITY_TYPE, requireLocation(data.getEntity(), "Goety brewing entity tag cannot be empty"))
                : null;
        EntityType<?> entityType = kind == GoetyBrewingEntityKind.ENTITY
                ? requireEntity(data.getEntity(), "Goety brewing entity cannot be empty")
                : null;
        return new BrewingRecipe(
                FALLBACK_ID,
                requireIngredient(data.getIngredient(), "Goety brewing catalyst cannot be empty"),
                entityTag,
                entityType,
                requireEffect(data.getEffect()),
                Math.max(0, data.getSoulCost()),
                Math.max(0, data.getCapacityExtra()),
                Math.max(1, data.getDuration())
        );
    }

    private static Ingredient compileIngredient(RecipeIngredient data) {
        return data == null ? Ingredient.EMPTY : data.compile();
    }

    private static Ingredient requireIngredient(RecipeIngredient data, String message) {
        var ingredient = compileIngredient(data);
        if (ingredient.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return ingredient;
    }

    private static ItemStack requireItem(ItemStack stack, String message) {
        if (stack == null || stack.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return stack.copy();
    }

    private static ResourceLocation requireLocation(ResourceLocation id, String message) {
        if (id == null) {
            throw new IllegalArgumentException(message);
        }
        return id;
    }

    private static EntityType<?> requireEntity(ResourceLocation id, String message) {
        return BuiltInRegistries.ENTITY_TYPE.getOptional(requireLocation(id, message))
                .orElseThrow(() -> new IllegalArgumentException("Unknown entity type: " + id));
    }

    private static MobEffect requireEffect(ResourceLocation id) {
        return BuiltInRegistries.MOB_EFFECT.getOptional(requireLocation(id, "Goety brewing effect cannot be empty"))
                .orElseThrow(() -> new IllegalArgumentException("Unknown mob effect: " + id));
    }

    private static Block requireBlock(ResourceLocation id) {
        var block = BuiltInRegistries.BLOCK.getOptional(requireLocation(id, "Goety pulverize block result cannot be empty"))
                .orElseThrow(() -> new IllegalArgumentException("Unknown block: " + id));
        if (block == Blocks.CAVE_AIR || block == Blocks.AIR || block == Blocks.VOID_AIR) {
            throw new IllegalArgumentException("Goety pulverize block result cannot be air");
        }
        return block;
    }

    private static String safeString(String value) {
        return value == null ? "" : value;
    }
}
