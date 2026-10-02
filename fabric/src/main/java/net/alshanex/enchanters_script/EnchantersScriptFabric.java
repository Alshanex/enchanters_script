package net.alshanex.enchanters_script;

import net.alshanex.enchanters_script.data.CipherSavedData;
import net.alshanex.enchanters_script.network.*;
import net.alshanex.enchanters_script.registry.ModComponents;
import net.alshanex.enchanters_script.registry.ModItems;
import net.alshanex.enchanters_script.registry.ModMenus;
import net.alshanex.enchanters_script.word.FabricWordReloadListener;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.CreativeModeTabs;

public class EnchantersScriptFabric implements ModInitializer {
    
    @Override
    public void onInitialize() {
        
        // This method is invoked by the Fabric mod loader when it is ready
        // to load your mod. You can access Fabric and Common code in this
        // project.

        // Use Fabric to bootstrap the Common mod.
        CommonClass.init();

        FabricAttachments.init();

        ModItems.register((id, item) -> Registry.register(BuiltInRegistries.ITEM, id, item));
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(entries -> entries.accept(ModItems.PRIMER));

        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new FabricWordReloadListener());

        PayloadTypeRegistry.playS2C().register(OfferPreviewsPayload.TYPE, OfferPreviewsPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(WritingStartPayload.TYPE, WritingStartPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(BonusViewPayload.TYPE, BonusViewPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(PrimerPayload.TYPE, PrimerPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(ServerSettingsPayload.TYPE, ServerSettingsPayload.STREAM_CODEC);

        PayloadTypeRegistry.playC2S().register(WritingActionPayload.TYPE, WritingActionPayload.STREAM_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(WritingActionPayload.TYPE,
                (payload, context) -> ServerPayloadHandler.handleWritingAction(context.player(), payload));
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> ServerSettingsPayload.sendTo(handler.player));

        Registry.register(BuiltInRegistries.MENU, ModMenus.ENCHANTING_TABLE_ID, ModMenus.ENCHANTING_TABLE);
        Registry.register(BuiltInRegistries.MENU, ModMenus.BOOK_ID, ModMenus.BOOK);
        Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, ModComponents.DECIPHERED_ID, ModComponents.DECIPHERED);

        //ServerLifecycleEvents.SERVER_STARTED.register(server -> Constants.LOG.info(CipherSavedData.get(server).encode("CURSE OF BINDING")));
    }
}
