package net.alshanex.enchanters_script.client;

import net.alshanex.enchanters_script.Constants;
import net.alshanex.enchanters_script.enchanting.EnchantersTableMenu;
import net.alshanex.enchanters_script.enchanting.OfferPreview;
import net.alshanex.enchanters_script.network.OfferPreviewsPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public final class ClientPayloadHandler {

    private ClientPayloadHandler() {
    }

    public static void handleOfferPreviews(OfferPreviewsPayload payload) {
        /*
        Constants.LOG.info("Client received {} previews for container {}", payload.previews().size(), payload.containerId());
        for (OfferPreview preview : payload.previews()) {
            Constants.LOG.info("  {} (cost {})", preview.galactic(), preview.cost());
        }
        */
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        if (player.containerMenu instanceof EnchantersTableMenu menu && menu.containerId == payload.containerId()) {
            menu.setPreviews(payload.previews());
        }
    }
}
