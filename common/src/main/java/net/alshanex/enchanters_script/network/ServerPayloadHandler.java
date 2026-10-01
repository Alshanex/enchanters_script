package net.alshanex.enchanters_script.network;

import net.alshanex.enchanters_script.enchanting.EnchantersTableMenu;
import net.alshanex.enchanters_script.minigame.WritingMenu;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;

public final class ServerPayloadHandler {

    private ServerPayloadHandler() {
    }

    public static void handleWritingAction(ServerPlayer player, WritingActionPayload payload) {
        AbstractContainerMenu open = player.containerMenu;
        // Only the menu this packet was meant for; anything else is stale or forged
        if (open.containerId == payload.containerId() && open instanceof WritingMenu menu) {
            menu.handleWritingAction(player, payload.action(), payload.slot(), payload.key());
        }
    }
}
