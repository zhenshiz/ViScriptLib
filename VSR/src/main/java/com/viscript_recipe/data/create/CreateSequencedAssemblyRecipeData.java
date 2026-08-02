package com.viscript_recipe.data.create;

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

    public CreateSequencedAssemblyStepData createDefaultStep() {
        return new CreateSequencedAssemblyStepData();
    }

    @Override
    public Recipe<?> compile(ResourceLocation typeId) {
        return CreateRecipeFactory.compileSequencedAssembly(this);
    }
}
