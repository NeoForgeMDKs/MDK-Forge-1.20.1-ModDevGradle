package com.closky.rpgmod.config;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * CLIENT-конфиг (файл {@code rpgmod-client.toml}).
 *
 * <p>Настройки интерфейса. Именно этот конфиг открывается кнопкой «Config» в списке модов
 * (Mods -> RPG Overhaul -> Config) прямо в игре, поэтому требование «можно отключить
 * отображение HP в настройках мода» выполняется без написания собственного экрана.</p>
 */
public final class RpgClientConfig {
    public static final ForgeConfigSpec SPEC;

    /** Показывать над враждебными мобами их уровень. */
    public static final ForgeConfigSpec.BooleanValue SHOW_ENEMY_LEVEL;
    /** Показывать над враждебными мобами полоску/цифры здоровья. */
    public static final ForgeConfigSpec.BooleanValue SHOW_ENEMY_HEALTH;
    /** Показывать полоску маны на HUD. */
    public static final ForgeConfigSpec.BooleanValue SHOW_MANA_BAR;
    /** Показывать надёванный «класс» (полный сет) в HUD. */
    public static final ForgeConfigSpec.BooleanValue SHOW_PLAYER_CLASS;
    /** Показывать опыт оружия в подсказке предмета. */
    public static final ForgeConfigSpec.BooleanValue SHOW_WEAPON_XP;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
        b.comment("Интерфейс").push("hud");
        SHOW_ENEMY_LEVEL = b.comment("Отображать уровень врага над его головой")
                .define("showEnemyLevel", true);
        SHOW_ENEMY_HEALTH = b.comment("Отображать здоровье врага над его головой")
                .define("showEnemyHealth", true);
        SHOW_MANA_BAR = b.comment("Отображать полоску маны")
                .define("showManaBar", true);
        SHOW_PLAYER_CLASS = b.comment("Отображать текущий класс персонажа (по полному сету брони)")
                .define("showPlayerClass", true);
        SHOW_WEAPON_XP = b.comment("Показывать прогресс уровня оружия в подсказке")
                .define("showWeaponXp", true);
        b.pop();
        SPEC = b.build();
    }

    private RpgClientConfig() {}
}
