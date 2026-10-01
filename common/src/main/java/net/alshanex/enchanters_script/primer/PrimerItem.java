package net.alshanex.enchanters_script.primer;

import net.alshanex.enchanters_script.data.CipherSavedData;
import net.alshanex.enchanters_script.network.PrimerPayload;
import net.alshanex.enchanters_script.platform.Services;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class PrimerItem extends Item {

    public PrimerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        // The server decides what the player knows; the client only opens the screen when the page arrives
        if (player instanceof ServerPlayer serverPlayer) {
            String page = Primer.pageFor(serverPlayer, CipherSavedData.get(serverPlayer.server));
            Services.NETWORK.sendToPlayer(serverPlayer, new PrimerPayload(page));
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }
}
