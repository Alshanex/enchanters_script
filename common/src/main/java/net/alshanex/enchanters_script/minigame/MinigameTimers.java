package net.alshanex.enchanters_script.minigame;

import net.alshanex.enchanters_script.EnchantersConfig;

public final class MinigameTimers {
    // Reveal: 0.4 seconds per letter, at least 3 seconds
    private static final int REVEAL_TICKS_PER_LETTER = 8;
    private static final int MIN_REVEAL_TICKS = 60;

    // Writing: a base for reading the slots, plus time per letter to place
    private static final int BASE_SECONDS = 8;
    private static final int SECONDS_PER_LETTER = 3;

    // Lapis scales the table's writing time from about 0.75x (1 lapis) to 1.5x (a full stack)
    private static final double LAPIS_MULTIPLIER_FLOOR = 0.5;
    private static final double FULL_STACK_CURVE_SECONDS = 39.0;

    private MinigameTimers() {
    }

    public static int revealTicks(String fullWord) {
        int ticks = Math.max(MIN_REVEAL_TICKS, letters(fullWord) * REVEAL_TICKS_PER_LETTER);
        return scale(ticks, EnchantersConfig.revealTimeMultiplier());
    }

    /**
     * Writing time at the table: the name's base time, scaled by the lapis spent.
     */
    public static int writingTicks(String fullWord, int lapis) {
        int ticks = (int) Math.round(baseSeconds(fullWord) * lapisMultiplier(lapis)) * 20;
        return scale(ticks, EnchantersConfig.writingTimeMultiplier());
    }

    /**
     * Writing time for a ciphered book: the name's base time, since books use no lapis.
     */
    public static int bookTicks(String fullWord) {
        return scale(baseSeconds(fullWord) * 20, EnchantersConfig.writingTimeMultiplier());
    }

    /**
     * 0.5 plus the lapis curve as a fraction of its full-stack value:
     * about 0.76 with 1 lapis, about 1 with 10, and 1.5 with 64.
     */
    public static double lapisMultiplier(int lapis) {
        return LAPIS_MULTIPLIER_FLOOR + lapisCurveSeconds(lapis) / FULL_STACK_CURVE_SECONDS;
    }

    /**
     * 1 second per lapis up to 20, then diminishing, 39 seconds at 64.
     */
    static long lapisCurveSeconds(int lapis) {
        lapis = Math.max(1, Math.min(64, lapis));
        return lapis <= 20
                ? 9 + lapis
                : Math.round(29 + 4 * Math.log(1 + (lapis - 20) / 4.0));
    }

    private static int baseSeconds(String fullWord) {
        return BASE_SECONDS + letters(fullWord) * SECONDS_PER_LETTER;
    }

    // Spaces are fixed gaps, so only letters count
    private static int letters(String fullWord) {
        return fullWord.replace(" ", "").length();
    }

    /**
     * Applies an accessibility multiplier from the config, rounded to whole ticks.
     */
    private static int scale(int ticks, double multiplier) {
        return (int) Math.round(ticks * multiplier);
    }
}
