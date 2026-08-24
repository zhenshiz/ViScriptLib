package com.viscript_recipe.data;

import com.viscript_recipe.ViScriptRecipe;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**缺失模组的配方数据会被原封不动地保存在这里，即使用编辑器编辑后保存也不会丢失。当缺少的模组再次加载后，数据还能正常加载*/
@Getter
@Setter
@Accessors(chain = true)
public class MissingRecipeTypeHolder implements IVSRecipeData {
    public static final RecipeEditorType TYPE = RecipeEditorType.of(
            ViScriptRecipe.id("missing"), ViScriptRecipe.id("missing"), "missing_recipe_type",
            MissingRecipeTypeHolder.class, MissingRecipeTypeHolder::new, null);
    static final List<String> entryKeys = List.of("enabled", "operation", "type", "recipeId");

    String missingDataName = "";
    CompoundTag missingData = new CompoundTag();

    @Override
    public Recipe<?> compile(ResourceLocation typeId) {
        throw new UnsupportedOperationException("The mod for " + missingDataName + " is not loaded.");
    }

    @Override
    public void deserializeNBT(HolderLookup.@NotNull Provider provider, @NotNull CompoundTag tag) {
        for (String key : tag.getAllKeys()) {
            if (entryKeys.contains(key)) continue;
            setMissingDataName(key).setMissingData(tag.getCompound(key));
            break;
        }
    }
}
