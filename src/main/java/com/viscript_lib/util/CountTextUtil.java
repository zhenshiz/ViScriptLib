package com.viscript_lib.util;

import java.util.Locale;

public final class CountTextUtil {
    private CountTextUtil() {
    }

    public static String formatCount(long count) {
        double abs = Math.abs((double) count);
        if (abs < 1_000) {
            return String.valueOf(count);
        }
        if (abs < 1_000_000) {
            return formatCompact(count / 1_000.0, "k");
        }
        return formatCompact(count / 1_000_000.0, "m");
    }

    public static String formatCount(int count) {
        return formatCount((long) count);
    }

    private static String formatCompact(double value, String suffix) {
        var text = String.format(Locale.US, "%.1f", value);
        if (text.endsWith(".0")) {
            text = text.substring(0, text.length() - 2);
        }
        return text + suffix;
    }
}
