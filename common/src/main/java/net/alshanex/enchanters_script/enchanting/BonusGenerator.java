package net.alshanex.enchanters_script.enchanting;

import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class BonusGenerator {

    private BonusGenerator() {
    }

    /**
     * Rolls up to count bonus enchantments for the item, at the offer's power.
     * Every choice is compatible with the main enchantment and with every other choice.
     */
    public static List<EnchantmentInstance> generate(RegistryAccess registries, ItemStack stack, Offer offer,
                                                     int count, RandomSource random) {
        if (count <= 0) {
            return List.of();
        }

        Optional<HolderSet.Named<Enchantment>> pool = registries
                .registryOrThrow(Registries.ENCHANTMENT)
                .getTag(EnchantmentTags.IN_ENCHANTING_TABLE);
        if (pool.isEmpty()) {
            return List.of();
        }

        // Every enchantment that fits the item at this power, each at the highest level the power allows
        List<EnchantmentInstance> available =
                EnchantmentHelper.getAvailableEnchantmentResults(offer.cost(), stack, pool.get().stream());

        // Only enchantments that can sit next to the main one (this also removes the main one itself)
        available.removeIf(instance -> !Enchantment.areCompatible(instance.enchantment, offer.enchantment()));

        List<EnchantmentInstance> chosen = new ArrayList<>();
        while (chosen.size() < count && !available.isEmpty()) {
            Optional<EnchantmentInstance> pick = WeightedRandom.getRandomItem(random, available);
            if (pick.isEmpty()) {
                break;
            }
            chosen.add(pick.get());

            // Drop anything incompatible with this pick, so any combination of cards is valid
            EnchantmentHelper.filterCompatibleEnchantments(available, pick.get());
        }
        return chosen;
    }
}
