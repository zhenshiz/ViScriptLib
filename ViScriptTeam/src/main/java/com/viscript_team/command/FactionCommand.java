package com.viscript_team.command;

import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.viscript_team.ViScriptTeam;
import com.viscript_team.data.faction.Faction;
import com.viscript_team.data.faction.FactionAttitude;
import com.viscript_team.data.faction.FactionSavedData;
import com.viscript_team.network.FactionEditorSync;
import com.viscript_team.util.ViScriptTeamServerUtil;
import com.viscript_lib.register.ICommand;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.Collection;
import java.util.Locale;
import java.util.TreeSet;

@LDLRegister(name = "faction", registry = ICommand.COMMAND_ID)
public class FactionCommand implements ICommand {
    private static final String KEY_PREFIX = ViScriptTeam.MOD_ID + ".command.faction.";
    private static final String FACTION_EXISTS_KEY = KEY_PREFIX + "error.exists";
    private static final String FACTION_NOT_FOUND_KEY = KEY_PREFIX + "error.not_found";
    private static final String FACTION_CREATED_KEY = KEY_PREFIX + "created";
    private static final String FACTION_DELETED_KEY = KEY_PREFIX + "deleted";
    private static final String FACTION_LIST_EMPTY_KEY = KEY_PREFIX + "list.empty";
    private static final String FACTION_LIST_KEY = KEY_PREFIX + "list";
    private static final String FACTION_INFO_KEY = KEY_PREFIX + "info";
    private static final String FACTION_NONE_KEY = KEY_PREFIX + "none";
    private static final String ENEMY_ADDED_KEY = KEY_PREFIX + "enemy.added";
    private static final String ENEMY_REMOVED_KEY = KEY_PREFIX + "enemy.removed";
    private static final String ENEMY_UNCHANGED_KEY = KEY_PREFIX + "enemy.unchanged";
    private static final String ENTITY_SET_KEY = KEY_PREFIX + "entity.set";
    private static final String ENTITY_CLEARED_KEY = KEY_PREFIX + "entity.cleared";
    private static final String ENTITY_GET_KEY = KEY_PREFIX + "entity.get";
    private static final String STANDING_SET_KEY = KEY_PREFIX + "standing.set";
    private static final String STANDING_ADDED_KEY = KEY_PREFIX + "standing.added";
    private static final String STANDING_GET_KEY = KEY_PREFIX + "standing.get";
    private static final String ATTITUDE_PREFIX = ViScriptTeam.MOD_ID + ".attitude.";
    private static final DynamicCommandExceptionType FACTION_EXISTS = new DynamicCommandExceptionType(id -> Component.translatable(FACTION_EXISTS_KEY, id));
    private static final DynamicCommandExceptionType FACTION_NOT_FOUND = new DynamicCommandExceptionType(id -> Component.translatable(FACTION_NOT_FOUND_KEY, id));

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext, Commands.CommandSelection commandSelection) {
        dispatcher.register(Commands.literal(ViScriptTeam.MOD_ID)
                .then(factionCommands()));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> factionCommands() {
        return Commands.literal("faction")
                .requires(source -> source.hasPermission(2))
                .executes(context -> openFactionEditor(context.getSource()))
                .then(Commands.literal("open")
                        .executes(context -> openFactionEditor(context.getSource())))
                .then(Commands.literal("create")
                        .then(Commands.argument("id", StringArgumentType.word())
                                .executes(context -> createFaction(context.getSource(), StringArgumentType.getString(context, "id")))))
                .then(Commands.literal("delete")
                        .then(Commands.argument("id", StringArgumentType.word())
                                .suggests(FactionCommand::suggestFactions)
                                .executes(context -> deleteFaction(context.getSource(), StringArgumentType.getString(context, "id")))))
                .then(Commands.literal("list")
                        .executes(context -> listFactions(context.getSource())))
                .then(Commands.literal("info")
                        .then(Commands.argument("id", StringArgumentType.word())
                                .suggests(FactionCommand::suggestFactions)
                                .executes(context -> showFactionInfo(context.getSource(), StringArgumentType.getString(context, "id")))))
                .then(enemyCommands())
                .then(entityFactionCommands())
                .then(standingCommands());
    }

    private static int openFactionEditor(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        FactionEditorSync.open(source.getPlayerOrException());
        return 1;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> enemyCommands() {
        return Commands.literal("enemy")
                .then(Commands.literal("add")
                        .then(Commands.argument("faction", StringArgumentType.word())
                                .suggests(FactionCommand::suggestFactions)
                                .then(Commands.argument("enemy", StringArgumentType.word())
                                        .suggests(FactionCommand::suggestFactions)
                                        .executes(context -> addEnemyFaction(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "faction"),
                                                StringArgumentType.getString(context, "enemy"))))))
                .then(Commands.literal("remove")
                        .then(Commands.argument("faction", StringArgumentType.word())
                                .suggests(FactionCommand::suggestFactions)
                                .then(Commands.argument("enemy", StringArgumentType.word())
                                        .suggests(FactionCommand::suggestFactions)
                                        .executes(context -> removeEnemyFaction(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "faction"),
                                                StringArgumentType.getString(context, "enemy"))))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> entityFactionCommands() {
        return Commands.literal("entity")
                .then(Commands.literal("set")
                        .then(Commands.argument("targets", EntityArgument.entities())
                                .then(Commands.argument("faction", StringArgumentType.word())
                                        .suggests(FactionCommand::suggestFactions)
                                        .executes(context -> setEntityFaction(
                                                context.getSource(),
                                                EntityArgument.getEntities(context, "targets"),
                                                StringArgumentType.getString(context, "faction"))))))
                .then(Commands.literal("clear")
                        .then(Commands.argument("targets", EntityArgument.entities())
                                .executes(context -> clearEntityFaction(
                                        context.getSource(),
                                        EntityArgument.getEntities(context, "targets")))))
                .then(Commands.literal("get")
                        .then(Commands.argument("target", EntityArgument.entity())
                                .executes(context -> getEntityFaction(
                                        context.getSource(),
                                        EntityArgument.getEntity(context, "target")))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> standingCommands() {
        return Commands.literal("standing")
                .then(Commands.literal("set")
                        .then(Commands.argument("targets", EntityArgument.players())
                                .then(Commands.argument("faction", StringArgumentType.word())
                                        .suggests(FactionCommand::suggestFactions)
                                        .then(Commands.argument("points", IntegerArgumentType.integer())
                                                .executes(context -> setPlayerStanding(
                                                        context.getSource(),
                                                        EntityArgument.getPlayers(context, "targets"),
                                                        StringArgumentType.getString(context, "faction"),
                                                        IntegerArgumentType.getInteger(context, "points")))))))
                .then(Commands.literal("add")
                        .then(Commands.argument("targets", EntityArgument.players())
                                .then(Commands.argument("faction", StringArgumentType.word())
                                        .suggests(FactionCommand::suggestFactions)
                                        .then(Commands.argument("delta", IntegerArgumentType.integer())
                                                .executes(context -> addPlayerStanding(
                                                        context.getSource(),
                                                        EntityArgument.getPlayers(context, "targets"),
                                                        StringArgumentType.getString(context, "faction"),
                                                        IntegerArgumentType.getInteger(context, "delta")))))))
                .then(Commands.literal("get")
                        .then(Commands.argument("target", EntityArgument.player())
                                .then(Commands.argument("faction", StringArgumentType.word())
                                        .suggests(FactionCommand::suggestFactions)
                                        .executes(context -> getPlayerStanding(
                                                context.getSource(),
                                                EntityArgument.getPlayer(context, "target"),
                                                StringArgumentType.getString(context, "faction"))))));
    }

    private static int createFaction(CommandSourceStack source, String factionId) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        String id = Faction.normalizeId(factionId);
        if (!ViScriptTeamServerUtil.createFaction(source.getLevel(), id)) {
            throw FACTION_EXISTS.create(id);
        }
        source.sendSuccess(() -> Component.translatable(FACTION_CREATED_KEY, id), true);
        return 1;
    }

    private static int deleteFaction(CommandSourceStack source, String factionId) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        String id = Faction.normalizeId(factionId);
        if (!ViScriptTeamServerUtil.deleteFaction(source.getLevel(), id)) {
            throw FACTION_NOT_FOUND.create(id);
        }
        source.sendSuccess(() -> Component.translatable(FACTION_DELETED_KEY, id), true);
        return 1;
    }

    private static int listFactions(CommandSourceStack source) {
        TreeSet<String> ids = new TreeSet<>(ViScriptTeamServerUtil.getFactionIds(source.getLevel()));
        Component message = ids.isEmpty()
                ? Component.translatable(FACTION_LIST_EMPTY_KEY)
                : Component.translatable(FACTION_LIST_KEY, String.join(", ", ids));
        source.sendSuccess(() -> message, false);
        return ids.size();
    }

    private static int showFactionInfo(CommandSourceStack source, String factionId) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        String id = Faction.normalizeId(factionId);
        Faction faction = ViScriptTeamServerUtil.getFaction(source.getLevel(), id);
        if (faction == null) {
            throw FACTION_NOT_FOUND.create(id);
        }
        TreeSet<String> enemies = new TreeSet<>(faction.getEnemyFactions());
        Object enemyText = enemies.isEmpty() ? Component.translatable(FACTION_NONE_KEY) : String.join(", ", enemies);
        source.sendSuccess(() -> Component.translatable(FACTION_INFO_KEY,
                id,
                faction.getDefaultPoints(),
                enemyText), false);
        return 1;
    }

    private static int addEnemyFaction(CommandSourceStack source, String factionId, String enemyFactionId) {
        String id = Faction.normalizeId(factionId);
        String enemyId = Faction.normalizeId(enemyFactionId);
        boolean changed = ViScriptTeamServerUtil.addEnemyFaction(source.getLevel(), id, enemyId);
        source.sendSuccess(() -> changed
                ? Component.translatable(ENEMY_ADDED_KEY, id, enemyId)
                : Component.translatable(ENEMY_UNCHANGED_KEY), true);
        return changed ? 1 : 0;
    }

    private static int removeEnemyFaction(CommandSourceStack source, String factionId, String enemyFactionId) {
        String id = Faction.normalizeId(factionId);
        String enemyId = Faction.normalizeId(enemyFactionId);
        boolean changed = ViScriptTeamServerUtil.removeEnemyFaction(source.getLevel(), id, enemyId);
        source.sendSuccess(() -> changed
                ? Component.translatable(ENEMY_REMOVED_KEY, id, enemyId)
                : Component.translatable(ENEMY_UNCHANGED_KEY), true);
        return changed ? 1 : 0;
    }

    private static int setEntityFaction(CommandSourceStack source, Collection<? extends Entity> targets, String factionId) {
        String id = Faction.normalizeId(factionId);
        int count = ViScriptTeamServerUtil.setEntityFaction(targets, id);
        source.sendSuccess(() -> Component.translatable(ENTITY_SET_KEY, count, id), true);
        return count;
    }

    private static int clearEntityFaction(CommandSourceStack source, Collection<? extends Entity> targets) {
        int count = ViScriptTeamServerUtil.clearEntityFaction(targets);
        source.sendSuccess(() -> Component.translatable(ENTITY_CLEARED_KEY, count), true);
        return count;
    }

    private static int getEntityFaction(CommandSourceStack source, Entity target) {
        String factionId = ViScriptTeamServerUtil.getEntityFactionId(target);
        Object factionText = factionId == null ? Component.translatable(FACTION_NONE_KEY) : factionId;
        source.sendSuccess(() -> Component.translatable(ENTITY_GET_KEY, target.getDisplayName(), factionText), false);
        return 1;
    }

    private static int setPlayerStanding(CommandSourceStack source, Collection<ServerPlayer> targets, String factionId, int points) {
        String id = Faction.normalizeId(factionId);
        int count = ViScriptTeamServerUtil.setPlayerStanding(targets, id, points);
        source.sendSuccess(() -> Component.translatable(STANDING_SET_KEY, count, id, points), true);
        return count;
    }

    private static int addPlayerStanding(CommandSourceStack source, Collection<ServerPlayer> targets, String factionId, int delta) {
        String id = Faction.normalizeId(factionId);
        int count = ViScriptTeamServerUtil.addPlayerStanding(targets, id, delta);
        source.sendSuccess(() -> Component.translatable(STANDING_ADDED_KEY, count, id, "%+d".formatted(delta)), true);
        return count;
    }

    private static int getPlayerStanding(CommandSourceStack source, ServerPlayer target, String factionId) {
        String id = Faction.normalizeId(factionId);
        int points = ViScriptTeamServerUtil.getPlayerStanding(target, id);
        FactionAttitude attitude = ViScriptTeamServerUtil.getPlayerAttitude(target, id);
        source.sendSuccess(() -> Component.translatable(STANDING_GET_KEY,
                target.getGameProfile().getName(),
                id,
                points,
                attitudeName(attitude)), false);
        return points;
    }

    private static Component attitudeName(FactionAttitude attitude) {
        return Component.translatable(ATTITUDE_PREFIX + attitude.name().toLowerCase(Locale.ROOT));
    }

    private static java.util.concurrent.CompletableFuture<com.mojang.brigadier.suggestion.Suggestions> suggestFactions(
            com.mojang.brigadier.context.CommandContext<CommandSourceStack> context,
            com.mojang.brigadier.suggestion.SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(FactionSavedData.get(context.getSource().getLevel()).getFactionIds(), builder);
    }
}
