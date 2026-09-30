package net.alshanex.enchanters_script;


import net.alshanex.enchanters_script.data.CipherSavedData;
import net.alshanex.enchanters_script.registry.ModMenus;
import net.alshanex.enchanters_script.word.WordReloadListener;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod(Constants.MOD_ID)
public class EnchantersScriptNeoforge {

    public EnchantersScriptNeoforge(IEventBus eventBus) {

        // This method is invoked by the NeoForge mod loader when it is ready
        // to load your mod. You can access NeoForge and Common code in this
        // project.

        // Use NeoForge to bootstrap the Common mod.
        Constants.LOG.info("Hello NeoForge world!");
        CommonClass.init();

        eventBus.addListener(EnchantersScriptNeoforge::onRegister);

        NeoForge.EVENT_BUS.addListener(EnchantersScriptNeoforge::onAddReloadListeners);
        NeoForge.EVENT_BUS.addListener(EnchantersScriptNeoforge::onServerLoad);
    }

    private static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new WordReloadListener());
    }

    private static void onServerLoad (ServerStartedEvent event){
        //Constants.LOG.info(CipherSavedData.get(event.getServer()).encode("CURSE OF BINDING"));
    }

    private static void onRegister(RegisterEvent event) {
        event.register(Registries.MENU, helper ->
                helper.register(ModMenus.ENCHANTING_TABLE_ID, ModMenus.ENCHANTING_TABLE));
    }
}