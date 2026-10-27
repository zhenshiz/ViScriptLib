package com.viscript_lib.gui.editor.dev;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.lowdragmc.lowdraglib2.registry.RegistrationEnvironment;
import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegisterClient;
import com.lowdragmc.lowdraglib2.uitest.ScenarioBuilder;
import com.lowdragmc.lowdraglib2.uitest.ScenarioOptions;
import com.lowdragmc.lowdraglib2.uitest.UIScenario;
import com.viscript_lib.ViScriptLib;
import com.viscript_lib.gui.ai.AiClientRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.ItemTags;

import java.util.ArrayList;
import java.util.List;

/** 验证真实客户端注册表、标签及标签成员的分页裁剪与连续偏移。 */
@LDLRegisterClient(name = "vsl_ai_registry_query", group = ViScriptLib.MOD_ID,
        registry = UIScenario.REGISTRY, environment = RegistrationEnvironment.DEV_ONLY)
public final class AiRegistryQueryScenario implements UIScenario {
    @Override
    public void configure(ScenarioOptions options) {
        options.requiresWorld(true).defaultTimeoutMs(15000).scenarioTimeoutMs(60000);
    }

    @Override
    public void define(ScenarioBuilder s) {
        s.waitUntil("世界注册表标签已同步", ctx -> ctx.mc().level != null
                        && BuiltInRegistries.ITEM.getTag(ItemTags.LOGS).isPresent())
                .step("超出配置的查询页大小自动裁剪", ctx -> {
                    var args = arguments("entries", "minecraft:", 256);
                    var first = AiClientRegistry.query(args, 64);
                    ctx.check("请求 256 项时实际返回最多 64 项", first.get("limit").getAsInt() == 64
                            && first.getAsJsonArray("entries").size() == 64
                            && first.get("next_offset").getAsInt() == 64 && first.get("has_more").getAsBoolean());
                    args.addProperty("offset", first.get("next_offset").getAsInt());
                    var second = AiClientRegistry.query(args, 64);
                    var expected = BuiltInRegistries.ITEM.keySet().stream().map(Object::toString)
                            .filter(id -> id.startsWith("minecraft:")).sorted().toList();
                    ctx.check("裁剪后的下一页不跳过条目", ids(first.getAsJsonArray("entries")).equals(expected.subList(0, 64))
                            && ids(second.getAsJsonArray("entries")).equals(expected.subList(64, 128))
                            && second.get("next_offset").getAsInt() == 128);
                    args.remove("offset");
                    args.remove("limit");
                    ctx.check("省略页大小时使用配置上限", AiClientRegistry.query(args, 64).get("limit").getAsInt() == 64);
                    args.addProperty("limit", 2);
                    var small = AiClientRegistry.query(args, 64);
                    ctx.check("较小的合法页大小保持原值", small.get("limit").getAsInt() == 2
                            && small.getAsJsonArray("entries").size() == 2 && small.get("next_offset").getAsInt() == 2);
                })
                .step("完整遍历普通物品查询", ctx -> {
                    var expected = BuiltInRegistries.ITEM.keySet().stream().map(Object::toString)
                            .filter(id -> id.contains("minecraft:oak")).sorted().toList();
                    ctx.check("普通条目分页不漏项不重复且正确结束", pages(arguments("entries", "minecraft:oak", 128), 3, expected));
                })
                .step("完整遍历标签名称查询", ctx -> {
                    var expected = BuiltInRegistries.ITEM.getTagNames().map(tag -> tag.location().toString())
                            .filter(id -> id.contains("minecraft:")).sorted().toList();
                    ctx.check("标签列表分页不漏项不重复且正确结束", pages(arguments("tags", "minecraft:", 128), 7, expected));
                })
                .step("完整遍历原木标签成员", ctx -> {
                    var expected = BuiltInRegistries.ITEM.getTag(ItemTags.LOGS).orElseThrow().stream()
                            .map(holder -> BuiltInRegistries.ITEM.getKey(holder.value()).toString()).sorted().toList();
                    ctx.check("标签成员夹具包含多页", expected.size() > 7);
                    ctx.check("标签成员分页不漏项不重复且正确结束", pages(arguments("entries", "#minecraft:logs", 128), 7, expected));
                })
                .step("无效页大小仍然拒绝", ctx -> {
                    boolean rejected = true;
                    for (String limit : List.of("0", "-1", "1.5", "\"100\"")) {
                        var args = arguments("entries", "minecraft:", 1);
                        args.add("limit", com.google.gson.JsonParser.parseString(limit));
                        try {
                            AiClientRegistry.query(args, 64);
                            rejected = false;
                        } catch (IllegalArgumentException expected) { }
                    }
                    ctx.check("仅接受正整数页大小", rejected);
                });
    }

    private static JsonObject arguments(String kind, String query, int limit) {
        var args = new JsonObject();
        args.addProperty("registry", "item");
        args.addProperty("kind", kind);
        args.addProperty("query", query);
        args.addProperty("limit", limit);
        return args;
    }

    private static boolean pages(JsonObject args, int pageLimit, List<String> expected) {
        var collected = new ArrayList<String>();
        int offset = 0;
        for (int page = 0; page <= expected.size() / pageLimit + 1; page++) {
            args.addProperty("offset", offset);
            var response = AiClientRegistry.query(args, pageLimit);
            var entries = response.getAsJsonArray("entries");
            int next = response.get("next_offset").getAsInt();
            if (response.get("limit").getAsInt() != pageLimit || entries.size() > pageLimit
                    || next != offset + entries.size()) return false;
            collected.addAll(ids(entries));
            boolean more = response.get("has_more").getAsBoolean();
            if (more != (next < expected.size())) return false;
            if (!more) {
                args.addProperty("offset", next);
                var tail = AiClientRegistry.query(args, pageLimit);
                return collected.equals(expected) && tail.getAsJsonArray("entries").isEmpty()
                        && !tail.get("has_more").getAsBoolean() && tail.get("next_offset").getAsInt() == next;
            }
            if (next <= offset) return false;
            offset = next;
        }
        return false;
    }

    private static List<String> ids(JsonArray entries) {
        var ids = new ArrayList<String>();
        for (var entry : entries) ids.add(entry.getAsJsonObject().get("id").getAsString());
        return ids;
    }
}
