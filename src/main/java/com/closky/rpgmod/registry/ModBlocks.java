package com.closky.rpgmod.registry;

import com.closky.rpgmod.RpgMod;
import com.closky.rpgmod.block.BossAltarBlock;
import com.closky.rpgmod.block.VillageHearthBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Блоки мода: декор данжей, алтарь босса, очаг деревни. */
public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, RpgMod.MODID);

    public static final RegistryObject<Block> RUNE_BRICKS = register("rune_bricks",
            () -> new Block(BlockBehaviour.Properties.copy(net.minecraft.world.level.block.Blocks.DEEPSLATE_BRICKS)
                    .mapColor(MapColor.DEEPSLATE)));
    public static final RegistryObject<Block> CRACKED_RUNE_BRICKS = register("cracked_rune_bricks",
            () -> new Block(BlockBehaviour.Properties.copy(net.minecraft.world.level.block.Blocks.CRACKED_DEEPSLATE_BRICKS)
                    .mapColor(MapColor.DEEPSLATE)));
    public static final RegistryObject<Block> GILDED_RUNE_BRICKS = register("gilded_rune_bricks",
            () -> new Block(BlockBehaviour.Properties.copy(net.minecraft.world.level.block.Blocks.DEEPSLATE_BRICKS)
                    .mapColor(MapColor.GOLD)));
    public static final RegistryObject<Block> FROST_BRICKS = register("frost_bricks",
            () -> new Block(BlockBehaviour.Properties.copy(net.minecraft.world.level.block.Blocks.PACKED_ICE)
                    .mapColor(MapColor.ICE)
                    .sound(SoundType.GLASS)));
    public static final RegistryObject<Block> EMBER_BRICKS = register("ember_bricks",
            () -> new Block(BlockBehaviour.Properties.copy(net.minecraft.world.level.block.Blocks.NETHER_BRICKS)
                    .mapColor(MapColor.NETHER)));
    public static final RegistryObject<Block> SOUL_LAMP = register("soul_lamp",
            () -> new Block(BlockBehaviour.Properties.copy(net.minecraft.world.level.block.Blocks.SEA_LANTERN)
                    .mapColor(MapColor.COLOR_CYAN)
                    .lightLevel(state -> 15)));
    public static final RegistryObject<Block> BOSS_ALTAR = register("boss_altar",
            () -> new BossAltarBlock(BlockBehaviour.Properties.copy(net.minecraft.world.level.block.Blocks.DEEPSLATE_TILES)
                    .mapColor(MapColor.COLOR_RED)
                    .lightLevel(state -> 7)));
    public static final RegistryObject<Block> VILLAGE_HEARTH = register("village_hearth",
            () -> new VillageHearthBlock(BlockBehaviour.Properties.copy(net.minecraft.world.level.block.Blocks.STONE_BRICKS)
                    .mapColor(MapColor.COLOR_ORANGE)
                    .lightLevel(state -> 10)));

    private ModBlocks() {}

    private static RegistryObject<Block> register(String name, java.util.function.Supplier<Block> supplier) {
        return BLOCKS.register(name, supplier);
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
    }
}
