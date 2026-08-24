package com.viscript_recipe.network;

import com.lowdragmc.lowdraglib2.Platform;
import com.lowdragmc.lowdraglib2.utils.ByteBufUtil;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import io.netty.buffer.Unpooled;
import net.minecraft.nbt.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

import java.nio.ByteBuffer;
import java.util.*;

/**
 * Compact recipe state transfer used by the fast reload path.
 *
 * <p>The RPC layer transports this object as a {@link CompoundTag}; recipes still use their
 * registered vanilla codecs, so optional recipe serializers keep their normal network contract.
 */
public record RecipeDeltaSnapshot(
        long baseRevision,
        long revision,
        boolean baseline,
        boolean showcaseOnly,
        boolean arcaneAnvilChanged,
        List<ResourceLocation> removedRecipeIds,
        List<Recipe<?>> upsertedRecipes,
        Map<ResourceLocation, ResourceLocation> managedEditorTypes,
        Map<ResourceLocation, ResourceLocation> recipeTypeHints
        //List<IronArcaneAnvilOverrideManager.CompiledRecipe> arcaneAnvilRecipes
) {
    public static final int PROTOCOL_VERSION = 1;

    public RecipeDeltaSnapshot {
        removedRecipeIds = List.copyOf(removedRecipeIds);
        upsertedRecipes = List.copyOf(upsertedRecipes);
        managedEditorTypes = Map.copyOf(managedEditorTypes);
        recipeTypeHints = Map.copyOf(recipeTypeHints);
//        arcaneAnvilRecipes = List.copyOf(arcaneAnvilRecipes);
    }

    public int changedRecipeCount() {
        return removedRecipeIds.size() + upsertedRecipes.size();
    }

    public boolean hasChanges() {
        return changedRecipeCount() > 0 || arcaneAnvilChanged;
    }

    public CompoundTag serialize() {
        var root = new CompoundTag();
        root.putInt("protocol", PROTOCOL_VERSION);
        root.putLong("base_revision", baseRevision);
        root.putLong("revision", revision);
        root.putBoolean("baseline", baseline);
        root.putBoolean("showcase_only", showcaseOnly);
        root.putBoolean("arcane_anvil_changed", arcaneAnvilChanged);

        var removed = new ListTag();
        removedRecipeIds.forEach(id -> removed.add(StringTag.valueOf(id.toString())));
        root.put("removed", removed);

        var upserted = new ListTag();
        for (var holder : upsertedRecipes) {
            var entry = new CompoundTag();
            entry.putString("id", holder.getId().toString());
            entry.put("recipe", encode(RECIPE_CODEC, holder, "recipe " + holder.getId()));
            upserted.add(entry);
        }
        root.put("upserted", upserted);
        root.put("managed_editor_types", encodeResourceLocationMap(managedEditorTypes, "editor_type"));
        root.put("recipe_type_hints", encodeResourceLocationMap(recipeTypeHints, "recipe_type"));

        var arcaneAnvil = new ListTag();
/*        for (var recipe : arcaneAnvilRecipes) {
            var entry = new CompoundTag();
            entry.putString("id", recipe.getId().toString());
            entry.put("input", encode(Ingredient.CODEC, recipe, "arcane anvil input " + recipe.getId()));
            entry.put("material", encode(Ingredient.CODEC, recipe.material(), "arcane anvil material " + recipe.id()));
            entry.put("result", encode(ItemStack.CODEC, recipe.result(), "arcane anvil result " + recipe.id()));
            arcaneAnvil.add(entry);
        }*/
        root.put("arcane_anvil", arcaneAnvil);
        return root;
    }

    public static RecipeDeltaSnapshot deserialize(CompoundTag root) {
        var protocol = root.getInt("protocol");
        if (protocol != PROTOCOL_VERSION) {
            throw new IllegalArgumentException("Unsupported recipe delta protocol " + protocol);
        }

        var removed = new ArrayList<ResourceLocation>();
        for (var tag : root.getList("removed", Tag.TAG_STRING)) {
            removed.add(new ResourceLocation(tag.getAsString()));
        }

        var upserted = new ArrayList<Recipe<?>>();
        for (var tag : root.getList("upserted", Tag.TAG_COMPOUND)) {
            var entry = (CompoundTag) tag;
            var id = new ResourceLocation(entry.getString("id"));
            var recipe = decode(RECIPE_CODEC, entry.get("recipe"), "recipe " + id);
            upserted.add(recipe);
        }

/*        var arcaneAnvil = new ArrayList<IronArcaneAnvilOverrideManager.CompiledRecipe>();
        for (var tag : root.getList("arcane_anvil", Tag.TAG_COMPOUND)) {
            var entry = (CompoundTag) tag;
            var id = ResourceLocation.parse(entry.getString("id"));
            var input = decode(Ingredient.CODEC, entry.get("input"), "arcane anvil input " + id);
            var material = decode(Ingredient.CODEC, entry.get("material"), "arcane anvil material " + id);
            var result = decode(ItemStack.CODEC, entry.get("result"), "arcane anvil result " + id);
            arcaneAnvil.add(new IronArcaneAnvilOverrideManager.CompiledRecipe(id, input, material, result));
        }*/

        return new RecipeDeltaSnapshot(
                root.getLong("base_revision"),
                root.getLong("revision"),
                root.getBoolean("baseline"),
                root.getBoolean("showcase_only"),
                root.getBoolean("arcane_anvil_changed"),
                removed,
                upserted,
                decodeResourceLocationMap(root.getList("managed_editor_types", Tag.TAG_COMPOUND), "editor_type"),
                decodeResourceLocationMap(root.getList("recipe_type_hints", Tag.TAG_COMPOUND), "recipe_type")
        );
    }

    public static Tag encodeRecipe(Recipe<?> holder) {
        return encode(RECIPE_CODEC, holder, "recipe " + holder.getId());
    }

/*    public static Tag encodeArcaneAnvilRecipes(
            List<IronArcaneAnvilOverrideManager.CompiledRecipe> recipes
    ) {
        var encoded = new ListTag();
        for (var recipe : recipes) {
            var entry = new CompoundTag();
            entry.putString("id", recipe.id().toString());
            entry.put("input", encode(Ingredient.CODEC, recipe.input(), "arcane anvil input " + recipe.id()));
            entry.put("material", encode(Ingredient.CODEC, recipe.material(), "arcane anvil material " + recipe.id()));
            entry.put("result", encode(ItemStack.CODEC, recipe.result(), "arcane anvil result " + recipe.id()));
            encoded.add(entry);
        }
        return encoded;
    }*/

    private static ListTag encodeResourceLocationMap(
            Map<ResourceLocation, ResourceLocation> values,
            String valueKey
    ) {
        var encoded = new ListTag();
        values.forEach((id, value) -> {
            var entry = new CompoundTag();
            entry.putString("id", id.toString());
            entry.putString(valueKey, value.toString());
            encoded.add(entry);
        });
        return encoded;
    }

    private static Map<ResourceLocation, ResourceLocation> decodeResourceLocationMap(
            ListTag encoded,
            String valueKey
    ) {
        var values = new LinkedHashMap<ResourceLocation, ResourceLocation>();
        for (var tag : encoded) {
            var entry = (CompoundTag) tag;
            values.put(
                    new ResourceLocation(entry.getString("id")),
                    new ResourceLocation(entry.getString(valueKey))
            );
        }
        return values;
    }

    private static <T> Tag encode(
            Codec<T> codec,
            T value,
            String description
    ) {
        return codec.encodeStart(NbtOps.INSTANCE, value)
                .getOrThrow(false, message -> new IllegalArgumentException("Failed to encode " + description + ": " + message));
    }

    private static <T> T decode(
            Codec<T> codec,
            Tag tag,
            String description
    ) {
        if (tag == null) {
            throw new IllegalArgumentException("Missing encoded " + description);
        }
        return codec.parse(NbtOps.INSTANCE, tag)
                .getOrThrow(false, message -> new IllegalArgumentException("Failed to decode " + description + ": " + message));
    }

    public static final Codec<Recipe<?>> RECIPE_CODEC = new Codec<>() {
        @Override
        public <T> DataResult<T> encode(Recipe<?> input, DynamicOps<T> ops, T prefix) {
            var data = ByteBufUtil.writeCustomData(buf ->
                    ClientboundUpdateRecipesPacket.toNetwork(buf, input), Platform.getFrozenRegistry());
            T encoded = ops.createByteList(ByteBuffer.wrap(data));
            return DataResult.success(encoded);
        }

        @Override
        public <T> DataResult<Pair<Recipe<?>, T>> decode(DynamicOps<T> ops, T input) {
            DataResult<ByteBuffer> result = ops.getByteBuffer(input);
            Optional<ByteBuffer> left = result.get().left();
            if (left.isPresent()) {
                var buffer = left.get();
                byte[] data = DataFixUtils.toArray(buffer);
                FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(data));
                var recipe = ClientboundUpdateRecipesPacket.fromNetwork(buf);
                buf.release();
                return DataResult.success(Pair.of(recipe, input));
            }
            return DataResult.error(() -> "Missing recipe data");
        }
    };
}
