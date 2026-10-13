package com.closky.rpgmod.item;

import com.closky.rpgmod.config.RpgClientConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Сборка подсказки предмета: класс, уровень, качество, подковка, бонусы, описание. */
public final class RpgTooltips {
    private RpgTooltips() {}

    public static void appendWeaponTooltip(ItemStack stack, RpgWeaponItem item, List<Component> tooltip, TooltipFlag flag) {
        WeaponStats stats = WeaponData.getStats(stack);
        WeaponQuality quality = WeaponData.getQuality(stack);

        tooltip.add(Component.translatable("tooltip.rpgmod.quality", Component.translatable(quality.getTranslationKey()))
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.rpgmod.weapon_class",
                        Component.translatable(item.getWeaponClass().getTranslationKey()))
                .withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("tooltip.rpgmod.weapon_level",
                        Component.literal(String.valueOf(WeaponData.getLevel(stack))),
                        Component.literal(String.valueOf(com.closky.rpgmod.config.RpgConfig.MAX_WEAPON_LEVEL.get())))
                .withStyle(ChatFormatting.YELLOW));
        if (RpgClientConfig.SHOW_WEAPON_XP.get()) {
            int level = WeaponData.getLevel(stack);
            int need = WeaponData.getXpForNextLevel(level);
            tooltip.add(Component.translatable("tooltip.rpgmod.weapon_xp",
                            Component.literal(WeaponData.getXp(stack) + "/" + need))
                    .withStyle(ChatFormatting.DARK_GREEN));
        }

        tooltip.add(Component.translatable("tooltip.rpgmod.damage", Component.literal(fmt(stats.damage())))
                .withStyle(ChatFormatting.RED));
        tooltip.add(Component.translatable("tooltip.rpgmod.attack_speed",
                        Component.literal(fmt(4.0D + stats.attackSpeed())))
                .withStyle(ChatFormatting.RED));
        if (stats.reach() > 0) {
            tooltip.add(Component.translatable("tooltip.rpgmod.reach", Component.literal(fmt(stats.reach())))
                    .withStyle(ChatFormatting.GRAY));
        }
        if (stats.critChance() > 0) {
            tooltip.add(Component.translatable("tooltip.rpgmod.crit",
                            Component.literal(fmt(stats.critChance() * 100.0D) + "%"))
                    .withStyle(ChatFormatting.GOLD));
        }
        if (stats.lifesteal() > 0) {
            tooltip.add(Component.translatable("tooltip.rpgmod.lifesteal",
                            Component.literal(fmt(stats.lifesteal() * 100.0D) + "%"))
                    .withStyle(ChatFormatting.DARK_RED));
        }
        if (stats.armor() > 0) {
            tooltip.add(Component.translatable("tooltip.rpgmod.armor_bonus", Component.literal(fmt(stats.armor())))
                    .withStyle(ChatFormatting.BLUE));
        }
        if (stats.bonusMana() > 0) {
            tooltip.add(Component.translatable("tooltip.rpgmod.mana_bonus", Component.literal(fmt(stats.bonusMana())))
                    .withStyle(ChatFormatting.AQUA));
        }

        tooltip.add(WeaponData.describeHone(stack));

        Map<WeaponAffix, Integer> affixes = WeaponData.getAffixes(stack);
        if (!affixes.isEmpty()) {
            tooltip.add(Component.translatable("tooltip.rpgmod.affixes").withStyle(ChatFormatting.DARK_PURPLE));
            for (Map.Entry<WeaponAffix, Integer> entry : affixes.entrySet()) {
                tooltip.add(Component.literal(" ").append(entry.getKey().describe(entry.getValue())
                        .withStyle(entry.getKey().getColor())));
            }
        }

        tooltip.add(Component.translatable(item.getWeaponClass().getDescriptionKey())
                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        tooltip.add(Component.translatable("tooltip.rpgmod.loot_only").withStyle(ChatFormatting.DARK_GRAY));
        if (flag.isAdvanced()) {
            tooltip.add(Component.translatable("tooltip.rpgmod.hone_hint").withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    /** Описание обычного предмета лута. */
    public static void appendDescription(List<Component> tooltip, String key) {
        tooltip.add(Component.translatable(key).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }

    public static String fmt(double value) {
        return String.format(Locale.ROOT, "%.1f", value);
    }
}
