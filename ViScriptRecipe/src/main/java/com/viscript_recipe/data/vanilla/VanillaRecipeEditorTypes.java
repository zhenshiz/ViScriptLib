package com.viscript_recipe.data.vanilla;

import com.viscript_recipe.data.RecipeEditorCategory;
import com.viscript_recipe.data.RecipeEditorLayout;
import com.viscript_recipe.data.RecipeEditorType;
import com.viscript_recipe.data.RecipeEditorTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.SmokingRecipe;

import java.util.List;

public final class VanillaRecipeEditorTypes {
    public static final String MOD_ID = "minecraft";

    public static final ResourceLocation CRAFTING_TABLE = minecraft("crafting_table");
    public static final ResourceLocation FURNACE = minecraft("furnace");
    public static final ResourceLocation BLAST_FURNACE = minecraft("blast_furnace");
    public static final ResourceLocation SMOKER = minecraft("smoker");
    public static final ResourceLocation CAMPFIRE = minecraft("campfire");
    public static final ResourceLocation STONECUTTER = minecraft("stonecutter");
    public static final ResourceLocation SMITHING_TABLE = minecraft("smithing_table");
    public static final ResourceLocation CRAFTING_SHAPED = minecraft("crafting_shaped");
    public static final ResourceLocation CRAFTING_SHAPELESS = minecraft("crafting_shapeless");
    public static final ResourceLocation SMELTING = minecraft("smelting");
    public static final ResourceLocation BLASTING = minecraft("blasting");
    public static final ResourceLocation SMOKING = minecraft("smoking");
    public static final ResourceLocation CAMPFIRE_COOKING = minecraft("campfire_cooking");
    public static final ResourceLocation STONECUTTING = minecraft("stonecutting");
    public static final ResourceLocation SMITHING_TRANSFORM = minecraft("smithing_transform");

    private static boolean registered;

    private VanillaRecipeEditorTypes() {
    }

    public static synchronized void registerAll() {
        if (registered) {
            return;
        }
        registered = true;
        registerCategories();
        registerTypes();
    }

    private static void registerCategories() {
        RecipeEditorTypes.registerCategory(new RecipeEditorCategory(
                CRAFTING_TABLE,
                "viscript_recipe.editor.category.minecraft.crafting_table",
                MOD_ID,
                List.of(),
                CRAFTING_SHAPED,
                RecipeEditorLayout.CRAFTING_GRID
        ));
        RecipeEditorTypes.registerCategory(new RecipeEditorCategory(
                FURNACE,
                "viscript_recipe.editor.category.minecraft.furnace",
                MOD_ID,
                List.of(),
                SMELTING,
                RecipeEditorLayout.COOKING
        ));
        RecipeEditorTypes.registerCategory(new RecipeEditorCategory(
                BLAST_FURNACE,
                "viscript_recipe.editor.category.minecraft.blast_furnace",
                MOD_ID,
                List.of(),
                BLASTING,
                RecipeEditorLayout.COOKING
        ));
        RecipeEditorTypes.registerCategory(new RecipeEditorCategory(
                SMOKER,
                "viscript_recipe.editor.category.minecraft.smoker",
                MOD_ID,
                List.of(),
                SMOKING,
                RecipeEditorLayout.COOKING
        ));
        RecipeEditorTypes.registerCategory(new RecipeEditorCategory(
                CAMPFIRE,
                "viscript_recipe.editor.category.minecraft.campfire",
                MOD_ID,
                List.of(),
                CAMPFIRE_COOKING,
                RecipeEditorLayout.COOKING
        ));
        RecipeEditorTypes.registerCategory(new RecipeEditorCategory(
                STONECUTTER,
                "viscript_recipe.editor.category.minecraft.stonecutter",
                MOD_ID,
                List.of(),
                STONECUTTING,
                RecipeEditorLayout.SINGLE_INPUT
        ));
        RecipeEditorTypes.registerCategory(new RecipeEditorCategory(
                SMITHING_TABLE,
                "viscript_recipe.editor.category.minecraft.smithing_table",
                MOD_ID,
                List.of(),
                SMITHING_TRANSFORM,
                RecipeEditorLayout.SMITHING
        ));
    }

