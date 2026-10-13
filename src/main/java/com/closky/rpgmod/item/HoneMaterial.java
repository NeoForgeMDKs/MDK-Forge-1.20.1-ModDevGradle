package com.closky.rpgmod.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.function.Supplier;

/**
 * Материал подковки оружия (наковальня + материал = бонус).
 *
 * <p>Каждый материал даёт РАЗНЫЙ набор бонусов — это и есть требование «каждый материал
 * даёт свой бонус».</p>
 */
public enum HoneMaterial {
    NONE("none", "-", () -> Items.AIR, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0, 0),
    IRON("iron", "Железо", () -> Items.IRON_INGOT, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0, 0),
    GOLD("gold", "Золото", () -> Items.GOLD_INGOT, 0.0F, 0.20F, 0.02F, 0.0F, 0.0F, 0.0F, 0, 0),
    DIAMOND("diamond", "Алмаз", () -> Items.DIAMOND, 2.0F, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0, 0),
    NETHERITE("netherite", "Незерит", () -> Items.NETHERITE_INGOT, 3.0F, 0.0F, 0.0F, 0.03F, 1.5F, 0.0F, 0, 0),
    AMETHYST("amethyst", "Аметист", () -> Items.AMETHYST_SHARD, 0.0F, 0.0F, 0.08F, 0.0F, 0.0F, 15.0F, 0, 0),
    FROST("frost", "Морозный осколок", () -> com.closky.rpgmod.registry.ModItems.FROST_SHARD.get(), 2.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1, 0),
    EMBER("ember", "Угольный слиток", () -> com.closky.rpgmod.registry.ModItems.EMBER_INGOT.get(), 2.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0, 1),
    VOID("void", "Осколок пустоты", () -> com.closky.rpgmod.registry.ModItems.VOID_SHARD.get(), 1.5F, 0.0F, 0.05F, 0.05F, 0.0F, 10.0F, 0, 0);

    private final String id;
    private final String displayName;
    private final Supplier<Item> item;
    private final float flatDamage;
    private final float attackSpeed;
    private final float critChance;
    private final float lifesteal;
    private final float armor;
    private final float bonusMana;
    private final int frostDamage;
    private final int fireDamage;

    HoneMaterial(String id, String displayName, Supplier<Item> item, float flatDamage, float attackSpeed,
                 float critChance, float lifesteal, float armor, float bonusMana, int frostDamage, int fireDamage) {
        this.id = id;
        this.displayName = displayName;
        this.item = item;
        this.flatDamage = flatDamage;
        this.attackSpeed = attackSpeed;
        this.critChance = critChance;
        this.lifesteal = lifesteal;
        this.armor = armor;
        this.bonusMana = bonusMana;
        this.frostDamage = frostDamage;
        this.fireDamage = fireDamage;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Item getItem() {
        return item.get();
    }

    public float getFlatDamage() {
        return flatDamage;
    }

    public float getAttackSpeed() {
        return attackSpeed;
    }

    public float getCritChance() {
        return critChance;
    }

    public float getLifesteal() {
        return lifesteal;
    }

    public float getArmor() {
        return armor;
    }

    public float getBonusMana() {
        return bonusMana;
    }

    public int getFrostDamage() {
        return frostDamage;
    }

    public int getFireDamage() {
        return fireDamage;
    }

    public String getTranslationKey() {
        return isNone() ? "hone.rpgmod.none" : "hone.rpgmod." + id;
    }

    public String getDescriptionKey() {
        return "hone.rpgmod." + id + ".desc";
    }

    public boolean isNone() {
        return this == NONE;
    }

    public static HoneMaterial byId(String id) {
        for (HoneMaterial m : values()) {
            if (m.id.equals(id)) return m;
        }
        return NONE;
    }

    /** Найти материал подковки по предмету, который положили в наковальню. */
    public static HoneMaterial byItem(Item item) {
        for (HoneMaterial m : values()) {
            if (m != NONE && m.getItem() == item) return m;
        }
        return NONE;
    }
}
