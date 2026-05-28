package com.viscript_lib.gui.editor;

import com.lowdragmc.lowdraglib2.LDLib2;

import java.io.File;

/**
 * 解析编辑器文件在 LDLib2 资产目录下的约定路径。
 *
 * <p>工程文件统一放在 <code>assets/&lt;modid&gt;/project</code>，运行时文件
 * 放在 <code>assets/&lt;modid&gt;/&lt;domain&gt;</code>。调用方只需要传入
 * 模组 id 和编辑器域名，不需要关心 LDLib2 当前资产目录的位置。
 */
public final class EditorAssetPaths {

    /**
     * 返回运行时文件目录。
     *
     * @param modId 模组 id
     * @param domain 运行时文件域名
     * @return 运行时文件目录
     */
    public static File functionDirectory(String modId, String domain) {
        return new File(new File(LDLib2.getAssetsDir(), modId), domain);
    }

    /**
     * 返回工程文件目录。
     *
     * @param modId 模组 id
     * @return 工程文件目录
     */
    public static File projectDirectory(String modId) {
        return new File(new File(LDLib2.getAssetsDir(), modId), "project");
    }
}
