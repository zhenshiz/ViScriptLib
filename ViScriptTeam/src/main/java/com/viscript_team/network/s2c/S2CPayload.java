package com.viscript_team.network.s2c;

import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacket;
import com.lowdragmc.lowdraglib2.syncdata.rpc.RPCSender;
import com.viscript_team.ViScriptTeam;
import com.viscript_team.client.ClientFactionNameTagCache;
import com.viscript_team.client.FactionEditorClientBridge;
import com.viscript_team.client.PartyClientBridge;
import lombok.experimental.UtilityClass;
import net.minecraft.nbt.CompoundTag;

@UtilityClass
public class S2CPayload {
    public static final String MOD_ID = ViScriptTeam.MOD_ID + ":";
    public static final String SYNC_FACTION_NAME_TAGS = MOD_ID + "sync_faction_name_tags";
    public static final String OPEN_PARTY_SCREEN = MOD_ID + "open_party_screen";
    public static final String SYNC_PARTY_SCREEN = MOD_ID + "sync_party_screen";
    public static final String OPEN_FACTION_EDITOR = MOD_ID + "open_faction_editor";
    public static final String SYNC_FACTION_EDITOR = MOD_ID + "sync_faction_editor";

    @RPCPacket(SYNC_FACTION_NAME_TAGS)
    public static void syncFactionNameTags(RPCSender sender, CompoundTag tag) {
        if (sender.isServer()) {
            ClientFactionNameTagCache.apply(tag);
        }
    }

    @RPCPacket(OPEN_PARTY_SCREEN)
    public static void openPartyScreen(RPCSender sender, CompoundTag snapshot) {
        if (sender.isServer()) {
            PartyClientBridge.open(snapshot);
        }
    }

    @RPCPacket(SYNC_PARTY_SCREEN)
    public static void syncPartyScreen(RPCSender sender, CompoundTag snapshot) {
        if (sender.isServer()) {
            PartyClientBridge.sync(snapshot);
        }
    }

    @RPCPacket(OPEN_FACTION_EDITOR)
    public static void openFactionEditor(RPCSender sender, CompoundTag snapshot) {
        if (sender.isServer()) {
            FactionEditorClientBridge.open(snapshot);
        }
    }

    @RPCPacket(SYNC_FACTION_EDITOR)
    public static void syncFactionEditor(RPCSender sender, CompoundTag snapshot) {
        if (sender.isServer()) {
            FactionEditorClientBridge.sync(snapshot);
        }
    }
}
