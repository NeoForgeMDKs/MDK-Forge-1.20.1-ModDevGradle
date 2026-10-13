package com.closky.rpgmod;

import com.closky.rpgmod.config.RpgClientConfig;
import com.closky.rpgmod.config.RpgConfig;
import com.closky.rpgmod.entity.CaveStalker;
import com.closky.rpgmod.entity.DungeonWarden;
import com.closky.rpgmod.network.ModNetwork;
import com.closky.rpgmod.registry.ModBlockEntities;
import com.closky.rpgmod.registry.ModBlocks;
import com.closky.rpgmod.registry.ModCreativeTabs;
import com.closky.rpgmod.registry.ModEffects;
import com.closky.rpgmod.registry.ModEntities;
import com.closky.rpgmod.registry.ModItems;
import com.closky.rpgmod.registry.ModLootFunctions;
import com.closky.rpgmod.registry.ModLootModifiers;
import com.mojang.logging.LogUtils;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

/**
 * Главный класс мода.
 *
 * <p>Здесь только: регистрация DeferredRegister'ов, загрузка конфигов и общий setup.
 * Вся игровая логика лежит в подпакетах:</p>
 * <ul>
 *     <li>{@code item} — оружие, посохи, броня, генерация лута</li>
 *     <li>{@code player} — прокачка персонажа, статы, классы</li>
 *     <li>{@code enemy} — уровни враждебных мобов</li>
 *     <li>{@code magic} — магия (огонь/лёд/молния/природа)</li>
 *     <li>{@code entity} — свои мобы и снаряды</li>
 *     <li>{@code village} — развитие деревень</li>
 *     <li>{@code worldgen} — генерация мира (в основном JSON'ы в resources)</li>
 * </ul>
 */
@Mod(RpgMod.MODID)
public class RpgMod {
    /** ID мода. Обязан совпадать с mod_id в gradle.properties и mods.toml. */
    public static final String MODID = "rpgmod";
    public static final Logger LOGGER = LogUtils.getLogger();

    public RpgMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        // --- Регистрация контента -------------------------------------------------
        ModBlocks.register(modBus);
        ModBlockEntities.register(modBus);
        ModItems.register(modBus);
        ModCreativeTabs.register(modBus);
        ModEffects.register(modBus);
        ModEntities.register(modBus);
        ModLootFunctions.register(modBus);
        ModLootModifiers.register(modBus);

        // --- События жизненного цикла мода ---------------------------------------
        modBus.addListener(this::commonSetup);
        modBus.addListener(RpgMod::onEntityAttributes);

        // --- Конфиги --------------------------------------------------------------
        // COMMON — серверные/игровые значения (работают и в одиночной игре, и на сервере)
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, RpgConfig.SPEC, "rpgmod-common.toml");
        // CLIENT — чисто клиентские настройки (например, показывать ли HP над мобами).
        // У этой категории есть кнопка "Config" в списке модов прямо в игре.
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, RpgClientConfig.SPEC, "rpgmod-client.toml");

        LOGGER.info("RPG Overhaul загружается...");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Сетевые пакеты регистрируем в enqueueWork — это безопасный поток.
        event.enqueueWork(ModNetwork::register);
    }

    /** Атрибуты (здоровье, урон) для наших мобов. Вызывается Forge'ом один раз при старте. */
    private static void onEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.DUNGEON_WARDEN.get(), DungeonWarden.createAttributes().build());
        event.put(ModEntities.CAVE_STALKER.get(), CaveStalker.createAttributes().build());
    }
}
