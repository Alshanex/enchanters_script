package net.alshanex.enchanters_script.network;

import io.netty.buffer.ByteBuf;
import net.alshanex.enchanters_script.Constants;
import net.alshanex.enchanters_script.minigame.WritingView;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record WritingStartPayload(int containerId, WritingView writing) implements CustomPacketPayload {

    public static final Type<WritingStartPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "writing_start"));

    public static final StreamCodec<ByteBuf, WritingStartPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, WritingStartPayload::containerId,
            WritingView.STREAM_CODEC, WritingStartPayload::writing,
            WritingStartPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
