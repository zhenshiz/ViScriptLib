package com.viscript_recipe.data.iceandfire;

import com.viscript_recipe.data.RecipeEditorCategory;
import com.viscript_recipe.data.RecipeEditorLayout;
import com.viscript_recipe.data.RecipeEditorTypes;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public final class IceAndFireRecipeEditorTypes {
    public static final String MOD_ID = "iceandfire";

    public static final ResourceLocation DRAGON_FORGE = iceandfire("dragon_forge");
    public static final ResourceLocation DRAGONFORGE = iceandfire("dragonforge");

    private static final List<String> REQUIRED_MODS = List.of(MOD_ID);
    private static boolean registered;

    private IceAndFireRecipeEditorTypes() {
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
                DRAGON_FORGE,
                "viscript_recipe.editor.category.iceandfire.dragon_forge",
                MOD_ID,
                REQUIRED_MODS,
                DRAGONFORGE,
                RecipeEditorLayout.DRAGON_FORGE,
                iceandfire("dragonforge_fire_core")
        ));
    }

    private static void registerTypes() {
/*        RecipeEditorTypes.register(new RecipeEditorType(
                DRAGONFORGE,
                DRAGON_FORGE,
                "viscript_recipe.editor.type.iceandfire.dragonforge",
                REQUIRED_MODS,
                false,
                entry -> entry.getIceAndFireDragonForge().compile(),
                entry -> false,
                (entry, value) -> {
                },
                entry -> entry.getIceAndFireDragonForge().getResult(),
                (entry, stack) -> entry.getIceAndFireDragonForge().setResult(stack.copy())
        ));*/
    }

    private static ResourceLocation iceandfire(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
