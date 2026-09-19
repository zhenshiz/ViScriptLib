package com.viscript_recipe.compat.mekanism;

import com.viscript_recipe.compat.mekanism.data.*;
import com.viscript_recipe.data.FluidIngredientData;
import com.viscript_recipe.recipe.importer.RecipeImportException;
import com.viscript_recipe.recipe.importer.RecipeImportHandler;
import com.viscript_recipe.recipe.importer.RecipeImportResult;
import com.viscript_recipe.recipe.importer.RecipeImporter;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.recipes.*;
import mekanism.api.recipes.chemical.*;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.recipe.ingredient.chemical.SingleChemicalStackIngredient;
import mekanism.common.recipe.ingredient.chemical.TaggedChemicalStackIngredient;
import mekanism.common.recipe.ingredient.creator.FluidStackIngredientCreator;
import mekanism.common.recipe.ingredient.creator.ItemStackIngredientCreator;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Imports native Mekanism recipe objects into the shared typed editor model.
 */
public final class MekanismRecipeImporter implements RecipeImportHandler {
    public static final MekanismRecipeImporter INSTANCE = new MekanismRecipeImporter();

    private MekanismRecipeImporter() {
    }

    @Override
    public boolean canImport(Recipe<?> holder) {
        return holder != null && kind(holder) != null;
    }

    @Override
    public RecipeImportResult tryImport(Recipe<?> recipe, HolderLookup.Provider provider) throws RecipeImportException {
        var kind = kind(recipe);
        if (kind == null) {
            return null;
        }
        if (recipe instanceof RotaryRecipe rotaryRecipe) {
            return importRotary(rotaryRecipe);
        }
        var data = new MekanismRecipeData();

        switch (kind) {
            case CRUSHING, ENRICHING, SMELTING -> {
                var typed = (ItemStackToItemStackRecipe) recipe;
                importItemInput(data, typed.getInput(), false);
                data.setItemOutput(copyFirstItem(typed.getOutputDefinition()));
            }
            case CHEMICAL_INFUSING, PIGMENT_MIXING -> {
                var typed = (ChemicalChemicalToChemicalRecipe) recipe;
                data.setChemicalInput(importChemicalInput(typed.getLeftInput()))
                        .setExtraChemicalInput(importChemicalInput(typed.getRightInput()))
                        .setChemicalOutput(copyFirstChemical(typed.getOutputDefinition()));
            }
            case COMBINING -> {
                var typed = (CombinerRecipe) recipe;
                importItemInput(data, typed.getMainInput(), false);
                importItemInput(data, typed.getExtraInput(), true);
                data.setItemOutput(copyFirstItem(typed.getOutputDefinition()));
            }
            case SEPARATING -> {
                var typed = (ElectrolysisRecipe) recipe;
                var output = typed.getOutputDefinition().get(0);
                data.setFluidInput(importFluidInput(typed.getInput()))
                        .setEnergyMultiplier(Math.max(1, typed.getEnergyMultiplier().getValue()))
                        .setChemicalOutput(copyChemical(output.left()))
                        .setSecondaryChemicalOutput(copyChemical(output.right()));
            }
            case WASHING -> {
                var typed = (FluidChemicalToChemicalRecipe) recipe;
                data.setFluidInput(importFluidInput(typed.getFluidInput()))
                        .setChemicalInput(importChemicalInput(typed.getChemicalInput()))
                        .setChemicalOutput(copyFirstChemical(typed.getOutputDefinition()));
            }
            case EVAPORATING -> {
                var typed = (FluidToFluidRecipe) recipe;
                data.setFluidInput(importFluidInput(typed.getInput()))
                        .setFluidOutput(typed.getOutputDefinition().get(0).copy());
            }
            case ACTIVATING, CENTRIFUGING -> {
                var typed = (ChemicalToChemicalRecipe) recipe;
                data.setChemicalInput(importChemicalInput(typed.getInput()))
                        .setChemicalOutput(copyFirstChemical(typed.getOutputDefinition()));
            }
            case CRYSTALLIZING -> {
                var typed = (ChemicalCrystallizerRecipe) recipe;
                data.setChemicalInput(importChemicalInput(typed.getInput()))
                        .setItemOutput(copyFirstItem(typed.getOutputDefinition()));
            }
            case DISSOLUTION -> {
                var typed = (ChemicalDissolutionRecipe) recipe;
                importItemInput(data, typed.getItemInput(), false);
                data.setChemicalInput(importChemicalInput(typed.getGasInput()))
                        .setChemicalOutput(copyFirstChemical(List.of(typed.getOutputDefinition().get(0).getChemicalStack())));
            }
            case COMPRESSING, PURIFYING, INJECTING, METALLURGIC_INFUSING, PAINTING -> {
                var typed = (ItemStackChemicalToItemStackRecipe) recipe;
                importItemInput(data, typed.getItemInput(), false);
                data.setChemicalInput(importChemicalInput(typed.getChemicalInput()))
                        .setItemOutput(copyFirstItem(typed.getOutputDefinition()));
            }
            case NUCLEOSYNTHESIZING -> {
                var typed = (NucleosynthesizingRecipe) recipe;
                importItemInput(data, typed.getItemInput(), false);
                data.setChemicalInput(importChemicalInput(typed.getChemicalInput()))
                        .setItemOutput(copyFirstItem(typed.getOutputDefinition()))
                        .setDuration(Math.max(1, typed.getDuration()));
            }
            case ENERGY_CONVERSION -> {
                var typed = (ItemStackToEnergyRecipe) recipe;
                importItemInput(data, typed.getInput(), false);
                data.setEnergyOutput(Math.max(1, typed.getOutputDefinition().get(0).getValue()));
            }
            case GAS_CONVERSION, INFUSION_CONVERSION, OXIDIZING, PIGMENT_EXTRACTING -> {
                var typed = (ItemStackToChemicalRecipe) recipe;
                importItemInput(data, typed.getInput(), false);
                data.setChemicalOutput(copyFirstChemical(typed.getOutputDefinition()));
            }
            case REACTION -> {
                var typed = (PressurizedReactionRecipe) recipe;
                var output = typed.getOutputDefinition().get(0);
                importItemInput(data, typed.getInputSolid(), false);
                data.setFluidInput(importFluidInput(typed.getInputFluid()))
                        .setChemicalInput(importChemicalInput(typed.getInputGas()))
                        .setEnergyRequired(Math.max(0, typed.getEnergyRequired().getValue()))
                        .setDuration(Math.max(1, typed.getDuration()))
                        .setItemOutput(output.item().copy())
                        .setChemicalOutput(copyChemical(output.gas()));
            }
            case CONDENSENTRATING, DECONDENSENTRATING -> throw new IllegalStateException(
                    "Rotary recipes must be imported before the serializer switch"
            );
            case SAWING -> {
                var typed = (SawmillRecipe) recipe;
                importItemInput(data, typed.getInput(), false);
                data.setItemOutput(copyFirstItemOrEmpty(typed.getMainOutputDefinition()))
                        .setSecondaryItemOutput(copyFirstItemOrEmpty(typed.getSecondaryOutputDefinition()))
                        .setSecondaryChance((float) typed.getSecondaryChance());
            }
        }
        return RecipeImporter.success(RecipeImporter.baseEntry(recipe.getId(), kind.typeId()).setData(data));
    }

