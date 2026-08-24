package com.viscript_recipe.data;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.viscript_lib.util.ISkipDefaultedSerialize;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public interface IVSRecipeData extends ISkipDefaultedSerialize, IConfigurable {

    default String getDataName() {
        String name = getClass().getSimpleName().replace("RecipeData", "");
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }

    @Override
    default String getConfigurableName() {return "viscript_recipe.config.entry." + getDataName();}

    /**默认情况表示不支持该功能*/
    default Boolean getShowNotification() {return null;}

    default <T extends IVSRecipeData> T setShowNotification(Boolean showNotification) {return (T) this;}

    default Recipe<?> compile(ResourceLocation typeId) {return null;}

    default String[] getCompatNames() {return new String[0];}

    @Override
    default void deserializeNBT(HolderLookup.@NotNull Provider provider, @NotNull CompoundTag tag) {
        CompoundTag nbt = tag;
        String name = getDataName();
        if (tag.contains(name)) nbt = tag.getCompound(name);
        else for (String compatName : getCompatNames()) if (tag.contains(compatName)) {
            nbt = tag.getCompound(compatName);
            break;
        }
        ISkipDefaultedSerialize.super.deserializeNBT(provider, nbt);
    }

    /**需要方便读写的原料列表数据，如果返回的列表不是ArrayList，则无法使用setIngredient方法设置原料*/
    default List<RecipeIngredient> getIngredients() {return List.of();}

    default RecipeIngredient ingredient(int index) {
        if (index < 0 || index >= getIngredients().size()) return RecipeIngredient.empty();
        return getIngredients().get(index);
    }

    /**设置指定索引的原料，如果索引超出范围，则自动在列表添加空原料*/
    default <T extends IVSRecipeData> T setIngredient(int index, RecipeIngredient ingredient) {
        if (index < 0 || !(getIngredients() instanceof ArrayList<RecipeIngredient>)) return (T) this;
        while (getIngredients().size() <= index) getIngredients().add(RecipeIngredient.empty());
        getIngredients().set(index, ingredient);
        return (T) this;
    }

    /**新增或修改配方类型时应用默认数据的方法*/
    default void applyDefaultData(ResourceLocation typeId) {}
}
