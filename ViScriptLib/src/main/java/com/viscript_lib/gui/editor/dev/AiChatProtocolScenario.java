package com.viscript_lib.gui.editor.dev;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lowdragmc.lowdraglib2.registry.RegistrationEnvironment;
import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegisterClient;
import com.lowdragmc.lowdraglib2.uitest.ScenarioBuilder;
import com.lowdragmc.lowdraglib2.uitest.ScenarioOptions;
import com.lowdragmc.lowdraglib2.uitest.UIScenario;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.viscript_lib.ViScriptLib;
import com.viscript_lib.gui.ai.AiChatClient;
import com.viscript_lib.gui.settings.AiEditorSettings;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/** 使用回环夹具验证两种流式协议、空闲超时、截断与取消。 */
@LDLRegisterClient(name = "vsl_ai_chat_protocol", group = ViScriptLib.MOD_ID,
        registry = UIScenario.REGISTRY, environment = RegistrationEnvironment.DEV_ONLY)
public final class AiChatProtocolScenario implements UIScenario {
    @Override
    public void configure(ScenarioOptions options) {
        options.requiresWorld(false).defaultTimeoutMs(30000).scenarioTimeoutMs(45000);
    }

    @Override
    public void define(ScenarioBuilder s) {
        s.step("启动流式协议夹具", ctx -> {
            var server = new Fixture();
            ctx.put("server", server);
            ctx.put("future", CompletableFuture.runAsync(() -> verify(server), server.executor));
        }).waitUntil("协议检查完成", ctx -> ((CompletableFuture<?>) ctx.get("future")).isDone())
                .step("核对协议检查结果", ctx -> ((CompletableFuture<?>) ctx.get("future")).join())
                .check("持续思考超过超时长度仍成功且进度可见", ctx -> ((Fixture) ctx.get("server")).longStreamPassed.get())
                .check("界面处理正文耗时不会误判为网络停滞", ctx -> ((Fixture) ctx.get("server")).slowConsumerPassed.get())
                .check("慢速工具参数持续进展不会误判超时", ctx -> ((Fixture) ctx.get("server")).slowToolPassed.get())
                .check("只有心跳的请求会按停滞超时结束", ctx -> ((Fixture) ctx.get("server")).heartbeatPassed.get())
                .check("输出预算使用模型最大值且长回复和大量工具不被本地截断", ctx -> ((Fixture) ctx.get("server")).unlimitedPassed.get())
                .check("推理强度按协议传递且默认不附加参数", ctx -> ((Fixture) ctx.get("server")).reasoningSettingsPassed.get())
                .check("OpenAI 思考字段保留回传且不显示给玩家", ctx -> ((Fixture) ctx.get("server")).reasoningRoundtripPassed.get())
                .check("完全无数据时在一秒空闲后结束", ctx -> ((Fixture) ctx.get("server")).idlePassed.get())
                .check("两种协议的工具参数分片和签名回传正确", ctx -> ((Fixture) ctx.get("server")).protocolPassed.get())
                .check("不完整流没有被当成完整回复", ctx -> ((Fixture) ctx.get("server")).truncatedPassed.get())
                .check("输出截断与无效工具参数有明确原因", ctx -> ((Fixture) ctx.get("server")).toolFailurePassed.get())
                .check("等待流数据时可以停止", ctx -> ((Fixture) ctx.get("server")).cancelPassed.get())
                .teardown("关闭协议夹具", ctx -> { if (ctx.get("server") instanceof Fixture server) server.close(); });
    }

