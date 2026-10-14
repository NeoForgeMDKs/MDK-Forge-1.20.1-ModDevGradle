package com.closky.rpgmod.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Наше оружие.
 *
 * <p>Механики:</p>
 * <ul>
 *     <li>урон/скорость/дальность зависят от класса, материала, уровня, подковки и аффиксов;</li>
 *     <li>за удары оружие получает опыт и повышает уровень ({@link WeaponData#addXp});</li>
 *     <li>подсказка показывает все бонусы (требование «у каждого лута есть описание и бонусы»).</li>
 * </ul>
 */
public class RpgWeaponItem extends SwordItem {
    private final WeaponClass weaponClass;
    private final WeaponMaterial weaponMaterial;

    public RpgWeaponItem(WeaponClass weaponClass, WeaponMaterial weaponMaterial, Properties properties) {
        super(new RpgTier(weaponMaterial), 0, 0.0F, properties.durability(weaponMaterial.getDurability()));
        this.weaponClass = weaponClass;
        this.weaponMaterial = weaponMaterial;
    }

    public WeaponClass getWeaponClass() {
        return weaponClass;
    }

    public WeaponMaterial getWeaponMaterial() {
        return weaponMaterial;
    }

    /**
     * Здесь игра спрашивает характеристики предмета. В отличие от ванильного
     * {@code getDefaultAttributeModifiers(slot)} у этого метода есть САМ ItemStack,
     * поэтому можно читать уровень/подковку из NBT.
     */
    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        if (slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND) {
            return WeaponData.buildAttributeModifiers(stack);
        }
        return ImmutableMultimap.of();
    }

    /** Опыт оружия за удар. */
    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean result = super.hurtEnemy(stack, target, attacker);
        if (attacker instanceof net.minecraft.server.level.ServerPlayer player && !target.level().isClientSide) {
            WeaponData.addXp(stack, com.closky.rpgmod.config.RpgConfig.WEAPON_XP_PER_HIT.get(), player);
        }
        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        RpgTooltips.appendWeaponTooltip(stack, this, tooltip, flag);
        super.appendHoverText(stack, level, tooltip, flag);
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return WeaponData.getQuality(stack).getRarity();
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public int getEnchantmentValue() {
        return new RpgTier(weaponMaterial).getEnchantmentValue();
    }
}
