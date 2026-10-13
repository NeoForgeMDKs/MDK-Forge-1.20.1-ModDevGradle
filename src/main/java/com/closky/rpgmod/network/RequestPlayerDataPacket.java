package com.closky.rpgmod.network;

import com.closky.rpgmod.player.RpgPlayerData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Клиент -> сервер: "пришли мои актуальные данные" (открытие экрана прокачки по клавише). */
public class RequestPlayerDataPacket {
    public static void encode(RequestPlayerDataPacket msg, FriendlyByteBuf buf) {}

    public static RequestPlayerDataPacket decode(FriendlyByteBuf buf) {
        return new RequestPlayerDataPacket();
    }

    public static void handle(RequestPlayerDataPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player != null) {
                SyncPlayerDataPacket.sendTo(player, RpgPlayerData.get(player));
            }
        });
        ctx.setPacketHandled(true);
    }
}
