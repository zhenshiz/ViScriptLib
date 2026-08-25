package com.viscript_team.command;

import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.viscript_team.ViScriptTeam;
import com.viscript_team.data.faction.Faction;
import com.viscript_team.data.faction.FactionAttitude;
import com.viscript_team.data.party.Party;
import com.viscript_team.data.party.PartyStandingStrategy;
import com.viscript_team.network.PartyScreenSync;
import com.viscript_team.util.ViScriptTeamServerUtil;
import com.viscript_lib.register.ICommand;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.Locale;
import java.util.TreeSet;
import java.util.UUID;

@LDLRegister(name = "party", registry = ICommand.COMMAND_ID)
public class PartyCommand implements ICommand {
    private static final String KEY_PREFIX = ViScriptTeam.MOD_ID + ".command.party.";
    private static final String PARTY_EXISTS_KEY = KEY_PREFIX + "error.exists";
    private static final String PARTY_NOT_FOUND_KEY = KEY_PREFIX + "error.not_found";
    private static final String PARTY_CREATED_KEY = KEY_PREFIX + "created";
    private static final String PARTY_DELETED_KEY = KEY_PREFIX + "deleted";
    private static final String PARTY_LIST_EMPTY_KEY = KEY_PREFIX + "list.empty";
    private static final String PARTY_LIST_KEY = KEY_PREFIX + "list";
    private static final String PARTY_INFO_KEY = KEY_PREFIX + "info";
    private static final String PARTY_NONE_KEY = KEY_PREFIX + "none";
    private static final String PARTY_JOINED_KEY = KEY_PREFIX + "joined";
    private static final String PARTY_LEFT_KEY = KEY_PREFIX + "left";
    private static final String PARTY_GET_KEY = KEY_PREFIX + "get";
    private static final String PARTY_LEADER_SET_KEY = KEY_PREFIX + "leader.set";
    private static final String PARTY_LEADER_UNCHANGED_KEY = KEY_PREFIX + "leader.unchanged";
    private static final String PARTY_FRIENDLY_FIRE_SET_KEY = KEY_PREFIX + "modify.friendly_fire";
    private static final String PARTY_STANDING_GET_KEY = KEY_PREFIX + "standing.get";
    private static final String PARTY_UNCHANGED_KEY = KEY_PREFIX + "unchanged";
    private static final String ENABLED_KEY = ViScriptTeam.MOD_ID + ".common.enabled";
    private static final String DISABLED_KEY = ViScriptTeam.MOD_ID + ".common.disabled";
    private static final String ATTITUDE_PREFIX = ViScriptTeam.MOD_ID + ".attitude.";
    private static final String PARTY_STANDING_STRATEGY_PREFIX = ViScriptTeam.MOD_ID + ".party_standing_strategy.";
    private static final DynamicCommandExceptionType PARTY_EXISTS = new DynamicCommandExceptionType(id -> Component.translatable(PARTY_EXISTS_KEY, id));
    private static final DynamicCommandExceptionType PARTY_NOT_FOUND = new DynamicCommandExceptionType(id -> Component.translatable(PARTY_NOT_FOUND_KEY, id));
    private static final DynamicCommandExceptionType PARTY_STANDING_STRATEGY_NOT_FOUND = new DynamicCommandExceptionType(strategy -> Component.translatable(KEY_PREFIX + "error.strategy_not_found", strategy));

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext, Commands.CommandSelection commandSelection) {
        dispatcher.register(Commands.literal(ViScriptTeam.MOD_ID)
                .then(partyCommands()));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> partyCommands() {
        return Commands.literal("party")
                .executes(context -> openPartyScreen(context.getSource()))
                .then(Commands.literal("open")
                        .executes(context -> openPartyScreen(context.getSource())))
                .then(Commands.literal("create")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("id", StringArgumentType.word())
                                .then(Commands.argument("leader", EntityArgument.player())
                                        .executes(context -> createParty(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "id"),
                                                EntityArgument.getPlayer(context, "leader"))))))
                .then(Commands.literal("delete")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("id", StringArgumentType.word())
                                .suggests(PartyCommand::suggestParties)
                                .executes(context -> deleteParty(context.getSource(), StringArgumentType.getString(context, "id")))))
                .then(Commands.literal("list")
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> listParties(context.getSource())))
                .then(Commands.literal("info")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("id", StringArgumentType.word())
                                .suggests(PartyCommand::suggestParties)
                                .executes(context -> showPartyInfo(context.getSource(), StringArgumentType.getString(context, "id")))))
                .then(Commands.literal("join")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("targets", EntityArgument.players())
                                .then(Commands.argument("party", StringArgumentType.word())
                                        .suggests(PartyCommand::suggestParties)
                                        .executes(context -> joinParty(
                                                context.getSource(),
                                                EntityArgument.getPlayers(context, "targets"),
                                                StringArgumentType.getString(context, "party"))))))
                .then(Commands.literal("leave")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("targets", EntityArgument.players())
                                .executes(context -> leaveParty(
                                        context.getSource(),
                                        EntityArgument.getPlayers(context, "targets")))))
                .then(Commands.literal("get")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("target", EntityArgument.player())
                                .executes(context -> getPlayerParty(
                                        context.getSource(),
                                        EntityArgument.getPlayer(context, "target")))))
                .then(leaderCommands())
                .then(modifyCommands())
                .then(standingCommands());
    }

    private static LiteralArgumentBuilder<CommandSourceStack> leaderCommands() {
        return Commands.literal("leader")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("set")
                        .then(Commands.argument("party", StringArgumentType.word())
                                .suggests(PartyCommand::suggestParties)
                                .then(Commands.argument("leader", EntityArgument.player())
                                        .executes(context -> setPartyLeader(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "party"),
                                                EntityArgument.getPlayer(context, "leader"))))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> modifyCommands() {
        return Commands.literal("modify")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("party", StringArgumentType.word())
                        .suggests(PartyCommand::suggestParties)
                        .then(Commands.literal("friendly_fire")
                                .then(Commands.argument("enabled", BoolArgumentType.bool())
                                        .executes(context -> setPartyFriendlyFire(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "party"),
                                                BoolArgumentType.getBool(context, "enabled"))))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> standingCommands() {
        return Commands.literal("standing")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("get")
                        .then(Commands.argument("party", StringArgumentType.word())
                                .suggests(PartyCommand::suggestParties)
                                .then(Commands.argument("faction", StringArgumentType.word())
                                        .suggests(PartyCommand::suggestFactions)
                                        .executes(context -> getPartyStanding(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "party"),
                                                StringArgumentType.getString(context, "faction"),
                                                PartyStandingStrategy.defaultStrategy()))
                                        .then(Commands.argument("strategy", StringArgumentType.word())
                                                .suggests(PartyCommand::suggestStandingStrategies)
                                                .executes(context -> getPartyStanding(
                                                        context.getSource(),
                                                        StringArgumentType.getString(context, "party"),
                                                        StringArgumentType.getString(context, "faction"),
                                                        parseStandingStrategy(StringArgumentType.getString(context, "strategy"))))))));
    }

    private static int openPartyScreen(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        PartyScreenSync.open(source.getPlayerOrException());
        return 1;
    }

    private static int createParty(CommandSourceStack source, String partyId, ServerPlayer leader) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        String id = Party.normalizeId(partyId);
        if (!ViScriptTeamServerUtil.createParty(source.getLevel(), id, leader)) {
            throw PARTY_EXISTS.create(id);
        }
        source.sendSuccess(() -> Component.translatable(PARTY_CREATED_KEY, id, leader.getGameProfile().getName()), true);
        return 1;
    }

    private static int deleteParty(CommandSourceStack source, String partyId) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        String id = Party.normalizeId(partyId);
        if (!ViScriptTeamServerUtil.deleteParty(source.getLevel(), id)) {
            throw PARTY_NOT_FOUND.create(id);
        }
        source.sendSuccess(() -> Component.translatable(PARTY_DELETED_KEY, id), true);
        return 1;
    }

    private static int listParties(CommandSourceStack source) {
        TreeSet<String> ids = new TreeSet<>(ViScriptTeamServerUtil.getPartyIds(source.getLevel()));
        Component message = ids.isEmpty()
                ? Component.translatable(PARTY_LIST_EMPTY_KEY)
                : Component.translatable(PARTY_LIST_KEY, String.join(", ", ids));
        source.sendSuccess(() -> message, false);
        return ids.size();
    }

    private static int showPartyInfo(CommandSourceStack source, String partyId) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        Party party = getPartyOrThrow(source, partyId);
        String members = memberNames(source, party);
        source.sendSuccess(() -> Component.translatable(PARTY_INFO_KEY,
                party.getId(),
                playerName(source, party.getLeaderId()),
                members,
                stateName(party.allowsFriendlyFire())), false);
        return party.getMembers().size();
    }

    private static int joinParty(CommandSourceStack source, Collection<ServerPlayer> targets, String partyId) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        Party party = getPartyOrThrow(source, partyId);
        int changed = ViScriptTeamServerUtil.joinParty(targets, party.getId());
        source.sendSuccess(() -> changed == 0
                ? Component.translatable(PARTY_UNCHANGED_KEY)
                : Component.translatable(PARTY_JOINED_KEY, changed, party.getId()), true);
        return changed;
    }

    private static int leaveParty(CommandSourceStack source, Collection<ServerPlayer> targets) {
        int changed = ViScriptTeamServerUtil.leaveParty(targets);
        source.sendSuccess(() -> changed == 0
                ? Component.translatable(PARTY_UNCHANGED_KEY)
                : Component.translatable(PARTY_LEFT_KEY, changed), true);
        return changed;
    }

    private static int getPlayerParty(CommandSourceStack source, ServerPlayer target) {
        String partyId = ViScriptTeamServerUtil.getPlayerPartyId(target);
        Object partyText = partyId == null ? Component.translatable(PARTY_NONE_KEY) : partyId;
        source.sendSuccess(() -> Component.translatable(PARTY_GET_KEY, target.getGameProfile().getName(), partyText), false);
        return partyId == null ? 0 : 1;
    }

    private static int setPartyLeader(CommandSourceStack source, String partyId, ServerPlayer leader) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        Party party = getPartyOrThrow(source, partyId);
        boolean changed = ViScriptTeamServerUtil.setPartyLeader(source.getLevel(), party.getId(), leader);
        source.sendSuccess(() -> changed
                ? Component.translatable(PARTY_LEADER_SET_KEY, party.getId(), leader.getGameProfile().getName())
                : Component.translatable(PARTY_LEADER_UNCHANGED_KEY), true);
        return changed ? 1 : 0;
    }

    private static int setPartyFriendlyFire(CommandSourceStack source, String partyId, boolean friendlyFire) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        Party party = getPartyOrThrow(source, partyId);
        ViScriptTeamServerUtil.setPartyFriendlyFire(source.getLevel(), party.getId(), friendlyFire);
        source.sendSuccess(() -> Component.translatable(PARTY_FRIENDLY_FIRE_SET_KEY, party.getId(), stateName(friendlyFire)), true);
        return 1;
    }

    private static int getPartyStanding(CommandSourceStack source, String partyId, String factionId, PartyStandingStrategy strategy) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        Party party = getPartyOrThrow(source, partyId);
        String faction = Faction.normalizeId(factionId);
        Integer points = ViScriptTeamServerUtil.getPartyEffectiveStanding(source.getLevel(), party.getId(), faction, strategy.getId());
        FactionAttitude attitude = ViScriptTeamServerUtil.getPartyEffectiveAttitude(source.getLevel(), party.getId(), faction, strategy.getId());
        source.sendSuccess(() -> Component.translatable(PARTY_STANDING_GET_KEY,
                party.getId(),
                faction,
                points,
                attitudeName(attitude == null ? FactionAttitude.NEUTRAL : attitude),
                standingStrategyName(strategy)), false);
        return points == null ? 0 : points;
    }

    private static Party getPartyOrThrow(CommandSourceStack source, String partyId) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        String id = Party.normalizeId(partyId);
        Party party = ViScriptTeamServerUtil.getParty(source.getLevel(), id);
        if (party == null) {
            throw PARTY_NOT_FOUND.create(id);
        }
        return party;
    }

    private static String memberNames(CommandSourceStack source, Party party) {
        TreeSet<String> names = new TreeSet<>();
        party.getMembers().forEach(memberId -> names.add(playerName(source, memberId)));
        return names.isEmpty() ? "-" : String.join(", ", names);
    }

    private static String playerName(CommandSourceStack source, UUID playerId) {
        ServerPlayer player = source.getServer().getPlayerList().getPlayer(playerId);
        return player == null ? playerId.toString() : player.getGameProfile().getName();
    }

    private static Component stateName(boolean enabled) {
        return Component.translatable(enabled ? ENABLED_KEY : DISABLED_KEY);
    }

    private static Component attitudeName(FactionAttitude attitude) {
        return Component.translatable(ATTITUDE_PREFIX + attitude.name().toLowerCase(Locale.ROOT));
    }

    private static Component standingStrategyName(PartyStandingStrategy strategy) {
        return Component.translatable(PARTY_STANDING_STRATEGY_PREFIX + strategy.getId());
    }

    private static PartyStandingStrategy parseStandingStrategy(String strategy) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        return PartyStandingStrategy.byName(strategy).orElseThrow(() -> PARTY_STANDING_STRATEGY_NOT_FOUND.create(strategy));
    }

    private static java.util.concurrent.CompletableFuture<com.mojang.brigadier.suggestion.Suggestions> suggestParties(
            com.mojang.brigadier.context.CommandContext<CommandSourceStack> context,
            com.mojang.brigadier.suggestion.SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(ViScriptTeamServerUtil.getPartyIds(context.getSource().getLevel()), builder);
    }

    private static java.util.concurrent.CompletableFuture<com.mojang.brigadier.suggestion.Suggestions> suggestFactions(
            com.mojang.brigadier.context.CommandContext<CommandSourceStack> context,
            com.mojang.brigadier.suggestion.SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(ViScriptTeamServerUtil.getFactionIds(context.getSource().getLevel()), builder);
    }

    private static java.util.concurrent.CompletableFuture<com.mojang.brigadier.suggestion.Suggestions> suggestStandingStrategies(
            com.mojang.brigadier.context.CommandContext<CommandSourceStack> context,
            com.mojang.brigadier.suggestion.SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(PartyStandingStrategy.ids(), builder);
    }
}
