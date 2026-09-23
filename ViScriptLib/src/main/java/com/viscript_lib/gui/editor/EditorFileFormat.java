package com.viscript_lib.gui.editor;

import com.viscript_lib.ViScriptLib;

import java.io.File;

/**
 * 描述一个编辑器域的本地路径和文件后缀规则。
 *
 * <p><code>suffix</code> 表示运行时文件后缀，工程文件后缀会自动派生为
 * <code>.&lt;suffix&gt;proj</code>。这让不同编辑器可以共用
 * <code>assets/&lt;modid&gt;/project</code> 目录，并通过后缀过滤各自的工程文件。
 *
 * @param modId      模组 id
 * @param domain     运行时文件域名
 * @param suffix     运行时文件后缀
 * @param compressed 运行时文件是否使用压缩 NBT
 */
public record EditorFileFormat(String modId, String domain, String suffix, boolean compressed) {
    public EditorFileFormat {
        modId = EditorFileNames.normalizePathSegment(modId, ViScriptLib.MOD_ID);
        domain = EditorFileNames.normalizePathSegment(domain, "data");
        suffix = EditorFileNames.normalizeSuffix(suffix);
    }

    /**
     * 创建运行时文件使用普通 NBT 的格式定义。
     *
     * @param modId  模组 id
     * @param domain 运行时文件域名
     * @param suffix 运行时文件后缀
     * @return 普通运行时文件格式
     */
    public static EditorFileFormat of(String modId, String domain, String suffix) {
        return new EditorFileFormat(modId, domain, suffix, false);
    }

    /**
     * 创建运行时文件使用压缩 NBT 的格式定义。
     *
     * @param modId  模组 id
     * @param domain 运行时文件域名
     * @param suffix 运行时文件后缀
     * @return 压缩运行时文件格式
     */
    public static EditorFileFormat compressed(String modId, String domain, String suffix) {
        return new EditorFileFormat(modId, domain, suffix, true);
    }

    /**
     * 返回运行时文件目录。
     *
     * @return 运行时文件目录
     */
    public File functionDirectory() {
        return EditorAssetPaths.functionDirectory(modId, domain);
    }

    /**
     * 返回工程文件目录。
     *
     * @return 工程文件目录
     */
    public File projectDirectory() {
        return EditorAssetPaths.projectDirectory(modId);
    }

    /**
     * 返回规范化后的运行时文件后缀。
     *
     * @return 运行时文件后缀
     */
    public String runtimeSuffix() {
        return suffix;
    }

    /**
     * 返回由运行时后缀派生出的工程文件后缀。
     *
     * @return 工程文件后缀
     */
    public String projectSuffix() {
        return EditorFileNames.normalizeSuffix(EditorFileNames.stripLeadingDot(suffix) + "proj");
    }
}
