package net.alshanex.enchanters_script.primer;

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
}
