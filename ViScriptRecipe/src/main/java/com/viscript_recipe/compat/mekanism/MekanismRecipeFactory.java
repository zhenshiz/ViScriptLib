package com.viscript_recipe.compat.mekanism;

import com.viscript_recipe.ViScriptRecipe;
import com.viscript_recipe.compat.mekanism.data.*;
import com.viscript_recipe.data.FluidIngredientData;
import com.viscript_recipe.data.FluidIngredientKind;
import com.viscript_recipe.data.RecipeIngredient;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalTags;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.api.recipes.ingredients.creator.IChemicalStackIngredientCreator;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.recipe.impl.*;
import mekanism.common.recipe.ingredient.creator.GasStackIngredientCreator;
import mekanism.common.recipe.ingredient.creator.InfusionStackIngredientCreator;
import mekanism.common.recipe.ingredient.creator.PigmentStackIngredientCreator;
import mekanism.common.recipe.ingredient.creator.SlurryStackIngredientCreator;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;

/**
 * Compiles editor data into Mekanism's native basic recipe implementations.
 */
public final class MekanismRecipeFactory {
    private MekanismRecipeFactory() {
    }

    /**
     * Compiles a recipe using the serializer represented by {@code type}.
     *
     * @param  type the Mekanism recipe serializer identifier
     * @param  data the editable recipe data
     * @return the native Mekanism recipe
     * @throws IllegalArgumentException if required input, output, amount, or registry data is invalid
     */
    @SuppressWarnings("all")
    public static Recipe<?> compile(ResourceLocation type, MekanismRecipeData data) {
        var kind = MekanismRecipeKind.byType(type)
                .orElseThrow(() -> new IllegalArgumentException("Unsupported Mekanism recipe type: " + type));
        var itemInput = kind.itemInputs() > 0 ? itemIngredient(data.getItemInput(), data.getItemInput().getCount(), "item input") : null;
        var extraItemInput = kind.itemInputs() > 1 ? itemIngredient(data.getExtraItemInput(), data.getExtraItemInput().getCount(), "extra item input") : null;
        var fluidInput = kind.fluidInputs() > 0 ? fluidIngredient(data.getFluidInput()) : null;
        var chemicalInput = kind.chemicalInputs() > 0 ? chemicalIngredient(data.getChemicalInput(), "chemical input") : null;
        var extraChemicalInput = kind.chemicalInputs() > 1 ? chemicalIngredient(data.getExtraChemicalInput(), "extra chemical input") : null;

        return switch (kind) {
            case ACTIVATING -> new ActivatingIRecipe(ViScriptRecipe.placeholder, (ChemicalStackIngredient.GasStackIngredient) chemicalInput, (GasStack) chemicalOutput(data.getChemicalOutput(), false));
            case CENTRIFUGING -> new CentrifugingIRecipe(ViScriptRecipe.placeholder, (ChemicalStackIngredient.GasStackIngredient) chemicalInput, (GasStack) chemicalOutput(data.getChemicalOutput(), false));
            case CRYSTALLIZING -> new ChemicalCrystallizerIRecipe(ViScriptRecipe.placeholder, chemicalInput, itemOutput(data));
            case DISSOLUTION -> new ChemicalDissolutionIRecipe(ViScriptRecipe.placeholder, itemInput, (ChemicalStackIngredient.GasStackIngredient) chemicalInput, chemicalOutput(data.getChemicalOutput(), false));
            case CHEMICAL_INFUSING -> new ChemicalInfuserIRecipe(ViScriptRecipe.placeholder, (ChemicalStackIngredient.GasStackIngredient) chemicalInput, (ChemicalStackIngredient.GasStackIngredient) extraChemicalInput, (GasStack) chemicalOutput(data.getChemicalOutput(), false));
            case OXIDIZING -> new ChemicalOxidizerIRecipe(ViScriptRecipe.placeholder, itemInput, (GasStack) chemicalOutput(data.getChemicalOutput(), false));
            case COMBINING -> new CombinerIRecipe(ViScriptRecipe.placeholder, itemInput, extraItemInput, itemOutput(data));
            case COMPRESSING -> new CompressingIRecipe(ViScriptRecipe.placeholder, itemInput, (ChemicalStackIngredient.GasStackIngredient) chemicalInput, itemOutput(data));
            case CRUSHING -> new CrushingIRecipe(ViScriptRecipe.placeholder, itemInput, itemOutput(data));
            case SEPARATING -> new ElectrolysisIRecipe(ViScriptRecipe.placeholder, fluidInput, FloatingLong.create(positive(data.getEnergyMultiplier(), "energy multiplier")), (GasStack) chemicalOutput(data.getChemicalOutput(), false), (GasStack) chemicalOutput(data.getSecondaryChemicalOutput(), false));
            case ENERGY_CONVERSION -> new EnergyConversionIRecipe(ViScriptRecipe.placeholder, itemInput, FloatingLong.create(positive(data.getEnergyOutput(), "energy output")));
            case ENRICHING -> new EnrichingIRecipe(ViScriptRecipe.placeholder, itemInput, itemOutput(data));
            case WASHING -> new FluidSlurryToSlurryIRecipe(ViScriptRecipe.placeholder, fluidInput, (ChemicalStackIngredient.SlurryStackIngredient) chemicalInput, (SlurryStack) chemicalOutput(data.getChemicalOutput(), false));
            case EVAPORATING -> new FluidToFluidIRecipe(ViScriptRecipe.placeholder, fluidInput, fluidOutput(data));
            case GAS_CONVERSION -> new GasConversionIRecipe(ViScriptRecipe.placeholder, itemInput, (GasStack) chemicalOutput(data.getChemicalOutput(), false));
            case INFUSION_CONVERSION -> new InfusionConversionIRecipe(ViScriptRecipe.placeholder, itemInput, (InfusionStack) chemicalOutput(data.getChemicalOutput(), false));
            case INJECTING -> new InjectingIRecipe(ViScriptRecipe.placeholder, itemInput, (ChemicalStackIngredient.GasStackIngredient) chemicalInput, itemOutput(data));
            case METALLURGIC_INFUSING -> new MetallurgicInfuserIRecipe(ViScriptRecipe.placeholder, itemInput, (ChemicalStackIngredient.InfusionStackIngredient) chemicalInput, itemOutput(data));
            case NUCLEOSYNTHESIZING -> new NucleosynthesizingIRecipe(ViScriptRecipe.placeholder, itemInput, (ChemicalStackIngredient.GasStackIngredient) chemicalInput, itemOutput(data), positive(data.getDuration(), "duration"));

            case PAINTING -> new PaintingIRecipe(ViScriptRecipe.placeholder, itemInput, (ChemicalStackIngredient.PigmentStackIngredient) chemicalInput, itemOutput(data));
            case PIGMENT_EXTRACTING -> new PigmentExtractingIRecipe(ViScriptRecipe.placeholder, itemInput, (PigmentStack) chemicalOutput(data.getChemicalOutput(), false));
            case PIGMENT_MIXING -> new PigmentMixingIRecipe(ViScriptRecipe.placeholder, (ChemicalStackIngredient.PigmentStackIngredient) chemicalInput, (ChemicalStackIngredient.PigmentStackIngredient) extraChemicalInput, (PigmentStack) chemicalOutput(data.getChemicalOutput(), false));
            case REACTION -> new PressurizedReactionIRecipe(ViScriptRecipe.placeholder, itemInput, fluidInput, (ChemicalStackIngredient.GasStackIngredient) chemicalInput, FloatingLong.create(positive(data.getEnergyRequired(), "energy required")), positive(data.getDuration(), "duration"), optionalItemOutput(data.getItemOutput()), (GasStack) chemicalOutput(data.getChemicalOutput(), true));
            case PURIFYING -> new PurifyingIRecipe(ViScriptRecipe.placeholder, itemInput, (ChemicalStackIngredient.GasStackIngredient) chemicalInput, itemOutput(data));
            case CONDENSENTRATING -> new RotaryIRecipe(ViScriptRecipe.placeholder, (ChemicalStackIngredient.GasStackIngredient) chemicalInput, fluidOutput(data));
            case DECONDENSENTRATING -> new RotaryIRecipe(ViScriptRecipe.placeholder, fluidInput,
                    (GasStack) chemicalOutput(data.getChemicalOutput(), false));
            case SAWING -> new SawmillIRecipe(ViScriptRecipe.placeholder, itemInput, optionalItemOutput(data.getItemOutput()),
                    optionalItemOutput(data.getSecondaryItemOutput()), sawmillChance(data));
            case SMELTING -> new SmeltingIRecipe(ViScriptRecipe.placeholder, itemInput, itemOutput(data));
        };
    }

