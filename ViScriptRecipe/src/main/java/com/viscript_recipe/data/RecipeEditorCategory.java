package com.viscript_recipe.data;

import com.viscript_recipe.ViScriptRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public record RecipeEditorCategory(
        ResourceLocation id,
        String translationKey,
        String ownerModId,
        List<String> requiredMods,
        ResourceLocation defaultType,
        RecipeEditorLayout layout,
        ResourceLocation workstationItemId
) {
    public RecipeEditorCategory(ResourceLocation id, String translationKey, String ownerModId, List<String> requiredMods,
                                ResourceLocation defaultType, RecipeEditorLayout layout) {
        this(id, translationKey, ownerModId, requiredMods, defaultType, layout, id);
    }

    public RecipeEditorCategory {
        requiredMods = requiredMods == null ? List.of() : List.copyOf(requiredMods);
    }

    public boolean isAvailable() {
        for (var modId : requiredMods) {
            if (!ViScriptRecipe.isModLoaded(modId)) {
                return false;
            }
        }
        return true;
    }

    public Component displayName() {
        var stack = workstationStack();
        return stack.isEmpty() ? Component.translatable(translationKey) : stack.getHoverName();
    }

    public ItemStack workstationStack() {
        if (workstationItemId == null || !BuiltInRegistries.ITEM.containsKey(workstationItemId)) {
            return ItemStack.EMPTY;
        }
        var item = BuiltInRegistries.ITEM.get(workstationItemId);
        return item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item);
    }

    public Component ownerName() {
        return Component.literal(ownerModId);
    }
}
