package com.viscript_team.client;

import com.viscript_team.data.faction.FactionAttitude;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderNameTagEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;

public class FactionClientEvents {
    @SubscribeEvent
    public static void onRenderNameTag(RenderNameTagEvent event) {
        if (!(event.getEntity() instanceof LivingEntity livingEntity)) {
            return;
        }

        FactionAttitude attitude = ClientFactionNameTagCache.getAttitude(livingEntity.getUUID());
        ChatFormatting color = colorFor(attitude);
        if (color != null) {
            event.setContent(event.getContent().copy().withStyle(color));
        }
    }

    @SubscribeEvent
    public static void onClientLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientFactionNameTagCache.clear();
    }

    @Nullable
    private static ChatFormatting colorFor(@Nullable FactionAttitude attitude) {
        if (attitude == null) {
            return null;
        }
        return switch (attitude) {
            case FRIENDLY -> ChatFormatting.GREEN;
            case NEUTRAL -> ChatFormatting.YELLOW;
            case HOSTILE -> ChatFormatting.RED;
        };
    }
}
