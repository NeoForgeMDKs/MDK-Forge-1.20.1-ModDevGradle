package com.closky.rpgmod.config;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

/**
 * COMMON-конфиг мода (файл {@code rpgmod-common.toml} в папке config).
 *
 * <p>Здесь собраны все «числовые ручки» мода: скорость прокачки, бонусы за статы,
 * сила врагов по уровням и т.д. Меняя значения, вы перебалансируете мод без перекомпиляции.</p>
 *
 * <p>Значения читаются через статические поля, например {@code RpgConfig.STAT_POINT_DAMAGE.get()}.</p>
 */
public final class RpgConfig {
    public static final ForgeConfigSpec SPEC;

    // --- Прогрессия персонажа ----------------------------------------------------
    public static final ForgeConfigSpec.IntValue XP_LEVELS_PER_RPG_LEVEL;
    public static final ForgeConfigSpec.IntValue STAT_POINTS_PER_LEVEL;
    public static final ForgeConfigSpec.IntValue BONUS_STAT_POINTS_EVERY_N_LEVELS;
    public static final ForgeConfigSpec.IntValue MAX_RPG_LEVEL;

    // --- Статы -------------------------------------------------------------------
    public static final ForgeConfigSpec.DoubleValue DAMAGE_PER_STRENGTH_POINT;
    public static final ForgeConfigSpec.DoubleValue HEALTH_PER_VITALITY_POINT;
    public static final ForgeConfigSpec.DoubleValue MANA_PER_INTELLECT_POINT;
    public static final ForgeConfigSpec.DoubleValue BASE_MAX_MANA;
    public static final ForgeConfigSpec.DoubleValue MANA_REGEN_PER_SECOND;

    // --- Магия -------------------------------------------------------------------
    public static final ForgeConfigSpec.DoubleValue SPELL_POWER_PER_INTELLECT;
    public static final ForgeConfigSpec.BooleanValue SPELLS_REQUIRE_STAFF;

    // --- Оружие ------------------------------------------------------------------
    public static final ForgeConfigSpec.IntValue MAX_WEAPON_LEVEL;
    public static final ForgeConfigSpec.DoubleValue WEAPON_DAMAGE_PER_LEVEL;
    public static final ForgeConfigSpec.IntValue WEAPON_XP_PER_HIT;
    public static final ForgeConfigSpec.IntValue WEAPON_XP_PER_KILL;
    public static final ForgeConfigSpec.IntValue WEAPON_XP_BASE;
    public static final ForgeConfigSpec.BooleanValue ANVIL_HONING;

    // --- Враги -------------------------------------------------------------------
    public static final ForgeConfigSpec.BooleanValue ENEMY_LEVELS_ENABLED;
    public static final ForgeConfigSpec.IntValue ENEMY_BASE_LEVEL;
    public static final ForgeConfigSpec.DoubleValue ENEMY_LEVEL_PER_CHUNK;
    public static final ForgeConfigSpec.IntValue ENEMY_LEVEL_RANDOM_SPREAD;
    public static final ForgeConfigSpec.DoubleValue ENEMY_HEALTH_PER_LEVEL;
    public static final ForgeConfigSpec.DoubleValue ENEMY_DAMAGE_PER_LEVEL;
    public static final ForgeConfigSpec.DoubleValue LEVEL_DAMAGE_REDUCTION;
    public static final ForgeConfigSpec.DoubleValue MAX_LEVEL_DAMAGE_REDUCTION;
    public static final ForgeConfigSpec.DoubleValue PLAYER_DAMAGE_BONUS_PER_LEVEL;
    public static final ForgeConfigSpec.DoubleValue EMERALD_DROP_CHANCE_PER_LEVEL;
    public static final ForgeConfigSpec.DoubleValue WEAPON_DROP_CHANCE_PER_LEVEL;
    public static final ForgeConfigSpec.BooleanValue ENEMY_LEVELS_AFFECT_XP_DROPS;

