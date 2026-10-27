package com.viscript_lib.gui.ai;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.viscript_lib.gui.settings.AiEditorSettings;
import com.viscript_lib.ViScriptLib;
import lombok.Getter;
import java.util.Locale;
import java.util.HashSet;
import java.util.Set;

import java.io.BufferedReader;
import java.io.FilterInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/** 使用 OpenAI Chat Completions 或 Anthropic Messages 兼容协议；仅在会话工作线程调用。 */
public final class AiChatClient {
    /** 模型响应没有形成完整、可执行消息的具体原因。 */
    public enum ResponseFailure { OUTPUT_LIMIT, CONTEXT_LIMIT, TOOL_ARGUMENTS, FORMAT, INCOMPLETE }

    /** 不携带原始响应或工具参数的协议异常，供会话决定是否请求模型重新生成。 */
    @Getter
    public static final class ResponseException extends IOException {
        private final ResponseFailure failure;

        private ResponseException(ResponseFailure failure) {
            super("viscript_lib.ai.error.response." + failure.name().toLowerCase(Locale.ROOT));
            this.failure = failure;
        }
    }

    /** 当前请求正在接收的数据类别；不包含模型思考正文。 */
    public enum Phase { WAITING, REASONING, TOOL_ARGUMENTS, ANSWER }

    /** 网络线程更新、界面线程读取的请求进度；仅保存时间和计数。 */
    @Getter
    public static final class RequestProgress {
        private final long started = System.nanoTime();
        private volatile long lastData = started;
        private volatile long lastProgress = started;
        private volatile Phase phase = Phase.WAITING;
        private volatile int characters;
        private volatile boolean deliveringText;
        private volatile long lastDelivery = started;

        private void deliverText(Consumer<String> consumer, String text) {
            deliveringText = true;
            try {
                consumer.accept(text);
            } finally {
                // 回调可能等待客户端线程；这段本地处理时间不属于模型无响应。
                lastDelivery = System.nanoTime();
                deliveringText = false;
            }
        }

        private void advance(Phase phase, int characters) {
            this.phase = phase;
            this.characters += characters;
            lastProgress = System.nanoTime();
        }

