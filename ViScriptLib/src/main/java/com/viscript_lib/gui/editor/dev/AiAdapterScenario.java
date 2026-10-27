package com.viscript_lib.gui.editor.dev;

import com.google.gson.*;
import com.lowdragmc.lowdraglib2.Platform;
import com.lowdragmc.lowdraglib2.gui.holder.ModularUIScreen;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.uitest.*;
import com.sun.net.httpserver.HttpServer;
import com.viscript_lib.gui.ai.AiChatSession;
import com.viscript_lib.gui.editor.ViScriptEditor;
import com.viscript_lib.gui.settings.AiEditorSettings;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.Executors;

/** 使用本机确定性模型协议验证领域适配器的真实工具循环、预览和原生撤销历史。 */
public abstract class AiAdapterScenario implements UIScenario {
    /**
     * 创建已载入确定性夹具的真实编辑器。
     *
     * @param context 客户端测试上下文
     * @return 正式编辑器类的测试实例
     */
    protected abstract ViScriptEditor createEditor(TestContext context);

    /**
     * 提供本轮要执行的实际领域操作。
     *
     * @param context 已打开编辑器的上下文
     * @return 工具参数
     */
    protected abstract JsonObject edits(TestContext context);

    /**
     * 提供本轮需要读取的领域页。
     *
     * @param context 已打开编辑器的上下文
     * @return 读取参数
     */
    protected abstract List<JsonObject> reads(TestContext context);

    /**
     * 检查读取上下文、领域数据和视图。
     *
     * @param context 客户端测试上下文
     */
    protected abstract void verifyEdited(TestContext context);

    /**
     * 检查错误请求、未知字段或领域约束。
     *
     * @param context 客户端测试上下文
     */
    protected abstract void verifyValidation(TestContext context);

    /**
     * 检查恢复前一状态后仍保留的领域视图或手动历史。
     *
     * @param context 客户端测试上下文
     */
    protected void verifyRestored(TestContext context) { }

    @Override
    public void configure(ScenarioOptions options) {
        options.requiresWorld(true).guiScale(2).defaultTimeoutMs(15000).scenarioTimeoutMs(90000).tags("ui", "ai_adapter");
    }

