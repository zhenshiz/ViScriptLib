package com.viscript_recipe.data;

import com.lowdragmc.lowdraglib2.Platform;
import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_lib.util.ISkipDefaultedSerialize;
import com.viscript_recipe.ViScriptRecipe;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

@Getter
@Setter
@Accessors(chain = true)
@SuppressWarnings("unchecked")
public class RecipeEntry implements ISkipDefaultedSerialize, IConfigurable {
    private final HashMap<Class<? extends IVSRecipeData>, IVSRecipeData> recipeData = new HashMap<>();

    @Persisted
    private boolean enabled = true;
    @Persisted
    private RecipeOperation operation = RecipeOperation.REPLACE;
    @Persisted
    private ResourceLocation recipeId = ViScriptRecipe.id("example");
    @Persisted
    private ResourceLocation type = RecipeEditorTypes.CRAFTING_SHAPED;

    @Override
    public CompoundTag serializeNBT(HolderLookup.@NotNull Provider provider) {
        var tag = ISkipDefaultedSerialize.super.serializeNBT(provider);
        var data = getData();
        if (data instanceof MissingRecipeTypeHolder holder) tag.put(holder.missingDataName, holder.missingData);
        else tag.put(data.getDataName(), data.serializeNBT(provider));
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.@NotNull Provider provider, @NotNull CompoundTag tag) {
        ISkipDefaultedSerialize.super.deserializeNBT(provider, tag);
        getData().deserializeNBT(provider, tag);
    }

    public <T extends IVSRecipeData> Class<T> getDataClass() {
        return (Class<T>) RecipeEditorTypes.require(getType()).dataClass();
    }

    public <T extends IVSRecipeData> T getData() {
        var clazz = getDataClass();
        if (!recipeData.containsKey(clazz)) recipeData.put(clazz, RecipeEditorTypes.require(getType()).dataSupplier().get());
        return (T) recipeData.get(clazz);
    }

    public RecipeEntry setData(IVSRecipeData data) {
        var dataClass = getDataClass();
        if (data != null && data.getClass().equals(dataClass)) recipeData.put(dataClass, data);
        return this;
    }

    public Recipe<?> compile() {return getData().compile(getType());}

    public ResourceLocation getType() {
        return type == null ? RecipeEditorTypes.CRAFTING_SHAPED : type;
    }

    public RecipeEntry setType(ResourceLocation type) {
        this.type = type == null ? RecipeEditorTypes.CRAFTING_SHAPED : type;
        return this;
    }

    public boolean isType(ResourceLocation type) {
        return getType().equals(type);
    }

    public void applyDefaultData() {getData().applyDefaultData(getType());}

    public RecipeEntry copy() {
        var provider = Platform.getFrozenRegistry();
        var copy = new RecipeEntry();
        copy.deserializeNBT(provider, serializeNBT(provider).copy());
        return copy;
    }
}
