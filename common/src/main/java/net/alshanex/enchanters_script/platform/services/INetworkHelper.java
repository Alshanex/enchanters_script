package net.alshanex.enchanters_script.platform.services;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public interface INetworkHelper {
    void sendToPlayer(ServerPlayer player, CustomPacketPayload payload);
}