    private static RecipeImportResult importRotary(RotaryRecipe recipe)
            throws RecipeImportException {
        var entries = new ArrayList<com.viscript_recipe.data.RecipeEntry>(2);
        boolean split = recipe.hasGasToFluid() && recipe.hasFluidToGas();
        if (recipe.hasGasToFluid()) {
            var data = new MekanismRecipeData()
                    .setChemicalInput(importChemicalInput(recipe.getGasInput()))
                    .setFluidOutput(recipe.getFluidOutputDefinition().get(0).copy());
            var id = split ? splitRotaryId(recipe.getId(), "condensentrating") : recipe.getId();
            entries.add(RecipeImporter.baseEntry(id, MekanismRecipeKind.CONDENSENTRATING.typeId())
                    .setData(data));
        }
        if (recipe.hasFluidToGas()) {
            var data = new MekanismRecipeData()
                    .setFluidInput(importFluidInput(recipe.getFluidInput()))
                    .setChemicalOutput(copyFirstChemical(List.of(recipe.getGasOutputDefinition().get(0))));
            var id = split ? splitRotaryId(recipe.getId(), "decondensentrating") : recipe.getId();
            entries.add(RecipeImporter.baseEntry(id, MekanismRecipeKind.DECONDENSENTRATING.typeId())
                    .setData(data));
        }
        if (entries.isEmpty()) {
            throw new RecipeImportException("viscript_recipe.editor.import_recipe.error.empty_result");
        }
        if (!split) {
            return RecipeImporter.success(entries.get(0));
        }
        return RecipeImportResult.success(entries, Component.translatable(
                "viscript_recipe.editor.import_recipe.success.mekanism_rotary_split",
                recipe.getId().toString()
        ));
    }

