package com.viscript_lib.gui.editor.dev;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lowdragmc.lowdraglib2.editor.settings.EditorSettings;
import com.lowdragmc.lowdraglib2.gui.holder.ModularUIScreen;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextArea;
import com.lowdragmc.lowdraglib2.gui.ui.window.ModularUIWindow;
import com.lowdragmc.lowdraglib2.registry.RegistrationEnvironment;
import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegisterClient;
import com.lowdragmc.lowdraglib2.uitest.*;
import com.sun.jna.Function;
import com.sun.jna.Memory;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;
import com.sun.net.httpserver.HttpServer;
import com.viscript_lib.ViScriptLib;
import com.viscript_lib.gui.ai.AiChatClient;
import com.viscript_lib.gui.ai.AiChatSession;
import com.viscript_lib.gui.settings.AiEditorSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import javax.imageio.ImageIO;
import java.awt.Toolkit;
import java.awt.datatransfer.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;

/** 验证真实系统剪贴板、原生窗口快捷键与两种识图消息协议。 */
@LDLRegisterClient(name = "vsl_ai_chat_images", group = ViScriptLib.MOD_ID,
        registry = UIScenario.REGISTRY, environment = RegistrationEnvironment.DEV_ONLY)
public final class AiChatImageScenario implements UIScenario {
    @Override
    public void configure(ScenarioOptions options) {
        options.requiresWorld(false).guiScale(2).defaultTimeoutMs(15000).scenarioTimeoutMs(90000);
    }

