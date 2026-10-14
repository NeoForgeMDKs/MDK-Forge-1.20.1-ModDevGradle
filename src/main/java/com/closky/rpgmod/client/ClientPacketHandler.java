package com.closky.rpgmod.client;

import com.closky.rpgmod.network.EnemySyncPacket;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Клиентская обработка пакетов — вынесена в отдельный класс, чтобы серверная часть
 * (EnemySyncPacket) не загружала графические классы на выделенном сервере.
 *
 * <p>Прогресс самого игрока обрабатывается прямо в {@code SyncPlayerDataPacket}
 * через {@link ClientPlayerData#set(int, int, int, int, int, float, float)}.</p>
 */
@OnlyIn(Dist.CLIENT)
public final class ClientPacketHandler {
    private ClientPacketHandler() {}

    public static void handleEnemySync(EnemySyncPacket msg) {
        ClientEnemyData.setLevel(msg.getEntityId(), msg.getLevel());
        ClientEnemyData.setMaxHealth(msg.getEntityId(), msg.getMaxHealth());
    }
}
