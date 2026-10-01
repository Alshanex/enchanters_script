package net.alshanex.enchanters_script.minigame;

public final class WritingButtons {
    public static final int SPACE = 10;
    public static final int DELETE = 11;
    public static final int DONE = 12;

    // Tile i is sent as TILE_BASE + i
    public static final int TILE_BASE = 100;

    private WritingButtons() {
    }

    public static boolean isTile(int id) {
        return id >= TILE_BASE;
    }
}
