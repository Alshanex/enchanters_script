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

    static DataResult<String> validateWord(String word) {
        int counter = 0;
        char lastChar = '.';

        if(word.isEmpty()){
            return DataResult.error(() -> "There's no word to read");
        }

        if (word.charAt(0) == ' ' || word.charAt(word.length() - 1) == ' '){
            return DataResult.error(() -> "Words can't have spaces at the start or at the end of the word");
        }

        for (int i = 0; i < word.length(); i++){
            char currentChar = word.charAt(i);

            boolean isLetter = (currentChar >= 'a' && currentChar <= 'z') || (currentChar >= 'A' && currentChar <= 'Z');

            if(!(isLetter || currentChar == ' ')){
                return DataResult.error(() -> "Word \"" + word + "\" contains '" + currentChar + "', but only letters and spaces are allowed");
            }
            if(lastChar == ' ' && currentChar == ' '){
                return DataResult.error(() -> "The word " + word + " has two spaces in a row");
            }
            lastChar = currentChar;

            if (isLetter) {
                counter++;
            }
        }

        if(counter < 1 || counter > 32){
            return DataResult.error(() -> "Words need to have between 1 and 32 letters");
        }

        return DataResult.success(word.toUpperCase(Locale.ROOT));
    }
}
