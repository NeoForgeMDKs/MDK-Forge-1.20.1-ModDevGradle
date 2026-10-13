package com.closky.rpgmod.village;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Мировые сохранённые данные о деревнях (SavedData = NBT-файл в папке мира).
 *
 * <p>Здесь лежат все деревни, их тип и прогресс развития. Благодаря SavedData деревня
 * «помнит» себя, даже если игрок не был рядом неделю.</p>
 */
public class VillageSavedData extends SavedData {
    public static final String DATA_NAME = "rpgmod_villages";

    private final Map<BlockPos, VillageData> villages = new HashMap<>();
    private final RandomSource random = RandomSource.create();

    public static VillageSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(VillageSavedData::load, VillageSavedData::new, DATA_NAME);
    }

    public static VillageSavedData load(CompoundTag tag) {
        VillageSavedData data = new VillageSavedData();
        CompoundTag villagesTag = tag.getCompound("Villages");
        for (String key : villagesTag.getAllKeys()) {
            CompoundTag entry = villagesTag.getCompound(key);
            BlockPos pos = BlockPos.of(entry.getLong("Pos"));
            data.villages.put(pos, new VillageData(pos, entry));
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        CompoundTag villagesTag = new CompoundTag();
        int index = 0;
        for (Map.Entry<BlockPos, VillageData> entry : villages.entrySet()) {
            CompoundTag value = entry.getValue().save();
            value.putLong("Pos", entry.getKey().asLong());
            villagesTag.put("village_" + (index++), value);
        }
        tag.put("Villages", villagesTag);
        return tag;
    }

    /** Найти деревню рядом с позицией или создать новую (тип выбирается случайно). */
    public VillageData getOrCreate(BlockPos pos) {
        for (VillageData data : villages.values()) {
            if (data.getCenter().distSqr(pos) < 96.0D * 96.0D) {
                return data;
            }
        }
        VillageType type = VillageType.values()[random.nextInt(VillageType.values().length)];
        VillageData data = new VillageData(pos, type);
        villages.put(pos, data);
        setDirty();
        return data;
    }

    public Collection<VillageData> getVillages() {
        return new ArrayList<>(villages.values());
    }

    public void markDirty() {
        setDirty();
    }
}