    private static ItemStackIngredient itemIngredient(RecipeIngredient data, int amount, String field) {
        var ingredient = data == null ? Ingredient.EMPTY : data.compile();
        if (ingredient.isEmpty()) {
            throw new IllegalArgumentException("Mekanism " + field + " cannot be empty");
        }
        return IngredientCreatorAccess.item().from(ingredient, positive(amount, field + " amount"));
    }

    private static FluidStackIngredient fluidIngredient(FluidIngredientData data) {
        if (data == null) {
            throw new IllegalArgumentException("Mekanism fluid input cannot be empty");
        }
        var amount = positive(data.getKind() == FluidIngredientKind.FLUID && data.getFluid() != null
                ? data.getFluid().getAmount() : data.getAmount(), "fluid input amount");
        if (data.getKind() == FluidIngredientKind.TAG) {
            var id = requireId(data.getTag(), "fluid tag");
            return IngredientCreatorAccess.fluid().from(TagKey.create(Registries.FLUID, id), amount);
        }
        var stack = data.getFluid();
        if (stack == null || stack.isEmpty() || stack.getFluid() == Fluids.EMPTY) {
            throw new IllegalArgumentException("Mekanism fluid input cannot be empty");
        }
        FluidStack copy = stack.copy();
        copy.setAmount(amount);
        return IngredientCreatorAccess.fluid().from(copy);
    }

