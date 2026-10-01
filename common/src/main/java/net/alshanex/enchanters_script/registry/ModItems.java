package net.alshanex.enchanters_script.registry;

import net.alshanex.enchanters_script.Constants;
import net.alshanex.enchanters_script.primer.PrimerItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.function.BiConsumer;

public final class ModItems {
    public static final ResourceLocation PRIMER_ID = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "primer");

    // Created during registration, not when this class loads
    public static Item PRIMER;

    private ModItems() {
    }

    /**
     * Creates every item and hands it to the loader's registry.
     */
    public static void register(BiConsumer<ResourceLocation, Item> registrar) {
        PRIMER = new PrimerItem(new Item.Properties().stacksTo(1));
        registrar.accept(PRIMER_ID, PRIMER);
    }
}