package com.viscript_lib.gui.ai;

import com.google.gson.JsonObject;
import com.sun.jna.Function;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Base64;

/** 保存一张原始尺寸的 PNG 附件；不写入磁盘或编辑器设置。 */
public record AiChatImage(String base64, int width, int height) {
    /**
     * 读取系统剪贴板中的图片并转换为 PNG。
     *
     * @return 图片附件；剪贴板没有图片时返回 {@code null}
     * @throws IOException 剪贴板或图片无法读取
     */
    public static @Nullable AiChatImage readClipboard() throws IOException {
        try {
            BufferedImage image;
            if (Minecraft.ON_OSX) {
                // macOS 的游戏由 GLFW 持有主线程，避免初始化 AWT AppKit 工具包。
                byte[] bytes = MacClipboard.read();
                if (bytes == null) return null;
                image = ImageIO.read(new ByteArrayInputStream(bytes));
            } else if (com.sun.jna.Platform.isWindows()) {
                image = WindowsClipboard.read();
                if (image == null) return null;
            } else {
                byte[] bytes = LinuxClipboard.read();
                if (bytes == null) return null;
                image = ImageIO.read(new ByteArrayInputStream(bytes));
            }
            if (image == null) throw new IOException("Unsupported clipboard image");
            return fromImage(image);
        } catch (Exception | LinkageError exception) {
            throw new IOException("Cannot read clipboard image", exception);
        }
    }

    private static final class LinuxClipboard {
        private static byte @Nullable [] read() throws IOException, InterruptedException {
            // Minecraft 启用了 AWT headless，使用桌面已有的剪贴板工具访问 Wayland/X11。
            for (String[] command : new String[][]{
                    {"wl-paste", "--no-newline", "--type", "image/png"},
                    {"xclip", "-selection", "clipboard", "-target", "image/png", "-out"}}) {
                Process process;
                try { process = new ProcessBuilder(command).redirectError(ProcessBuilder.Redirect.DISCARD).start(); }
                catch (IOException unavailable) { continue; }
                try {
                    var output = java.util.concurrent.CompletableFuture.supplyAsync(() -> {
                        try (var input = process.getInputStream()) { return input.readAllBytes(); }
                        catch (IOException exception) { throw new java.io.UncheckedIOException(exception); }
                    });
                    if (!process.waitFor(2, java.util.concurrent.TimeUnit.SECONDS)) throw new IOException("Clipboard did not respond");
                    byte[] bytes = output.join();
                    if (process.exitValue() == 0 && bytes.length > 0) return bytes;
                } finally { process.destroyForcibly(); }
            }
            return null;
        }
    }

    private static final class WindowsClipboard {
        private static final NativeLibrary USER = NativeLibrary.getInstance("user32");
        private static final NativeLibrary KERNEL = NativeLibrary.getInstance("kernel32");

        private static Function user(String name) { return USER.getFunction(name, Function.ALT_CONVENTION); }
        private static Function kernel(String name) { return KERNEL.getFunction(name, Function.ALT_CONVENTION); }

        private static BufferedImage read() throws IOException {
            if (user("OpenClipboard").invokeInt(new Object[]{Pointer.NULL}) == 0) throw new IOException("Clipboard busy");
            try {
                int png = user("RegisterClipboardFormatA").invokeInt(new Object[]{"PNG"});
                for (int format : new int[]{png, 17, 8}) {
                    var handle = user("GetClipboardData").invokePointer(new Object[]{format});
                    if (handle == null) continue;
                    var data = kernel("GlobalLock").invokePointer(new Object[]{handle});
                    if (data == null) continue;
                    try {
                        long length = kernel("GlobalSize").invokeLong(new Object[]{handle});
                        byte[] bytes = data.getByteArray(0, Math.toIntExact(length));
                        if (format != png) bytes = bitmapFile(bytes);
                        var image = ImageIO.read(new ByteArrayInputStream(bytes));
                        if (image != null) return image;
                    } finally { kernel("GlobalUnlock").invokeInt(new Object[]{handle}); }
                }
                return null;
            } finally { user("CloseClipboard").invokeInt(new Object[]{}); }
        }

        private static byte[] bitmapFile(byte[] dib) throws IOException {
            if (dib.length < 40) throw new IOException("Invalid clipboard bitmap");
            var header = ByteBuffer.wrap(dib).order(ByteOrder.LITTLE_ENDIAN);
            int size = header.getInt(0), bits = Short.toUnsignedInt(header.getShort(14));
            long colors = Integer.toUnsignedLong(header.getInt(32));
            if (colors == 0 && bits <= 8) colors = 1L << bits;
            int offset = Math.toIntExact(14L + size + colors * 4 + (size == 40 && header.getInt(16) == 3 ? 12 : 0));
            var file = ByteBuffer.allocate(Math.addExact(14, dib.length)).order(ByteOrder.LITTLE_ENDIAN);
            file.put((byte) 'B').put((byte) 'M').putInt(file.capacity()).putInt(0).putInt(offset).put(dib);
            return file.array();
        }
    }

    /**
     * 将像素保存为无损 PNG 附件。
     *
     * @param image 原始图片
     * @return 保留尺寸和透明度的附件
     * @throws IOException PNG 无法编码
     */
    public static AiChatImage fromImage(BufferedImage image) throws IOException {
        var bytes = new ByteArrayOutputStream();
        if (!ImageIO.write(image, "png", bytes)) throw new IOException("PNG encoder unavailable");
        return new AiChatImage(Base64.getEncoder().encodeToString(bytes.toByteArray()), image.getWidth(), image.getHeight());
    }

    JsonObject content() {
        var content = new JsonObject();
        content.addProperty("type", "image_url");
        var image = new JsonObject();
        image.addProperty("url", "data:image/png;base64," + base64);
        content.add("image_url", image);
        return content;
    }

    private static final class MacClipboard {
        private static final NativeLibrary APPKIT = NativeLibrary.getInstance("AppKit");
        private static final NativeLibrary OBJC = NativeLibrary.getInstance("objc");
        private static final Function SEND = OBJC.getFunction("objc_msgSend");

        private static Pointer send(Pointer target, String selector, Object... arguments) {
            var args = new Object[arguments.length + 2];
            args[0] = target;
            args[1] = OBJC.getFunction("sel_registerName").invokePointer(new Object[]{selector});
            System.arraycopy(arguments, 0, args, 2, arguments.length);
            return SEND.invokePointer(args);
        }

        private static Pointer type(String name) {
            return OBJC.getFunction("objc_getClass").invokePointer(new Object[]{name});
        }

        private static byte @Nullable [] read() {
            Pointer pool = send(send(type("NSAutoreleasePool"), "alloc"), "init");
            try {
                var pasteboard = send(type("NSPasteboard"), "generalPasteboard");
                for (String format : new String[]{"public.png", "public.tiff", "public.jpeg"}) {
                    var name = send(type("NSString"), "stringWithUTF8String:", format);
                    var data = send(pasteboard, "dataForType:", name);
                    if (data == null) continue;
                    long length = Pointer.nativeValue(send(data, "length"));
                    if (length > 0) return send(data, "bytes").getByteArray(0, Math.toIntExact(length));
                }
                return null;
            } finally { send(pool, "drain"); }
        }
    }
}
