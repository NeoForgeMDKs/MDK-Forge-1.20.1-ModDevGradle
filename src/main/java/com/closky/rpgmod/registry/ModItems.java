package com.closky.rpgmod.registry;

import com.closky.rpgmod.RpgMod;
import com.closky.rpgmod.item.DescribedItem;
import com.closky.rpgmod.item.HoneMaterial;
import com.closky.rpgmod.item.MagicStaffItem;
import com.closky.rpgmod.item.PlayerArmorMaterials;
import com.closky.rpgmod.item.RpgArmorItem;
import com.closky.rpgmod.item.RpgWeaponItem;
import com.closky.rpgmod.item.WeaponClass;
import com.closky.rpgmod.item.WeaponMaterial;
import com.closky.rpgmod.magic.SpellElement;
import com.closky.rpgmod.player.PlayerClass;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * ВСЕ предметы мода.
 *
 * <p>Как добавить свой предмет (кратко, подробно — в docs/01_Предметы_и_лут.md):</p>
 * <ol>
 *     <li>добавьте поле {@code public static final RegistryObject<Item> MY_ITEM = ITEMS.register("my_item", ...)};</li>
 *     <li>создайте JSON-модель в assets/rpgmod/models/item/my_item.json;</li>
 *     <li>название — в assets/rpgmod/lang/ru_ru.json и en_us.json;</li>
 *     <li>текстуру — в assets/rpgmod/textures/item/my_item.png.</li>
 * </ol>
 */
