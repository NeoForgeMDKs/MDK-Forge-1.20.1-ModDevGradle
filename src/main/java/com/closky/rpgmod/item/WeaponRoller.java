package com.closky.rpgmod.item;

import com.closky.rpgmod.registry.ModItems;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

/**
 * Генератор случайного оружия для сундуков и боссов.
 *
 * <p>Именно этот класс гарантирует, что «любое оружие можно получить только из лута»:
 * предметы не крафтятся, а катаются здесь и кладутся в сундуки (см. LootModifier и
 * loot-таблицы в resources/data/rpgmod/loot_tables).</p>
 */
public final class WeaponRoller {
    private WeaponRoller() {}

    public static ItemStack roll(RandomSource random, int minLevel, int maxLevel, float honeChance) {
        ItemStack stack = ModItems.getRandomWeapon(random).getDefaultInstance();

        int level = minLevel + (maxLevel > minLevel ? random.nextInt(maxLevel - minLevel + 1) : 0);
        level = Math.max(1, Math.min(level, com.closky.rpgmod.config.RpgConfig.MAX_WEAPON_LEVEL.get()));
        WeaponData.setLevel(stack, level);
        WeaponData.setXp(stack, 0);
        WeaponData.setQuality(stack, rollQuality(random));
        WeaponData.rollAffixes(stack, random);

        if (random.nextFloat() < honeChance) {
            WeaponData.setHone(stack, rollHone(random));
        } else {
            WeaponData.setHone(stack, HoneMaterial.NONE);
        }
        return stack;
    }

    /** Уровень оружия растёт от уровня врага/сложности сундука. */
    public static ItemStack rollForSource(RandomSource random, int sourceLevel) {
        int min = Math.max(1, sourceLevel - 2);
        int max = Math.max(min, sourceLevel + 2);
        return roll(random, min, max, 0.28F);
    }

    private static WeaponQuality rollQuality(RandomSource random) {
        int total = 0;
        for (WeaponQuality q : WeaponQuality.values()) total += q.getWeight();
        int pick = random.nextInt(Math.max(1, total));
        for (WeaponQuality q : WeaponQuality.values()) {
            pick -= q.getWeight();
            if (pick < 0) return q;
        }
        return WeaponQuality.COMMON;
    }

    private static HoneMaterial rollHone(RandomSource random) {
        HoneMaterial[] values = HoneMaterial.values();
        // НЕ подковываем NONE-ом, но редкие материалы должны попадаться реже.
        int index = 1 + random.nextInt(values.length - 1);
        if (index >= values.length) index = 1;
        return values[index];
    }
}
