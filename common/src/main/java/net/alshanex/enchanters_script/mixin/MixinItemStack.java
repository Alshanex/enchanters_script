package net.alshanex.enchanters_script.mixin;

import net.alshanex.enchanters_script.book.CipheredBooks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.List;

@Mixin(ItemStack.class)
public class MixinItemStack {

    @Inject(method = "getTooltipLines", at = @At("RETURN"))
    private void addCipheredLines(Item.TooltipContext context, @Nullable Player player, TooltipFlag flag,
                                  CallbackInfoReturnable<List<Component>> cir) {
        if (!CipheredBooks.isCiphered((ItemStack) (Object) this, player == null || player.level().isClientSide)) {
            return;
        }

        List<Component> lines = cir.getReturnValue();
        // An empty list means the tooltip is hidden, and it can't be changed
        if (lines.isEmpty()) {
            return;
        }

        // Below the name and every stored enchantment line
        int enchantmentLines = ((ItemStack) (Object) this)
                .getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY).size();
        int index = Math.min(lines.size(), 1 + enchantmentLines);
        lines.add(index, Component.translatable("tooltip.enchanters_script.ciphered").withStyle(ChatFormatting.LIGHT_PURPLE));
        lines.add(index + 1, Component.translatable("tooltip.enchanters_script.decipher_hint").withStyle(ChatFormatting.GRAY).withStyle(ChatFormatting.ITALIC));
    }
}
