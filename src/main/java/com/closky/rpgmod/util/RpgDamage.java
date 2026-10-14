package com.closky.rpgmod.util;

import com.closky.rpgmod.RpgMod;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;

/**
 * Свои типы урона. Сами типы описаны в data/rpgmod/damage_type/*.json
 * (в 1.20.1 урон стал data-driven — это правильный способ добавить «кровотечение» и т.п.).
 *
 * <p>Важно: в 1.20.1 метод {@code DamageSources#source(...)} приватный, поэтому
 * {@link DamageSource} собирается напрямую из реестра типов урона — так делают все моды
 * на этой версии (Botania, ProjectE и др.).</p>
 */
public final class RpgDamage {
    public static final ResourceKey<DamageType> BLEEDING = key("bleeding");
    public static final ResourceKey<DamageType> FROSTBITE = key("frostbite");
    public static final ResourceKey<DamageType> SHOCK = key("shock");

    private RpgDamage() {}

    private static ResourceKey<DamageType> key(String name) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(RpgMod.MODID, name));
    }

    /** Источник урона по ключу из data/rpgmod/damage_type. */
    private static DamageSource of(Entity victim, ResourceKey<DamageType> key) {
        Holder<DamageType> holder = victim.level().registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(key);
        return new DamageSource(holder, null, null);
    }

    public static DamageSource bleeding(Entity victim) {
        return of(victim, BLEEDING);
    }

    public static DamageSource frostbite(Entity victim) {
        return of(victim, FROSTBITE);
    }

    public static DamageSource shock(Entity victim) {
        return of(victim, SHOCK);
    }
}
