package net.alshanex.enchanters_script.word;

import net.alshanex.enchanters_script.Constants;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.ResourceLocation;

public class FabricWordReloadListener extends WordReloadListener implements IdentifiableResourceReloadListener {

    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "enchanter_words");

    @Override
    public ResourceLocation getFabricId() {
        return ID;
    }
}