    @Override
    public final void define(ScenarioBuilder s) {
        s.openScreen("打开领域编辑器", ctx -> {
                    var editor = createEditor(ctx);
                    ctx.put("ai-editor", editor);
                    var server = new Fixture(reads(ctx), edits(ctx));
                    ctx.put("ai-fixture", server);
                    var settings = AiEditorSettings.of(editor);
                    settings.setEnabled(true); settings.setAllowProjectEdit(true); settings.setRequireConfirmation(true);
                    settings.setApiProtocol("openai");
                    settings.setEndpoint(server.endpoint()); settings.setModel("fixture"); settings.setApiKey("test-only");
                    settings.setMaxContextEntries(64);
                    return new ModularUIScreen(ModularUI.of(UI.of(editor)), Component.empty());
                }).awaitModularUI()
                .step("验证注册表与错误请求约束", this::verifyValidation)
                .step("通过真实 AI 会话发起编辑", ctx -> {
                    ctx.put("ai-before", snapshot(editor(ctx)));
                    ctx.put("ai-history", editor(ctx).historyView.getUndoStack().size());
                    session(ctx).send("Apply the fixture edits");
                })
                .waitUntil("工具调用完成并显示待确认预览", ctx -> !session(ctx).isBusy())
                .step("检查预览与读取结果", ctx -> {
                    ctx.check("成功进入待应用状态", turn(ctx).getState() == AiChatSession.State.PENDING, "PENDING", turn(ctx).getState() + " / " + turn(ctx).getActivity() + " / " + fixture(ctx).errors);
                    if (turn(ctx).getState() != AiChatSession.State.PENDING) throw new IllegalStateException("Adapter tool failed: " + fixture(ctx).errors + " / " + turn(ctx).getActivity());
                    ctx.check("主编辑器显示预览按钮", session(ctx).isPreviewing() && ctx.count("#vsl_ai_editor_apply") == 1);
                    ctx.check("预览未增加历史", editor(ctx).historyView.getUndoStack().size() == (int) ctx.get("ai-history"));
                    ctx.check("服务收到所有读取结果且无工具错误", fixture(ctx).readResults.size() == reads(ctx).size() && !fixture(ctx).failed);
                    ctx.check("工具 Schema 提供领域读取分区", fixture(ctx).sawReadSchema);
                    verifyEdited(ctx);
                    ctx.put("ai-after", snapshot(editor(ctx)));
                })
                .screenshot("domain_ai_preview")
                .hover("#vsl_ai_editor_discard")
                .step("按下放弃", ctx -> press(ctx, "#vsl_ai_editor_discard"))
                .step("释放放弃", AiAdapterScenario::release)
                .step("验证放弃恢复完整工程", ctx -> {
                    ctx.check("放弃恢复快照", snapshot(editor(ctx)).equals(ctx.get("ai-before")));
                    ctx.check("放弃没有增加历史", editor(ctx).historyView.getUndoStack().size() == (int) ctx.get("ai-history"));
                    verifyRestored(ctx);
                    fixture(ctx).reset();
                    session(ctx).send("Apply again");
                })
                .waitUntil("第二轮工具调用完成", ctx -> !session(ctx).isBusy())
                .check("第二轮仍能预览", ctx -> session(ctx).isPreviewing() && turn(ctx).getState() == AiChatSession.State.PENDING)
                .step("记录本轮实际草稿", ctx -> ctx.put("ai-after", snapshot(editor(ctx))))
                .hover("#vsl_ai_editor_apply")
                .step("按下应用", ctx -> press(ctx, "#vsl_ai_editor_apply"))
                .step("释放应用", AiAdapterScenario::release)
                .step("验证应用与撤销", ctx -> {
                    ctx.check("一轮只产生一条历史", editor(ctx).historyView.getUndoStack().size() == (int) ctx.get("ai-history") + 1);
                    ctx.check("应用保留完整草稿", snapshot(editor(ctx)).equals(ctx.get("ai-after")));
                    editor(ctx).historyView.undo();
                    ctx.check("撤销恢复完整工程", snapshot(editor(ctx)).equals(ctx.get("ai-before")));
                    verifyRestored(ctx);
                    editor(ctx).historyView.redo();
                    ctx.check("重做恢复全部修改", snapshot(editor(ctx)).equals(ctx.get("ai-after")));
                }).ticks(3)
                .step("重做后的领域视图仍一致", this::verifyEdited)
                .screenshot("domain_ai_applied")
                .closeScreen()
                .teardown("关闭本地协议夹具与会话", ctx -> {
                    if (ctx.get("ai-editor") instanceof ViScriptEditor editor) editor.getAiChat().getSession().dispose();
                    if (ctx.get("ai-fixture") instanceof Fixture fixture) fixture.close();
                });
    }

    /**
     * 返回当前场景编辑器。
     *
     * @param ctx 测试上下文
     * @return 已打开的编辑器
     */
    protected static ViScriptEditor editor(TestContext ctx) { return ctx.get("ai-editor"); }

    /**
     * 返回本地模型收到的领域读取结果。
     *
     * @param ctx 测试上下文
     * @return 按调用顺序保存的结果
     */
    protected static List<JsonObject> readResults(TestContext ctx) { return fixture(ctx).readResults; }

    /**
     * 解析静态测试参数。
     *
     * @param json 参数文本
     * @return JSON 对象
     */
    protected static JsonObject json(String json) { return JsonParser.parseString(json).getAsJsonObject(); }

