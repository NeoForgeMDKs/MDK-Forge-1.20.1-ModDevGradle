package com.closky.rpgmod.magic;

import com.closky.rpgmod.config.RpgConfig;
import com.closky.rpgmod.network.SyncPlayerDataPacket;
import com.closky.rpgmod.player.RpgPlayerData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;

/** Общая логика каста: проверка маны, кулдаун, списание маны, сообщения. */
public final class SpellCasting {
    private SpellCasting() {}

    /**
     * Попытаться скастовать заклинание.
     *
     * @return true, если каст состоялся
     */
    public static boolean attemptCast(ServerPlayer caster, ItemStack staff, Spell spell) {
        RpgPlayerData data = RpgPlayerData.get(caster);

        if (RpgConfig.SPELLS_REQUIRE_STAFF.get() && !(staff.getItem() instanceof com.closky.rpgmod.item.MagicStaffItem)) {
            caster.displayClientMessage(Component.translatable("msg.rpgmod.need_staff").withStyle(ChatFormatting.RED), true);
            return false;
        }
        if (caster.getCooldowns().isOnCooldown(staff.getItem())) {
            return false;
        }
        if (data.getMana() < spell.getManaCost()) {
            caster.displayClientMessage(Component.translatable("msg.rpgmod.not_enough_mana",
                    Component.literal(String.valueOf((int) spell.getManaCost())),
                    Component.literal(String.valueOf((int) data.getMana()))).withStyle(ChatFormatting.RED), true);
            return false;
        }

        data.consumeMana(spell.getManaCost());
        caster.getCooldowns().addCooldown(staff.getItem(), spell.getCooldownTicks());
        caster.level().playSound(null, caster.blockPosition(), spell.getElement().getCastSound(), SoundSource.PLAYERS, 1.0F, 1.0F);

        spell.cast(caster, staff, data.getSpellPowerMultiplier());

        // Тратим прочность посоха (кроме творческого режима)
        if (!caster.isCreative()) {
            staff.hurtAndBreak(1, caster, player -> player.broadcastBreakEvent(player.getUsedItemHand()));
        }

        if (caster.level() instanceof ServerLevel) {
            SyncPlayerDataPacket.sendTo(caster, data);
        }
        caster.displayClientMessage(Component.translatable("msg.rpgmod.mana_left",
                Component.literal(String.valueOf((int) data.getMana())),
                Component.literal(String.valueOf((int) data.getMaxMana()))).withStyle(ChatFormatting.AQUA), true);
        return true;
    }
}
