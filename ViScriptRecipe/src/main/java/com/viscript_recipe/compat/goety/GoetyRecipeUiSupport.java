package com.viscript_recipe.compat.goety;

import com.Polarice3.Goety.api.ritual.RitualType;
import com.Polarice3.Goety.common.items.ModItems;
import com.Polarice3.Goety.common.items.research.ResearchScroll;
import com.Polarice3.Goety.common.research.ResearchList;
import com.Polarice3.Goety.utils.BrewUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Resolves Goety-owned read-only JEI preview items behind the optional-mod loading boundary.
 */
public final class GoetyRecipeUiSupport {
    private GoetyRecipeUiSupport() {
    }

    /**
     * Returns the official JEI icon for a ritual craft type.
     *
     * @param  craftType serialized ritual craft type
     * @return ritual icon, or an empty stack when the type is unknown
     */
    public static ItemStack ritualTypeIcon(String craftType) {
        var type = RitualType.getRitualType(craftType);
        return type == null || type.getJeiIcon() == null ? ItemStack.EMPTY : type.getJeiIcon().copy();
    }

    /**
     * Returns the scroll item associated with a Goety research key.
     *
     * @param  researchName serialized research key
     * @return matching research scroll, or an empty stack when no scroll is registered
     */
    public static ItemStack researchScroll(String researchName) {
        var research = ResearchList.getResearch(researchName == null ? "" : researchName);
        if (research == null) {
            return ItemStack.EMPTY;
        }
        for (var item : BuiltInRegistries.ITEM) {
            if (item instanceof ResearchScroll scroll && scroll.research == research) {
                return new ItemStack(scroll);
            }
        }
        return ItemStack.EMPTY;
    }

    /**
     * Creates the same effect-bearing brew preview used by Goety's JEI recipe maker.
     *
     * @param  effectId mob effect registry ID
     * @param  duration effect duration in ticks
     * @return derived Goety brew preview
     */
    public static ItemStack brewPreview(ResourceLocation effectId, int duration) {
        var stack = new ItemStack(ModItems.BREW.get());
        if (effectId == null) {
            return stack;
        }
        var effect = BuiltInRegistries.MOB_EFFECT.getOptional(effectId).orElse(null);
        if (effect == null) {
            return stack;
        }
        var effects = List.of(new MobEffectInstance(effect, Math.max(1, duration)));
        BrewUtils.setCustomEffects(stack, effects, List.of());
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt("CustomPotionColor", BrewUtils.getColor(effects, List.of()));
        stack.setTag(tag);
        return stack;
    }
}
