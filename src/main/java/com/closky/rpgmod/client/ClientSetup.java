package com.closky.rpgmod.client;

import com.closky.rpgmod.RpgMod;
import com.closky.rpgmod.registry.ModEntities;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Регистрация всего клиентского (модели сущностей, HUD).
 *
 * <p>Класс помечен {@code Dist.CLIENT} — на выделенном сервере он вообще не загружается,
 * поэтому графические классы не могут уронить сервер.</p>
 */
@Mod.EventBusSubscriber(modid = RpgMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ClientSetup {
    private ClientSetup() {}

    /** HUD: полоска маны + уровень персонажа. */
    @SubscribeEvent
    public static void onRegisterGuiOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("mana_bar", ManaHudOverlay.INSTANCE);
    }

    /**
     * Модели (рендереры) наших сущностей.
     *
     * <p>ВАЖНО: если зарегистрировать сущность, но не зарегистрировать её рендерер,
     * клиент падает при первом появлении сущности в мире. Поэтому все три здесь.</p>
     */
    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.SPELL_PROJECTILE.get(), SpellProjectileRenderer::new);
        event.registerEntityRenderer(ModEntities.DUNGEON_WARDEN.get(), DungeonWardenRenderer::new);
        event.registerEntityRenderer(ModEntities.CAVE_STALKER.get(), CaveStalkerRenderer::new);
    }
}
