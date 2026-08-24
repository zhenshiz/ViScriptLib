package com.viscript_recipe.compat.create;

import com.simibubi.create.content.equipment.sandPaper.SandPaperPolishingRecipe;
import com.simibubi.create.content.fluids.transfer.EmptyingRecipe;
import com.simibubi.create.content.fluids.transfer.FillingRecipe;
import com.simibubi.create.content.kinetics.crusher.CrushingRecipe;
import com.simibubi.create.content.kinetics.deployer.DeployerApplicationRecipe;
import com.simibubi.create.content.kinetics.deployer.ItemApplicationRecipe;
import com.simibubi.create.content.kinetics.deployer.ManualApplicationRecipe;
import com.simibubi.create.content.kinetics.fan.processing.HauntingRecipe;
import com.simibubi.create.content.kinetics.fan.processing.SplashingRecipe;
import com.simibubi.create.content.kinetics.millstone.MillingRecipe;
import com.simibubi.create.content.kinetics.mixer.CompactingRecipe;
import com.simibubi.create.content.kinetics.mixer.MixingRecipe;
import com.simibubi.create.content.kinetics.press.PressingRecipe;
import com.simibubi.create.content.kinetics.saw.CuttingRecipe;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipeBuilder;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import com.simibubi.create.foundation.item.ItemHelper;
import com.viscript_lib.util.math.Clamp;
import com.viscript_recipe.ViScriptRecipe;
import com.viscript_recipe.compat.create.data.*;
import com.viscript_recipe.data.FluidIngredientData;
import com.viscript_recipe.data.FluidIngredientKind;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.data.RecipeOutputData;
import com.viscript_recipe.data.vanilla.CookingRecipeData;
import com.viscript_recipe.recipe.vanilla.ViscriptShapelessRecipe;
import com.viscript_recipe.recipe.vanilla.ViscriptStonecutterRecipe;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraftforge.common.crafting.CompoundIngredient;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class CreateRecipeFactory {
    private static final ResourceLocation DUMMY_RECIPE_ID = ViScriptRecipe.id("create_processing_preview");

    private CreateRecipeFactory() {
    }

    public static Recipe<?> compileProcessing(ResourceLocation type, CreateProcessingRecipeData data) {
        var kind = CreateProcessingKind.byType(type)
                .orElseThrow(() -> new IllegalArgumentException("Unsupported Create processing recipe type: " + type));
        return switch (kind) {
            case CRUSHING -> compileStandard(kind, data, CrushingRecipe::new);
            case MILLING -> compileStandard(kind, data, MillingRecipe::new);
            case CUTTING -> compileStandard(kind, data, CuttingRecipe::new);
            case BLOCK_CUTTING -> compileBlockCutting(kind, data).get(0);
            case AUTO_PACKING -> compileAutoPacking(kind, data);
            case PRESSING -> compileStandard(kind, data, PressingRecipe::new);
            case SANDPAPER_POLISHING -> compileStandard(kind, data, SandPaperPolishingRecipe::new);
            case BLASTING -> compileCooking(kind, data, SmeltingRecipe::new);
            case SPLASHING -> compileStandard(kind, data, SplashingRecipe::new);
            case HAUNTING -> compileStandard(kind, data, HauntingRecipe::new);
            case SMOKING -> compileCooking(kind, data, SmokingRecipe::new);
            case FILLING -> compileStandard(kind, data, FillingRecipe::new);
            case EMPTYING -> compileStandard(kind, data, EmptyingRecipe::new);
            case MIXING -> compileStandard(kind, data, MixingRecipe::new);
            case AUTOMATIC_SHAPELESS -> compileAutomaticShapeless(kind, data);
            case AUTOMATIC_BREWING -> compileStandard(kind, data, MixingRecipe::new);
            case COMPACTING -> compileStandard(kind, data, CompactingRecipe::new);
            case DEPLOYING -> compileItemApplication(kind, data, DeployerApplicationRecipe::new);
            case ITEM_APPLICATION -> compileItemApplication(kind, data, ManualApplicationRecipe::new);
        };
    }

    public static Recipe<?> compileSequencedAssembly(CreateSequencedAssemblyRecipeData data) {
        var ingredient = compileIngredient(data.getIngredient());
        if (ingredient.isEmpty()) {
            throw new IllegalArgumentException("Create sequenced assembly recipe must have an ingredient");
        }
        var transitionalItem = normalizeSequencedTransitionalItem(data.getTransitionalItem());
        var outputs = compileSequencedOutputs(data);
        if (outputs.isEmpty()) {
            throw new IllegalArgumentException("Create sequenced assembly recipe must have at least one output");
        }
        var steps = safeList(data.getSequence());
        if (steps.isEmpty()) {
            throw new IllegalArgumentException("Create sequenced assembly recipe must have at least one step");
        }

        var builder = new SequencedAssemblyRecipeBuilder(DUMMY_RECIPE_ID)
                .require(ingredient)
                .transitionTo(transitionalItem.getItem())
                .loops(Math.max(1, data.getLoops()));
        for (var output : outputs) {
            builder.addOutput(output.getStack().copy(), Math.max(0.0001F, output.getChance()));
        }
        for (var step : steps) {
            addSequencedStep(builder, step);
        }
        var recipe = builder.build();
        initializeSequencedAssembly(recipe);
        return recipe;
    }

    private static void initializeSequencedAssembly(SequencedAssemblyRecipe recipe) {
        var transit = Ingredient.of(recipe.getTransitionalItem());
        var sequence = recipe.getSequence();
        for (int i = 0; i < sequence.size(); i++) {
            var wrapped = sequence.get(i).getRecipe();
            if (!sequence.get(i).getAsAssemblyRecipe().supportsAssembly() || wrapped.getIngredients().isEmpty()) {
                continue;
            }
            wrapped.getIngredients().set(0, i == 0 ? CompoundIngredient.of(transit, recipe.getIngredient()) : transit);
        }
    }

    private static void addSequencedStep(SequencedAssemblyRecipeBuilder builder, CreateSequencedAssemblyStepData step) {
        switch (step.getKind()) {
            case DEPLOYING -> builder.addStep(DeployerApplicationRecipe::new, stepBuilder -> {
                var ingredient = compileIngredient(step.getIngredient());
                if (ingredient.isEmpty()) {
                    throw new IllegalArgumentException("Create sequenced assembly deploying step must have a held item ingredient");
                }
                stepBuilder.require(ingredient);
                if (step.isKeepHeldItem()) {
                    stepBuilder.toolNotConsumed();
                }
                return stepBuilder;
            });
            case PRESSING -> builder.addStep(PressingRecipe::new, stepBuilder -> stepBuilder);
            case CUTTING -> builder.addStep(CuttingRecipe::new, stepBuilder -> {
                stepBuilder.duration(Math.max(0, step.getProcessingTime()));
                return stepBuilder;
            });
            case FILLING -> builder.addStep(FillingRecipe::new, stepBuilder -> {
                var fluidIngredient = compileFluidIngredient(step.getFluidIngredient());
                if (fluidIngredient == null || fluidIngredient.getMatchingFluidStacks().isEmpty()) {
                    throw new IllegalArgumentException("Create sequenced assembly filling step must have a fluid ingredient");
                }
                stepBuilder.require(fluidIngredient);
                return stepBuilder;
            });
        }
    }

    private static Recipe<?> compileStandard(CreateProcessingKind kind, CreateProcessingRecipeData data,
                                             ProcessingRecipeBuilder.ProcessingRecipeFactory<? extends ProcessingRecipe<?>> factory) {
        var builder = new ProcessingRecipeBuilder<>(factory, DUMMY_RECIPE_ID);
        builder.withItemIngredients(compileItemIngredients(data, kind.maxItemInputs()));
        builder.withFluidIngredients(compileFluidIngredients(data, kind.maxFluidInputs()));
        builder.withItemOutputs(compileItemOutputs(data, kind.maxItemOutputs()));
        builder.withFluidOutputs(compileFluidOutputs(data, kind.maxFluidOutputs()));
        if (kind.durationAllowed()) {
            builder.duration(Math.max(0, data.getProcessingTime()));
        }
        if (kind.heatAllowed()) {
            builder.requiresHeat(compileHeat(data.getHeatRequirement()));
        }
        validateHasInput(kind, data);
        validateHasOutput(kind, data);
        return builder.build();
    }

    private static Recipe<?> compileCooking(CreateProcessingKind kind, CreateProcessingRecipeData data,
                                            CookingRecipeData.Factory factory) {
        var ingredients = compileItemIngredients(data, kind.maxItemInputs());
        if (ingredients.isEmpty()) {
            throw new IllegalArgumentException("Create " + kind.typeId() + " recipe must have an item input");
        }
        var outputs = compileItemOutputs(data, 1);
        if (outputs.isEmpty()) {
            throw new IllegalArgumentException("Create " + kind.typeId() + " recipe must have an item output");
        }
        return factory.create(ViScriptRecipe.placeholder, "", CookingBookCategory.MISC, ingredients.get(0), outputs.get(0).getStack().copy(), 0, Math.max(1, data.getProcessingTime()));
    }

    private static List<Recipe<?>> compileBlockCutting(CreateProcessingKind kind, CreateProcessingRecipeData data) {
        var ingredients = compileItemIngredients(data, kind.maxItemInputs());
        if (ingredients.isEmpty()) {
            throw new IllegalArgumentException("Create " + kind.typeId() + " recipe must have an item input");
        }
        var outputs = compileItemOutputs(data, kind.maxItemOutputs());
        if (outputs.isEmpty()) {
            throw new IllegalArgumentException("Create " + kind.typeId() + " recipe must have an item output");
        }
        var recipes = new ArrayList<Recipe<?>>();
        for (var output : outputs) {
            recipes.add(new ViscriptStonecutterRecipe("", ingredients.get(0), output.getStack().copy(), true));
        }
        return recipes;
    }

    private static Recipe<?> compileAutoPacking(CreateProcessingKind kind, CreateProcessingRecipeData data) {
        var ingredients = compileItemIngredients(data, kind.maxItemInputs());
        if (ingredients.size() != 4 && ingredients.size() != 9) {
            throw new IllegalArgumentException("Create " + kind.typeId() + " recipe must have 4 or 9 item inputs");
        }
        if (!ItemHelper.matchAllIngredients(ingredients)) {
            throw new IllegalArgumentException("Create " + kind.typeId() + " recipe inputs must all be the same ingredient");
        }
        var outputs = compileItemOutputs(data, kind.maxItemOutputs());
        if (outputs.isEmpty()) {
            throw new IllegalArgumentException("Create " + kind.typeId() + " recipe must have an item output");
        }
        return new ViscriptShapelessRecipe("", CraftingBookCategory.MISC, outputs.get(0).getStack().copy(), ingredients, false);
    }

    private static Recipe<?> compileAutomaticShapeless(CreateProcessingKind kind, CreateProcessingRecipeData data) {
        var ingredients = compileItemIngredients(data, kind.maxItemInputs());
        if (ingredients.size() < 2) {
            throw new IllegalArgumentException("Create " + kind.typeId() + " recipe must have at least 2 item inputs");
        }
        var outputs = compileItemOutputs(data, kind.maxItemOutputs());
        if (outputs.isEmpty()) {
            throw new IllegalArgumentException("Create " + kind.typeId() + " recipe must have an item output");
        }
        return new ViscriptShapelessRecipe("", CraftingBookCategory.MISC, outputs.get(0).getStack().copy(), ingredients, false);
    }

    private static Recipe<?> compileItemApplication(CreateProcessingKind kind, CreateProcessingRecipeData data,
                                                    ProcessingRecipeBuilder.ProcessingRecipeFactory<? extends ItemApplicationRecipe> factory) {
        var builder = new ProcessingRecipeBuilder<>(factory, DUMMY_RECIPE_ID);
        builder.withItemIngredients(compileItemIngredients(data, kind.maxItemInputs()));
        builder.withItemOutputs(compileItemOutputs(data, kind.maxItemOutputs()));
        if (data.isKeepHeldItem()) {
            builder.toolNotConsumed();
        }
        validateHasInput(kind, data);
        validateHasOutput(kind, data);
        return builder.build();
    }

    private static NonNullList<Ingredient> compileItemIngredients(CreateProcessingRecipeData data, int maxCount) {
        var ingredients = NonNullList.<Ingredient>create();
        for (var ingredientData : safeList(data.getIngredients())) {
            if (ingredients.size() >= maxCount) {
                break;
            }
            var normalizedData = CreateItemInputCounts.copyWithClampedWeight(ingredientData, maxCount - ingredients.size());
            var ingredient = compileIngredient(normalizedData);
            if (!ingredient.isEmpty()) {
                var repeat = Math.max(1, CreateItemInputCounts.slotWeight(normalizedData));
                for (int i = 0; i < repeat && ingredients.size() < maxCount; i++) {
                    ingredients.add(ingredient);
                }
            }
        }
        return ingredients;
    }

    private static NonNullList<FluidIngredient> compileFluidIngredients(CreateProcessingRecipeData data, int maxCount) {
        var ingredients = NonNullList.<FluidIngredient>create();
        for (var ingredientData : safeList(data.getFluidIngredients())) {
            if (ingredients.size() >= maxCount) {
                break;
            }
            var ingredient = compileFluidIngredient(ingredientData);
            if (ingredient != null && !ingredient.getMatchingFluidStacks().isEmpty()) {
                ingredients.add(ingredient);
            }
        }
        return ingredients;
    }

    private static NonNullList<ProcessingOutput> compileItemOutputs(CreateProcessingRecipeData data, int maxCount) {
        var outputs = NonNullList.<ProcessingOutput>create();
        for (var outputData : safeList(data.getOutputs())) {
            if (outputs.size() >= maxCount) {
                break;
            }
            var stack = normalizeOutputStack(outputData);
            if (!stack.isEmpty()) {
                outputs.add(new ProcessingOutput(stack, clampChance(outputData.getChance())));
            }
        }
        return outputs;
    }

    private static NonNullList<FluidStack> compileFluidOutputs(CreateProcessingRecipeData data, int maxCount) {
        var outputs = NonNullList.<FluidStack>create();
        for (var stack : safeList(data.getFluidOutputs())) {
            if (outputs.size() >= maxCount) {
                break;
            }
            var copy = copyFluid(stack);
            if (!copy.isEmpty() && copy.getAmount() > 0) {
                outputs.add(copy);
            }
        }
        return outputs;
    }

    private static NonNullList<ProcessingOutput> compileSequencedOutputs(CreateSequencedAssemblyRecipeData data) {
        var outputs = NonNullList.<ProcessingOutput>create();
        for (var outputData : safeList(data.getOutputs())) {
            var stack = normalizeOutputStack(outputData);
            if (!stack.isEmpty()) {
                outputs.add(new ProcessingOutput(stack, Math.max(0, outputData.getChance())));
            }
        }
        return outputs;
    }

    private static Ingredient compileIngredient(RecipeIngredient ingredient) {
        return ingredient == null ? Ingredient.EMPTY : ingredient.compile();
    }

    private static FluidIngredient compileFluidIngredient(FluidIngredientData data) {
        if (data == null) {
            return null;
        }
        var kind = data.getKind() == null ? FluidIngredientKind.FLUID : data.getKind();
        if (kind == FluidIngredientKind.TAG) {
            if (data.getTag() == null) {
                throw new IllegalArgumentException("Create fluid ingredient tag cannot be empty");
            }
            return FluidIngredient.fromTag(TagKey.create(Registries.FLUID, data.getTag()), Math.max(1, data.getAmount()));
        }
        var stack = copyFluid(data.getFluid());
        if (stack.isEmpty() || stack.getFluid() == BuiltInRegistries.FLUID.get(new ResourceLocation("empty"))) {
            return null;
        }
        return FluidIngredient.fromFluidStack(stack.copy());
    }

    private static HeatCondition compileHeat(CreateHeatCondition condition) {
        return switch (condition == null ? CreateHeatCondition.NONE : condition) {
            case NONE -> HeatCondition.NONE;
            case HEATED -> HeatCondition.HEATED;
            case SUPERHEATED -> HeatCondition.SUPERHEATED;
        };
    }

    private static void validateHasInput(CreateProcessingKind kind, CreateProcessingRecipeData data) {
        if (!compileItemIngredients(data, kind.maxItemInputs()).isEmpty()) {
            return;
        }
        if (!compileFluidIngredients(data, kind.maxFluidInputs()).isEmpty()) {
            return;
        }
        throw new IllegalArgumentException("Create " + kind.typeId() + " recipe must have at least one input");
    }

    private static void validateHasOutput(CreateProcessingKind kind, CreateProcessingRecipeData data) {
        if (!compileItemOutputs(data, kind.maxItemOutputs()).isEmpty()) {
            return;
        }
        if (!compileFluidOutputs(data, kind.maxFluidOutputs()).isEmpty()) {
            return;
        }
        throw new IllegalArgumentException("Create " + kind.typeId() + " recipe must have at least one output");
    }

    private static ItemStack normalizeOutputStack(RecipeOutputData outputData) {
        if (outputData == null || outputData.getItem() == null || outputData.getItem().isEmpty() || outputData.getItem().is(Items.AIR)) {
            return ItemStack.EMPTY;
        }
        var stack = outputData.getItem().copy();
        stack.setCount(Clamp.clamp(stack.getCount(), 1, 99));
        return stack;
    }

    private static ItemStack normalizeSequencedTransitionalItem(ItemStack stack) {
        var copy = stack == null ? ItemStack.EMPTY : stack.copy();
        if (copy.isEmpty() || copy.is(Items.AIR)) {
            throw new IllegalArgumentException("Create sequenced assembly recipe must have a transitional item");
        }
        copy.setCount(1);
        return copy;
    }

    private static float clampChance(float chance) {
        return Clamp.clamp(chance, 0, 1);
    }

    private static FluidStack copyFluid(FluidStack stack) {
        return stack == null ? FluidStack.EMPTY : stack.copy();
    }

    private static <T> List<T> safeList(List<T> list) {
        return list == null ? List.of() : list.stream().filter(Objects::nonNull).toList();
    }
}
