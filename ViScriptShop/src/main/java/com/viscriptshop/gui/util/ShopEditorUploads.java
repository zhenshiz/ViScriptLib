package com.viscriptshop.gui.util;

import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacketDistributor;
import com.lowdragmc.lowdraglib2.syncdata.rpc.RPCSender;
import com.viscript_lib.gui.editor.EditorFileNames;
import com.viscript_lib.gui.editor.EditorServerUploads;
import com.viscriptshop.ViscriptShop;
import com.viscriptshop.gui.data.Shop;
import com.viscriptshop.network.c2s.C2SPayload;
import com.viscriptshop.util.ViScriptShopServerUtil;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.io.IOException;

/**
 * 处理商店编辑器的服务端文件上传和可选热重载。
 */
public final class ShopEditorUploads {
    private static final String EDITOR_UPLOAD_RESULT = "viscript_lib:editor_upload_result";
    private static final String TAG_RELOAD_SHOP = "reloadShopAfterUpload";

    private ShopEditorUploads() {
    }

    public static void uploadShopToServer(String fileName, CompoundTag fileData, boolean reloadShop) {
        CompoundTag request = new CompoundTag();
        request.putString(EditorServerUploads.TAG_TARGET, EditorServerUploads.TARGET_RUNTIME);
        request.putString(EditorServerUploads.TAG_MOD_ID, Shop.FORMAT.modId());
        request.putString(EditorServerUploads.TAG_DOMAIN, Shop.FORMAT.domain());
        request.putString(EditorServerUploads.TAG_SUFFIX, Shop.FORMAT.runtimeSuffix());
        request.putString(EditorServerUploads.TAG_FILE_NAME, fileName);
        request.putBoolean(EditorServerUploads.TAG_COMPRESSED, Shop.FORMAT.compressed());
        request.put(EditorServerUploads.TAG_DATA, fileData.copy());
        request.putBoolean(TAG_RELOAD_SHOP, reloadShop);
        RPCPacketDistributor.rpcToServer(C2SPayload.UPLOAD_SHOP_FILE_C2S, request);
    }

    public static void receiveShopUpload(RPCSender sender, CompoundTag request) {
        if (sender.isServer()) {
            return;
        }

        ServerPlayer player = sender.asPlayer();
        if (player == null) {
            return;
        }

        if (!player.hasPermissions(Commands.LEVEL_OWNERS)) {
            sendUploadResult(player,
                    Component.translatable("viscript_lib.editor.server_upload_result.error.title"),
                    Component.translatable("viscript_shop.editor.server_upload_result.error.permission"));
            return;
        }

        try {
            validateShopUpload(request);
            EditorServerUploads.UploadResult result = EditorServerUploads.writeOnServer(request);
            boolean reloadShop = !request.contains(TAG_RELOAD_SHOP, Tag.TAG_BYTE)
                    || request.getBoolean(TAG_RELOAD_SHOP);
            if (reloadShop) {
                String shopId = EditorFileNames.normalizeBaseName(
                        result.file().getFileName().toString(), Shop.FORMAT.runtimeSuffix());
                ViScriptShopServerUtil.reloadOpenShop(shopId);
                sendUploadResult(player,
                        Component.translatable("viscript_lib.editor.server_upload_result.success.title"),
                        Component.translatable("viscript_shop.editor.server_upload_result.success.reloaded",
                                result.file().toString(), shopId));
                ViscriptShop.LOGGER.info("Received shop upload from {} and wrote {}; reloaded saved shop {}",
                        player.getGameProfile().getName(), result.file(), shopId);
            } else {
                String contentKey = result.overwritten()
                        ? "viscript_lib.editor.server_upload_result.success.overwritten"
                        : "viscript_lib.editor.server_upload_result.success.created";
                sendUploadResult(player,
                        Component.translatable("viscript_lib.editor.server_upload_result.success.title"),
                        Component.translatable(contentKey, result.file().toString()));
                ViscriptShop.LOGGER.info("Received shop upload from {} and wrote {} without reloading saved shop data",
                        player.getGameProfile().getName(), result.file());
            }
        } catch (Exception e) {
            ViscriptShop.LOGGER.error("Failed to save shop upload from {}", player.getGameProfile().getName(), e);
            String message = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
            sendUploadResult(player,
                    Component.translatable("viscript_lib.editor.server_upload_result.error.title"),
                    Component.translatable("viscript_lib.editor.server_upload_result.error.content", message));
        }
    }

    private static void validateShopUpload(CompoundTag request) throws IOException {
        if (!EditorServerUploads.TARGET_RUNTIME.equals(request.getString(EditorServerUploads.TAG_TARGET))
                || !Shop.FORMAT.modId().equals(request.getString(EditorServerUploads.TAG_MOD_ID))
                || !Shop.FORMAT.domain().equals(request.getString(EditorServerUploads.TAG_DOMAIN))
                || !Shop.FORMAT.runtimeSuffix().equals(request.getString(EditorServerUploads.TAG_SUFFIX))
                || request.getBoolean(EditorServerUploads.TAG_COMPRESSED) != Shop.FORMAT.compressed()) {
            throw new IOException("Invalid shop upload request");
        }
    }

    private static void sendUploadResult(ServerPlayer player, Component title, Component content) {
        RPCPacketDistributor.rpcToPlayer(player, EDITOR_UPLOAD_RESULT, title, content);
    }
}
