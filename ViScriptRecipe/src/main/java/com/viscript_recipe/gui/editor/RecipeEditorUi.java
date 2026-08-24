package com.viscript_recipe.gui.editor;

import com.lowdragmc.lowdraglib2.gui.ColorPattern;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.TextWrap;
import com.lowdragmc.lowdraglib2.gui.ui.data.Vertical;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Selector;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextField;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEventListener;
import com.lowdragmc.lowdraglib2.gui.ui.styletemplate.Sprites;
import dev.vfyjxf.taffy.style.FlexDirection;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public final class RecipeEditorUi {
    private RecipeEditorUi() {
    }

    public static UIElement row() {
        return new UIElement().layout(layout -> layout.flexDirection(FlexDirection.ROW));
    }

    public static UIElement column() {
        return new UIElement().layout(layout -> layout.flexDirection(FlexDirection.COLUMN));
    }

    public static Label label(Component text) {
        var label = new Label();
        label.setText(text);
        label.textStyle(style -> style
                .textAlignHorizontal(Horizontal.LEFT)
                .textAlignVertical(Vertical.CENTER)
                .textWrap(TextWrap.HOVER_ROLL));
        return label;
    }

    public static Label sectionTitle(String key) {
        var label = label(Component.translatable(key));
        label.textStyle(style -> style
                .textColor(ColorPattern.WHITE.color)
                .textWrap(TextWrap.HOVER_ROLL));
        label.layout(layout -> layout.height(16));
        return label;
    }

    public static UIElement fieldGroup(String labelKey, UIElement input) {
        return fieldGroup(labelKey, input, new Component[0]);
    }

    public static UIElement fieldGroup(String labelKey, UIElement input, Component... tooltips) {
        var label = label(Component.translatable(labelKey)).textStyle(style -> style.textColor(ColorPattern.LIGHT_GRAY.color));
        var group = column().layout(layout -> {
            layout.widthPercent(100);
            layout.gapAll(2);
        }).addChildren(
                label,
                input
        );
        if (tooltips.length > 0) {
            group.style(style -> style.tooltips(tooltips));
            label.style(style -> style.tooltips(tooltips));
            input.style(style -> style.tooltips(tooltips));
        }
        return group;
    }

    public static Button iconButton(IGuiTexture icon, String tooltipKey, UIEventListener onClick) {
        var button = new Button()
                .noText()
                .addPreIcon(icon)
                .setOnClick(onClick);
        button.style(style -> style.tooltips(Component.translatable(tooltipKey)));
        return button;
    }

    public static Button textButton(Component text, @Nullable IGuiTexture icon, UIEventListener onClick) {
        var button = new Button()
                .setText(text)
                .setOnClick(onClick)
                .textStyle(style -> style
                        .textAlignHorizontal(Horizontal.LEFT)
                        .textWrap(TextWrap.HOVER_ROLL));
        if (icon != null && icon != IGuiTexture.EMPTY) {
            button.addPreIcon(icon);
        }
        return button;
    }

    public static <T> Selector<T> selector(List<T> candidates, T value, Function<T, Component> name, Consumer<T> consumer) {
        var selector = new Selector<T>()
                .setCandidates(candidates)
                .setCandidateUIProvider(candidate -> label(candidate == null ? Component.literal("---") : name.apply(candidate)).layout(layout -> {
                    layout.widthPercent(100);
                    layout.height(12);
                }))
                .setSelected(value, false)
                .setOnValueChanged(consumer);
        selector.layout(layout -> {
            layout.widthPercent(100);
            layout.height(18);
        });
        return selector;
    }

    public static TextField textField(String value, Consumer<String> consumer) {
        var textField = new TextField()
                .setText(value == null ? "" : value, false)
                .setTextResponder(consumer);
        textField.layout(layout -> {
            layout.widthPercent(100);
            layout.height(16);
        });
        return textField;
    }

    public static TextField resourceLocationField(@Nullable ResourceLocation value, Consumer<ResourceLocation> consumer) {
        var field = textField(value == null ? "" : value.toString(), text -> {
            var parsed = ResourceLocation.tryParse(text);
            if (parsed != null) {
                consumer.accept(parsed);
            }
        });
        field.setResourceLocationOnly();
        return field;
    }

    public static TextField intField(int value, int min, int max, Consumer<Integer> consumer) {
        var field = textField(String.valueOf(value), text -> {
            try {
                consumer.accept(Integer.parseInt(text));
            } catch (NumberFormatException ignored) {
            }
        });
        field.setNumbersOnlyInt(min, max);
        return field;
    }

    public static TextField longField(long value, long min, long max, Consumer<Long> consumer) {
        var field = textField(String.valueOf(value), text -> {
            try {
                consumer.accept(Long.parseLong(text));
            } catch (NumberFormatException ignored) {
            }
        });
        field.setNumbersOnlyLong(min, max);
        return field;
    }

    public static TextField floatField(float value, float min, float max, Consumer<Float> consumer) {
        var field = textField(String.valueOf(value), text -> {
            try {
                var parsed = Float.parseFloat(text);
                if (parsed >= min && parsed <= max) {
                    consumer.accept(parsed);
                }
            } catch (NumberFormatException ignored) {
            }
        });
        field.setNumbersOnlyFloat(min, max);
        return field;
    }

    public static TextField doubleField(double value, double min, double max, Consumer<Double> consumer) {
        var field = textField(String.valueOf(value), text -> {
            try {
                var parsed = Double.parseDouble(text);
                if (parsed >= min && parsed <= max) {
                    consumer.accept(parsed);
                }
            } catch (NumberFormatException ignored) {
            }
        });
        field.setNumbersOnlyDouble(min, max);
        return field;
    }

    public static UIElement panelRoot() {
        return column().layout(layout -> {
            layout.widthPercent(100);
            layout.heightPercent(100);
            layout.paddingAll(4);
            layout.gapAll(5);
        }).style(style -> style.backgroundTexture(Sprites.RECT_SOLID));
    }
}
