package com.viscript_lib.gui.ai;

import com.lowdragmc.lowdraglib2.editor.ClipboardManager;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.TextWrap;
import com.lowdragmc.lowdraglib2.gui.ui.data.Vertical;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextElement;
import com.lowdragmc.lowdraglib2.gui.ui.event.CommandEvents;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.gui.ui.rendering.GUIContext;
import com.lowdragmc.lowdraglib2.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib2.utils.TextUtilities;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.lwjgl.glfw.GLFW;
import net.minecraft.client.Minecraft;

/** 允许鼠标选择及系统复制命令的只读富文本，软换行不进入剪贴板。 */
public final class AiMessageText extends TextElement {
    private record Line(int start, int end) { }
    private List<Line> lines = List.of();
    private Component measured;
    private float measuredWidth = -1;
    private int anchor;
    private int caret;

    /**
     * 创建自动换行的消息正文。
     *
     * @param text 已解析样式的可显示文本
     */
    public AiMessageText(Component text) {
        setText(text);
        textStyle(s -> s.textWrap(TextWrap.WRAP).adaptiveHeight(true)
                .textAlignHorizontal(Horizontal.LEFT).textAlignVertical(Vertical.TOP));
        layout(l -> l.widthPercent(100).flexShrink(0));
        setFocusable(true);
        addEventListener(UIEvents.MOUSE_DOWN, e -> {
            if (e.button != 0) return;
            var mouse = getLocalMouse(e.x, e.y);
            caret = cursor(mouse.x, mouse.y);
            anchor = caret;
            focus();
            startDrag(this, null);
            e.stopPropagation();
        });
        addEventListener(UIEvents.DRAG_SOURCE_UPDATE, e -> {
            if (e.dragHandler.draggingObject != this) return;
            var mouse = getLocalMouse(e.x, e.y);
            caret = cursor(mouse.x, mouse.y);
            e.stopPropagation();
        });
        addEventListener(UIEvents.BLUR, e -> anchor = caret);
        addEventListener(UIEvents.KEY_DOWN, e -> {
            int commandMask = Minecraft.ON_OSX ? GLFW.GLFW_MOD_SUPER : GLFW.GLFW_MOD_CONTROL;
            if ((e.modifiers & commandMask) == 0 || (e.modifiers & GLFW.GLFW_MOD_ALT) != 0) return;
            if (e.keyCode == GLFW.GLFW_KEY_C) { copySelection(); e.stopPropagation(); }
            else if (e.keyCode == GLFW.GLFW_KEY_A) { anchor = 0; caret = getText().getString().length(); e.stopPropagation(); }
        });
        addEventListener(UIEvents.VALIDATE_COMMAND, e -> {
            if (isFocused() && (CommandEvents.COPY.equals(e.command) || CommandEvents.SELECT_ALL.equals(e.command))) e.stopPropagation();
        });
        addEventListener(UIEvents.EXECUTE_COMMAND, e -> {
            if (!isFocused()) return;
            if (CommandEvents.COPY.equals(e.command)) {
                copySelection();
                e.stopPropagation();
            } else if (CommandEvents.SELECT_ALL.equals(e.command)) {
                anchor = 0;
                caret = getText().getString().length();
                e.stopPropagation();
            }
        });
    }

    private void copySelection() {
        if (anchor != caret) ClipboardManager.INSTANCE.copyDirect(getText().getString().substring(Math.min(anchor, caret), Math.max(anchor, caret)));
    }

    private void measure() {
        if (measured == getText() && measuredWidth == getContentWidth()) return;
        measured = getText();
        measuredWidth = getContentWidth();
        String plain = measured.getString();
        var result = new ArrayList<Line>();
        int offset = 0;
        float scale = getTextStyle().fontSize() / getFont().lineHeight;
        for (FormattedText line : getFont().getSplitter().splitLines(TextUtilities.withFont(measured, getTextStyle().font()),
                Math.max(1, (int) (measuredWidth / scale)), Style.EMPTY)) {
            String part = line.getString();
            int start = plain.indexOf(part, offset);
            if (start < 0) start = offset;
            int end = Math.min(plain.length(), start + part.length());
            result.add(new Line(start, end));
            offset = end;
            if (offset < plain.length() && (plain.charAt(offset) == '\n' || plain.charAt(offset) == ' ')) offset++;
        }
        lines = result;
    }

    private int cursor(float x, float y) {
        measure();
        if (lines.isEmpty()) return 0;
        if (y < getContentY()) return 0;
        int index = Math.clamp((int) ((y - getContentY()) / (getTextStyle().fontSize() + getTextStyle().lineSpacing())), 0, lines.size() - 1);
        var line = lines.get(index);
        String plain = getText().getString();
        float previous = 0;
        for (int pos = line.start; pos < line.end;) {
            int next = pos + Character.charCount(plain.codePointAt(pos));
            float width = width(line.start, next);
            if (x - getContentX() < (previous + width) / 2) return pos;
            previous = width;
            pos = next;
        }
        return line.end;
    }

    private float width(int start, int end) {
        return getFont().getSplitter().stringWidth(TextUtilities.withFont(slice(getText(), start, end), getTextStyle().font()))
                * getTextStyle().fontSize() / getFont().lineHeight;
    }

    private static Component slice(Component text, int start, int end) {
        var result = Component.empty();
        int[] offset = {0};
        text.visit((style, part) -> {
            int from = Math.max(0, start - offset[0]);
            int to = Math.min(part.length(), end - offset[0]);
            if (from < to) result.append(Component.literal(part.substring(from, to)).setStyle(style));
            offset[0] += part.length();
            return Optional.empty();
        }, Style.EMPTY);
        return result;
    }

    @Override
    public void drawBackgroundAdditional(GUIContext context) {
        super.drawBackgroundAdditional(context);
        if (anchor == caret) return;
        measure();
        for (int i = 0; i < lines.size(); i++) {
            var line = lines.get(i);
            int from = Math.max(line.start, Math.min(anchor, caret));
            int to = Math.min(line.end, Math.max(anchor, caret));
            if (from >= to) continue;
            float x = width(line.start, from);
            DrawerHelper.drawSolidRect(context.graphics, RenderType.guiTextHighlight(), getContentX() + x,
                    getContentY() + i * (getTextStyle().fontSize() + getTextStyle().lineSpacing()),
                    width(line.start, to) - x, getTextStyle().fontSize(), 0xFF0000FF);
        }
    }
}
