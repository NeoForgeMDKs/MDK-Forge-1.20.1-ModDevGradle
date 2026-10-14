package com.closky.rpgmod.registry;

import com.closky.rpgmod.RpgMod;
import com.closky.rpgmod.block.entity.VillageHearthBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Регистрация блок-сущностей. */
public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, RpgMod.MODID);

    public static final RegistryObject<BlockEntityType<VillageHearthBlockEntity>> VILLAGE_HEARTH =
            BLOCK_ENTITIES.register("village_hearth",
                    () -> BlockEntityType.Builder.of(VillageHearthBlockEntity::new, ModBlocks.VILLAGE_HEARTH.get())
                            .build(null));

    private ModBlockEntities() {}

    public static void register(IEventBus modBus) {
        BLOCK_ENTITIES.register(modBus);
    }
}
