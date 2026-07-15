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

final class RecipeEditorUi {
    private RecipeEditorUi() {
    }

    static UIElement row() {
        return new UIElement().layout(layout -> layout.flexDirection(FlexDirection.ROW));
    }

    static UIElement column() {
        return new UIElement().layout(layout -> layout.flexDirection(FlexDirection.COLUMN));
    }

    static Label label(Component text) {
        var label = new Label();
        label.setText(text);
        label.textStyle(style -> style
                .textAlignHorizontal(Horizontal.LEFT)
                .textAlignVertical(Vertical.CENTER)
                .textWrap(TextWrap.HOVER_ROLL));
        return label;
    }

    static Label sectionTitle(String key) {
        var label = label(Component.translatable(key));
        label.textStyle(style -> style
                .textColor(ColorPattern.WHITE.color)
                .textWrap(TextWrap.HOVER_ROLL));
        label.layout(layout -> layout.height(16));
        return label;
    }

    static UIElement fieldGroup(String labelKey, UIElement input) {
        return fieldGroup(labelKey, input, new Component[0]);
    }

    static UIElement fieldGroup(String labelKey, UIElement input, Component... tooltips) {
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

    static Button iconButton(IGuiTexture icon, String tooltipKey, UIEventListener onClick) {
        var button = new Button()
                .noText()
                .addPreIcon(icon)
                .setOnClick(onClick);
        button.style(style -> style.tooltips(Component.translatable(tooltipKey)));
        return button;
    }

    static Button textButton(Component text, @Nullable IGuiTexture icon, UIEventListener onClick) {
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

    static <T> Selector<T> selector(List<T> candidates, T value, Function<T, Component> name, Consumer<T> consumer) {
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

    static TextField textField(String value, Consumer<String> consumer) {
        var textField = new TextField()
                .setText(value == null ? "" : value, false)
                .setTextResponder(consumer);
        textField.layout(layout -> {
            layout.widthPercent(100);
            layout.height(16);
        });
        return textField;
    }

    static TextField resourceLocationField(@Nullable ResourceLocation value, Consumer<ResourceLocation> consumer) {
        var field = textField(value == null ? "" : value.toString(), text -> {
            var parsed = ResourceLocation.tryParse(text);
            if (parsed != null) {
                consumer.accept(parsed);
            }
        });
        field.setResourceLocationOnly();
        return field;
    }

    static TextField intField(int value, int min, int max, Consumer<Integer> consumer) {
        var field = textField(String.valueOf(value), text -> {
            try {
                consumer.accept(Integer.parseInt(text));
            } catch (NumberFormatException ignored) {
            }
        });
        field.setNumbersOnlyInt(min, max);
        return field;
    }

    static TextField floatField(float value, float min, float max, Consumer<Float> consumer) {
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

    static UIElement panelRoot() {
        return column().layout(layout -> {
            layout.widthPercent(100);
            layout.heightPercent(100);
            layout.paddingAll(4);
            layout.gapAll(5);
        }).style(style -> style.backgroundTexture(Sprites.RECT_SOLID));
    }
}
