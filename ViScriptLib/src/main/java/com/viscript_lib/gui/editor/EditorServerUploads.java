package com.viscript_lib.gui.editor;

import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacketDistributor;
import com.viscript_lib.network.c2s.EditorUploadC2SPackets;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 附属模组复用的编辑器文件上传入口。
 *
 * <p>客户端通过本类发送 LDLib2 RPC 到服务端，服务端会根据目标类型把文件写入
 * <code>assets/&lt;modid&gt;/project</code> 或
 * <code>assets/&lt;modid&gt;/&lt;domain&gt;</code>。
 */
public final class EditorServerUploads {
    public static final String TARGET_RUNTIME = "runtime";
    public static final String TARGET_PROJECT = "project";

    public static final String TAG_TARGET = "target";
    public static final String TAG_MOD_ID = "modId";
    public static final String TAG_DOMAIN = "domain";
    public static final String TAG_SUFFIX = "suffix";
    public static final String TAG_FILE_NAME = "fileName";
    public static final String TAG_COMPRESSED = "compressed";
    public static final String TAG_DATA = "data";

    /**
     * 上传运行时文件到服务端。
     *
     * @param format 文件格式定义
     * @param fileName 用户输入或已规范化的文件名
     * @param fileData 运行时文件 NBT
     */
    public static void uploadToServer(EditorFileFormat format, String fileName, CompoundTag fileData) {
        uploadToServer(format, TARGET_RUNTIME, fileName, format.runtimeSuffix(), format.compressed(), fileData);
    }

    /**
     * 上传工程文件到服务端。
     *
     * @param format 文件格式定义
     * @param fileName 用户输入或已规范化的文件名
     * @param fileData 工程文件 NBT
     */
    public static void uploadProjectToServer(EditorFileFormat format, String fileName, CompoundTag fileData) {
        uploadToServer(format, TARGET_PROJECT, fileName, format.projectSuffix(), false, fileData);
    }

    private static void uploadToServer(EditorFileFormat format, String target, String fileName, String suffix,
                                       boolean compressed, CompoundTag fileData) {
        var request = new CompoundTag();
        request.putString(TAG_TARGET, target);
        request.putString(TAG_MOD_ID, format.modId());
        request.putString(TAG_DOMAIN, format.domain());
        request.putString(TAG_SUFFIX, suffix);
        request.putString(TAG_FILE_NAME, fileName);
        request.putBoolean(TAG_COMPRESSED, compressed);
        request.put(TAG_DATA, fileData.copy());
        RPCPacketDistributor.rpcToServer(EditorUploadC2SPackets.UPLOAD_EDITOR_FILE, request);
    }

    /**
     * 在服务端写入上传请求携带的 NBT 文件。
     *
     * @param request 上传请求 NBT
     * @return 写入结果
     * @throws IOException 创建目录或写文件失败时抛出
     */
    public static UploadResult writeOnServer(CompoundTag request) throws IOException {
        var modId = EditorFileNames.normalizePathSegment(request.getString(TAG_MOD_ID), "");
        var domain = EditorFileNames.normalizePathSegment(request.getString(TAG_DOMAIN), "");
        if (modId.isBlank() || domain.isBlank()) {
            throw new IOException("Missing editor upload path");
        }
        if (!request.contains(TAG_DATA)) {
            throw new IOException("Missing editor upload data");
        }

        var suffix = EditorFileNames.normalizeSuffix(request.getString(TAG_SUFFIX));
        var fileName = EditorFileNames.normalizeFileName(request.getString(TAG_FILE_NAME), suffix);
        var outputDir = TARGET_PROJECT.equals(request.getString(TAG_TARGET))
                ? EditorAssetPaths.projectDirectory(modId).toPath()
                : EditorAssetPaths.functionDirectory(modId, domain).toPath();
        Files.createDirectories(outputDir);

        var file = outputDir.resolve(fileName);
        var overwritten = Files.exists(file);
        var data = request.getCompound(TAG_DATA).copy();
        if (request.getBoolean(TAG_COMPRESSED)) {
            NbtIo.writeCompressed(data, file);
        } else {
            NbtIo.write(data, file);
        }
        return new UploadResult(file, overwritten);
    }

    /**
     * 描述服务端上传写入结果。
     *
     * @param file 写入的服务端文件
     * @param overwritten 是否覆盖了已有文件
     */
    public record UploadResult(Path file, boolean overwritten) {
    }
}
