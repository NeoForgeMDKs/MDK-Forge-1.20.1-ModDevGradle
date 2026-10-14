package com.closky.rpgmod.player;

import net.minecraft.ChatFormatting;

/**
 * Класс персонажа. Определяется НАДЕТЫМ ПОЛНЫМ СЕТОМ брони (как в Terraria:
 * собрал другой сет — сменил класс без потери прогресса).
 */
public enum PlayerClass {
    NONE("none", "Без класса", ChatFormatting.GRAY, 0xFFFFFF,
            0.0F, 0.0F, 0.0F, 0.0F, 0.0F),
    WARRIOR("warrior", "Воин", ChatFormatting.RED, 0xC0392B,
            1.15F, 0.0F, 0.0F, 0.0F, 4.0F),
    RANGER("ranger", "Стрелок", ChatFormatting.GREEN, 0x27AE60,
            1.0F, 0.20F, 0.08F, 0.0F, 1.0F),
    MAGE("mage", "Маг", ChatFormatting.LIGHT_PURPLE, 0x8E44AD,
            1.0F, 0.0F, 0.0F, 40.0F, 0.0F),
    DRUID("druid", "Друид", ChatFormatting.AQUA, 0x16A085,
            1.05F, 0.05F, 0.0F, 15.0F, 2.0F);

    private final String id;
    private final String displayName;
    private final ChatFormatting color;
    private final int tint;
    private final float damageMultiplier;
    private final float speedBonus;
    private final float attackSpeedBonus;
    private final float bonusMana;
    private final float bonusArmor;

    PlayerClass(String id, String displayName, ChatFormatting color, int tint, float damageMultiplier,
                float speedBonus, float attackSpeedBonus, float bonusMana, float bonusArmor) {
        this.id = id;
        this.displayName = displayName;
        this.color = color;
        this.tint = tint;
        this.damageMultiplier = damageMultiplier;
        this.speedBonus = speedBonus;
        this.attackSpeedBonus = attackSpeedBonus;
        this.bonusMana = bonusMana;
        this.bonusArmor = bonusArmor;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public ChatFormatting getColor() {
        return color;
    }

    /** Цвет тонировки брони (используется в RegisterColorHandlersEvent.Item). */
    public int getTint() {
        return tint;
    }

    public float getDamageMultiplier() {
        return damageMultiplier;
    }

    public float getSpeedBonus() {
        return speedBonus;
    }

    public float getAttackSpeedBonus() {
        return attackSpeedBonus;
    }

    public float getBonusMana() {
        return bonusMana;
    }

    public float getBonusArmor() {
        return bonusArmor;
    }

    public String getTranslationKey() {
        return "class.rpgmod." + id;
    }

    public String getBonusKey() {
        return "class.rpgmod." + id + ".bonus";
    }

    public static PlayerClass byId(String id) {
        for (PlayerClass value : values()) {
            if (value.id.equals(id)) return value;
        }
        return NONE;
    }
}
