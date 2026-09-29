package net.alshanex.enchanters_script.cipher;

import com.mojang.serialization.DataResult;
import net.minecraft.util.RandomSource;

public final class WorldCipher {
    private static final int LETTERS = 26;

    private final int[] forward;
    private final int[] inverse;

    private WorldCipher(int[] forward) {
        this.forward = forward;
        this.inverse = new int[LETTERS];

        for (int letter = 0; letter < LETTERS; letter++) {
            inverse[forward[letter]] = letter;
        }
    }

    public static WorldCipher random(RandomSource random){
        int[] symbols = new int[LETTERS];

        for (int i = 0; i < LETTERS; i++) {
            symbols[i] = i;
        }

        for (int i = symbols.length - 1; i > 0; i--) {

            int j = random.nextInt(i+1);

            while (j == i){
                j = random.nextInt(i+1);
            }

            // Swap arr[i] with the element at random index
            int temp = symbols[i];
            symbols[i] = symbols[j];
            symbols[j] = temp;
        }
        return new WorldCipher(symbols);
    }

    public char encode(char letter) {
        if (letter == ' ') {
            return ' ';
        }
        return toLetter(forward[toIndex(letter)]);
    }

    public char decode(char symbol) {
        if (symbol == ' ') {
            return ' ';
        }
        return toLetter(inverse[toIndex(symbol)]);
    }

    public String encode(String word) {
        StringBuilder result = new StringBuilder(word.length());
        for (int i = 0; i < word.length(); i++) {
            result.append(encode(word.charAt(i)));
        }
        return result.toString();
    }

    private static int toIndex(char letter) {
        if (letter < 'A' || letter > 'Z') {
            throw new IllegalArgumentException("Expected an uppercase letter A-Z, got '" + letter + "'");
        }
        return letter - 'A';
    }

    private static char toLetter(int index) {
        return (char) ('A' + index);
    }

    public int[] toArray(){
        return this.forward.clone();
    }

    public static DataResult<WorldCipher> fromArray(int[] symbols){
        boolean[] marks = new boolean[LETTERS];

        if(symbols.length != LETTERS){
            return DataResult.error(() -> "Saved cipher has " + symbols.length + " entries, but needs 26");
        }
        for(int i = 0; i < symbols.length; i++){
            int value = symbols[i];

            if(value == i){
                return DataResult.error(() -> "The value " + value + " hasn't had its position randomized");
            }

            if (value < 0 || value > 25) {
                return DataResult.error(() -> "The value " + value + " must be between 0 and 25");
            }

            if (marks[value]){
                return DataResult.error(() -> "The value " + value + " is duplicated in the array");
            }

            marks[value] = true;
        }
        return DataResult.success(new WorldCipher(symbols.clone()));
    }
}
