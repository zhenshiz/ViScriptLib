package com.viscript_lib.util;

import com.google.common.base.Strings;
import com.mojang.brigadier.StringReader;
import it.unimi.dsi.fastutil.bytes.ByteCollection;
import it.unimi.dsi.fastutil.bytes.ByteOpenHashSet;
import net.minecraft.nbt.*;
import org.jetbrains.annotations.NotNull;

import java.util.function.IntFunction;

public class NbtHelper {

    public static CompoundTag tagFromString(String nbt) throws Exception {
        StringReader reader = new StringReader(nbt);
        return new TagParser(reader).readStruct();
    }

    /** 默认两空格缩进。 */
    public static String toJson(Tag tag) {return toJson(tag, "  ");}
    /** 指定缩进，"" 表示单行。 */
    public static String toJson(Tag tag, String indentation) {
        return new NbtToJsonVisitor(indentation, 0).visit(tag);
    }

    /**
     * 把 NBT 转成 JSON 文本（保留缩进）。
     * <p>
     * 用法：
     *   String pretty  = NbtToJsonVisitor.toJson(tag);           // 2 空格缩进
     *   String tab     = NbtToJsonVisitor.toJson(tag, "\t");     // Tab 缩进
     * <p>
     * 映射规则：
     *   - Byte/Short/Int/Long/Float/Double  -> JSON 数字（SNBT 的类型后缀 b/s/L/f/d 全部丢弃）
     *   - NaN / Infinity                    -> null（JSON 不支持这些字面量）
     *   - String                            -> JSON 字符串（按 JSON 规则转义）
     *   - ByteArray/IntArray/LongArray      -> 数组（始终内联，与 SNBT 一致）
     *   - ListTag                           -> 数组（数字元素且 <= 8 个时内联，否则缩进展开）
     *   - CompoundTag                       -> 对象
     *   - EndTag                            -> 空字符串
     */
    public static class NbtToJsonVisitor implements TagVisitor {
        // 与 SNBT 一致：纯数字的短列表内联，避免刷屏
        private static final int INLINE_LIST_THRESHOLD = 8;
        private static final ByteCollection INLINE_ELEMENT_TYPES =
                new ByteOpenHashSet(new byte[] {1, 2, 3, 4, 5, 6}); // Byte..Double

        private static final String LIST_OPEN = "[";
        private static final String LIST_CLOSE = "]";
        private static final String STRUCT_OPEN = "{";
        private static final String STRUCT_CLOSE = "}";
        private static final String ELEMENT_SEPARATOR = ",";
        private static final String NAME_VALUE_SEPARATOR = ":";
        private static final String ELEMENT_SPACING = " ";
        private static final String NEWLINE = "\n";
        private static final String NULL = "null";

        private final String indentation;
        private final int depth;
        private String result;

        public NbtToJsonVisitor(String indentation, int depth) {
            this.indentation = indentation;
            this.depth = depth;
            this.result = "";
        }

        public String visit(Tag tag) {
            if (tag == null) this.result = NULL;
            else tag.accept(this);
            return this.result;
        }

        /* ========================= 基本类型 ========================= */

        @Override
        public void visitString(StringTag tag) {
            this.result = quote(tag.getAsString());
        }

        @Override
        public void visitByte(ByteTag tag) {
            this.result = Byte.toString(tag.getAsByte());
        }

        @Override
        public void visitShort(ShortTag tag) {
            this.result = Short.toString(tag.getAsShort());
        }

        @Override
        public void visitInt(IntTag tag) {
            this.result = Integer.toString(tag.getAsInt());
        }

        @Override
        public void visitLong(LongTag tag) {
            this.result = Long.toString(tag.getAsLong());
        }

        @Override
        public void visitFloat(FloatTag tag) {
            this.result = formatFloat(tag.getAsFloat());
        }

        @Override
        public void visitDouble(DoubleTag tag) {
            this.result = formatDouble(tag.getAsDouble());
        }

        /* ========================= 数组 ========================= */

        @Override
        public void visitByteArray(ByteArrayTag tag) {
            byte[] array = tag.getAsByteArray();
            this.result = formatArray(array.length, i -> Byte.toString(array[i]));
        }

        @Override
        public void visitIntArray(IntArrayTag tag) {
            int[] array = tag.getAsIntArray();
            this.result = formatArray(array.length, i -> Integer.toString(array[i]));
        }

        @Override
        public void visitLongArray(LongArrayTag tag) {
            long[] array = tag.getAsLongArray();
            this.result = formatArray(array.length, i -> Long.toString(array[i]));
        }

