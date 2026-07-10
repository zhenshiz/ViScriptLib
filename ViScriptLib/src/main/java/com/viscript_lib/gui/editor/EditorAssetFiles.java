package com.viscript_lib.gui.editor;

import com.lowdragmc.lowdraglib2.LDLib2;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

/**
 * 编辑器资产文件工具。
 *
 * <p>这个类负责扫描和解析 LDLib2 assets 目录下的运行时/工程文件。返回值保持为
 * 普通 <code>List&lt;String&gt;</code> 和 <code>Path</code>，命令补全、反序列化等业务逻辑
 * 由附属模组自己处理。
 */
public final class EditorAssetFiles {

    /**
     * 扫描指定模组 assets 子目录下的文件。
     *
     * <p>例如 <code>listAssetFiles("viscript_recipe", "recipe", "recipe", true)</code>
     * 会递归扫描 <code>assets/viscript_recipe/recipe</code> 下所有 <code>.recipe</code>
     * 文件，并返回不带后缀的相对路径。
     *
     * @param modId 模组 id
     * @param directory assets/&lt;modid&gt; 下的相对目录
     * @param suffix 文件后缀
     * @param stripSuffix 是否移除返回路径末尾的后缀
     * @return 相对指定目录的文件路径列表，路径分隔符固定为 <code>/</code>
     */
    public static List<String> listAssetFiles(String modId, String directory, String suffix, boolean stripSuffix) {
        return listFiles(resolveAssetDirectory(modId, directory), suffix, stripSuffix);
    }

    /**
     * 扫描运行时文件目录。
     *
     * @param format 文件格式定义
     * @param stripSuffix 是否移除返回路径末尾的运行时文件后缀
     * @return 相对运行时文件目录的文件路径列表
     */
    public static List<String> listRuntimeFiles(EditorFileFormat format, boolean stripSuffix) {
        return listFiles(format.functionDirectory(), format.runtimeSuffix(), stripSuffix);
    }

    /**
     * 扫描工程文件目录。
     *
     * @param format 文件格式定义
     * @param stripSuffix 是否移除返回路径末尾的工程文件后缀
     * @return 相对工程文件目录的文件路径列表
     */
    public static List<String> listProjectFiles(EditorFileFormat format, boolean stripSuffix) {
        return listFiles(format.projectDirectory(), format.projectSuffix(), stripSuffix);
    }

    /**
     * 扫描指定目录下的文件。
     *
     * @param rootDirectory 扫描根目录
     * @param suffix 文件后缀
     * @param stripSuffix 是否移除返回路径末尾的后缀
     * @return 相对扫描根目录的文件路径列表
     */
    public static List<String> listFiles(File rootDirectory, String suffix, boolean stripSuffix) {
        return listFiles(rootDirectory.toPath(), suffix, stripSuffix);
    }

    /**
     * 扫描指定目录下的文件。
     *
     * @param rootDirectory 扫描根目录
     * @param suffix 文件后缀
     * @param stripSuffix 是否移除返回路径末尾的后缀
     * @return 相对扫描根目录的文件路径列表
     */
    public static List<String> listFiles(Path rootDirectory, String suffix, boolean stripSuffix) {
        var normalizedSuffix = EditorFileNames.normalizeSuffix(suffix);
        var root = rootDirectory.toAbsolutePath().normalize();
        if (!Files.isDirectory(root)) {
            return List.of();
        }

        try (var stream = Files.walk(root)) {
            return stream
                    .filter(Files::isRegularFile)
                    .map(root::relativize)
                    .map(EditorAssetFiles::toAssetPath)
                    .filter(path -> hasSuffix(path, normalizedSuffix))
                    .map(path -> stripSuffix ? stripSuffix(path, normalizedSuffix) : path)
                    .sorted()
                    .toList();
        } catch (IOException ignored) {
            return List.of();
        }
    }

    /**
     * 解析模组 assets 子目录下的文件路径。
     *
     * @param modId 模组 id
     * @param directory assets/&lt;modid&gt; 下的相对目录
     * @param relativePath 相对目录内的文件路径
     * @param suffix 文件后缀
     * @param appendSuffix 路径没有后缀时是否自动补齐后缀
     * @return 安全规范化后的服务端文件路径
     */
    public static Path resolveAssetFile(String modId, String directory, String relativePath,
                                        String suffix, boolean appendSuffix) {
        return resolveFile(resolveAssetDirectory(modId, directory), relativePath, suffix, appendSuffix);
    }

