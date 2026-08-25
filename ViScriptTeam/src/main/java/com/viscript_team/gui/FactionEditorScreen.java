package com.viscript_team.gui;

import com.lowdragmc.lowdraglib2.configurator.ui.ConfiguratorGroup;
import com.lowdragmc.lowdraglib2.gui.holder.ModularUIScreen;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.ScrollDisplay;
import com.lowdragmc.lowdraglib2.gui.ui.data.ScrollerMode;
import com.lowdragmc.lowdraglib2.gui.ui.data.TextWrap;
import com.lowdragmc.lowdraglib2.gui.ui.data.Vertical;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Dialog;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextField;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEventListener;
import com.lowdragmc.lowdraglib2.gui.ui.style.StylesheetManager;
import com.lowdragmc.lowdraglib2.math.Size;
import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacketDistributor;
import com.viscript_team.network.c2s.C2SPayload;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexDirection;
import dev.vfyjxf.taffy.style.TaffyPosition;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@OnlyIn(Dist.CLIENT)
public class FactionEditorScreen extends UIElement {
    private static final String KEY = "viscript_team.faction_editor.";
    private static final float EDITOR_SCALE = 0.82f;
    private CompoundTag snapshot;
    private String selectedId = "";
    private String pendingSelection = "";

    private FactionEditorScreen(CompoundTag snapshot) {
        this.snapshot = snapshot;
        selectedId = firstFactionId(snapshot);
        layout(layout -> {
            layout.widthPercent(100);
            layout.heightPercent(100);
            layout.justifyContent(AlignContent.CENTER);
            layout.alignItems(AlignItems.CENTER);
        });
        rebuild();
    }

    public static void open(CompoundTag snapshot) {
        var root = new FactionEditorScreen(snapshot);
        var modularUI = new ModularUI(UI.of(
                root,
                List.of(StylesheetManager.INSTANCE.getStylesheetSafe(StylesheetManager.GDP)),
                FactionEditorScreen::getAutoGuiScaledSize
        ));
        Minecraft.getInstance().setScreen(new ModularUIScreen(
                modularUI,
                Component.translatable(KEY + "title")
        ));
    }

    public void applySnapshot(CompoundTag snapshot) {
        this.snapshot = snapshot;
        if (!pendingSelection.isBlank() && findFaction(pendingSelection) != null) {
            selectedId = pendingSelection;
        }
        pendingSelection = "";
        if (findFaction(selectedId) == null) {
            selectedId = firstFactionId(snapshot);
        }
        rebuild();

        String noticeKey = snapshot.getString("noticeKey");
        if (!noticeKey.isBlank()) {
            Dialog notification = Dialog.showNotification(noticeKey, 2f);
            notification.layout(layout -> layout.top(0));
            notification.show(this);
        }
    }

    @Override
    public void initScreen(int screenWidth, int screenHeight) {
        super.initScreen(screenWidth, screenHeight);
        transform(transform -> transform.pivot(0.5f, 0.5f).scale(getAutoGuiScaleFactor() * EDITOR_SCALE));
    }

    public static Size getAutoGuiScaledSize(Size screenSize) {
        float scale = getAutoGuiScaleFactor();
        if (scale <= 0f) {
            return screenSize;
        }
        return Size.of(
                Math.max(1, Math.round(screenSize.getWidth() / scale)),
                Math.max(1, Math.round(screenSize.getHeight() / scale))
        );
    }

    private static float getAutoGuiScaleFactor() {
        Minecraft minecraft = Minecraft.getInstance();
        double currentScale = minecraft.getWindow().getGuiScale();
        if (currentScale <= 0d) {
            return 1f;
        }
        int autoScale = minecraft.getWindow().calculateScale(0, minecraft.isEnforceUnicode());
        return Math.max(1f, (float) (autoScale / currentScale));
    }

