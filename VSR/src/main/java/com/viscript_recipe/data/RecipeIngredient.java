package com.viscript_recipe.data;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_lib.util.ISkipDefaultedSerialize;
import com.viscript_recipe.compat.farmersdelight.FarmersDelightRecipeFactory;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.StrictNBTIngredient;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.viscript_recipe.gui.canvas.RecipeCanvas.itemFromAbility;
import static com.viscript_recipe.gui.canvas.RecipeCanvas.itemsFromTag;

@Getter
@Setter
@Accessors(chain = true)
public class RecipeIngredient implements ISkipDefaultedSerialize, IConfigurable {
    @Persisted
    private IngredientValueKind kind = IngredientValueKind.ITEM;
    @Persisted
    private ItemStack item = new ItemStack(Items.STONE);
    @Persisted
    private ResourceLocation tag = new ResourceLocation("minecraft", "planks");
    @Persisted
    private String itemAbility = "knife_dig";

    /**请使用工厂方法*/
    @Deprecated
    public RecipeIngredient() {}

    public static RecipeIngredient empty() {return item(ItemStack.EMPTY);}

    public static RecipeIngredient item(Item item) {return item(new ItemStack(item));}

    public static RecipeIngredient item(ItemStack stack) {
        return new RecipeIngredient().setKind(IngredientValueKind.ITEM)
                .setItem(stack == null ? ItemStack.EMPTY : stack.copyWithCount(1));
    }

    public static RecipeIngredient tag(ResourceLocation tagId) {
        return new RecipeIngredient().setKind(IngredientValueKind.TAG).setTag(tagId);
    }

    public static RecipeIngredient itemAbility(String itemAbility) {
        return new RecipeIngredient().setKind(IngredientValueKind.ITEM_ABILITY)
                .setItemAbility(itemAbility == null || itemAbility.isBlank() ? "knife_dig" : itemAbility);
    }

    public Ingredient compile() {
        return switch (kind) {
            case ITEM -> {
                var stack = item.copyWithCount(1);
                if (stack.isEmpty()) yield Ingredient.EMPTY;
                if (ItemStack.isSameItemSameTags(stack, stack.getItem().getDefaultInstance())) {
                    yield Ingredient.of(stack.getItem());
                }
                yield StrictNBTIngredient.of(stack);
            }
            case TAG -> {
                if (tag == null) {
                    throw new IllegalArgumentException("Ingredient tag cannot be empty");
                }
                var tagKey = TagKey.create(Registries.ITEM, tag);
                if (BuiltInRegistries.ITEM.getTag(tagKey).isEmpty()) {
                    throw new IllegalArgumentException("Unknown item tag: " + tag);
                }
                yield Ingredient.of(tagKey);
            }
            case ITEM_ABILITY -> FarmersDelightRecipeFactory.compileItemAbilityIngredient(itemAbility);
        };
    }

    public boolean isEmpty() {
        switch (kind) {
            case ITEM ->         { if (!item.isEmpty()) return false; }
            case TAG ->          { if (tag != null) return false; }
            case ITEM_ABILITY -> { if (!itemAbility.isBlank()) return false; }
        }
        return true;
    }

    public ItemStack toStack() {
        return switch (kind) {
            case ITEM -> item.copy();
            case TAG -> {
                var tagItems = itemsFromTag(tag);
                if (tagItems.length > 0) yield tagItems[0].copy();
                yield ItemStack.EMPTY;
            }
            case ITEM_ABILITY -> itemFromAbility(itemAbility);
        };
    }

    public ItemStack[] getDisplayStacks() {
        var stacks = new ArrayList<ItemStack>();
        switch (kind) {
            case ITEM -> {
                if (!item.isEmpty()) stacks.add(item.copyWithCount(1));
            }
            case TAG -> stacks.addAll(List.of(itemsFromTag(tag)));
            case ITEM_ABILITY -> {
                if (!itemAbility.isBlank()) stacks.add(itemFromAbility(itemAbility));
            }
        }
        return stacks.toArray(ItemStack[]::new);
    }

    public RecipeIngredient copy() {
        return new RecipeIngredient().setKind(kind).setItem(item.copy()).setTag(tag).setItemAbility(itemAbility);
    }

    @Override
    public void deserializeNBT(HolderLookup.@NotNull Provider provider, @NotNull CompoundTag tag) {
        if (tag.contains("values") && !tag.getList("values", 10).isEmpty()) {
            tag = tag.getList("values", 10).getCompound(0);
        }
        ISkipDefaultedSerialize.super.deserializeNBT(provider, tag);
    }
}