    @Override
    public void define(ScenarioBuilder s) {
        s.step("创建图片与保存系统剪贴板", ctx -> ctx.put("fixture", new Fixture()))
                .openScreen("打开实际编辑器", ctx -> {
                    var editor = new TestEditor();
                    editor.loadProject(DevFunctionFileProjectType.TYPE.newEmptyProject(), null);
                    var settings = AiEditorSettings.of(editor);
                    settings.setEnabled(true);
                    settings.setApiProtocol("openai");
                    settings.setEndpoint(fixture(ctx).endpoint());
                    settings.setModel("fixture");
                    settings.setApiKey("image-fixture-key");
                    ctx.put("editor", editor);
                    return new ModularUIScreen(ModularUI.of(UI.of(editor)), Component.empty());
                }).awaitModularUI()
                .step("打开真实独立聊天窗口", ctx -> editor(ctx).getAiChat().open())
                .waitUntil("窗口已显示", ctx -> window(ctx) != null && window(ctx).isOpen()).ticks(4)
                .check("初始无图片附件", ctx -> count(ctx, ".vsl_ai_pending_image") == 0)
                .step("放入系统图片剪贴板", ctx -> fixture(ctx).clipboard.image(fixture(ctx).image, "png"));
        click(s, "#vsl_ai_input");
        paste(s);
        s.waitUntil("快捷键生成待发送预览", ctx -> count(ctx, ".vsl_ai_pending_image") == 1)
                .check("粘贴图片不写入文字输入框", ctx -> input(ctx).getLines().stream().allMatch(String::isEmpty))
                .check("尚未发送任何网络请求", ctx -> fixture(ctx).requests.isEmpty())
                .screenshotSurface("ai-image-pending", ctx -> window(ctx).surface());
        paste(s);
        s.waitUntil("同一条消息可添加多张图片", ctx -> count(ctx, ".vsl_ai_pending_image") == 2)
                .step("定位第一张图片移除按钮", ctx -> ctx.in(window(ctx).getModularUI(), ".vsl_ai_remove_image")
                        .nth(0).one().element().setId("vsl_test_remove_image"));
        click(s, "#vsl_test_remove_image");
        s.check("移除图片不影响其他附件", ctx -> count(ctx, ".vsl_ai_pending_image") == 1)
                .step("禁用设置以验证发送失败保留附件", ctx -> AiEditorSettings.of(editor(ctx)).setEnabled(false));
        click(s, "#vsl_ai_send");
        s.check("设置校验失败仍保留附件", ctx -> count(ctx, ".vsl_ai_pending_image") == 1 && session(ctx).getTurns().isEmpty())
                .step("恢复发送设置", ctx -> AiEditorSettings.of(editor(ctx)).setEnabled(true));
        click(s, "#vsl_ai_send");
        s.waitUntil("只含图片的消息收到回复", ctx -> !session(ctx).isBusy() && !session(ctx).getTurns().isEmpty())
                .check("仅图片消息正常结束", ctx -> turn(ctx).getState() == AiChatSession.State.ANSWERED
                        && turn(ctx).getPrompt().isEmpty() && turn(ctx).getImages().size() == 1)
                .check("发送后附件进入消息且输入区清空", ctx -> count(ctx, ".vsl_ai_pending_image") == 0 && count(ctx, ".vsl_ai_sent_image") == 1)
                .step("核对 OpenAI 原图与透明度", ctx -> {
                    var content = fixture(ctx).requests.getFirst().getAsJsonArray("messages").get(1).getAsJsonObject().getAsJsonArray("content");
                    ctx.check("OpenAI 仅图片消息没有空文字块", content.size() == 1 && content.get(0).getAsJsonObject().get("type").getAsString().equals("image_url"));
                    verifyPixels(ctx, content.get(0).getAsJsonObject().getAsJsonObject("image_url").get("url").getAsString().split(",", 2)[1]);
                })
                .screenshotSurface("ai-image-sent", ctx -> window(ctx).surface())
                .step("替换为普通文字剪贴板", ctx -> {
                    ctx.mc().keyboardHandler.setClipboard("look again");
                    ctx.check("系统文字剪贴板可读取", ctx.mc().keyboardHandler.getClipboard().equals("look again"));
                });
        click(s, "#vsl_ai_input");
        paste(s);
        s.step("检查普通文字粘贴", ctx -> ctx.check("普通文字粘贴内容", String.join("\n", input(ctx).getLines()).equals("look again"), "look again", String.join("\n", input(ctx).getLines()) + " | " + ctx.in(window(ctx).getModularUI(), "#vsl_ai_status").one().as(com.lowdragmc.lowdraglib2.gui.ui.elements.TextElement.class).getText().getString()))
                .screenshotSurface("ai-text-paste", ctx -> window(ctx).surface())
                .waitUntil("文字仍按原生路径粘贴", ctx -> String.join("\n", input(ctx).getLines()).equals("look again"))
                .check("普通文字不会创建图片附件", ctx -> count(ctx, ".vsl_ai_pending_image") == 0);
        click(s, "#vsl_ai_send");
        s.waitUntil("后续对话完成", ctx -> !session(ctx).isBusy() && session(ctx).getTurns().size() == 2)
                .check("后续请求保留前一轮图片", ctx -> {
                    var messages = fixture(ctx).requests.getLast().getAsJsonArray("messages");
                    return messages.get(1).getAsJsonObject().get("content").isJsonArray()
                            && messages.get(3).getAsJsonObject().get("content").getAsString().equals("look again");
                })
                .step("切换 Anthropic 并复制系统 TIFF 图片", ctx -> {
                    AiEditorSettings.of(editor(ctx)).setApiProtocol("anthropic");
                    fixture(ctx).clipboard.image(fixture(ctx).image, Minecraft.ON_OSX ? "tiff" : "png");
                });
        click(s, "#vsl_ai_input");
        paste(s);
        s.waitUntil("系统 TIFF 也可粘贴", ctx -> count(ctx, ".vsl_ai_pending_image") == 1);
        click(s, "#vsl_ai_paste_image");
        s.waitUntil("图片按钮可继续添加附件", ctx -> count(ctx, ".vsl_ai_pending_image") == 2)
                .step("输入配图文字", ctx -> "two images".codePoints().forEach(c -> ctx.input(window(ctx)).charTyped(c, 0)));
        click(s, "#vsl_ai_send");
        s.waitUntil("Anthropic 图文请求完成", ctx -> !session(ctx).isBusy() && session(ctx).getTurns().size() == 3)
                .check("图文消息保留文字与全部附件", ctx -> turn(ctx).getState() == AiChatSession.State.ANSWERED
                        && turn(ctx).getPrompt().equals("two images") && turn(ctx).getImages().size() == 2)
                .step("核对 Anthropic 图像源", ctx -> {
                    var messages = fixture(ctx).requests.getLast().getAsJsonArray("messages");
                    var content = messages.get(messages.size() - 1).getAsJsonObject().getAsJsonArray("content");
                    ctx.check("文字与两张图片均发送", content.size() == 3 && content.get(0).getAsJsonObject().get("text").getAsString().equals("two images"));
                    var source = content.get(1).getAsJsonObject().getAsJsonObject("source");
                    ctx.check("Anthropic 使用 base64 PNG 源", content.get(1).getAsJsonObject().get("type").getAsString().equals("image")
                            && source.get("type").getAsString().equals("base64") && source.get("media_type").getAsString().equals("image/png"));
                    ctx.check("历史图片也转换为 Anthropic 内容块", messages.get(0).getAsJsonObject().getAsJsonArray("content")
                            .get(0).getAsJsonObject().get("type").getAsString().equals("image"));
                    verifyPixels(ctx, source.get("data").getAsString());
                })
                .step("关闭窗口释放预览", ctx -> editor(ctx).getAiChat().close())
                .check("窗口关闭后会话保留图片", ctx -> window(ctx) == null && turn(ctx).getImages().size() == 2)
                .step("再次打开窗口", ctx -> editor(ctx).getAiChat().open())
                .waitUntil("历史图片重新显示", ctx -> window(ctx) != null && window(ctx).isOpen() && count(ctx, ".vsl_ai_sent_image") == 3)
                .ticks(3).screenshotSurface("ai-image-history", ctx -> window(ctx).surface());
        click(s, "#vsl_ai_new_chat");
        s.check("新对话清除图片和历史上下文", ctx -> session(ctx).getTurns().isEmpty()
                        && count(ctx, ".vsl_ai_sent_image") == 0 && count(ctx, ".vsl_ai_pending_image") == 0)
                .teardown("关闭夹具并恢复系统剪贴板", ctx -> {
                    if (ctx.get("editor") instanceof TestEditor editor) editor.getAiChat().close();
                    if (ctx.get("fixture") instanceof Fixture fixture) fixture.close();
                    ctx.mc().setScreen(null);
                });
    }

