package com.viscript_recipe.data.spore;

import com.viscript_recipe.data.RecipeEditorCategory;
import com.viscript_recipe.data.RecipeEditorLayout;
import com.viscript_recipe.data.RecipeEditorTypes;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public final class SporeRecipeEditorTypes {
    public static final String MOD_ID = "spore";

    public static final ResourceLocation SURGERY_TABLE = spore("surgery_table");
    public static final ResourceLocation SURGERY = spore("surgery");
    public static final ResourceLocation GRAFTING = spore("grafting");

    private static final List<String> REQUIRED_MODS = List.of(MOD_ID);
    private static boolean registered;

    private SporeRecipeEditorTypes() {
    }

    public static synchronized void registerAll() {
        if (registered) {
            return;
        }
        registered = true;
        RecipeEditorTypes.registerCategory(new RecipeEditorCategory(
                SURGERY_TABLE,
                "viscript_recipe.editor.category.spore.surgery_table",
                MOD_ID,
                REQUIRED_MODS,
                SURGERY,
                RecipeEditorLayout.SPORE,
                SURGERY_TABLE
        ));
        registerTypes();
    }

    private static void registerTypes() {
/*        RecipeEditorTypes.register(new RecipeEditorType(
                SURGERY,
                SURGERY_TABLE,
                "viscript_recipe.editor.type.spore.surgery",
                REQUIRED_MODS,
                false,
                entry -> entry.getSporeSurgery().compile(),
                entry -> false,
                (entry, value) -> {
                },
                entry -> entry.getSporeSurgery().getResult(),
                (entry, stack) -> entry.getSporeSurgery().setResult(stack.copy())
        ));
        RecipeEditorTypes.register(new RecipeEditorType(
                GRAFTING,
                SURGERY_TABLE,
                "viscript_recipe.editor.type.spore.grafting",
                REQUIRED_MODS,
                false,
                entry -> entry.getSporeGrafting().compile(),
                entry -> false,
                (entry, value) -> {
                },
                entry -> entry.getSporeGrafting().getResult(),
                (entry, stack) -> entry.getSporeGrafting().setResult(stack.copy())
        ));*/
    }

    public static ResourceLocation spore(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
