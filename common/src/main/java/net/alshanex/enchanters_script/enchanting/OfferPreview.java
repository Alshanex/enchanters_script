package net.alshanex.enchanters_script.enchanting;

import io.netty.buffer.ByteBuf;
import net.alshanex.enchanters_script.cipher.WorldCipher;
import net.alshanex.enchanters_script.word.EnchantmentWords;
import net.alshanex.enchanters_script.word.WordBuilder;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record OfferPreview(String galactic, int cost) {
    public static OfferPreview from(Offer offer, WorldCipher cipher){
        ResourceLocation enchantmentId = offer.enchantment().unwrapKey().orElseThrow().location();
        String baseWord = EnchantmentWords.SERVER.baseWord(enchantmentId);
        String fullWord = WordBuilder.fullWord(baseWord, offer.level(), offer.enchantment().value().getMaxLevel());
        return new OfferPreview(cipher.encode(fullWord), offer.cost());
    }

    public static final StreamCodec<ByteBuf, OfferPreview> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, OfferPreview::galactic,
                    ByteBufCodecs.VAR_INT, OfferPreview::cost,
                    OfferPreview::new
            );
}
