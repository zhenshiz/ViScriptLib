package com.viscript_team.gui;

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
import com.lowdragmc.lowdraglib2.gui.ui.elements.Menu;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Switch;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextField;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvent;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEventListener;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.gui.ui.style.StylesheetManager;
import com.lowdragmc.lowdraglib2.gui.util.TreeBuilder;
import com.lowdragmc.lowdraglib2.math.Size;
import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacketDistributor;
import com.viscript_team.network.c2s.C2SPayload;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexDirection;
import dev.vfyjxf.taffy.style.FlexWrap;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@OnlyIn(Dist.CLIENT)
public class PartyScreen extends UIElement {
    private static final String KEY = "viscript_team.party_ui.";
    private static final UUID EMPTY_UUID = new UUID(0L, 0L);
    private CompoundTag snapshot;
    private String chatDraft = "";

    private PartyScreen(CompoundTag snapshot) {
        this.snapshot = snapshot;
        layout(layout -> {
            layout.widthPercent(100);
            layout.heightPercent(100);
            layout.justifyContent(AlignContent.CENTER);
            layout.alignItems(AlignItems.CENTER);
        });
        rebuild();
    }

    public static void open(CompoundTag snapshot) {
        var root = new PartyScreen(snapshot);
        var modularUI = new ModularUI(UI.of(
                root,
                List.of(StylesheetManager.INSTANCE.getStylesheetSafe(StylesheetManager.GDP)),
                PartyScreen::getAutoGuiScaledSize
        ));
        Minecraft.getInstance().setScreen(new ModularUIScreen(
                modularUI,
                Component.translatable(KEY + "title")
        ));
    }

