package com.viscript_recipe.recipe;

import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacketDistributor;
import com.viscript_recipe.Config;
import com.viscript_recipe.network.RecipeDeltaSnapshot;
import com.viscript_recipe.network.s2c.JeiShowcaseS2CPayload;
import com.viscript_recipe.network.s2c.RecipeDeltaS2CPayload;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateTagsPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagNetworkSerialization;

/** Coordinates full and incremental recipe synchronization without command-layer duplication. */
public final class RecipeReloadSyncService {
    private RecipeReloadSyncService() {
    }

    public static void syncDeltaToPlayers(MinecraftServer server, RecipeDeltaSnapshot delta) {
        var players = server.getPlayerList().getPlayers();
        if (players.isEmpty()) {
            return;
        }
        var payload = delta.serialize();
        for (var player : players) {
            RPCPacketDistributor.rpcToPlayer(player, RecipeDeltaS2CPayload.APPLY_RECIPE_DELTA, payload.copy());
        }
    }

    public static void syncFullToPlayers(MinecraftServer server, boolean syncTags) {
        var players = server.getPlayerList().getPlayers();
        if (players.isEmpty()) {
            return;
        }

        var recipesPacket = new ClientboundUpdateRecipesPacket(server.getRecipeManager().getRecipes());
        var tagsPacket = syncTags
                ? new ClientboundUpdateTagsPacket(TagNetworkSerialization.serializeTagsToNetwork(server.registries()))
                : null;
        var baseline = RecipeOverrideManager.createBaseline(server.getRecipeManager()).serialize();
        for (var player : players) {
            syncFullToPlayer(player, recipesPacket, tagsPacket, baseline);
        }
    }

    public static void syncFullToPlayer(ServerPlayer player, boolean syncTags) {
        var server = player.getServer();
        if (server == null) {
            return;
        }
        var recipesPacket = new ClientboundUpdateRecipesPacket(server.getRecipeManager().getRecipes());
        var tagsPacket = syncTags
                ? new ClientboundUpdateTagsPacket(TagNetworkSerialization.serializeTagsToNetwork(server.registries()))
                : null;
        var baseline = RecipeOverrideManager.createBaseline(server.getRecipeManager()).serialize();
        syncFullToPlayer(player, recipesPacket, tagsPacket, baseline);
    }

    public static void syncBaselineToPlayer(ServerPlayer player) {
        var server = player.getServer();
        if (server == null) {
            return;
        }
        var baseline = RecipeOverrideManager.createBaseline(server.getRecipeManager()).serialize();
        RPCPacketDistributor.rpcToPlayer(player, RecipeDeltaS2CPayload.SYNC_RECIPE_BASELINE, baseline);
    }

    private static void syncFullToPlayer(
            ServerPlayer player,
            ClientboundUpdateRecipesPacket recipesPacket,
            ClientboundUpdateTagsPacket tagsPacket,
            net.minecraft.nbt.CompoundTag baseline
    ) {
        player.connection.send(recipesPacket);
        if (tagsPacket != null) {
            player.connection.send(tagsPacket);
        }
        player.getRecipeBook().sendInitialRecipeBook(player);
        RPCPacketDistributor.rpcToPlayer(
                player,
                JeiShowcaseS2CPayload.SYNC_SHOWCASE_MODE,
                Config.SHOWCASE_ONLY_VISCRIPT_RECIPES.get()
        );
        RPCPacketDistributor.rpcToPlayer(player, RecipeDeltaS2CPayload.SYNC_RECIPE_BASELINE, baseline.copy());
    }
}
