package com.viscript_lib.gui.editor;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.Icons;
import net.minecraft.network.chat.Component;

/**
 * 描述 ViScript 编辑器提供的一个服务端上传动作。
 *
 * <p>菜单会先询问文件名，再调用本接口把对应数据发送给服务端。
 */
public interface EditorUploadAction {
    /**
     * 返回上传菜单图标。
     *
     * @return 上传菜单图标
     */
    default IGuiTexture getIcon() {
        return Icons.EXPORT;
    }

    /**
     * 返回上传菜单显示名。
     *
     * @return 菜单显示名
     */
    default Component getDisplayName() {
        return Component.translatable("viscript_lib.editor.menu.upload_to_server");
    }

    /**
     * 返回文件名输入弹窗标题翻译键。
     *
     * @return 弹窗标题翻译键
     */
    default String getDialogTitleKey() {
        return "viscript_lib.editor.dialog.upload_to_server";
    }

    /**
     * 返回默认文件名。
     *
     * @return 默认文件名
     */
    default String getDefaultFileName() {
        return "test";
    }

    /**
     * 返回上传目标文件后缀。
     *
     * @return 文件后缀
     */
    default String getSuffix() {
        return ".nbt";
    }

    /**
     * 规范化输入的文件名。
     *
     * @param fileName 用户输入的文件名
     * @return 传递给上传动作的文件名
     */
    default String normalizeFileName(String fileName) {
        return EditorFileNames.normalizeFileName(fileName, EditorFileNames.normalizeSuffix(getSuffix()));
    }

    /**
     * 执行上传动作。
     *
     * @param fileName 已规范化的文件名
     * @throws Exception 上传准备失败时抛出
     */
    void uploadToServer(String fileName) throws Exception;
}
