package com.closky.rpgmod.magic.spells;

import com.closky.rpgmod.magic.Spell;
import com.closky.rpgmod.magic.SpellElement;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/** Заклинание-«нова»: бьёт всех врагов в радиусе вокруг заклинателя. */
public class NovaSpell implements Spell {
    private final String id;
    private final SpellElement element;
    private final float manaCost;
    private final int cooldown;
    private final float baseDamage;
    private final double radius;
    private final String displayName;
    private final String description;

    public NovaSpell(String id, SpellElement element, float manaCost, int cooldown, float baseDamage,
                     double radius, String displayName, String description) {
        this.id = id;
        this.element = element;
        this.manaCost = manaCost;
        this.cooldown = cooldown;
        this.baseDamage = baseDamage;
        this.radius = radius;
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
            // Новы бьют только мобов: в кооперативе свои игроки не должны получать урон от союзника.
            if (target == caster || target instanceof Player) continue;
            target.hurt(caster.damageSources().playerAttack(caster), baseDamage * power);
            element.applyOnHit(target, caster);
        }
        spawnRing(level, center, element.getImpactParticle());
    }

    private void spawnRing(ServerLevel level, Vec3 center, ParticleOptions particle) {
        int points = 36;
        for (int i = 0; i < points; i++) {
            double angle = (Math.PI * 2.0D / points) * i;
            double x = center.x + Math.cos(angle) * radius;
            double z = center.z + Math.sin(angle) * radius;
            level.sendParticles(particle, x, center.y + 0.6D, z, 2, 0.05D, 0.05D, 0.05D, 0.0D);
        }
    }
}
