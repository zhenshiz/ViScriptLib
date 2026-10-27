package com.viscript_lib.gui.ai;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import dev.vfyjxf.taffy.style.FlexDirection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/** 将聊天常用 Markdown 转为编辑器主题的只读文本、代码块和表格。 */
public final class AiMarkdownView extends UIElement {
    private static final Pattern HEADING = Pattern.compile("^(#{1,6})\\s+(.+)$");
    private static final Pattern LIST = Pattern.compile("^\\s*(?:[-+*]|(\\d+)[.)])\\s+(.+)$");
    private static final Pattern SEPARATOR = Pattern.compile("^:?-{3,}:?$");

    /**
     * 解析一条助手消息；不执行 HTML、脚本或链接。
     *
     * @param markdown 原始 Markdown 正文
     */
    public AiMarkdownView(String markdown) {
        addClass("vsl_ai_markdown");
        layout(l -> l.widthPercent(100).gapAll(5).flexShrink(0));
        String[] lines = markdown.replace("\r\n", "\n").split("\n", -1);
        for (int i = 0; i < lines.length;) {
            String line = lines[i];
            if (line.isBlank()) { i++; continue; }
            if (line.stripLeading().startsWith("```") || line.stripLeading().startsWith("~~~")) {
                String fence = line.stripLeading().substring(0, 3);
                String language = line.stripLeading().substring(3).strip();
                var code = new StringBuilder();
                for (i++; i < lines.length && !lines[i].stripLeading().startsWith(fence); i++) {
                    if (!code.isEmpty()) code.append('\n');
                    code.append(lines[i]);
                }
                if (i < lines.length) i++;
                var block = new UIElement().addClass("preview_bg").addClass("vsl_ai_code_block")
                        .layout(l -> l.widthPercent(100).paddingAll(6).gapAll(4));
                if (!language.isBlank()) block.addChild(new AiMessageText(Component.literal(language).withStyle(s -> s.withColor(0xA0A0A0))));
                block.addChild(new AiMessageText(Component.literal(code.toString())));
                addChild(block);
                continue;
            }
            if (i + 1 < lines.length && line.contains("|") && tableSeparator(lines[i + 1])) {
                var header = cells(line);
                var table = new UIElement().addClass("vsl_ai_markdown_table").layout(l -> l.widthPercent(100).gapAll(1));
                table.addChild(row(header, header.size(), true));
                i += 2;
                while (i < lines.length && !lines[i].isBlank() && lines[i].contains("|")) table.addChild(row(cells(lines[i++]), header.size(), false));
                addChild(table);
                continue;
            }
            var heading = HEADING.matcher(line);
            var list = LIST.matcher(line);
            if (heading.matches()) {
                int level = heading.group(1).length();
                var text = new AiMessageText(inline(heading.group(2), Style.EMPTY.withBold(true), 0));
                text.addClass("vsl_ai_markdown_heading");
                text.textStyle(s -> s.fontSize(Math.max(10, 15 - level)));
                addChild(text);
            } else if (list.matches()) {
                addChild(new AiMessageText(Component.literal(list.group(1) == null ? "• " : list.group(1) + ". ")
                        .append(inline(list.group(2), Style.EMPTY, 0))));
            } else if (line.stripLeading().startsWith(">")) {
                var quote = new UIElement().addClass("preview_bg").layout(l -> l.widthPercent(100).paddingAll(5));
                quote.addChild(new AiMessageText(inline(line.stripLeading().substring(1).stripLeading(), Style.EMPTY.withItalic(true), 0)));
                addChild(quote);
            } else if (line.strip().matches("(?:-{3,}|\\*{3,}|_{3,})")) {
                addChild(new UIElement().addClass("preview_bg").layout(l -> l.widthPercent(100).height(1)));
            } else {
                var paragraph = new StringBuilder(line);
                while (i + 1 < lines.length && !lines[i + 1].isBlank() && !special(lines[i + 1])
                        && !(i + 2 < lines.length && tableSeparator(lines[i + 2]))) paragraph.append('\n').append(lines[++i]);
                addChild(new AiMessageText(inline(paragraph.toString(), Style.EMPTY, 0)));
            }
            i++;
        }
    }

