package com.viscript_lib.command;

import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.viscript_lib.ViScriptLib;
import com.viscript_lib.register.ICommand;
import com.viscript_lib.stage.StageUtil;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/** 注册独立于任何附属模组的阶段增删和查询指令。 */
@LDLRegister(name = "stage", registry = ICommand.COMMAND_ID)
public class StageCommand implements ICommand {
    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext,
                         Commands.CommandSelection commandSelection) {
        var stage = Commands.literal("stage");
        stage.then(change("add", true)).then(change("remove", false));
        stage.then(Commands.literal("list").executes(context -> list(context.getSource(), context.getSource().getPlayerOrException()))
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> list(context.getSource(), EntityArgument.getPlayer(context, "player")))));
        dispatcher.register(Commands.literal(ViScriptLib.MOD_ID)
                .requires(source -> source.hasPermission(Commands.LEVEL_GAMEMASTERS)).then(stage));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> change(String action, boolean add) {
        return Commands.literal(action).then(Commands.argument("flag", StringArgumentType.string())
                .suggests((context, builder) -> !add && context.getSource().getPlayer() != null
                        ? SharedSuggestionProvider.suggest(StageUtil.getStageFlags(context.getSource().getPlayer()), builder)
                        : builder.buildFuture())
                .executes(context -> change(context.getSource(), context.getSource().getPlayerOrException(),
                        StringArgumentType.getString(context, "flag"), add))
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> change(context.getSource(), EntityArgument.getPlayer(context, "player"),
                                StringArgumentType.getString(context, "flag"), add))));
    }

    private static int change(CommandSourceStack source, ServerPlayer player, String flag, boolean add) {
        if (flag.isBlank()) {
            source.sendFailure(Component.translatable("command.viscript_lib.stage.empty"));
            return 0;
        }
        boolean changed = add ? StageUtil.addStageFlag(player, flag) : StageUtil.removeStageFlag(player, flag);
        String key = "command.viscript_lib.stage." + (add ? "add" : "remove") + (changed ? "" : ".unchanged");
        source.sendSuccess(() -> Component.translatable(key, player.getDisplayName(), flag.trim()), true);
        return changed ? 1 : 0;
    }

    private static int list(CommandSourceStack source, ServerPlayer player) {
        var flags = StageUtil.getStageFlags(player);
        source.sendSuccess(() -> Component.translatable("command.viscript_lib.stage.list", player.getDisplayName(),
                String.join(", ", flags)), false);
        return flags.size();
    }
}
