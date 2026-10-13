package com.closky.rpgmod.item;

import com.closky.rpgmod.config.RpgConfig;
import com.closky.rpgmod.magic.SpellElement;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeMod;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Вся «математика» оружия и хранение его состояния в NBT.
 *
 * <p>Что где лежит:</p>
 * <ul>
 *     <li>класс и материал — поля самого предмета ({@link RpgWeaponItem})</li>
 *     <li>уровень, опыт, качество, подковка, аффиксы — в NBT предмета, поэтому две «стальные секиры»
 *     могут быть совершенно разными</li>
 * </ul>
 *
 * <p>Итоговые характеристики считаются на лету в {@link #getStats(ItemStack)} — поэтому
 * их не нужно «пересчитывать» при апгрейде: достаточно поменять NBT.</p>
 */
public final class WeaponData {
    public static final String TAG_LEVEL = "rpg_level";
    public static final String TAG_XP = "rpg_xp";
    public static final String TAG_HONE = "rpg_hone";
    public static final String TAG_QUALITY = "rpg_quality";
    public static final String TAG_AFFIXES = "rpg_affixes";
    public static final String TAG_OWNER = "rpg_owner";

    private static final UUID UUID_DAMAGE = uuid("weapon_damage");
    private static final UUID UUID_SPEED = uuid("weapon_speed");
    private static final UUID UUID_REACH = uuid("weapon_reach");
    private static final UUID UUID_ARMOR = uuid("weapon_armor");

    private WeaponData() {}

    private static UUID uuid(String name) {
        return UUID.nameUUIDFromBytes(("rpgmod:" + name).getBytes(StandardCharsets.UTF_8));
    }

    // ------------------------------------------------------------------ чтение

    public static int getLevel(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? 1 : Math.max(1, tag.getInt(TAG_LEVEL));
    }

    public static void setLevel(ItemStack stack, int level) {
        stack.getOrCreateTag().putInt(TAG_LEVEL, Math.max(1, Math.min(level, RpgConfig.MAX_WEAPON_LEVEL.get())));
    }

    public static int getXp(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? 0 : tag.getInt(TAG_XP);
    }

    public static void setXp(ItemStack stack, int xp) {
        stack.getOrCreateTag().putInt(TAG_XP, Math.max(0, xp));
    }

    /** Сколько опыта нужно, чтобы поднять оружие на следующий уровень. */
    public static int getXpForNextLevel(int level) {
        return RpgConfig.WEAPON_XP_BASE.get() * Math.max(1, level);
    }

    public static HoneMaterial getHone(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? HoneMaterial.NONE : HoneMaterial.byId(tag.getString(TAG_HONE));
    }

    public static void setHone(ItemStack stack, HoneMaterial material) {
        stack.getOrCreateTag().putString(TAG_HONE, material.getId());
    }

    public static WeaponQuality getQuality(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? WeaponQuality.COMMON : WeaponQuality.byId(tag.getString(TAG_QUALITY));
    }

    public static void setQuality(ItemStack stack, WeaponQuality quality) {
        stack.getOrCreateTag().putString(TAG_QUALITY, quality.getId());
    }

    public static void addAffix(ItemStack stack, WeaponAffix affix, int roll) {
        CompoundTag tag = stack.getOrCreateTag();
        CompoundTag affixes = tag.contains(TAG_AFFIXES) ? tag.getCompound(TAG_AFFIXES) : new CompoundTag();
        affixes.putInt(affix.getId(), Math.min(roll, affix.getMaxRoll()));
        tag.put(TAG_AFFIXES, affixes);
    }

    public static Map<WeaponAffix, Integer> getAffixes(ItemStack stack) {
        Map<WeaponAffix, Integer> result = new LinkedHashMap<>();
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_AFFIXES)) return result;
        CompoundTag affixes = tag.getCompound(TAG_AFFIXES);
        for (String key : affixes.getAllKeys()) {
            int roll = affixes.getInt(key);
            if (roll > 0) result.put(WeaponAffix.byId(key), roll);
        }
        return result;
    }

    public static int getAffixRoll(ItemStack stack, WeaponAffix affix) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_AFFIXES)) return 0;
        return tag.getCompound(TAG_AFFIXES).getInt(affix.getId());
    }

    // ----------------------------------------------------------- характеристики

    /** Итоговые характеристики оружия с учётом уровня, подковки и аффиксов. */
    public static WeaponStats getStats(ItemStack stack) {
        if (!(stack.getItem() instanceof RpgWeaponItem weapon)) {
            return WeaponStats.EMPTY;
        }
        WeaponClass weaponClass = weapon.getWeaponClass();
        WeaponMaterial material = weapon.getWeaponMaterial();
        HoneMaterial hone = getHone(stack);
        int level = getLevel(stack);

        float damage = material.getBaseDamage() * weaponClass.getDamageMultiplier()
                + (level - 1) * RpgConfig.WEAPON_DAMAGE_PER_LEVEL.get().floatValue()
                + hone.getFlatDamage();

        float attackSpeed = weaponClass.getAttackSpeedModifier() + hone.getAttackSpeed();
        float reach = weaponClass.getReachBonus();
        float crit = weaponClass.getCritChance() + hone.getCritChance();
        float lifesteal = hone.getLifesteal();
        float armor = hone.getArmor();
        float mana = hone.getBonusMana();
        float spellPower = 0.0F;
        int frost = hone.getFrostDamage();
        int fire = hone.getFireDamage();
        int lightning = 0;
        int verdant = 0;

        for (Map.Entry<WeaponAffix, Integer> entry : getAffixes(stack).entrySet()) {
            int roll = entry.getValue();
            switch (entry.getKey()) {
                case SHARP -> damage += 0.75F * roll;
                case SWIFT -> attackSpeed += 0.06F * roll;
                case VAMPIRIC -> lifesteal += 0.025F * roll;
                case RUNIC -> {
                    mana += 8.0F * roll;
                    spellPower += 0.04F * roll;
                }
                case REACHING -> reach += 0.25F * roll;
                case STALWART -> armor += 0.5F * roll;
                case FROSTBOUND -> frost += roll;
                case EMBERFORGED -> fire += roll;
                case STORMBOUND -> lightning += roll;
                case VERDANT -> verdant += roll;
            }
        }

        return new WeaponStats(damage, attackSpeed, reach, crit, 1.75D, lifesteal, armor, mana, spellPower,
                frost, fire, lightning, verdant);
    }

    /**
     * Атрибуты оружия для игрока. Вызывается Forge'ом, когда игра спрашивает
     * «что даёт этот предмет в слоте MAINHAND» — поэтому здесь МОЖНО читать NBT.
     */
    public static Multimap<Attribute, AttributeModifier> buildAttributeModifiers(ItemStack stack) {
        WeaponStats stats = getStats(stack);
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(UUID_DAMAGE, "rpgmod.weapon.damage",
                stats.damage(), AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(UUID_SPEED, "rpgmod.weapon.speed",
                stats.attackSpeed(), AttributeModifier.Operation.ADDITION));
        if (stats.reach() > 0.0D) {
            builder.put(ForgeMod.ENTITY_REACH.get(), new AttributeModifier(UUID_REACH, "rpgmod.weapon.reach",
                    stats.reach(), AttributeModifier.Operation.ADDITION));
        }
        if (stats.armor() > 0.0D) {
            builder.put(Attributes.ARMOR, new AttributeModifier(UUID_ARMOR, "rpgmod.weapon.armor",
                    stats.armor(), AttributeModifier.Operation.ADDITION));
        }
        return builder.build();
    }

    // --------------------------------------------------------------- опыт/уровни

    /**
     * Начислить опыт оружию. Вызывается при ударе ({@code WeaponCombatHandler}) и при убийстве.
     *
     * @return true, если оружие подняло уровень
     */
    public static boolean addXp(ItemStack stack, int amount, ServerPlayer owner) {
        if (!(stack.getItem() instanceof RpgWeaponItem) || amount <= 0) return false;
        int level = getLevel(stack);
        if (level >= RpgConfig.MAX_WEAPON_LEVEL.get()) return false;
        int xp = getXp(stack) + amount;
        boolean leveledUp = false;
        while (level < RpgConfig.MAX_WEAPON_LEVEL.get() && xp >= getXpForNextLevel(level)) {
            xp -= getXpForNextLevel(level);
            level++;
            leveledUp = true;
        }
        setLevel(stack, level);
        setXp(stack, xp);
        if (leveledUp && owner != null) {
            owner.displayClientMessage(Component.translatable("msg.rpgmod.weapon_level_up",
                    stack.getHoverName().copy().withStyle(ChatFormatting.GOLD), Component.literal(String.valueOf(level))), true);
            Level world = owner.level();
            world.playSound(null, owner.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.6F, 1.4F);
        }
        return leveledUp;
    }

    // ------------------------------------------------------------- генерация лута

    /** Случайные аффиксы по качеству предмета. */
    public static void rollAffixes(ItemStack stack, RandomSource random) {
        WeaponQuality quality = getQuality(stack);
        int rolls = quality.getAffixRolls();
        if (rolls <= 0) return;
        WeaponAffix[] values = WeaponAffix.values();
        for (int i = 0; i < rolls; i++) {
            WeaponAffix affix = values[random.nextInt(values.length)];
            int roll = 1 + random.nextInt(Math.max(1, affix.getMaxRoll()));
            addAffix(stack, affix, roll);
        }
    }

    /** Полное имя предмета для подсказок: "Эпический Стальной Двуручник". */
    public static Component getDisplayName(RpgWeaponItem item, ItemStack stack) {
        WeaponQuality quality = getQuality(stack);
        return Component.translatable("name.rpgmod.weapon", Component.translatable(quality.getTranslationKey()),
                Component.translatable(item.getWeaponMaterial().getTranslationKey()),
                Component.translatable(item.getWeaponClass().getTranslationKey()))
                .withStyle(quality.getColor());
    }

    /** Служебный метод: положить ResourceLocation в NBT (для меток). */
    public static void putResourceLocation(CompoundTag tag, String key, ResourceLocation value) {
        tag.putString(key, value == null ? "" : value.toString());
    }

    /** Материал подковки для отображения в подсказке. */
    public static Component describeHone(ItemStack stack) {
        HoneMaterial hone = getHone(stack);
        if (hone.isNone()) {
            return Component.translatable("tooltip.rpgmod.hone.none").withStyle(ChatFormatting.DARK_GRAY);
        }
        return Component.translatable("tooltip.rpgmod.hone", Component.translatable(hone.getTranslationKey()))
                .withStyle(ChatFormatting.AQUA);
    }

    /** Наносимый элементом урон (для обработчика боёв). */
    public static SpellElement getElementDamage(WeaponStats stats) {
        if (stats.frostDamage() > 0) return SpellElement.FROST;
        if (stats.fireDamage() > 0) return SpellElement.FIRE;
        if (stats.lightningDamage() > 0) return SpellElement.LIGHTNING;
        if (stats.verdant() > 0) return SpellElement.NATURE;
        return null;
    }

    /** Небольшая утилита для {@link LivingEntity}, чтобы не тянуть лишние импорты. */
    public static boolean isHeld(ItemStack stack, LivingEntity entity) {
        return entity.getItemBySlot(EquipmentSlot.MAINHAND) == stack
                || entity.getItemBySlot(EquipmentSlot.OFFHAND) == stack;
    }
}
