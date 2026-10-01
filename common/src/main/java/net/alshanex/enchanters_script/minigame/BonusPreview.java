package net.alshanex.enchanters_script.minigame;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * One bonus card as the client sees it: the name in Galactic, and the hint line
 * with question marks for hidden letters and real letters where amethyst revealed them.
 */
public record BonusPreview(String galactic, String hint) {

    public static final StreamCodec<ByteBuf, BonusPreview> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, BonusPreview::galactic,
            ByteBufCodecs.STRING_UTF8, BonusPreview::hint,
            BonusPreview::new
    );
}