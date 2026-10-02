package net.alshanex.enchanters_script.minigame;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Everything the client needs to draw the writing page.
 * Only encoded symbols, never the real name or the cipher.
 */
public record WritingView(String reveal, String tiles, String hints, int revealTicks, int writingTicks) {

    public static final StreamCodec<ByteBuf, WritingView> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, WritingView::reveal,
            ByteBufCodecs.STRING_UTF8, WritingView::tiles,
            ByteBufCodecs.STRING_UTF8, WritingView::hints,
            ByteBufCodecs.VAR_INT, WritingView::revealTicks,
            ByteBufCodecs.VAR_INT, WritingView::writingTicks,
            WritingView::new
    );
}
