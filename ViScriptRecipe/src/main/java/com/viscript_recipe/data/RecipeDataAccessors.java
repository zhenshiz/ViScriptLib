package com.viscript_recipe.data;

import com.viscript_lib.annotation.ViScriptRegisterAccessors;
import com.viscript_lib.event.RegisterAccessorEvent;
import com.viscript_recipe.compat.create.data.CreateMechanicalCraftingRecipeData;
import com.viscript_recipe.compat.create.data.CreateProcessingRecipeData;
import com.viscript_recipe.compat.create.data.CreateSequencedAssemblyRecipeData;
import com.viscript_recipe.compat.create.data.CreateSequencedAssemblyStepData;
import com.viscript_recipe.compat.farmersdelight.data.FarmerCookingPotRecipeData;
import com.viscript_recipe.compat.farmersdelight.data.FarmerCuttingRecipeData;
import com.viscript_recipe.data.vanilla.*;

public final class RecipeDataAccessors {

    @ViScriptRegisterAccessors
    public static void register(RegisterAccessorEvent event) {
        registerBase(event);
        registerVanilla(event);
        registerFarmersDelight(event);
        registerCreate(event);
    }

    private static void registerBase(RegisterAccessorEvent event) {
        event.register(RecipeFile.class, RecipeFile::new);
        event.register(RecipeEntry.class, RecipeEntry::new);
        event.register(RecipeIngredient.class, RecipeIngredient::of);
        event.register(FluidIngredientData.class, FluidIngredientData::of);
        event.register(RecipeOutputData.class, RecipeOutputData::of);
    }

    private static void registerVanilla(RegisterAccessorEvent event) {
        event.register(CraftingRemainderRule.class, CraftingRemainderRule::new);
        event.register(ShapedKeyEntry.class, ShapedKeyEntry::new);
        event.register(ShapedCraftingRecipeData.class, ShapedCraftingRecipeData::new);
        event.register(ShapelessCraftingRecipeData.class, ShapelessCraftingRecipeData::new);
        event.register(CookingRecipeData.class, CookingRecipeData::new);
        event.register(StonecuttingRecipeData.class, StonecuttingRecipeData::new);
        event.register(SmithingTransformRecipeData.class, SmithingTransformRecipeData::new);
    }

    private static void registerFarmersDelight(RegisterAccessorEvent event) {
        event.register(FarmerCookingPotRecipeData.class, FarmerCookingPotRecipeData::new);
        event.register(FarmerCuttingRecipeData.class, FarmerCuttingRecipeData::new);
    }

    private static void registerCreate(RegisterAccessorEvent event) {
        event.register(CreateMechanicalCraftingRecipeData.class, CreateMechanicalCraftingRecipeData::new);
        event.register(CreateProcessingRecipeData.class, CreateProcessingRecipeData::new);
        event.register(CreateSequencedAssemblyRecipeData.class, CreateSequencedAssemblyRecipeData::new);
        event.register(CreateSequencedAssemblyStepData.class, CreateSequencedAssemblyStepData::new);
    }
}
