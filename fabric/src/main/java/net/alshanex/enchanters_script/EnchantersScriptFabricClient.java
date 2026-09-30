package net.alshanex.enchanters_script;

import net.alshanex.enchanters_script.client.ClientPayloadHandler;
import net.alshanex.enchanters_script.client.EnchantersTableScreen;
import net.alshanex.enchanters_script.network.OfferPreviewsPayload;
import net.alshanex.enchanters_script.registry.ModMenus;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screens.MenuScreens;

public class EnchantersScriptFabricClient implements ClientModInitializer {
    /**
     * Runs the mod initializer on the client environment.
     */
    @Override
    public void onInitializeClient() {
        MenuScreens.register(ModMenus.ENCHANTING_TABLE, EnchantersTableScreen::new);

        ClientPlayNetworking.registerGlobalReceiver(OfferPreviewsPayload.TYPE,
                (payload, context) -> ClientPayloadHandler.handleOfferPreviews(payload));
    }
}