        /** 数组始终内联，和 SNBT 行为一致。 */
        private static String formatArray(int length, IntFunction<String> formatter) {
            StringBuilder sb = new StringBuilder(LIST_OPEN);
            for (int i = 0; i < length; i++) {
                if (i > 0) sb.append(ELEMENT_SEPARATOR).append(ELEMENT_SPACING);
                sb.append(formatter.apply(i));
            }
            return sb.append(LIST_CLOSE).toString();
        }

        /* ========================= ListTag ========================= */

        @Override
        public void visitList(ListTag tag) {
            if (tag.isEmpty()) {
                this.result = LIST_OPEN + LIST_CLOSE;
                return;
            }
            // 用首元素类型判断是否内联（1.20.5+ 的 ListTag 可能是混合类型，
            // 这里沿用原版习惯：只看首元素，避免依赖 getElementType()）
            boolean inline = INLINE_ELEMENT_TYPES.contains(tag.get(0).getId()) && tag.size() <= INLINE_LIST_THRESHOLD;

            StringBuilder sb = new StringBuilder(LIST_OPEN);
            if (inline) {
                for (int i = 0; i < tag.size(); i++) {
                    if (i > 0) sb.append(ELEMENT_SEPARATOR).append(ELEMENT_SPACING);
                    sb.append(new NbtToJsonVisitor(this.indentation, this.depth).visit(tag.get(i)));
                }
            } else {
                if (!this.indentation.isEmpty()) sb.append(NEWLINE);
                String childIndent = Strings.repeat(this.indentation, this.depth + 1);

                for (int i = 0; i < tag.size(); i++) {
                    sb.append(childIndent);
                    sb.append(new NbtToJsonVisitor(this.indentation, this.depth + 1).visit(tag.get(i)));
                    if (i != tag.size() - 1) {
                        sb.append(ELEMENT_SEPARATOR);
                        sb.append(this.indentation.isEmpty() ? ELEMENT_SPACING : NEWLINE);
                    }
                }

                if (!this.indentation.isEmpty()) {
                    sb.append(NEWLINE).append(Strings.repeat(this.indentation, this.depth));
                }
            }
            sb.append(LIST_CLOSE);
            this.result = sb.toString();
        }

        /* ========================= CompoundTag ========================= */

        @Override
        public void visitCompound(CompoundTag tag) {
            if (tag.isEmpty()) {
                this.result = STRUCT_OPEN + STRUCT_CLOSE;
                return;
            }
            var keys = tag.getAllKeys();
            StringBuilder sb = new StringBuilder(STRUCT_OPEN);
            if (!this.indentation.isEmpty()) sb.append(NEWLINE);
            String childIndent = Strings.repeat(this.indentation, this.depth + 1);

            var iterator = keys.iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                sb.append(childIndent)
                        .append(quote(key))
                        .append(NAME_VALUE_SEPARATOR)
                        .append(ELEMENT_SPACING)
                        .append(new NbtToJsonVisitor(this.indentation, this.depth + 1).visit(tag.get(key)));
                if (iterator.hasNext()) {
                    sb.append(ELEMENT_SEPARATOR);
                    sb.append(this.indentation.isEmpty() ? ELEMENT_SPACING : NEWLINE);
                }
            }

            if (!this.indentation.isEmpty()) {
                sb.append(NEWLINE).append(Strings.repeat(this.indentation, this.depth));
            }
            sb.append(STRUCT_CLOSE);
            this.result = sb.toString();
        }

        @Override
        public void visitEnd(@NotNull EndTag tag) {this.result = "";}

        private static String formatFloat(float f) {
            if (Float.isNaN(f) || Float.isInfinite(f)) return NULL;
            return Float.toString(f);
        }

        private static String formatDouble(double d) {
            if (Double.isNaN(d) || Double.isInfinite(d)) return NULL;
            return Double.toString(d);
        }

        /** 按 JSON 规则转义字符串（与 SNBT 的 quoteAndEscape 略有不同）。 */
        private static String quote(String value) {
            StringBuilder sb = new StringBuilder(value.length() + 2);
            sb.append('"');
            for (int i = 0; i < value.length(); i++) {
                char c = value.charAt(i);
                switch (c) {
                    case '"'  -> sb.append("\\\"");
                    case '\\' -> sb.append("\\\\");
                    case '\b' -> sb.append("\\b");
                    case '\f' -> sb.append("\\f");
                    case '\n' -> sb.append("\\n");
                    case '\r' -> sb.append("\\r");
                    case '\t' -> sb.append("\\t");
                    default -> {
                        if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                        else sb.append(c);
                    }
                }
            }
            return sb.append('"').toString();
        }
    }
}
