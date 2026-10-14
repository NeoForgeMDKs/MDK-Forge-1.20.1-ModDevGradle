package com.closky.rpgmod.magic.spells;

import com.closky.rpgmod.entity.SpellProjectile;
import com.closky.rpgmod.magic.Spell;
import com.closky.rpgmod.magic.SpellElement;
import com.closky.rpgmod.registry.ModEntities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/** Заклинание-снаряд: создаёт SpellProjectile и запускает его туда, куда смотрит игрок. */
public class ProjectileSpell implements Spell {
    private final String id;
    private final SpellElement element;
    private final float manaCost;
    private final int cooldown;
    private final float baseDamage;
    private final float speed;
    private final String displayName;
    private final String description;

    public ProjectileSpell(String id, SpellElement element, float manaCost, int cooldown, float baseDamage,
                           float speed, String displayName, String description) {
        this.id = id;
        this.element = element;
        this.manaCost = manaCost;
        this.cooldown = cooldown;
        this.baseDamage = baseDamage;
        this.speed = speed;
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
        SpellProjectile projectile = new SpellProjectile(ModEntities.SPELL_PROJECTILE.get(), caster, level,
                element, baseDamage * power);
        Vec3 look = caster.getLookAngle();
        projectile.setPos(caster.getX() + look.x * 0.7D, caster.getEyeY() - 0.15D, caster.getZ() + look.z * 0.7D);
        projectile.shoot(look, speed);
        level.addFreshEntity(projectile);
    }
}