    public void applySnapshot(CompoundTag snapshot) {
        this.snapshot = snapshot;
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
        transform(transform -> transform.pivot(0.5f, 0.5f).scale(getAutoGuiScaleFactor()));
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
            layout.widthPercent(90);
            layout.heightPercent(91);
            layout.gapAll(3);
        });

        UIElement body = row().layout(layout -> {
            layout.widthPercent(100);
            layout.flex(1);
            layout.gapAll(3);
        });
        body.addChildren(createPlayerPanel(), createChatPanel());
        frame.addChild(body);
        addChild(frame);
    }

    private UIElement createPlayerPanel() {
        boolean hasParty = snapshot.getBoolean("hasParty");
        CompoundTag party = snapshot.getCompound("party");
        boolean isLeader = hasParty && party.getBoolean("isLeader");

        UIElement panel = column().layout(layout -> {
            layout.widthPercent(35);
            layout.heightPercent(100);
            layout.paddingAll(5);
            layout.gapAll(5);
        }).addClass("panel_bg");

        UIElement header = column().layout(layout -> {
            layout.widthPercent(100);
            layout.gapAll(3);
        });
        header.addChildren(
                titleLabel(hasParty
                        ? Component.literal(party.getString("name"))
                        : Component.translatable(KEY + "personal")),
                label(Component.translatable(hasParty
                        ? KEY + (isLeader ? "role.leader" : "role.member")
                        : KEY + "role.solo"))
        );

        UIElement actions = row().layout(layout -> {
            layout.widthPercent(100);
            layout.gapAll(3);
            layout.flexWrap(FlexWrap.WRAP);
        });
        if (!hasParty) {
            actions.addChildren(
                    actionButton(KEY + "action.create", event -> showCreatePartyDialog()),
                    actionButton(KEY + "action.browse", event -> showBrowsePartiesDialog()),
                    actionButton(KEY + "action.invitations", event -> showInvitationsDialog())
            );
        } else if (isLeader) {
            actions.addChildren(
                    actionButton(KEY + "action.invite", event -> showInvitePlayerDialog()),
                    actionButton(KEY + "action.requests", event -> showJoinRequestsDialog()),
                    actionButton(KEY + "action.disband", event -> showDisbandDialog()),
                    actionButton(KEY + "action.settings", event -> showSettingsDialog())
            );
        } else {
            actions.addChild(actionButton(KEY + "action.leave", event -> showLeaveDialog()));
        }
        header.addChild(actions);

        ScrollerView members = verticalScroller();
        members.layout(layout -> {
            layout.widthPercent(100);
            layout.flex(1);
        });

        List<PlayerEntry> memberEntries;
        if (hasParty) {
            memberEntries = readPlayers(party, "members");
        } else {
            memberEntries = List.of(new PlayerEntry(
                    snapshot.hasUUID("viewerId") ? snapshot.getUUID("viewerId") : EMPTY_UUID,
                    snapshot.getString("viewerName"),
                    true,
                    false
            ));
        }

        panel.addChildren(header, sectionLabel(Component.translatable(KEY + "members", memberEntries.size())), members);
        for (PlayerEntry member : memberEntries) {
            members.addScrollViewChild(createMemberRow(member, isLeader));
        }
        return panel;
    }

    private UIElement createMemberRow(PlayerEntry member, boolean viewerIsLeader) {
        UUID viewerId = snapshot.hasUUID("viewerId") ? snapshot.getUUID("viewerId") : EMPTY_UUID;
        boolean self = viewerId.equals(member.id());
        UIElement row = row().layout(layout -> {
            layout.widthPercent(100);
            layout.height(34);
            layout.paddingAll(5);
            layout.gapAll(5);
            layout.alignItems(AlignItems.CENTER);
        }).addClass("preview_bg");
        row.setOverflowVisible(false);

        UIElement info = column().layout(layout -> {
            layout.flex(1);
            layout.minWidth(0);
            layout.gapAll(2);
            layout.justifyContent(AlignContent.CENTER);
        });
        info.setOverflowVisible(false);
        Label memberName = label(self
                ? Component.translatable(KEY + "member.self", member.name())
                : Component.literal(member.name()));
        memberName.textStyle(style -> style.adaptiveWidth(false).textWrap(TextWrap.HOVER_ROLL));
        memberName.layout(layout -> layout.widthPercent(100));
        memberName.setOverflowVisible(false);
        Label memberRole = smallLabel(Component.translatable(
                !snapshot.getBoolean("hasParty")
                        ? KEY + "role.solo"
                        : member.leader() ? KEY + "role.leader" : KEY + "role.member"
        ));
        memberRole.textStyle(style -> style.adaptiveWidth(false));
        memberRole.layout(layout -> layout.widthPercent(100));
        memberRole.setOverflowVisible(false);
        info.addChildren(memberName, memberRole);

        Label status = smallLabel(Component.translatable(
                member.online() ? KEY + "status.online" : KEY + "status.offline"
        ));
        status.textStyle(style -> style
                .adaptiveWidth(false)
                .textAlignHorizontal(Horizontal.RIGHT));
        status.layout(layout -> layout.width(32).height(14));
        status.setOverflowVisible(false);
        row.addChildren(info, status);

        if (viewerIsLeader && !self) {
            row.style(style -> style.tooltips(Component.translatable(KEY + "member.right_click")));
            row.addEventListener(UIEvents.MOUSE_DOWN, event -> {
                if (event.button == 1) {
                    showMemberMenu(event, member);
                    event.stopPropagation();
                }
            });
        }
        return row;
    }

    private UIElement createChatPanel() {
        boolean hasParty = snapshot.getBoolean("hasParty");
        CompoundTag party = snapshot.getCompound("party");
        UIElement panel = column().layout(layout -> {
            layout.flex(1);
            layout.heightPercent(100);
            layout.paddingAll(5);
            layout.gapAll(5);
        }).addClass("panel_bg");

        panel.addChild(titleLabel(Component.translatable(KEY + "chat.title")));
        ScrollerView messages = verticalScroller();
        messages.layout(layout -> {
            layout.widthPercent(100);
            layout.flex(1);
        });
        panel.addChild(messages);

        if (!hasParty) {
            messages.addScrollViewChild(emptyState(KEY + "chat.no_party"));
            return panel;
        }

        List<ChatEntry> chatEntries = readChat(party);
        if (chatEntries.isEmpty()) {
            messages.addScrollViewChild(emptyState(KEY + "chat.empty"));
        } else {
            for (ChatEntry message : chatEntries) {
                messages.addScrollViewChild(chatMessage(message));
            }
            messages.verticalScroller.setValue(1f, false);
        }

        TextField field = new TextField();
        field.setAnyString();
        field.setText(chatDraft, false);
        field.setTextResponder(value -> chatDraft = value);
        field.textFieldStyle(style -> style.placeholder(Component.translatable(KEY + "chat.placeholder")));
        field.layout(layout -> {
            layout.flex(1);
            layout.heightPercent(100);
            layout.paddingLeft(5);
        });

        Runnable send = () -> {
            String message = field.getText().trim();
            if (message.isEmpty()) {
                return;
            }
            chatDraft = "";
            field.setText("", false);
            RPCPacketDistributor.rpcToServer(C2SPayload.SEND_CHAT_MESSAGE, message);
        };
        field.addEventListener(UIEvents.KEY_DOWN, event -> {
            if (event.keyCode == GLFW.GLFW_KEY_ENTER || event.keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                send.run();
                event.stopPropagation();
            }
        });

        UIElement composer = row().layout(layout -> {
            layout.widthPercent(100);
            layout.height(24);
            layout.gapAll(4);
        });
        composer.addChildren(
                field,
                button(KEY + "chat.send", event -> send.run()).layout(layout -> {
                    layout.width(58);
                    layout.heightPercent(100);
                })
        );
        panel.addChild(composer);
        return panel;
    }

    private UIElement chatMessage(ChatEntry message) {
        UIElement row = column().layout(layout -> {
            layout.widthPercent(100);
            layout.paddingAll(5);
            layout.gapAll(2);
        }).addClass("preview_bg");
        row.addChildren(
                smallLabel(Component.literal(message.senderName())),
                wrappedLabel(Component.literal(message.content()))
        );
        return row;
    }

    private void showCreatePartyDialog() {
        Dialog dialog = new Dialog();
        dialog.setTitle(KEY + "dialog.create.title");
        dialog.overlay.layout(layout -> layout.width(250));
        TextField name = new TextField();
        name.setAnyString();
        name.textFieldStyle(style -> style.placeholder(Component.translatable(KEY + "dialog.create.placeholder")));
        name.layout(layout -> layout.widthPercent(100).height(22).paddingLeft(5));
        dialog.addContent(name);
        dialog.addButton(button(KEY + "action.create", event -> {
            RPCPacketDistributor.rpcToServer(C2SPayload.CREATE_PARTY, name.getText());
            dialog.close();
        }).addClass("__confirm-button__"));
        dialog.addButton(closeButton(dialog));
        dialog.show(this);
    }

    private void showBrowsePartiesDialog() {
        Dialog dialog = listDialog(KEY + "dialog.browse.title");
        ScrollerView list = dialogList();
        List<PartyEntry> parties = readParties(snapshot, "availableParties");
        if (parties.isEmpty()) {
            list.addScrollViewChild(emptyState(KEY + "dialog.browse.empty"));
        } else {
            for (PartyEntry party : parties) {
                Button apply = button(party.invited()
                                ? KEY + "dialog.browse.invited"
                                : party.applied() ? KEY + "dialog.browse.applied" : KEY + "dialog.browse.apply",
                        event -> {
                            RPCPacketDistributor.rpcToServer(C2SPayload.APPLY_TO_PARTY, party.id());
                            dialog.close();
                        });
                if (party.applied() || party.invited()) {
                    apply.setActive(false);
                }
                list.addScrollViewChild(partyRow(party, apply));
            }
        }
        dialog.addContent(list);
        dialog.addButton(closeButton(dialog));
        dialog.show(this);
    }

    private void showInvitationsDialog() {
        Dialog dialog = listDialog(KEY + "dialog.invitations.title");
        ScrollerView list = dialogList();
        List<PartyEntry> parties = readParties(snapshot, "invitations");
        if (parties.isEmpty()) {
            list.addScrollViewChild(emptyState(KEY + "dialog.invitations.empty"));
        } else {
            for (PartyEntry party : parties) {
                UIElement actions = row().layout(layout -> layout.gapAll(3));
                actions.addChildren(
                        button(KEY + "dialog.accept", event -> {
                            RPCPacketDistributor.rpcToServer(C2SPayload.ACCEPT_INVITATION, party.id());
                            dialog.close();
                        }),
                        button(KEY + "dialog.decline", event -> {
                            RPCPacketDistributor.rpcToServer(C2SPayload.DECLINE_INVITATION, party.id());
                            dialog.close();
                        })
                );
                list.addScrollViewChild(partyRow(party, actions));
            }
        }
        dialog.addContent(list);
        dialog.addButton(closeButton(dialog));
        dialog.show(this);
    }

    private void showInvitePlayerDialog() {
        Dialog dialog = new Dialog();
        dialog.setTitle(KEY + "dialog.invite.title");
        dialog.overlay.layout(layout -> layout.width(260));
        dialog.overlay.transform(transform -> transform
                .pivot(0.5f, 0.5f)
                .scale(1f / getAutoGuiScaleFactor()));

        ScrollerView list = dialogList();
        list.layout(layout -> layout.height(112));
        List<PlayerEntry> players = readPlayers(snapshot.getCompound("party"), "invitablePlayers");

        TextField search = new TextField();
        search.setAnyString();
        search.textFieldStyle(style -> style.placeholder(Component.translatable(KEY + "dialog.invite.search")));
        search.layout(layout -> layout.widthPercent(100).height(22).paddingLeft(5));
        search.setTextResponder(query -> populateInvitablePlayers(dialog, list, players, query));

        dialog.addContent(search);
        populateInvitablePlayers(dialog, list, players, "");
        dialog.addContent(list);
        dialog.addButton(closeButton(dialog));
        dialog.show(this);
    }

    private void populateInvitablePlayers(Dialog dialog, ScrollerView list, List<PlayerEntry> players, String query) {
        list.clearAllScrollViewChildren();
        String normalizedQuery = query.trim().toLowerCase(Locale.ROOT);
        int matches = 0;
        for (PlayerEntry player : players) {
            if (!normalizedQuery.isEmpty()
                    && !player.name().toLowerCase(Locale.ROOT).contains(normalizedQuery)
                    && !player.id().toString().toLowerCase(Locale.ROOT).contains(normalizedQuery)) {
                continue;
            }
            matches++;
            list.addScrollViewChild(playerActionRow(player, button(KEY + "action.invite", event -> {
                RPCPacketDistributor.rpcToServer(C2SPayload.INVITE_PLAYER, player.id().toString());
                dialog.close();
            })));
        }
        if (matches == 0) {
            list.addScrollViewChild(emptyState(KEY + (players.isEmpty()
                    ? "dialog.invite.empty"
                    : "dialog.invite.no_results")));
        }
        list.verticalScroller.setValue(0f, false);
    }

    private void showJoinRequestsDialog() {
        Dialog dialog = listDialog(KEY + "dialog.requests.title");
        ScrollerView list = dialogList();
        List<PlayerEntry> players = readPlayers(snapshot.getCompound("party"), "joinRequests");
        if (players.isEmpty()) {
            list.addScrollViewChild(emptyState(KEY + "dialog.requests.empty"));
        } else {
            for (PlayerEntry player : players) {
                UIElement actions = row().layout(layout -> layout.gapAll(3));
                actions.addChildren(
                        button(KEY + "dialog.accept", event -> {
                            RPCPacketDistributor.rpcToServer(C2SPayload.ACCEPT_JOIN_REQUEST, player.id().toString());
                            dialog.close();
                        }),
                        button(KEY + "dialog.reject", event -> {
                            RPCPacketDistributor.rpcToServer(C2SPayload.REJECT_JOIN_REQUEST, player.id().toString());
                            dialog.close();
                        })
                );
                list.addScrollViewChild(playerActionRow(player, actions));
            }
        }
        dialog.addContent(list);
        dialog.addButton(closeButton(dialog));
        dialog.show(this);
    }

    private void showDisbandDialog() {
        confirmDialog(
                KEY + "dialog.disband.title",
                Component.translatable(KEY + "dialog.disband.content"),
                () -> RPCPacketDistributor.rpcToServer(C2SPayload.DISBAND_PARTY)
        );
    }

    private void showLeaveDialog() {
        confirmDialog(
                KEY + "dialog.leave.title",
                Component.translatable(KEY + "dialog.leave.content"),
                () -> RPCPacketDistributor.rpcToServer(C2SPayload.LEAVE_PARTY)
        );
    }

    private void showSettingsDialog() {
        CompoundTag party = snapshot.getCompound("party");
        Dialog dialog = new Dialog();
        dialog.setTitle(KEY + "dialog.settings.title");
        dialog.overlay.layout(layout -> layout.width(250));

        Switch friendlyFire = new Switch();
        friendlyFire.setOn(party.getBoolean("friendlyFire"), false);
        friendlyFire.setOnSwitchChanged(enabled -> {
            RPCPacketDistributor.rpcToServer(C2SPayload.SET_FRIENDLY_FIRE, enabled);
            dialog.close();
        });

        Label friendlyFireLabel = label(Component.translatable(KEY + "settings.friendly_fire"));
        friendlyFireLabel.textStyle(style -> style
                .adaptiveWidth(false)
                .textWrap(TextWrap.HOVER_ROLL));
        friendlyFireLabel.layout(layout -> {
            layout.flex(1);
            layout.minWidth(0);
            layout.height(14);
        });
        friendlyFireLabel.setOverflowVisible(false);

        UIElement setting = row().layout(layout -> {
            layout.widthPercent(100);
            layout.height(26);
            layout.gapAll(8);
            layout.alignItems(AlignItems.CENTER);
        });
        setting.addChildren(friendlyFireLabel, friendlyFire);
        dialog.addContent(setting);
        dialog.addButton(closeButton(dialog));
        dialog.show(this);
    }

    private void showMemberMenu(UIEvent event, PlayerEntry member) {
        var builder = TreeBuilder.Menu.start()
                .leaf(KEY + "member.kick", () -> confirmDialog(
                        KEY + "dialog.kick.title",
                        Component.translatable(KEY + "dialog.kick.content", member.name()),
                        () -> RPCPacketDistributor.rpcToServer(C2SPayload.KICK_MEMBER, member.id().toString())
                ))
                .leaf(KEY + "member.transfer", () -> confirmDialog(
                        KEY + "dialog.transfer.title",
                        Component.translatable(KEY + "dialog.transfer.content", member.name()),
                        () -> RPCPacketDistributor.rpcToServer(C2SPayload.TRANSFER_LEADERSHIP, member.id().toString())
                ));
        Vector2f offset = worldToLocalLayoutOffset(new Vector2f(event.x, event.y));
        addChild(new Menu<>(builder.build(), TreeBuilder.Menu::uiProvider)
                .setHoverTextureProvider(TreeBuilder.Menu::hoverTextureProvider)
                .setOnNodeClicked(TreeBuilder.Menu::handle)
                .layout(layout -> {
                    layout.left(offset.x);
                    layout.top(offset.y);
                }));
    }

    private void confirmDialog(String titleKey, Component content, Runnable confirmed) {
        Dialog dialog = new Dialog();
        dialog.setTitle(titleKey);
        dialog.overlay.layout(layout -> layout.width(250));
        dialog.addContent(wrappedLabel(content).layout(layout -> layout.widthPercent(100)));
        dialog.addButton(button(KEY + "dialog.confirm", event -> {
            confirmed.run();
            dialog.close();
        }).addClass("__confirm-button__"));
        dialog.addButton(closeButton(dialog));
        dialog.show(this);
    }

    private static UIElement partyRow(PartyEntry party, UIElement action) {
        UIElement row = row().layout(layout -> {
            layout.widthPercent(100);
            layout.height(44);
            layout.paddingAll(5);
            layout.gapAll(5);
            layout.alignItems(AlignItems.CENTER);
        }).addClass("preview_bg");
        UIElement info = column().layout(layout -> {
            layout.flex(1);
            layout.gapAll(2);
        });
        info.addChildren(
                label(Component.literal(party.name())),
                smallLabel(Component.translatable(KEY + "party.summary", party.leaderName(), party.memberCount()))
        );
        row.addChildren(info, action);
        return row;
    }

    private static UIElement playerActionRow(PlayerEntry player, UIElement action) {
        UIElement row = row().layout(layout -> {
            layout.widthPercent(100);
            layout.height(36);
            layout.paddingAll(5);
            layout.gapAll(5);
            layout.alignItems(AlignItems.CENTER);
        }).addClass("preview_bg");
        row.addChildren(
                label(Component.literal(player.name())).layout(layout -> layout.flex(1)),
                action
        );
        return row;
    }

    private static Dialog listDialog(String titleKey) {
        Dialog dialog = new Dialog();
        dialog.setTitle(titleKey);
        dialog.overlay.layout(layout -> layout.width(330));
        return dialog;
    }

    private static ScrollerView dialogList() {
        ScrollerView list = verticalScroller();
        list.layout(layout -> layout.widthPercent(100).height(190));
        return list;
    }

    private static Button closeButton(Dialog dialog) {
        Button button = button(KEY + "dialog.close", event -> dialog.close());
        button.addClass("__cancel-button__");
        return button;
    }

    private static Button actionButton(String key, UIEventListener listener) {
        Button button = button(key, listener);
        button.layout(layout -> {
            layout.widthPercent(48);
            layout.height(20);
        });
        return button;
    }

    private static Button button(String key, UIEventListener listener) {
        Button button = new Button();
        button.setText(key);
        button.setOnClick(listener);
        button.textStyle(style -> style
                .fontSize(8)
                .textAlignHorizontal(Horizontal.CENTER)
                .textAlignVertical(Vertical.CENTER)
                .textWrap(TextWrap.HIDE));
        return button;
    }

    private static Label titleLabel(Component text) {
        Label label = label(text);
        label.textStyle(style -> style.fontSize(11).textWrap(TextWrap.HIDE));
        label.layout(layout -> layout.widthPercent(100).height(16));
        return label;
    }

    private static Label sectionLabel(Component text) {
        Label label = label(text);
        label.layout(layout -> layout.widthPercent(100).height(14));
        return label;
    }

    private static Label label(Component text) {
        Label label = new Label();
        label.setText(text);
        label.textStyle(style -> style
                .fontSize(9)
                .textAlignVertical(Vertical.CENTER)
                .textWrap(TextWrap.HIDE));
        return label;
    }

    private static Label smallLabel(Component text) {
        Label label = label(text);
        label.textStyle(style -> style.fontSize(7).textWrap(TextWrap.HIDE));
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
        label.layout(layout -> layout.widthPercent(100).height(36).paddingAll(5));
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
            layout.gapAll(4);
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

    private static List<PlayerEntry> readPlayers(CompoundTag parent, String key) {
        List<PlayerEntry> entries = new ArrayList<>();
        ListTag list = parent.getList(key, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag tag = list.getCompound(i);
            entries.add(new PlayerEntry(
                    tag.hasUUID("id") ? tag.getUUID("id") : EMPTY_UUID,
                    tag.getString("name"),
                    tag.getBoolean("online"),
                    tag.getBoolean("leader")
            ));
        }
        return entries;
    }

    private static List<PartyEntry> readParties(CompoundTag parent, String key) {
        List<PartyEntry> entries = new ArrayList<>();
        ListTag list = parent.getList(key, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag tag = list.getCompound(i);
            entries.add(new PartyEntry(
                    tag.getString("id"),
                    tag.getString("name"),
                    tag.getString("leaderName"),
                    tag.getInt("memberCount"),
                    tag.getBoolean("applied"),
                    tag.getBoolean("invited")
            ));
        }
        return entries;
    }

    private static List<ChatEntry> readChat(CompoundTag party) {
        List<ChatEntry> entries = new ArrayList<>();
        ListTag list = party.getList("messages", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag tag = list.getCompound(i);
            entries.add(new ChatEntry(tag.getString("senderName"), tag.getString("content")));
        }
        return entries;
    }

    private record PlayerEntry(UUID id, String name, boolean online, boolean leader) {
    }

    private record PartyEntry(String id, String name, String leaderName, int memberCount, boolean applied,
                              boolean invited) {
    }

    private record ChatEntry(String senderName, String content) {
    }
}
