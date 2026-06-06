package com.viscript_lib.util.item;

import com.viscript_lib.compat.JechHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.Locale;

/**
 * 简单的 ItemStack 筛选器
 * 支持：物品ID、物品名称、附魔、药水效果、描述筛选
 */
public class SimpleItemStackFilter {

    /**
     * 检查物品是否匹配搜索条件
     *
     * @param merchantItem 要检查的物品堆
     * @param searchItem   搜索字符串，支持多个条件用空格分隔
     *                     例如: "diamond sharpness:5 strength"
     * @return 是否匹配
     */
    public static boolean matchItemSearch(ItemStack merchantItem, String searchItem) {
        if (merchantItem.isEmpty()) {
            return false;
        }

        if (searchItem == null || searchItem.trim().isEmpty()) {
            return true;
        }

        // 分割搜索字符串为多个条件
        String[] searchTerms = searchItem.trim().split("\\s+");

        // 所有条件都必须匹配（AND关系）
        for (String searchTerm : searchTerms) {
            if (!matchesSearchTerm(merchantItem, searchTerm.trim())) {
                return false;
            }
        }

        return true;
    }

    /**
     * 检查单个搜索条件是否匹配
     */
    private static boolean matchesSearchTerm(ItemStack stack, String searchTerm) {
        if (searchTerm.isEmpty()) {
            return true;
        }

        // 检查附魔条件 (格式: enchant:sharpness:5)
        if (searchTerm.startsWith("enchant:")) {
            return matchesEnchantment(stack, searchTerm.substring(8));
        }

        // 检查药水效果条件 (格式: potion:strength:2)
        if (searchTerm.startsWith("potion:")) {
            return matchesPotionEffect(stack, searchTerm.substring(7));
        }

        // 检查描述条件 (格式: lore:legendary)
        if (searchTerm.startsWith("lore:")) {
            return matchesLore(stack, searchTerm.substring(5));
        }

        // 检查物品ID条件 (格式: id:diamond_sword)
        if (searchTerm.startsWith("id:")) {
            return matchesItemId(stack, searchTerm.substring(3));
        }

        // 默认检查物品名称
        return matchesItemName(stack, searchTerm);
    }

    /**
     * 检查物品ID是否匹配
     */
    private static boolean matchesItemId(ItemStack stack, String itemId) {
        return containsPlainText(getItemId(stack), itemId);
    }

    /**
     * 检查物品名称是否匹配（支持模糊匹配）
     */
    private static boolean matchesItemName(ItemStack stack, String searchTerm) {
        return containsReadableText(stack.getHoverName().getString(), searchTerm);
    }

    /**
     * 检查附魔是否匹配
     */
    private static boolean matchesEnchantment(ItemStack stack, String enchantCondition) {
        ItemEnchantments enchantments = EnchantmentHelper.getEnchantmentsForCrafting(stack);
        if (enchantments.isEmpty()) {
            return false;
        }

        // 解析条件: "sharpness" 或 "sharpness:5"
        String[] parts = enchantCondition.split(":");
        String enchantName = parts[0].toLowerCase();
        int requiredLevel = parts.length > 1 ? parseIntSafely(parts[1], -1) : -1;

        for (Holder<Enchantment> enchantmentHolder : enchantments.keySet()) {
            int level = enchantments.getLevel(enchantmentHolder);
            Enchantment enchantment = enchantmentHolder.value();

            // 获取附魔ID
            String enchantId = getEnchantmentId(enchantmentHolder);
            String enchantDisplayName = Enchantment.getFullname(enchantmentHolder, level).getString();

            // 检查名称是否匹配
            boolean nameMatches = containsPlainText(enchantId, enchantName) ||
                    containsReadableText(enchantDisplayName, enchantName);

            if (nameMatches) {
                // 如果没有指定等级，或者等级匹配
                if (requiredLevel == -1 || level == requiredLevel) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 检查药水效果是否匹配
     * 支持本地化名称和等级，例如: "力量"、"strength"、"力量:2"
     */
    private static boolean matchesPotionEffect(ItemStack stack, String potionCondition) {
        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        if (contents == null) {
            return false;
        }

        String[] parts = potionCondition.split(":");
        String effectName = parts[0].toLowerCase();
        int requiredLevel = parts.length > 1 ? parseIntSafely(parts[1], -1) : -1;

        for (MobEffectInstance effectInstance : contents.getAllEffects()) {
            var effectHolder = effectInstance.getEffect();

            String effectId = effectHolder.unwrapKey()
                    .map(key -> key.location().toString())
                    .orElse("");

            String localizedName = Component.translatable(effectHolder.value().getDescriptionId()).getString();

            if (containsPlainText(effectId, effectName) || containsReadableText(localizedName, effectName)) {
                if (requiredLevel == -1) {
                    return true;
                }

                int inGameLevel = effectInstance.getAmplifier() + 1;
                if (inGameLevel == requiredLevel) {
                    return true;
                }
            }
        }

        return containsReadableText(stack.getHoverName().getString(), effectName);
    }

    /**
     * 检查物品描述是否匹配
     * 格式: "legendary"
     */
    private static boolean matchesLore(ItemStack stack, String loreCondition) {
        var loreLines = stack.get(DataComponents.LORE);

        if (loreLines == null || loreLines.lines().isEmpty()) {
            return false;
        }

        for (var loreComponent : loreLines.lines()) {
            if (containsReadableText(loreComponent.getString(), loreCondition)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 获取物品ID
     */
    private static String getItemId(ItemStack stack) {
        return net.minecraft.core.registries.BuiltInRegistries.ITEM
                .getKey(stack.getItem())
                .toString();
    }

    /**
     * 获取附魔ID
     */
    private static String getEnchantmentId(Holder<Enchantment> enchantmentHolder) {
        return enchantmentHolder.unwrapKey()
                .map(key -> key.location().toString())
                .orElse("");
    }

    /**
     * 获取药水效果ID
     */
    private static String getEffectId(MobEffectInstance effect) {
        return net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT
                .getKey(effect.getEffect().value())
                .toString();
    }

    /**
     * 安全解析整数
     */
    private static int parseIntSafely(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 清理文本中的格式化代码
     */
    private static String cleanText(String text) {
        // 移除 §x 格式的颜色代码
        return text.replaceAll("§[0-9a-fk-or]", "");
    }

    private static boolean containsReadableText(String source, String searchTerm) {
        String normalizedSource = normalizeText(source);
        String normalizedSearch = normalizeText(searchTerm);

        if (normalizedSearch.isEmpty()) {
            return true;
        }
        if (normalizedSource.isEmpty()) {
            return false;
        }
        if (normalizedSource.contains(normalizedSearch)) {
            return true;
        }

        return JechHelper.containsIgnoreCase(normalizedSource, normalizedSearch);
    }

    private static boolean containsPlainText(String source, String searchTerm) {
        String normalizedSource = normalizeText(source);
        String normalizedSearch = normalizeText(searchTerm);

        if (normalizedSearch.isEmpty()) {
            return true;
        }
        if (normalizedSource.isEmpty()) {
            return false;
        }

        return normalizedSource.contains(normalizedSearch);
    }

    private static String normalizeText(String text) {
        if (text == null) {
            return "";
        }
        return cleanText(text).toLowerCase(Locale.ROOT).trim();
    }
}
