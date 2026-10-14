package com.closky.rpgmod.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

/** Благословение природы: усиливает лечение и даёт регенерацию. */
public class NatureBlessingEffect extends MobEffect {
    public NatureBlessingEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x63E06B);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.getHealth() < entity.getMaxHealth()) {
            entity.heal(1.0F + amplifier * 0.5F);
        }
        entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, amplifier, true, false));
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 20 == 0;
    }
}
