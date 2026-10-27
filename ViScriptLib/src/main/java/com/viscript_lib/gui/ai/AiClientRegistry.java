package com.viscript_lib.gui.ai;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.Comparator;
import java.util.Locale;

/** 查询客户端已加载的注册表及同步数据；不访问其他玩家或服务端私有状态。 */
public final class AiClientRegistry {
    private AiClientRegistry() { }

    /**
     * 在客户端线程按 ID 或本地化名称查询一页注册表条目。
     *
     * <p>请求的页大小必须是正整数；超过当前配置时使用配置上限，并在结果中返回有效页大小。
     * 后续查询应使用返回的 {@code next_offset}，不能按原请求的页大小递增偏移。
     *
     * @param arguments 含注册表名称及可选查询类型、搜索词、偏移、页大小的参数
     * @param pageLimit 当前设置允许的最大页大小，范围为 1 至 128
     * @return 条目或标签及下一页偏移；条目的标签最多 64 项，标签成员样例最多 16 项
     * @throws IllegalArgumentException 注册表不可用或参数无效
     */
    public static JsonObject query(JsonObject arguments, int pageLimit) {
        AiJson.keys(arguments, "registry", "kind", "query", "offset", "limit");
        String name = AiJson.string(arguments, "registry", 128);
        var id = ResourceLocation.tryParse(name.contains(":") ? name : "minecraft:" + name);
        if (id == null) throw new IllegalArgumentException("Invalid registry ID");
        Registry<?> registry = BuiltInRegistries.REGISTRY.get(id);
        var level = Minecraft.getInstance().level;
        if (registry == null && level != null) {
            registry = level.registryAccess().registries().filter(entry -> entry.key().location().equals(id))
                    .map(entry -> (Registry<?>) entry.value()).findFirst().orElse(null);
        }
        if (registry == null) throw new IllegalArgumentException("Registry unavailable on this client: " + id);
        int offset = AiJson.integer(arguments, "offset", 0, 0, 1_000_000);
        int limit = Math.min(AiJson.integer(arguments, "limit", pageLimit, 1, Integer.MAX_VALUE), pageLimit);
        String query = arguments.has("query") ? AiJson.string(arguments, "query", 128).toLowerCase(Locale.ROOT) : "";
        String kind = arguments.has("kind") ? AiJson.string(arguments, "kind", 16) : "entries";
        if (kind.equals("tags")) return queryTags(registry, id, query, offset, limit);
        if (!kind.equals("entries")) throw new IllegalArgumentException("Unknown registry query kind: " + kind);
        var data = new JsonArray();
        int matched = 0;
        for (var key : registry.keySet().stream().sorted(Comparator.comparing(ResourceLocation::toString)).toList()) {
            Object value = registry.get(key);
            String display = value instanceof Item item ? item.getDescription().getString()
                    : value instanceof Block block ? block.getName().getString() : key.toString();
            if (query.startsWith("#")) {
                String tag = query.substring(1);
                if (registry.getHolder(key).stream().flatMap(holder -> holder.tags())
                        .noneMatch(candidate -> candidate.location().toString().equals(tag))) continue;
            } else if (!key.toString().contains(query) && !display.toLowerCase(Locale.ROOT).contains(query)) continue;
            if (matched++ < offset) continue;
            if (data.size() == limit) break;
            var entry = new JsonObject();
            entry.addProperty("id", key.toString());
            entry.addProperty("name", display);
            var tags = new JsonArray();
            registry.getHolder(key).ifPresent(holder -> holder.tags().limit(64)
                    .forEach(tag -> tags.add(tag.location().toString())));
            entry.add("tags", tags);
            data.add(entry);
        }
        var result = new JsonObject();
        result.addProperty("registry", id.toString());
        result.addProperty("limit", limit);
        result.add("entries", data);
        result.addProperty("has_more", matched > offset + limit);
        result.addProperty("next_offset", offset + data.size());
        return result;
    }
    private static <T> JsonObject queryTags(Registry<T> registry, ResourceLocation id, String query, int offset, int limit) {
        var data = new JsonArray();
        String search = query.startsWith("#") ? query.substring(1) : query;
        int matched = 0;
        for (var tag : registry.getTagNames().sorted(Comparator.comparing(key -> key.location().toString())).toList()) {
            if (!tag.location().toString().contains(search)) continue;
            if (matched++ < offset) continue;
            if (data.size() == limit) break;
            var entry = new JsonObject();
            entry.addProperty("id", tag.location().toString());
            var members = registry.getTag(tag).orElseThrow();
            entry.addProperty("count", members.size());
            var sample = new JsonArray();
            members.stream().limit(16).forEach(holder -> holder.unwrapKey().ifPresent(key -> sample.add(key.location().toString())));
            entry.add("sample", sample);
            entry.addProperty("members_truncated", members.size() > 16);
            data.add(entry);
        }
        var result = new JsonObject();
        result.addProperty("registry", id.toString());
        result.addProperty("kind", "tags");
        result.addProperty("limit", limit);
        result.add("entries", data);
        result.addProperty("has_more", matched > offset + limit);
        result.addProperty("next_offset", offset + data.size());
        return result;
    }

}