    private static void paste(ScenarioBuilder s) {
        s.step("按粘贴快捷键", ctx -> ctx.input(window(ctx))
                .keyDown(Minecraft.ON_OSX ? GLFW.GLFW_KEY_LEFT_SUPER : GLFW.GLFW_KEY_LEFT_CONTROL, 0)
                .key(GLFW.GLFW_KEY_V, Minecraft.ON_OSX ? GLFW.GLFW_MOD_SUPER : GLFW.GLFW_MOD_CONTROL)
                .keyUp(Minecraft.ON_OSX ? GLFW.GLFW_KEY_LEFT_SUPER : GLFW.GLFW_KEY_LEFT_CONTROL, 0)).ticks(2);
    }

    private static void click(ScenarioBuilder s, String selector) {
        s.step("定位 " + selector, ctx -> ctx.input(window(ctx)).moveTo(ctx.in(window(ctx).getModularUI(), selector).one().element()))
                .step("按下 " + selector, ctx -> ctx.input(window(ctx)).mouseDown(0))
                .step("释放 " + selector, ctx -> ctx.input(window(ctx)).mouseUp(0)).ticks(2);
    }

    private static void verifyPixels(TestContext ctx, String base64) {
        try {
            var image = ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(base64)));
            ctx.check("发送完整尺寸与像素透明度", image.getWidth() == 640 && image.getHeight() == 360
                    && image.getRGB(10, 10) == fixture(ctx).image.getRGB(10, 10)
                    && image.getRGB(300, 180) == fixture(ctx).image.getRGB(300, 180));
        } catch (IOException exception) { throw new IllegalStateException(exception); }
    }

    private static int count(TestContext ctx, String selector) { return ctx.in(window(ctx).getModularUI(), selector).count(); }
    private static TextArea input(TestContext ctx) { return ctx.in(window(ctx).getModularUI(), "#vsl_ai_input").one().as(TextArea.class); }
    private static TestEditor editor(TestContext ctx) { return ctx.get("editor"); }
    private static Fixture fixture(TestContext ctx) { return ctx.get("fixture"); }
    private static AiChatSession session(TestContext ctx) { return editor(ctx).getAiChat().getSession(); }
    private static AiChatSession.Turn turn(TestContext ctx) { return session(ctx).getTurns().getLast(); }
    private static ModularUIWindow window(TestContext ctx) { return editor(ctx).getAiChat().getWindow(); }

    private static final class TestEditor extends DevFunctionFileEditor {
        @Override
        protected EditorSettings createSettings() {
            return super.createSettings().setSettingsFile(Path.of(System.getProperty("ldlib2.uitest.out"), "ai-image-unused-settings.json").toFile());
        }
    }

    private static final class Fixture implements AutoCloseable {
        private final HttpServer server;
        private final java.util.concurrent.ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        private final List<JsonObject> requests = new CopyOnWriteArrayList<>();
        private final ClipboardFixture clipboard = new ClipboardFixture();
        private final BufferedImage image = new BufferedImage(640, 360, BufferedImage.TYPE_INT_ARGB);

        private Fixture() {
            try {
                for (int y = 0; y < image.getHeight(); y++) for (int x = 0; x < image.getWidth(); x++)
                    image.setRGB(x, y, ((x < 80 ? 128 : 255) << 24) | ((x * 255 / 640) << 16) | ((y * 255 / 360) << 8) | 128);
                server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
                server.setExecutor(executor);
                server.createContext("/", exchange -> {
                    try (exchange) {
                        exchange.getResponseHeaders().set("Content-Type", "application/json");
                        if (exchange.getRequestMethod().equals("GET")) {
                            exchange.sendResponseHeaders(200, 0);
                            exchange.getResponseBody().write("{\"max_tokens\":10000}".getBytes(StandardCharsets.UTF_8));
                            return;
                        }
                        var request = JsonParser.parseString(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
                        requests.add(request);
                        JsonObject response;
                        if (exchange.getRequestURI().getPath().endsWith("/messages")) {
                            response = new JsonObject();
                            var content = new JsonArray(); var block = new JsonObject();
                            block.addProperty("type", "text"); block.addProperty("text", "已收到图片。"); content.add(block);
                            response.add("content", content); response.addProperty("stop_reason", "end_turn");
                        } else {
                            response = new JsonObject(); var choices = new JsonArray(); var choice = new JsonObject();
                            choice.add("message", AiChatClient.message("assistant", "已收到图片。"));
                            choice.addProperty("finish_reason", "stop"); choices.add(choice); response.add("choices", choices);
                        }
                        exchange.sendResponseHeaders(200, 0);
                        exchange.getResponseBody().write(response.toString().getBytes(StandardCharsets.UTF_8));
                    }
                });
                server.start();
            } catch (IOException exception) { throw new IllegalStateException(exception); }
        }

        private String endpoint() { return "http://127.0.0.1:" + server.getAddress().getPort() + "/v1"; }

        @Override
        public void close() { server.stop(0); executor.shutdownNow(); clipboard.close(); }
    }

    private static final class ClipboardFixture implements AutoCloseable {
        private final Transferable original = Minecraft.ON_OSX ? null : Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        private final Map<String, byte[]> macOriginal = new LinkedHashMap<>();

        private ClipboardFixture() {
            if (Minecraft.ON_OSX) {
                var types = Mac.call(Mac.board(), "types");
                int count = (int) Pointer.nativeValue(Mac.call(types, "count"));
                for (int i = 0; i < count; i++) {
                    var type = Mac.call(types, "objectAtIndex:", (long) i);
                    var data = Mac.call(Mac.board(), "dataForType:", type);
                    if (data != null) macOriginal.put(Mac.call(type, "UTF8String").getString(0, "UTF-8"),
                            Mac.call(data, "bytes").getByteArray(0, (int) Pointer.nativeValue(Mac.call(data, "length"))));
                }
            }
        }

        private void image(BufferedImage image, String format) {
            try {
                if (Minecraft.ON_OSX) {
                    var output = new ByteArrayOutputStream();
                    if (!ImageIO.write(image, format, output)) throw new IllegalStateException("No image encoder: " + format);
                    Mac.call(Mac.board(), "clearContents");
                    Mac.write("public." + format, output.toByteArray());
                } else Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new Transferable() {
                    @Override public DataFlavor[] getTransferDataFlavors() { return new DataFlavor[]{DataFlavor.imageFlavor}; }
                    @Override public boolean isDataFlavorSupported(DataFlavor flavor) { return flavor.equals(DataFlavor.imageFlavor); }
                    @Override public Object getTransferData(DataFlavor flavor) { return image; }
                }, null);
            } catch (IOException exception) { throw new IllegalStateException(exception); }
        }

        @Override
        public void close() {
            if (Minecraft.ON_OSX) {
                Mac.call(Mac.board(), "clearContents"); macOriginal.forEach(Mac::write);
            } else if (original != null) Toolkit.getDefaultToolkit().getSystemClipboard().setContents(original, null);
        }
    }

    private static final class Mac {
        private static final NativeLibrary APPKIT = NativeLibrary.getInstance("AppKit");
        private static final NativeLibrary OBJC = NativeLibrary.getInstance("objc");
        private static final Function SEND = OBJC.getFunction("objc_msgSend");
        private static Pointer call(Pointer target, String selector, Object... arguments) {
            var values = new Object[arguments.length + 2]; values[0] = target;
            values[1] = OBJC.getFunction("sel_registerName").invokePointer(new Object[]{selector});
            System.arraycopy(arguments, 0, values, 2, arguments.length);
            return SEND.invokePointer(values);
        }
        private static Pointer type(String name) { return OBJC.getFunction("objc_getClass").invokePointer(new Object[]{name}); }
        private static Pointer board() { return call(type("NSPasteboard"), "generalPasteboard"); }
        private static void write(String name, byte[] bytes) {
            try (var memory = new Memory(Math.max(1, bytes.length))) {
                memory.write(0, bytes, 0, bytes.length);
                call(board(), "setData:forType:", call(type("NSData"), "dataWithBytes:length:", memory, (long) bytes.length),
                        call(type("NSString"), "stringWithUTF8String:", name));
            }
        }
    }
}
