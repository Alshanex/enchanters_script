package net.alshanex.enchanters_script.minigame;

/**
 * Button ids for the bonus view. Offers use 0 to 2 and the writing keys 10 and up.
 */
public final class BonusButtons {
    // Card i is sent as TOGGLE_BASE + i
    public static final int TOGGLE_BASE = 20;
    public static final int CONFIRM = 30;
    public static final int USE = 31;

    private BonusButtons() {
    }

    public static boolean isToggle(int id) {
        return id >= TOGGLE_BASE && id < TOGGLE_BASE + 3;
    }
}
