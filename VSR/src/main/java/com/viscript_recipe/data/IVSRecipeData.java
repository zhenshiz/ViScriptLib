package com.viscript_recipe.data;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.viscript_lib.util.ISkipDefaultedSerialize;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.NotNull;

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

    @Override
    default void deserializeNBT(HolderLookup.@NotNull Provider provider, @NotNull CompoundTag tag) {
        CompoundTag nbt;
        String name = getDataName();
        if (tag.contains(name)) nbt = tag.getCompound(name); else nbt = tag;
        ISkipDefaultedSerialize.super.deserializeNBT(provider, nbt);
    }
}
