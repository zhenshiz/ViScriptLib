package com.viscript_recipe.mixin;

import com.Polarice3.Goety.common.crafting.*;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import com.viscript_recipe.recipe.RecipeIdSetter;
import committee.nova.mods.avaritia.common.crafting.recipe.CompressorRecipe;
import committee.nova.mods.avaritia.common.crafting.recipe.ShapedTableCraftingRecipe;
import committee.nova.mods.avaritia.common.crafting.recipe.ShapelessTableCraftingRecipe;
import mekanism.api.recipes.MekanismRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;
import vectorwing.farmersdelight.common.crafting.CuttingBoardRecipe;

@Mixin(CustomRecipe.class)
public class CustomRecipeMixin implements RecipeIdSetter {
    @Mutable @Shadow @Final
    private ResourceLocation id;

    @Override
    public void setId(ResourceLocation id) {this.id = id;}
}

@Mixin(ShapedRecipe.class)
class ShapedRecipeMixin implements RecipeIdSetter {
    @Mutable @Shadow @Final
    private ResourceLocation id;

    @Override
    public void setId(ResourceLocation id) {this.id = id;}
}

@Mixin(ShapelessRecipe.class)
class ShapelessRecipeMixin implements RecipeIdSetter {
    @Mutable @Shadow @Final
    private ResourceLocation id;

    @Override
    public void setId(ResourceLocation id) {this.id = id;}
}

@Mixin(AbstractCookingRecipe.class)
class AbstractCookingRecipeMixin implements RecipeIdSetter {
    @Mutable @Shadow @Final
    protected ResourceLocation id;

    @Override
    public void setId(ResourceLocation id) {this.id = id;}
}

@Mixin(SmithingTrimRecipe.class)
class SmithingTrimRecipeMixin implements RecipeIdSetter {
    @Mutable @Shadow @Final
    private ResourceLocation id;

    @Override
    public void setId(ResourceLocation id) {this.id = id;}
}

@Mixin(SmithingTransformRecipe.class)
class SmithingTransformRecipeMixin implements RecipeIdSetter {
    @Mutable @Shadow @Final
    private ResourceLocation id;

    @Override
    public void setId(ResourceLocation id) {this.id = id;}
}

@Mixin(SingleItemRecipe.class)
class SingleItemRecipeMixin implements RecipeIdSetter {
    @Mutable @Shadow @Final
    protected ResourceLocation id;

    @Override
    public void setId(ResourceLocation id) {this.id = id;}
}

@Mixin(value = ProcessingRecipe.class, remap = false)
class ProcessingRecipeMixin implements RecipeIdSetter {
    @Mutable @Shadow
    protected ResourceLocation id;

    @Override
    public void setId(ResourceLocation id) {this.id = id;}
}

@Mixin(value = SequencedAssemblyRecipe.class, remap = false)
class SequencedAssemblyRecipeMixin implements RecipeIdSetter {
    @Mutable @Shadow
    protected ResourceLocation id;

    @Override
    public void setId(ResourceLocation id) {this.id = id;}
}

@Mixin(value = CookingPotRecipe.class, remap = false)
class CookingPotRecipeMixin implements RecipeIdSetter {
    @Final @Mutable @Shadow
    private ResourceLocation id;

    @Override
    public void setId(ResourceLocation id) {this.id = id;}
}

@Mixin(value = CuttingBoardRecipe.class, remap = false)
class CuttingBoardRecipeMixin implements RecipeIdSetter {
    @Final @Mutable @Shadow
    private ResourceLocation id;

    @Override
    public void setId(ResourceLocation id) {this.id = id;}
}

@Mixin(value = CompressorRecipe.class, remap = false)
class CompressorRecipeMixin implements RecipeIdSetter {
    @Mutable @Shadow @Final
    private ResourceLocation recipeId;

    @Override
    public void setId(ResourceLocation id) {this.recipeId = id;}
}

@Mixin(value = ShapedTableCraftingRecipe.class, remap = false)
class ShapedTableCraftingRecipeMixin implements RecipeIdSetter {
    @Mutable @Shadow @Final
    private ResourceLocation recipeId;

    @Override
    public void setId(ResourceLocation id) {this.recipeId = id;}
}

@Mixin(value = ShapelessTableCraftingRecipe.class, remap = false)
class ShapelessTableCraftingRecipeMixin implements RecipeIdSetter {
    @Mutable @Shadow @Final
    private ResourceLocation recipeId;

    @Override
    public void setId(ResourceLocation id) {this.recipeId = id;}
}

@Mixin(value = BrazierRecipe.class, remap = false)
class BrazierRecipeMixin implements RecipeIdSetter {
    @Final @Mutable @Shadow
    private ResourceLocation id;

    @Override
    public void setId(ResourceLocation id) {this.id = id;}
}

@Mixin(value = BrewingRecipe.class, remap = false)
class BrewingRecipeRecipeMixin implements RecipeIdSetter {
    @Final @Mutable @Shadow
    private ResourceLocation id;

    @Override
    public void setId(ResourceLocation id) {this.id = id;}
}

@Mixin(value = CauldronRecipe.class, remap = false)
class CauldronRecipeMixin implements RecipeIdSetter {
    @Final @Mutable @Shadow
    public ResourceLocation id;

    @Override
    public void setId(ResourceLocation id) {this.id = id;}
}

@Mixin(value = ModCookingRecipe.class, remap = false)
class ModCookingRecipeMixin implements RecipeIdSetter {
    @Final @Mutable @Shadow
    protected ResourceLocation id;

    @Override
    public void setId(ResourceLocation id) {this.id = id;}
}

@Mixin(value = ModShapelessRecipe.class, remap = false)
class ModShapelessRecipeMixin implements RecipeIdSetter {
    @Final @Mutable @Shadow
    private ResourceLocation id;

    @Override
    public void setId(ResourceLocation id) {this.id = id;}
}

@Mixin(value = PulverizeRecipe.class, remap = false)
class PulverizeRecipeMixin implements RecipeIdSetter {
    @Final @Mutable @Shadow
    protected ResourceLocation id;

    @Override
    public void setId(ResourceLocation id) {this.id = id;}
}

@Mixin(value = SoulAbsorberRecipes.class, remap = false)
class SoulAbsorberRecipesMixin implements RecipeIdSetter {
    @Final @Mutable @Shadow
    protected ResourceLocation id;

    @Override
    public void setId(ResourceLocation id) {this.id = id;}
}

@Mixin(value = MekanismRecipe.class, remap = false)
class MekanismRecipeMixin implements RecipeIdSetter {
    @Final @Mutable @Shadow
    private ResourceLocation id;

    @Override
    public void setId(ResourceLocation id) {this.id = id;}
}
