package net.alshanex.enchanters_script.network;

import io.netty.buffer.ByteBuf;
import net.alshanex.enchanters_script.Constants;
import net.alshanex.enchanters_script.EnchantersConfig;
import net.alshanex.enchanters_script.platform.Services;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/**
 * The server's settings that clients need, sent when a player joins.
 */
public record ServerSettingsPayload(boolean bookDeciphering) implements CustomPacketPayload {

    public static final Type<ServerSettingsPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "server_settings"));

    public static final StreamCodec<ByteBuf, ServerSettingsPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ServerSettingsPayload::bookDeciphering,
            ServerSettingsPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void sendTo(ServerPlayer player) {
        Services.NETWORK.sendToPlayer(player, new ServerSettingsPayload(EnchantersConfig.bookDeciphering(false)));
    }
}