    private void rebuild() {
        clearAllChildren();

        UIElement frame = column().layout(layout -> {
            layout.widthPercent(84);
            layout.heightPercent(84);
            layout.paddingAll(4);
            layout.gapAll(3);
        }).addClass("panel_bg");
        frame.addChildren(
                titleLabel(Component.translatable(KEY + "title")),
                wrappedLabel(Component.translatable(KEY + "scope_hint"))
                        .layout(layout -> layout.widthPercent(100)),
                row().layout(layout -> {
                    layout.widthPercent(100);
                    layout.flex(1);
                    layout.gapAll(5);
                }).addChildren(createFactionList(), createEditorPanel())
        );
        addChild(frame);
    }

    private UIElement createFactionList() {
        UIElement panel = column().layout(layout -> {
            layout.widthPercent(28);
            layout.heightPercent(100);
            layout.paddingAll(3);
            layout.gapAll(3);
        }).addClass("preview_bg");

        List<FactionEntry> factions = readFactions();
        panel.addChildren(
                sectionLabel(Component.translatable(KEY + "list", factions.size())),
                button(KEY + "action.create", event -> showCreateDialog()).layout(layout -> {
                    layout.widthPercent(100);
                    layout.height(18);
                })
        );

        ScrollerView list = verticalScroller();
        list.layout(layout -> layout.widthPercent(100).flex(1));
        panel.addChild(list);
        if (factions.isEmpty()) {
            list.addScrollViewChild(emptyState(KEY + "empty"));
            return panel;
        }

        for (FactionEntry faction : factions) {
            boolean selected = faction.id().equals(selectedId);
            Button factionButton = button(
                    Component.literal(faction.name()),
                    event -> {
                        selectedId = faction.id();
                        rebuild();
                    }
            );
            factionButton.layout(layout -> layout.widthPercent(100).height(22));
            factionButton.textStyle(style -> style
                    .adaptiveWidth(false)
                    .textAlignHorizontal(Horizontal.CENTER)
                    .textWrap(TextWrap.HOVER_ROLL));
            factionButton.text.layout(layout -> {
                layout.flex(1);
                layout.minWidth(0);
                layout.heightPercent(100);
                layout.marginHorizontal(12);
            });
            factionButton.text.setOverflowVisible(false);
            if (selected) {
                Label marker = label(Component.literal("▶"));
                marker.textStyle(style -> style
                        .fontSize(7)
                        .adaptiveWidth(false)
                        .textAlignHorizontal(Horizontal.CENTER));
                marker.layout(layout -> {
                    layout.positionType(TaffyPosition.ABSOLUTE);
                    layout.left(4);
                    layout.top(0);
                    layout.width(8);
                    layout.heightPercent(100);
                });
                marker.setOverflowVisible(false);
                factionButton.addChild(marker);
            }
            factionButton.style(style -> style.tooltips(Component.literal(faction.id())));
            list.addScrollViewChild(factionButton);
        }
        return panel;
    }

    private UIElement createEditorPanel() {
        UIElement panel = column().layout(layout -> {
            layout.flex(1);
            layout.heightPercent(100);
            layout.paddingAll(3);
            layout.gapAll(3);
        }).addClass("preview_bg");

        FactionEntry faction = findFaction(selectedId);
        if (faction == null) {
            panel.addChild(emptyState(KEY + "select_hint"));
            return panel;
        }

        panel.addChildren(
                titleLabel(Component.literal(faction.name())),
                smallLabel(Component.translatable(KEY + "immutable_id", faction.id()))
        );

        Set<String> factionIds = new HashSet<>();
        readFactions().forEach(entry -> factionIds.add(entry.id()));
        FactionEditorForm editorForm = new FactionEditorForm(
                faction.id(),
                faction.name(),
                faction.color(),
                faction.friendlyFire(),
                faction.attackEnemyFactions(),
                faction.enemyFactions(),
                factionIds
        );

        ScrollerView form = verticalScroller();
        form.layout(layout -> layout.widthPercent(100).flex(1));
        ConfiguratorGroup generatedForm = new ConfiguratorGroup("", false).hideTitle();
        generatedForm.setCanCollapse(false);
        generatedForm.configuratorContainer.layout(layout -> layout.paddingAll(3).gapAll(1));
        generatedForm.layout(layout -> layout.widthPercent(100));
        editorForm.buildConfigurator(generatedForm);
        form.addScrollViewChild(generatedForm);
        panel.addChild(form);

        Button save = button(KEY + "action.save", event -> {
            RPCPacketDistributor.rpcToServer(
                    C2SPayload.UPDATE_FACTION,
                    faction.id(),
                    editorForm.toSettingsTag()
            );
        });
        save.addClass("__confirm-button__");
        save.layout(layout -> layout.flex(1).height(20));

        Button delete = button(KEY + "action.delete", event -> showDeleteDialog(faction));
        delete.addClass("__reject-button__");
        delete.layout(layout -> layout.width(82).height(20));

        panel.addChild(row().layout(layout -> {
            layout.widthPercent(100);
            layout.gapAll(3);
        }).addChildren(save, delete));
        return panel;
    }

