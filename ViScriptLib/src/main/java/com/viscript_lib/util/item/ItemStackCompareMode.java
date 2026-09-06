package com.viscript_lib.util.item;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

/**
 * 物品比较模式，用于控制 ItemStack 的物品组件参与规则。
 */
public enum ItemStackCompareMode implements StringRepresentable {
    /**
     * 比较物品 id 和所有物品组件，等同于原版 ItemStack.isSameItemSameComponents。
     */
    ALL_COMPONENTS("viscript_lib.item_stack_compare_mode.all_components"),

    /**
     * 只比较传入列表中的物品组件；列表为空时相当于只比较物品 id。
     */
    INCLUDE_COMPONENTS("viscript_lib.item_stack_compare_mode.include_components"),

    /**
     * 比较除传入列表以外的所有物品组件。
     */
    EXCLUDE_COMPONENTS("viscript_lib.item_stack_compare_mode.exclude_components");

    private final String translationKey;

    ItemStackCompareMode(String translationKey) {
        this.translationKey = translationKey;
    }

    @Override
    public @NotNull String getSerializedName() {
        return translationKey;
    }

    public static ItemStackCompareMode fromSerializedName(String name) {
        for (ItemStackCompareMode mode : values()) {
            if (mode.getSerializedName().equals(name)) return mode;
        }
        return ALL_COMPONENTS;
    }
}
