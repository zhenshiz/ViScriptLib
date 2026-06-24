package com.viscript_lib.util;

import com.mojang.brigadier.StringReader;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.nbt.TextComponentTagVisitor;

public class NbtHelper {

    public static CompoundTag tagFromString(String nbt) throws Exception {
        StringReader reader = new StringReader(nbt);
        return new TagParser(reader).readStruct();
    }

    public static String tagToString(Tag tag) {return tagToString(tag, true);}
    public static String tagToString(Tag tag, boolean toJson) {
        var s = new TextComponentTagVisitor("  ").visit(tag).getString()
                .replaceAll("[BIL];\\s*", ""); // 移除Byte, Int, Long数组的类型标签
        return toJson ? nbtToJson(s) : s;
    }

    static String nbtToJson(String input) {
        /* 1. 布尔值 0b / 1b -> false / true */
        String output = input.replaceAll("\\b0b\\b", "false").replaceAll("\\b1b\\b", "true");
        /* 2. 移除其他数字后缀 (b/B, s/S, f/F, d/D, l/L)
            匹配整数或浮点数后的类型字母，大小写不敏感 */
        output = output.replaceAll("(?i)(\\b-?\\d+(?:\\.\\d+)?(?:[eE][+-]?\\d+)?)[bBsSfFdDlL]\\b", "$1");
        /* 3. 键名加双引号
            匹配 { 或 , 之后、空白、字母/下划线开头的标识符（含点）、空白、冒号 */
        output = output.replaceAll("([{,]\\s*)([a-zA-Z_][a-zA-Z0-9_.]*)\\s*:", "$1\"$2\":");
        /* 4. 未加引号的字符串值加双引号
            匹配冒号后非引号的值，且值不是 true/false/null，后面跟着逗号、}、] 或行尾 */
        /*output = output.replaceAll(
                ":(?!\\s*\")\\s*((?!true|false|null)[a-zA-Z_][a-zA-Z0-9_.]*)" + "(\\s*(?:,|}|]|$))", ": \"$1\"$2"
        );*/
        return output;
    }
}
