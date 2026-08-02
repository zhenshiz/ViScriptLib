package com.viscript_recipe.gui.editor;

import com.viscript_recipe.data.RecipeEditorTypes;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.data.RecipeOutputData;
import com.viscript_recipe.data.create.*;
import com.viscript_recipe.data.farmersdelight.FarmerCuttingRecipeData;
import com.viscript_recipe.data.farmersdelight.FarmersDelightRecipeEditorTypes;
import com.viscript_recipe.data.vanilla.ShapedKeyEntry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

import static com.viscript_recipe.gui.canvas.RecipeCanvas.itemFromRegistry;

public final class RecipeDefaultDataInitializer {
    private RecipeDefaultDataInitializer() {
    }

    public static void apply(RecipeEntry entry, ResourceLocation type) {
        if (type.equals(RecipeEditorTypes.BLASTING)) {
            entry.getCooking().setCookingTime(100);
        } else if (type.equals(RecipeEditorTypes.SMOKING)) {
            entry.getCooking().setCookingTime(100);
        } else if (type.equals(RecipeEditorTypes.CAMPFIRE_COOKING)) {
            entry.getCooking().setCookingTime(600);
        } else if (type.equals(RecipeEditorTypes.SMELTING)) {
            entry.getCooking().setCookingTime(200);
        } else if (type.equals(FarmersDelightRecipeEditorTypes.COOKING)) {
            entry.getFarmerCookingPot()
                    .setIngredients(new ArrayList<>(List.of(
                            RecipeIngredient.item(Items.BEEF),
                            RecipeIngredient.item(Items.CARROT),
                            RecipeIngredient.item(Items.POTATO)
                    )))
                    .setResult(new ItemStack(itemFromRegistry("farmersdelight:beef_stew", Items.RABBIT_STEW)))
                    .setContainer(new ItemStack(Items.BOWL))
                    .setExperience(1.0F)
                    .setCookingTime(200);
        } else if (type.equals(FarmersDelightRecipeEditorTypes.CUTTING)) {
            entry.getFarmerCuttingBoard()
                    .setInput(RecipeIngredient.item(Items.BEEF))
                    .setTool(FarmerCuttingRecipeData.defaultKnifeTool())
                    .setResults(new ArrayList<>(List.of(RecipeOutputData.of(new ItemStack(itemFromRegistry("farmersdelight:minced_beef", Items.BEEF))))))
                    .setCustomSound(false)
                    .setSound(new ResourceLocation("item.axe.strip"));
        } else if (type.equals(RecipeEditorTypes.CREATE_MECHANICAL_CRAFTING)) {
            applyCreateMechanicalCrafting(entry.getCreateMechanicalCrafting());
        } else if (type.equals(RecipeEditorTypes.CREATE_SEQUENCED_ASSEMBLY)) {
            applyCreateSequencedAssembly(entry.getCreateSequencedAssembly());
        } else CreateProcessingKind.byType(type).ifPresent(kind -> applyCreateProcessing(entry.getCreateProcessing(), kind));
    }

    private static void applyCreateMechanicalCrafting(CreateMechanicalCraftingRecipeData data) {
        data.setWidth(3)
                .setHeight(3)
                .setAcceptMirrored(true)
                .setPattern(new ArrayList<>(List.of(
                        "AAA",
                        "A A",
                        "AAA"
                )))
                .setKey(new ArrayList<>(List.of(
                        ShapedKeyEntry.of("A", RecipeIngredient.item(itemFromRegistry("create:brass_ingot", Items.IRON_INGOT)))
                )))
                .setResult(new ItemStack(itemFromRegistry("create:mechanical_crafter", Items.CRAFTING_TABLE)));
    }

    private static void applyCreateSequencedAssembly(CreateSequencedAssemblyRecipeData data) {
        data.setIngredient(RecipeIngredient.item(itemFromRegistry("create:golden_sheet", Items.GOLD_INGOT)))
                .setTransitionalItem(new ItemStack(itemFromRegistry("create:incomplete_precision_mechanism", Items.CLOCK)))
                .setLoops(5)
                .setSequence(new ArrayList<>(List.of(
                        createSequencedDeployingStep("create:cogwheel", Items.IRON_NUGGET),
                        createSequencedDeployingStep("create:large_cogwheel", Items.IRON_NUGGET),
                        createSequencedDeployingStep("minecraft:iron_nugget", Items.IRON_NUGGET)
                )))
                .setOutputs(new ArrayList<>(List.of(RecipeOutputData.of(new ItemStack(itemFromRegistry("create:precision_mechanism", Items.CLOCK))))));
    }

    private static CreateSequencedAssemblyStepData createSequencedDeployingStep(String itemId, Item fallback) {
        return new CreateSequencedAssemblyStepData()
                .setKind(CreateSequencedAssemblyStepKind.DEPLOYING)
                .setIngredient(RecipeIngredient.item(itemFromRegistry(itemId, fallback)));
    }

    private static void applyCreateProcessing(CreateProcessingRecipeData data, CreateProcessingKind kind) {
        var defaultIngredients = new ArrayList<RecipeIngredient>();
        var defaultInputCount = switch (kind) {
            case AUTO_PACKING -> 9;
            case AUTOMATIC_SHAPELESS -> 2;
            default -> 1;
        };
        for (int i = 0; i < defaultInputCount; i++) {
            defaultIngredients.add(RecipeIngredient.item(kind.defaultInput()));
        }
        data.setIngredients(defaultIngredients);
        data.setFluidIngredients(new ArrayList<>());
        data.setOutputs(kind.maxItemOutputs() > 0
                ? new ArrayList<>(List.of(RecipeOutputData.of(new ItemStack(kind.defaultOutput()))))
                : new ArrayList<>());
        data.setFluidOutputs(new ArrayList<>());
        data.setProcessingTime(kind.durationAllowed() ? 100 : 0);
        data.setHeatRequirement(kind == CreateProcessingKind.AUTOMATIC_BREWING ? CreateHeatCondition.HEATED : CreateHeatCondition.NONE);
        data.setKeepHeldItem(false);
        if (kind.maxFluidInputs() > 0) {
            data.getFluidIngredients().add(FluidIngredientData.fluid(new FluidStack(Fluids.WATER, 1000)));
        }
        if (kind.maxFluidOutputs() > 0 && kind.maxItemOutputs() == 1 && kind == CreateProcessingKind.EMPTYING) {
            data.getFluidOutputs().add(new FluidStack(Fluids.WATER, 250));
        } else if (kind.maxFluidOutputs() > 0 && kind == CreateProcessingKind.AUTOMATIC_BREWING) {
            data.getFluidOutputs().add(new FluidStack(Fluids.WATER, 1000));
        }
    }
}
