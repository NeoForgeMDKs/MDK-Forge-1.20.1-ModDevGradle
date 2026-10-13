package com.closky.rpgmod.item;

import net.minecraft.world.item.Ingredient;
import net.minecraft.world.item.Tier;

/**
 * «Уровень добычи» для нашего оружия. Нужен только потому, что {@code SwordItem}
 * требует Tier: урон и скорость мы всё равно задаём сами через атрибуты (WeaponData).
 */
public class RpgTier implements Tier {
    private final WeaponMaterial material;

    public RpgTier(WeaponMaterial material) {
        this.material = material;
    }

    @Override
    public int getUses() {
        return material.getDurability();
    }

    @Override
    public float getSpeed() {
        return 6.0F;
    }

    @Override
    public float getAttackDamageBonus() {
        return 0.0F; // настоящий урон приходит из WeaponData#buildAttributeModifiers
    }

    @Override
    public int getLevel() {
        return material.getTierLevel();
    }

    @Override
    public int getEnchantmentValue() {
        return 12 + material.getTierLevel() * 3;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.of(material.getRepairItem());
    }
}
