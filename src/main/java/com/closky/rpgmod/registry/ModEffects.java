package com.closky.rpgmod.registry;

import com.closky.rpgmod.RpgMod;
import com.closky.rpgmod.effect.BleedingEffect;
import com.closky.rpgmod.effect.FrostbiteEffect;
import com.closky.rpgmod.effect.NatureBlessingEffect;
import com.closky.rpgmod.effect.ShockedEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Свои эффекты (MobEffect).
 * Добавить новый: создайте класс-наследник MobEffect и допишите строчку в register().
 */
public class ModEffects {
    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, RpgMod.MODID);

    public static final RegistryObject<MobEffect> BLEEDING = EFFECTS.register("bleeding", BleedingEffect::new);
    public static final RegistryObject<MobEffect> FROSTBITE = EFFECTS.register("frostbite", FrostbiteEffect::new);
    public static final RegistryObject<MobEffect> SHOCKED = EFFECTS.register("shocked", ShockedEffect::new);
    public static final RegistryObject<MobEffect> NATURE_BLESSING =
            EFFECTS.register("nature_blessing", NatureBlessingEffect::new);

    private ModEffects() {}

    public static void register(IEventBus modBus) {
        EFFECTS.register(modBus);
    }
}
