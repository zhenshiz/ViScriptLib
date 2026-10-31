package com.viscript_lib.gui.ai;

import com.google.gson.JsonObject;

import java.util.List;
import java.util.Locale;

/** 将显式推理强度写入所选协议的请求，默认值不改变服务端行为。 */
public final class AiReasoning {
    /** 设置中保存的稳定值；具体模型可以只支持其中一部分。 */
    public static final List<String> VALUES = List.of("default", "none", "minimal", "low", "medium", "high", "xhigh", "max");

    private AiReasoning() { }

    /**
     * 将显式档位写入请求；未指定档位时保持请求不变。
     *
     * <p>OpenAI 兼容协议使用 <code>reasoning_effort</code>，Anthropic 协议使用
     * <code>output_config.effort</code>。这些参数不保证所有模型都支持；服务端仍负责校验
     * 模型能力。GLM-5.3 的已知档位限制在发送前校验，其 Anthropic 兼容接口尚无已确认映射。
     *
     * @param body 待发送的请求对象
     * @param anthropic 是否使用 Anthropic Messages 协议
     * @param model 用户配置的模型名称
     * @param effort 设置中的稳定档位值
     * @throws IllegalArgumentException 档位无效或不符合已知协议限制
     */
    public static void apply(JsonObject body, boolean anthropic, String model, String effort) {
        if ("default".equals(effort)) return;
        if (!VALUES.contains(effort)) throw new IllegalArgumentException("viscript_lib.ai.error.reasoning_effort");
        String name = model.toLowerCase(Locale.ROOT);
        if (name.equals("glm-5.3") || name.startsWith("glm-5.3-")) {
            if (anthropic) throw new IllegalArgumentException("viscript_lib.ai.error.reasoning_glm_protocol");
            if (!List.of("low", "high", "max").contains(effort))
                throw new IllegalArgumentException("viscript_lib.ai.error.reasoning_glm_level");
        }
        if (anthropic) {
            if (!List.of("low", "medium", "high", "xhigh", "max").contains(effort))
                throw new IllegalArgumentException("viscript_lib.ai.error.reasoning_anthropic_level");
            var output = body.has("output_config") ? body.getAsJsonObject("output_config") : new JsonObject();
            output.addProperty("effort", effort);
            body.add("output_config", output);
        } else {
            body.addProperty("reasoning_effort", effort);
        }
    }
}
