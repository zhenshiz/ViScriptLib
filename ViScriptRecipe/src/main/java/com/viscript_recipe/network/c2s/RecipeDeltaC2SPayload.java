package com.viscript_recipe.network.c2s;

import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacket;
import com.lowdragmc.lowdraglib2.syncdata.rpc.RPCSender;
import com.viscript_recipe.ViScriptRecipe;
import com.viscript_recipe.recipe.RecipeReloadSyncService;

public final class RecipeDeltaC2SPayload {
    public static final String REQUEST_FULL_RECIPE_SYNC = ViScriptRecipe.MOD_ID + ":request_full_recipe_sync";

    private RecipeDeltaC2SPayload() {
    }

    @RPCPacket(value = REQUEST_FULL_RECIPE_SYNC, modId = ViScriptRecipe.MOD_ID)
    public static void requestFullRecipeSync(RPCSender sender) {
        var player = sender.asPlayer();
        if (player == null) {
            return;
        }
        var server = player.getServer();
        if (server != null) {
            server.execute(() -> RecipeReloadSyncService.syncFullToPlayer(player, false));
        }
    }
}
