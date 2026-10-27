package com.viscript_lib.gui.ai;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.texture.Icons;
import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.lowdragmc.lowdraglib2.gui.ui.event.CommandEvents;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvent;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.gui.ui.data.ScrollerMode;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;
import net.minecraft.client.Minecraft;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.TextWrap;
import com.lowdragmc.lowdraglib2.gui.ui.data.Vertical;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextArea;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextElement;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexDirection;
import dev.vfyjxf.taffy.style.FlexWrap;
import dev.vfyjxf.taffy.style.TaffyPosition;
import net.minecraft.network.chat.Component;

import java.util.HashSet;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Set;

/** 使用编辑器主题区分玩家消息、AI 回复、工具记录和可回退的修改。 */
public final class AiChatPanel extends UIElement {
    public final UIElement titleBar = new UIElement();
    private final AiChatSession session;
    private final ScrollerView messages = new ScrollerView();
    private final TextElement emptyHint = text("");
    private final ScrollerView navigation = new ScrollerView();
    private final TextArea input = new TextArea() {
        @Override
        protected void onKeyDown(UIEvent event) {
            int mask = Minecraft.ON_OSX ? GLFW.GLFW_MOD_SUPER : GLFW.GLFW_MOD_CONTROL;
            if (isEditable() && event.keyCode == GLFW.GLFW_KEY_V && (event.modifiers & mask) != 0
                    && (event.modifiers & GLFW.GLFW_MOD_ALT) == 0) {
                var paste = UIEvent.create(UIEvents.EXECUTE_COMMAND);
                paste.command = CommandEvents.PASTE;
                onExecuteCommand(paste);
                event.stopPropagation();
                return;
            }
            super.onKeyDown(event);
        }

        @Override
        protected void onExecuteCommand(UIEvent event) {
            if (isEditable() && CommandEvents.PASTE.equals(event.command) && pasteImage()) {
                event.stopPropagation();
                return;
            }
            super.onExecuteCommand(event);
        }
    };
    private final List<AiChatImage> pendingImages = new ArrayList<>();
    private final ScrollerView attachments = new ScrollerView();
    private final Map<AiChatImage, ResourceLocation> imageTextures = new HashMap<>();
    private final TextElement status = text("");
    private final Button send = new Button();
    private final Button stop = new Button();
    private final Set<Integer> expandedTools = new HashSet<>();
    private int lastTurnNumber;
    private Float pendingScroll;
    private final java.util.Map<AiChatSession.Turn, TextElement> progressLabels = new java.util.LinkedHashMap<>();
    private long lastProgressUpdate;