    /**
     * 解析运行时文件路径。
     *
     * @param format 文件格式定义
     * @param relativePath 相对运行时文件目录的路径
     * @param appendSuffix 路径没有后缀时是否自动补齐运行时文件后缀
     * @return 安全规范化后的运行时文件路径
     */
    public static Path resolveRuntimeFile(EditorFileFormat format, String relativePath, boolean appendSuffix) {
        return resolveFile(format.functionDirectory(), relativePath, format.runtimeSuffix(), appendSuffix);
    }

    /**
     * 解析工程文件路径。
     *
     * @param format 文件格式定义
     * @param relativePath 相对工程文件目录的路径
     * @param appendSuffix 路径没有后缀时是否自动补齐工程文件后缀
     * @return 安全规范化后的工程文件路径
     */
    public static Path resolveProjectFile(EditorFileFormat format, String relativePath, boolean appendSuffix) {
        return resolveFile(format.projectDirectory(), relativePath, format.projectSuffix(), appendSuffix);
    }

    /**
     * 解析指定目录下的文件路径。
     *
     * @param rootDirectory 根目录
     * @param relativePath 相对根目录的路径
     * @param suffix 文件后缀
     * @param appendSuffix 路径没有后缀时是否自动补齐后缀
     * @return 安全规范化后的文件路径
     */
    public static Path resolveFile(File rootDirectory, String relativePath, String suffix, boolean appendSuffix) {
        return resolveFile(rootDirectory.toPath(), relativePath, suffix, appendSuffix);
    }

    /**
     * 解析指定目录下的文件路径。
     *
     * @param rootDirectory 根目录
     * @param relativePath 相对根目录的路径
     * @param suffix 文件后缀
     * @param appendSuffix 路径没有后缀时是否自动补齐后缀
     * @return 安全规范化后的文件路径
     */
    public static Path resolveFile(Path rootDirectory, String relativePath, String suffix, boolean appendSuffix) {
        var safePath = normalizeRelativePath(relativePath);
        if (safePath.isBlank()) {
            throw new IllegalArgumentException("Editor asset file path is empty");
        }

        var normalizedSuffix = EditorFileNames.normalizeSuffix(suffix);
        if (appendSuffix && !hasSuffix(safePath, normalizedSuffix)) {
            safePath += normalizedSuffix;
        }

        var root = rootDirectory.toAbsolutePath().normalize();
        var file = root.resolve(safePath).normalize();
        if (!file.startsWith(root)) {
            throw new IllegalArgumentException("Editor asset file path escapes root directory");
        }
        return file;
    }

    /**
     * 返回模组 assets 子目录路径。
     *
     * @param modId 模组 id
     * @param directory assets/&lt;modid&gt; 下的相对目录
     * @return 安全规范化后的目录路径
     */
    public static Path resolveAssetDirectory(String modId, String directory) {
        var safeModId = EditorFileNames.normalizePathSegment(modId, "");
        if (safeModId.isBlank()) {
            throw new IllegalArgumentException("Editor asset mod id is empty");
        }

        var root = new File(LDLib2.getAssetsDir(), safeModId).toPath().toAbsolutePath().normalize();
        var safeDirectory = normalizeRelativePath(directory);
        if (safeDirectory.isBlank()) {
            return root;
        }
        return root.resolve(safeDirectory).normalize();
    }

    /**
     * 规范化相对资产路径。
     *
     * <p>该方法会保留目录层级，但会清理非法文件名字符，并拒绝 <code>..</code>。
     *
     * @param path 原始相对路径
     * @return 规范化后的相对路径
     */
    public static String normalizeRelativePath(String path) {
        var normalized = path == null ? "" : path.trim().replace('\\', '/');
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }

        var result = new StringBuilder();
        for (var segment : normalized.split("/")) {
            if (segment.isBlank() || ".".equals(segment)) {
                continue;
            }
            if ("..".equals(segment)) {
                throw new IllegalArgumentException("Editor asset path cannot contain '..'");
            }
            if (!result.isEmpty()) {
                result.append('/');
            }
            result.append(segment.replaceAll("[^A-Za-z0-9_.-]", "_"));
        }
        return result.toString();
    }

    /**
     * 移除路径末尾的指定后缀。
     *
     * @param path 文件路径
     * @param suffix 文件后缀
     * @return 不带后缀的路径
     */
    public static String stripSuffix(String path, String suffix) {
        var normalizedSuffix = EditorFileNames.normalizeSuffix(suffix);
        if (hasSuffix(path, normalizedSuffix)) {
            return path.substring(0, path.length() - normalizedSuffix.length());
        }
        return path;
    }

    private static boolean hasSuffix(String path, String suffix) {
        return suffix.isEmpty() || path.toLowerCase(Locale.ROOT).endsWith(suffix.toLowerCase(Locale.ROOT));
    }

    private static String toAssetPath(Path path) {
        return path.toString().replace('\\', '/');
    }
}
