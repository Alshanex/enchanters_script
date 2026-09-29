package net.alshanex.enchanters_script.scoring;

public final class TranscriptionScore {

    private TranscriptionScore() {
    }

    public static int editDistance(String a, String b) {
        // table[i][j] = fixes needed to turn the first i letters of a into the first j letters of b
        int[][] table = new int[a.length() + 1][b.length() + 1];

        // Turning i letters into nothing takes i deletes
        for (int i = 0; i <= a.length(); i++) {
            table[i][0] = i;
        }
        // Turning nothing into j letters takes j inserts
        for (int j = 0; j <= b.length(); j++) {
            table[0][j] = j;
        }

        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int delete = table[i - 1][j] + 1;
                int insert = table[i][j - 1] + 1;

                // Prefix length i ends with the letter at index i - 1
                int pairCost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                int matchOrReplace = table[i - 1][j - 1] + pairCost;

                table[i][j] = Math.min(Math.min(delete, insert), matchOrReplace);
            }
        }

        // The answer for the full strings
        return table[a.length()][b.length()];
    }

    public static float score(String written, String correct) {
        int distance = editDistance(written, correct);
        float score = 1f - (float) distance / correct.length();
        return Math.max(0f, Math.min(1f, score));
    }
}