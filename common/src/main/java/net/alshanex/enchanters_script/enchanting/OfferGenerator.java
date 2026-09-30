package net.alshanex.enchanters_script.enchanting;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EnchantingTableBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class OfferGenerator {
    private OfferGenerator(){}

    public static int countBookshelves(Level level, BlockPos tablePos){
        int i = 0;

        for(BlockPos shelfPos : EnchantingTableBlock.BOOKSHELF_OFFSETS) {
            if (EnchantingTableBlock.isValidBookShelf(level, tablePos, shelfPos)) {
                ++i;
            }
        }

        return i;
    }

    public static int offerCount(int shelves){
        if (shelves <= 5){
            return 1;
        } else if (shelves <= 10){
            return 2;
        } else {
            return 3;
        }
    }

    public static List<Offer> generate(RegistryAccess registries, ItemStack stack, int shelves, int seed){
        RandomSource random = RandomSource.create(seed);

        int[] costs = new int[3];
        List<Integer> validSlots = new ArrayList<>();
        for (int slot = 0; slot < 3; slot++) {
            costs[slot] = EnchantmentHelper.getEnchantmentCost(random, slot, shelves, stack);
            if (costs[slot] >= slot + 1) {
                validSlots.add(slot);
            }
        }

        Util.shuffle(validSlots, random);
        List<Integer> chosen = new ArrayList<>(validSlots.subList(0, Math.min(offerCount(shelves), validSlots.size())));
        chosen.sort(null);

        Optional<HolderSet.Named<Enchantment>> pool = registries
                .registryOrThrow(Registries.ENCHANTMENT)
                .getTag(EnchantmentTags.IN_ENCHANTING_TABLE);
        if (pool.isEmpty()) {
            return List.of();
        }

        List<Offer> offers = new ArrayList<>();
        for (int slot : chosen) {
            random.setSeed(seed + slot);
            List<EnchantmentInstance> list = EnchantmentHelper.selectEnchantment(random, stack, costs[slot], pool.get().stream());

            if (!list.isEmpty()) {
                EnchantmentInstance first = list.getFirst();
                offers.add(new Offer(slot, costs[slot], first.enchantment, first.level));
            }
        }
        return offers;
    }
}
