package net.alshanex.enchanters_script.minigame;


/**
 * The writing slots and the keyboard tiles.
 * Each slot is empty, holds a space, or holds a tile.
 * Used on both sides with the same rules: the server with real letters, the client with encoded ones.
 */
public final class SlotInput {
    // Slot contents; tiles are 0 and up
    public static final int EMPTY = -2;
    public static final int SPACE = -1;

    private final String tiles;
    private final boolean[] used;
    private final int[] slots;

    /**
     * @param tiles the keyboard's letters, one per tile
     * @param template the name, real or encoded; only its spaces are read
     */
    public SlotInput(String tiles, String template) {
        this.tiles = tiles;
        this.used = new boolean[tiles.length()];
        this.slots = new int[template.length()];
        for (int i = 0; i < template.length(); i++) {
            this.slots[i] = template.charAt(i) == ' ' ? SPACE : EMPTY;
        }
    }

    /**
     * Puts a tile into an empty slot. Returns false if the move isn't allowed.
     */
    public boolean place(int slot, int tile) {
        if (slot < 0 || slot >= this.slots.length || this.slots[slot] != EMPTY) {
            return false;
        }
        if (tile < 0 || tile >= this.tiles.length() || this.used[tile]) {
            return false;
        }
        this.used[tile] = true;
        this.slots[slot] = tile;
        return true;
    }

    /**
     * Empties a slot that holds a tile, returning the tile to the keyboard. Spaces stay.
     */
    public boolean clear(int slot) {
        if (slot < 0 || slot >= this.slots.length || this.slots[slot] < 0) {
            return false;
        }
        this.used[this.slots[slot]] = false;
        this.slots[slot] = EMPTY;
        return true;
    }

    public int slotCount() {
        return this.slots.length;
    }

    public int content(int slot) {
        return this.slots[slot];
    }

    public boolean isSpace(int slot) {
        return this.slots[slot] == SPACE;
    }

    /**
     * True when the slot holds a tile.
     */
    public boolean isFilled(int slot) {
        return this.slots[slot] >= 0;
    }

    public int tileCount() {
        return this.tiles.length();
    }

    public char tile(int index) {
        return this.tiles.charAt(index);
    }

    public boolean isUsed(int tile) {
        return this.used[tile];
    }

    /**
     * The written text, left to right. Spaces are always there; empty slots are skipped.
     */
    public String text() {
        StringBuilder text = new StringBuilder(this.slots.length);
        for (int content : this.slots) {
            if (content == SPACE) {
                text.append(' ');
            } else if (content >= 0) {
                text.append(this.tiles.charAt(content));
            }
        }
        return text.toString();
    }
}
