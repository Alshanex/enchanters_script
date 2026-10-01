package net.alshanex.enchanters_script.primer;

import net.alshanex.enchanters_script.platform.Services;
import net.minecraft.server.level.ServerPlayer;

/**
 * The letters a player has learned, as a 26-bit mask stored by the loader's player data.
 */
public final class LearnedLetters {

    private LearnedLetters() {
    }

    public static int mask(ServerPlayer player) {
        return Services.PLAYER_DATA.learnedLetters(player);
    }

    /**
     * Marks the given letters as learned. Returns how many of them were new.
     */
    public static int learn(ServerPlayer player, String letters) {
        int before = mask(player);
        int after = before;
        for (int i = 0; i < letters.length(); i++) {
            after |= bit(letters.charAt(i));
        }

        // Only write when something changed
        if (after != before) {
            Services.PLAYER_DATA.setLearnedLetters(player, after);
        }
        return Integer.bitCount(after) - Integer.bitCount(before);
    }

    public static int bit(char letter) {
        return 1 << (letter - 'A');
    }
}
