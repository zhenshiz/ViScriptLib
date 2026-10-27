package com.viscript_lib.ai;

import com.google.gson.JsonParser;
import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import com.viscript_lib.ViScriptLib;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * 按页提供当前服务端的注册表条目、标签和标签成员。
 *
 * <p>内置注册表与当前世界的数据包注册表均可查询。未提供命名空间的类型名使用
 * {@code minecraft} 命名空间，{@code dimension} 是 {@code minecraft:dimension} 的别名。
 * 资源和标签 ID 必须包含命名空间；游标绑定查询条件和数据版本。
 */
@LDLRegister(name = "registry", registry = AiKnowledgeProvider.REGISTRY_ID)
public final class RegistryKnowledgeProvider implements AiKnowledgeProvider {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ViScriptLib.MOD_ID, "registry");
    private static final int SCHEMA_VERSION = 1;

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public AiProviderDescriptor descriptor() {
        var schema = JsonParser.parseString("""
                {
                  "type": "object",
                  "properties": {
                    "operation": {"type":"string", "enum":["types","list","get","tags","tag"]},
                    "type": {"type":"string", "description":"Registry id, for example item, biome, enchantment or mod:registry"},
                    "id": {"type":"string", "description":"Namespaced entry id for get, or tag id for tag"},
                    "namespace": {"type":"string"},
                    "query": {"type":"string", "description":"Substring of an id, translation key or server-side display name"},
                    "tag": {"type":"string", "description":"Namespaced tag filter for list"},
                    "cursor": {"type":"string", "description":"Opaque next_cursor from the previous result"},
                    "limit": {"type":"integer", "minimum":1, "maximum":128},
                    "include_tags": {"type":"boolean"}
                  },
                  "required": ["operation"],
                  "additionalProperties": false
                }
                """).getAsJsonObject();
        return new AiProviderDescriptor(ID, SCHEMA_VERSION,
                "Read server registries and tags. Use types to discover available registries.",
                List.of("types", "list", "get", "tags", "tag"), schema);
    }

    @Override
    public AiQueryResult query(AiQueryRequest request, AiAccessContext context) {
        if (context == null || !context.allows(AiCapability.REGISTRY_READ)) return failure("Registry access is not granted");
        if (!context.level().getServer().isSameThread()) return failure("Registry queries require the server thread");
        if (request == null) return failure("A query request is required");
        if (request.cursor().length() > 512 || request.query().length() > 256 || request.id().length() > 512
                || request.type().length() > 512 || request.tag().length() > 512
                || request.namespace().length() > 512 || request.operation().length() > 32) return failure("Query input is too long");
        if (request.operation().equals("types")) return types(request, context);
        var registry = registry(request.type(), context);
        if (registry == null) return failure("Unknown registry type");
        return switch (request.operation()) {
            case "list", "get", "tags", "tag" -> queryRegistry(request, registry);
            default -> failure("Unsupported registry operation");
        };
    }

    private static AiQueryResult types(AiQueryRequest request, AiAccessContext context) {
        var keys = Stream.concat(BuiltInRegistries.REGISTRY.keySet().stream(),
                        context.level().registryAccess().registries().map(entry -> entry.key().location()))
                .distinct().filter(id -> matchesId(id, request)).sorted().toList();
        var bounds = bounds(request, keys.size());
        if (bounds == null) return failure("Invalid or stale cursor; restart the query");
        var entries = keys.subList(bounds[0], bounds[1]).stream().map(id -> {
            var data = new CompoundTag();
            data.putString("id", id.toString());
            data.putString("type", id.equals(Registries.BIOME.location()) ? "biome"
                    : id.equals(Registries.STRUCTURE.location()) ? "structure"
                    : id.getNamespace().equals("minecraft") ? id.getPath() : id.toString());
            return data;
        }).toList();
        return page(request, entries, bounds[1], keys.size());
    }

    @Nullable
    private static Registry<?> registry(String type, AiAccessContext context) {
        var id = switch (type) {
            case "biome" -> Registries.BIOME.location();
            case "structure" -> Registries.STRUCTURE.location();
            default -> ResourceLocation.tryParse(type);
        };
        if (id == null || type.isBlank()) return null;
        var dynamic = context.level().registryAccess().registry(ResourceKey.createRegistryKey(id));
        return dynamic.isPresent() ? dynamic.get() : BuiltInRegistries.REGISTRY.get(id);
    }

    private static <T> AiQueryResult queryRegistry(AiQueryRequest request, Registry<T> registry) {
        if (request.operation().equals("get")) {
            var id = namespacedId(request.id());
            if (id == null) return failure("A namespaced entry id is required");
            // 默认注册表的 get 方法会把缺失 ID 映射为空气等默认值，因此使用精确 Holder 查询。
            var holder = registry.getHolder(id);
            return holder.map(value -> page(request, List.of(describe(value, registry, request.includeTags())), 1, 1))
                    .orElseGet(() -> failure("Registry entry was not found"));
        }
        if (request.operation().equals("tags")) {
            var tags = registry.getTags().filter(pair -> matchesId(pair.getFirst().location(), request))
                    .sorted(Comparator.comparing(pair -> pair.getFirst().location())).toList();
            var bounds = bounds(request, tags.size());
            if (bounds == null) return failure("Invalid or stale cursor; restart the query");
            var entries = tags.subList(bounds[0], bounds[1]).stream().map(pair -> {
                var data = new CompoundTag();
                data.putString("id", pair.getFirst().location().toString());
                data.putString("registry", registry.key().location().toString());
                data.putInt("entry_count", pair.getSecond().size());
                return data;
            }).toList();
            return page(request, entries, bounds[1], tags.size());
        }

        Stream<? extends Holder<T>> holders = registry.holders();
        var tagId = request.operation().equals("tag") ? namespacedId(request.id()) : namespacedId(request.tag());
        if (request.operation().equals("tag") || !request.tag().isEmpty()) {
            if (tagId == null) return failure("A namespaced tag id is required");
            var tag = registry.getTag(TagKey.create(registry.key(), tagId));
            if (tag.isEmpty()) return failure("Registry tag was not found");
            holders = tag.get().stream();
        }
        var matches = holders.filter(holder -> holder.unwrapKey().isPresent()).filter(holder -> {
            var id = holder.unwrapKey().orElseThrow().location();
            if (!request.namespace().isEmpty() && !request.namespace().equals(id.getNamespace())) return false;
            var query = request.query();
            if (query.isEmpty() || id.toString().contains(query)) return true;
            var name = name(holder.value(), registry, id);
            return name.getString().toLowerCase(Locale.ROOT).contains(query)
                    || translationKey(holder.value(), registry, id).toLowerCase(Locale.ROOT).contains(query);
        }).sorted(Comparator.comparing(holder -> holder.unwrapKey().orElseThrow().location())).toList();
        var bounds = bounds(request, matches.size());
        if (bounds == null) return failure("Invalid or stale cursor; restart the query");
        var entries = matches.subList(bounds[0], bounds[1]).stream()
                .map(holder -> describe(holder, registry, request.includeTags())).toList();
        return page(request, entries, bounds[1], matches.size());
    }

    private static boolean matchesId(ResourceLocation id, AiQueryRequest request) {
        return (request.namespace().isEmpty() || request.namespace().equals(id.getNamespace()))
                && (request.query().isEmpty() || id.toString().contains(request.query()));
    }

    @Nullable
    private static ResourceLocation namespacedId(String value) {
        return value.indexOf(':') > 0 ? ResourceLocation.tryParse(value) : null;
    }

    private static <T> CompoundTag describe(Holder<T> holder, Registry<T> registry, boolean includeTags) {
        var id = holder.unwrapKey().orElseThrow().location();
        var value = holder.value();
        var data = new CompoundTag();
        data.putString("id", id.toString());
        data.putString("registry", registry.key().location().toString());
        data.putString("namespace", id.getNamespace());
        data.putString("translation_key", translationKey(value, registry, id));
        data.putString("display_name", name(value, registry, id).getString());
        if (value instanceof Item item) {
            var stack = item.getDefaultInstance();
            data.putInt("max_stack_size", stack.getMaxStackSize());
            data.putInt("max_damage", stack.getMaxDamage());
        } else if (value instanceof Block block) {
            data.putString("item_id", BuiltInRegistries.ITEM.getKey(block.asItem()).toString());
        }
        if (includeTags) {
            var tags = new ListTag();
            holder.tags().map(tag -> tag.location().toString()).sorted()
                    .forEach(tag -> tags.add(StringTag.valueOf(tag)));
            data.put("tags", tags);
        }
        return data;
    }

    private static Component name(Object value, Registry<?> registry, ResourceLocation id) {
        return switch (value) {
            case Item item -> item.getDefaultInstance().getHoverName();
            case Block block -> block.getName();
            case Fluid fluid -> fluid.getFluidType().getDescription();
            case EntityType<?> entity -> entity.getDescription();
            case MobEffect effect -> effect.getDisplayName();
            case Enchantment enchantment -> enchantment.description();
            default -> registry.key().equals(Registries.BIOME)
                    ? Component.translatable(Util.makeDescriptionId("biome", id)) : Component.literal(id.toString());
        };
    }

    private static String translationKey(Object value, Registry<?> registry, ResourceLocation id) {
        return switch (value) {
            case Item item -> item.getDescriptionId();
            case Block block -> block.getDescriptionId();
            case Fluid fluid -> fluid.getFluidType().getDescriptionId();
            case EntityType<?> entity -> entity.getDescriptionId();
            case MobEffect effect -> effect.getDescriptionId();
            case Enchantment enchantment -> enchantment.description().getContents() instanceof
                    net.minecraft.network.chat.contents.TranslatableContents contents ? contents.getKey() : "";
            default -> registry.key().equals(Registries.BIOME) ? Util.makeDescriptionId("biome", id) : "";
        };
    }

    @Nullable
    private static int[] bounds(AiQueryRequest request, int size) {
        int start = 0;
        if (!request.cursor().isEmpty()) {
            try {
                var decoded = new String(Base64.getUrlDecoder().decode(request.cursor()), StandardCharsets.UTF_8).split("\\n", -1);
                if (decoded.length != 3 || !decoded[0].equals(AiEnvironment.revision())
                        || !decoded[1].equals(signature(request))) return null;
                start = Integer.parseInt(decoded[2]);
                if (start < 0 || start >= size) return null;
            } catch (IllegalArgumentException exception) {
                return null;
            }
        }
        return new int[]{start, start + Math.min(request.limit(), size - start)};
    }

    private static String signature(AiQueryRequest request) {
        var signature = String.join("\u0000", request.operation(), request.type(), request.id(),
                request.namespace(), request.query(), request.tag());
        return UUID.nameUUIDFromBytes(signature.getBytes(StandardCharsets.UTF_8)).toString();
    }

    private static AiQueryResult page(AiQueryRequest request, List<CompoundTag> entries, int end, int size) {
        var cursor = end >= size ? "" : Base64.getUrlEncoder().withoutPadding().encodeToString(
                (AiEnvironment.revision() + "\n" + signature(request) + "\n" + end).getBytes(StandardCharsets.UTF_8));
        return AiQueryResult.success(ID, SCHEMA_VERSION, entries, cursor);
    }

    private static AiQueryResult failure(String error) {
        return AiQueryResult.failure(ID, SCHEMA_VERSION, error);
    }
}
