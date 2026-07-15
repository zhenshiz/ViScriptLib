package com.viscript_recipe.data.create;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.configurator.annotation.ConfigList;
import com.lowdragmc.lowdraglib2.configurator.annotation.ConfigSelector;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.viscript_recipe.compat.create.CreateRecipeFactory;
import com.viscript_recipe.data.RecipeIngredient;
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
public class CreateProcessingRecipeData implements IPersistedSerializable, IConfigurable {
    @Configurable(name = "viscript_recipe.config.create.ingredients")
    @ConfigList(addDefaultMethod = "createDefaultIngredient")
    private List<RecipeIngredient> ingredients = new ArrayList<>(List.of(RecipeIngredient.item(Items.COBBLESTONE)));

    @Configurable(name = "viscript_recipe.config.create.fluid_ingredients")
    @ConfigList(addDefaultMethod = "createDefaultFluidIngredient")
    private List<CreateFluidIngredientData> fluidIngredients = new ArrayList<>();

    @Configurable(name = "viscript_recipe.config.create.outputs")
    @ConfigList(addDefaultMethod = "createDefaultOutput")
    private List<CreateProcessingOutputData> outputs = new ArrayList<>(List.of(new CreateProcessingOutputData()
            .setItem(new ItemStack(Items.GRAVEL))
            .setChance(1.0F)));

    @Configurable(name = "viscript_recipe.config.create.fluid_outputs")
    @ConfigList(addDefaultMethod = "createDefaultFluidOutput")
    private List<FluidStack> fluidOutputs = new ArrayList<>();

    @Configurable(name = "viscript_recipe.config.create.processing_time")
    private int processingTime = 100;

    @Configurable(name = "viscript_recipe.config.create.heat_requirement")
    @ConfigSelector(candidate = {"none", "heated", "superheated"})
    private CreateHeatCondition heatRequirement = CreateHeatCondition.NONE;

    @Configurable(name = "viscript_recipe.config.create.keep_held_item")
    private boolean keepHeldItem;

    public RecipeIngredient createDefaultIngredient() {
        return RecipeIngredient.item(Items.COBBLESTONE);
    }

    public CreateFluidIngredientData createDefaultFluidIngredient() {
        return CreateFluidIngredientData.fluid(new FluidStack(Fluids.WATER, 1000));
    }

    public CreateProcessingOutputData createDefaultOutput() {
        return new CreateProcessingOutputData();
    }

    public FluidStack createDefaultFluidOutput() {
        return new FluidStack(Fluids.WATER, 1000);
    }

    public Recipe<?> compile(ResourceLocation type) {
        return CreateRecipeFactory.compileProcessing(type, this);
    }
}