    private static ResourceLocation splitRotaryId(ResourceLocation id, String direction) {
        return new ResourceLocation(id.getNamespace(), id.getPath() + '_' + direction);
    }

    private static void importItemInput(MekanismRecipeData data, ItemStackIngredient input, boolean extra) throws RecipeImportException {
        if (input instanceof ItemStackIngredientCreator.SingleItemStackIngredient single) {
            var amount = Math.max(1, single.getAmountRaw());
            var imported = RecipeImporter.importIngredient(single.getInputRaw()).setCount(amount);
            if (extra) {
                data.setExtraItemInput(imported);
            } else {
                data.setItemInput(imported);
            }
        } else throw new IllegalArgumentException("Unsupported ItemStackIngredient");
    }

    private static FluidIngredientData importFluidInput(FluidStackIngredient input) throws RecipeImportException {
        if (input instanceof FluidStackIngredientCreator.SingleFluidStackIngredient single) {
            FluidStack raw = single.getInputRaw();
            return FluidIngredientData.fluid(raw).setAmount(raw.getAmount());
        }
        if (input instanceof FluidStackIngredientCreator.TaggedFluidStackIngredient tag) {
            return FluidIngredientData.tag(tag.getTag().location()).setAmount(Math.max(1, tag.getRawAmount()));
        }
        throw new RecipeImportException("viscript_recipe.editor.import_recipe.error.mekanism_unsupported_fluid_ingredient");
    }

    private static MekanismChemicalIngredientData importChemicalInput(ChemicalStackIngredient<?,?> input) throws RecipeImportException {
        if (input instanceof SingleChemicalStackIngredient<?,?> single) {
            return new MekanismChemicalIngredientData()
                    .setChemicalType(ChemicalType.getTypeFor(single))
                    .setKind(MekanismChemicalIngredientKind.CHEMICAL)
                    .setChemical(single.getInputRaw().getRegistryName())
                    .setAmount(Math.max(1, input.getRepresentations().get(0).getAmount()));
        }
        if (input instanceof TaggedChemicalStackIngredient<?,?> tag) {
            var serialize = tag.serialize().getAsJsonObject();
            return new MekanismChemicalIngredientData()
                    .setChemicalType(ChemicalType.getTypeFor(tag))
                    .setKind(MekanismChemicalIngredientKind.TAG)
                    .setTag(new ResourceLocation(serialize.get("tag").getAsString()))
                    .setAmount(Math.max(1, serialize.get("amount").getAsLong()));
        }
        throw new RecipeImportException("viscript_recipe.editor.import_recipe.error.mekanism_unsupported_chemical_ingredient");
    }

    private static MekanismChemicalStackData copyFirstChemical(java.util.List<ChemicalStack<?>> stacks) throws RecipeImportException {
        if (stacks == null || stacks.isEmpty()) {
            throw new RecipeImportException("viscript_recipe.editor.import_recipe.error.empty_result");
        }
        return copyChemical(stacks.get(0));
    }

    private static MekanismChemicalStackData copyChemical(ChemicalStack<?> stack) {
        if (stack == null || stack.isEmpty()) {
            return MekanismChemicalStackData.empty();
        }
        return new MekanismChemicalStackData()
                .setChemicalType(ChemicalType.getTypeFor(stack))
                .setChemical(stack.getRaw().getRegistryName())
                .setAmount(stack.getAmount());
    }

    private static ItemStack copyFirstItem(java.util.List<ItemStack> stacks) throws RecipeImportException {
        var result = copyFirstItemOrEmpty(stacks);
        if (result.isEmpty()) {
            throw new RecipeImportException("viscript_recipe.editor.import_recipe.error.empty_result");
        }
        return result;
    }

    private static ItemStack copyFirstItemOrEmpty(java.util.List<ItemStack> stacks) {
        return stacks == null || stacks.isEmpty() ? ItemStack.EMPTY : stacks.get(0).copy();
    }

    private static MekanismRecipeKind kind(Recipe<?> holder) {
        if (holder instanceof RotaryRecipe) {
            return MekanismRecipeKind.CONDENSENTRATING;
        }
        var serializer = BuiltInRegistries.RECIPE_SERIALIZER.getKey(holder.getSerializer());
        return MekanismRecipeKind.byType(serializer).orElse(null);
    }
}
