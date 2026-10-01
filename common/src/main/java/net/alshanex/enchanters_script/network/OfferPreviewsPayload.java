package net.alshanex.enchanters_script.network;

import io.netty.buffer.ByteBuf;
import net.alshanex.enchanters_script.Constants;
import net.alshanex.enchanters_script.enchanting.OfferPreview;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record OfferPreviewsPayload(int containerId, List<OfferPreview> previews) implements CustomPacketPayload {

    public static final Type<OfferPreviewsPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "offer_previews"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OfferPreviewsPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, OfferPreviewsPayload::containerId,
            OfferPreview.STREAM_CODEC.apply(ByteBufCodecs.list()), OfferPreviewsPayload::previews,
            OfferPreviewsPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
