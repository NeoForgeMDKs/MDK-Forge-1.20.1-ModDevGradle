package com.closky.rpgmod.network;

import com.closky.rpgmod.player.RpgPlayerData;
import com.closky.rpgmod.player.StatType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Клиент -> сервер: игрок нажал кнопку "+" у характеристики. */
public class SpendStatPointPacket {
    private final int statOrdinal;

    public SpendStatPointPacket(StatType stat) {
        this.statOrdinal = stat.ordinal();
    }

    public SpendStatPointPacket(int statOrdinal) {
        this.statOrdinal = statOrdinal;
    }

    public static void encode(SpendStatPointPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.statOrdinal);
    }

    public static SpendStatPointPacket decode(FriendlyByteBuf buf) {
        return new SpendStatPointPacket(buf.readVarInt());
    }

    public static void handle(SpendStatPointPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) return;
            StatType[] values = StatType.values();
            if (msg.statOrdinal < 0 || msg.statOrdinal >= values.length) return; // защита от подделки пакета
            RpgPlayerData data = RpgPlayerData.get(player);
            if (data.spendStatPoint(values[msg.statOrdinal])) {
                data.applyBonuses(player);
                SyncPlayerDataPacket.sendTo(player, data);
            }
        });
        ctx.setPacketHandled(true);
    }
}
