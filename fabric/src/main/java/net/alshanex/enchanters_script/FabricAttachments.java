package net.alshanex.enchanters_script;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.resources.ResourceLocation;

public final class FabricAttachments {

    public static final AttachmentType<Integer> LEARNED_LETTERS = AttachmentRegistry.<Integer>builder()
            .persistent(Codec.INT)
            .copyOnDeath()
            .initializer(() -> 0)
            .buildAndRegister(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "learned_letters"));

    private FabricAttachments() {
    }

    /**
     * Loads this class, which registers the attachment types above.
     */
    public static void init() {
    }
}
