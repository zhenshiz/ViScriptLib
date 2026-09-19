package com.viscript_recipe.compat.touhou_little_maid.data;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.compat.touhou_little_maid.TouhouLittleMaidRecipeFactory;
import com.viscript_recipe.data.IVSRecipeData;
import com.viscript_recipe.data.RecipeIngredient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class TouhouLittleMaidAltarRecipeData implements IVSRecipeData {
    public static final int INPUT_COUNT = 6;

    @Persisted
    private List<RecipeIngredient> ingredients = new ArrayList<>();
    @Persisted
    private ItemStack result = new ItemStack(Items.STICK);
    @Persisted
    private float power = 0.2F;
    @Persisted
    private ResourceLocation entityType = new ResourceLocation("item");
//    @Persisted
//    private String langKey = "jei.touhou_little_maid.altar_craft.item_craft.result";
    @Persisted
    private CompoundTag extraData = new CompoundTag();

    public TouhouLittleMaidAltarRecipeData setExtraData(Tag extraData) {
        if (extraData instanceof CompoundTag tag) this.extraData = tag;
        return this;
    }

    public boolean isItemCraft() {
        return entityType.equals(new ResourceLocation("item"));
    }

    @Override
    public Recipe<?> compile(ResourceLocation typeId) {
        return TouhouLittleMaidRecipeFactory.compileAltar(this);
    }

    @Override
    public void applyDefaultData(ResourceLocation typeId) {
        setResult(new ItemStack(com.viscript_recipe.recipe.RecipeHelper.itemFromRegistry("touhou_little_maid:broom", Items.STICK)))
                .setIngredients(new ArrayList<>(List.of(
                        RecipeIngredient.item(Items.HAY_BLOCK),
                        RecipeIngredient.item(Items.HAY_BLOCK),
                        RecipeIngredient.item(Items.HAY_BLOCK),
                        RecipeIngredient.tag("c:rods/wooden"),
                        RecipeIngredient.tag("c:rods/wooden"),
                        RecipeIngredient.item(Items.ENDER_EYE)
                )));
    }
}
