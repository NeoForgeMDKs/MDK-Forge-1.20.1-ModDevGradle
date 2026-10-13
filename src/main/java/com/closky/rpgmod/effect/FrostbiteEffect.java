package com.closky.rpgmod.effect;

import com.closky.rpgmod.util.RpgDamage;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

/** Обморожение: замедляет и наносит урон, пока цель не согреется (в огне эффект снимается). */
public class FrostbiteEffect extends MobEffect {
    public FrostbiteEffect() {
        super(MobEffectCategory.HARMFUL, 0x8FE8FF);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.isOnFire()) {
            entity.removeEffect(this);
            return;
        }
        entity.hurt(RpgDamage.frostbite(entity), 1.0F + amplifier * 0.5F);
        entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, amplifier));
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 20 == 0;
    }
}
