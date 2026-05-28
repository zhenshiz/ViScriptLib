package com.viscript_lib.gui.editor;

import java.util.Locale;

/**
 * 规范化编辑器文件名、路径片段和后缀。
 *
 * <p>这些方法会剥离路径分隔符，避免文件名输入逃出编辑器约定目录。
 */
public final class EditorFileNames {

    /**
     * 返回带前导点号的文件后缀。
     *
     * @param suffix 文件后缀
     * @return 规范化后的文件后缀
     */
    public static String normalizeSuffix(String suffix) {
        if (suffix == null || suffix.isBlank()) {
            return "";
        }
        var trimmed = suffix.trim();
        return trimmed.startsWith(".") ? trimmed : "." + trimmed;
    }

    /**
     * 移除后缀开头的点号。
     *
     * @param suffix 文件后缀
     * @return 不带前导点号的后缀
     */
    public static String stripLeadingDot(String suffix) {
        var normalized = suffix == null ? "" : suffix.trim();
        return normalized.startsWith(".") ? normalized.substring(1) : normalized;
    }

    /**
     * 规范化文件名并补齐目标后缀。
     *
     * @param fileName 用户输入的文件名
     * @param suffix 需要补齐的文件后缀
     * @return 安全的文件名
     */
    public static String normalizeFileName(String fileName, String suffix) {
        var normalized = fileName == null ? "" : fileName.trim().replace('\\', '/');
        var lastSlash = normalized.lastIndexOf('/');
        if (lastSlash >= 0) {
            normalized = normalized.substring(lastSlash + 1);
        }
        if (!suffix.isEmpty() && normalized.toLowerCase(Locale.ROOT).endsWith(suffix.toLowerCase(Locale.ROOT))) {
            normalized = normalized.substring(0, normalized.length() - suffix.length());
        }
        normalized = normalized.replaceAll("[^A-Za-z0-9_.-]", "_");
        if (normalized.isBlank()) {
            normalized = "test";
        }
        return normalized + suffix;
    }

    /**
     * 规范化基础文件名并移除指定后缀。
     *
     * @param fileName 用户输入的文件名
     * @param suffixesToStrip 需要从末尾移除的后缀
     * @return 不带目标后缀的安全基础文件名
     */
    public static String normalizeBaseName(String fileName, String... suffixesToStrip) {
        var normalized = normalizeFileName(fileName, "");
        for (var suffix : suffixesToStrip) {
            var normalizedSuffix = normalizeSuffix(suffix);
            if (!normalizedSuffix.isEmpty()
                    && normalized.toLowerCase(Locale.ROOT).endsWith(normalizedSuffix.toLowerCase(Locale.ROOT))) {
                normalized = normalized.substring(0, normalized.length() - normalizedSuffix.length());
            }
        }
        return normalized.isBlank() ? "test" : normalized;
    }

    /**
     * 规范化单级目录名。
     *
     * @param value 用户或调用方提供的目录片段
     * @param fallback 空值时使用的默认片段
     * @return 安全的目录片段
     */
    public static String normalizePathSegment(String value, String fallback) {
        var normalized = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
        normalized = normalized.replace('\\', '/');
        var lastSlash = normalized.lastIndexOf('/');
        if (lastSlash >= 0) {
            normalized = normalized.substring(lastSlash + 1);
        }
        normalized = normalized.replaceAll("[^a-z0-9_.-]", "_");
        return normalized.isBlank() ? fallback : normalized;
    }
}
