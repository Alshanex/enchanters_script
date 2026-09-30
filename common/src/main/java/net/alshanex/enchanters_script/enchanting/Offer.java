package net.alshanex.enchanters_script.enchanting;

import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.Enchantment;

public record Offer(int slot, int cost, Holder<Enchantment> enchantment, int level) {
}