    /**
     * 创建与一个编辑器会话绑定的聊天面板。
     *
     * @param session 编辑器会话
     * @param close 关闭所属窗口的回调
     */
    public AiChatPanel(AiChatSession session, Runnable close) {
        this.session = session;
        setId("vsl_ai_chat");
        addClass("panel_bg");
        layout(l -> l.widthPercent(100).heightPercent(100).paddingAll(8).gapAll(6));
        titleBar.layout(l -> l.widthPercent(100).height(20).flexDirection(FlexDirection.ROW).gapAll(4));
        titleBar.addChildren(text(Component.translatable("viscript_lib.ai.title")).layout(l -> l.flex(1)),
                new Button().setText("×").setOnClick(e -> close.run()).setId("vsl_ai_close")
                        .layout(l -> l.width(22).heightPercent(100)));
        messages.setId("vsl_ai_messages");
        messages.layout(l -> l.flex(1).minWidth(0).heightPercent(100));
        messages.viewContainer.layout(l -> l.gapAll(10).paddingAll(4));
        emptyHint.setId("vsl_ai_empty_hint");
        emptyHint.textStyle(s -> s.adaptiveHeight(false)
                .textAlignHorizontal(Horizontal.CENTER).textAlignVertical(Vertical.CENTER));
        emptyHint.layout(l -> l.positionType(TaffyPosition.ABSOLUTE).left(0).top(0)
                .widthPercent(100).heightPercent(100).paddingAll(12));
        messages.viewPort.addChild(emptyHint);
        navigation.setId("vsl_ai_navigation");
        navigation.layout(l -> l.width(30).heightPercent(100).flexShrink(0));
        navigation.viewPort.layout(l -> l.paddingAll(0));
        navigation.viewContainer.layout(l -> l.gapAll(4));
        var conversation = new UIElement().layout(l -> l.widthPercent(100).flex(1).minHeight(0)
                .flexDirection(FlexDirection.ROW).gapAll(3));
        conversation.addChildren(messages, navigation);
        input.setId("vsl_ai_input");
        input.layout(l -> l.widthPercent(100).height(55).flexShrink(0));
        input.textAreaStyle(s -> s.placeholder(Component.translatable("viscript_lib.ai.input")));
        input.style(s -> s.tooltips(Component.translatable("viscript_lib.ai.image.paste_tip")));
        attachments.setId("vsl_ai_attachments");
        attachments.layout(l -> l.widthPercent(100).height(110).flexShrink(0));
        attachments.getScrollerViewStyle().mode(ScrollerMode.HORIZONTAL);
        attachments.viewContainer.layout(l -> l.flexDirection(FlexDirection.ROW).gapAll(6));
        attachments.setDisplay(false);
        status.setId("vsl_ai_status");
        var buttons = new UIElement().layout(l -> l.widthPercent(100).height(20)
                .flexDirection(FlexDirection.ROW).gapAll(4));
        send.setText("viscript_lib.ai.send").setId("vsl_ai_send");
        send.setOnClick(e -> attempt(() -> {
            session.send(String.join("\n", input.getLines()), pendingImages);
            input.setLines(List.of(""));
            pendingImages.clear();
            refreshAttachments();
            status.setText("");
        }));
        stop.setText("viscript_lib.ai.stop").setId("vsl_ai_stop");
        stop.setOnClick(e -> session.stop());
        buttons.addChildren(send, stop, new Button().setText("viscript_lib.ai.new_chat")
                        .setOnClick(e -> { session.clear(); expandedTools.clear(); pendingImages.clear(); refreshAttachments(); status.setText(""); })
                        .setId("vsl_ai_new_chat"),
                new Button().setText("viscript_lib.ai.image.paste").setOnClick(e -> {
                            if (!pasteImage()) status.setText(Component.translatable("viscript_lib.ai.image.empty"));
                            input.focus();
                        }).setId("vsl_ai_paste_image"),
                new UIElement().layout(l -> l.flex(1)),
                new Button().setText("viscript_lib.ai.latest").setOnClick(e -> pendingScroll = 1f)
                        .setId("vsl_ai_latest"));
        addChildren(titleBar, conversation, status, attachments, input, buttons);
        session.setListener(this::refresh);
        refresh();
    }

