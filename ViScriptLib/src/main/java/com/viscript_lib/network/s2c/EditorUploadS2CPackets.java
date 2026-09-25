package com.viscript_lib.network.s2c;

import com.lowdragmc.lowdraglib2.editor.ui.Editor;
import com.lowdragmc.lowdraglib2.editor.ui.EditorWindow;
import com.lowdragmc.lowdraglib2.gui.holder.ModularUIContainerScreen;
import com.lowdragmc.lowdraglib2.gui.holder.ModularUIScreen;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Dialog;
import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacket;
import com.lowdragmc.lowdraglib2.syncdata.rpc.RPCSender;
import com.viscript_lib.ViScriptLib;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/**
 * 向客户端编辑器回传上传结果的 S2C RPC。
 */
public final class EditorUploadS2CPackets {
    public static final String EDITOR_UPLOAD_RESULT = ViScriptLib.MOD_ID + ":editor_upload_result";

    private EditorUploadS2CPackets() {
    }

    /**
     * 在客户端显示服务端上传结果。
     *
     * @param sender RPC 发送者
     * @param title 提示标题
     * @param content 提示内容
     */
    @RPCPacket(EDITOR_UPLOAD_RESULT)
    public static void receiveEditorUploadResult(RPCSender sender, Component title, Component content) {
        if (!sender.isServer()) return;

        var editor = getCurrentEditor();
        if (editor != null) {
            Dialog.showNotification(title.getString(), content.getString(), null).show(editor);
            return;
        }

        var player = Minecraft.getInstance().player;
        if (player != null) {
            player.displayClientMessage(Component.empty().append(title).append(": ").append(content), false);
        }
    }

    private static Editor getCurrentEditor() {
        if (Minecraft.getInstance().screen instanceof ModularUIContainerScreen screen
                && screen.getMenu().getModularUI().ui.rootElement instanceof EditorWindow editorWindow) {
            return editorWindow.getCurrentEditor();
        }
        if (Minecraft.getInstance().screen instanceof ModularUIScreen screen
                && screen.modularUI.ui.rootElement instanceof EditorWindow editorWindow) {
            return editorWindow.getCurrentEditor();
        }
        return null;
    }
}