    private void showCreateDialog() {
        Dialog dialog = new Dialog();
        dialog.setTitle(KEY + "dialog.create.title");
        dialog.overlay.layout(layout -> layout.width(240));

        TextField id = textField("", KEY + "dialog.create.id");
        TextField name = textField("", KEY + "dialog.create.name");
        dialog.addContent(fieldRow(KEY + "field.id", id));
        dialog.addContent(fieldRow(KEY + "field.name", name));
        dialog.addContent(wrappedLabel(Component.translatable(KEY + "dialog.create.hint"))
                .layout(layout -> layout.widthPercent(100)));
        dialog.addButton(button(KEY + "action.create", event -> {
            pendingSelection = id.getText().trim().toLowerCase(Locale.ROOT);
            RPCPacketDistributor.rpcToServer(C2SPayload.CREATE_FACTION, id.getText(), name.getText());
            dialog.close();
        }).addClass("__confirm-button__"));
        dialog.addButton(closeButton(dialog));
        dialog.show(this);
    }

    private void showDeleteDialog(FactionEntry faction) {
        Dialog dialog = Dialog.showCheckBox(
                KEY + "dialog.delete.title",
                Component.translatable(KEY + "dialog.delete.content", faction.name(), faction.id()).getString(),
                confirmed -> {
                    if (confirmed) {
                        RPCPacketDistributor.rpcToServer(C2SPayload.DELETE_FACTION, faction.id());
                    }
                }
        );
        dialog.overlay.layout(layout -> layout.width(260));
        dialog.show(this);
    }

    private List<FactionEntry> readFactions() {
        List<FactionEntry> factions = new ArrayList<>();
        var tags = snapshot.getList("factions", Tag.TAG_COMPOUND);
        for (int i = 0; i < tags.size(); i++) {
            CompoundTag tag = tags.getCompound(i);
            List<String> enemies = new ArrayList<>();
            var enemyTags = tag.getList("enemyFactions", Tag.TAG_STRING);
            for (int j = 0; j < enemyTags.size(); j++) {
                enemies.add(enemyTags.getString(j));
            }
            factions.add(new FactionEntry(
                    tag.getString("id"),
                    tag.getString("name"),
                    tag.getInt("color"),
                    tag.getBoolean("friendlyFire"),
                    tag.getBoolean("attackEnemyFactions"),
                    List.copyOf(enemies)
            ));
        }
        return factions;
    }

    private FactionEntry findFaction(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        return readFactions().stream()
                .filter(faction -> faction.id().equals(id))
                .findFirst()
                .orElse(null);
    }

    private static String firstFactionId(CompoundTag snapshot) {
        var factions = snapshot.getList("factions", Tag.TAG_COMPOUND);
        return factions.isEmpty() ? "" : factions.getCompound(0).getString("id");
    }

    private static UIElement fieldRow(String key, UIElement input) {
        return fieldRow(Component.translatable(key), input);
    }

