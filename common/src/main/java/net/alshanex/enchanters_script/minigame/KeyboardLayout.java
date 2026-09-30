package net.alshanex.enchanters_script.minigame;

import net.minecraft.Util;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.List;

public final class KeyboardLayout {
    public static List<Character> generate(String fullWord, int level, RandomSource random){
        List<Character> uniqueChars = new ArrayList<>();
        for(int i = 0; i < fullWord.length(); i++){
            char currentChar = fullWord.charAt(i);

            if(currentChar == ' ') continue;

            if(!uniqueChars.contains(currentChar)){
                uniqueChars.add(currentChar);
            }
        }

        List<Character> unusedChars = new ArrayList<>();
        for(char c = 'A'; c <= 'Z'; c++){
            if(!uniqueChars.contains(c)) unusedChars.add(c);
        }

        Util.shuffle(unusedChars, random);
        List<Character> decoys = unusedChars.subList(0, Math.min(2 * level, unusedChars.size()));

        uniqueChars.addAll(decoys);
        Util.shuffle(uniqueChars, random);

        return uniqueChars;
    }
}
