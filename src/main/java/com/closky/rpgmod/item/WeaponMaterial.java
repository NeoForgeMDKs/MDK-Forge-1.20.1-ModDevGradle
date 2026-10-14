package com.closky.rpgmod.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tiers;

import java.util.function.Supplier;

/**
 * Материал основы оружия. Определяет базовый урон, прочность и цвет.
 * Уровень/подковка/качества добавляются сверху (см. {@link WeaponData}).
 */
public enum WeaponMaterial {
    STEEL("steel", "Сталь", 6.0F, 320, Tiers.IRON.getLevel(), 0xC2CBD6, () -> Items.IRON_INGOT),
    SILVER("silver", "Серебро", 5.5F, 260, Tiers.IRON.getLevel(), 0xEDF3FA, () -> Items.IRON_INGOT),
    DUSKSTEEL("dusksteel", "Сумеречная сталь", 7.5F, 520, Tiers.DIAMOND.getLevel(), 0x7A5BC7, () -> Items.DIAMOND);

    private final String id;
    private final String displayName;
    private final float baseDamage;
    private final int durability;
    private final int tierLevel;
    private final int tint;
    private final Supplier<Item> repairItem;

    WeaponMaterial(String id, String displayName, float baseDamage, int durability, int tierLevel, int tint,
                   Supplier<Item> repairItem) {
        this.id = id;
        this.displayName = displayName;
        this.baseDamage = baseDamage;
        this.durability = durability;
        this.tierLevel = tierLevel;
        this.tint = tint;
        this.repairItem = repairItem;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public float getBaseDamage() {
        return baseDamage;
    }

    public int getDurability() {
        return durability;
    }

    public int getTierLevel() {
        return tierLevel;
    }

    /** Цвет тонировки текстуры оружия (RegisterColorHandlersEvent.Item). */
    public int getTint() {
        return tint;
    }

    public Item getRepairItem() {
        return repairItem.get();
    }

    public String getTranslationKey() {
        return "weapon_material.rpgmod." + id;
    }
}
