package net.alshanex.enchanters_script.minigame;

import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

/**
 * A menu that runs the writing minigame. The packet handlers talk to this
 * instead of a specific menu, so the table and ciphered books share the same packets.
 */
public interface WritingMenu {

    @Nullable
    WritingView writing();

    void setWriting(WritingView writing);

    /**
     * A move from the client's packet. Implementations check everything before applying it.
     */
    void handleWritingAction(Player player, int action, int slot, int key);
}