    private void refresh() {
        send.setActive(!session.isBusy());
        stop.setActive(session.isBusy());
        float oldScroll = messages.verticalScroller.getNormalizedValue();
        boolean follow = oldScroll >= .98f || messages.getContainerHeight() <= messages.viewPort.getContentHeight();
        int newest = session.getTurns().isEmpty() ? 0 : session.getTurns().getLast().getNumber();
        follow |= newest != lastTurnNumber;
        lastTurnNumber = newest;
        messages.clearAllScrollViewChildren();
        navigation.clearAllScrollViewChildren();
        progressLabels.clear();
        emptyHint.setDisplay(session.getTurns().isEmpty());
        emptyHint.setText(Component.translatable(session.supportsEditing()
                ? "viscript_lib.ai.welcome" : "viscript_lib.ai.welcome_readonly"));
        for (var turn : session.getTurns()) {
            var user = new UIElement().addClass("preview_bg").addClass("vsl_ai_user_message")
                    .setId("vsl_ai_turn_" + turn.getNumber())
                    .layout(l -> l.widthPercent(85).alignSelf(AlignItems.FLEX_END).paddingAll(7).gapAll(4).flexShrink(0));
            user.addChild(text(Component.translatable("viscript_lib.ai.role.you")));
            if (!turn.getPrompt().isEmpty()) user.addChild(new AiMessageText(Component.literal(turn.getPrompt())));
            if (!turn.getImages().isEmpty()) {
                var pictures = new UIElement().layout(l -> l.widthPercent(100).flexDirection(FlexDirection.ROW)
                        .flexWrap(FlexWrap.WRAP).gapAll(6));
                turn.getImages().forEach(image -> pictures.addChild(imageCard(image, null).addClass("vsl_ai_sent_image")));
                user.addChild(pictures);
            }
            if (!turn.getPrompt().isEmpty()) user.addChild(copyButton(turn.getPrompt(), "vsl_ai_copy_user_" + turn.getNumber())
                    .layout(l -> l.alignSelf(AlignItems.FLEX_END)));
            messages.addScrollViewChild(user);
            var answer = new UIElement().addClass("vsl_ai_assistant_message")
                    .setId("vsl_ai_answer_" + turn.getNumber())
                    .layout(l -> l.widthPercent(100).paddingAll(4).gapAll(6).flexShrink(0));
            answer.addChild(text(Component.translatable("viscript_lib.ai.role.assistant")));
            if (!turn.getAnswer().isBlank()) {
                answer.addChild(new AiMarkdownView(turn.getAnswer()).setId("vsl_ai_text_" + turn.getNumber()));
            }
            var progress = text(turn.progressText());
            progress.setId("vsl_ai_progress_" + turn.getNumber());
            answer.addChild(progress);
            progressLabels.put(turn, progress);
            if (!turn.getActivity().isEmpty()) {
                boolean expanded = expandedTools.contains(turn.getNumber());
                var tools = new UIElement().addClass("preview_bg")
                        .layout(l -> l.widthPercent(100).paddingAll(4).gapAll(3));
                var toggle = new Button().setText(Component.translatable("viscript_lib.ai.tools",
                        expanded ? "−" : "+", turn.getActivity().size()))
                        .setOnClick(e -> {
                            if (!expandedTools.add(turn.getNumber())) expandedTools.remove(turn.getNumber());
                            refresh();
                        }).setId("vsl_ai_tools_" + turn.getNumber()).layout(l -> l.height(18).widthPercent(100));
                tools.addChild(toggle);
                if (expanded) {
                    var details = new UIElement().setId("vsl_ai_tool_details_" + turn.getNumber())
                            .layout(l -> l.widthPercent(100).gapAll(3));
                    for (String activity : turn.getActivity()) details.addChild(text("• " + activity));
                    tools.addChild(details);
                }
                answer.addChild(tools);
            }
            if (!turn.getChanges().isEmpty()) {
                var changes = new UIElement().addClass("preview_bg")
                        .layout(l -> l.widthPercent(100).paddingAll(6).gapAll(4));
                changes.addChild(text(Component.translatable("viscript_lib.ai.changes")));
                for (String change : turn.getChanges()) changes.addChild(text("• " + change));
                answer.addChild(changes);
            }
            if (!turn.getError().isEmpty()) answer.addChild(text(Component.translatable(turn.getError())));
            var actions = new UIElement().layout(l -> l.widthPercent(100).height(20)
                    .flexDirection(FlexDirection.ROW).gapAll(4));
            if (turn.getState() == AiChatSession.State.PENDING) {
                actions.addChildren(new Button().setText("viscript_lib.ai.apply")
                                .setOnClick(e -> attempt(() -> session.apply(turn))).setId("vsl_ai_apply_" + turn.getNumber()),
                        new Button().setText("viscript_lib.ai.discard")
                                .setOnClick(e -> attempt(() -> session.discard(turn))).setId("vsl_ai_discard_" + turn.getNumber()));
                answer.addChild(actions);
            } else if (turn.getState() == AiChatSession.State.APPLIED) {
                actions.addChild(new Button().setText("viscript_lib.ai.revert")
                        .setOnClick(e -> attempt(() -> session.revert(turn))).setId("vsl_ai_revert_" + turn.getNumber()));
                answer.addChild(actions);
            }
            messages.addScrollViewChild(answer);
            navigation.addScrollViewChild(new Button().setText(Integer.toString(turn.getNumber()))
                    .setOnClick(e -> {
                        pendingScroll = null;
                        messages.scrollToChild(user);
                    }).setId("vsl_ai_jump_" + turn.getNumber()).layout(l -> l.widthPercent(100).height(19))
                    .style(s -> s.tooltips(Component.translatable("viscript_lib.ai.jump", turn.getNumber()),
                            Component.literal(excerpt(turn.getPrompt(), 100)), Component.literal(excerpt(turn.getAnswer(), 160)))));
        }
        pendingScroll = follow ? 1f : oldScroll;
        releaseUnusedImages();
    }

    private boolean pasteImage() {
        try {
            var image = AiChatImage.readClipboard();
            if (image == null) return false;
            pendingImages.add(image);
            refreshAttachments();
            status.setText(Component.translatable("viscript_lib.ai.image.ready", pendingImages.size()));
        } catch (java.io.IOException | RuntimeException exception) {
            status.setText(Component.translatable("viscript_lib.ai.image.failed"));
        }
        return true;
    }

    private void refreshAttachments() {
        attachments.clearAllScrollViewChildren();
        attachments.setDisplay(!pendingImages.isEmpty());
        for (int i = 0; i < pendingImages.size(); i++) {
            int index = i;
            attachments.addScrollViewChild(imageCard(pendingImages.get(i), () -> {
                pendingImages.remove(index);
                refreshAttachments();
                status.setText(pendingImages.isEmpty() ? Component.empty()
                        : Component.translatable("viscript_lib.ai.image.ready", pendingImages.size()));
            }).addClass("vsl_ai_pending_image"));
        }
        releaseUnusedImages();
    }