    private static void press(TestContext ctx, String selector) {
        var bounds = ctx.query(selector).one().bounds();
        ctx.put("ai-click-x", bounds.centerX()); ctx.put("ai-click-y", bounds.centerY());
        ctx.input().mouseDown(bounds.centerX(), bounds.centerY(), 0);
    }

    private static void release(TestContext ctx) {
        ctx.input().mouseUp((float) ctx.get("ai-click-x"), (float) ctx.get("ai-click-y"), 0);
    }

    private static CompoundTag snapshot(ViScriptEditor editor) { return editor.getCurrentProject().serializeNBT(Platform.getFrozenRegistry()).copy(); }
    private static AiChatSession session(TestContext ctx) { return editor(ctx).getAiChat().getSession(); }
    private static AiChatSession.Turn turn(TestContext ctx) { return session(ctx).getTurns().getLast(); }
    private static Fixture fixture(TestContext ctx) { return ctx.get("ai-fixture"); }

    private static final class Fixture implements AutoCloseable {
        private final HttpServer server;
        private final java.util.concurrent.ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        private final List<JsonObject> reads;
        private final JsonObject edits;
        private final List<JsonObject> readResults = new java.util.concurrent.CopyOnWriteArrayList<>();
        private volatile int request;
        private volatile boolean failed, sawReadSchema;
        private final List<String> errors = new java.util.concurrent.CopyOnWriteArrayList<>();

        private Fixture(List<JsonObject> reads, JsonObject edits) {
            this.reads = reads; this.edits = edits;
            try {
                server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
                server.setExecutor(executor);
                server.createContext("/chat/completions", exchange -> {
                    var input = JsonParser.parseString(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
                    for (var tool : input.getAsJsonArray("tools")) {
                        var function = tool.getAsJsonObject().getAsJsonObject("function");
                        if (function.get("name").getAsString().equals("read_editor")) sawReadSchema = function.getAsJsonObject("parameters").getAsJsonObject("properties").has("section");
                    }
                    var messages = input.getAsJsonArray("messages");
                    if (!messages.isEmpty() && messages.get(messages.size() - 1).getAsJsonObject().get("role").getAsString().equals("tool")) {
                        var result = JsonParser.parseString(messages.get(messages.size() - 1).getAsJsonObject().get("content").getAsString()).getAsJsonObject();
                        if (result.has("error")) { failed = true; errors.add(result.toString()); }
                        if (request <= reads.size()) readResults.add(result);
                    }
                    var message = new JsonObject(); message.addProperty("role", "assistant"); message.addProperty("content", "");
                    if (request <= reads.size()) {
                        var call = new JsonObject(); call.addProperty("id", "fixture-" + request); call.addProperty("type", "function");
                        var function = new JsonObject();
                        function.addProperty("name", request < reads.size() ? "read_editor" : "edit_project");
                        function.addProperty("arguments", (request < reads.size() ? reads.get(request) : edits).toString());
                        call.add("function", function); var calls = new JsonArray(); calls.add(call); message.add("tool_calls", calls);
                    } else message.addProperty("content", "Fixture edits staged.");
                    request++;
                    var choice = new JsonObject(); choice.add("message", message); choice.addProperty("finish_reason", message.has("tool_calls") ? "tool_calls" : "stop");
                    var choices = new JsonArray(); choices.add(choice); var body = new JsonObject(); body.add("choices", choices);
                    byte[] bytes = body.toString().getBytes(StandardCharsets.UTF_8);
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(200, bytes.length); exchange.getResponseBody().write(bytes); exchange.close();
                });
                server.start();
            } catch (java.io.IOException exception) { throw new IllegalStateException(exception); }
        }

        private String endpoint() { return "http://127.0.0.1:" + server.getAddress().getPort(); }
        private void reset() { request = 0; readResults.clear(); errors.clear(); failed = sawReadSchema = false; }
        @Override public void close() { server.stop(0); executor.shutdownNow(); }
    }
}
