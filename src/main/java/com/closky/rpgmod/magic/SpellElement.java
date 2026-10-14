package com.closky.rpgmod.magic;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * Четыре школы магии: огонь, мороз, молния и природа.
 *
 * <p>Каждая школа — это цвет, частицы, звук каста и «эффект попадания». Такой enum удобно
 * использовать и для посохов, и для свитков, и для зачарованного оружия.</p>
 */
public enum SpellElement {
    FIRE("fire", "Огонь", 0xFF7A00, ParticleTypes.FLAME, ParticleTypes.LAVA, SoundEvents.BLAZE_SHOOT),
    FROST("frost", "Мороз", 0x7FE3FF, ParticleTypes.SNOWFLAKE, ParticleTypes.ITEM_SNOWBALL, SoundEvents.GLASS_BREAK),
    LIGHTNING("lightning", "Молния", 0xA985FF, ParticleTypes.ELECTRIC_SPARK, ParticleTypes.END_ROD, SoundEvents.LIGHTNING_BOLT_IMPACT),
    NATURE("nature", "Природа", 0x62E06B, ParticleTypes.HAPPY_VILLAGER, ParticleTypes.HEART, SoundEvents.AMETHYST_BLOCK_CHIME);

    private final String id;
    private final String displayName;
    private final int color;
    private final ParticleOptions trailParticle;
    private final ParticleOptions impactParticle;
    private final SoundEvent castSound;

    SpellElement(String id, String displayName, int color, ParticleOptions trailParticle,
                 ParticleOptions impactParticle, SoundEvent castSound) {
        this.id = id;
        this.displayName = displayName;
        this.color = color;
        this.trailParticle = trailParticle;
        this.impactParticle = impactParticle;
        this.castSound = castSound;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getColor() {
        return color;
    }

    public ParticleOptions getTrailParticle() {
        return trailParticle;
    }

    public ParticleOptions getImpactParticle() {
        return impactParticle;
    }

    public SoundEvent getCastSound() {
        return castSound;
    }

    public String getTranslationKey() {
        return "element.rpgmod." + id;
    }

    public String getDescriptionKey() {
        return "element.rpgmod." + id + ".desc";
    }

    /**
     * Что происходит с целью, в которую попали снарядом этой школы.
     * (Урон наносит сам снаряд, здесь только «спецэффекты».)
     */
    public void applyOnHit(LivingEntity target, Entity owner) {
        switch (this) {
            case FIRE -> target.setSecondsOnFire(4);
            case FROST -> target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1));
            case LIGHTNING -> target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 80, 0));
            case NATURE -> {
                // Природа лечит союзников, а не бьёт их: если цель — игрок, ему станет лучше.
                if (owner instanceof Player caster && target != caster && !(target instanceof Player)) {
                    caster.heal(1.0F);
                } else if (target instanceof Player player && owner instanceof Player caster && player != caster) {
                    player.heal(2.0F);
                }
            }
        }
    }

    public static SpellElement byId(String id) {
        for (SpellElement e : values()) {
            if (e.id.equals(id)) return e;
        }
        return FIRE;
    }
}