        /** @return 当前 HTTP 请求已经持续的秒数 */
        public long elapsedSeconds() { return secondsSince(started); }
        /** @return 距最后一批网络数据的秒数，心跳也算网络数据 */
        public long dataIdleSeconds() { return secondsSince(lastData); }
        /** @return 距正文、思考或工具参数进展的秒数，心跳不算进展 */
        public long progressIdleSeconds() { return secondsSince(lastProgress); }
        private static long secondsSince(long time) { return TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - time); }
    }

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15)).followRedirects(HttpClient.Redirect.NEVER).build();
    private final URI endpoint;
    private final String model;
    private final String apiKey;
    private final int timeout;
    private final boolean anthropic;
    private final String reasoningEffort;
    private boolean modelMaximumResolved;
    private Integer modelMaximum;

    /**
     * 固定本轮的连接参数，后续设置变动在下一轮生效。
     *
     * @param settings 本地编辑器设置
     * @throws IllegalArgumentException 地址、模型或密钥格式无效
     */
    public AiChatClient(AiEditorSettings settings) {
        String address = settings.getEndpoint().strip().replaceAll("/+$", "");
        anthropic = "anthropic".equals(settings.getApiProtocol()) || ("auto".equals(settings.getApiProtocol())
                && (address.contains("/anthropic") || address.endsWith("/messages")));
        try {
            String suffix = anthropic ? (address.endsWith("/v1") ? "/messages" : "/v1/messages") : "/chat/completions";
            endpoint = URI.create(address.endsWith(anthropic ? "/messages" : "/chat/completions") ? address : address + suffix);
            if (!("https".equals(endpoint.getScheme()) || "http".equals(endpoint.getScheme()))
                    || endpoint.getHost() == null || endpoint.getUserInfo() != null
                    || endpoint.getQuery() != null || endpoint.getFragment() != null) throw new IllegalArgumentException();
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("viscript_lib.ai.error.endpoint");
        }
        model = settings.getModel().strip();
        reasoningEffort = settings.getReasoningEffort();
        AiReasoning.apply(new JsonObject(), anthropic, model, reasoningEffort);
        apiKey = settings.getApiKey().strip();
        timeout = Math.clamp(settings.getRequestTimeoutSeconds(), 1, 300);
        if (model.isEmpty()) throw new IllegalArgumentException("viscript_lib.ai.error.model");
        if (apiKey.chars().anyMatch(c -> c < 33 || c > 126)) throw new IllegalArgumentException("viscript_lib.ai.error.key");
    }

    /**
     * 请求一条助手消息，保留工具调用供主线程校验。
     *
     * @param messages 完整的消息序列
     * @param tools 当前会话允许的工具
     * @return 助手消息
     * @throws IOException 网络请求失败或响应无效
     * @throws InterruptedException 用户停止会话
     */
    public JsonObject complete(JsonArray messages, JsonArray tools) throws IOException, InterruptedException {
        return complete(messages, tools, ignored -> {});
    }

    /**
     * 流式接收助手回复；连续没有网络数据或有效内容进展时超时。
     *
     * @param messages 完整的消息序列
     * @param tools 当前会话允许的工具
     * @param onText 在工作线程接收可显示的增量正文，不包含模型思考
     * @return 完整助手消息及工具调用
     * @throws IOException 网络请求失败、空闲超时或响应无效
     * @throws InterruptedException 用户停止会话
     */
    public JsonObject complete(JsonArray messages, JsonArray tools, Consumer<String> onText)
            throws IOException, InterruptedException {
        return complete(messages, tools, onText, new RequestProgress());
    }

    /**
     * 流式接收回复并更新不含正文的进度计数。
     *
     * @param messages 完整消息序列
     * @param tools 当前允许的工具
     * @param onText 可显示正文的增量回调
     * @param progress 本次请求专用的进度对象
     * @return 完整助手消息及工具调用
     * @throws IOException 网络失败、停滞超时或响应无效
     * @throws InterruptedException 用户停止会话
     */
    public JsonObject complete(JsonArray messages, JsonArray tools, Consumer<String> onText, RequestProgress progress)
            throws IOException, InterruptedException {
        var body = anthropic ? anthropicBody(messages, tools) : new JsonObject();
        body.addProperty("model", model);
        body.addProperty("stream", true);
        if (anthropic) {
            Integer maximum = modelMaximum();
            if (maximum != null) body.addProperty("max_tokens", maximum);
        }
        progress.lastData = progress.lastProgress = System.nanoTime();
        AiReasoning.apply(body, anthropic, model, reasoningEffort);
        if (!anthropic) {
            body.add("messages", messages);
            body.add("tools", tools);
            body.addProperty("tool_choice", "auto");
        }
        var builder = request(endpoint).header("Content-Type", "application/json")
                .header("Accept", "text/event-stream, application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()));
        var response = HTTP.send(builder.build(), HttpResponse.BodyHandlers.ofInputStream());
        boolean streaming = response.headers().firstValue("Content-Type").orElse("").contains("text/event-stream");
        try (var input = new GuardedBody(response.body(), timeout, progress, streaming)) {
            try {
                if (response.statusCode() < 200 || response.statusCode() >= 300) {
                    throw new IOException(httpError(response.statusCode(), new String(input.readAllBytes(), StandardCharsets.UTF_8)));
                }
                if (streaming) {
                    return stream(input, onText, progress);
                }
                // 一些兼容服务仍返回完整 JSON，保持对这些服务的支持。
                var value = JsonParser.parseString(new String(input.readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
                JsonObject reply;
                if (anthropic) {
                    checkOutputLimit(value, "stop_reason");
                    reply = anthropicReply(value);
                } else {
                    var choice = value.getAsJsonArray("choices").get(0).getAsJsonObject();
                    checkOutputLimit(choice, "finish_reason");
                    reply = choice.getAsJsonObject("message").deepCopy();
                }
                validateCalls(reply);
                if (reply.has("content") && !reply.get("content").isJsonNull()) progress.deliverText(onText, reply.get("content").getAsString());
                return reply;
            } catch (IOException exception) {
                if (Thread.currentThread().isInterrupted()) throw new InterruptedException();
                if (input.stalled) throw new IOException("viscript_lib.ai.error.no_progress");
                if (input.timedOut) throw new java.net.http.HttpTimeoutException("Model response idle timeout");
                throw exception;
            } catch (RuntimeException exception) {
                if (exception instanceof java.util.concurrent.CompletionException) throw exception;
                // 不打印异常正文：JSON 解析异常可能包含响应、工具参数或服务端反射的数据。
                ViScriptLib.LOGGER.warn("AI response parsing failed (protocol={}, cause={})",
                        anthropic ? "anthropic" : "openai", exception.getClass().getSimpleName());
                throw new ResponseException(ResponseFailure.FORMAT);
            }
        }
    }

    private JsonObject stream(InputStream input, Consumer<String> onText, RequestProgress progress) throws IOException {
        var reply = new StreamingReply(onText, progress);
        var reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
        var data = new StringBuilder();
        for (String line; (line = reader.readLine()) != null;) {
            if (line.isEmpty()) {
                if (!data.isEmpty()) {
                    reply.accept(data.toString());
                    data.setLength(0);
                    if (reply.finished) break;
                }
            } else if (line.startsWith("data:")) {
                if (!data.isEmpty()) data.append('\n');
                data.append(line.substring(line.startsWith("data: ") ? 6 : 5));
            }
        }
        if (!data.isEmpty() && !reply.finished) reply.accept(data.toString());
        if (!reply.finished) throw new ResponseException(ResponseFailure.INCOMPLETE);
        return reply.result();
    }

    private final class StreamingReply {
        private final Consumer<String> onText;
        private final RequestProgress progress;
        private final StringBuilder text = new StringBuilder();
        private final StringBuilder reasoning = new StringBuilder();
        private final TreeMap<Integer, JsonObject> calls = new TreeMap<>();
        private final TreeMap<Integer, JsonObject> blocks = new TreeMap<>();
        private final TreeMap<Integer, StringBuilder> arguments = new TreeMap<>();
        private final Set<Integer> closedBlocks = new HashSet<>();
        private boolean finished;
        private String stopReason = "";

        private StreamingReply(Consumer<String> onText, RequestProgress progress) {
            this.onText = onText;
            this.progress = progress;
        }

        private void appendText(String value) throws IOException {
            text.append(value);
            if (!value.isEmpty()) {
                progress.advance(Phase.ANSWER, value.length());
                progress.deliverText(onText, value);
            }
        }

        private void accept(String data) throws IOException {
            if (data.equals("[DONE]")) { finished = true; return; }
            var event = JsonParser.parseString(data).getAsJsonObject();
            if (event.has("error")) throw new IOException(httpError(200, data).replace("HTTP 200", "Model error"));
            if (anthropic) {
                switch (event.get("type").getAsString()) {
                    case "content_block_start" -> {
                        int index = event.get("index").getAsInt();
                        var block = event.getAsJsonObject("content_block").deepCopy();
                        blocks.put(index, block);
                        String type = block.get("type").getAsString();
                        if (type.equals("thinking")) progress.advance(Phase.REASONING, 0);
                        if (type.equals("tool_use")) progress.advance(Phase.TOOL_ARGUMENTS, 0);
                        if (block.has("thinking") && !block.get("thinking").isJsonNull())
                            progress.advance(Phase.REASONING, block.get("thinking").getAsString().length());
                        if (block.has("text") && !block.get("text").isJsonNull()) appendText(block.get("text").getAsString());
                    }
                    case "content_block_delta" -> {
                        int index = event.get("index").getAsInt();
                        var block = blocks.get(index);
                        var delta = event.getAsJsonObject("delta");
                        switch (delta.get("type").getAsString()) {
                            case "text_delta" -> { append(block, "text", delta.get("text").getAsString()); appendText(delta.get("text").getAsString()); }
                            case "thinking_delta", "signature_delta" -> {
                                String key = delta.get("type").getAsString().equals("thinking_delta") ? "thinking" : "signature";
                                String value = delta.get(key).getAsString();
                                append(block, key, value);
                                if (!value.isEmpty()) progress.advance(Phase.REASONING, key.equals("thinking") ? value.length() : 0);
                            }
                            case "input_json_delta" -> {
                                String value = delta.get("partial_json").getAsString();
                                arguments.computeIfAbsent(index, ignored -> new StringBuilder()).append(value);
                                if (!value.isEmpty()) progress.advance(Phase.TOOL_ARGUMENTS, value.length());
                            }
                            default -> { }
                        }
                    }
                    case "content_block_stop" -> {
                        int index = event.get("index").getAsInt();
                        closedBlocks.add(index);
                    }
                    case "message_delta" -> {
                        var delta = event.getAsJsonObject("delta");
                        if (delta != null && delta.has("stop_reason") && !delta.get("stop_reason").isJsonNull())
                            stopReason = delta.get("stop_reason").getAsString();
                    }
                    case "message_stop" -> finished = true;
                    default -> { }
                }
            } else {
                var choices = event.getAsJsonArray("choices");
                if (choices == null || choices.isEmpty()) return;
                var choice = choices.get(0).getAsJsonObject();
                if (choice.has("finish_reason") && !choice.get("finish_reason").isJsonNull()) {
                    stopReason = choice.get("finish_reason").getAsString();
                    finished = true;
                }
                var delta = choice.has("delta") && !choice.get("delta").isJsonNull() ? choice.getAsJsonObject("delta") : null;
                if (delta == null) return;
                for (String key : new String[]{"reasoning_content", "reasoning"}) {
                    if (delta.has(key) && delta.get(key).isJsonPrimitive() && delta.get(key).getAsJsonPrimitive().isString()) {
                        String value = delta.get(key).getAsString();
                        if (!value.isEmpty()) {
                            progress.advance(Phase.REASONING, value.length());
                            if (key.equals("reasoning_content")) reasoning.append(value);
                        }
                    }
                }
                if (delta.has("content") && !delta.get("content").isJsonNull()) appendText(delta.get("content").getAsString());
                if (delta.has("tool_calls")) for (var element : delta.getAsJsonArray("tool_calls")) {
                    var part = element.getAsJsonObject();
                    int index = part.get("index").getAsInt();
                    if (index < 0) throw new ResponseException(ResponseFailure.FORMAT);
                    var call = calls.computeIfAbsent(index, ignored -> {
                        var value = new JsonObject(); value.addProperty("type", "function"); value.add("function", new JsonObject()); return value;
                    });
                    if (part.has("id") && !part.get("id").isJsonNull()) append(call, "id", part.get("id").getAsString());
                    if (part.has("function") && !part.get("function").isJsonNull()) {
                        var function = part.getAsJsonObject("function");
                        for (String key : new String[]{"name", "arguments"})
                            if (function.has(key) && !function.get(key).isJsonNull()) {
                                String value = function.get(key).getAsString();
                                append(call.getAsJsonObject("function"), key, value);
                                if (!value.isEmpty()) progress.advance(Phase.TOOL_ARGUMENTS, key.equals("arguments") ? value.length() : 0);
                            }
                    }
                }
            }
        }

        private JsonObject result() throws ResponseException {
            // Anthropic 的结束原因在 content_block_stop 之后，先检查原因再解析工具 JSON。
            checkOutputLimit(stopReason);
            JsonObject value;
            if (anthropic) {
                for (var entry : blocks.entrySet()) {
                    if (entry.getValue().get("type").getAsString().equals("tool_use") && !closedBlocks.contains(entry.getKey()))
                        throw new ResponseException(ResponseFailure.INCOMPLETE);
                }
                for (var entry : arguments.entrySet()) {
                    blocks.get(entry.getKey()).add("input", toolArguments(entry.getValue().toString()));
                }
                var response = new JsonObject();
                var content = new JsonArray(); blocks.values().forEach(content::add);
                response.add("content", content);
                value = anthropicReply(response);
            } else {
                value = message("assistant", text.toString());
                if (!reasoning.isEmpty()) value.addProperty("reasoning_content", reasoning.toString());
                if (!calls.isEmpty()) { var tools = new JsonArray(); calls.values().forEach(tools::add); value.add("tool_calls", tools); }
            }
            validateCalls(value);
            return value;
        }
    }

    private static void checkOutputLimit(JsonObject value, String key) throws ResponseException {
        if (value.has(key) && !value.get(key).isJsonNull()) checkOutputLimit(value.get(key).getAsString());
    }

    private static void checkOutputLimit(String reason) throws ResponseException {
        if (reason.equals("model_context_window_exceeded")) throw new ResponseException(ResponseFailure.CONTEXT_LIMIT);
        if (reason.equals("max_tokens") || reason.equals("length")) throw new ResponseException(ResponseFailure.OUTPUT_LIMIT);
    }

    private static JsonObject toolArguments(String text) throws ResponseException {
        try {
            return JsonParser.parseString(text).getAsJsonObject();
        } catch (RuntimeException exception) {
            throw new ResponseException(ResponseFailure.TOOL_ARGUMENTS);
        }
    }

    private static void validateCalls(JsonObject message) throws ResponseException {
        if (!message.has("tool_calls")) return;
        try {
            for (var element : message.getAsJsonArray("tool_calls")) {
                var call = element.getAsJsonObject();
                AiJson.string(call, "id", 256);
                var function = call.getAsJsonObject("function");
                AiJson.string(function, "name", 128);
                toolArguments(AiJson.string(function, "arguments"));
            }
        } catch (RuntimeException exception) {
            throw new ResponseException(ResponseFailure.TOOL_ARGUMENTS);
        }
    }

    private static void append(JsonObject object, String key, String fragment) {
        object.addProperty(key, (object.has(key) && !object.get(key).isJsonNull() ? object.get(key).getAsString() : "") + fragment);
    }

    private String httpError(int status, String body) {
        String summary = "HTTP " + status;
        try {
            var response = JsonParser.parseString(body).getAsJsonObject();
            var error = response.has("error") && response.get("error").isJsonObject()
                    ? response.getAsJsonObject("error") : response;
            var value = error.get("message");
            if (value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
                String message = value.getAsString();
                if (!apiKey.isEmpty()) message = message.replace(apiKey, "***");
                message = message.replaceAll("[\\p{Cntrl}\\s]+", " ").strip();
                if (!message.isEmpty()) summary += ": " + message.substring(0, Math.min(400, message.length()));
            }
        } catch (RuntimeException ignored) {
        }
        return summary;
    }

    private HttpRequest.Builder request(URI uri) {
        var builder = HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(timeout));
        if (!apiKey.isEmpty()) builder.header("Authorization", "Bearer " + apiKey);
        if (anthropic) {
            builder.header("anthropic-version", "2023-06-01");
            if (!apiKey.isEmpty()) builder.header("x-api-key", apiKey);
        }
        return builder;
    }

    private Integer modelMaximum() throws InterruptedException {
        if (modelMaximumResolved) return modelMaximum;
        String name = model.toLowerCase(Locale.ROOT);
        if (name.equals("glm-5.3") || name.startsWith("glm-5.3-")) {
            // GLM-5.3 官方公布最大输出为 128K，Anthropic 协议需要显式填写该预算。
            modelMaximum = 131072;
        } else {
            String encoded = java.net.URLEncoder.encode(model, StandardCharsets.UTF_8).replace("+", "%20");
            URI models = URI.create(endpoint.toString().substring(0, endpoint.toString().length() - "messages".length()) + "models/" + encoded);
            try {
                var response = HTTP.send(request(models).GET().build(), HttpResponse.BodyHandlers.ofInputStream());
                try (var input = new GuardedBody(response.body(), timeout, new RequestProgress(), false)) {
                    if (response.statusCode() == 200) {
                        var info = JsonParser.parseString(new String(input.readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
                        if (info.has("max_tokens") && !info.get("max_tokens").isJsonNull())
                            modelMaximum = AiJson.integer(info, "max_tokens", 0, 1, Integer.MAX_VALUE);
                    }
                }
            } catch (IOException | RuntimeException ignored) {
                if (Thread.currentThread().isInterrupted()) throw new InterruptedException();
                // 部分兼容服务不提供 Models API；交由服务默认值处理，不另设本地预算。
            }
        }
        modelMaximumResolved = true;
        return modelMaximum;
    }

    private static JsonObject anthropicBody(JsonArray canonical, JsonArray tools) {
        var body = new JsonObject();
        var messages = new JsonArray();
        StringBuilder system = new StringBuilder();
        for (var element : canonical) {
            var input = element.getAsJsonObject();
            String role = input.get("role").getAsString();
            if (role.equals("system")) { system.append(input.get("content").getAsString()).append('\n'); continue; }
            if (role.equals("tool")) {
                var result = new JsonObject();
                result.addProperty("type", "tool_result");
                result.addProperty("tool_use_id", input.get("tool_call_id").getAsString());
                result.addProperty("content", input.get("content").getAsString());
                JsonObject previous = messages.isEmpty() ? null : messages.get(messages.size() - 1).getAsJsonObject();
                if (previous != null && previous.get("role").getAsString().equals("user") && previous.get("content").isJsonArray()) {
                    previous.getAsJsonArray("content").add(result);
                } else {
                    var content = new JsonArray(); content.add(result);
                    var message = new JsonObject(); message.addProperty("role", "user"); message.add("content", content);
                    messages.add(message);
                }
                continue;
            }
            var message = new JsonObject();
            message.addProperty("role", role);
            if (input.has("anthropic_content")) {
                // 保留服务端的签名思考块及工具块供协议回传，不把思考内容显示给玩家。
                message.add("content", input.get("anthropic_content").deepCopy());
            } else if (input.get("content").isJsonArray()) {
                var content = new JsonArray();
                for (var part : input.getAsJsonArray("content")) {
                    var block = part.getAsJsonObject();
                    if (block.get("type").getAsString().equals("image_url")) {
                        String url = block.getAsJsonObject("image_url").get("url").getAsString();
                        int separator = url.indexOf(";base64,");
                        var source = new JsonObject();
                        source.addProperty("type", "base64");
                        source.addProperty("media_type", url.substring("data:".length(), separator));
                        source.addProperty("data", url.substring(separator + ";base64,".length()));
                        var image = new JsonObject();
                        image.addProperty("type", "image");
                        image.add("source", source);
                        content.add(image);
                    } else content.add(block.deepCopy());
                }
                message.add("content", content);
            } else {
                message.addProperty("content", input.get("content").getAsString());
            }
            messages.add(message);
        }
        body.addProperty("system", system.toString());
        body.add("messages", messages);
        var definitions = new JsonArray();
        for (var tool : tools) {
            var function = tool.getAsJsonObject().getAsJsonObject("function");
            var definition = new JsonObject();
            definition.add("name", function.get("name"));
            definition.add("description", function.get("description"));
            definition.add("input_schema", function.get("parameters"));
            definitions.add(definition);
        }
        body.add("tools", definitions);
        var choice = new JsonObject(); choice.addProperty("type", "auto"); body.add("tool_choice", choice);
        return body;
    }

    private static JsonObject anthropicReply(JsonObject response) {
        var blocks = response.getAsJsonArray("content");
        var calls = new JsonArray();
        var text = new StringBuilder();
        for (var element : blocks) {
            var block = element.getAsJsonObject();
            switch (block.get("type").getAsString()) {
                case "text" -> text.append(block.get("text").getAsString());
                case "tool_use" -> {
                    var function = new JsonObject();
                    function.add("name", block.get("name"));
                    function.addProperty("arguments", block.get("input").toString());
                    var call = new JsonObject();
                    call.add("id", block.get("id"));
                    call.addProperty("type", "function");
                    call.add("function", function);
                    calls.add(call);
                }
                default -> { }
            }
        }
        var message = message("assistant", text.toString());
        if (!calls.isEmpty()) message.add("tool_calls", calls);
        message.add("anthropic_content", blocks.deepCopy());
        return message;
    }

    /**
     * 创建协议转换前的文本消息。
     *
     * @param role 消息角色
     * @param text 消息正文
     * @return 新建的消息对象
     */
    public static JsonObject message(String role, String text) {
        var message = new JsonObject();
        message.addProperty("role", role);
        message.addProperty("content", text);
        return message;
    }

    /** 分别监控网络空闲和有效内容停滞，心跳不能无限延长没有进展的请求。 */
    private static final class GuardedBody extends FilterInputStream {
        private static final java.util.concurrent.ScheduledExecutorService WATCHDOG = Executors.newSingleThreadScheduledExecutor(
                Thread.ofPlatform().daemon().name("vsl-ai-idle-timeout").factory());
        private final ScheduledFuture<?> watchdog;
        private final RequestProgress progress;
        private final boolean streaming;
        private volatile boolean timedOut;
        private volatile boolean stalled;

        private GuardedBody(InputStream input, int seconds, RequestProgress progress, boolean streaming) {
            super(input);
            this.progress = progress;
            this.streaming = streaming;
            Thread owner = Thread.currentThread();
            watchdog = WATCHDOG.scheduleAtFixedRate(() -> {
                if (progress.deliveringText && !owner.isInterrupted()) return;
                long now = System.nanoTime();
                boolean idle = now - Math.max(progress.lastData, progress.lastDelivery) >= TimeUnit.SECONDS.toNanos(seconds);
                boolean noProgress = now - Math.max(progress.lastProgress, progress.lastDelivery) >= TimeUnit.SECONDS.toNanos(seconds);
                if (owner.isInterrupted() || idle || noProgress) {
                    timedOut = !owner.isInterrupted() && idle;
                    stalled = !owner.isInterrupted() && !idle && noProgress;
                    try { in.close(); } catch (IOException ignored) { }
                }
            }, 100, 100, TimeUnit.MILLISECONDS);
        }

        @Override
        public int read() throws IOException {
            int value = in.read();
            if (value >= 0) received(1);
            return value;
        }

        @Override
        public int read(byte[] bytes, int offset, int length) throws IOException {
            int count = in.read(bytes, offset, length);
            if (count > 0) received(count);
            return count;
        }

        private void received(int count) throws IOException {
            progress.lastData = System.nanoTime();
            if (!streaming) progress.lastProgress = progress.lastData;
        }

        @Override
        public void close() throws IOException { watchdog.cancel(false); super.close(); }
    }
}