    private static void registerTypes() {
        RecipeEditorTypes.register(new RecipeEditorType(
                CRAFTING_SHAPED,
                CRAFTING_TABLE,
                "viscript_recipe.editor.type.minecraft.crafting_shaped",
                List.of(),
                true,
                entry -> entry.getShaped().compile(),
                entry -> entry.getShaped().isShowNotification(),
                (entry, value) -> entry.getShaped().setShowNotification(value),
                entry -> entry.getShaped().getResult(),
                (entry, stack) -> entry.getShaped().setResult(stack.copy())
        ));
        RecipeEditorTypes.register(new RecipeEditorType(
                CRAFTING_SHAPELESS,
                CRAFTING_TABLE,
                "viscript_recipe.editor.type.minecraft.crafting_shapeless",
                List.of(),
                true,
                entry -> entry.getShapeless().compile(),
                entry -> entry.getShapeless().isShowNotification(),
                (entry, value) -> entry.getShapeless().setShowNotification(value),
                entry -> entry.getShapeless().getResult(),
                (entry, stack) -> entry.getShapeless().setResult(stack.copy())
        ));
        RecipeEditorTypes.register(new RecipeEditorType(
                SMELTING,
                FURNACE,
                "viscript_recipe.editor.type.minecraft.smelting",
                List.of(),
                false,
                entry -> entry.getCooking().compile(SmeltingRecipe::new),
                entry -> false,
                (entry, value) -> {
                },
                entry -> entry.getCooking().getResult(),
                (entry, stack) -> entry.getCooking().setResult(stack.copy())
        ));
        RecipeEditorTypes.register(new RecipeEditorType(
                BLASTING,
                BLAST_FURNACE,
                "viscript_recipe.editor.type.minecraft.blasting",
                List.of(),
                false,
                entry -> entry.getCooking().compile(BlastingRecipe::new),
                entry -> false,
                (entry, value) -> {
                },
                entry -> entry.getCooking().getResult(),
                (entry, stack) -> entry.getCooking().setResult(stack.copy())
        ));
        RecipeEditorTypes.register(new RecipeEditorType(
                SMOKING,
                SMOKER,
                "viscript_recipe.editor.type.minecraft.smoking",
                List.of(),
                false,
                entry -> entry.getCooking().compile(SmokingRecipe::new),
                entry -> false,
                (entry, value) -> {
                },
                entry -> entry.getCooking().getResult(),
                (entry, stack) -> entry.getCooking().setResult(stack.copy())
        ));
        RecipeEditorTypes.register(new RecipeEditorType(
                CAMPFIRE_COOKING,
                CAMPFIRE,
                "viscript_recipe.editor.type.minecraft.campfire_cooking",
                List.of(),
                false,
                entry -> entry.getCooking().compile(CampfireCookingRecipe::new),
                entry -> false,
                (entry, value) -> {
                },
                entry -> entry.getCooking().getResult(),
                (entry, stack) -> entry.getCooking().setResult(stack.copy())
        ));
        RecipeEditorTypes.register(new RecipeEditorType(
                STONECUTTING,
                STONECUTTER,
                "viscript_recipe.editor.type.minecraft.stonecutting",
                List.of(),
                true,
                entry -> entry.getStonecutting().compile(),
                entry -> entry.getStonecutting().isShowNotification(),
                (entry, value) -> entry.getStonecutting().setShowNotification(value),
                entry -> entry.getStonecutting().getResult(),
                (entry, stack) -> entry.getStonecutting().setResult(stack.copy())
        ));
        RecipeEditorTypes.register(new RecipeEditorType(
                SMITHING_TRANSFORM,
                SMITHING_TABLE,
                "viscript_recipe.editor.type.minecraft.smithing_transform",
                List.of(),
                true,
                entry -> entry.getSmithingTransform().compile(),
                entry -> entry.getSmithingTransform().isShowNotification(),
                (entry, value) -> entry.getSmithingTransform().setShowNotification(value),
                entry -> entry.getSmithingTransform().getResult(),
                (entry, stack) -> entry.getSmithingTransform().setResult(stack.copy())
        ));
    }

    private static ResourceLocation minecraft(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
