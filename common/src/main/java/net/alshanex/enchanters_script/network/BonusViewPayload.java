package net.alshanex.enchanters_script.network;

import io.netty.buffer.ByteBuf;
import net.alshanex.enchanters_script.Constants;
import net.alshanex.enchanters_script.minigame.BonusPreview;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * Bonus cards for the client. Sent once when the bonus view opens (initial) and again after each reveal.
 */
public record BonusViewPayload(int containerId, List<BonusPreview> previews, int picks, int revealsLeft, boolean initial)
        implements CustomPacketPayload {

    public static final Type<BonusViewPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "bonus_view"));

    public static final StreamCodec<ByteBuf, BonusViewPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, BonusViewPayload::containerId,
            BonusPreview.STREAM_CODEC.apply(ByteBufCodecs.list()), BonusViewPayload::previews,
            ByteBufCodecs.VAR_INT, BonusViewPayload::picks,
            ByteBufCodecs.VAR_INT, BonusViewPayload::revealsLeft,
            ByteBufCodecs.BOOL, BonusViewPayload::initial,
            BonusViewPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
