package net.alshanex.enchanters_script;

import net.alshanex.enchanters_script.client.BookScreen;
import net.alshanex.enchanters_script.client.EnchantersTableScreen;
import net.alshanex.enchanters_script.registry.ModMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@Mod(value = Constants.MOD_ID, dist = Dist.CLIENT)
public class EnchantersScriptNeoforgeClient {
    public EnchantersScriptNeoforgeClient(IEventBus eventBus) {
        eventBus.addListener(EnchantersScriptNeoforgeClient::onRegisterScreens);
    }

    private static void onRegisterScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.ENCHANTING_TABLE, EnchantersTableScreen::new);
        event.register(ModMenus.BOOK, BookScreen::new);
    }
}
