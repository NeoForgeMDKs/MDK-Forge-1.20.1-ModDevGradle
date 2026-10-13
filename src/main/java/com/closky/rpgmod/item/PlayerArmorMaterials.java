package com.closky.rpgmod.item;

import com.closky.rpgmod.player.PlayerClass;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.Items;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Материалы брони для четырёх классов.
 *
 * <p>Имя материала ({@code getName()}) — это имя файлов текстур:
 * {@code assets/rpgmod/textures/models/armor/<имя>_layer_1.png} и {@code ..._layer_2.png}.</p>
 */
public enum PlayerArmorMaterials implements ArmorMaterial {
    // defense = {ботинки, штаны, нагрудник, шлем} — порядок ванильный!
    WARRIOR(PlayerClass.WARRIOR, "warrior", 22, new int[]{4, 7, 6, 4}, 12, 2.0F, 0.05F,
            SoundEvents.ARMOR_EQUIP_IRON, () -> Items.IRON_INGOT),
    RANGER(PlayerClass.RANGER, "ranger", 18, new int[]{2, 5, 4, 2}, 18, 0.0F, 0.0F,
            SoundEvents.ARMOR_EQUIP_LEATHER, () -> Items.LEATHER),
    MAGE(PlayerClass.MAGE, "mage", 14, new int[]{1, 4, 3, 2}, 25, 0.0F, 0.0F,
            SoundEvents.ARMOR_EQUIP_GOLD, () -> Items.AMETHYST_SHARD),
    DRUID(PlayerClass.DRUID, "druid", 17, new int[]{2, 5, 4, 2}, 20, 0.5F, 0.0F,
            SoundEvents.ARMOR_EQUIP_LEATHER, () -> Items.IRON_INGOT);

    private static final Map<PlayerClass, PlayerArmorMaterials> BY_CLASS = new EnumMap<>(PlayerClass.class);

    static {
        for (PlayerArmorMaterials material : values()) {
            BY_CLASS.put(material.playerClass, material);
        }
    }

    private final PlayerClass playerClass;
    private final String name;
    private final int baseDurability;
    private final int[] defense;
    private final int enchantmentValue;
    private final float toughness;
    private final float knockbackResistance;
    private final SoundEvent equipSound;
    private final Supplier<net.minecraft.world.item.Item> repairItem;

    PlayerArmorMaterials(PlayerClass playerClass, String name, int baseDurability, int[] defense, int enchantmentValue,
                         float toughness, float knockbackResistance, SoundEvent equipSound,
                         Supplier<net.minecraft.world.item.Item> repairItem) {
        this.playerClass = playerClass;
        this.name = name;
        this.baseDurability = baseDurability;
        this.defense = defense;
        this.enchantmentValue = enchantmentValue;
        this.toughness = toughness;
        this.knockbackResistance = knockbackResistance;
        this.equipSound = equipSound;
        this.repairItem = repairItem;
    }

    public static PlayerArmorMaterials forClass(PlayerClass playerClass) {
        return BY_CLASS.getOrDefault(playerClass, WARRIOR);
    }

    @Override
    public int getDurabilityForType(ArmorItem.Type type) {
        return switch (type) {
            case BOOTS -> baseDurability * 13;
            case LEGGINGS -> baseDurability * 15;
            case CHESTPLATE -> baseDurability * 16;
            case HELMET -> baseDurability * 11;
        };
    }

    @Override
    public int getDefenseForType(ArmorItem.Type type) {
        return switch (type) {
            case BOOTS -> defense[0];
            case LEGGINGS -> defense[1];
            case CHESTPLATE -> defense[2];
            case HELMET -> defense[3];
        };
    }

    @Override
    public int getEnchantmentValue() {
        return enchantmentValue;
    }

    @Override
    public SoundEvent getEquipSound() {
        return equipSound;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.of(repairItem.get());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public float getToughness() {
        return toughness;
    }

    @Override
    public float getKnockbackResistance() {
        return knockbackResistance;
    }
}
