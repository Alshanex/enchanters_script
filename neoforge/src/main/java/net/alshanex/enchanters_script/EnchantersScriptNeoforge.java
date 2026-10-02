package net.alshanex.enchanters_script;


import net.alshanex.enchanters_script.client.ClientPayloadHandler;
import net.alshanex.enchanters_script.data.CipherSavedData;
import net.alshanex.enchanters_script.network.*;
import net.alshanex.enchanters_script.registry.ModComponents;
import net.alshanex.enchanters_script.registry.ModItems;
import net.alshanex.enchanters_script.registry.ModMenus;
import net.alshanex.enchanters_script.word.WordReloadListener;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod(Constants.MOD_ID)
public class EnchantersScriptNeoforge {

    public EnchantersScriptNeoforge(IEventBus eventBus) {

        // This method is invoked by the NeoForge mod loader when it is ready
        // to load your mod. You can access NeoForge and Common code in this
        // project.

        // Use NeoForge to bootstrap the Common mod.
        CommonClass.init();

        NeoForgeAttachments.ATTACHMENT_TYPES.register(eventBus);

        eventBus.addListener(EnchantersScriptNeoforge::onRegister);
        eventBus.addListener(EnchantersScriptNeoforge::onBuildCreativeTabs);

        NeoForge.EVENT_BUS.addListener(EnchantersScriptNeoforge::onAddReloadListeners);
        NeoForge.EVENT_BUS.addListener(EnchantersScriptNeoforge::onServerLoad);
        NeoForge.EVENT_BUS.addListener(EnchantersScriptNeoforge::onPlayerLoggedIn);
        eventBus.addListener(EnchantersScriptNeoforge::onRegisterPayloads);
    }

    private static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new WordReloadListener());
    }

    private static void onServerLoad (ServerStartedEvent event){
        //Constants.LOG.info(CipherSavedData.get(event.getServer()).encode("CURSE OF BINDING"));
    }

    private static void onRegister(RegisterEvent event) {
        event.register(Registries.MENU, helper -> {
            helper.register(ModMenus.ENCHANTING_TABLE_ID, ModMenus.ENCHANTING_TABLE);
            helper.register(ModMenus.BOOK_ID, ModMenus.BOOK);
        });

        event.register(Registries.DATA_COMPONENT_TYPE, helper ->
                helper.register(ModComponents.DECIPHERED_ID, ModComponents.DECIPHERED));

        event.register(Registries.ITEM, helper -> ModItems.register(helper::register));
    }

    private static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ServerSettingsPayload.sendTo(player);
        }
    }

    private static void onBuildCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModItems.PRIMER);
        }
    }

    private static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1.0");
        registrar.playToClient(OfferPreviewsPayload.TYPE, OfferPreviewsPayload.STREAM_CODEC,
                (payload, context) -> ClientPayloadHandler.handleOfferPreviews(payload));
        registrar.playToClient(WritingStartPayload.TYPE, WritingStartPayload.STREAM_CODEC,
                (payload, context) -> ClientPayloadHandler.handleWritingStart(payload));
        registrar.playToClient(BonusViewPayload.TYPE, BonusViewPayload.STREAM_CODEC,
                (payload, context) -> ClientPayloadHandler.handleBonusView(payload));
        registrar.playToClient(PrimerPayload.TYPE, PrimerPayload.STREAM_CODEC,
                (payload, context) -> ClientPayloadHandler.handlePrimer(payload));
        registrar.playToClient(ServerSettingsPayload.TYPE, ServerSettingsPayload.STREAM_CODEC,
                (payload, context) -> ClientPayloadHandler.handleServerSettings(payload));

        registrar.playToServer(WritingActionPayload.TYPE, WritingActionPayload.STREAM_CODEC,
                (payload, context) -> ServerPayloadHandler.handleWritingAction((ServerPlayer) context.player(), payload));
    }
}