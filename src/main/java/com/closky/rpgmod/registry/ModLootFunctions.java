package com.closky.rpgmod.registry;

import com.closky.rpgmod.RpgMod;
import com.closky.rpgmod.lootgen.RollWeaponFunction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/** Регистрация лут-функций (то, что в JSON лут-таблиц пишется как {@code "function": "rpgmod:..."}). */
public class ModLootFunctions {
    public static final DeferredRegister<LootItemFunctionType> LOOT_FUNCTIONS =
            DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, RpgMod.MODID);

    public static final RegistryObject<LootItemFunctionType> ROLL_WEAPON =
            LOOT_FUNCTIONS.register("roll_weapon", () -> new LootItemFunctionType(new RollWeaponFunction.Serializer()));

    private ModLootFunctions() {}

    public static void register(IEventBus modBus) {
        LOOT_FUNCTIONS.register(modBus);
    }
}
