package com.closky.rpgmod.magic.spells;

import com.closky.rpgmod.magic.Spell;
import com.closky.rpgmod.magic.SpellElement;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Цепная молния: перескакивает между врагами, теряя часть урона на каждом прыжке. */
public class ChainLightningSpell implements Spell {
    private final String id;
    private final SpellElement element;
    private final float manaCost;
    private final int cooldown;
    private final float baseDamage;
    private final int maxTargets;
    private final double range;
    private final String displayName;
    private final String description;

    public ChainLightningSpell(String id, SpellElement element, float manaCost, int cooldown, float baseDamage,
                               int maxTargets, double range, String displayName, String description) {
        this.id = id;
        this.element = element;
        this.manaCost = manaCost;
        this.cooldown = cooldown;
        this.baseDamage = baseDamage;
        this.maxTargets = maxTargets;
        this.range = range;
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
        Set<LivingEntity> hit = new HashSet<>();
        LivingEntity current = findNearest(level, caster.position(), null, hit, caster);
        float damage = baseDamage * power;
        int jumps = 0;
        Vec3 previous = caster.position().add(0, 1, 0);

        while (current != null && jumps < maxTargets) {
            current.hurt(caster.damageSources().playerAttack(caster), damage);
            drawLightning(level, previous, current.position().add(0, current.getBbHeight() / 2.0D, 0));
            element.applyOnHit(current, caster);
            hit.add(current);
            previous = current.position();
            damage *= 0.75F;
            jumps++;
            current = findNearest(level, previous, current, hit, caster);
        }
    }

    private LivingEntity findNearest(ServerLevel level, Vec3 around, LivingEntity self, Set<LivingEntity> excluded,
                                     ServerPlayer caster) {
        AABB box = new AABB(around.x - range, around.y - range, around.z - range,
                around.x + range, around.y + range, around.z + range);
        List<Mob> candidates = level.getEntitiesOfClass(Mob.class, box);
        return candidates.stream()
                .filter(mob -> mob != self && !excluded.contains(mob) && mob.isAlive())
                .min(Comparator.comparingDouble(mob -> mob.distanceToSqr(around)))
                .orElse(null);
    }

    private void drawLightning(ServerLevel level, Vec3 from, Vec3 to) {
        Vec3 diff = to.subtract(from);
        double length = diff.length();
        int points = (int) Math.max(4, length * 2);
        for (int i = 0; i <= points; i++) {
            Vec3 pos = from.add(diff.scale((double) i / points));
            level.sendParticles(element.getImpactParticle(), pos.x, pos.y, pos.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

}
