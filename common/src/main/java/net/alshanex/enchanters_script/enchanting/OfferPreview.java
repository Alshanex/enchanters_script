package net.alshanex.enchanters_script.enchanting;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.enchantment.Enchantment;

public record OfferPreview(Component name, int slot, int cost) {

    public static final StreamCodec<RegistryFriendlyByteBuf, OfferPreview> STREAM_CODEC = StreamCodec.composite(
            ComponentSerialization.STREAM_CODEC, OfferPreview::name,
            ByteBufCodecs.VAR_INT, OfferPreview::slot,
            ByteBufCodecs.VAR_INT, OfferPreview::cost,
            OfferPreview::new
    );

    public static OfferPreview from(Offer offer) {
        Enchantment enchantment = offer.enchantment().value();

        MutableComponent name = enchantment.description().copy();
        if (enchantment.getMaxLevel() > 1) {
            name.append(CommonComponents.SPACE).append(Component.translatable("enchantment.level." + offer.level()));
        }

        return new OfferPreview(name, offer.slot(), offer.cost());
    }
}