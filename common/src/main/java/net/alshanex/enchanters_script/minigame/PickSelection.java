package net.alshanex.enchanters_script.minigame;

import java.util.ArrayList;
import java.util.List;

/**
 * Which bonus cards are selected. Used on both sides with the same rules,
 * so the client can show selections instantly while the server keeps the real state.
 */
public final class PickSelection {
    private final int picks;
    private final List<Integer> selected = new ArrayList<>();

    public PickSelection(int picks) {
        this.picks = picks;
    }

    /**
     * Selects or deselects a card. A new card can only be selected while picks remain.
     */
    public void toggle(int index, int choiceCount) {
        if (index < 0 || index >= choiceCount) {
            return;
        }
        if (this.selected.contains(index)) {
            this.selected.remove(Integer.valueOf(index));
        } else if (this.selected.size() < this.picks) {
            this.selected.add(index);
        }
    }

    public boolean isSelected(int index) {
        return this.selected.contains(index);
    }

    public boolean canSelect(int index) {
        return isSelected(index) || this.selected.size() < this.picks;
    }

    public int count() {
        return this.selected.size();
    }

    public int picks() {
        return this.picks;
    }

    public List<Integer> selected() {
        return List.copyOf(this.selected);
    }
}
