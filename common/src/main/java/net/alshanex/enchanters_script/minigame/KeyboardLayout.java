package net.alshanex.enchanters_script.minigame;

import net.minecraft.Util;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.List;

public final class KeyboardLayout {
    private KeyboardLayout() {
    }

    public static List<Character> generate(String fullWord, int level, RandomSource random) {
        // One tile per letter, duplicates included
        List<Character> keys = new ArrayList<>();
        for (int i = 0; i < fullWord.length(); i++) {
            char currentChar = fullWord.charAt(i);
            if (currentChar != ' ') {
                keys.add(currentChar);
            }
        }

        // Decoys only come from letters that aren't in the name
        List<Character> unusedChars = new ArrayList<>();
        for (char c = 'A'; c <= 'Z'; c++) {
            if (!keys.contains(c)) {
                unusedChars.add(c);
            }
        }

        Util.shuffle(unusedChars, random);
        List<Character> decoys = unusedChars.subList(0, Math.min(level, unusedChars.size()));

        keys.addAll(decoys);
        Util.shuffle(keys, random);

        return keys;
    }

    /**
     * The keys as one string, one character per tile.
     */
    public static String join(List<Character> keys) {
        StringBuilder text = new StringBuilder(keys.size());
        for (char key : keys) {
            text.append(key);
        }
        return text.toString();
    }
}
