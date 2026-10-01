package net.alshanex.enchanters_script.word;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.Map;

public final class EnchantmentWords {
    private volatile Map<ResourceLocation, String> overrides = Map.of();

    public static final EnchantmentWords SERVER = new EnchantmentWords();

    private EnchantmentWords() {
    }

    public void replaceAll(Map<ResourceLocation, String> newOverrides) {
        this.overrides = Map.copyOf(newOverrides);
    }

    public String baseWord(ResourceLocation enchantment) {
        String resultWord = overrides.get(enchantment);
        return resultWord != null
                ? resultWord
                : WordBuilder.wordFromId(enchantment);
    }

    public String fullWord(Holder<Enchantment> enchantment, int level) {
        ResourceLocation id = enchantment.unwrapKey().orElseThrow().location();
        return WordBuilder.fullWord(baseWord(id), level, enchantment.value().getMaxLevel());
    }
}
