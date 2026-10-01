package net.alshanex.enchanters_script.network;

import net.alshanex.enchanters_script.enchanting.EnchantersTableMenu;
import net.minecraft.server.level.ServerPlayer;

public final class ServerPayloadHandler {

    private ServerPayloadHandler() {
    }

    public static void handleWritingAction(ServerPlayer player, WritingActionPayload payload) {
        // Only the menu this packet was meant for; anything else is stale or forged
        if (player.containerMenu instanceof EnchantersTableMenu menu && menu.containerId == payload.containerId()) {
            menu.handleWritingAction(player, payload.action(), payload.slot(), payload.key());
        }
    }
}