    // --- Деревни -----------------------------------------------------------------
    public static final ForgeConfigSpec.BooleanValue VILLAGE_DEVELOPMENT_ENABLED;
    public static final ForgeConfigSpec.IntValue VILLAGE_DEVELOPMENT_INTERVAL_TICKS;
    public static final ForgeConfigSpec.IntValue VILLAGE_GROWTH_POINTS_PER_CYCLE;
    public static final ForgeConfigSpec.IntValue VILLAGE_GROWTH_POINTS_PER_BUILDING;
    public static final ForgeConfigSpec.IntValue VILLAGE_FORCED_CHUNK_TICKS;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();

        b.comment("Прокачка персонажа").push("progression");
        XP_LEVELS_PER_RPG_LEVEL = b
                .comment("Сколько уровней ванильного опыта дают 1 уровень персонажа (по ТЗ: 5)")
                .defineInRange("xpLevelsPerRpgLevel", 5, 1, 100);
        STAT_POINTS_PER_LEVEL = b
                .comment("Сколько очков характеристик даётся за каждый уровень персонажа")
                .defineInRange("statPointsPerLevel", 1, 0, 10);
        BONUS_STAT_POINTS_EVERY_N_LEVELS = b
                .comment("Каждые N уровней персонажа выдаётся дополнительное очко (0 = выключено)")
                .defineInRange("bonusStatPointsEveryNLevels", 5, 0, 100);
        MAX_RPG_LEVEL = b.comment("Максимальный уровень персонажа")
                .defineInRange("maxRpgLevel", 100, 1, 1000);
        b.pop();

        b.comment("Характеристики персонажа").push("stats");
        DAMAGE_PER_STRENGTH_POINT = b
                .comment("Прибавка к урону в ближнем бою за 1 очко силы (0.05 = +5%)")
                .defineInRange("damagePerStrengthPoint", 0.04D, 0.0D, 10.0D);
        HEALTH_PER_VITALITY_POINT = b
                .comment("Прибавка к максимальному здоровью за 1 очко живучести")
                .defineInRange("healthPerVitalityPoint", 2.0D, 0.0D, 100.0D);
        MANA_PER_INTELLECT_POINT = b
                .comment("Прибавка к максимуму маны за 1 очко интеллекта")
                .defineInRange("manaPerIntellectPoint", 10.0D, 0.0D, 1000.0D);
        BASE_MAX_MANA = b.comment("Базовый максимум маны (до вложений в интеллект)")
                .defineInRange("baseMaxMana", 50.0D, 0.0D, 100000.0D);
        MANA_REGEN_PER_SECOND = b.comment("Восстановление маны в секунду")
                .defineInRange("manaRegenPerSecond", 1.0D, 0.0D, 1000.0D);
        b.pop();

        b.comment("Магия").push("magic");
        SPELL_POWER_PER_INTELLECT = b
                .comment("Прибавка к силе заклинаний за 1 очко интеллекта (0.01 = +1%)")
                .defineInRange("spellPowerPerIntellect", 0.015D, 0.0D, 10.0D);
        SPELLS_REQUIRE_STAFF = b
                .comment("Разрешать каст заклинаний только с посохом в руке")
                .define("spellsRequireStaff", true);
        b.pop();

        b.comment("Оружие").push("weapons");
        MAX_WEAPON_LEVEL = b.comment("Максимальный уровень оружия")
                .defineInRange("maxWeaponLevel", 20, 1, 100);
        WEAPON_DAMAGE_PER_LEVEL = b
                .comment("Прибавка к урону оружия за каждый его уровень")
                .defineInRange("weaponDamagePerLevel", 0.5D, 0.0D, 100.0D);
        WEAPON_XP_PER_HIT = b.comment("Опыт оружия за попадание")
                .defineInRange("weaponXpPerHit", 1, 0, 1000);
        WEAPON_XP_PER_KILL = b.comment("Опыт оружия за убийство")
                .defineInRange("weaponXpPerKill", 8, 0, 10000);
        WEAPON_XP_BASE = b.comment("Базовый опыт, нужный для уровня оружия (умножается на уровень)")
                .defineInRange("weaponXpBase", 25, 1, 100000);
        ANVIL_HONING = b.comment("Разрешить подковку оружия материалами на наковальне")
                .define("anvilHoning", true);
        b.pop();

