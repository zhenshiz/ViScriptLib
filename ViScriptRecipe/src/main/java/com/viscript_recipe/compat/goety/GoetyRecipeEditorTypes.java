package com.viscript_recipe.compat.goety;

import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import com.viscript_recipe.IModModule;
import com.viscript_recipe.compat.goety.canvas.*;
import com.viscript_recipe.compat.goety.data.*;
import com.viscript_recipe.data.RecipeEditorCategory;
import com.viscript_recipe.data.RecipeEditorType;
import com.viscript_recipe.recipe.importer.RecipeImportHandler;
import net.minecraft.resources.ResourceLocation;

@LDLRegister(registry = IModModule.ID, name = GoetyRecipeEditorTypes.MOD_ID, modID = GoetyRecipeEditorTypes.MOD_ID)
public final class GoetyRecipeEditorTypes implements IModModule{
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

    private static boolean registered;

    @Override
    public RecipeImportHandler importHandler() {return GoetyRecipeImporter.INSTANCE;}

    @Override
    public void registerEditorTypes() {
        if (registered) return;
        registered = true;
        registerCategories();
        registerTypes();
    }

    private void registerCategories() {
        registerCategory(CURSED_INFUSER, CURSED_INFUSER_RECIPE, CURSED_INFUSER);
        registerCategory(DARK_ALTAR, RITUAL, DARK_ALTAR);
        registerCategory(NECRO_BRAZIER, BRAZIER, NECRO_BRAZIER);
        registerCategory(PULVERIZE_FOCUS, PULVERIZE, PULVERIZE_FOCUS);
        registerCategory(WITCH_CAULDRON, BREWING, WITCH_CAULDRON);
    }

    private void registerCategory(ResourceLocation id, ResourceLocation defaultType,
                                         ResourceLocation workstation) {
        registerCategory(RecipeEditorCategory.of(
                id, "viscript_recipe.editor.category.goety." + id.getPath(),
                MOD_ID, defaultType, workstation
        ));
    }

    private void registerTypes() {
        registerEditorType(RecipeEditorType.of(
                CURSED_INFUSER_RECIPE, CURSED_INFUSER,
                "viscript_recipe.editor.type.goety.cursed_infuser",
                GoetyCursedInfuserRecipeData.class, GoetyCursedInfuserRecipeData::new,
                CursedInfuserCanvas::new, MOD_ID
        ));
        registerEditorType(RecipeEditorType.of(
                RITUAL, DARK_ALTAR,
                "viscript_recipe.editor.type.goety.ritual",
                GoetyRitualRecipeData.class, GoetyRitualRecipeData::new,
                RitualCanvas::new, MOD_ID
        ));
        registerEditorType(RecipeEditorType.of(
                BRAZIER, NECRO_BRAZIER,
                "viscript_recipe.editor.type.goety.brazier",
                GoetyBrazierRecipeData.class, GoetyBrazierRecipeData::new,
                BrazierCanvas::new, MOD_ID
        ));
        registerEditorType(RecipeEditorType.of(
                PULVERIZE, PULVERIZE_FOCUS,
                "viscript_recipe.editor.type.goety.pulverize",
                GoetyPulverizeRecipeData.class, GoetyPulverizeRecipeData::new,
                PulverizeCanvas::new, MOD_ID
        ));
        registerEditorType(RecipeEditorType.of(
                BREWING, WITCH_CAULDRON,
                "viscript_recipe.editor.type.goety.brewing",
                GoetyBrewingRecipeData.class, GoetyBrewingRecipeData::new,
                BrewingCanvas::new, MOD_ID
        ));
    }

    public static ResourceLocation goety(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
