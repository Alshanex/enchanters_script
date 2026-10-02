package net.alshanex.enchanters_script.primer;

import net.alshanex.enchanters_script.EnchantersConfig;
import net.alshanex.enchanters_script.cipher.WorldCipher;
import net.alshanex.enchanters_script.minigame.SlotInput;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class Primer {
    // Shown in place of a letter the player hasn't learned
    public static final char UNKNOWN = '?';

    private Primer() {
    }

    public static void learnFrom(ServerPlayer player, SlotInput input, String fullWord) {
        int learned = LearnedLetters.learn(player, input.correctLetters(fullWord));
        if (learned > 0) {
            player.sendSystemMessage(Component.translatable("message.enchanters_script.letters_learned", learned)
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
        }
    }

    public static String pageFor(ServerPlayer player, WorldCipher cipher) {
        int mask = LearnedLetters.mask(player);
        StringBuilder page = new StringBuilder(26);
        for (char symbol = 'A'; symbol <= 'Z'; symbol++) {
            char letter = cipher.decode(symbol);
            boolean known = (mask & LearnedLetters.bit(letter)) != 0;
            page.append(known ? letter : UNKNOWN);
        }
        return page.toString();
    }

    /**
     * One hint per tile: its real letter if the player has learned it, '?' if not.
     * All '?' when hover hints are turned off in the config.
     */
    public static String hintsFor(ServerPlayer player, String tiles) {
        if (!EnchantersConfig.hoverHints()) {
            return String.valueOf(UNKNOWN).repeat(tiles.length());
        }

        int mask = LearnedLetters.mask(player);
        StringBuilder hints = new StringBuilder(tiles.length());
        for (int i = 0; i < tiles.length(); i++) {
            char letter = tiles.charAt(i);
            hints.append((mask & LearnedLetters.bit(letter)) != 0 ? letter : UNKNOWN);
        }
        return hints.toString();
    }
}
