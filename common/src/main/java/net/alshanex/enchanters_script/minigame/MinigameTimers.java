package net.alshanex.enchanters_script.minigame;

public final class MinigameTimers {
    private static final int BOOK_BASE_SECONDS = 8;
    private static final int BOOK_SECONDS_PER_LETTER = 3;

    private MinigameTimers(){

    }

    public static int revealTicks(String fullWord){
        int ticksPerLetter = 8;
        int minTicks = 60;

        String result = fullWord.replaceAll("\\s", "");
        return Math.max(minTicks, result.length() * ticksPerLetter);
    }

    public static int writingTicks(int lapis){
        lapis = Math.max(1, Math.min(64, lapis));
        return lapis <= 20
                ? (9 + lapis) * 20
                : (int) (Math.round(29 + 4 * Math.log(1 + (lapis - 20) / 4.0)) * 20);
    }

    public static int bookTicks(String fullWord) {
        int letters = fullWord.replace(" ", "").length();
        return (BOOK_BASE_SECONDS + letters * BOOK_SECONDS_PER_LETTER) * 20;
    }
}
