package com.viscript_recipe.data.goety;

import com.viscript_recipe.data.RecipeEditorCategory;
import com.viscript_recipe.data.RecipeEditorLayout;
import com.viscript_recipe.data.RecipeEditorTypes;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * Registers Goety's five JEI-backed custom recipe types.
 */
public final class GoetyRecipeEditorTypes {
    public static final String MOD_ID = "goety";

    public static final ResourceLocation CURSED_INFUSER = goety("cursed_infuser");
    public static final ResourceLocation DARK_ALTAR = goety("dark_altar");
    public static final ResourceLocation NECRO_BRAZIER = goety("necro_brazier");
    public static final ResourceLocation PULVERIZE_FOCUS = goety("pulverize_focus");
    public static final ResourceLocation WITCH_CAULDRON = goety("witch_cauldron");

    public static final ResourceLocation CURSED_INFUSER_RECIPE = goety("cursed_infuser_recipes");
    public static final ResourceLocation RITUAL = goety("ritual");
    public static final ResourceLocation BRAZIER = goety("brazier");
    public static final ResourceLocation PULVERIZE = goety("pulverize");
    public static final ResourceLocation BREWING = goety("brewing");

    private static final List<String> REQUIRED_MODS = List.of(MOD_ID);
    private static boolean registered;

    private GoetyRecipeEditorTypes() {
    }

    /**
     * Registers all Goety categories and editor types once.
     */
    public static synchronized void registerAll() {
        if (registered) {
            return;
        }
        registered = true;
        registerCategories();
        registerTypes();
    }

    private static void registerCategories() {
        registerCategory(CURSED_INFUSER, CURSED_INFUSER_RECIPE, RecipeEditorLayout.GOETY_CURSED_INFUSER, CURSED_INFUSER);
        registerCategory(DARK_ALTAR, RITUAL, RecipeEditorLayout.GOETY_RITUAL, DARK_ALTAR);
        registerCategory(NECRO_BRAZIER, BRAZIER, RecipeEditorLayout.GOETY_BRAZIER, NECRO_BRAZIER);
        registerCategory(PULVERIZE_FOCUS, PULVERIZE, RecipeEditorLayout.GOETY_PULVERIZE, PULVERIZE_FOCUS);
        registerCategory(WITCH_CAULDRON, BREWING, RecipeEditorLayout.GOETY_BREWING, WITCH_CAULDRON);
    }

    private static void registerCategory(ResourceLocation id, ResourceLocation defaultType, RecipeEditorLayout layout,
                                         ResourceLocation workstation) {
        RecipeEditorTypes.registerCategory(new RecipeEditorCategory(
                id,
                "viscript_recipe.editor.category.goety." + id.getPath(),
                MOD_ID,
                REQUIRED_MODS,
                defaultType,
                layout,
                workstation
        ));
    }

    private static void registerTypes() {
/*        RecipeEditorTypes.register(new RecipeEditorType(
                CURSED_INFUSER_RECIPE,
                CURSED_INFUSER,
                "viscript_recipe.editor.type.goety.cursed_infuser",
                REQUIRED_MODS,
                false,
                entry -> entry.getGoetyCursedInfuser().compile(),
                entry -> false,
                (entry, value) -> {
                },
                entry -> entry.getGoetyCursedInfuser().getResult(),
                (entry, stack) -> entry.getGoetyCursedInfuser().setResult(stack.copy())
        ));
        RecipeEditorTypes.register(new RecipeEditorType(
                RITUAL,
                DARK_ALTAR,
                "viscript_recipe.editor.type.goety.ritual",
                REQUIRED_MODS,
                false,
                entry -> entry.getGoetyRitual().compile(),
                entry -> false,
                (entry, value) -> {
                },
                entry -> entry.getGoetyRitual().getResult(),
                (entry, stack) -> entry.getGoetyRitual().setResult(stack.copy())
        ));
        RecipeEditorTypes.register(new RecipeEditorType(
                BRAZIER,
                NECRO_BRAZIER,
                "viscript_recipe.editor.type.goety.brazier",
                REQUIRED_MODS,
                false,
                entry -> entry.getGoetyBrazier().compile(),
                entry -> false,
                (entry, value) -> {
                },
                entry -> entry.getGoetyBrazier().getResult(),
                (entry, stack) -> entry.getGoetyBrazier().setResult(stack.copy())
        ));
        RecipeEditorTypes.register(new RecipeEditorType(
                PULVERIZE,
                PULVERIZE_FOCUS,
                "viscript_recipe.editor.type.goety.pulverize",
                REQUIRED_MODS,
                false,
                entry -> entry.getGoetyPulverize().compile(),
                entry -> false,
                (entry, value) -> {
                },
                entry -> entry.getGoetyPulverize().visibleResult(),
                (entry, stack) -> entry.getGoetyPulverize().setVisibleResult(stack)
        ));
        RecipeEditorTypes.register(new RecipeEditorType(
                BREWING,
                WITCH_CAULDRON,
                "viscript_recipe.editor.type.goety.brewing",
                REQUIRED_MODS,
                false,
                entry -> entry.getGoetyBrewing().compile(),
                entry -> false,
                (entry, value) -> {
                },
                entry -> entry.getGoetyBrewing().visibleResult(),
                (entry, stack) -> {
                }
        ));*/
    }

    /**
     * Creates a resource location in the Goety namespace.
     *
     * @param  path resource path
     * @return Goety resource location
     */
    public static ResourceLocation goety(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
