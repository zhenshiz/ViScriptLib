package com.viscript_recipe.network.s2c;

import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacket;
import com.lowdragmc.lowdraglib2.syncdata.rpc.RPCSender;
import com.viscript_recipe.ViScriptRecipe;
import com.viscript_recipe.client.RecipeDeltaClientState;
import net.minecraft.nbt.CompoundTag;

public final class RecipeDeltaS2CPayload {
    public static final String APPLY_RECIPE_DELTA = ViScriptRecipe.MOD_ID + ":apply_recipe_delta";
    public static final String SYNC_RECIPE_BASELINE = ViScriptRecipe.MOD_ID + ":sync_recipe_delta_baseline";

    private RecipeDeltaS2CPayload() {
    }

    @RPCPacket(value = APPLY_RECIPE_DELTA, modId = ViScriptRecipe.MOD_ID)
    public static void applyRecipeDelta(RPCSender sender, CompoundTag payload) {
        RecipeDeltaClientState.apply(payload, false);
    }

    @RPCPacket(value = SYNC_RECIPE_BASELINE, modId = ViScriptRecipe.MOD_ID)
    public static void syncRecipeBaseline(RPCSender sender, CompoundTag payload) {
        RecipeDeltaClientState.apply(payload, true);
    }
}
