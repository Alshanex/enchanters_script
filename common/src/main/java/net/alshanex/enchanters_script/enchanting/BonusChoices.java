package net.alshanex.enchanters_script.enchanting;

import net.alshanex.enchanters_script.cipher.WorldCipher;
import net.alshanex.enchanters_script.minigame.BonusPreview;
import net.alshanex.enchanters_script.minigame.PickSelection;
import net.alshanex.enchanters_script.primer.Primer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.enchantment.EnchantmentInstance;

import java.util.ArrayList;
import java.util.List;

/**
 * The server's state for the bonus view: the choices, their real words,
 * which letters amethyst has revealed, and the player's selection.
 */
public final class BonusChoices {
    private static final int MAX_REVEALS = 3;

    private final List<EnchantmentInstance> choices;
    private final List<String> words;
    private final List<boolean[]> revealed = new ArrayList<>();
    private final PickSelection selection;
    private int revealsUsed;

    public BonusChoices(List<EnchantmentInstance> choices, List<String> words, int picks) {
        this.choices = List.copyOf(choices);
        this.words = List.copyOf(words);
        for (String word : words) {
            this.revealed.add(new boolean[word.length()]);
        }
        this.selection = new PickSelection(picks);
    }

    public int size() {
        return this.choices.size();
    }

    public PickSelection selection() {
        return this.selection;
    }

    /**
     * True while reveals remain and at least one card still has a hidden letter.
     */
    public boolean canReveal() {
        if (this.revealsUsed >= MAX_REVEALS) {
            return false;
        }
        for (int i = 0; i < this.words.size(); i++) {
            if (!hiddenPositions(i).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Reveals one more letter on every card, at a random hidden position.
     */
    public void revealOne(RandomSource random) {
        for (int i = 0; i < this.words.size(); i++) {
            List<Integer> hidden = hiddenPositions(i);
            if (!hidden.isEmpty()) {
                this.revealed.get(i)[hidden.get(random.nextInt(hidden.size()))] = true;
            }
        }
        this.revealsUsed++;
    }

    private List<Integer> hiddenPositions(int index) {
        String word = this.words.get(index);
        boolean[] shown = this.revealed.get(index);
        List<Integer> hidden = new ArrayList<>();
        for (int i = 0; i < word.length(); i++) {
            if (word.charAt(i) != ' ' && !shown[i]) {
                hidden.add(i);
            }
        }
        return hidden;
    }

    /**
     * The cards as the client may see them: Galactic names plus hint lines.
     */
    public List<BonusPreview> previews(WorldCipher cipher) {
        List<BonusPreview> previews = new ArrayList<>();
        for (int i = 0; i < this.words.size(); i++) {
            String word = this.words.get(i);
            boolean[] shown = this.revealed.get(i);

            StringBuilder hint = new StringBuilder(word.length());
            for (int j = 0; j < word.length(); j++) {
                char c = word.charAt(j);
                hint.append(c == ' ' ? ' ' : shown[j] ? c : Primer.UNKNOWN);
            }
            previews.add(new BonusPreview(cipher.encode(word), hint.toString()));
        }
        return previews;
    }

    public List<EnchantmentInstance> selectedChoices() {
        List<EnchantmentInstance> picked = new ArrayList<>();
        for (int index : this.selection.selected()) {
            picked.add(this.choices.get(index));
        }
        return picked;
    }

    /**
     * Reveals still available, or 0 when nothing is left to reveal.
     */
    public int revealsLeft() {
        return canReveal() ? MAX_REVEALS - this.revealsUsed : 0;
    }
}