    private static void verify(Fixture fixture) {
        try {
            var messages = new JsonArray(); messages.add(AiChatClient.message("user", "fixture"));
            var visible = new StringBuilder();
            long start = System.nanoTime();
            var progress = new AiChatClient.RequestProgress();
            var longRequest = CompletableFuture.supplyAsync(() -> {
                try { return client(fixture, "long", 1).complete(messages, new JsonArray(), visible::append, progress); }
                catch (Exception exception) { throw new java.util.concurrent.CompletionException(exception); }
            }, fixture.executor);
            boolean sawReasoning = false;
            while (!longRequest.isDone()) {
                sawReasoning |= progress.getPhase() == AiChatClient.Phase.REASONING && progress.getCharacters() > 2;
                Thread.sleep(20);
            }
            var reply = longRequest.join();
            require(System.nanoTime() - start > 2_000_000_000L && visible.toString().equals("开始完成"), "long streaming content");
            require(sawReasoning && progress.getCharacters() == 79 && progress.elapsedSeconds() >= 2
                    && progress.dataIdleSeconds() == 0 && progress.progressIdleSeconds() == 0, "reasoning progress and timing");
            require(reply.get("reasoning_content").getAsString().equals("hidden ".repeat(8)), "hidden reasoning preserved");
            require(reply.getAsJsonArray("tool_calls").get(0).getAsJsonObject().getAsJsonObject("function")
                    .get("arguments").getAsString().equals("{\"registry\":\"item\"}"), "fragmented arguments");
            fixture.longStreamPassed.set(true);
            var delayedVisible = new StringBuilder();
            var firstDelivery = new AtomicBoolean(true);
            start = System.nanoTime();
            var delayedReply = client(fixture, "long", 1).complete(messages, new JsonArray(), text -> {
                if (firstDelivery.getAndSet(false)) {
                    try { Thread.sleep(1500); }
                    catch (InterruptedException exception) {
                        Thread.currentThread().interrupt();
                        throw new java.util.concurrent.CompletionException(exception);
                    }
                }
                delayedVisible.append(text);
            }, new AiChatClient.RequestProgress());
            require(System.nanoTime() - start > 2_000_000_000L && delayedVisible.toString().equals("开始完成")
                    && delayedReply.get("content").getAsString().equals("开始完成"), "slow visible-text consumer");
            fixture.slowConsumerPassed.set(true);
            var roundtrip = messages.deepCopy(); roundtrip.add(reply);
            var result = AiChatClient.message("tool", "{}"); result.addProperty("tool_call_id", "query-1"); roundtrip.add(result);
            client(fixture, "openai-roundtrip", 2).complete(roundtrip, new JsonArray());
            require(fixture.requests.get("/openai-roundtrip/v1/chat/completions").getAsJsonArray("messages")
                    .get(1).getAsJsonObject().get("reasoning_content").getAsString().equals("hidden ".repeat(8)), "reasoning roundtrip");
            fixture.reasoningRoundtripPassed.set(true);
            for (String path : new String[]{"slow-tool", "anthropic-slow-tool"}) {
                var toolProgress = new AiChatClient.RequestProgress();
                start = System.nanoTime();
                var slow = client(fixture, path, 1).complete(messages, new JsonArray(), ignored -> {}, toolProgress);
                require(System.nanoTime() - start > 2_000_000_000L && toolProgress.getPhase() == AiChatClient.Phase.TOOL_ARGUMENTS
                        && toolProgress.getCharacters() == 19 && toolProgress.progressIdleSeconds() == 0, "slow tool progress: " + path);
                require(slow.getAsJsonArray("tool_calls").get(0).getAsJsonObject().getAsJsonObject("function")
                        .get("arguments").getAsString().equals("{\"registry\":\"item\"}"), "slow tool arguments: " + path);
            }
            fixture.slowToolPassed.set(true);
            var heartbeatProgress = new AiChatClient.RequestProgress();
            start = System.nanoTime();
            try {
                client(fixture, "heartbeat", 1).complete(messages, new JsonArray(), ignored -> {}, heartbeatProgress);
                throw new IllegalStateException("Expected heartbeat-only timeout");
            } catch (IOException expected) {
                require(expected.getMessage().equals("viscript_lib.ai.error.no_progress")
                        && System.nanoTime() - start < 1_900_000_000L && heartbeatProgress.dataIdleSeconds() == 0
                        && heartbeatProgress.getCharacters() == 0, "heartbeat-only deadline");
            }
            fixture.heartbeatPassed.set(true);
            verifyUnlimited(fixture);
            fixture.unlimitedPassed.set(true);
            verifyReasoningSettings(fixture);
            fixture.reasoningSettingsPassed.set(true);
            start = System.nanoTime();
            try { client(fixture, "idle", 1).complete(messages, new JsonArray()); throw new IllegalStateException("Expected idle timeout"); }
            catch (HttpTimeoutException expected) { require(System.nanoTime() - start < 1_900_000_000L, "idle deadline"); }
            fixture.idlePassed.set(true);
            visible.setLength(0);
            var anthropic = client(fixture, "anthropic", 2);
            var first = anthropic.complete(messages, new JsonArray(), visible::append);
            require(visible.toString().equals("可见正文") && !visible.toString().contains("private thinking"), "thinking hidden");
            messages.add(first);
            var tool = AiChatClient.message("tool", "{}"); tool.addProperty("tool_call_id", "call-1"); messages.add(tool);
            var second = anthropic.complete(messages, new JsonArray());
            require(second.get("content").getAsString().equals("完成") && fixture.signatureRoundtrip.get(), "signed thinking roundtrip");
            fixture.protocolPassed.set(true);
            for (String path : new String[]{"anthropic-limit", "openai-limit", "anthropic-json-limit", "openai-json-limit",
                    "anthropic-bad-tool", "openai-bad-tool", "anthropic-context-limit", "format"}) {
                String expected = path.equals("format") ? "format" : path.contains("context-limit") ? "context_limit"
                        : path.endsWith("bad-tool") ? "tool_arguments" : "output_limit";
                try {
                    client(fixture, path, 2).complete(new JsonArray(), new JsonArray());
                    throw new IllegalStateException("Expected tool response rejection: " + path);
                } catch (IOException exception) {
                    require(exception.getMessage().equals("viscript_lib.ai.error.response." + expected),
                            path + " reports " + exception.getMessage());
                }
            }
            fixture.toolFailurePassed.set(true);
            try { client(fixture, "truncated", 2).complete(new JsonArray(), new JsonArray()); throw new IllegalStateException("Expected truncated stream"); }
            catch (IOException expected) { require(expected.getMessage().equals("viscript_lib.ai.error.response.incomplete"), "truncated stream rejected"); }
            fixture.truncatedPassed.set(true);
            var cancelled = new AtomicBoolean();
            var worker = Thread.ofVirtual().start(() -> {
                try { client(fixture, "cancel", 10).complete(new JsonArray(), new JsonArray()); }
                catch (InterruptedException expected) { cancelled.set(true); }
                catch (IOException ignored) { }
            });
            long deadline = System.nanoTime() + 2_000_000_000L;
            while (!fixture.cancelStarted.get() && System.nanoTime() < deadline) Thread.sleep(10);
            Thread.sleep(100);
            worker.interrupt(); worker.join(2000);
            require(!worker.isAlive() && cancelled.get(), "stream cancellation");
            fixture.cancelPassed.set(true);
        } catch (Exception exception) { throw new java.util.concurrent.CompletionException(exception); }
    }

