package com.viscript_recipe.data;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.configurator.annotation.ConfigRL;
import com.lowdragmc.lowdraglib2.configurator.annotation.ConfigSelector;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.StrictNBTIngredient;

@Getter
@Setter
@Accessors(chain = true)
public class RecipeIngredientValue implements IPersistedSerializable, IConfigurable {
    @Configurable(name = "viscript_recipe.config.ingredient.value.kind")
    @ConfigSelector(candidate = {"item", "tag", "item_ability"})
    private IngredientValueKind kind = IngredientValueKind.ITEM;

    @Configurable(name = "viscript_recipe.config.ingredient.value.item")
    private ItemStack item = new ItemStack(Items.STONE);

    @Configurable(name = "viscript_recipe.config.ingredient.value.tag")
    @ConfigRL(ConfigRL.Type.ITEM_TAG_KEY)
    private ResourceLocation tag = new ResourceLocation("minecraft", "planks");

    @Configurable(name = "viscript_recipe.config.ingredient.value.item_ability")
    private String itemAbility = "knife_dig";

    public static RecipeIngredientValue item(Item item) {
        return item(new ItemStack(item));
    }

    public static RecipeIngredientValue item(ItemStack stack) {
        return new RecipeIngredientValue()
                .setKind(IngredientValueKind.ITEM)
                .setItem(stack == null ? ItemStack.EMPTY : stack.copyWithCount(1));
    }

    public static RecipeIngredientValue tag(ResourceLocation tag) {
        return new RecipeIngredientValue().setKind(IngredientValueKind.TAG).setTag(tag);
    }

    public static RecipeIngredientValue itemAbility(String itemAbility) {
        return new RecipeIngredientValue()
                //.setKind(IngredientValueKind.ITEM_ABILITY)
                .setItemAbility(itemAbility == null || itemAbility.isBlank() ? "knife_dig" : itemAbility);
    }

    public Ingredient compile() {
        return switch (kind) {
            case ITEM -> {
                var stack = item.copyWithCount(1);
                if (stack.isEmpty() || stack.is(Items.AIR)) {
                    throw new IllegalArgumentException("Ingredient item cannot be empty");
                }
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
            case ITEM_ABILITY -> null /*FarmersDelightRecipeFactory.compileItemAbilityIngredient(itemAbility)*/;
        };
    }
}
