package com.vss_market.command;

import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.viscript_lib.register.ICommand;
import com.vss_market.VSSMarket;
import com.vss_market.data.MarketSavedData;
import com.vss_market.data.PlayerShopData;
import com.vss_market.network.s2c.S2CPayload;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

import java.util.concurrent.CompletableFuture;

@LDLRegister(name = "market",registry = ICommand.COMMAND_ID)
public class MarketCommands implements ICommand {
    private static final String PLAYER_ID = "player_id";

    public void register(CommandDispatcher<CommandSourceStack> dispatcher,
                         CommandBuildContext buildContext,
                         Commands.CommandSelection commandSelection) {
        dispatcher.register(Commands.literal(VSSMarket.MOD_ID)
                .then(Commands.literal("open")
                        .executes(context -> open(context.getSource()))
                        .then(Commands.argument(PLAYER_ID, StringArgumentType.word())
                                .suggests((context, builder) -> suggestPlayerIds(context.getSource(), builder))
                                .executes(context -> open(context.getSource(), StringArgumentType.getString(context, PLAYER_ID)))))
                .then(Commands.literal("delete")
                        .requires(source -> source.hasPermission(4))
                        .then(Commands.argument(PLAYER_ID, StringArgumentType.word())
                                .suggests((context, builder) -> suggestPlayerIds(context.getSource(), builder))
                                .executes(context -> deleteShop(context.getSource(), StringArgumentType.getString(context, PLAYER_ID))))));
    }

    private static int open(CommandSourceStack source) {
        return open(source, "");
    }

    private static int open(CommandSourceStack source, String requestedPlayerId) {
        var player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.translatable("vss_market.command.player_only"));
            return 0;
        }
        String selectedShopId = "";
        var shop = MarketSavedData.get(player.server.overworld()).findShopByCommandKey(requestedPlayerId).orElse(null);
        if (shop != null) {
            selectedShopId = shop.getOwnerId().toString();
        }
        S2CPayload.open(player, selectedShopId, null);
        return 1;
    }

    private static int deleteShop(CommandSourceStack source, String requestedPlayerId) {
        var savedData = MarketSavedData.get(source.getServer().overworld());
        var shop = savedData.findShopByCommandKey(requestedPlayerId).orElse(null);
        if (shop == null) {
            source.sendFailure(Component.translatable("vss_market.message.shop_not_found"));
            return 0;
        }
        savedData.removeShop(shop.getOwnerId());
        source.sendSuccess(() -> Component.translatable("vss_market.command.shop_deleted", shop.getOwnerName()), true);
        return 1;
    }

    private static CompletableFuture<com.mojang.brigadier.suggestion.Suggestions> suggestPlayerIds(
            CommandSourceStack source,
            com.mojang.brigadier.suggestion.SuggestionsBuilder builder
    ) {
        return SharedSuggestionProvider.suggest(
                MarketSavedData.get(source.getServer().overworld()).getShops().stream()
                        .map(PlayerShopData::getOwnerName)
                        .filter(name -> !name.isBlank()),
                builder
        );
    }
}