    private static AiChatClient client(Fixture fixture, String path, int timeout) {
        return client(fixture, path, timeout, "fixture", "default");
    }

    private static AiChatClient client(Fixture fixture, String path, int timeout, String model, String effort) {
        var settings = new AiEditorSettings();
        settings.setEndpoint("http://127.0.0.1:" + fixture.server.getAddress().getPort() + "/" + path + "/v1");
        settings.setApiProtocol(path.startsWith("anthropic") ? "anthropic" : "openai");
        settings.setModel(model); settings.setReasoningEffort(effort); settings.setRequestTimeoutSeconds(timeout);
        return new AiChatClient(settings);
    }

    private static void verifyReasoningSettings(Fixture fixture) throws IOException, InterruptedException {
        for (String effort : new String[]{"default", "low", "max"}) {
            client(fixture, "effort-" + effort, 2, "fixture", effort).complete(new JsonArray(), new JsonArray());
            var sent = fixture.requests.get("/effort-" + effort + "/v1/chat/completions");
            require(effort.equals("default") ? !sent.has("reasoning_effort") : sent.get("reasoning_effort").getAsString().equals(effort),
                    "OpenAI effort: " + effort);
            require(!sent.has("output_config") && !sent.has("thinking"), "OpenAI request has no Anthropic fields");
        }
        for (String effort : new String[]{"default", "high"}) {
            client(fixture, "anthropic-effort-" + effort, 2, "fixture", effort).complete(new JsonArray(), new JsonArray());
            var sent = fixture.requests.get("/anthropic-effort-" + effort + "/v1/messages");
            require(effort.equals("default") ? !sent.has("output_config")
                    : sent.getAsJsonObject("output_config").get("effort").getAsString().equals("high"), "Anthropic effort: " + effort);
            require(!sent.has("reasoning_effort") && !sent.has("thinking"), "No implicit thinking mode");
        }
        client(fixture, "effort-glm", 2, "glm-5.3", "low").complete(new JsonArray(), new JsonArray());
        require(fixture.requests.get("/effort-glm/v1/chat/completions").get("reasoning_effort").getAsString().equals("low"), "GLM effort");
        for (String path : new String[]{"effort-invalid-glm", "anthropic-effort-invalid-glm"}) {
            boolean anthropic = path.startsWith("anthropic");
            try {
                client(fixture, path, 2, "glm-5.3", anthropic ? "high" : "medium").complete(new JsonArray(), new JsonArray());
                throw new IllegalStateException("Expected GLM effort rejection");
            } catch (IllegalArgumentException expected) {
                require(expected.getMessage().equals("viscript_lib.ai.error.reasoning_glm_" + (anthropic ? "protocol" : "level")), "GLM effort diagnostic");
                require(fixture.requests.keySet().stream().noneMatch(key -> key.startsWith("/" + path + "/")), "Rejected before network");
            }
        }
    }

