package com.viscript_recipe.data.kaleidoscope_cookery;

import com.viscript_recipe.data.RecipeEditorCategory;
import com.viscript_recipe.data.RecipeEditorLayout;
import com.viscript_recipe.data.RecipeEditorTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class KaleidoscopeCookeryRecipeEditorTypes {
    public static final String MOD_ID = "kaleidoscope_cookery";

    public static final ResourceLocation POT = kaleidoscope("pot");
    public static final ResourceLocation STOCKPOT = kaleidoscope("stockpot");
    public static final ResourceLocation MILLSTONE = kaleidoscope("millstone");
    public static final ResourceLocation CHOPPING_BOARD = kaleidoscope("chopping_board");
    public static final ResourceLocation STEAMER = kaleidoscope("steamer");
    public static final ResourceLocation TEAPOT = kaleidoscope("teapot");

    private static final List<String> REQUIRED_MODS = List.of(MOD_ID);
    private static boolean registered;

    private KaleidoscopeCookeryRecipeEditorTypes() {
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
        registerCategory(POT, "viscript_recipe.editor.category.kaleidoscope_cookery.pot", POT, RecipeEditorLayout.KALEIDOSCOPE_POT);
        registerCategory(STOCKPOT, "viscript_recipe.editor.category.kaleidoscope_cookery.stockpot", STOCKPOT, RecipeEditorLayout.KALEIDOSCOPE_STOCKPOT);
        registerCategory(MILLSTONE, "viscript_recipe.editor.category.kaleidoscope_cookery.millstone", MILLSTONE, RecipeEditorLayout.KALEIDOSCOPE_MILLSTONE);
        registerCategory(CHOPPING_BOARD, "viscript_recipe.editor.category.kaleidoscope_cookery.chopping_board", CHOPPING_BOARD, RecipeEditorLayout.KALEIDOSCOPE_CHOPPING_BOARD);
        registerCategory(STEAMER, "viscript_recipe.editor.category.kaleidoscope_cookery.steamer", STEAMER, RecipeEditorLayout.KALEIDOSCOPE_STEAMER);
        registerCategory(TEAPOT, "viscript_recipe.editor.category.kaleidoscope_cookery.teapot", TEAPOT, RecipeEditorLayout.KALEIDOSCOPE_TEAPOT);
    }

    private static void registerCategory(ResourceLocation category, String translationKey, ResourceLocation defaultType, RecipeEditorLayout layout) {
        RecipeEditorTypes.registerCategory(new RecipeEditorCategory(
                category,
                translationKey,
                MOD_ID,
                REQUIRED_MODS,
                defaultType,
                layout,
                category
        ));
    }

    private static void registerTypes() {
/*        RecipeEditorTypes.register(new RecipeEditorType(
                POT,
                POT,
                "viscript_recipe.editor.type.kaleidoscope_cookery.pot",
                REQUIRED_MODS,
                false,
                entry -> entry.getKaleidoscopePot().compile(),
                entry -> false,
                (entry, value) -> {
                },
                entry -> entry.getKaleidoscopePot().getResult(),
                (entry, stack) -> entry.getKaleidoscopePot().setResult(copy(stack))
        ));
        RecipeEditorTypes.register(new RecipeEditorType(
                STOCKPOT,
                STOCKPOT,
                "viscript_recipe.editor.type.kaleidoscope_cookery.stockpot",
                REQUIRED_MODS,
                false,
                entry -> entry.getKaleidoscopeStockpot().compile(),
                entry -> false,
                (entry, value) -> {
                },
                entry -> entry.getKaleidoscopeStockpot().getResult(),
                (entry, stack) -> entry.getKaleidoscopeStockpot().setResult(copy(stack))
        ));
        RecipeEditorTypes.register(new RecipeEditorType(
                MILLSTONE,
                MILLSTONE,
                "viscript_recipe.editor.type.kaleidoscope_cookery.millstone",
                REQUIRED_MODS,
                false,
                entry -> entry.getKaleidoscopeMillstone().compile(),
                entry -> false,
                (entry, value) -> {
                },
                entry -> entry.getKaleidoscopeMillstone().getResult(),
                (entry, stack) -> entry.getKaleidoscopeMillstone().setResult(copy(stack))
        ));
        RecipeEditorTypes.register(new RecipeEditorType(
                CHOPPING_BOARD,
                CHOPPING_BOARD,
                "viscript_recipe.editor.type.kaleidoscope_cookery.chopping_board",
                REQUIRED_MODS,
                false,
                entry -> entry.getKaleidoscopeChoppingBoard().compile(),
                entry -> false,
                (entry, value) -> {
                },
                entry -> entry.getKaleidoscopeChoppingBoard().getResult(),
                (entry, stack) -> entry.getKaleidoscopeChoppingBoard().setResult(copy(stack))
        ));
        RecipeEditorTypes.register(new RecipeEditorType(
                STEAMER,
                STEAMER,
                "viscript_recipe.editor.type.kaleidoscope_cookery.steamer",
                REQUIRED_MODS,
                false,
                entry -> entry.getKaleidoscopeSteamer().compile(),
                entry -> false,
                (entry, value) -> {
                },
                entry -> entry.getKaleidoscopeSteamer().getResult(),
                (entry, stack) -> entry.getKaleidoscopeSteamer().setResult(copy(stack))
        ));
        RecipeEditorTypes.register(new RecipeEditorType(
                TEAPOT,
                TEAPOT,
                "viscript_recipe.editor.type.kaleidoscope_cookery.teapot",
                REQUIRED_MODS,
                false,
                entry -> entry.getKaleidoscopeTeapot().compile(),
                entry -> false,
                (entry, value) -> {
                },
                entry -> entry.getKaleidoscopeTeapot().getResult(),
                (entry, stack) -> entry.getKaleidoscopeTeapot().setResult(copy(stack))
        ));*/
    }

    private static ItemStack copy(ItemStack stack) {
        return stack == null ? ItemStack.EMPTY : stack.copy();
    }

    public static ResourceLocation kaleidoscope(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
