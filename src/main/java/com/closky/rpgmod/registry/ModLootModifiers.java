package com.closky.rpgmod.registry;

import com.closky.rpgmod.RpgMod;
import com.closky.rpgmod.lootgen.AddWeaponLootModifier;
import com.mojang.serialization.Codec;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Регистрация сериализаторов глобальных лут-модификаторов.
 * Название здесь должно совпадать с полем "type" в data/rpgmod/loot_modifiers/*.json.
 */
public class ModLootModifiers {
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIERS =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, RpgMod.MODID);

    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> ADD_WEAPON =
            LOOT_MODIFIERS.register("add_weapon", () -> AddWeaponLootModifier.CODEC);

    private ModLootModifiers() {}

    public static void register(IEventBus modBus) {
        LOOT_MODIFIERS.register(modBus);
    }
}
