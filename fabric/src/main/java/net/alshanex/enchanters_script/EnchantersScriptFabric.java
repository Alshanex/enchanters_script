package net.alshanex.enchanters_script;

import net.alshanex.enchanters_script.data.CipherSavedData;
import net.alshanex.enchanters_script.word.FabricWordReloadListener;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.server.packs.PackType;

public class EnchantersScriptFabric implements ModInitializer {
    
    @Override
    public void onInitialize() {
        
        // This method is invoked by the Fabric mod loader when it is ready
        // to load your mod. You can access Fabric and Common code in this
        // project.

        // Use Fabric to bootstrap the Common mod.
        Constants.LOG.info("Hello Fabric world!");
        CommonClass.init();

        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new FabricWordReloadListener());

        //ServerLifecycleEvents.SERVER_STARTED.register(server -> Constants.LOG.info(CipherSavedData.get(server).encode("CURSE OF BINDING")));
    }
}
