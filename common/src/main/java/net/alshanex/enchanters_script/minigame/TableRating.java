package net.alshanex.enchanters_script.minigame;

public enum TableRating {
    PERFECT(1.0F, 3, 2),
    GREAT(0.8F, 3, 1),
    DECENT(0.5F, 2, 1),
    POOR(0F, 0, 0);

    private final float minScore;
    private final int bonusChoices;
    private final int picks;

    TableRating(float minScore, int bonusChoices, int picks) {
        this.minScore = minScore;
        this.bonusChoices = bonusChoices;
        this.picks = picks;
    }

    public int bonusChoices(){
        return this.bonusChoices;
    }

    public int picks(){
        return this.picks;
    }

    public static TableRating fromScore(float score) {
        for (TableRating rating : values()) {
            if (score >= rating.minScore) {
                return rating;
            }
        }
        return POOR;
    }

    /**
     * Whether this rating passes the minigame, which is what teaches letters for the primer.
     */
    public boolean teachesLetters() {
        return this != POOR;
    }
}
