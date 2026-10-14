package com.closky.rpgmod.magic;

import com.closky.rpgmod.magic.spells.ChainLightningSpell;
import com.closky.rpgmod.magic.spells.HealSpell;
import com.closky.rpgmod.magic.spells.NovaSpell;
import com.closky.rpgmod.magic.spells.ProjectileSpell;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Реестр заклинаний мода. Это обычный Java-реестр (не игровой registry), потому что
 * заклинания не нужно синхронизировать: клиенту достаточно знать id и ману.
 *
 * <p>Хотите добавить своё заклинание — допишите строчку в {@link #register(Spell)} ниже.</p>
 */
public final class Spells {
    private static final Map<String, Spell> BY_ID = new LinkedHashMap<>();

    public static final Spell FIREBOLT = register(new ProjectileSpell("firebolt", SpellElement.FIRE, 12.0F, 20,
            6.0F, 1.15F, "Огненная стрела", "Быстрый снаряд, поджигающий цель"));
    public static final Spell FLAME_NOVA = register(new NovaSpell("flame_nova", SpellElement.FIRE, 25.0F, 120,
            9.0F, 5.0D, "Вспышка пламени", "Взрыв огня вокруг заклинателя"));
    public static final Spell FROST_SHARD = register(new ProjectileSpell("frost_shard", SpellElement.FROST, 10.0F, 16,
            5.0F, 1.45F, "Осколок льда", "Замораживает цель, замедляя её"));
    public static final Spell FROST_NOVA = register(new NovaSpell("frost_nova", SpellElement.FROST, 30.0F, 160,
            7.0F, 6.0D, "Ледяная новая", "Волна холода: урон и сильное замедление"));
    public static final Spell SPARK_BOLT = register(new ProjectileSpell("spark_bolt", SpellElement.LIGHTNING, 14.0F, 24,
            7.0F, 1.7F, "Искра", "Быстрый электрический снаряд"));
    public static final Spell CHAIN_LIGHTNING = register(new ChainLightningSpell("chain_lightning", SpellElement.LIGHTNING,
            32.0F, 140, 8.0F, 4, 9.0D, "Цепная молния", "Бьёт до 4 врагов подряд"));
    public static final Spell HEALING_BLOOM = register(new HealSpell("healing_bloom", SpellElement.NATURE, 30.0F, 120,
            8.0F, 6.0D, false, "Цветение", "Лечит вас и союзников рядом"));
    public static final Spell NATURES_RENEWAL = register(new HealSpell("natures_renewal", SpellElement.NATURE, 45.0F, 300,
            12.0F, 8.0D, true, "Обновление природы", "Сильное лечение и очищение от ядов"));

    private Spells() {}

    public static Spell register(Spell spell) {
        BY_ID.put(spell.getId(), spell);
        return spell;
    }

    public static Spell byId(String id) {
        return BY_ID.get(id);
    }

    public static List<Spell> byElement(SpellElement element) {
        List<Spell> list = new ArrayList<>();
        for (Spell spell : BY_ID.values()) {
            if (spell.getElement() == element) list.add(spell);
        }
        return list;
    }

    public static List<Spell> all() {
        return new ArrayList<>(BY_ID.values());
    }
}
