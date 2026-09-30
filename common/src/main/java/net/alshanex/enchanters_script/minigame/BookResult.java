package net.alshanex.enchanters_script.minigame;

public enum BookResult {
    PERFECT(1.0F, 1),
    DECENT(0.75F, 0),
    BAD(0.25F, -1),
    RETRY(0F, 0);

    private final float minScore;
    private final int levelChange;

    BookResult(float minScore, int levelChange) {
        this.minScore = minScore;
        this.levelChange = levelChange;
    }

    public int levelChange(){
        return this.levelChange;
    }

    public static BookResult fromScore(float score) {
        for (BookResult bookResult : values()) {
            if (score >= bookResult.minScore) {
                return bookResult;
            }
        }
        return RETRY;
    }

    public boolean deciphers(){
        return this != RETRY;
    }
}
