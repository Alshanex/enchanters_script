package net.alshanex.enchanters_script.word;

import net.minecraft.resources.ResourceLocation;

public final class WordBuilder {
    private static final String FALLBACK_WORD = "ENCHANTMENT";
    private static final int[] values = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
    private static final String[] symbols = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};

    public static String wordFromId(ResourceLocation id) {
        String path = id.getPath();
        String lastSegment = path.substring(path.lastIndexOf('/') + 1);

        StringBuilder word = new StringBuilder();
        for (int i = 0; i < lastSegment.length(); i++) {
            char c = lastSegment.charAt(i);
            if (c >= 'a' && c <= 'z') {
                word.append(Character.toUpperCase(c));
            } else if (!word.isEmpty() && word.charAt(word.length() - 1) != ' ') {
                word.append(' ');
            }
        }

        String result = word.toString().stripTrailing();
        return result.isEmpty() ? FALLBACK_WORD : result;
    }

    public static String toRoman(int number){
        StringBuilder roman = new StringBuilder();


        for (int i = 0; i < values.length; i++) {
            while (number >= values[i]) {
                roman.append(symbols[i]);
                number -= values[i];
            }
        }

        return roman.toString();
    }

    public static String fullWord(String baseWord, int level, int maxLevel){
        if(maxLevel > 1){
            return baseWord + " " + toRoman(level);
        }

        return baseWord;
    }
}
