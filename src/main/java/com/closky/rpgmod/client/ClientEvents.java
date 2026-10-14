package com.closky.rpgmod.client;

import com.closky.rpgmod.RpgMod;
import com.closky.rpgmod.network.ModNetwork;
import com.closky.rpgmod.network.RequestPlayerDataPacket;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

/**
 * Клиентские события жизненного цикла мира.
 *
 * <p>При входе в мир просим у сервера свои данные (уровень, статы, ману) и чистим кэши,
 * при выходе — чистим кэши, чтобы на новом мире не остались «чужие» цифры.</p>
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = RpgMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ClientEvents {
    private ClientEvents() {}

    @SubscribeEvent
    public static void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        ClientEnemyData.clear();
        ClientPlayerData.reset();
        // «сервер, пришли мои актуальные статы» — на случай, если игрок зашёл позже синхронизации
        ModNetwork.CHANNEL.sendToServer(new RequestPlayerDataPacket());
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel().isClientSide()) {
            ClientEnemyData.clear();
            ClientPlayerData.reset();
        }
    }
}
