package com.viscript_lib.gui.components.dev;

import com.lowdragmc.lowdraglib2.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.ScrollDisplay;
import com.lowdragmc.lowdraglib2.gui.ui.data.ScrollerMode;
import com.lowdragmc.lowdraglib2.gui.ui.data.TextWrap;
import com.lowdragmc.lowdraglib2.gui.ui.data.Vertical;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import com.lowdragmc.lowdraglib2.gui.ui.styletemplate.Sprites;
import com.lowdragmc.lowdraglib2.registry.RegistrationEnvironment;
import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegisterClient;
import com.lowdragmc.lowdraglib2.test.ui.IScreenTest;
import com.viscript_lib.gui.components.DraggableUI;
import dev.vfyjxf.taffy.style.FlexDirection;
import lombok.NoArgsConstructor;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.stream.IntStream;

@LDLRegisterClient(
        name = "viscript_draggable_ui",
        registry = "ldlib2:screen_test",
        environment = RegistrationEnvironment.DEV_ONLY
)
@NoArgsConstructor
public class DraggableUIScreenTest implements IScreenTest {

    @Override
    public ModularUI createUI(Player entityPlayer) {
        List<String> items = IntStream.rangeClosed(1, 36)
                .mapToObj(i -> "Item " + String.format("%02d", i))
                .toList();

        Label orderLabel = new Label();
        orderLabel.setText(orderSummary(items));
        orderLabel.textStyle(style -> style
                .fontSize(10)
                .textWrap(TextWrap.ROLL)
                .textAlignVertical(Vertical.CENTER));
        orderLabel.setOverflowVisible(false);
        orderLabel.layout(layout -> layout.height(16));

        var draggable = new DraggableUI<String>(items, order -> orderLabel.setText(orderSummary(order)))
                .setAutoScrollSpeed(2.5f, 14.0f, 52.0f);
        draggable.layout(layout -> {
            layout.widthPercent(100);
            layout.paddingAll(6);
            layout.gapAll(6);
        });

        for (int i = 0; i < items.size(); i++) {
            String item = items.get(i);
            draggable.addSortableCard(item, createCard(i, item));
        }

        var scrollerView = new ScrollerView()
                .scrollerStyle(style -> style
                        .mode(ScrollerMode.VERTICAL)
                        .verticalScrollDisplay(ScrollDisplay.ALWAYS)
                        .minScrollPixel(6)
                        .maxScrollPixel(12));
        scrollerView.layout(layout -> {
            layout.widthPercent(100);
            layout.height(230);
        });
        scrollerView.addScrollViewChild(draggable);

        var root = new UIElement().layout(layout -> {
            layout.width(330);
            layout.height(285);
            layout.paddingAll(8);
            layout.gapRow(6);
        }).style(style -> style.backgroundTexture(Sprites.BORDER));

        root.addChildren(
                new Label()
                        .setText("DraggableUI")
                        .textStyle(style -> style
                                .fontSize(15)
                                .textAlignHorizontal(Horizontal.CENTER)
                                .textAlignVertical(Vertical.CENTER))
                        .layout(layout -> layout.height(20)),
                scrollerView,
                orderLabel
        );

        return new ModularUI(UI.of(root), entityPlayer)
                .shouldCloseOnEsc(true)
                .shouldCloseOnKeyInventory(false);
    }

    private static UIElement createCard(int index, String name) {
        int background = index % 2 == 0 ? 0xff2f4050 : 0xff38475a;
        int stripe = index % 3 == 0 ? 0xfff0b84a : index % 3 == 1 ? 0xff5dc3a7 : 0xffd96d61;

        var stripeElement = new UIElement()
                .layout(layout -> {
                    layout.width(5);
                    layout.heightPercent(100);
                })
                .style(style -> style.backgroundTexture(new ColorRectTexture(stripe)));

        var label = new Label()
                .setText(name)
                .textStyle(style -> style
                        .fontSize(12)
                        .textColor(0xffffffff)
                        .textAlignVertical(Vertical.CENTER))
                .layout(layout -> {
                    layout.flex(1);
                    layout.heightPercent(100);
                });

        return new UIElement()
                .layout(layout -> {
                    layout.widthPercent(100);
                    layout.height(34);
                    layout.paddingAll(4);
                    layout.gapColumn(6);
                    layout.flexDirection(FlexDirection.ROW);
                })
                .style(style -> style.backgroundTexture(new ColorRectTexture(background)))
                .addChildren(stripeElement, label);
    }

    private static String orderSummary(List<String> order) {
        if (order.isEmpty()) {
            return "Order: empty";
        }
        return "Top: " + order.getFirst() + "    Bottom: " + order.getLast();
    }
}
