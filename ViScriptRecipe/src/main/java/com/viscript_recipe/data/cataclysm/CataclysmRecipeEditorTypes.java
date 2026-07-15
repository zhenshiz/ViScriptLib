package com.viscript_recipe.data.cataclysm;

import com.viscript_recipe.data.RecipeEditorCategory;
import com.viscript_recipe.data.RecipeEditorLayout;
import com.viscript_recipe.data.RecipeEditorTypes;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * Registers the explicitly supported Cataclysm recipe editor categories and types.
 */
public final class CataclysmRecipeEditorTypes {
    public static final String MOD_ID = "cataclysm";

    public static final ResourceLocation MECHANICAL_FUSION_ANVIL = cataclysm("mechanical_fusion_anvil");
    public static final ResourceLocation ALTAR_OF_AMETHYST = cataclysm("altar_of_amethyst");
    public static final ResourceLocation WEAPON_FUSION = cataclysm("weapon_fusion");
    public static final ResourceLocation AMETHYST_BLESS = cataclysm("amethyst_bless");

    private static final List<String> REQUIRED_MODS = List.of(MOD_ID);
    private static boolean registered;

    private CataclysmRecipeEditorTypes() {
    }

    /**
     * Registers Cataclysm categories and editor types once.
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
        RecipeEditorTypes.registerCategory(new RecipeEditorCategory(
                MECHANICAL_FUSION_ANVIL,
                "viscript_recipe.editor.category.cataclysm.mechanical_fusion_anvil",
                MOD_ID,
                REQUIRED_MODS,
                WEAPON_FUSION,
                RecipeEditorLayout.CATACLYSM_WEAPON_FUSION,
                MECHANICAL_FUSION_ANVIL
        ));
        RecipeEditorTypes.registerCategory(new RecipeEditorCategory(
                ALTAR_OF_AMETHYST,
                "viscript_recipe.editor.category.cataclysm.altar_of_amethyst",
                MOD_ID,
                REQUIRED_MODS,
                AMETHYST_BLESS,
                RecipeEditorLayout.CATACLYSM_AMETHYST_BLESS,
                ALTAR_OF_AMETHYST
        ));
    }

    private static void registerTypes() {
/*        RecipeEditorTypes.register(new RecipeEditorType(
                WEAPON_FUSION,
                MECHANICAL_FUSION_ANVIL,
                "viscript_recipe.editor.type.cataclysm.weapon_fusion",
                REQUIRED_MODS,
                false,
                entry -> entry.getCataclysmWeaponFusion().compile(),
                entry -> false,
                (entry, value) -> {
                },
                entry -> entry.getCataclysmWeaponFusion().getResult(),
                (entry, stack) -> entry.getCataclysmWeaponFusion().setResult(stack.copy())
        ));
        RecipeEditorTypes.register(new RecipeEditorType(
                AMETHYST_BLESS,
                ALTAR_OF_AMETHYST,
                "viscript_recipe.editor.type.cataclysm.amethyst_bless",
                REQUIRED_MODS,
                false,
                entry -> entry.getCataclysmAmethystBless().compile(),
                entry -> false,
                (entry, value) -> {
                },
                entry -> entry.getCataclysmAmethystBless().getResult(),
                (entry, stack) -> entry.getCataclysmAmethystBless().setResult(stack.copy())
        ));*/
    }

    /**
     * Creates a resource location in the Cataclysm namespace.
     *
     * @param path resource path
     * @return Cataclysm resource location
     */
    public static ResourceLocation cataclysm(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
