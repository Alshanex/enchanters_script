package net.alshanex.enchanters_script;

import net.alshanex.enchanters_script.client.BookScreen;
import net.alshanex.enchanters_script.client.ClientPayloadHandler;
import net.alshanex.enchanters_script.client.EnchantersTableScreen;
import net.alshanex.enchanters_script.client.FabricGuiColorsReloadListener;
import net.alshanex.enchanters_script.network.*;
import net.alshanex.enchanters_script.registry.ModMenus;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.server.packs.PackType;

public class EnchantersScriptFabricClient implements ClientModInitializer {
    /**
     * Runs the mod initializer on the client environment.
     */
    @Override
    public void onInitializeClient() {
        MenuScreens.register(ModMenus.ENCHANTING_TABLE, EnchantersTableScreen::new);
        MenuScreens.register(ModMenus.BOOK, BookScreen::new);

        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new FabricGuiColorsReloadListener());

        ClientPlayNetworking.registerGlobalReceiver(OfferPreviewsPayload.TYPE,
                (payload, context) -> ClientPayloadHandler.handleOfferPreviews(payload));
        ClientPlayNetworking.registerGlobalReceiver(WritingStartPayload.TYPE,
                (payload, context) -> ClientPayloadHandler.handleWritingStart(payload));
        ClientPlayNetworking.registerGlobalReceiver(BonusViewPayload.TYPE,
                (payload, context) -> ClientPayloadHandler.handleBonusView(payload));
        ClientPlayNetworking.registerGlobalReceiver(PrimerPayload.TYPE,
                (payload, context) -> ClientPayloadHandler.handlePrimer(payload));
        ClientPlayNetworking.registerGlobalReceiver(ServerSettingsPayload.TYPE,
                (payload, context) -> ClientPayloadHandler.handleServerSettings(payload));
    }
}
