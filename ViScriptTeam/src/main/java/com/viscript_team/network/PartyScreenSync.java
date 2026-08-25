package com.viscript_team.network;

import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacketDistributor;
import com.mojang.authlib.GameProfile;
import com.viscript_team.data.faction.FactionSavedData;
import com.viscript_team.data.party.Party;
import com.viscript_team.network.s2c.S2CPayload;
import com.viscript_team.util.PartyChatService;
import com.viscript_team.util.PartyPlayerService;
import lombok.experimental.UtilityClass;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.TreeSet;
import java.util.UUID;

@UtilityClass
public class PartyScreenSync {
    public static void open(ServerPlayer player) {
        RPCPacketDistributor.rpcToPlayer(player, S2CPayload.OPEN_PARTY_SCREEN, createSnapshot(player, null));
    }

    public static void refresh(ServerPlayer player, @Nullable PartyPlayerService.Result result) {
        RPCPacketDistributor.rpcToPlayer(player, S2CPayload.SYNC_PARTY_SCREEN, createSnapshot(player, result));
    }

    public static void refreshAll(MinecraftServer server, ServerPlayer actor, PartyPlayerService.Result result) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            refresh(player, player.getUUID().equals(actor.getUUID()) ? result : null);
        }
    }

    public static void refreshParty(MinecraftServer server, String partyId, ServerPlayer actor, PartyPlayerService.Result result) {
        FactionSavedData data = FactionSavedData.get(server.overworld());
        Party party = data.getParty(partyId).orElse(null);
        if (party == null) {
            refresh(actor, result);
            return;
        }
        for (UUID memberId : party.getMembers()) {
            ServerPlayer member = server.getPlayerList().getPlayer(memberId);
            if (member != null) {
                refresh(member, memberId.equals(actor.getUUID()) ? result : null);
            }
        }
    }

    private static CompoundTag createSnapshot(ServerPlayer viewer, @Nullable PartyPlayerService.Result result) {
        FactionSavedData data = FactionSavedData.get(viewer.serverLevel());
        CompoundTag snapshot = new CompoundTag();
        snapshot.putUUID("viewerId", viewer.getUUID());
        snapshot.putString("viewerName", viewer.getGameProfile().getName());
        if (result != null) {
            snapshot.putString("noticeKey", result.messageKey());
            snapshot.putBoolean("noticeSuccess", result.success());
        }

        Party ownParty = data.getPlayerParty(viewer.getUUID()).orElse(null);
        snapshot.putBoolean("hasParty", ownParty != null);
        if (ownParty == null) {
            snapshot.put("availableParties", createAvailableParties(viewer, data));
            snapshot.put("invitations", createInvitations(viewer, data));
            return snapshot;
        }

        CompoundTag partyTag = new CompoundTag();
        partyTag.putString("id", ownParty.getId());
        partyTag.putString("name", ownParty.getName());
        partyTag.putUUID("leaderId", ownParty.getLeaderId());
        partyTag.putBoolean("isLeader", ownParty.isLeader(viewer.getUUID()));
        partyTag.putBoolean("friendlyFire", ownParty.allowsFriendlyFire());
        partyTag.put("members", createMembers(viewer.server, ownParty));
        partyTag.put("messages", createMessages(viewer.server, ownParty));
        if (ownParty.isLeader(viewer.getUUID())) {
            partyTag.put("joinRequests", createJoinRequests(viewer.server, data, ownParty));
            partyTag.put("invitablePlayers", createInvitablePlayers(viewer, data, ownParty));
        }
        snapshot.put("party", partyTag);
        return snapshot;
    }

    private static ListTag createAvailableParties(ServerPlayer viewer, FactionSavedData data) {
        ListTag entries = new ListTag();
        for (String partyId : new TreeSet<>(data.getPartyIds())) {
            Party party = data.getParty(partyId).orElse(null);
            if (party == null) {
                continue;
            }
            CompoundTag entry = new CompoundTag();
            entry.putString("id", party.getId());
            entry.putString("name", party.getName());
            entry.putString("leaderName", playerName(viewer.server, party.getLeaderId()));
            entry.putInt("memberCount", party.getMembers().size());
            entry.putBoolean("applied", party.hasJoinRequest(viewer.getUUID()));
            entry.putBoolean("invited", party.isInvited(viewer.getUUID()));
            entries.add(entry);
        }
        return entries;
    }

    private static ListTag createInvitations(ServerPlayer viewer, FactionSavedData data) {
        ListTag entries = new ListTag();
        for (String partyId : new TreeSet<>(data.getPartyIds())) {
            Party party = data.getParty(partyId).orElse(null);
            if (party == null || !party.isInvited(viewer.getUUID())) {
                continue;
            }
            CompoundTag entry = new CompoundTag();
            entry.putString("id", party.getId());
            entry.putString("name", party.getName());
            entry.putString("leaderName", playerName(viewer.server, party.getLeaderId()));
            entry.putInt("memberCount", party.getMembers().size());
            entries.add(entry);
        }
        return entries;
    }

    private static ListTag createMembers(MinecraftServer server, Party party) {
        ListTag entries = new ListTag();
        party.getMembers().stream()
                .sorted(Comparator.comparing(memberId -> playerName(server, memberId), String.CASE_INSENSITIVE_ORDER))
                .forEach(memberId -> {
                    CompoundTag entry = new CompoundTag();
                    entry.putUUID("id", memberId);
                    entry.putString("name", playerName(server, memberId));
                    entry.putBoolean("online", server.getPlayerList().getPlayer(memberId) != null);
                    entry.putBoolean("leader", party.isLeader(memberId));
                    entries.add(entry);
                });
        return entries;
    }

    private static ListTag createJoinRequests(MinecraftServer server, FactionSavedData data, Party party) {
        ListTag entries = new ListTag();
        party.getJoinRequests().stream()
                .filter(playerId -> data.getPlayerParty(playerId).isEmpty())
                .sorted(Comparator.comparing(playerId -> playerName(server, playerId), String.CASE_INSENSITIVE_ORDER))
                .forEach(playerId -> entries.add(createPlayerEntry(server, playerId)));
        return entries;
    }

    private static ListTag createInvitablePlayers(ServerPlayer viewer, FactionSavedData data, Party ownParty) {
        ListTag entries = new ListTag();
        viewer.server.getPlayerList().getPlayers().stream()
                .filter(player -> !player.getUUID().equals(viewer.getUUID()))
                .filter(player -> data.getPlayerParty(player.getUUID()).isEmpty())
                .filter(player -> !ownParty.isInvited(player.getUUID()))
                .filter(player -> !ownParty.hasJoinRequest(player.getUUID()))
                .sorted(Comparator.comparing(player -> player.getGameProfile().getName(), String.CASE_INSENSITIVE_ORDER))
                .forEach(player -> entries.add(createPlayerEntry(viewer.server, player.getUUID())));
        return entries;
    }

    private static CompoundTag createPlayerEntry(MinecraftServer server, UUID playerId) {
        CompoundTag entry = new CompoundTag();
        entry.putUUID("id", playerId);
        entry.putString("name", playerName(server, playerId));
        entry.putBoolean("online", server.getPlayerList().getPlayer(playerId) != null);
        return entry;
    }

    private static ListTag createMessages(MinecraftServer server, Party party) {
        ListTag entries = new ListTag();
        for (PartyChatService.Message message : PartyChatService.getMessages(server, party.getId())) {
            CompoundTag entry = new CompoundTag();
            entry.putString("senderName", message.senderName());
            entry.putString("content", message.content());
            entries.add(entry);
        }
        return entries;
    }

    private static String playerName(MinecraftServer server, UUID playerId) {
        ServerPlayer online = server.getPlayerList().getPlayer(playerId);
        if (online != null) {
            return online.getGameProfile().getName();
        }
        return server.getProfileCache().get(playerId)
                .map(GameProfile::getName)
                .orElseGet(() -> playerId.toString().substring(0, 8));
    }
}
