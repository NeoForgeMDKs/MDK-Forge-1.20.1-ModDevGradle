package com.closky.rpgmod.magic.spells;

import com.closky.rpgmod.magic.Spell;
import com.closky.rpgmod.magic.SpellElement;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Магия природы: лечит игроков (и союзных мобов) в радиусе.
 * Именно это закрывает требование «магия природы может лечить игроков».
 */
public class HealSpell implements Spell {
    private final String id;
    private final SpellElement element;
    private final float manaCost;
    private final int cooldown;
    private final float baseHeal;
    private final double radius;
    private final boolean cleanse;
    private final String displayName;
    private final String description;

    public HealSpell(String id, SpellElement element, float manaCost, int cooldown, float baseHeal, double radius,
                     boolean cleanse, String displayName, String description) {
        this.id = id;
        this.element = element;
        this.manaCost = manaCost;
        this.cooldown = cooldown;
        this.baseHeal = baseHeal;
        this.radius = radius;
        this.cleanse = cleanse;
        this.displayName = displayName;
        this.description = description;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public SpellElement getElement() {
        return element;
    }

    @Override
    public float getManaCost() {
        return manaCost;
    }

    @Override
    public int getCooldownTicks() {
        return cooldown;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public void cast(ServerPlayer caster, ItemStack staff, float power) {
        if (!(caster.level() instanceof ServerLevel level)) return;
        Vec3 center = caster.position();
        AABB box = new AABB(center.x - radius, center.y - radius, center.z - radius,
                center.x + radius, center.y + radius, center.z + radius);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, box);
        for (LivingEntity target : targets) {
            boolean friendly = target instanceof Player || target.getType().getCategory().isFriendly();
            if (!friendly) continue;
            target.heal(baseHeal * power);
            target.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 80, cleanse ? 1 : 0));
            if (cleanse) {
                target.removeEffect(MobEffects.POISON);
                target.removeEffect(MobEffects.WITHER);
                target.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
            }
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(element.getImpactParticle(), target.getX(), target.getY() + 1.0D, target.getZ(),
                        6, 0.3D, 0.3D, 0.3D, 0.01D);
            }
        }
    }
}
