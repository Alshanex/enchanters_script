package net.alshanex.enchanters_script.enchanting;

import net.alshanex.enchanters_script.minigame.SlotInput;
import net.alshanex.enchanters_script.minigame.TileInput;

import java.util.List;

/**
 * The server's record of a minigame in progress.
 */
public record WritingSession(Offer offer, String fullWord, SlotInput input, long revealEnd, long deadline) {

    // Extra ticks after the deadline for moves still travelling from the client
    private static final int GRACE_TICKS = 10;

    public static WritingSession start(Offer offer, String fullWord, String tiles,
                                       long now, int revealTicks, int writingTicks) {
        long revealEnd = now + revealTicks;
        // One slot per character of the name, spaces included
        SlotInput input = new SlotInput(tiles, fullWord);
        return new WritingSession(offer, fullWord, input, revealEnd, revealEnd + writingTicks);
    }

    public boolean acceptsInput(long now) {
        return now >= this.revealEnd && now <= this.deadline + GRACE_TICKS;
    }

    public boolean isExpired(long now) {
        return now > this.deadline + GRACE_TICKS;
    }
}
