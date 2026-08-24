package com.viscript_recipe.recipe;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;

/** Supplies the recipe-delta baseline after vanilla finishes a player's normal login sync. */
public final class RecipeDeltaServerEvents {
    private static boolean registered;

    private RecipeDeltaServerEvents() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        MinecraftForge.EVENT_BUS.addListener(
                RecipeDeltaServerEvents::onPlayerLoggedIn
        );
        registered = true;
    }

    private static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            RecipeReloadSyncService.syncBaselineToPlayer(player);
        }
    }
}
