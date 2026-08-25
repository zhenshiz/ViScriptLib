package com.viscript_team.util;

import com.viscript_team.ViScriptTeam;
import com.viscript_team.data.faction.FactionSavedData;
import com.viscript_team.data.party.Party;
import com.viscript_team.network.FactionNameTagSync;
import lombok.experimental.UtilityClass;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

@UtilityClass
public class PartyPlayerService {
    private static final String RESULT_PREFIX = ViScriptTeam.MOD_ID + ".party_ui.result.";
    private static final int MAX_PARTY_NAME_LENGTH = 32;
    private static final int MAX_CHAT_LENGTH = 256;

    public static Result createParty(ServerPlayer player, String requestedName) {
        FactionSavedData data = data(player);
        if (data.getPlayerParty(player.getUUID()).isPresent()) {
            return failure("already_in_party");
        }
        String name = requestedName == null ? "" : requestedName.trim();
        if (name.isEmpty() || name.length() > MAX_PARTY_NAME_LENGTH) {
            return failure("invalid_name");
        }
        String id = UUID.randomUUID().toString();
        if (!data.createParty(id, name, player.getUUID())) {
            return failure("state_changed");
        }
        FactionNameTagSync.syncAll(player.server);
        return success("created");
    }

    public static Result applyToParty(ServerPlayer player, String partyId) {
        FactionSavedData data = data(player);
        if (data.getPlayerParty(player.getUUID()).isPresent()) {
            return failure("already_in_party");
        }
        Party party = getParty(data, partyId);
        if (party == null) {
            return failure("party_not_found");
        }
        if (party.isInvited(player.getUUID())) {
            return failure("already_invited");
        }
        if (party.hasJoinRequest(player.getUUID())) {
            return failure("already_applied");
        }
        return data.addPartyJoinRequest(party.getId(), player.getUUID())
                ? success("application_sent")
                : failure("state_changed");
    }

    public static Result acceptInvitation(ServerPlayer player, String partyId) {
        FactionSavedData data = data(player);
        if (data.getPlayerParty(player.getUUID()).isPresent()) {
            return failure("already_in_party");
        }
        Party party = getParty(data, partyId);
        if (party == null || !party.isInvited(player.getUUID())) {
            return failure("invitation_not_found");
        }
        if (!data.joinParty(player.getUUID(), party.getId())) {
            return failure("state_changed");
        }
        FactionNameTagSync.syncAll(player.server);
        return success("invitation_accepted");
    }

    public static Result declineInvitation(ServerPlayer player, String partyId) {
        FactionSavedData data = data(player);
        Party party = getParty(data, partyId);
        if (party == null) {
            return failure("invitation_not_found");
        }
        return data.removePartyInvitation(party.getId(), player.getUUID())
                ? success("invitation_declined")
                : failure("invitation_not_found");
    }

    public static Result invitePlayer(ServerPlayer leader, UUID targetId) {
        FactionSavedData data = data(leader);
        Party party = leaderParty(data, leader);
        if (party == null) {
            return failure("not_leader");
        }
        if (leader.getUUID().equals(targetId) || leader.server.getPlayerList().getPlayer(targetId) == null) {
            return failure("player_not_available");
        }
        if (data.getPlayerParty(targetId).isPresent()) {
            return failure("target_in_party");
        }
        if (party.isInvited(targetId)) {
            return failure("already_invited");
        }
        return data.invitePlayer(party.getId(), targetId)
                ? success("player_invited")
                : failure("state_changed");
    }

    public static Result acceptJoinRequest(ServerPlayer leader, UUID targetId) {
        FactionSavedData data = data(leader);
        Party party = leaderParty(data, leader);
        if (party == null) {
            return failure("not_leader");
        }
        if (!party.hasJoinRequest(targetId)) {
            return failure("request_not_found");
        }
        if (data.getPlayerParty(targetId).isPresent() || !data.joinParty(targetId, party.getId())) {
            data.removePartyJoinRequest(party.getId(), targetId);
            return failure("target_in_party");
        }
        FactionNameTagSync.syncAll(leader.server);
        return success("request_accepted");
    }