    private static void verifyUnlimited(Fixture fixture) throws IOException, InterruptedException {
        for (String path : new String[]{"unlimited", "anthropic-unlimited"}) {
            var visible = new StringBuilder();
            var reply = client(fixture, path, 10).complete(new JsonArray(), new JsonArray(), visible::append);
            require(visible.length() == 70_000 && reply.get("content").getAsString().equals(visible.toString()), "long visible content: " + path);
            var calls = reply.getAsJsonArray("tool_calls");
            require(calls.size() == 20, "more than sixteen tools: " + path);
            require(calls.get(19).getAsJsonObject().getAsJsonObject("function").get("arguments").getAsString().length() > 64_000,
                    "large tool arguments: " + path);
            if (path.startsWith("anthropic")) require(reply.getAsJsonArray("anthropic_content").get(0).getAsJsonObject()
                    .get("thinking").getAsString().length() == 2_100_000, "large hidden Anthropic block");
            else require(reply.get("reasoning_content").getAsString().length() == 2_100_000, "large hidden OpenAI block");
        }
        var openai = fixture.requests.get("/unlimited/v1/chat/completions");
        require(!openai.has("max_tokens") && !openai.has("max_completion_tokens"), "OpenAI uses service output default");
        require(fixture.requests.get("/anthropic-unlimited/v1/messages").get("max_tokens").getAsInt() == 196608,
                "Anthropic uses Models API maximum");
        client(fixture, "anthropic-effort-glm", 2, "glm-5.3", "default").complete(new JsonArray(), new JsonArray());
        require(fixture.requests.get("/anthropic-effort-glm/v1/messages").get("max_tokens").getAsInt() == 131072,
                "GLM-5.3 uses published 128K maximum");
        client(fixture, "anthropic-effort-no-models", 2).complete(new JsonArray(), new JsonArray());
        require(!fixture.requests.get("/anthropic-effort-no-models/v1/messages").has("max_tokens"),
                "Compatibility API without metadata uses its own default");
    }

    private static void require(boolean condition, String message) { if (!condition) throw new IllegalStateException(message); }

