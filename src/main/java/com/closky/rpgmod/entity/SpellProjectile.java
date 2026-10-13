package com.closky.rpgmod.entity;

import com.closky.rpgmod.magic.SpellElement;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;

/**
 * Снаряд заклинаний (летит прямо, без гравитации, при попадании наносит урон своей школы).
 *
 * <p>Сделан по образцу ванильных метательных снарядов: движение, поиск столкновений и
 * серверная логика попадания. На клиенте рисует только частицы — сам рендер снаряда
 * в {@code client.SpellProjectileRenderer}.</p>
 */
public class SpellProjectile extends Projectile {
    private static final EntityDataAccessor<String> DATA_ELEMENT =
            SynchedEntityData.defineId(SpellProjectile.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Float> DATA_DAMAGE =
            SynchedEntityData.defineId(SpellProjectile.class, EntityDataSerializers.FLOAT);

    private static final int MAX_LIFETIME = 100;

    public SpellProjectile(EntityType<? extends SpellProjectile> type, Level level) {
        super(type, level);
    }

    public SpellProjectile(EntityType<? extends SpellProjectile> type, LivingEntity owner, Level level,
                           SpellElement element, float damage) {
        super(type, level);
        this.setOwner(owner);
        this.entityData.set(DATA_ELEMENT, element.getId());
        this.entityData.set(DATA_DAMAGE, damage);
        this.setPos(owner.getX(), owner.getEyeY() - 0.1D, owner.getZ());
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_ELEMENT, SpellElement.FIRE.getId());
        this.entityData.define(DATA_DAMAGE, 5.0F);
    }

    public SpellElement getElement() {
        return SpellElement.byId(this.entityData.get(DATA_ELEMENT));
    }

    public float getSpellDamage() {
        return this.entityData.get(DATA_DAMAGE);
    }

    /** Запустить снаряд в заданном направлении с заданной скоростью. */
    public void shoot(Vec3 direction, float speed) {
        this.setDeltaMovement(direction.normalize().scale(speed));
        this.hasImpulse = true;
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        return super.canHitEntity(target) && target != getOwner();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.tickCount > MAX_LIFETIME) {
            this.discard();
            return;
        }

        if (this.level().isClientSide) {
            trailParticles();
            return;
        }

        HitResult hit = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        if (hit.getType() != HitResult.Type.MISS && !ForgeEventFactory.onProjectileImpact(this, hit)) {
            this.onHit(hit);
        }
        this.setPos(this.position().add(this.getDeltaMovement()));
    }

    private void trailParticles() {
        SpellElement element = getElement();
        for (int i = 0; i < 2; i++) {
            this.level().addParticle(element.getTrailParticle(),
                    this.getX() + (this.random.nextDouble() - 0.5D) * 0.2D,
                    this.getY() + (this.random.nextDouble() - 0.5D) * 0.2D,
                    this.getZ() + (this.random.nextDouble() - 0.5D) * 0.2D,
                    0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    protected void onHit(HitResult result) {
        SpellElement element = getElement();
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(element.getImpactParticle(), this.getX(), this.getY(), this.getZ(),
                    16, 0.2D, 0.2D, 0.2D, 0.05D);
        }
        super.onHit(result);
        this.discard();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (!(this.level() instanceof ServerLevel)) return;
        SpellElement element = getElement();
        if (result.getEntity() instanceof LivingEntity living) {
            Entity owner = this.getOwner();
            DamageSource source = owner instanceof Player player
                    ? this.damageSources().playerAttack(player)
                    : this.damageSources().indirectMagic(this, owner);
            living.hurt(source, getSpellDamage());
            element.applyOnHit(living, owner);
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(getElement().getImpactParticle(),
                    result.getLocation().x, result.getLocation().y, result.getLocation().z,
                    8, 0.1D, 0.1D, 0.1D, 0.02D);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("element", getElement().getId());
        tag.putFloat("damage", getSpellDamage());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(DATA_ELEMENT, tag.getString("element"));
        this.entityData.set(DATA_DAMAGE, tag.getFloat("damage"));
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 4096.0D;
    }
}
