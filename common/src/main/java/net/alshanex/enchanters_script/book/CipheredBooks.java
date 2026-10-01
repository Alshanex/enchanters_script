package net.alshanex.enchanters_script.book;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.alshanex.enchanters_script.registry.ModComponents;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.Optional;

/**
 * The rule for ciphered books: every enchanted book is ciphered until it's marked deciphered.
 */
public final class CipheredBooks {

    private CipheredBooks() {
    }

    public static boolean isCiphered(ItemStack stack) {
        return stack.is(Items.ENCHANTED_BOOK) && !stack.has(ModComponents.DECIPHERED);
    }

    public static void markDeciphered(ItemStack stack) {
        stack.set(ModComponents.DECIPHERED, Unit.INSTANCE);
    }

    /**
     * The enchantment a book is deciphered through: its first stored one.
     */
    public record BookEnchantment(Holder<Enchantment> enchantment, int level) {
    }

    public static Optional<BookEnchantment> mainEnchantment(ItemStack stack) {
        ItemEnchantments stored = stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        for (Object2IntMap.Entry<Holder<Enchantment>> entry : stored.entrySet()) {
            return Optional.of(new BookEnchantment(entry.getKey(), entry.getIntValue()));
        }
        return Optional.empty();
    }

    /**
     * Levels charged for deciphering, on vanilla's 1 to 3 scale: 1 for levels I-II, 2 for III, 3 for IV and up.
     */
    public static int xpCost(int level) {
        if (level <= 2) {
            return 1;
        }
        return level == 3 ? 2 : 3;
    }
}
