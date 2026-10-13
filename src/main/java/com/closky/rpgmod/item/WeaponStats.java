package com.closky.rpgmod.item;

/**
 * Итоговые характеристики оружия (record — просто неизменяемый контейнер).
 * Считается в {@link WeaponData#getStats(net.minecraft.world.item.ItemStack)}.
 */
public record WeaponStats(
        double damage,
        double attackSpeed,
        double reach,
        double critChance,
        double critMultiplier,
        double lifesteal,
        double armor,
        double bonusMana,
        double spellPower,
        int frostDamage,
        int fireDamage,
        int lightningDamage,
        int verdant) {

    public static final WeaponStats EMPTY = new WeaponStats(0, 0, 0, 0, 1.0D, 0, 0, 0, 0, 0, 0, 0, 0);
}
