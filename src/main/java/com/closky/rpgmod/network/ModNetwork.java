package com.closky.rpgmod.network;

import com.closky.rpgmod.RpgMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * Сетевой канал мода.
 *
 * <p>Пакеты нужны, потому что данные игрока (уровень, статы, мана) и данные врагов
 * (уровень, максимум HP) живут на сервере, а рисовать их надо на клиенте.</p>
 *
 * <p>Версия протокола обязана совпадать у клиента и сервера, иначе игрока не пустит на сервер
 * (это нормальное поведение мода, у которого отличается логика на двух сторонах).</p>
 */
public final class ModNetwork {
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(RpgMod.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    private static int nextId = 0;

    private ModNetwork() {}

    /** Вызывается из commonSetup (см. RpgMod#commonSetup). */
    public static void register() {
        CHANNEL.registerMessage(nextId++, SyncPlayerDataPacket.class,
                SyncPlayerDataPacket::encode, SyncPlayerDataPacket::decode, SyncPlayerDataPacket::handle);
        CHANNEL.registerMessage(nextId++, SpendStatPointPacket.class,
                SpendStatPointPacket::encode, SpendStatPointPacket::decode, SpendStatPointPacket::handle);
        CHANNEL.registerMessage(nextId++, RequestPlayerDataPacket.class,
                RequestPlayerDataPacket::encode, RequestPlayerDataPacket::decode, RequestPlayerDataPacket::handle);
        CHANNEL.registerMessage(nextId++, EnemySyncPacket.class,
                EnemySyncPacket::encode, EnemySyncPacket::decode, EnemySyncPacket::handle);
    }
}
