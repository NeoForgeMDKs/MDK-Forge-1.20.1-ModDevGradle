package com.closky.rpgmod.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/**
 * Случайный бонус («аффикс») на оружии. Именно они создают разницу между двумя мечами
 * одного класса и уровня.
 */
public enum WeaponAffix {
    SHARP("sharp", "Заточенное", ChatFormatting.RED, 3, "Урон"),
    SWIFT("swift", "Стремительное", ChatFormatting.YELLOW, 3, "Скорость атаки"),
    VAMPIRIC("vampiric", "Вампирическое", ChatFormatting.DARK_RED, 3, "Вампиризм"),
    RUNIC("runic", "Руническое", ChatFormatting.LIGHT_PURPLE, 3, "Мана и сила магии"),
    REACHING("reaching", "Длинное", ChatFormatting.GRAY, 3, "Дальность атаки"),
    STALWART("stalwart", "Крепкое", ChatFormatting.DARK_GRAY, 3, "Броня"),
    FROSTBOUND("frostbound", "Ледяное", ChatFormatting.AQUA, 3, "Урон льдом"),
    EMBERFORGED("emberforged", "Пламенное", ChatFormatting.GOLD, 3, "Урон огнём"),
    STORMBOUND("stormbound", "Громовое", ChatFormatting.BLUE, 3, "Урон молнией"),
    VERDANT("verdant", "Живое", ChatFormatting.GREEN, 3, "Лечение при убийстве");

    private final String id;
    private final String displayName;
    private final ChatFormatting color;
    private final int maxRoll;
    private final String bonusName;

    WeaponAffix(String id, String displayName, ChatFormatting color, int maxRoll, String bonusName) {
        this.id = id;
        this.displayName = displayName;
        this.color = color;
        this.maxRoll = maxRoll;
        this.bonusName = bonusName;
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

    public int getMaxRoll() {
        return maxRoll;
    }

    public String getTranslationKey() {
        return "weapon_affix.rpgmod." + id;
    }

    /** Текст бонуса для подсказки предмета, например "Заточенное II: +1.5 урона". */
    public MutableComponent describe(int roll) {
        return Component.translatable("tooltip.rpgmod.affix", Component.translatable(getTranslationKey()),
                RomanNumbers.of(roll), bonusName);
    }

    public static WeaponAffix byId(String id) {
        for (WeaponAffix a : values()) {
            if (a.id.equals(id)) return a;
        }
        return SHARP;
    }
}
