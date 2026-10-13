package com.closky.rpgmod.enemy;

import com.closky.rpgmod.config.RpgConfig;
import com.closky.rpgmod.player.RpgPlayerData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Уровни врагов (требование 5а) и связанные с ними бонусы.
 *
 * <p>Уровень хранится в NBT моба (ForgeData) — он сохраняется вместе с мобом, поэтому
 * «зомби 7 уровня» останется семиуровневым и после перезагрузки мира.</p>
 */
public final class EnemyLevelHelper {
    public static final String TAG_LEVEL = "rpg_enemy_level";

    private static final UUID UUID_HEALTH = uuid("enemy_level_health");
    private static final UUID UUID_DAMAGE = uuid("enemy_level_damage");

    private EnemyLevelHelper() {}

    public static int getLevel(LivingEntity entity) {
        CompoundTag tag = entity.getPersistentData();
        return tag.getInt(TAG_LEVEL);
    }

    public static void setLevel(LivingEntity entity, int level) {
        entity.getPersistentData().putInt(TAG_LEVEL, level);
    }

    /** Кого вообще можно «прокачивать»: враждебные мобы. */
    public static boolean isScalable(Entity entity) {
        return entity instanceof LivingEntity && entity instanceof Enemy;
    }

    /**
     * Случайный уровень моба по его позиции: чем дальше от точки спавна мира, тем опаснее.
     * Формулу легко перенастроить в конфиге (enemyBaseLevel, enemyLevelPerChunk, ...).
     */
    public static int rollLevel(ServerLevel level, double x, double z, RandomSource random) {
        int base = RpgConfig.ENEMY_BASE_LEVEL.get();
        double perChunk = RpgConfig.ENEMY_LEVEL_PER_CHUNK.get();
        net.minecraft.core.BlockPos spawn = level.getSharedSpawnPos();
        double dx = x - spawn.getX();
        double dz = z - spawn.getZ();
        double distanceChunks = Math.sqrt(dx * dx + dz * dz) / 16.0D;
        int spread = RpgConfig.ENEMY_LEVEL_RANDOM_SPREAD.get();
        int level = base + (int) (distanceChunks * perChunk);
        if (spread > 0) {
            level += random.nextInt(spread * 2 + 1) - spread;
        }
        return Math.max(1, Math.min(level, RpgConfig.MAX_RPG_LEVEL.get()));
    }

    /** Проставить уровень и пересчитать атрибуты моба. */
    public static void applyLevel(LivingEntity entity, int level) {
        setLevel(entity, level);

        AttributeInstance health = entity.getAttribute(Attributes.MAX_HEALTH);
        if (health != null) {
            health.removeModifier(UUID_HEALTH);
            double bonus = level * RpgConfig.ENEMY_HEALTH_PER_LEVEL.get();
            if (bonus > 0.0D) {
                health.addPermanentModifier(new AttributeModifier(UUID_HEALTH, "rpgmod:enemy_health", bonus,
                        AttributeModifier.Operation.MULTIPLY_TOTAL));
            }
            entity.setHealth(entity.getMaxHealth());
        }

        AttributeInstance damage = entity.getAttribute(Attributes.ATTACK_DAMAGE);
        if (damage != null) {
            damage.removeModifier(UUID_DAMAGE);
            double bonus = level * RpgConfig.ENEMY_DAMAGE_PER_LEVEL.get();
            if (bonus > 0.0D) {
                damage.addPermanentModifier(new AttributeModifier(UUID_DAMAGE, "rpgmod:enemy_damage", bonus,
                        AttributeModifier.Operation.MULTIPLY_TOTAL));
            }
        }
    }

    /**
     * Множитель урона игрока по врагу с учётом разницы уровней (требование 5в).
     * Если враг выше уровнем — урон режется, если ниже — игрок бьёт сильнее.
     */
    public static float getLevelDamageMultiplier(int playerLevel, int enemyLevel) {
        int diff = enemyLevel - playerLevel;
        if (diff > 0) {
            double reduction = Math.min(RpgConfig.MAX_LEVEL_DAMAGE_REDUCTION.get(),
                    diff * RpgConfig.LEVEL_DAMAGE_REDUCTION.get());
            return (float) Math.max(0.05D, 1.0D - reduction);
        } else if (diff < 0) {
            double bonus = Math.min(2.0D, -diff * RpgConfig.PLAYER_DAMAGE_BONUS_PER_LEVEL.get());
            return (float) (1.0D + bonus);
        }
        return 1.0F;
    }

    public static int getPlayerLevel(Player player) {
        return RpgPlayerData.get(player).getLevel();
    }

    /** Отображаемое имя врага: "Lv.5 Зомби". */
    public static net.minecraft.network.chat.Component getDisplayNameWithLevel(LivingEntity entity) {
        int level = getLevel(entity);
        if (level <= 0) return entity.getName();
        return net.minecraft.network.chat.Component.translatable("name.rpgmod.enemy_level",
                net.minecraft.network.chat.Component.literal(String.valueOf(level)), entity.getName());
    }

    private static UUID uuid(String name) {
        return UUID.nameUUIDFromBytes(("rpgmod:" + name).getBytes(StandardCharsets.UTF_8));
    }
}
