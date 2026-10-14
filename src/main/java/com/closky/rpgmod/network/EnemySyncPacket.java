package com.closky.rpgmod.network;

import com.closky.rpgmod.enemy.EnemyLevelHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

/**
 * Сервер -> клиент: «у сущности с таким id уровень такой-то».
 * Нужен для таблички с уровнем и HP над головой моба (требование 5г).
 */
public class EnemySyncPacket {
    private final int entityId;
    private final int level;
    private final float maxHealth;

    public EnemySyncPacket(int entityId, int level, float maxHealth) {
        this.entityId = entityId;
        this.level = level;
        this.maxHealth = maxHealth;
    }

    public static EnemySyncPacket forEntity(LivingEntity entity) {
        return new EnemySyncPacket(entity.getId(), EnemyLevelHelper.getLevel(entity), entity.getMaxHealth());
    }

    public static void encode(EnemySyncPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.entityId);
        buf.writeVarInt(msg.level);
        buf.writeFloat(msg.maxHealth);
    }

    public static EnemySyncPacket decode(FriendlyByteBuf buf) {
        return new EnemySyncPacket(buf.readVarInt(), buf.readVarInt(), buf.readFloat());
    }

    public static void handle(EnemySyncPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> com.closky.rpgmod.client.ClientPacketHandler.handleEnemySync(msg));
        ctx.setPacketHandled(true);
    }

    public int getEntityId() {
        return entityId;
    }

    public int getLevel() {
        return level;
    }

    public float getMaxHealth() {
        return maxHealth;
    }

    /** Отправить конкретному игроку (например, когда он впервые видит моба). */
    public static void sendTo(ServerPlayer player, LivingEntity entity) {
        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), forEntity(entity));
    }

    /** Отправить всем, кто сейчас видит моба (например, после удара по нему). */
    public static void sendToTracking(LivingEntity entity) {
        if (entity.level().isClientSide()) return;
        ModNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), forEntity(entity));
    }
}
