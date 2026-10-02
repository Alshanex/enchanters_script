package net.alshanex.enchanters_script.client;

import net.alshanex.enchanters_script.Constants;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.ResourceLocation;

public class FabricGuiColorsReloadListener extends GuiColorsReloadListener implements IdentifiableResourceReloadListener {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "gui_colors");

    @Override
    public ResourceLocation getFabricId() {
        return ID;
    }
}