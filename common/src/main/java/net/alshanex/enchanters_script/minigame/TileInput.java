package net.alshanex.enchanters_script.minigame;

import java.util.ArrayList;
import java.util.List;

/**
 * What the player has written so far, as a list of pressed tiles and spaces.
 * The server uses it with real letters and the client with encoded ones;
 * both apply the same rules, so their copies stay in sync.
 */
public final class TileInput {
    // Marks a space in the written list, since tiles are 0 or above
    private static final int SPACE_ENTRY = -1;
    // Stops space spam from making the written line endless
    private static final int MAX_WRITTEN = 64;

    private final String tiles;
    private final boolean[] used;
    private final List<Integer> written = new ArrayList<>();

    public TileInput(String tiles) {
        this.tiles = tiles;
        this.used = new boolean[tiles.length()];
    }

    /**
     * Applies a tile, space or delete press. Invalid presses are ignored.
     */
    public void press(int id) {
        if (id == WritingButtons.SPACE) {
            if (this.written.size() < MAX_WRITTEN) {
                this.written.add(SPACE_ENTRY);
            }
        } else if (id == WritingButtons.DELETE) {
            if (!this.written.isEmpty()) {
                int last = this.written.remove(this.written.size() - 1);
                // Deleting a tile puts it back on the keyboard
                if (last != SPACE_ENTRY) {
                    this.used[last] = false;
                }
            }
        } else if (WritingButtons.isTile(id)) {
            int tile = id - WritingButtons.TILE_BASE;
            if (tile < this.tiles.length() && !this.used[tile] && this.written.size() < MAX_WRITTEN) {
                this.used[tile] = true;
                this.written.add(tile);
            }
        }
    }

    public boolean isUsed(int tile) {
        return this.used[tile];
    }

    public int tileCount() {
        return this.tiles.length();
    }

    public char tile(int index) {
        return this.tiles.charAt(index);
    }

    /**
     * The written line as text: tile letters and spaces, in order.
     */
    public String text() {
        StringBuilder text = new StringBuilder(this.written.size());
        for (int entry : this.written) {
            text.append(entry == SPACE_ENTRY ? ' ' : this.tiles.charAt(entry));
        }
        return text.toString();
    }
}
