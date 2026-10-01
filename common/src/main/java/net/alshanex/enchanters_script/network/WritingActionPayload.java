package net.alshanex.enchanters_script.network;

import io.netty.buffer.ByteBuf;
import net.alshanex.enchanters_script.Constants;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * A writing move from the client: place a key in a slot, clear a slot, or finish.
 */
public record WritingActionPayload(int containerId, int action, int slot, int key) implements CustomPacketPayload {
    public static final int PLACE = 0;
    public static final int CLEAR = 1;
    public static final int DONE = 2;

    public static final Type<WritingActionPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "writing_action"));

    public static final StreamCodec<ByteBuf, WritingActionPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, WritingActionPayload::containerId,
            ByteBufCodecs.VAR_INT, WritingActionPayload::action,
            ByteBufCodecs.VAR_INT, WritingActionPayload::slot,
            ByteBufCodecs.VAR_INT, WritingActionPayload::key,
            WritingActionPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
