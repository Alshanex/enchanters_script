package net.alshanex.enchanters_script.word;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.Locale;

public record EnchantmentWord(ResourceLocation enchantment, String word) {
    private static final Codec<String> VALID_WORD = Codec.STRING.comapFlatMap(EnchantmentWord::validateWord, word -> word);

    public static final Codec<EnchantmentWord> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("enchantment").forGetter(EnchantmentWord::enchantment),
            VALID_WORD.fieldOf("word").forGetter(EnchantmentWord::word)
    ).apply(instance, EnchantmentWord::new));

    private static final int MAX_LETTERS = 32;

    static DataResult<String> validateWord(String word) {
        if (word.isEmpty()) {
            return DataResult.error(() -> "The word is empty");
        }
        if (word.charAt(0) == ' ' || word.charAt(word.length() - 1) == ' ') {
            return DataResult.error(() -> "Word \"" + word + "\" starts or ends with a space");
        }

        int letters = 0;
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            if (c == ' ') {
                if (word.charAt(i - 1) == ' ') {
                    return DataResult.error(() -> "Word \"" + word + "\" has two spaces in a row");
                }
            } else if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                letters++;
            } else {
                return DataResult.error(() -> "Word \"" + word + "\" contains '" + c + "', but only letters and spaces are allowed");
            }
        }

        if (letters > MAX_LETTERS) {
            int count = letters;
            return DataResult.error(() -> "Word \"" + word + "\" has " + count + " letters, but the maximum is " + MAX_LETTERS);
        }
        return DataResult.success(word.toUpperCase(Locale.ROOT));
    }
}