    static IChemicalStackIngredientCreator<?,?,?> getIngredientCreator(ChemicalType type) {
        return switch (type) {
            case GAS -> IngredientCreatorAccess.gas();
            case INFUSION -> IngredientCreatorAccess.infusion();
            case PIGMENT -> IngredientCreatorAccess.pigment();
            case SLURRY -> IngredientCreatorAccess.slurry();
        };
    }

    @SuppressWarnings("all")
    public static ChemicalStackIngredient<?,?> chemicalIngredient(MekanismChemicalIngredientData data, String field) {
        if (data == null) {
            throw new IllegalArgumentException("Mekanism " + field + " cannot be empty");
        }
        var amount = positive(data.getAmount(), field + " amount");
        var type = data.getChemicalType();
        var creator = getIngredientCreator(type);
        if (data.getKind() == MekanismChemicalIngredientKind.TAG) {
            ResourceLocation tag = requireId(data.getTag(), field + " tag");
            return switch (type) {
                case GAS -> ((GasStackIngredientCreator) creator).from(ChemicalTags.GAS.tag(tag), amount);
                case INFUSION -> ((InfusionStackIngredientCreator) creator).from(ChemicalTags.INFUSE_TYPE.tag(tag), amount);
                case PIGMENT -> ((PigmentStackIngredientCreator) creator).from(ChemicalTags.PIGMENT.tag(tag), amount);
                case SLURRY -> ((SlurryStackIngredientCreator) creator).from(ChemicalTags.SLURRY.tag(tag), amount);
            };
        }
        ResourceLocation id = data.getChemical();
        return switch (type) {
            case GAS -> ((GasStackIngredientCreator) creator).from(MekanismAPI.gasRegistry().getValue(id), amount);
            case INFUSION -> ((InfusionStackIngredientCreator) creator).from(MekanismAPI.infuseTypeRegistry().getValue(id), amount);
            case PIGMENT -> ((PigmentStackIngredientCreator) creator).from(MekanismAPI.pigmentRegistry().getValue(id), amount);
            case SLURRY -> ((SlurryStackIngredientCreator) creator).from(MekanismAPI.slurryRegistry().getValue(id), amount);
        };
    }

    @SuppressWarnings("all")
    private static ChemicalStack<?> chemicalOutput(MekanismChemicalStackData data, boolean allowEmpty) {
        if (data == null || data.isEmpty()) {
            if (allowEmpty) {
                return GasStack.EMPTY;
            }
            throw new IllegalArgumentException("Mekanism chemical output cannot be empty");
        }
        ResourceLocation id = data.getChemical();
        long amount = positive(data.getAmount(), "chemical output amount");
        return switch (data.getChemicalType()) {
            case GAS -> new GasStack(MekanismAPI.gasRegistry().getValue(id), amount);
            case INFUSION -> new InfusionStack(MekanismAPI.infuseTypeRegistry().getValue(id), amount);
            case PIGMENT -> new PigmentStack(MekanismAPI.pigmentRegistry().getValue(id), amount);
            case SLURRY -> new SlurryStack(MekanismAPI.slurryRegistry().getValue(id), amount);
        };
    }

    private static ItemStack itemOutput(MekanismRecipeData data) {
        var stack = optionalItemOutput(data.getItemOutput());
        if (stack.isEmpty()) {
            throw new IllegalArgumentException("Mekanism item output cannot be empty");
        }
        return stack;
    }

    private static ItemStack optionalItemOutput(ItemStack stack) {
        return stack == null ? ItemStack.EMPTY : stack.copy();
    }

    private static FluidStack fluidOutput(MekanismRecipeData data) {
        var stack = data.getFluidOutput();
        if (stack == null || stack.isEmpty() || stack.getFluid() == Fluids.EMPTY) {
            throw new IllegalArgumentException("Mekanism fluid output cannot be empty");
        }
        return stack.copy();
    }

    private static double sawmillChance(MekanismRecipeData data) {
        var secondary = optionalItemOutput(data.getSecondaryItemOutput());
        if (secondary.isEmpty()) {
            if (optionalItemOutput(data.getItemOutput()).isEmpty()) {
                throw new IllegalArgumentException("Mekanism sawmill requires at least one output");
            }
            return 0;
        }
        var chance = data.getSecondaryChance();
        if (chance <= 0 || chance > 1) {
            throw new IllegalArgumentException("Mekanism sawmill secondary chance must be greater than 0 and at most 1");
        }
        return chance;
    }

    private static ResourceLocation requireId(ResourceLocation id, String field) {
        if (id == null) {
            throw new IllegalArgumentException("Mekanism " + field + " cannot be empty");
        }
        return id;
    }

    private static int positive(int value, String field) {
        if (value <= 0) {
            throw new IllegalArgumentException("Mekanism " + field + " must be positive");
        }
        return value;
    }

    private static long positive(long value, String field) {
        if (value <= 0) {
            throw new IllegalArgumentException("Mekanism " + field + " must be positive");
        }
        return value;
    }

    private static long nonNegative(long value, String field) {
        if (value < 0) {
            throw new IllegalArgumentException("Mekanism " + field + " must not be negative");
        }
        return value;
    }
}
