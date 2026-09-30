package net.alshanex.enchanters_script.mixin;

import net.alshanex.enchanters_script.enchanting.EnchantersTableMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EnchantingTableBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.EnchantingTableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantingTableBlock.class)
public class MixinEnchantingTableBlock {
    @Inject(method = "getMenuProvider", at = @At("HEAD"), cancellable = true)
    private void replaceScreen(BlockState state, Level level, BlockPos pos, CallbackInfoReturnable<MenuProvider> cir){
        BlockEntity blockentity = level.getBlockEntity(pos);
        if (blockentity instanceof EnchantingTableBlockEntity) {
            Component component = ((Nameable)blockentity).getDisplayName();
            MenuProvider menu = new SimpleMenuProvider((containerId, playerInventory, player) ->
                    new EnchantersTableMenu(containerId, playerInventory, ContainerLevelAccess.create(level, pos)), component);
            cir.setReturnValue(menu);
        }
    }
}
