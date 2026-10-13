package com.closky.rpgmod.util;

import com.closky.rpgmod.RpgMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;

/**
 * Свои типы урона. Сами типы описаны в data/rpgmod/damage_type/*.json
 * (в 1.20.1 урон стал data-driven — это правильный способ добавить «кровотечение» и т.п.).
 */
public final class RpgDamage {
    public static final ResourceKey<net.minecraft.world.damagesource.DamageType> BLEEDING =
            key("bleeding");
    public static final ResourceKey<net.minecraft.world.damagesource.DamageType> FROSTBITE =
            key("frostbite");
    public static final ResourceKey<net.minecraft.world.damagesource.DamageType> SHOCK =
            key("shock");

    private RpgDamage() {}

    private static ResourceKey<net.minecraft.world.damagesource.DamageType> key(String name) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(RpgMod.MODID, name));
    }

    public static DamageSource bleeding(Entity victim) {
        return victim.damageSources().source(BLEEDING);
    }

    public static DamageSource frostbite(Entity victim) {
        return victim.damageSources().source(FROSTBITE);
    }

    public static DamageSource shock(Entity victim) {
        return victim.damageSources().source(SHOCK);
    }
}
