package com.viscript_recipe.data.goety;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.viscript_recipe.data.RecipeIngredient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

/**
 * Stores Goety pulverize input and mutually selected item or block result data.
 */
@Getter
@Setter
@Accessors(chain = true)
public class GoetyPulverizeRecipeData implements IPersistedSerializable, IConfigurable {
    @Configurable(name = "viscript_recipe.config.goety.pulverize.ingredient", subConfigurable = true)
    private RecipeIngredient ingredient = RecipeIngredient.item(Items.STONE);

    @Configurable(name = "viscript_recipe.config.goety.pulverize.result_kind")
    private GoetyPulverizeResultKind resultKind = GoetyPulverizeResultKind.ITEM;

    @Configurable(name = "viscript_recipe.config.goety.pulverize.item_result")
    private ItemStack itemResult = new ItemStack(Items.COBBLESTONE);

    @Configurable(name = "viscript_recipe.config.goety.pulverize.block_result")
    private ResourceLocation blockResult = new ResourceLocation("cobblestone");

    /**
     * Returns the result shown in the editor, following Goety JEI's item-first display rule.
     *
     * @return the visible result stack
     */
    public ItemStack visibleResult() {
        if (resultKind == GoetyPulverizeResultKind.ITEM) {
            return itemResult == null ? ItemStack.EMPTY : itemResult.copy();
        }
        var block = BuiltInRegistries.BLOCK.getOptional(blockResult).orElse(Blocks.CAVE_AIR);
        return block.asItem().getDefaultInstance();
    }

    /**
     * Replaces the selected result from the visual output slot.
     *
     * @param stack the replacement result stack
     */
    public void setVisibleResult(ItemStack stack) {
        var safe = stack == null ? ItemStack.EMPTY : stack.copy();
        if (resultKind == GoetyPulverizeResultKind.ITEM) {
            itemResult = safe;
            return;
        }
        var block = Blocks.CAVE_AIR;
        if (!safe.isEmpty() && safe.getItem() instanceof net.minecraft.world.item.BlockItem blockItem) {
            block = blockItem.getBlock();
        }
        blockResult = BuiltInRegistries.BLOCK.getKey(block);
    }

    /**
     * Compiles this data into Goety's native pulverize recipe.
     *
     * @return the compiled pulverize recipe
     */
//    public Recipe<?> compile() {
//        return GoetyRecipeFactory.compilePulverize(this);
//    }
}
