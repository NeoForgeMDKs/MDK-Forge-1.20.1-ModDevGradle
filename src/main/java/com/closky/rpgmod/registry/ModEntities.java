package com.closky.rpgmod.registry;

import com.closky.rpgmod.RpgMod;
import com.closky.rpgmod.entity.CaveStalker;
import com.closky.rpgmod.entity.DungeonWarden;
import com.closky.rpgmod.entity.SpellProjectile;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Регистрация сущностей (мобов и снарядов). */
public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, RpgMod.MODID);

    public static final RegistryObject<EntityType<SpellProjectile>> SPELL_PROJECTILE = ENTITIES.register("spell_projectile",
            () -> EntityType.Builder.<SpellProjectile>of(SpellProjectile::new, MobCategory.MISC)
                    .sized(0.45F, 0.45F)
                    .clientTrackingRange(6)
                    .updateInterval(2)
                    .build("spell_projectile"));

    public static final RegistryObject<EntityType<DungeonWarden>> DUNGEON_WARDEN = ENTITIES.register("dungeon_warden",
            () -> EntityType.Builder.of(DungeonWarden::new, MobCategory.MONSTER)
                    .sized(0.95F, 2.45F)
                    .clientTrackingRange(12)
                    .build("dungeon_warden"));

    public static final RegistryObject<EntityType<CaveStalker>> CAVE_STALKER = ENTITIES.register("cave_stalker",
            () -> EntityType.Builder.of(CaveStalker::new, MobCategory.MONSTER)
                    .sized(0.7F, 1.5F)
                    .clientTrackingRange(10)
                    .build("cave_stalker"));

    private ModEntities() {}

    public static void register(IEventBus modBus) {
        ENTITIES.register(modBus);
    }
}
