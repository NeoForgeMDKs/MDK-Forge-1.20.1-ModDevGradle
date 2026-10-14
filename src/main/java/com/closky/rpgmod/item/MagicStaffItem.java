package com.closky.rpgmod.item;

import com.closky.rpgmod.magic.Spell;
import com.closky.rpgmod.magic.SpellCasting;
import com.closky.rpgmod.magic.SpellElement;
import com.closky.rpgmod.magic.Spells;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Посох — единственный способ колдовать (требование 3г).
 *
 * <p>Правый клик — каст выбранного заклинания, Shift+правый клик — переключение между
 * заклинаниями своей школы. Выбранное заклинание хранится в NBT предмета.</p>
 */
public class MagicStaffItem extends Item {
    public static final String TAG_SPELL = "rpg_spell";

    private final SpellElement element;

    public MagicStaffItem(SpellElement element, Properties properties) {
        super(properties);
        this.element = element;
    }

    public SpellElement getElement() {
        return element;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                Spell next = cycleSpell(stack, player, 1);
                player.displayClientMessage(Component.translatable("msg.rpgmod.spell_selected",
                        Component.translatable(next.getTranslationKey()).withStyle(ChatFormatting.AQUA)), true);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }

        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }
        if (player instanceof ServerPlayer serverPlayer) {
            SpellCasting.attemptCast(serverPlayer, stack, getSelectedSpell(stack));
        }
        return InteractionResultHolder.success(stack);
    }

    /** Текущее заклинание посоха (по умолчанию — первое заклинание школы). */
    public Spell getSelectedSpell(ItemStack stack) {
        String id = stack.getTag() == null ? "" : stack.getTag().getString(TAG_SPELL);
        Spell spell = Spells.byId(id);
        if (spell == null || spell.getElement() != element) {
            List<Spell> list = Spells.byElement(element);
            spell = list.isEmpty() ? Spells.FIREBOLT : list.get(0);
        }
        return spell;
    }

    private Spell cycleSpell(ItemStack stack, Player player, int direction) {
        List<Spell> list = Spells.byElement(element);
        if (list.isEmpty()) return Spells.FIREBOLT;
        Spell current = getSelectedSpell(stack);
        int index = list.indexOf(current);
        int next = Math.floorMod(index + direction, list.size());
        Spell spell = list.get(next);
        stack.getOrCreateTag().putString(TAG_SPELL, spell.getId());
        return spell;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        Spell spell = getSelectedSpell(stack);
        tooltip.add(Component.translatable("tooltip.rpgmod.staff_element",
                        Component.translatable(element.getTranslationKey()).withStyle(ChatFormatting.AQUA))
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.rpgmod.staff_spell",
                        Component.translatable(spell.getTranslationKey()).withStyle(ChatFormatting.LIGHT_PURPLE))
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.rpgmod.staff_mana",
                        Component.literal(String.valueOf((int) spell.getManaCost())))
                .withStyle(ChatFormatting.BLUE));
        tooltip.add(Component.translatable(spell.getDescriptionKey()).withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        tooltip.add(Component.translatable("tooltip.rpgmod.staff_cycle").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable(element.getDescriptionKey()).withStyle(ChatFormatting.DARK_AQUA));
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public int getEnchantmentValue() {
        return 15;
    }
}
