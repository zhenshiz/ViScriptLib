package com.viscript_recipe.compat.create.data;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.compat.create.CreateRecipeFactory;
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

import java.util.ArrayList;
import java.util.List;

import static com.viscript_recipe.recipe.RecipeHelper.itemFromRegistry;

@Getter
@Setter
@Accessors(chain = true)
public class CreateSequencedAssemblyRecipeData implements IVSRecipeData {
    @Persisted
    private RecipeIngredient ingredient = RecipeIngredient.item(Items.GOLD_INGOT);
    @Persisted
    private ItemStack transitionalItem = ItemStack.EMPTY;
    @Persisted
    private List<CreateSequencedAssemblyStepData> sequence = new ArrayList<>(List.of(
            new CreateSequencedAssemblyStepData(),
            new CreateSequencedAssemblyStepData().setKind(CreateSequencedAssemblyStepKind.PRESSING)
    ));
    @Persisted
    private List<RecipeOutputData> outputs = new ArrayList<>(List.of(RecipeOutputData.of(new ItemStack(Items.CLOCK))));
    @Persisted
    private int loops = 1;

    @Override
    public Recipe<?> compile(ResourceLocation typeId) {
        return CreateRecipeFactory.compileSequencedAssembly(this);
    }

    @Override
    public void applyDefaultData(ResourceLocation typeId) {
        setIngredient(RecipeIngredient.item(itemFromRegistry("create:golden_sheet", Items.GOLD_INGOT)))
                .setTransitionalItem(new ItemStack(itemFromRegistry("create:incomplete_precision_mechanism", Items.CLOCK)))
                .setLoops(5)
                .setSequence(new ArrayList<>(List.of(
                        createSequencedDeployingStep("create:cogwheel"),
                        createSequencedDeployingStep("create:large_cogwheel"),
                        createSequencedDeployingStep("minecraft:iron_nugget")
                )))
                .setOutputs(new ArrayList<>(List.of(RecipeOutputData.of(new ItemStack(itemFromRegistry("create:precision_mechanism", Items.CLOCK))))));
    }

    private static CreateSequencedAssemblyStepData createSequencedDeployingStep(String itemId) {
        return new CreateSequencedAssemblyStepData().setIngredient(RecipeIngredient.item(itemFromRegistry(itemId, Items.IRON_NUGGET)));
    }
}
