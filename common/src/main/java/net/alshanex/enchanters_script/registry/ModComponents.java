package net.alshanex.enchanters_script.registry;

import net.alshanex.enchanters_script.Constants;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;

public final class ModComponents {

    public static final ResourceLocation DECIPHERED_ID =
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "deciphered");

    /**
     * Marks an enchanted book as deciphered. Enchanted books without it are ciphered.
     */
    public static final DataComponentType<Unit> DECIPHERED = DataComponentType.<Unit>builder()
            .persistent(Unit.CODEC)
            .networkSynchronized(StreamCodec.unit(Unit.INSTANCE))
            .build();

    private ModComponents() {
    }
}
