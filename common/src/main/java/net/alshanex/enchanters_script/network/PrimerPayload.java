package net.alshanex.enchanters_script.network;

import io.netty.buffer.ByteBuf;
import net.alshanex.enchanters_script.Constants;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * The primer's page: 26 characters, one per symbol in order, each a learned letter or '?'.
 */
public record PrimerPayload(String page) implements CustomPacketPayload {

    public static final Type<PrimerPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "primer"));

    public static final StreamCodec<ByteBuf, PrimerPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, PrimerPayload::page,
            PrimerPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
