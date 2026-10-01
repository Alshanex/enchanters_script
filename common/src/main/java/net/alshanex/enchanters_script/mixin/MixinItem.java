package net.alshanex.enchanters_script.mixin;

import net.alshanex.enchanters_script.book.BookMenu;
import net.alshanex.enchanters_script.book.CipheredBooks;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public class MixinItem {

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void openCipheredBook(Level level, Player player, InteractionHand hand,
                                  CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        ItemStack stack = player.getItemInHand(hand);
        if (!CipheredBooks.isCiphered(stack)) {
            return;
        }

        if (player instanceof ServerPlayer serverPlayer) {
            BookMenu.tryOpen(serverPlayer, hand);
        }
        // Success on both sides, so the client swings the arm like a normal use
        cir.setReturnValue(InteractionResultHolder.sidedSuccess(stack, level.isClientSide));
    }
}