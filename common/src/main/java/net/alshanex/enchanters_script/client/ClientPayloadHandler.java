package net.alshanex.enchanters_script.client;

import net.alshanex.enchanters_script.Constants;
import net.alshanex.enchanters_script.EnchantersConfig;
import net.alshanex.enchanters_script.enchanting.EnchantersTableMenu;
import net.alshanex.enchanters_script.enchanting.OfferPreview;
import net.alshanex.enchanters_script.minigame.WritingMenu;
import net.alshanex.enchanters_script.network.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;

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

    public static void handleWritingStart(WritingStartPayload payload) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        AbstractContainerMenu open = player.containerMenu;
        if (open.containerId == payload.containerId() && open instanceof WritingMenu menu) {
            menu.setWriting(payload.writing());
        }
    }

    public static void handleBonusView(BonusViewPayload payload) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        if (player.containerMenu instanceof EnchantersTableMenu menu && menu.containerId == payload.containerId()) {
            menu.setBonus(payload.previews(), payload.picks(), payload.revealsLeft(), payload.initial());
        }
    }

    public static void handlePrimer(PrimerPayload payload) {
        Minecraft.getInstance().setScreen(new PrimerScreen(payload.page()));
    }

    public static void handleServerSettings(ServerSettingsPayload payload) {
        EnchantersConfig.setSyncedBookDeciphering(payload.bookDeciphering());
    }
}
