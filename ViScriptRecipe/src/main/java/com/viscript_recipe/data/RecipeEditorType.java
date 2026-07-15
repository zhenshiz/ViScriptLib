package com.viscript_recipe.data;

import com.viscript_recipe.ViScriptRecipe;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public record RecipeEditorType(
        ResourceLocation id,
        ResourceLocation category,
        String translationKey,
        List<String> requiredMods,
        boolean supportsNotification,
        Function<RecipeEntry, Recipe<?>> compiler,
        Function<RecipeEntry, Boolean> showNotificationGetter,
        BiConsumer<RecipeEntry, Boolean> showNotificationSetter,
        Function<RecipeEntry, ItemStack> resultGetter,
        BiConsumer<RecipeEntry, ItemStack> resultSetter
) {
    public RecipeEditorType {
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
        return Component.translatable(translationKey);
    }

    public Recipe<?> compile(RecipeEntry entry) {
        return compiler.apply(entry);
    }

    public boolean showNotification(RecipeEntry entry) {
        return showNotificationGetter.apply(entry);
    }

    public void setShowNotification(RecipeEntry entry, boolean value) {
        showNotificationSetter.accept(entry, value);
    }

    public ItemStack result(RecipeEntry entry) {
        var stack = resultGetter.apply(entry);
        return stack == null ? ItemStack.EMPTY : stack;
    }

    public void setResult(RecipeEntry entry, ItemStack result) {
        resultSetter.accept(entry, result == null ? ItemStack.EMPTY : result);
    }
}
