package com.viscript_recipe.compat.create;

import com.viscript_recipe.compat.create.canvas.CreateProcessingCanvas;
import com.viscript_recipe.compat.create.canvas.MechanicalCraftingCanvas;
import com.viscript_recipe.compat.create.canvas.SequencedAssemblyCanvas;
import com.viscript_recipe.compat.create.data.CreateMechanicalCraftingRecipeData;
import com.viscript_recipe.compat.create.data.CreateProcessingKind;
import com.viscript_recipe.compat.create.data.CreateProcessingRecipeData;
import com.viscript_recipe.compat.create.data.CreateSequencedAssemblyRecipeData;
import com.viscript_recipe.data.RecipeEditorCategory;
import com.viscript_recipe.data.RecipeEditorType;
import com.viscript_recipe.data.RecipeEditorTypes;
import net.minecraft.resources.ResourceLocation;

public final class CreateRecipeEditorTypes {
    public static final String MOD_ID = "create";
    public static final ResourceLocation MECHANICAL_CRAFTER = create("mechanical_crafter");
    public static final ResourceLocation MECHANICAL_CRAFTING = create("mechanical_crafting");
    public static final ResourceLocation SEQUENCED_ASSEMBLY = create("sequenced_assembly");

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
        RecipeEditorTypes.registerCategory(RecipeEditorCategory.of(
                MECHANICAL_CRAFTER,
                "viscript_recipe.editor.category.create.mechanical_crafter",
                MOD_ID, MECHANICAL_CRAFTING, MECHANICAL_CRAFTER
        ));
        RecipeEditorTypes.registerCategory(RecipeEditorCategory.of(
                SEQUENCED_ASSEMBLY,
                "create.recipe.sequenced_assembly",
                MOD_ID, SEQUENCED_ASSEMBLY, null
        ));
        for (var kind : CreateProcessingKind.values()) {
            if (RecipeEditorTypes.getCategory(kind.categoryId()).isPresent()) {
                continue;
            }
            RecipeEditorTypes.registerCategory(RecipeEditorCategory.of(
                    kind.categoryId(),
                    categoryFallbackTranslationKey(kind),
                    MOD_ID, kind.typeId(), categoryWorkstationItem(kind)
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
        RecipeEditorTypes.register(RecipeEditorType.of(
                MECHANICAL_CRAFTING, MECHANICAL_CRAFTER,
                "viscript_recipe.editor.type.create.mechanical_crafting",
                CreateMechanicalCraftingRecipeData.class, CreateMechanicalCraftingRecipeData::new,
                MechanicalCraftingCanvas::new, MOD_ID
        ));
        RecipeEditorTypes.register(RecipeEditorType.of(
                SEQUENCED_ASSEMBLY, SEQUENCED_ASSEMBLY,
                "viscript_recipe.editor.type.create.sequenced_assembly",
                CreateSequencedAssemblyRecipeData.class, CreateSequencedAssemblyRecipeData::new,
                SequencedAssemblyCanvas::new, MOD_ID
        ));
        for (var kind : CreateProcessingKind.values()) {
            RecipeEditorTypes.register(RecipeEditorType.of(
                    kind.typeId(), kind.categoryId(),
                    "viscript_recipe.editor.type.create." + kind.translationPath(),
                    CreateProcessingRecipeData.class, CreateProcessingRecipeData::new,
                    CreateProcessingCanvas::new, MOD_ID
            ));
        }
    }

    public static ResourceLocation create(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
