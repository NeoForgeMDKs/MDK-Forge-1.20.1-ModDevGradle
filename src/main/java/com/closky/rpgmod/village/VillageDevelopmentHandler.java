package com.closky.rpgmod.village;

import com.closky.rpgmod.RpgMod;
import com.closky.rpgmod.config.RpgConfig;
import com.closky.rpgmod.entity.CaveStalker;
import com.closky.rpgmod.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * Самостоятельное развитие деревень (требование 2к, третья часть).
 *
 * <p>Как это работает:</p>
 * <ol>
 *     <li>очаг деревни ({@code rpgmod:village_hearth}) регистрирует деревню в SavedData;</li>
 *     <li>раз в N тиков сервер даёт деревне «очки роста» (тем больше, чем больше жителей);</li>
 *     <li>когда очков хватает — деревня строит новую постройку: сервер ПРИНУДИТЕЛЬНО грузит
 *     нужный чанк ({@code setChunkForced}), ставит структуру из шаблона .nbt, а через несколько
 *     секунд отпускает чанк;</li>
 *     <li>поэтому деревня растёт, даже если игрока рядом нет.</li>
 * </ol>
 *
 * <p>Шаблоны построек: {@code data/rpgmod/structures/village/*.nbt} (генерируются скриптом
 * tools/nbt_builder.py — см. docs/07_Данжи_и_структуры.md).</p>
 */
@Mod.EventBusSubscriber(modid = RpgMod.MODID)
public class VillageDevelopmentHandler {
    private static final String[] COMMON_BUILDINGS = {
            "village/house_small", "village/house_big", "village/farm"
    };

    private static final List<ForcedChunk> FORCED_CHUNKS = new ArrayList<>();
    private static int tickCounter;

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!RpgConfig.VILLAGE_DEVELOPMENT_ENABLED.get()) return;

        Iterator<ForcedChunk> iterator = FORCED_CHUNKS.iterator();
        while (iterator.hasNext()) {
            ForcedChunk forced = iterator.next();
            forced.ticksLeft--;
            if (forced.ticksLeft <= 0) {
                forced.level.setChunkForced(forced.pos.getX() >> 4, forced.pos.getZ() >> 4, false);
                iterator.remove();
            }
        }

        if (++tickCounter < RpgConfig.VILLAGE_DEVELOPMENT_INTERVAL_TICKS.get()) return;
        tickCounter = 0;

        for (ServerLevel level : event.getServer().getAllLevels()) {
            VillageSavedData data = VillageSavedData.get(level);
            for (VillageData village : data.getVillages()) {
                develop(level, data, village);
            }
        }
    }

    private static void develop(ServerLevel level, VillageSavedData savedData, VillageData village) {
        BlockPos center = village.getCenter();

        if (level.isLoaded(center)) {
            int population = level.getEntitiesOfClass(Villager.class, new AABB(center).inflate(village.getRadius())).size();
            village.setPopulation(population);
        }

        int perCycle = RpgConfig.VILLAGE_GROWTH_POINTS_PER_CYCLE.get();
        int workers = Math.max(1, village.getPopulation() / 4);
        village.addGrowthPoints(perCycle * workers);

        int cost = RpgConfig.VILLAGE_GROWTH_POINTS_PER_BUILDING.get();
        if (!village.canBuild(cost)) {
            savedData.markDirty();
            return;
        }

        Optional<BlockPos> spot = findSpot(level, village);
        if (spot.isEmpty()) {
            savedData.markDirty();
            return;
        }

        String template = pickTemplate(village, level.random);
        if (placeBuilding(level, spot.get(), template)) {
            village.consumeGrowth(cost);
            village.onBuildingBuilt();
            spawnVillager(level, spot.get());
            if (level.random.nextFloat() < 0.25F) {
                spawnGuard(level, spot.get());
            }
            RpgMod.LOGGER.debug("Деревня {} построила {} (всего построек: {})",
                    center, template, village.getBuildingsBuilt());
        }
        savedData.markDirty();
    }

    private static Optional<BlockPos> findSpot(ServerLevel level, VillageData village) {
        RandomSource random = level.random;
        BlockPos center = village.getCenter();
        for (int attempt = 0; attempt < 24; attempt++) {
            int radius = 8 + random.nextInt(Math.max(8, village.getRadius() - 8));
            double angle = random.nextDouble() * Math.PI * 2.0D;
            int x = center.getX() + (int) (Math.cos(angle) * radius);
            int z = center.getZ() + (int) (Math.sin(angle) * radius);
            BlockPos surface = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, new BlockPos(x, 0, z));
            if (surface.getY() < level.getMinBuildHeight() + 2) continue;
            BlockPos ground = surface.below();
            if (!level.getFluidState(ground).isEmpty()) continue;
            if (!level.getBlockState(ground).isFaceSturdy(level, ground, Direction.UP)) continue;
            return Optional.of(surface);
        }
        return Optional.empty();
    }

    private static String pickTemplate(VillageData village, RandomSource random) {
        // Каждая третья постройка — мастерская, специфичная для типа деревни
        // (именно из-за неё в деревне появляются нужные профессии жителей).
        if (village.getBuildingsBuilt() % 3 == 2) {
            return "village/workshop_" + village.getType().getId();
        }
        return COMMON_BUILDINGS[random.nextInt(COMMON_BUILDINGS.length)];
    }

    /** Поставить постройку. Чанк грузится принудительно, чтобы работало без игрока рядом. */
    public static boolean placeBuilding(ServerLevel level, BlockPos pos, String templatePath) {
        StructureTemplate template = level.getStructureManager()
                .get(new ResourceLocation(RpgMod.MODID, templatePath)).orElse(null);
        if (template == null) return false;

        level.setChunkForced(pos.getX() >> 4, pos.getZ() >> 4, true);
        FORCED_CHUNKS.add(new ForcedChunk(level, pos, RpgConfig.VILLAGE_FORCED_CHUNK_TICKS.get()));

        StructurePlaceSettings settings = new StructurePlaceSettings()
                .setIgnoreEntities(false)
                .setRotation(Rotation.getRandom(level.random))
                .setMirror(Mirror.NONE);

        template.placeInWorld(level, pos, pos, settings, level.random, Block.UPDATE_CLIENTS);
        return true;
    }

    private static void spawnVillager(ServerLevel level, BlockPos pos) {
        Villager villager = EntityType.VILLAGER.create(level);
        if (villager != null) {
            villager.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, level.random.nextFloat() * 360.0F, 0.0F);
            villager.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.STRUCTURE, null, null);
            level.addFreshEntity(villager);
        }
    }

    private static void spawnGuard(ServerLevel level, BlockPos pos) {
        CaveStalker guard = ModEntities.CAVE_STALKER.get().create(level);
        if (guard != null) {
            guard.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, 0.0F, 0.0F);
            guard.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.STRUCTURE, null, null);
            level.addFreshEntity(guard);
        }
    }

    /** Чанк, удерживаемый загруженным (обычный класс, а не record — счётчик должен меняться). */
    private static final class ForcedChunk {
        private final ServerLevel level;
        private final BlockPos pos;
        private int ticksLeft;

        private ForcedChunk(ServerLevel level, BlockPos pos, int ticksLeft) {
            this.level = level;
            this.pos = pos;
            this.ticksLeft = ticksLeft;
        }
    }
}