        b.comment("Враги").push("enemies");
        ENEMY_LEVELS_ENABLED = b.comment("Включить систему уровней врагов")
                .define("enemyLevelsEnabled", true);
        ENEMY_BASE_LEVEL = b.comment("Уровень врагов рядом с точкой спавна мира")
                .defineInRange("enemyBaseLevel", 1, 1, 1000);
        ENEMY_LEVEL_PER_CHUNK = b
                .comment("На сколько уровней растёт сложность за каждый чанк удаления от спавна мира")
                .defineInRange("enemyLevelPerChunk", 0.06D, 0.0D, 10.0D);
        ENEMY_LEVEL_RANDOM_SPREAD = b
                .comment("Случайный разброс уровней врагов при спавне (±)")
                .defineInRange("enemyLevelRandomSpread", 2, 0, 100);
        ENEMY_HEALTH_PER_LEVEL = b
                .comment("Прибавка к здоровью врага за каждый уровень (0.08 = +8% от базового)")
                .defineInRange("enemyHealthPerLevel", 0.08D, 0.0D, 10.0D);
        ENEMY_DAMAGE_PER_LEVEL = b
                .comment("Прибавка к урону врага за каждый уровень")
                .defineInRange("enemyDamagePerLevel", 0.05D, 0.0D, 10.0D);
        LEVEL_DAMAGE_REDUCTION = b
                .comment("Насколько процентов враг получает меньше урона за разницу в уровнях (0.07 = 7%)")
                .defineInRange("levelDamageReduction", 0.07D, 0.0D, 1.0D);
        MAX_LEVEL_DAMAGE_REDUCTION = b
                .comment("Максимальное снижение урона по врагу из-за разницы уровней (0.8 = 80%)")
                .defineInRange("maxLevelDamageReduction", 0.8D, 0.0D, 1.0D);
        PLAYER_DAMAGE_BONUS_PER_LEVEL = b
                .comment("Бонус урона игрока за каждый уровень ПЕРСОНАЖА выше уровня врага")
                .defineInRange("playerDamageBonusPerLevel", 0.03D, 0.0D, 10.0D);
        EMERALD_DROP_CHANCE_PER_LEVEL = b
                .comment("Шанс выпадения изумруда с врага (умножается на уровень врага)")
                .defineInRange("emeraldDropChancePerLevel", 0.02D, 0.0D, 1.0D);
        WEAPON_DROP_CHANCE_PER_LEVEL = b
                .comment("Шанс выпадения оружия из сундучного пула с врага (умножается на уровень врага)")
                .defineInRange("weaponDropChancePerLevel", 0.002D, 0.0D, 1.0D);
        ENEMY_LEVELS_AFFECT_XP_DROPS = b
                .comment("Давать больше ванильного опыта за врагов высокого уровня")
                .define("enemyLevelsAffectXpDrops", true);
        b.pop();

        b.comment("Деревни").push("villages");
        VILLAGE_DEVELOPMENT_ENABLED = b
                .comment("Включить самостоятельное развитие деревень (жители строят даже без игрока рядом)")
                .define("villageDevelopmentEnabled", true);
        VILLAGE_DEVELOPMENT_INTERVAL_TICKS = b
                .comment("Как часто (в тиках) сервер делает один шаг развития деревень")
                .defineInRange("villageDevelopmentIntervalTicks", 600, 20, 72000);
        VILLAGE_GROWTH_POINTS_PER_CYCLE = b
                .comment("Сколько очков роста деревня получает за цикл на каждые 4 жителя")
                .defineInRange("villageGrowthPointsPerCycle", 1, 0, 100);
        VILLAGE_GROWTH_POINTS_PER_BUILDING = b
                .comment("Сколько очков роста нужно на одну новую постройку")
                .defineInRange("villageGrowthPointsPerBuilding", 24, 1, 10000);
        VILLAGE_FORCED_CHUNK_TICKS = b
                .comment("На сколько тиков удерживать чанки деревни загруженными при постройке")
                .defineInRange("villageForcedChunkTicks", 100, 20, 20000);
        b.pop();

        SPEC = b.build();
    }

    private RpgConfig() {}
}