    private UIElement imageCard(AiChatImage image, Runnable remove) {
        var card = new UIElement().addClass("preview_bg").layout(l -> l.width(104).paddingAll(3).gapAll(2).flexShrink(0));
        try {
            var texture = imageTextures.computeIfAbsent(image, key -> {
                try (var full = NativeImage.read(new java.io.ByteArrayInputStream(Base64.getDecoder().decode(key.base64())))) {
                    float scale = Math.min(1f, 256f / Math.max(full.getWidth(), full.getHeight()));
                    var thumbnail = new NativeImage(Math.max(1, (int) (full.getWidth() * scale)),
                            Math.max(1, (int) (full.getHeight() * scale)), true);
                    full.resizeSubRectTo(0, 0, full.getWidth(), full.getHeight(), thumbnail);
                    return Minecraft.getInstance().getTextureManager().register("vsl_ai_image", new DynamicTexture(thumbnail));
                } catch (java.io.IOException exception) { throw new IllegalStateException(exception); }
            });
            var preview = new UIElement().layout(l -> l.width(96).height(64));
            float width = Math.min(96f, 64f * image.width() / image.height());
            float height = Math.min(64f, 96f * image.height() / image.width());
            preview.addChild(new UIElement().layout(l -> l.width(width).height(height).alignSelf(AlignItems.CENTER))
                    .style(s -> s.backgroundTexture(SpriteTexture.of(texture))));
            card.addChild(preview);
        } catch (RuntimeException exception) {
            card.addChild(text(Component.translatable("viscript_lib.ai.image.preview_failed")));
        }
        var label = Component.literal(image.width() + " × " + image.height());
        card.addChild(text(label).textStyle(s -> s.fontSize(8)));
        if (remove != null) card.addChild(new Button().setText("×").setOnClick(e -> remove.run())
                .addClass("vsl_ai_remove_image").layout(l -> l.width(18).height(16).alignSelf(AlignItems.FLEX_END))
                .style(s -> s.tooltips(Component.translatable("viscript_lib.ai.image.remove"))));
        return card;
    }

    private void releaseUnusedImages() {
        var used = new HashSet<>(pendingImages);
        session.getTurns().forEach(turn -> used.addAll(turn.getImages()));
        imageTextures.entrySet().removeIf(entry -> {
            if (used.contains(entry.getKey())) return false;
            Minecraft.getInstance().getTextureManager().release(entry.getValue());
            return true;
        });
    }

    @Override
    protected void onRemoved() {
        imageTextures.values().forEach(Minecraft.getInstance().getTextureManager()::release);
        imageTextures.clear();
        super.onRemoved();
    }

    @Override
    public void screenTick() {
        super.screenTick();
        if (System.nanoTime() - lastProgressUpdate >= 250_000_000L) {
            progressLabels.forEach((turn, label) -> label.setText(turn.progressText()));
            lastProgressUpdate = System.nanoTime();
        }
        // 重建消息后等布局完成再恢复阅读位置，避免工具返回时把玩家拉回底部。
        if (pendingScroll != null && messages.getContainerHeight() > 0) {
            messages.verticalScroller.setNormalizedValue(pendingScroll);
            pendingScroll = null;
        }
    }

    private static Button copyButton(String message, String id) {
        var button = new Button().noText().addPreIcon(Icons.COPY);
        button.setId(id).layout(l -> l.width(16).height(16).flexShrink(0));
        button.style(s -> s.tooltips(Component.translatable("viscript_lib.ai.copy")));
        button.setOnClick(e -> {
            Minecraft.getInstance().keyboardHandler.setClipboard(message);
            button.getChildren().getFirst().style(s -> s.backgroundTexture(Icons.CHECK));
            button.style(s -> s.tooltips(Component.translatable("viscript_lib.ai.copied")));
        });
        return button;
    }

    private static String excerpt(String value, int maximum) {
        String flat = value.replace('\n', ' ');
        return flat.length() > maximum ? flat.substring(0, maximum) + "…" : flat;
    }

    private void attempt(Runnable action) {
        try {
            action.run();
        } catch (RuntimeException exception) {
            status.setText(Component.translatable(exception.getMessage() == null
                    ? "viscript_lib.ai.error.failed" : exception.getMessage()));
        }
    }

    private static TextElement text(String value) {
        return text(Component.literal(value));
    }

    private static TextElement text(Component value) {
        var text = new TextElement().setText(value).textStyle(s -> s.textWrap(TextWrap.WRAP).adaptiveHeight(true));
        text.layout(l -> l.widthPercent(100).flexShrink(0));
        return text;
    }
}
