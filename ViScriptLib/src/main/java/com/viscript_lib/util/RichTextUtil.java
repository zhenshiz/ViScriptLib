package com.viscript_lib.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public final class RichTextUtil {
    public static final char AMPERSAND_FORMAT_PREFIX = '&';
    public static final char VANILLA_FORMAT_PREFIX = ChatFormatting.PREFIX_CODE;

    private RichTextUtil() {
    }

    /**
     * 使用 {@code &} 作为默认前缀解析 Minecraft 旧版格式代码。
     * 支持颜色 {@code &0-&f}、样式 {@code &k-&o} 和重置 {@code &r}。
     */
    public static MutableComponent parse(String text) {
        return parse(text, AMPERSAND_FORMAT_PREFIX);
    }

    /**
     * 使用自定义替代前缀解析旧版格式代码。
     * 如果文本里已经存在原版 {@code section sign} 格式代码，也会一起识别。
     */
    public static MutableComponent parse(String text, char formatPrefix) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }

        MutableComponent result = Component.empty();
        Style currentStyle = Style.EMPTY;
        StringBuilder segment = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            char current = text.charAt(i);
            if (isFormatPrefix(current, formatPrefix) && i + 1 < text.length()) {
                ChatFormatting formatting = ChatFormatting.getByCode(text.charAt(i + 1));
                if (formatting != null) {
                    appendSegment(result, segment, currentStyle);
                    currentStyle = currentStyle.applyLegacyFormat(formatting);
                    i++;
                    continue;
                }
            }

            segment.append(current);
        }

        appendSegment(result, segment, currentStyle);
        return result;
    }

    /**
     * 将 {@code &} 格式代码转换成原版 {@code section sign} 格式代码。
     * 无效格式代码会作为普通文本保留。
     */
    public static String toLegacyText(String text) {
        return toLegacyText(text, AMPERSAND_FORMAT_PREFIX);
    }

    /**
     * 将自定义替代前缀的格式代码转换成原版 {@code section sign} 格式代码。
     * 无效格式代码会作为普通文本保留。
     */
    public static String toLegacyText(String text, char formatPrefix) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        StringBuilder result = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char current = text.charAt(i);
            if (current == formatPrefix && i + 1 < text.length()) {
                char code = text.charAt(i + 1);
                if (isFormattingCode(code)) {
                    result.append(VANILLA_FORMAT_PREFIX).append(Character.toLowerCase(code));
                    i++;
                    continue;
                }
            }

            result.append(current);
        }

        return result.toString();
    }

    /**
     * 移除文本中有效的原版和 {@code &} 格式代码。
     */
    public static String stripFormatting(String text) {
        return stripFormatting(text, AMPERSAND_FORMAT_PREFIX);
    }

    /**
     * 移除文本中有效的原版和自定义前缀格式代码。
     */
    public static String stripFormatting(String text, char formatPrefix) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        StringBuilder result = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char current = text.charAt(i);
            if (isFormatPrefix(current, formatPrefix) && i + 1 < text.length() && isFormattingCode(text.charAt(i + 1))) {
                i++;
                continue;
            }

            result.append(current);
        }

        return result.toString();
    }

    public static boolean isFormattingCode(char code) {
        return ChatFormatting.getByCode(code) != null;
    }

    private static boolean isFormatPrefix(char current, char formatPrefix) {
        return current == formatPrefix || current == VANILLA_FORMAT_PREFIX;
    }

    private static void appendSegment(MutableComponent result, StringBuilder segment, Style style) {
        if (segment.isEmpty()) {
            return;
        }

        result.append(Component.literal(segment.toString()).setStyle(style));
        segment.setLength(0);
    }
}
