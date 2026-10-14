package com.closky.rpgmod.lootgen;

import com.closky.rpgmod.item.WeaponRoller;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;

/**
 * Глобальный лут-модификатор: добавляет случайное оружие в ЛЮБОЙ сундук, к которому мы
 * его подключили через conditions (см. data/rpgmod/loot_modifiers/*.json).
 *
 * <p>Именно так ванильные сундуки (данжи, шахты, корабли) получают наше оружие, при этом
 * мы не переписываем ванильные лут-таблицы.</p>
 */
public class AddWeaponLootModifier extends LootModifier {
    public static final Codec<AddWeaponLootModifier> CODEC = RecordCodecBuilder.create(inst -> codecStart(inst)
            .and(Codec.INT.fieldOf("min_level").orElse(1).forGetter(m -> m.minLevel))
            .and(Codec.INT.fieldOf("max_level").orElse(5).forGetter(m -> m.maxLevel))
            .and(Codec.FLOAT.fieldOf("hone_chance").orElse(0.25F).forGetter(m -> m.honeChance))
            .apply(inst, AddWeaponLootModifier::new));

    private final int minLevel;
    private final int maxLevel;
    private final float honeChance;

    public AddWeaponLootModifier(LootItemCondition[] conditions, int minLevel, int maxLevel, float honeChance) {
        super(conditions);
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
        this.honeChance = honeChance;
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        generatedLoot.add(WeaponRoller.roll(context.getRandom(), minLevel, maxLevel, honeChance));
        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
