package com.closky.rpgmod.player;

import com.closky.rpgmod.RpgMod;
import com.closky.rpgmod.config.RpgConfig;
import com.closky.rpgmod.network.SyncPlayerDataPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Прокачка персонажа: уровни от ванильного опыта, восстановление маны, применение статов.
 *
 * <p>Требование 4а: 5 ванильных уровней опыта = 1 уровень персонажа. Отслеживаем это раз в
 * секунду на сервере — так нельзя «потерять» уровень даже при смерти (мы храним максимальный).</p>
 */
@Mod.EventBusSubscriber(modid = RpgMod.MODID)
public class PlayerProgressionHandler {
    public static final UUID UUID_VITALITY = uuid("stat_vitality");
    public static final UUID UUID_STRENGTH = uuid("stat_strength");

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;
        if (player.tickCount % 20 != 0) return;

        RpgPlayerData data = RpgPlayerData.get(player);

        // Восстановление маны
        if (data.getMana() < data.getMaxMana()) {
            data.addMana(RpgConfig.MANA_REGEN_PER_SECOND.get().floatValue());
        }

        // Повышение уровня за ванильный опыт
        if (data.checkExperienceLevelUp()) {
            onLevelUp(player, data);
        }

        // Синхронизация маны с клиентом (HUD), но не чаще раза в секунду и только при изменении
        if (Math.abs(data.getMana() - data.getLastSyncedMana()) >= 0.5F) {
            data.setLastSyncedMana(data.getMana());
            SyncPlayerDataPacket.sendTo(player, data);
        }
    }

    private static void onLevelUp(ServerPlayer player, RpgPlayerData data) {
        player.displayClientMessage(Component.translatable("msg.rpgmod.level_up",
                Component.literal(String.valueOf(data.getLevel())).withStyle(ChatFormatting.GOLD),
                Component.literal(String.valueOf(data.getStatPoints())).withStyle(ChatFormatting.YELLOW)), false);
        player.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.0F);
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 200, 1));
        applyStatBonuses(player);
        SyncPlayerDataPacket.sendTo(player, data);
    }

    /** Применить бонусы от вложенных очков (живучесть -> HP, сила -> урон). */
    public static void applyStatBonuses(ServerPlayer player) {
        RpgPlayerData data = RpgPlayerData.get(player);

        AttributeInstance health = player.getAttribute(Attributes.MAX_HEALTH);
        if (health != null) {
            health.removeModifier(UUID_VITALITY);
            double bonus = data.getSpentVitality() * RpgConfig.HEALTH_PER_VITALITY_POINT.get();
            if (bonus > 0.0D) {
                health.addTransientModifier(new AttributeModifier(UUID_VITALITY, "rpgmod:vitality", bonus,
                        AttributeModifier.Operation.ADDITION));
            }
        }

        AttributeInstance damage = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (damage != null) {
            damage.removeModifier(UUID_STRENGTH);
            double bonus = data.getSpentStrength() * RpgConfig.DAMAGE_PER_STRENGTH_POINT.get();
            if (bonus > 0.0D) {
                damage.addTransientModifier(new AttributeModifier(UUID_STRENGTH, "rpgmod:strength", bonus,
                        AttributeModifier.Operation.MULTIPLY_TOTAL));
            }
        }
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            RpgPlayerData.copyOnRespawn(event.getOriginal(), event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            applyStatBonuses(player);
            RpgPlayerData data = RpgPlayerData.get(player);
            data.setMana(data.getMaxMana());
            SyncPlayerDataPacket.sendTo(player, data);
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            applyStatBonuses(player);
            SyncPlayerDataPacket.sendTo(player, RpgPlayerData.get(player));
        }
    }

    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            SyncPlayerDataPacket.sendTo(player, RpgPlayerData.get(player));
        }
    }

    private static UUID uuid(String name) {
        return UUID.nameUUIDFromBytes(("rpgmod:" + name).getBytes(StandardCharsets.UTF_8));
    }
}
