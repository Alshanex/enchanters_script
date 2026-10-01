package net.alshanex.enchanters_script.platform;

import net.alshanex.enchanters_script.FabricAttachments;
import net.alshanex.enchanters_script.platform.services.IPlayerDataHelper;
import net.minecraft.server.level.ServerPlayer;

public class FabricPlayerDataHelper implements IPlayerDataHelper {

    @Override
    public int learnedLetters(ServerPlayer player) {
        return player.getAttachedOrElse(FabricAttachments.LEARNED_LETTERS, 0);
    }

    @Override
    public void setLearnedLetters(ServerPlayer player, int mask) {
        player.setAttached(FabricAttachments.LEARNED_LETTERS, mask);
    }
}
