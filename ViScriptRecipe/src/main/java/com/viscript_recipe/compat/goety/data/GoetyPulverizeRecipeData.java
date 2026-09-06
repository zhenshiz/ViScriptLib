package com.viscript_recipe.compat.goety.data;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.compat.goety.GoetyRecipeFactory;
import com.viscript_recipe.data.IVSRecipeData;
import com.viscript_recipe.data.RecipeIngredient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Blocks;

@Getter
@Setter
@Accessors(chain = true)
public class GoetyPulverizeRecipeData implements IVSRecipeData {
    @Persisted
    private RecipeIngredient ingredient = RecipeIngredient.item(Items.STONE);
    @Persisted
    private GoetyPulverizeResultKind resultKind = GoetyPulverizeResultKind.ITEM;
    @Persisted
    private ItemStack itemResult = new ItemStack(Items.COBBLESTONE);
    @Persisted
    private ResourceLocation blockResult = new ResourceLocation("cobblestone");

    public ItemStack result() {
        if (resultKind == GoetyPulverizeResultKind.ITEM) return itemResult.copy();
        var block = BuiltInRegistries.BLOCK.getOptional(blockResult).orElse(Blocks.CAVE_AIR);
        return block.asItem().getDefaultInstance();
    }

    public void setResult(ItemStack stack) {
        if (resultKind == GoetyPulverizeResultKind.ITEM) {
            setItemResult(stack.copy());
        } else {
            var block = Blocks.AIR;
            if (stack.getItem() instanceof BlockItem blockItem) block = blockItem.getBlock();
            setBlockResult(BuiltInRegistries.BLOCK.getKey(block));
        }
    }

    @Override
    public Recipe<?> compile(ResourceLocation type) {
        return GoetyRecipeFactory.compilePulverize(this);
    }
}
