package com.viscript_recipe.compat.create.data;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.compat.create.CreateRecipeFactory;
import com.viscript_recipe.data.FluidIngredientData;
import com.viscript_recipe.data.IVSRecipeData;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.data.RecipeOutputData;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class CreateProcessingRecipeData implements IVSRecipeData {
    @Persisted
    private List<RecipeIngredient> ingredients = new ArrayList<>(List.of(RecipeIngredient.item(Items.COBBLESTONE)));
    @Persisted
    private List<FluidIngredientData> fluidIngredients = new ArrayList<>();
    @Persisted
    private List<RecipeOutputData> outputs = new ArrayList<>(List.of(RecipeOutputData.of(new ItemStack(Items.GRAVEL))));
    @Persisted
    private List<FluidStack> fluidOutputs = new ArrayList<>();
    @Persisted
    private int processingTime = 100;
    @Persisted
    private CreateHeatCondition heatRequirement = CreateHeatCondition.NONE;
    @Persisted
    private boolean keepHeldItem;

    @Override
    public Recipe<?> compile(ResourceLocation type) {
        return CreateRecipeFactory.compileProcessing(type, this);
    }

    @Override
    public void applyDefaultData(ResourceLocation typeId) {
        CreateProcessingKind.byType(typeId).ifPresent(this::applyCreateProcessing);
    }

    private void applyCreateProcessing(CreateProcessingKind kind) {
        var defaultIngredients = new ArrayList<RecipeIngredient>();
        var defaultInputCount = switch (kind) {
            case AUTO_PACKING -> 9;
            case AUTOMATIC_SHAPELESS -> 2;
            default -> 1;
        };
        for (int i = 0; i < defaultInputCount; i++) {
            defaultIngredients.add(RecipeIngredient.item(kind.defaultInput()));
        }
        setIngredients(defaultIngredients)
                .setFluidIngredients(new ArrayList<>())
                .setOutputs(kind.maxItemOutputs() > 0
                    ? new ArrayList<>(List.of(RecipeOutputData.of(new ItemStack(kind.defaultOutput()))))
                    : new ArrayList<>())
                .setFluidOutputs(new ArrayList<>())
                .setProcessingTime(kind.durationAllowed() ? 100 : 0)
                .setHeatRequirement(kind == CreateProcessingKind.AUTOMATIC_BREWING ? CreateHeatCondition.HEATED : CreateHeatCondition.NONE);
        if (kind.maxFluidInputs() > 0) getFluidIngredients().add(FluidIngredientData.fluid(new FluidStack(Fluids.WATER, 1000)));
        if (kind.maxFluidOutputs() > 0 && kind.maxItemOutputs() == 1 && kind == CreateProcessingKind.EMPTYING) {
            getFluidOutputs().add(new FluidStack(Fluids.WATER, 250));
        } else if (kind.maxFluidOutputs() > 0 && kind == CreateProcessingKind.AUTOMATIC_BREWING) {
            getFluidOutputs().add(new FluidStack(Fluids.WATER, 1000));
        }
    }
}
