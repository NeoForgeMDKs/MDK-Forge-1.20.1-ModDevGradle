package com.closky.rpgmod.item;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Rarity;

/**
 * Качество (редкость) выпавшего оружия. Определяет, сколько случайных бонусов («аффиксов»)
 * получит предмет и как будет выглядеть его название.
 */
public enum WeaponQuality {
    COMMON("common", "Обычное", ChatFormatting.WHITE, Rarity.COMMON, 0, 12),
    UNCOMMON("uncommon", "Необычное", ChatFormatting.GREEN, Rarity.UNCOMMON, 1, 8),
    RARE("rare", "Редкое", ChatFormatting.BLUE, Rarity.RARE, 2, 5),
    EPIC("epic", "Эпическое", ChatFormatting.DARK_PURPLE, Rarity.EPIC, 3, 2),
    LEGENDARY("legendary", "Легендарное", ChatFormatting.GOLD, Rarity.EPIC, 4, 1);

    private final String id;
    private final String displayName;
    private final ChatFormatting color;
    private final Rarity rarity;
    private final int affixRolls;
    private final int weight;

    WeaponQuality(String id, String displayName, ChatFormatting color, Rarity rarity, int affixRolls, int weight) {
        this.id = id;
        this.displayName = displayName;
        this.color = color;
        this.rarity = rarity;
        this.affixRolls = affixRolls;
        this.weight = weight;
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

    public Rarity getRarity() {
        return rarity;
    }

    /** Сколько аффиксов катается при генерации предмета. */
    public int getAffixRolls() {
        return affixRolls;
    }

    /** Вес для случайного выбора (чем больше, тем чаще выпадает). */
    public int getWeight() {
        return weight;
    }

    public String getTranslationKey() {
        return "weapon_quality.rpgmod." + id;
    }

    public static WeaponQuality byId(String id) {
        for (WeaponQuality q : values()) {
            if (q.id.equals(id)) return q;
        }
        return COMMON;
    }
}
