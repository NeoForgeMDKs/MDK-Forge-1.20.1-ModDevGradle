package com.closky.rpgmod.network;

import com.closky.rpgmod.client.ClientPlayerData;
import com.closky.rpgmod.player.RpgPlayerData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

/** Сервер -> клиент: полное состояние прогресса игрока (уровень, очки, мана). */
public class SyncPlayerDataPacket {
    private final int level;
    private final int statPoints;
    private final int spentStrength;
    private final int spentVitality;
    private final int spentIntellect;
    private final float mana;
    private final float maxMana;

    public SyncPlayerDataPacket(RpgPlayerData data) {
        this.level = data.getLevel();
        this.statPoints = data.getStatPoints();
        this.spentStrength = data.getSpentStrength();
        this.spentVitality = data.getSpentVitality();
        this.spentIntellect = data.getSpentIntellect();
        this.mana = data.getMana();
        this.maxMana = data.getMaxMana();
    }

    public SyncPlayerDataPacket(int level, int statPoints, int spentStrength, int spentVitality, int spentIntellect, float mana, float maxMana) {
        this.level = level;
        this.statPoints = statPoints;
        this.spentStrength = spentStrength;
        this.spentVitality = spentVitality;
        this.spentIntellect = spentIntellect;
        this.mana = mana;
        this.maxMana = maxMana;
    }

    public static void encode(SyncPlayerDataPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.level);
        buf.writeVarInt(msg.statPoints);
        buf.writeVarInt(msg.spentStrength);
        buf.writeVarInt(msg.spentVitality);
        buf.writeVarInt(msg.spentIntellect);
        buf.writeFloat(msg.mana);
        buf.writeFloat(msg.maxMana);
    }

    public static SyncPlayerDataPacket decode(FriendlyByteBuf buf) {
        return new SyncPlayerDataPacket(buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(),
                buf.readVarInt(), buf.readFloat(), buf.readFloat());
    }

    public static void handle(SyncPlayerDataPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> ClientPlayerData.set(msg.level, msg.statPoints, msg.spentStrength, msg.spentVitality,
                msg.spentIntellect, msg.mana, msg.maxMana));
        ctx.setPacketHandled(true);
    }

    public static void sendTo(ServerPlayer player, RpgPlayerData data) {
        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SyncPlayerDataPacket(data));
    }
}
