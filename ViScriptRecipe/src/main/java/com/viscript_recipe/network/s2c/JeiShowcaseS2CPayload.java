package com.viscript_recipe.network.s2c;

import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacket;
import com.lowdragmc.lowdraglib2.syncdata.rpc.RPCSender;
import com.viscript_recipe.ViScriptRecipe;
import com.viscript_recipe.compat.jei.JeiShowcaseModeState;
import net.minecraft.client.Minecraft;

public final class JeiShowcaseS2CPayload {
    public static final String SYNC_SHOWCASE_MODE = ViScriptRecipe.MOD_ID + ":sync_jei_showcase_mode";

    private JeiShowcaseS2CPayload() {
    }

    @RPCPacket(value = SYNC_SHOWCASE_MODE, modId = ViScriptRecipe.MOD_ID)
    public static void syncShowcaseMode(RPCSender sender, boolean showcaseOnly) {
        Minecraft.getInstance().execute(() -> JeiShowcaseModeState.updateFromServer(showcaseOnly));
    }
}
