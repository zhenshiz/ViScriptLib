package com.viscript_recipe.data.irons_spellbooks;

import com.viscript_recipe.data.RecipeEditorCategory;
import com.viscript_recipe.data.RecipeEditorLayout;
import com.viscript_recipe.data.RecipeEditorTypes;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public final class IronSpellbooksRecipeEditorTypes {
    public static final String MOD_ID = "irons_spellbooks";

    public static final ResourceLocation ALCHEMIST_CAULDRON = iron("alchemist_cauldron");
    public static final ResourceLocation ARCANE_ANVIL = iron("arcane_anvil");
    public static final ResourceLocation ALCHEMIST_CAULDRON_FILL = iron("alchemist_cauldron_fill");
    public static final ResourceLocation ALCHEMIST_CAULDRON_EMPTY = iron("alchemist_cauldron_empty");
    public static final ResourceLocation ALCHEMIST_CAULDRON_BREW = iron("alchemist_cauldron_brew");
    public static final ResourceLocation ARCANE_ANVIL_TRANSFORM = iron("arcane_anvil_transform");
    public static final ResourceLocation SMITHING_TRANSFORM_NO_ADDITION = iron("smithing_transform_no_addition");

    private static final List<String> REQUIRED_MODS = List.of(MOD_ID);
    private static boolean registered;

    private IronSpellbooksRecipeEditorTypes() {
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
                ALCHEMIST_CAULDRON,
                "viscript_recipe.editor.category.irons_spellbooks.alchemist_cauldron",
                MOD_ID,
                REQUIRED_MODS,
                ALCHEMIST_CAULDRON_FILL,
                RecipeEditorLayout.ALCHEMIST_CAULDRON
        ));
        RecipeEditorTypes.registerCategory(new RecipeEditorCategory(
                ARCANE_ANVIL,
                "viscript_recipe.editor.category.irons_spellbooks.arcane_anvil",
                MOD_ID,
                REQUIRED_MODS,
                ARCANE_ANVIL_TRANSFORM,
                RecipeEditorLayout.SMITHING
        ));
    }

    private static void registerTypes() {
/*        RecipeEditorTypes.register(new RecipeEditorType(
                ALCHEMIST_CAULDRON_FILL,
                ALCHEMIST_CAULDRON,
                "viscript_recipe.editor.type.irons_spellbooks.alchemist_cauldron_fill",
                REQUIRED_MODS,
                false,
                entry -> entry.getIronAlchemistCauldron().compileFill(),
                entry -> false,
                (entry, value) -> {
                },
                entry -> entry.getIronAlchemistCauldron().getResult(),
                (entry, stack) -> entry.getIronAlchemistCauldron().setResult(stack.copy())
        ));
        RecipeEditorTypes.register(new RecipeEditorType(
                ALCHEMIST_CAULDRON_EMPTY,
                ALCHEMIST_CAULDRON,
                "viscript_recipe.editor.type.irons_spellbooks.alchemist_cauldron_empty",
                REQUIRED_MODS,
                false,
                entry -> entry.getIronAlchemistCauldron().compileEmpty(),
                entry -> false,
                (entry, value) -> {
                },
                entry -> entry.getIronAlchemistCauldron().getResult(),
                (entry, stack) -> entry.getIronAlchemistCauldron().setResult(stack.copy())
        ));
        RecipeEditorTypes.register(new RecipeEditorType(
                ALCHEMIST_CAULDRON_BREW,
                ALCHEMIST_CAULDRON,
                "viscript_recipe.editor.type.irons_spellbooks.alchemist_cauldron_brew",
                REQUIRED_MODS,
                false,
                entry -> entry.getIronAlchemistCauldron().compileBrew(),
                entry -> false,
                (entry, value) -> {
                },
                entry -> net.minecraft.world.item.ItemStack.EMPTY,
                (entry, stack) -> entry.getIronAlchemistCauldron().setByproduct(net.minecraft.world.item.ItemStack.EMPTY)
        ));
        RecipeEditorTypes.register(new RecipeEditorType(
                ARCANE_ANVIL_TRANSFORM,
                ARCANE_ANVIL,
                "viscript_recipe.editor.type.irons_spellbooks.arcane_anvil_transform",
                REQUIRED_MODS,
                false,
                entry -> {
                    throw new UnsupportedOperationException("Iron's Spells Arcane Anvil recipes are handled by ViScriptRecipe's Arcane Anvil menu hook");
                },
                entry -> false,
                (entry, value) -> {
                },
                entry -> entry.getIronArcaneAnvil().getResult(),
                (entry, stack) -> entry.getIronArcaneAnvil().setResult(stack.copy())
        ));*/
    }

    private static ResourceLocation iron(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
