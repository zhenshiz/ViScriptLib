package com.viscript_recipe.data.create;

import com.viscript_recipe.data.RecipeEditorCategory;
import com.viscript_recipe.data.RecipeEditorLayout;
import com.viscript_recipe.data.RecipeEditorType;
import com.viscript_recipe.data.RecipeEditorTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class CreateRecipeEditorTypes {
    public static final String MOD_ID = "create";
    public static final ResourceLocation MECHANICAL_CRAFTER = create("mechanical_crafter");
    public static final ResourceLocation MECHANICAL_CRAFTING = create("mechanical_crafting");
    public static final ResourceLocation SEQUENCED_ASSEMBLY = create("sequenced_assembly");

    private static final List<String> REQUIRED_MODS = List.of(MOD_ID);
    private static boolean registered;

    private CreateRecipeEditorTypes() {
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
                MECHANICAL_CRAFTER,
                "viscript_recipe.editor.category.create.mechanical_crafter",
                MOD_ID,
                REQUIRED_MODS,
                MECHANICAL_CRAFTING,
                RecipeEditorLayout.CREATE_MECHANICAL_CRAFTING,
                MECHANICAL_CRAFTER
        ));
        RecipeEditorTypes.registerCategory(new RecipeEditorCategory(
                SEQUENCED_ASSEMBLY,
                "create.recipe.sequenced_assembly",
                MOD_ID,
                REQUIRED_MODS,
                SEQUENCED_ASSEMBLY,
                RecipeEditorLayout.CREATE_SEQUENCED_ASSEMBLY,
                null
        ));
        for (var kind : CreateProcessingKind.values()) {
            if (RecipeEditorTypes.getCategory(kind.categoryId()).isPresent()) {
                continue;
            }
            RecipeEditorTypes.registerCategory(new RecipeEditorCategory(
                    kind.categoryId(),
                    categoryFallbackTranslationKey(kind),
                    MOD_ID,
                    REQUIRED_MODS,
                    kind.typeId(),
                    RecipeEditorLayout.CREATE_PROCESSING,
                    categoryWorkstationItem(kind)
            ));
        }
    }

    private static String categoryFallbackTranslationKey(CreateProcessingKind kind) {
        return kind == CreateProcessingKind.ITEM_APPLICATION
                ? "create.recipe.item_application"
                : "viscript_recipe.editor.category.create." + kind.categoryId().getPath();
    }

    private static ResourceLocation categoryWorkstationItem(CreateProcessingKind kind) {
        return kind == CreateProcessingKind.ITEM_APPLICATION ? null : kind.machineItemLocation();
    }

    private static void registerTypes() {
        RecipeEditorTypes.register(new RecipeEditorType(
                MECHANICAL_CRAFTING,
                MECHANICAL_CRAFTER,
                "viscript_recipe.editor.type.create.mechanical_crafting",
                REQUIRED_MODS,
                false,
                entry -> entry.getCreateMechanicalCrafting().compile(),
                entry -> false,
                (entry, value) -> {
                },
                entry -> entry.getCreateMechanicalCrafting().getResult(),
                (entry, stack) -> entry.getCreateMechanicalCrafting().setResult(stack.copy())
        ));
        RecipeEditorTypes.register(new RecipeEditorType(
                SEQUENCED_ASSEMBLY,
                SEQUENCED_ASSEMBLY,
                "viscript_recipe.editor.type.create.sequenced_assembly",
                REQUIRED_MODS,
                false,
                entry -> entry.getCreateSequencedAssembly().compile(),
                entry -> false,
                (entry, value) -> {
                },
                entry -> firstOutput(entry.getCreateSequencedAssembly()),
                (entry, stack) -> setFirstOutput(entry.getCreateSequencedAssembly(), stack)
        ));
        for (var kind : CreateProcessingKind.values()) {
            RecipeEditorTypes.register(new RecipeEditorType(
                    kind.typeId(),
                    kind.categoryId(),
                    "viscript_recipe.editor.type.create." + kind.translationPath(),
                    REQUIRED_MODS,
                    false,
                    entry -> entry.getCreateProcessing().compile(entry.getType()),
                    entry -> false,
                    (entry, value) -> {
                    },
                    entry -> firstOutput(entry.getCreateProcessing()),
                    (entry, stack) -> setFirstOutput(entry.getCreateProcessing(), stack)
            ));
        }
    }

    private static ItemStack firstOutput(CreateProcessingRecipeData data) {
        if (data.getOutputs() == null || data.getOutputs().isEmpty()) {
            return ItemStack.EMPTY;
        }
        var output = data.getOutputs().get(0);
        return output == null || output.getItem() == null ? ItemStack.EMPTY : output.getItem();
    }

    private static void setFirstOutput(CreateProcessingRecipeData data, ItemStack stack) {
        if (data.getOutputs() == null) {
            data.setOutputs(new java.util.ArrayList<>());
        }
        if (data.getOutputs().isEmpty()) {
            data.getOutputs().add(new CreateProcessingOutputData());
        }
        data.getOutputs().get(0).setItem(stack == null ? ItemStack.EMPTY : stack.copy());
    }

    private static ItemStack firstOutput(CreateSequencedAssemblyRecipeData data) {
        if (data.getOutputs() == null || data.getOutputs().isEmpty()) {
            return ItemStack.EMPTY;
        }
        var output = data.getOutputs().get(0);
        return output == null || output.getItem() == null ? ItemStack.EMPTY : output.getItem();
    }

    private static void setFirstOutput(CreateSequencedAssemblyRecipeData data, ItemStack stack) {
        if (data.getOutputs() == null) {
            data.setOutputs(new java.util.ArrayList<>());
        }
        if (data.getOutputs().isEmpty()) {
            data.getOutputs().add(new CreateProcessingOutputData());
        }
        data.getOutputs().get(0).setItem(stack == null ? ItemStack.EMPTY : stack.copy());
    }

    public static ResourceLocation create(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
