package com.closky.rpgmod.enemy;

import com.closky.rpgmod.RpgMod;
import com.closky.rpgmod.config.RpgConfig;
import com.closky.rpgmod.entity.DungeonWarden;
import com.closky.rpgmod.item.HoneMaterial;
import com.closky.rpgmod.item.RpgWeaponItem;
import com.closky.rpgmod.item.WeaponAffix;
import com.closky.rpgmod.item.WeaponData;
import com.closky.rpgmod.item.WeaponQuality;
import com.closky.rpgmod.item.WeaponRoller;
import com.closky.rpgmod.network.EnemySyncPacket;
import com.closky.rpgmod.player.RpgPlayerData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Логика врагов: назначение уровня при спавне, пересчёт урона по разнице уровней,
 * лут с мобов (изумруды, оружие) и синхронизация уровня с клиентом.
 */
@Mod.EventBusSubscriber(modid = RpgMod.MODID)
public class EnemyHandler {

    /** Назначение уровня при появлении моба. */
    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (!RpgConfig.ENEMY_LEVELS_ENABLED.get()) return;
        if (event.getLevel().isClientSide()) return;
        if (event.loadedFromDisk()) return; // моб уже с уровнем, ничего не трогаем
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;
        if (!(event.getEntity() instanceof LivingEntity living) || !EnemyLevelHelper.isScalable(living)) return;
        if (EnemyLevelHelper.getLevel(living) > 0) return;

        int rolled = EnemyLevelHelper.rollLevel(serverLevel, living.getX(), living.getZ(), serverLevel.random);
        EnemyLevelHelper.applyLevel(living, rolled);
    }

    /** Когда игрок «увидел» моба — отправляем ему уровень и HP для полоски над головой. */
    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof LivingEntity living && event.getEntity() instanceof ServerPlayer player) {
            EnemySyncPacket.sendTo(player, living);
        }
    }

    /** Урон игрока по врагу с поправкой на разницу уровней. */
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity target = event.getEntity();
        int enemyLevel = EnemyLevelHelper.getLevel(target);
        if (enemyLevel <= 0) return;

        if (event.getSource().getEntity() instanceof ServerPlayer player) {
            int playerLevel = EnemyLevelHelper.getPlayerLevel(player);
            float multiplier = EnemyLevelHelper.getLevelDamageMultiplier(playerLevel, enemyLevel);
            event.setAmount(event.getAmount() * multiplier);
            if (multiplier < 0.95F && !target.level().isClientSide) {
                player.displayClientMessage(Component.translatable("msg.rpgmod.too_strong",
                        Component.literal(String.valueOf(enemyLevel)),
                        Component.literal(String.valueOf(playerLevel))).withStyle(ChatFormatting.DARK_RED), true);
            }
        }
        if (event.getSource().getEntity() instanceof ServerPlayer || event.getSource().getDirectEntity() instanceof ServerPlayer) {
            EnemySyncPacket.sendToTracking(target);
        }
    }

    /** Опыт оружия за убийство и «живое» оружие (аффикс VERDANT). */
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity killed = event.getEntity();
        if (killed.level().isClientSide) return;
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;

        ItemStack weapon = player.getMainHandItem();
        if (weapon.getItem() instanceof RpgWeaponItem) {
            WeaponData.addXp(weapon, RpgConfig.WEAPON_XP_PER_KILL.get(), player);
            int verdant = WeaponData.getAffixRoll(weapon, WeaponAffix.VERDANT);
            if (verdant > 0) {
                player.heal(verdant * 2.0F);
            }
        }
    }

    /** Лут с врагов: изумруды и иногда оружие (требование 5б). */
    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity.level() instanceof ServerLevel level)) return;

        boolean isBoss = entity instanceof DungeonWarden;
        int enemyLevel = EnemyLevelHelper.getLevel(entity);
        if (enemyLevel <= 0 && !isBoss) return;

        RandomSource random = level.getRandom();
        int effectiveLevel = Math.max(1, enemyLevel);

        // Изумруды
        double emeraldChance = RpgConfig.EMERALD_DROP_CHANCE_PER_LEVEL.get() * effectiveLevel;
        if (isBoss) emeraldChance = 1.0D;
        if (random.nextDouble() < emeraldChance) {
            int count = 1 + random.nextInt(Math.max(1, effectiveLevel / 4));
            if (isBoss) count = 12 + random.nextInt(10);
            addDrop(level, entity, new ItemStack(Items.EMERALD, count));
        }

        // Оружие
        double weaponChance = isBoss ? 1.0D : RpgConfig.WEAPON_DROP_CHANCE_PER_LEVEL.get() * effectiveLevel;
        if (random.nextDouble() < weaponChance) {
            ItemStack weapon = WeaponRoller.roll(random, Math.max(1, effectiveLevel - 1),
                    effectiveLevel + 3, isBoss ? 1.0F : 0.15F);
            if (isBoss) {
                // С босса падает гарантированно хорошее оружие (требование 2з)
                WeaponData.setQuality(weapon, WeaponQuality.LEGENDARY);
                WeaponData.setHone(weapon, HoneMaterial.NETHERITE);
                WeaponData.rollAffixes(weapon, random);
            }
            addDrop(level, entity, weapon);
        }
    }

    private static void addDrop(ServerLevel level, LivingEntity entity, ItemStack stack) {
        ItemEntity itemEntity = new ItemEntity(level, entity.getX(), entity.getY() + 0.5D, entity.getZ(), stack);
        itemEntity.setDefaultPickUpDelay();
        level.addFreshEntity(itemEntity);
    }
}
