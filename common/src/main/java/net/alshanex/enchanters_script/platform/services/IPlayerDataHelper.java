package net.alshanex.enchanters_script.platform.services;

import net.minecraft.server.level.ServerPlayer;

/**
 * Per-player data kept by the loader's data attachments.
 */
public interface IPlayerDataHelper {

    /**
     * The player's learned letters as a 26-bit mask: bit 0 is A, bit 25 is Z.
     */
    int learnedLetters(ServerPlayer player);

    void setLearnedLetters(ServerPlayer player, int mask);
}
