package com.viscript_lib.util;

import com.mojang.brigadier.StringReader;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;

public class NbtHelper {
    private static final int INDENT_SPACES = 2; // 缩进所用的空格数量

    public static CompoundTag tagFromString(String nbt) throws Exception {
        StringReader reader = new StringReader(nbt);
        return new TagParser(reader).readStruct();
    }

    public static String tagToString(Tag tag) {return tagToString(tag, true);}
    public static String tagToString(Tag tag, boolean pretty) {
        String s = NbtUtils.toPrettyComponent(tag).getString();
        return pretty ? prettyPrint(s) : s;
    }

    /**
     * 将一行无换行的 NBT 风格数据格式化为带缩进的可读形式。
     *
     * @param input 原始紧凑文本
     * @return 格式化后的文本
     */
    private static String prettyPrint(String input) {
        StringBuilder out = new StringBuilder(input.length() * 2);
        int depth = 0;
        boolean inString = false;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            // 跳过字符串外部的所有空白字符，由我们重新生成换行和缩进
            if (!inString && (c == ' ' || c == '\t' || c == '\n' || c == '\r')) continue;
            // 处理字符串边界
            if (c == '"') {
                if (!inString) inString = true;
                else {
                    // 检查双引号是否被转义
                    int backslashes = 0;
                    int j = i - 1;
                    while (j >= 0 && input.charAt(j) == '\\') {
                        backslashes++;
                        j--;
                    }
                    if (backslashes % 2 == 0) inString = false;
                }
                out.append(c);
                continue;
            }
            // 字符串内部的内容原样输出
            if (inString) {
                out.append(c);
                continue;
            }
            // 处理结构字符
            switch (c) {
                case '{':
                case '[':
                    out.append(c);
                    char next = nextNonWhitespace(input, i);
                    // 如果是空容器 "{}" 或 "[]"，不增加缩进，也不换行
                    if (!((c == '{' && next == '}') || (c == '[' && next == ']'))) {
                        depth++;
                        newlineAndIndent(out, depth);
                    }
                    break;
                case '}':
                case ']': // 判断当前闭合是否对应一个空容器
                    if (isLastNonWhitespace(out, c == '}' ? '{' : '[')) out.append(c);
                    else {
                        depth = Math.max(0, depth - 1);
                        newlineAndIndent(out, depth);
                        out.append(c);
                    }
                    break;
                case ',':
                    out.append(',');
                    newlineAndIndent(out, depth);
                    break;
                case ':': // 冒号后添加一个空格，保持统一风格
                    out.append(": ");
                    break;
                default: // 普通字符（数字、字母、点、负号、类型后缀等）直接输出
                    out.append(c);
            }
        }
        return out.toString();
    }

    // 从 from 之后开始查找第一个非空白字符。
    private static char nextNonWhitespace(String s, int from) {
        for (int i = from + 1; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c != ' ' && c != '\t' && c != '\n' && c != '\r') return c;
        }
        return '\0';
    }

    // 检查输出缓冲区的最后一个非空白字符是否为期望的字符。用于判断是否刚刚输出了一个空容器的开始符号。
    private static boolean isLastNonWhitespace(StringBuilder sb, char expected) {
        for (int i = sb.length() - 1; i >= 0; i--) {
            char c = sb.charAt(i);
            if (c != ' ' && c != '\n') return c == expected;
        }
        return false;
    }

    // 添加一个换行符和 depth 层缩进空格。
    private static void newlineAndIndent(StringBuilder sb, int depth) {
        sb.append('\n');
        sb.repeat(" ", Math.max(0, depth * INDENT_SPACES));
    }
}
