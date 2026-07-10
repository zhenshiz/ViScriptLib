package com.viscriptshop.compat;

import com.viscriptshop.ViscriptShop;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.Optional;

@mezz.jei.api.JeiPlugin
public class JeiHelper implements IModPlugin {
    private static IJeiRuntime jeiRuntime;

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return ViscriptShop.id("jei_helper");
    }

    @Override
    public void onRuntimeAvailable(@NotNull IJeiRuntime jeiRuntime) {
        JeiHelper.jeiRuntime = jeiRuntime;
    }

    public static Optional<IJeiRuntime> getJeiRuntime() {
        return Optional.ofNullable(jeiRuntime);
    }

    public static void showRecipes(ItemStack itemStack) {
        JeiHelper.getJeiRuntime().ifPresent(jeiRuntime -> {
            jeiRuntime.getIngredientManager().getIngredientTypeChecked(itemStack)
                    .ifPresent(type -> {
                        jeiRuntime.getRecipesGui().show(
                                jeiRuntime.getJeiHelpers().getFocusFactory().createFocus(RecipeIngredientRole.OUTPUT, type, itemStack)
                        );
                    });
        });
    }

    public static void showUses(ItemStack itemStack) {
        JeiHelper.getJeiRuntime().ifPresent(jeiRuntime -> {
            jeiRuntime.getIngredientManager().getIngredientTypeChecked(itemStack)
                    .ifPresent(type -> {
                        jeiRuntime.getRecipesGui().show(
                                jeiRuntime.getJeiHelpers().getFocusFactory().createFocus(RecipeIngredientRole.INPUT, type, itemStack)
                        );
                    });
        });
    }

    public static int getShowRecipeKey() {
        for (KeyMapping keyMapping : Minecraft.getInstance().options.keyMappings) {
            if (keyMapping.getName().equals("key.jei.showRecipe")) {
                return keyMapping.getKey().getValue();
            }
        }
        return GLFW.GLFW_KEY_R;
    }

    public static int getShowUsesKey() {
        for (KeyMapping keyMapping : Minecraft.getInstance().options.keyMappings) {
            if (keyMapping.getName().equals("key.jei.showUses")) {
                return keyMapping.getKey().getValue();
            }
        }
        return GLFW.GLFW_KEY_U;
    }
}
