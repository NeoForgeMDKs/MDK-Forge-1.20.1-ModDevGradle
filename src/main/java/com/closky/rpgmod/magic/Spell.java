package com.closky.rpgmod.magic;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Заклинание. Реализации лежат в {@code magic.spells}.
 *
 * <p>Каст всегда идёт с сервера: клиент только просит, сервер проверяет ману, кулдаун
 * и выполняет эффект. Так читер не сможет «спамить» заклинания.</p>
 */
public interface Spell {
    /** Уникальный id (латиницей, без пробелов). */
    String getId();

    SpellElement getElement();

    /** Сколько маны стоит каст. */
    float getManaCost();

    /** Кулдаун в тиках. */
    int getCooldownTicks();

    /** Выполнить заклинание. Вызывается только на сервере. */
    void cast(ServerPlayer caster, ItemStack staff, float power);

    default String getTranslationKey() {
        return "spell.rpgmod." + getId();
    }

    default String getDescriptionKey() {
        return "spell.rpgmod." + getId() + ".desc";
    }

    /** Множитель силы заклинаний от интеллекта (передаётся в cast как power). */
    default float getPower(Player player) {
        return com.closky.rpgmod.player.RpgPlayerData.get(player).getSpellPowerMultiplier();
    }
}
