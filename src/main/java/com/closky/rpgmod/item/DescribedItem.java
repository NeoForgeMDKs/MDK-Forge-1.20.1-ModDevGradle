package com.closky.rpgmod.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Обычный предмет с описанием (требование «у каждого лута есть описание»). */
public class DescribedItem extends Item {
    private final String descriptionKey;
    private final boolean rare;

    public DescribedItem(String descriptionKey, boolean rare, Properties properties) {
        super(properties);
        this.descriptionKey = descriptionKey;
        this.rare = rare;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        RpgTooltips.appendDescription(tooltip, descriptionKey);
        if (rare) {
            tooltip.add(Component.translatable("tooltip.rpgmod.loot_only")
                    .withStyle(net.minecraft.ChatFormatting.DARK_GRAY));
        }
    }
}
