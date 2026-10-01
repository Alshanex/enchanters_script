package net.alshanex.enchanters_script.platform;

import net.alshanex.enchanters_script.NeoForgeAttachments;
import net.alshanex.enchanters_script.platform.services.IPlayerDataHelper;
import net.minecraft.server.level.ServerPlayer;

public class NeoForgePlayerDataHelper implements IPlayerDataHelper {

    @Override
    public int learnedLetters(ServerPlayer player) {
        return player.getData(NeoForgeAttachments.LEARNED_LETTERS);
    }

    @Override
    public void setLearnedLetters(ServerPlayer player, int mask) {
        player.setData(NeoForgeAttachments.LEARNED_LETTERS, mask);
    }
}