    private static boolean special(String line) {
        String stripped = line.stripLeading();
        return HEADING.matcher(line).matches() || LIST.matcher(line).matches() || stripped.startsWith(">")
                || stripped.startsWith("```") || stripped.startsWith("~~~") || stripped.matches("(?:-{3,}|\\*{3,}|_{3,})");
    }

    private static UIElement row(List<String> cells, int count, boolean header) {
        var row = new UIElement().addClass("preview_bg").layout(l -> l.widthPercent(100).flexDirection(FlexDirection.ROW).gapAll(1));
        for (int i = 0; i < count; i++) {
            var cell = new UIElement().layout(l -> l.widthPercent(100f / count).minWidth(0).paddingAll(4));
            cell.addChild(new AiMessageText(inline(i < cells.size() ? cells.get(i) : "", Style.EMPTY.withBold(header), 0)));
            row.addChild(cell);
        }
        return row;
    }

    private static boolean tableSeparator(String line) {
        var cells = cells(line);
        return line.contains("|") && !cells.isEmpty() && cells.stream().allMatch(cell -> SEPARATOR.matcher(cell.strip()).matches());
    }

    private static List<String> cells(String line) {
        String value = line.strip();
        if (value.startsWith("|")) value = value.substring(1);
        if (value.endsWith("|") && !value.endsWith("\\|")) value = value.substring(0, value.length() - 1);
        var cells = new ArrayList<String>();
        var cell = new StringBuilder();
        boolean code = false;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '\\' && i + 1 < value.length() && value.charAt(i + 1) == '|') { cell.append('|'); i++; }
            else if (c == '`') { code = !code; cell.append(c); }
            else if (c == '|' && !code) { cells.add(cell.toString().strip()); cell.setLength(0); }
            else cell.append(c);
        }
        cells.add(cell.toString().strip());
        return cells;
    }

    private static MutableComponent inline(String text, Style style, int depth) {
        var result = Component.empty();
        if (depth > 8) return result.append(Component.literal(text).setStyle(style));
        var plain = new StringBuilder();
        for (int i = 0; i < text.length();) {
            if (text.charAt(i) == '\\' && i + 1 < text.length()) { plain.append(text.charAt(i + 1)); i += 2; continue; }
            String delimiter = text.startsWith("**", i) ? "**" : text.startsWith("__", i) ? "__"
                    : text.startsWith("~~", i) ? "~~" : "`*_".indexOf(text.charAt(i)) >= 0 ? text.substring(i, i + 1) : "";
            int end = delimiter.isEmpty() ? -1 : text.indexOf(delimiter, i + delimiter.length());
            if (end > i + delimiter.length()) {
                result.append(Component.literal(plain.toString()).setStyle(style)); plain.setLength(0);
                String content = text.substring(i + delimiter.length(), end);
                Style nested = delimiter.equals("`") ? style.withColor(0xA9D7A0)
                        : delimiter.equals("~~") ? style.withStrikethrough(true)
                        : delimiter.length() == 2 ? style.withBold(true) : style.withItalic(true);
                result.append(delimiter.equals("`") ? Component.literal(content).setStyle(nested) : inline(content, nested, depth + 1));
                i = end + delimiter.length();
            } else if (text.charAt(i) == '[' && text.indexOf("](", i + 1) > i) {
                int labelEnd = text.indexOf("](", i + 1), urlEnd = text.indexOf(')', labelEnd + 2);
                if (urlEnd < 0) { plain.append(text.charAt(i++)); continue; }
                result.append(Component.literal(plain.toString()).setStyle(style)); plain.setLength(0);
                result.append(inline(text.substring(i + 1, labelEnd), style.withUnderlined(true).withColor(0x9DC9FF), depth + 1));
                result.append(Component.literal(" (" + text.substring(labelEnd + 2, urlEnd) + ")").setStyle(style));
                i = urlEnd + 1;
            } else plain.append(text.charAt(i++));
        }
        return result.append(Component.literal(plain.toString()).setStyle(style));
    }
}