    public static Result rejectJoinRequest(ServerPlayer leader, UUID targetId) {
        FactionSavedData data = data(leader);
        Party party = leaderParty(data, leader);
        if (party == null) {
            return failure("not_leader");
        }
        return data.removePartyJoinRequest(party.getId(), targetId)
                ? success("request_rejected")
                : failure("request_not_found");
    }

    public static Result leaveParty(ServerPlayer player) {
        FactionSavedData data = data(player);
        Party party = data.getPlayerParty(player.getUUID()).orElse(null);
        if (party == null) {
            return failure("not_in_party");
        }
        if (party.isLeader(player.getUUID())) {
            return failure("leader_cannot_leave");
        }
        if (!data.leaveParty(player.getUUID())) {
            return failure("state_changed");
        }
        FactionNameTagSync.syncAll(player.server);
        return success("left");
    }

    public static Result kickMember(ServerPlayer leader, UUID targetId) {
        FactionSavedData data = data(leader);
        Party party = leaderParty(data, leader);
        if (party == null) {
            return failure("not_leader");
        }
        if (party.isLeader(targetId) || !party.containsMember(targetId)) {
            return failure("target_not_member");
        }
        if (!data.leaveParty(targetId)) {
            return failure("state_changed");
        }
        FactionNameTagSync.syncAll(leader.server);
        return success("member_kicked");
    }

    public static Result transferLeadership(ServerPlayer leader, UUID targetId) {
        FactionSavedData data = data(leader);
        Party party = leaderParty(data, leader);
        if (party == null) {
            return failure("not_leader");
        }
        if (party.isLeader(targetId) || !party.containsMember(targetId)) {
            return failure("target_not_member");
        }
        return data.setPartyLeader(party.getId(), targetId)
                ? success("leadership_transferred")
                : failure("state_changed");
    }

    public static Result disbandParty(ServerPlayer leader) {
        FactionSavedData data = data(leader);
        Party party = leaderParty(data, leader);
        if (party == null) {
            return failure("not_leader");
        }
        String partyId = party.getId();
        if (!data.removeParty(partyId)) {
            return failure("state_changed");
        }
        PartyChatService.clear(leader.server, partyId);
        FactionNameTagSync.syncAll(leader.server);
        return success("disbanded");
    }

    public static Result setFriendlyFire(ServerPlayer leader, boolean enabled) {
        FactionSavedData data = data(leader);
        Party party = leaderParty(data, leader);
        if (party == null) {
            return failure("not_leader");
        }
        if (party.allowsFriendlyFire() == enabled) {
            return success("config_updated");
        }
        return data.setPartyFriendlyFire(party.getId(), enabled)
                ? success("config_updated")
                : failure("state_changed");
    }

    public static Result sendChatMessage(ServerPlayer sender, String rawMessage) {
        Party party = data(sender).getPlayerParty(sender.getUUID()).orElse(null);
        if (party == null) {
            return failure("not_in_party");
        }
        String message = rawMessage == null ? "" : rawMessage.trim();
        if (message.isEmpty() || message.length() > MAX_CHAT_LENGTH) {
            return failure("invalid_message");
        }
        PartyChatService.addMessage(sender, party.getId(), message);
        return success("message_sent");
    }

    private static FactionSavedData data(ServerPlayer player) {
        return FactionSavedData.get(player.serverLevel());
    }

    private static Party getParty(FactionSavedData data, String partyId) {
        try {
            return data.getParty(partyId).orElse(null);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static Party leaderParty(FactionSavedData data, ServerPlayer player) {
        Party party = data.getPlayerParty(player.getUUID()).orElse(null);
        return party != null && party.isLeader(player.getUUID()) ? party : null;
    }

    private static Result success(String key) {
        return new Result(true, RESULT_PREFIX + key);
    }

    private static Result failure(String key) {
        return new Result(false, RESULT_PREFIX + "error." + key);
    }

    public record Result(boolean success, String messageKey) {
        public static Result invalidTarget() {
            return failure("invalid_target");
        }
    }
}
