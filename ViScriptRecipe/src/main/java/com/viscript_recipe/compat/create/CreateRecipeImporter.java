package com.viscript_recipe.compat.create;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.fluids.transfer.FillingRecipe;
import com.simibubi.create.content.kinetics.crafter.MechanicalCraftingRecipe;
import com.simibubi.create.content.kinetics.deployer.ItemApplicationRecipe;
import com.simibubi.create.content.kinetics.press.PressingRecipe;
import com.simibubi.create.content.kinetics.saw.CuttingRecipe;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import com.viscript_recipe.compat.create.data.*;
import com.viscript_recipe.data.*;
import com.viscript_recipe.recipe.importer.RecipeImportException;
import com.viscript_recipe.recipe.importer.RecipeImportHandler;
import com.viscript_recipe.recipe.importer.RecipeImportResult;
import com.viscript_recipe.recipe.importer.RecipeImporter;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class CreateRecipeImporter implements RecipeImportHandler {
    public static final CreateRecipeImporter INSTANCE = new CreateRecipeImporter();

    private CreateRecipeImporter() {
    }

    @Override
    public boolean canImport(Recipe<?> recipe) {
        if (recipe == null) {
            return false;
        }
        if (recipe instanceof MechanicalCraftingRecipe || recipe instanceof SequencedAssemblyRecipe) {
            return true;
        }
        if (recipe instanceof ProcessingRecipe<?> processing) {
            if (processing.getType() == AllRecipeTypes.DEPLOYING.getType()
                    || processing.getType() == AllRecipeTypes.ITEM_APPLICATION.getType()) {
                return true;
            }
            return CreateProcessingKind.byType(processing.getTypeInfo().getId()).isPresent();
        }
        return false;
    }

    @Nullable
    @Override
    public RecipeImportResult tryImport(Recipe<?> recipe, HolderLookup.Provider provider) throws RecipeImportException {
        if (recipe instanceof MechanicalCraftingRecipe mechanicalCrafting) {
            return success(RecipeImporter.importMechanicalCrafting(recipe.getId(), mechanicalCrafting, mechanicalCrafting.acceptsMirrored(), provider));
        }
        if (recipe instanceof SequencedAssemblyRecipe sequencedAssembly) {
            return success(importSequencedAssembly(recipe.getId(), sequencedAssembly));
        }
        if (recipe instanceof ProcessingRecipe<?> processing) {
            return success(importProcessing(recipe.getId(), processing));
        }
        return null;
    }

    private static RecipeImportResult success(RecipeEntry entry) {
        return RecipeImporter.success(entry);
    }

    private static RecipeEntry importProcessing(ResourceLocation id, ProcessingRecipe<?> recipe) throws RecipeImportException {
        var kind = kindFor(recipe);
        var data = new CreateProcessingRecipeData()
                .setIngredients(importIngredients(recipe.getIngredients(), kind.maxItemInputs()))
                .setFluidIngredients(importFluidIngredients(recipe.getFluidIngredients(), kind.maxFluidInputs()))
                .setOutputs(importOutputs(recipe.getRollableResults(), kind.maxItemOutputs()))
                .setFluidOutputs(importFluidOutputs(recipe.getFluidResults(), kind.maxFluidOutputs()))
                .setProcessingTime(Math.max(0, recipe.getProcessingDuration()))
                .setHeatRequirement(importHeat(recipe.getRequiredHeat()));
        if (recipe instanceof ItemApplicationRecipe itemApplication) {
            data.setKeepHeldItem(itemApplication.shouldKeepHeldItem());
        }
        return RecipeImporter.baseEntry(id, kind.typeId()).setData(data);
    }

    private static CreateProcessingKind kindFor(ProcessingRecipe<?> recipe) throws RecipeImportException {
        if (recipe.getType() == AllRecipeTypes.DEPLOYING.getType()) {
            return CreateProcessingKind.DEPLOYING;
        }
        if (recipe.getType() == AllRecipeTypes.ITEM_APPLICATION.getType()) {
            return CreateProcessingKind.ITEM_APPLICATION;
        }
        var typeId = recipe.getTypeInfo().getId();
        return CreateProcessingKind.byType(typeId)
                .orElseThrow(() -> new RecipeImportException("viscript_recipe.editor.import_recipe.error.unsupported_type", typeId.toString()));
    }

    private static RecipeEntry importSequencedAssembly(ResourceLocation id, SequencedAssemblyRecipe recipe) throws RecipeImportException {
        var data = new CreateSequencedAssemblyRecipeData()
                .setIngredient(RecipeImporter.importIngredient(recipe.getIngredient()))
                .setTransitionalItem(recipe.getTransitionalItem().copyWithCount(1))
                .setOutputs(importOutputs(recipe.resultPool, 9))
                .setLoops(Math.max(1, recipe.getLoops()));
        var steps = new ArrayList<CreateSequencedAssemblyStepData>();
        for (var sequencedRecipe : recipe.getSequence()) {
            var stepRecipe = sequencedRecipe.getRecipe();
            steps.add(importSequencedStep(stepRecipe));
        }
        if (steps.isEmpty()) {
            throw new RecipeImportException("viscript_recipe.editor.import_recipe.error.empty_sequence");
        }
        data.setSequence(steps);
        return RecipeImporter.baseEntry(id, RecipeEditorTypes.CREATE_SEQUENCED_ASSEMBLY).setData(data);
    }

    private static CreateSequencedAssemblyStepData importSequencedStep(ProcessingRecipe<?> recipe) throws RecipeImportException {
        var data = new CreateSequencedAssemblyStepData();
        if (recipe instanceof ItemApplicationRecipe itemApplication) {
            data.setKind(CreateSequencedAssemblyStepKind.DEPLOYING)
                    .setIngredient(RecipeImporter.importIngredient(itemApplication.getRequiredHeldItem()))
                    .setKeepHeldItem(itemApplication.shouldKeepHeldItem());
            return data;
        }
        if (recipe instanceof PressingRecipe) {
            data.setKind(CreateSequencedAssemblyStepKind.PRESSING);
            return data;
        }
        if (recipe instanceof CuttingRecipe) {
            data.setKind(CreateSequencedAssemblyStepKind.CUTTING)
                    .setProcessingTime(Math.max(0, recipe.getProcessingDuration()));
            return data;
        }
        if (recipe instanceof FillingRecipe filling) {
            data.setKind(CreateSequencedAssemblyStepKind.FILLING)
                    .setFluidIngredient(importFluidIngredient(filling.getRequiredFluid()));
            return data;
        }
        throw new RecipeImportException("viscript_recipe.editor.import_recipe.error.unsupported_type", recipe.getTypeInfo().getId().toString());
    }

    private static List<RecipeIngredient> importIngredients(List<Ingredient> ingredients, int max) throws RecipeImportException {
        if (ingredients.size() > max) {
            throw new RecipeImportException("viscript_recipe.editor.import_recipe.error.too_many_ingredients", ingredients.size(), max);
        }
        var imported = new ArrayList<RecipeIngredient>();
        for (var ingredient : ingredients) {
            if (ingredient != null && !ingredient.isEmpty()) {
                imported.add(RecipeImporter.importIngredient(ingredient));
            }
        }
        return imported;
    }

    private static List<FluidIngredientData> importFluidIngredients(List<FluidIngredient> ingredients, int max) throws RecipeImportException {
        if (ingredients.size() > max) {
            throw new RecipeImportException("viscript_recipe.editor.import_recipe.error.too_many_fluid_ingredients", ingredients.size(), max);
        }
        var imported = new ArrayList<FluidIngredientData>();
        for (var ingredient : ingredients) {
            imported.add(importFluidIngredient(ingredient));
        }
        return imported;
    }

    private static FluidIngredientData importFluidIngredient(FluidIngredient ingredient) throws RecipeImportException {
        if (ingredient == null || ingredient.matchingFluidStacks.isEmpty()) {
            return FluidIngredientData.empty();
        }
        if (ingredient instanceof FluidIngredientAccessor.Tag tag) {
            return FluidIngredientData.tag(tag.tag().location())
                    .setAmount(Math.max(1, tag.amountRequired()));
        }
        if (ingredient instanceof FluidIngredientAccessor.Stack single) {
            return FluidIngredientData.fluid(new FluidStack(single.fluid(), Math.max(1, single.amountRequired())));
        }
        var stacks = ingredient.matchingFluidStacks;
        if (stacks.size() == 1) {
            return FluidIngredientData.fluid(new FluidStack(stacks.get(0).getFluid(), Math.max(1, ((FluidIngredientAccessor) ingredient).amountRequired())));
        }
        throw new RecipeImportException("viscript_recipe.editor.import_recipe.error.unsupported_fluid_ingredient");
    }

    private static List<RecipeOutputData> importOutputs(List<ProcessingOutput> outputs, int max) throws RecipeImportException {
        if (outputs.size() > max) {
            throw new RecipeImportException("viscript_recipe.editor.import_recipe.error.too_many_outputs", outputs.size(), max);
        }
        var imported = new ArrayList<RecipeOutputData>();
        for (var output : outputs) {
            if (output == null) {
                continue;
            }
            var stack = output.getStack();
            if (!stack.isEmpty()) {
                imported.add(RecipeOutputData.of(stack, output.getChance()));
            }
        }
        return imported;
    }

    private static List<FluidStack> importFluidOutputs(List<FluidStack> outputs, int max) throws RecipeImportException {
        if (outputs.size() > max) {
            throw new RecipeImportException("viscript_recipe.editor.import_recipe.error.too_many_fluid_outputs", outputs.size(), max);
        }
        var imported = new ArrayList<FluidStack>();
        for (var output : outputs) {
            if (output != null && !output.isEmpty() && output.getFluid() != Fluids.EMPTY) {
                imported.add(output.copy());
            }
        }
        return imported;
    }

    private static CreateHeatCondition importHeat(HeatCondition condition) {
        return switch (condition == null ? HeatCondition.NONE : condition) {
            case NONE -> CreateHeatCondition.NONE;
            case HEATED -> CreateHeatCondition.HEATED;
            case SUPERHEATED -> CreateHeatCondition.SUPERHEATED;
        };
    }
}
