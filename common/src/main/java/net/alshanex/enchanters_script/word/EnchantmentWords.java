package net.alshanex.enchanters_script.word;

import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public final class EnchantmentWords {
    private volatile Map<ResourceLocation, String> overrides = Map.of();

    public static final EnchantmentWords SERVER = new EnchantmentWords();
    public static final EnchantmentWords CLIENT = new EnchantmentWords();

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
}