    private static final class Fixture implements AutoCloseable {
        private final HttpServer server;
        private final java.util.concurrent.ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        private final Map<String, JsonObject> requests = new ConcurrentHashMap<>();
        private final AtomicBoolean longStreamPassed = new AtomicBoolean();
        private final AtomicBoolean slowConsumerPassed = new AtomicBoolean();
        private final AtomicBoolean slowToolPassed = new AtomicBoolean();
        private final AtomicBoolean heartbeatPassed = new AtomicBoolean();
        private final AtomicBoolean unlimitedPassed = new AtomicBoolean();
        private final AtomicBoolean reasoningSettingsPassed = new AtomicBoolean();
        private final AtomicBoolean reasoningRoundtripPassed = new AtomicBoolean();
        private final AtomicBoolean idlePassed = new AtomicBoolean();
        private final AtomicBoolean protocolPassed = new AtomicBoolean();
        private final AtomicBoolean truncatedPassed = new AtomicBoolean();
        private final AtomicBoolean toolFailurePassed = new AtomicBoolean();
        private final AtomicBoolean cancelPassed = new AtomicBoolean();
        private final AtomicBoolean cancelStarted = new AtomicBoolean();
        private final AtomicBoolean signatureRoundtrip = new AtomicBoolean();

        private Fixture() {
            try {
                server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
                server.setExecutor(executor);
                server.createContext("/", exchange -> {
                    try (exchange) {
                        if (exchange.getRequestMethod().equals("GET")) {
                            exchange.getResponseHeaders().set("Content-Type", "application/json");
                            exchange.sendResponseHeaders(exchange.getRequestURI().getPath().contains("no-models") ? 404 : 200, 0);
                            write(exchange, "{\"max_tokens\":196608}"); return;
                        }
                        var request = JsonParser.parseString(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
                        require(request.get("stream").getAsBoolean(), "stream enabled");
                        String path = exchange.getRequestURI().getPath();
                        requests.put(path, request);
                        if (path.startsWith("/anthropic-json-limit") || path.startsWith("/anthropic-context-limit")
                                || path.startsWith("/openai-json-limit") || path.startsWith("/format")) {
                            String body = path.startsWith("/format") ? "{\"choices\":\"sensitive response fixture\"}" : path.startsWith("/anthropic")
                                    ? "{\"stop_reason\":\"" + (path.contains("context-limit") ? "model_context_window_exceeded" : "max_tokens") + "\",\"content\":[]}"
                                    : "{\"choices\":[{\"finish_reason\":\"length\",\"message\":{\"role\":\"assistant\",\"content\":\"unfinished\"}}]}";
                            exchange.getResponseHeaders().set("Content-Type", "application/json");
                            exchange.sendResponseHeaders(200, 0); write(exchange, body); return;
                        }
                        exchange.getResponseHeaders().set("Content-Type", "text/event-stream"); exchange.sendResponseHeaders(200, 0);
                        if (path.startsWith("/unlimited") || path.startsWith("/anthropic-unlimited")) {
                            boolean anthropic = path.startsWith("/anthropic");
                            String hidden = "h".repeat(2_100_000), text = "t".repeat(70_000);
                            if (anthropic) {
                                block(exchange, 0, "{\"type\":\"thinking\",\"thinking\":\"\"}");
                                delta(exchange, 0, "{\"type\":\"thinking_delta\",\"thinking\":\"" + hidden + "\"}");
                                data(exchange, "{\"type\":\"content_block_stop\",\"index\":0}");
                                block(exchange, 1, "{\"type\":\"text\",\"text\":\"" + text + "\"}");
                                data(exchange, "{\"type\":\"content_block_stop\",\"index\":1}");
                            } else openai(exchange, "{\"reasoning_content\":\"" + hidden + "\",\"content\":\"" + text + "\"}", false);
                            for (int i = 0; i < 20; i++) {
                                var args = new JsonObject(); args.addProperty("padding", "p".repeat(70_000));
                                if (anthropic) {
                                    var tool = new JsonObject(); tool.addProperty("type", "tool_use"); tool.addProperty("id", "call-" + i);
                                    tool.addProperty("name", "query_registry"); tool.add("input", args);
                                    block(exchange, i + 2, tool.toString());
                                    data(exchange, "{\"type\":\"content_block_stop\",\"index\":" + (i + 2) + "}");
                                } else {
                                    var function = new JsonObject(); function.addProperty("name", "query_registry"); function.addProperty("arguments", args.toString());
                                    var call = new JsonObject(); call.addProperty("index", i); call.addProperty("id", "call-" + i); call.add("function", function);
                                    var calls = new JsonArray(); calls.add(call); var change = new JsonObject(); change.add("tool_calls", calls);
                                    openai(exchange, change.toString(), false);
                                }
                            }
                            if (anthropic) data(exchange, "{\"type\":\"message_stop\"}");
                            else openai(exchange, "{}", true);
                        } else if (path.startsWith("/anthropic-limit") || path.startsWith("/anthropic-bad-tool")) {
                            block(exchange, 0, "{\"type\":\"tool_use\",\"id\":\"unfinished\",\"name\":\"edit_project\",\"input\":{}}");
                            delta(exchange, 0, "{\"type\":\"input_json_delta\",\"partial_json\":\"{\\\"operations\\\":[\"}");
                            data(exchange, "{\"type\":\"content_block_stop\",\"index\":0}");
                            data(exchange, "{\"type\":\"message_delta\",\"delta\":{\"stop_reason\":\""
                                    + (path.startsWith("/anthropic-limit") ? "max_tokens" : "tool_use") + "\"}}");
                            data(exchange, "{\"type\":\"message_stop\"}");
                        } else if (path.startsWith("/openai-limit") || path.startsWith("/openai-bad-tool")) {
                            data(exchange, "{\"choices\":[{\"delta\":{\"tool_calls\":[{\"index\":0,\"id\":\"unfinished\",\"function\":{\"name\":\"edit_project\",\"arguments\":\"{\"}}]},\"finish_reason\":\""
                                    + (path.startsWith("/openai-limit") ? "length" : "tool_calls") + "\"}]}");
                        } else if (path.startsWith("/long")) {
                            openai(exchange, "{\"content\":\"开始\"}", false);
                            for (int i = 0; i < 8; i++) {
                                Thread.sleep(300); openai(exchange, "{\"reasoning_content\":\"hidden \"}", false);
                            }
                            openai(exchange, "{\"tool_calls\":[{\"index\":0,\"id\":\"query-1\",\"function\":{\"name\":\"query_registry\",\"arguments\":\"{\\\"registry\\\":\"}}]}", false);
                            openai(exchange, "{\"tool_calls\":[{\"index\":0,\"id\":null,\"function\":null}]}", false);
                            openai(exchange, "{\"tool_calls\":[{\"index\":0,\"id\":null,\"function\":{\"arguments\":\"\\\"item\\\"}\"}}],\"content\":\"完成\"}", true);
                        } else if (path.startsWith("/slow-tool") || path.startsWith("/anthropic-slow-tool")) {
                            boolean anthropic = path.startsWith("/anthropic");
                            if (anthropic) block(exchange, 0, "{\"type\":\"tool_use\",\"id\":\"slow\",\"name\":\"query_registry\",\"input\":{}}");
                            else openai(exchange, "{\"tool_calls\":[{\"index\":0,\"id\":\"slow\",\"function\":{\"name\":\"query_registry\",\"arguments\":\"\"}}]}", false);
                            for (String fragment : new String[]{"{", "\"reg", "is", "try\"", ":", "\"it", "em\"", "}"}) {
                                Thread.sleep(300);
                                var value = new JsonObject();
                                if (anthropic) {
                                    value.addProperty("type", "input_json_delta"); value.addProperty("partial_json", fragment);
                                    delta(exchange, 0, value.toString());
                                } else {
                                    var function = new JsonObject(); function.addProperty("arguments", fragment);
                                    value.addProperty("index", 0); value.add("function", function);
                                    var calls = new JsonArray(); calls.add(value);
                                    var change = new JsonObject(); change.add("tool_calls", calls);
                                    openai(exchange, change.toString(), false);
                                }
                            }
                            if (anthropic) {
                                data(exchange, "{\"type\":\"content_block_stop\",\"index\":0}");
                                data(exchange, "{\"type\":\"message_stop\"}");
                            } else openai(exchange, "{}", true);
                        } else if (path.startsWith("/heartbeat")) {
                            while (true) { write(exchange, ": ping\n\n"); Thread.sleep(250); }
                        } else if (path.startsWith("/effort") || path.startsWith("/openai-roundtrip")) {
                            openai(exchange, "{\"content\":\"完成\"}", true);
                        } else if (path.startsWith("/anthropic-effort")) {
                            block(exchange, 0, "{\"type\":\"text\",\"text\":\"完成\"}");
                            data(exchange, "{\"type\":\"content_block_stop\",\"index\":0}");
                            data(exchange, "{\"type\":\"message_stop\"}");
                        } else if (path.startsWith("/idle") || path.startsWith("/cancel")) {
                            if (path.startsWith("/cancel")) { write(exchange, ": started\n\n"); cancelStarted.set(true); }
                            Thread.sleep(3000);
                        } else if (path.startsWith("/truncated")) {
                            openai(exchange, "{\"content\":\"unfinished\"}", false);
                        } else if (path.startsWith("/anthropic")) {
                            if (request.getAsJsonArray("messages").size() > 1) {
                                var blocks = request.getAsJsonArray("messages").get(1).getAsJsonObject().getAsJsonArray("content");
                                signatureRoundtrip.set(blocks.get(0).getAsJsonObject().get("thinking").getAsString().equals("private thinking")
                                        && blocks.get(0).getAsJsonObject().get("signature").getAsString().equals("signed-value")
                                        && blocks.get(2).getAsJsonObject().getAsJsonObject("input").get("registry").getAsString().equals("item"));
                                block(exchange, 0, "{\"type\":\"text\",\"text\":\"完成\"}");
                                data(exchange, "{\"type\":\"content_block_stop\",\"index\":0}");
                            } else {
                                block(exchange, 0, "{\"type\":\"thinking\",\"thinking\":null,\"signature\":null}");
                                delta(exchange, 0, "{\"type\":\"thinking_delta\",\"thinking\":\"private thinking\"}");
                                delta(exchange, 0, "{\"type\":\"signature_delta\",\"signature\":\"signed-value\"}");
                                data(exchange, "{\"type\":\"content_block_stop\",\"index\":0}");
                                block(exchange, 1, "{\"type\":\"text\",\"text\":null}");
                                delta(exchange, 1, "{\"type\":\"text_delta\",\"text\":\"可见正文\"}");
                                data(exchange, "{\"type\":\"content_block_stop\",\"index\":1}");
                                block(exchange, 2, "{\"type\":\"tool_use\",\"id\":\"call-1\",\"name\":\"query_registry\",\"input\":{}}");
                                delta(exchange, 2, "{\"type\":\"input_json_delta\",\"partial_json\":\"{\\\"registry\\\":\"}");
                                delta(exchange, 2, "{\"type\":\"input_json_delta\",\"partial_json\":\"\\\"item\\\"}\"}");
                                data(exchange, "{\"type\":\"content_block_stop\",\"index\":2}");
                            }
                            data(exchange, "{\"type\":\"message_stop\"}");
                        }
                    } catch (InterruptedException exception) { Thread.currentThread().interrupt(); }
                });
                server.start();
            } catch (IOException exception) { throw new IllegalStateException(exception); }
        }

        private static void block(HttpExchange exchange, int index, String block) throws IOException {
            data(exchange, "{\"type\":\"content_block_start\",\"index\":" + index + ",\"content_block\":" + block + "}");
        }
        private static void delta(HttpExchange exchange, int index, String delta) throws IOException {
            data(exchange, "{\"type\":\"content_block_delta\",\"index\":" + index + ",\"delta\":" + delta + "}");
        }
        private static void openai(HttpExchange exchange, String delta, boolean done) throws IOException {
            data(exchange, "{\"choices\":[{\"delta\":" + delta + (done ? ",\"finish_reason\":\"stop\"" : "") + "}]}");
        }
        private static void data(HttpExchange exchange, String value) throws IOException { write(exchange, "data: " + value + "\n\n"); }
        private static void write(HttpExchange exchange, String value) throws IOException {
            exchange.getResponseBody().write(value.getBytes(StandardCharsets.UTF_8)); exchange.getResponseBody().flush();
        }
        @Override
        public void close() { server.stop(0); executor.shutdownNow(); }
    }
}
