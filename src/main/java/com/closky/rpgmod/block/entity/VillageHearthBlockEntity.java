package com.closky.rpgmod.block.entity;

import com.closky.rpgmod.registry.ModBlockEntities;
import com.closky.rpgmod.village.VillageSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Блок-сущность очага. Раз в секунду отмечается в сохранённых данных деревень —
 * так сервер знает координаты всех деревень, даже если игрок далеко.
 */
public class VillageHearthBlockEntity extends BlockEntity {
    private int tickCounter;

    public VillageHearthBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.VILLAGE_HEARTH.get(), pos, state);
    }

    public void serverTick(ServerLevel level, BlockPos pos) {
        if (++tickCounter < 20) return;
        tickCounter = 0;
        VillageSavedData.get(level).getOrCreate(pos);
    }
}