    private static UIElement fieldRow(Component text, UIElement input) {
        Label fieldLabel = label(text);
        fieldLabel.textStyle(style -> style.adaptiveWidth(false).textWrap(TextWrap.HOVER_ROLL));
        fieldLabel.layout(layout -> layout.width(82).height(18));
        input.layout(layout -> layout.flex(1).height(18));
        return row().layout(layout -> {
            layout.widthPercent(100);
            layout.height(22);
            layout.gapAll(4);
            layout.alignItems(AlignItems.CENTER);
        }).addChildren(fieldLabel, input);
    }

    private static TextField textField(String text, String placeholderKey) {
        TextField field = new TextField();
        field.setAnyString();
        field.setText(text, false);
        field.textFieldStyle(style -> style.placeholder(Component.translatable(placeholderKey)));
        field.layout(layout -> layout.paddingLeft(3));
        return field;
    }

    private static Button closeButton(Dialog dialog) {
        Button button = button(KEY + "dialog.close", event -> dialog.close());
        button.addClass("__cancel-button__");
        return button;
    }

    private static Button button(String key, UIEventListener listener) {
        return button(Component.translatable(key), listener);
    }

    private static Button button(Component text, UIEventListener listener) {
        Button button = new Button();
        button.setText(text);
        button.setOnClick(listener);
        button.textStyle(style -> style
                .fontSize(7)
                .textAlignHorizontal(Horizontal.CENTER)
                .textAlignVertical(Vertical.CENTER)
                .textWrap(TextWrap.HIDE));
        return button;
    }

    private static Label titleLabel(Component text) {
        Label label = label(text);
        label.textStyle(style -> style.fontSize(9).textWrap(TextWrap.HIDE));
        label.layout(layout -> layout.widthPercent(100).height(14));
        return label;
    }

    private static Label sectionLabel(Component text) {
        Label label = label(text);
        label.layout(layout -> layout.widthPercent(100).height(12));
        return label;
    }

    private static Label label(Component text) {
        Label label = new Label();
        label.setText(text);
        label.textStyle(style -> style
                .fontSize(8)
                .textAlignVertical(Vertical.CENTER)
                .textWrap(TextWrap.HIDE));
        return label;
    }

    private static Label smallLabel(Component text) {
        Label label = label(text);
        label.textStyle(style -> style.fontSize(6).textWrap(TextWrap.HIDE));
        return label;
    }

    private static Label wrappedLabel(Component text) {
        Label label = label(text);
        label.textStyle(style -> style.textWrap(TextWrap.WRAP).adaptiveHeight(true));
        return label;
    }

    private static UIElement emptyState(String key) {
        Label label = wrappedLabel(Component.translatable(key));
        label.textStyle(style -> style
                .textAlignHorizontal(Horizontal.CENTER)
                .textAlignVertical(Vertical.CENTER));
        label.layout(layout -> layout.widthPercent(100).height(30).paddingAll(3));
        return label;
    }

    private static ScrollerView verticalScroller() {
        ScrollerView scroller = new ScrollerView();
        scroller.scrollerStyle(style -> style
                .mode(ScrollerMode.VERTICAL)
                .horizontalScrollDisplay(ScrollDisplay.NEVER)
                .verticalScrollDisplay(ScrollDisplay.AUTO)
                .scrollerViewStyle(1));
        scroller.viewContainer(view -> view.layout(layout -> {
            layout.widthPercent(100);
            layout.flexDirection(FlexDirection.COLUMN);
            layout.gapAll(2);
            layout.paddingAll(2);
        }));
        return scroller;
    }

    private static UIElement row() {
        return new UIElement().layout(layout -> layout.flexDirection(FlexDirection.ROW));
    }

    private static UIElement column() {
        return new UIElement().layout(layout -> layout.flexDirection(FlexDirection.COLUMN));
    }

    private record FactionEntry(String id, String name, int color,
                                boolean friendlyFire, boolean attackEnemyFactions,
                                List<String> enemyFactions) {
    }
}
