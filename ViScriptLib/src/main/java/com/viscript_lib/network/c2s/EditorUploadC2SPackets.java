package com.viscript_lib.network.c2s;

import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacket;
import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacketDistributor;
import com.lowdragmc.lowdraglib2.syncdata.rpc.RPCSender;
import com.viscript_lib.ViScriptLib;
import com.viscript_lib.gui.editor.EditorServerUploads;
import com.viscript_lib.network.s2c.EditorUploadS2CPackets;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

/**
 * 接收客户端编辑器上传文件的 C2S RPC。
 *
 * <p>请求使用 <code>CompoundTag</code> 携带目标类型、路径信息、文件名、
 * 压缩标记和实际文件内容。服务端写入完成后会发送 S2C 结果通知。
 */
public final class EditorUploadC2SPackets {
    public static final String UPLOAD_EDITOR_FILE = ViScriptLib.MOD_ID + ":upload_editor_file";

    private EditorUploadC2SPackets() {
    }

    /**
     * 在服务端处理编辑器文件上传请求。
     *
     * @param sender RPC 发送者
     * @param request 上传请求 NBT
     */
    @RPCPacket(UPLOAD_EDITOR_FILE)
    public static void receiveEditorUpload(RPCSender sender, CompoundTag request) {
        if (sender.isServer()) return;

        var player = sender.asPlayer();
        if (player == null) return;

        try {
            var result = EditorServerUploads.writeOnServer(request);
            var contentKey = result.overwritten()
                    ? "viscript_lib.editor.server_upload_result.success.overwritten"
                    : "viscript_lib.editor.server_upload_result.success.created";
            RPCPacketDistributor.rpcToPlayer(player,
                    EditorUploadS2CPackets.EDITOR_UPLOAD_RESULT,
                    Component.translatable("viscript_lib.editor.server_upload_result.success.title"),
                    Component.translatable(contentKey, result.file().toString()));
            ViScriptLib.LOGGER.info("Received editor upload from {} and wrote {}",
                    player.getGameProfile().getName(), result.file());
        } catch (Exception e) {
            ViScriptLib.LOGGER.error("Failed to save editor upload from {}", player.getGameProfile().getName(), e);
            var message = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
            RPCPacketDistributor.rpcToPlayer(player,
                    EditorUploadS2CPackets.EDITOR_UPLOAD_RESULT,
                    Component.translatable("viscript_lib.editor.server_upload_result.error.title"),
                    Component.translatable("viscript_lib.editor.server_upload_result.error.content", message));
        }
    }
}