public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, RpgMod.MODID);

    // ------------------------------------------------------------------ материалы

    public static final RegistryObject<Item> STEEL_INGOT = ITEMS.register("steel_ingot",
            () -> new DescribedItem("item.rpgmod.steel_ingot.desc", false, new Item.Properties()));
    public static final RegistryObject<Item> SILVER_INGOT = ITEMS.register("silver_ingot",
            () -> new DescribedItem("item.rpgmod.silver_ingot.desc", false, new Item.Properties()));
    public static final RegistryObject<Item> DUSK_INGOT = ITEMS.register("dusk_ingot",
            () -> new DescribedItem("item.rpgmod.dusk_ingot.desc", false, new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> ARCANE_DUST = ITEMS.register("arcane_dust",
            () -> new DescribedItem("item.rpgmod.arcane_dust.desc", false, new Item.Properties()));
    public static final RegistryObject<Item> FROST_SHARD = ITEMS.register("frost_shard",
            () -> new DescribedItem("item.rpgmod.frost_shard.desc", false, new Item.Properties()));
    public static final RegistryObject<Item> EMBER_INGOT = ITEMS.register("ember_ingot",
            () -> new DescribedItem("item.rpgmod.ember_ingot.desc", false, new Item.Properties()));
    public static final RegistryObject<Item> VOID_SHARD = ITEMS.register("void_shard",
            () -> new DescribedItem("item.rpgmod.void_shard.desc", true, new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> MANA_CRYSTAL = ITEMS.register("mana_crystal",
            () -> new DescribedItem("item.rpgmod.mana_crystal.desc", false, new Item.Properties()));
    public static final RegistryObject<Item> DUNGEON_KEY = ITEMS.register("dungeon_key",
            () -> new DescribedItem("item.rpgmod.dungeon_key.desc", true, new Item.Properties().rarity(Rarity.UNCOMMON)));

    // -------------------------------------------------------------------- оружие

    private static final Map<WeaponClass, Map<WeaponMaterial, RegistryObject<Item>>> WEAPONS =
            new EnumMap<>(WeaponClass.class);
    private static final Map<WeaponMaterial, Integer> MATERIAL_WEIGHTS = Map.of(
            WeaponMaterial.STEEL, 6,
            WeaponMaterial.SILVER, 3,
            WeaponMaterial.DUSKSTEEL, 1);

    // --------------------------------------------------------------------- броня

    private static final Map<PlayerClass, Map<ArmorItem.Type, RegistryObject<Item>>> ARMOR =
            new EnumMap<>(PlayerClass.class);

    // -------------------------------------------------------------------- посохи

    public static final RegistryObject<Item> FIRE_STAFF = ITEMS.register("fire_staff",
            () -> new MagicStaffItem(SpellElement.FIRE, new Item.Properties().stacksTo(1).durability(260)));
    public static final RegistryObject<Item> FROST_STAFF = ITEMS.register("frost_staff",
            () -> new MagicStaffItem(SpellElement.FROST, new Item.Properties().stacksTo(1).durability(260)));
    public static final RegistryObject<Item> STORM_STAFF = ITEMS.register("storm_staff",
            () -> new MagicStaffItem(SpellElement.LIGHTNING, new Item.Properties().stacksTo(1).durability(260)));
    public static final RegistryObject<Item> NATURE_STAFF = ITEMS.register("nature_staff",
            () -> new MagicStaffItem(SpellElement.NATURE, new Item.Properties().stacksTo(1).durability(260)));

    // ------------------------------------------------------------- блоки и спавны

    public static final RegistryObject<Item> RUNE_BRICKS_ITEM = blockItem("rune_bricks", ModBlocks.RUNE_BRICKS);
    public static final RegistryObject<Item> CRACKED_RUNE_BRICKS_ITEM = blockItem("cracked_rune_bricks", ModBlocks.CRACKED_RUNE_BRICKS);
    public static final RegistryObject<Item> GILDED_RUNE_BRICKS_ITEM = blockItem("gilded_rune_bricks", ModBlocks.GILDED_RUNE_BRICKS);
    public static final RegistryObject<Item> FROST_BRICKS_ITEM = blockItem("frost_bricks", ModBlocks.FROST_BRICKS);
    public static final RegistryObject<Item> EMBER_BRICKS_ITEM = blockItem("ember_bricks", ModBlocks.EMBER_BRICKS);
    public static final RegistryObject<Item> SOUL_LAMP_ITEM = blockItem("soul_lamp", ModBlocks.SOUL_LAMP);
    public static final RegistryObject<Item> BOSS_ALTAR_ITEM = blockItem("boss_altar", ModBlocks.BOSS_ALTAR);
    public static final RegistryObject<Item> VILLAGE_HEARTH_ITEM = blockItem("village_hearth", ModBlocks.VILLAGE_HEARTH);

    public static final RegistryObject<Item> DUNGEON_WARDEN_SPAWN_EGG = ITEMS.register("dungeon_warden_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.DUNGEON_WARDEN, 0x5B1E1E, 0xC0392B, new Item.Properties()));
    public static final RegistryObject<Item> CAVE_STALKER_SPAWN_EGG = ITEMS.register("cave_stalker_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.CAVE_STALKER, 0x1E3B2E, 0x62E06B, new Item.Properties()));

    static {
        // Оружие: 6 классов x 3 материала = 18 предметов.
        for (WeaponClass weaponClass : WeaponClass.values()) {
            Map<WeaponMaterial, RegistryObject<Item>> perMaterial = new EnumMap<>(WeaponMaterial.class);
            for (WeaponMaterial material : WeaponMaterial.values()) {
                String name = material.getId() + "_" + weaponClass.getId();
                perMaterial.put(material, ITEMS.register(name,
                        () -> new RpgWeaponItem(weaponClass, material, new Item.Properties())));
            }
            WEAPONS.put(weaponClass, perMaterial);
        }
        // Броня: 4 класса x 4 слота = 16 предметов.
        for (PlayerClass playerClass : new PlayerClass[]{PlayerClass.WARRIOR, PlayerClass.RANGER, PlayerClass.MAGE, PlayerClass.DRUID}) {
            Map<ArmorItem.Type, RegistryObject<Item>> perSlot = new EnumMap<>(ArmorItem.Type.class);
            for (ArmorItem.Type type : new ArmorItem.Type[]{ArmorItem.Type.HELMET, ArmorItem.Type.CHESTPLATE,
                    ArmorItem.Type.LEGGINGS, ArmorItem.Type.BOOTS}) {
                String name = playerClass.getId() + "_" + type.getName();
                perSlot.put(type, ITEMS.register(name,
                        () -> new RpgArmorItem(playerClass, type, PlayerArmorMaterials.forClass(playerClass),
                                new Item.Properties())));
            }
            ARMOR.put(playerClass, perSlot);
        }
    }

    private ModItems() {}

    private static RegistryObject<Item> blockItem(String name, RegistryObject<net.minecraft.world.level.block.Block> block) {
        return ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    // ------------------------------------------------------------------- выборки

    /** Случайное оружие (используется при генерации лута). Редкие материалы выпадают реже. */
    public static Item getRandomWeapon(RandomSource random) {
        WeaponClass weaponClass = WeaponClass.values()[random.nextInt(WeaponClass.values().length)];
        int total = 0;
        for (int weight : MATERIAL_WEIGHTS.values()) total += weight;
        int pick = random.nextInt(total);
        WeaponMaterial chosen = WeaponMaterial.STEEL;
        for (Map.Entry<WeaponMaterial, Integer> entry : MATERIAL_WEIGHTS.entrySet()) {
            pick -= entry.getValue();
            if (pick < 0) {
                chosen = entry.getKey();
                break;
            }
        }
        return WEAPONS.get(weaponClass).get(chosen).get();
    }

    public static Item getArmor(PlayerClass playerClass, ArmorItem.Type type) {
        Map<ArmorItem.Type, RegistryObject<Item>> map = ARMOR.get(playerClass);
        return map == null ? null : map.get(type).get();
    }

    /** Все предметы для творческой вкладки. */
    public static List<Supplier<Item>> creativeTabItems() {
        List<Supplier<Item>> list = new ArrayList<>();
        list.add(STEEL_INGOT);
        list.add(SILVER_INGOT);
        list.add(DUSK_INGOT);
        list.add(ARCANE_DUST);
        list.add(FROST_SHARD);
        list.add(EMBER_INGOT);
        list.add(VOID_SHARD);
        list.add(MANA_CRYSTAL);
        list.add(DUNGEON_KEY);
        for (Map<WeaponMaterial, RegistryObject<Item>> perMaterial : WEAPONS.values()) {
            list.addAll(perMaterial.values());
        }
        list.add(FIRE_STAFF);
        list.add(FROST_STAFF);
        list.add(STORM_STAFF);
        list.add(NATURE_STAFF);
        for (Map<ArmorItem.Type, RegistryObject<Item>> perSlot : ARMOR.values()) {
            list.addAll(perSlot.values());
        }
        list.add(RUNE_BRICKS_ITEM);
        list.add(CRACKED_RUNE_BRICKS_ITEM);
        list.add(GILDED_RUNE_BRICKS_ITEM);
        list.add(FROST_BRICKS_ITEM);
        list.add(EMBER_BRICKS_ITEM);
        list.add(SOUL_LAMP_ITEM);
        list.add(BOSS_ALTAR_ITEM);
        list.add(VILLAGE_HEARTH_ITEM);
        list.add(DUNGEON_WARDEN_SPAWN_EGG);
        list.add(CAVE_STALKER_SPAWN_EGG);
        return list;
    }

    /** Пригодно ли это для подковки на наковальне. */
    public static HoneMaterial honeMaterialOf(Item item) {
        return HoneMaterial.byItem(item);
    }


    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }
}
