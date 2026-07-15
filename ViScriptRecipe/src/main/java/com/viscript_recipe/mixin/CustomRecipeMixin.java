package com.viscript_recipe.mixin;

import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import com.viscript_recipe.recipe.RecipeIdSetter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(CustomRecipe.class)
public class CustomRecipeMixin implements RecipeIdSetter {
    @Mutable @Shadow @Final
    private ResourceLocation id;

    @Override
    public void setId(ResourceLocation id) {
        this.id = id;
    }
}

@Mixin(ShapedRecipe.class)
class ShapedRecipeMixin implements RecipeIdSetter {
    @Mutable @Shadow @Final
    private ResourceLocation id;

    @Override
    public void setId(ResourceLocation id) {
        this.id = id;
    }
}

@Mixin(ShapelessRecipe.class)
class ShapelessRecipeMixin implements RecipeIdSetter {
    @Mutable @Shadow @Final
    private ResourceLocation id;

    @Override
    public void setId(ResourceLocation id) {
        this.id = id;
    }
}

@Mixin(AbstractCookingRecipe.class)
class AbstractCookingRecipeMixin implements RecipeIdSetter {
    @Mutable @Shadow @Final
    protected ResourceLocation id;

    @Override
    public void setId(ResourceLocation id) {
        this.id = id;
    }
}

@Mixin(SmithingTrimRecipe.class)
class SmithingTrimRecipeMixin implements RecipeIdSetter {
    @Mutable @Shadow @Final
    private ResourceLocation id;

    @Override
    public void setId(ResourceLocation id) {
        this.id = id;
    }
}

@Mixin(SmithingTransformRecipe.class)
class SmithingTransformRecipeMixin implements RecipeIdSetter {
    @Mutable @Shadow @Final
    private ResourceLocation id;

    @Override
    public void setId(ResourceLocation id) {
        this.id = id;
    }
}

@Mixin(SingleItemRecipe.class)
class SingleItemRecipeMixin implements RecipeIdSetter {
    @Mutable @Shadow @Final
    protected ResourceLocation id;

    @Override
    public void setId(ResourceLocation id) {
        this.id = id;
    }
}

@Mixin(value = ProcessingRecipe.class, remap = false)
class ProcessingRecipeMixin implements RecipeIdSetter {
    @Mutable @Shadow
    protected ResourceLocation id;

    @Override
    public void setId(ResourceLocation id) {
        this.id = id;
    }
}

@Mixin(value = SequencedAssemblyRecipe.class, remap = false)
class SequencedAssemblyRecipeMixin implements RecipeIdSetter {
    @Mutable @Shadow
    protected ResourceLocation id;

    @Override
    public void setId(ResourceLocation id) {
        this.id = id;
    }
}
