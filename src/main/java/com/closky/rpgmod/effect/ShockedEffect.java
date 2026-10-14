package com.closky.rpgmod.effect;

import com.closky.rpgmod.util.RpgDamage;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/** Разряд: бьёт током и мешает колдовать (используется посохами и молниями). */
public class ShockedEffect extends MobEffect {
    public ShockedEffect() {
        super(MobEffectCategory.HARMFUL, 0xB388FF);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        entity.hurt(RpgDamage.shock(entity), 1.5F + amplifier);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 60 == 0;
    }
}
