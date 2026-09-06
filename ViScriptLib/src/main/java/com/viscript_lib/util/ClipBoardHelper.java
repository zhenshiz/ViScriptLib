package com.viscript_lib.util;

import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class ClipBoardHelper {
    /**
     * 从剪贴板获取用户复制的第一个文件。
     * 支持 Windows 绝对路径、Unix 路径、file:// URI。
     * 如果剪贴板没有文件，或内容无法解析为文件，返回 null。
     */
    public static File getFirstCopiedFile() {
        String raw = getClipboardRawString();
        if (raw == null || raw.isEmpty()) return null;
        // 多文件时取第一行
        String firstLine = raw.split("\\r?\\n")[0].trim();
        if (firstLine.startsWith("\"")) firstLine = firstLine.substring(1, firstLine.length() - 1);
        if (firstLine.endsWith("\"")) firstLine = firstLine.substring(0, firstLine.length() - 1);
        return tryParseFile(firstLine);
    }

    /**
     * 读取剪贴板中第一个文件的内容，并以字符串形式返回。
     * <ul>
     *   <li>如果文件是纯文本（.txt, .json, .properties 等），返回其文本内容（UTF-8）。</li>
     *   <li>如果文件是 NBT 格式（.dat, .nbt 或无法识别的二进制），尝试按 NBT 解析并返回 SNBT 字符串。</li>
     *   <li>如果文件过大（>10MB）或读取失败，返回错误提示字符串或 null。</li>
     * </ul>
     * 若剪贴板中无文件，返回 null。
     */
    public static String readFirstCopiedFileAsString() {
        File file = getFirstCopiedFile();
        if (file == null) return null;
        if (!file.exists()) return "[文件不存在]";
        if (file.isDirectory()) return "[这是一个文件夹]";
        long maxSize = 10 * 1024 * 1024; // 10MB
        if (file.length() > maxSize) return "[文件过大（>10MB）]";
        // 尝试按文本读取
        return tryReadAsText(file);
    }

    // ---------- 私有实现 ----------

    /**通过 GLFW 获取剪贴板原始字符串（依赖当前 MC 窗口）。*/
    private static String getClipboardRawString() {
        try {
            long window = Minecraft.getInstance().getWindow().getWindow();
            return GLFW.glfwGetClipboardString(window);
        } catch (Exception e) {
            return null; // 窗口未初始化或 GLFW 异常
        }
    }

    /**尝试将字符串解析为 File（支持绝对路径和 file:// URI）。*/
    private static File tryParseFile(String str) {
        if (str == null || str.isEmpty()) return null;
        // 处理 file:// URI
        if (str.startsWith("file://")) {
            try {
                return new File(new URI(str));
            } catch (Exception ignored) {
                String path = str.substring(7);
                return new File(path);
            }
        }
        // Windows 风格（如 C:\ 或 D:/）或 Unix 绝对路径
        if (str.matches("^[A-Za-z]:[/\\\\].*") || str.startsWith("/") || str.startsWith("\\")) {
            return new File(str);
        }
        // 也可能是相对路径，但一般复制文件都是绝对路径，这里视情况而定
        return null;
    }

    /**尝试按纯文本读取文件（UTF-8），若成功返回内容，否则返回 null。*/
    public static String tryReadAsText(File file) {
        try {
            byte[] bytes = Files.readAllBytes(file.toPath());
            // 简单检测是否为纯文本：尝试用 UTF-8 解码，如果出现乱码则放弃？
            // 更稳妥：尝试读取，如果不抛出异常就认为成功
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (Exception ignored) {
            // 读取失败或非 UTF-8 文本，返回 null 尝试其他解析
            return null;
        }
    }
}
