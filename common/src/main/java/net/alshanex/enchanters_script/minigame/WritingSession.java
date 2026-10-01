package net.alshanex.enchanters_script.minigame;

/**
 * The server's record of a minigame in progress: the real word, the slots, and the deadlines.
 * Never sent to the client, since it holds the real name.
 */
public record WritingSession(String fullWord, SlotInput input, long revealEnd, long deadline) {

    // Extra ticks after the deadline for moves still travelling from the client
    private static final int GRACE_TICKS = 10;

    public static WritingSession start(String fullWord, String tiles, long now, int revealTicks, int writingTicks) {
        long revealEnd = now + revealTicks;
        // One slot per character of the name; its spaces become fixed gaps
        return new WritingSession(fullWord, new SlotInput(tiles, fullWord), revealEnd, revealEnd + writingTicks);
    }

    public boolean acceptsInput(long now) {
        return now >= this.revealEnd && now <= this.deadline + GRACE_TICKS;
    }

    public boolean isExpired(long now) {
        return now > this.deadline + GRACE_TICKS;
    }
}