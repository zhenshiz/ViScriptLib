package com.viscript_recipe.client;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;

public final class RecipeDeltaClientEvents {
    private static boolean registered;

    private RecipeDeltaClientEvents() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        MinecraftForge.EVENT_BUS.addListener(
                EventPriority.HIGHEST,
                RecipeDeltaClientState::onRecipesUpdated
        );
        MinecraftForge.EVENT_BUS.addListener(
                RecipeDeltaClientState::onClientLogout
        );
        registered = true;
    }
}
