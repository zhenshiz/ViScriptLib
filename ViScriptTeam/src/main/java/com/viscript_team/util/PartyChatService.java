package com.viscript_team.util;

import lombok.experimental.UtilityClass;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

@UtilityClass
public class PartyChatService {
    private static final int MAX_HISTORY = 100;
    private static final Map<MinecraftServer, Map<String, ArrayDeque<Message>>> SERVER_HISTORY =
            Collections.synchronizedMap(new WeakHashMap<>());

    public static void addMessage(ServerPlayer sender, String partyId, String content) {
        Map<String, ArrayDeque<Message>> parties = SERVER_HISTORY.computeIfAbsent(sender.server, ignored -> new java.util.HashMap<>());
        ArrayDeque<Message> messages = parties.computeIfAbsent(partyId, ignored -> new ArrayDeque<>());
        messages.addLast(new Message(sender.getGameProfile().getName(), content));
        while (messages.size() > MAX_HISTORY) {
            messages.removeFirst();
        }
    }

    public static List<Message> getMessages(MinecraftServer server, String partyId) {
        Map<String, ArrayDeque<Message>> parties = SERVER_HISTORY.get(server);
        if (parties == null) {
            return List.of();
        }
        ArrayDeque<Message> messages = parties.get(partyId);
        return messages == null ? List.of() : new ArrayList<>(messages);
    }

    public static void clear(MinecraftServer server, String partyId) {
        Map<String, ArrayDeque<Message>> parties = SERVER_HISTORY.get(server);
        if (parties != null) {
            parties.remove(partyId);
        }
    }

    public record Message(String senderName, String content) {
    }
}
