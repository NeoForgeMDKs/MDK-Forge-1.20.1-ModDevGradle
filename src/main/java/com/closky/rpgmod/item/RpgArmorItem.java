package com.closky.rpgmod.item;

import com.closky.rpgmod.player.PlayerClass;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Броня класса. Полный сет (4 предмета одного класса) даёт классовые бонусы —
 * см. {@code player.SetBonusHandler}.
 */
public class RpgArmorItem extends ArmorItem {
    private final PlayerClass playerClass;

    public RpgArmorItem(PlayerClass playerClass, ArmorItem.Type type, ArmorMaterial material, Properties properties) {
        super(material, type, properties);
        this.playerClass = playerClass;
    }

    public PlayerClass getPlayerClass() {
        return playerClass;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.rpgmod.set_part",
                        Component.translatable(playerClass.getTranslationKey()).withStyle(playerClass.getColor()))
                .withStyle(ChatFormatting.GRAY));
        if (flag.isAdvanced()) {
            tooltip.add(Component.translatable(playerClass.getBonusKey()).withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return Rarity.UNCOMMON;
    }
}
