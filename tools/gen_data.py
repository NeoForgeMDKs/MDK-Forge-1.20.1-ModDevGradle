#!/usr/bin/env python3
"""Генератор data-JSON'ов мода RPG Overhaul.

Запуск:  python3 tools/gen_data.py

Что создаётся в src/main/resources/data/:
  rpgmod/damage_type/{bleeding,frostbite,shock}.json      — типы урона для эффектов
  rpgmod/loot_tables/chests/*.json                        — лут сундуков (оружие, материалы, броня)
  rpgmod/loot_modifiers/*.json                            — добавка оружия в ванильные сундуки
  forge/loot_modifiers/global_loot_modifiers.json         — список подключённых лут-модификаторов
  rpgmod/tags/worldgen/biome/has_structure/*.json         — в каких биомах спавнятся наши структуры
  rpgmod/worldgen/structure/*.json                        — сами структуры (jigsaw, 1 элемент)
  rpgmod/worldgen/structure_set/*.json                    — как часто и где они расставляются
  rpgmod/worldgen/template_pool/*.json                    — какие .nbt-шаблоны используются
  minecraft/tags/blocks/...                               — кирки/топоры для наших блоков

JSON'ы не документация, а рабочий код генерации мира: правьте числа и перезапускайте скрипт
(или правьте JSON руками — они читаемые).
"""
from __future__ import annotations

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
RES = ROOT / "src/main/resources"
RP = RES / "data/rpgmod"
FORGE = RES / "data/forge"
MC = RES / "data/minecraft"

# ------------------------------------------------------------------- утилиты

def write_json(path: Path, data) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")


def item(name: str, weight: int = 1, count: tuple[int, int] | None = None,
         functions: list[dict] | None = None, quality: int | None = None) -> dict:
    entry: dict = {"type": "minecraft:item", "name": name, "weight": weight}
    if quality is not None:
        entry["quality"] = quality
    funcs = list(functions or [])
    if count is not None:
        funcs.insert(0, {"function": "minecraft:set_count",
                         "count": {"type": "minecraft:uniform", "min": float(count[0]), "max": float(count[1])}})
    if funcs:
        entry["functions"] = funcs
    return entry


def empty(weight: int) -> dict:
    return {"type": "minecraft:empty", "weight": weight}


def roll_weapon(min_level: int, max_level: int, hone_chance: float = 0.2) -> dict:
    """Наша лут-функция: превращает базовый предмет в случайное оружие нужного уровня."""
    return {"function": "rpgmod:roll_weapon", "min_level": min_level,
            "max_level": max_level, "hone_chance": hone_chance}


def pool(rolls: int | dict, entries: list[dict], conditions: list[dict] | None = None) -> dict:
    result: dict = {"rolls": rolls if isinstance(rolls, dict) else int(rolls), "entries": entries}
    if conditions:
        result["conditions"] = conditions
    return result


# ---------------------------------------------------------------- 1. урон

def gen_damage_types() -> None:
    write_json(RP / "damage_type/bleeding.json",
               {"message_id": "bleeding", "exhaustion": 0.1, "scaling": "when_caused_by_living_non_player"})
    write_json(RP / "damage_type/frostbite.json",
               {"message_id": "frostbite", "exhaustion": 0.1, "scaling": "when_caused_by_living_non_player"})
    write_json(RP / "damage_type/shock.json",
               {"message_id": "shock", "exhaustion": 0.0, "scaling": "when_caused_by_living_non_player"})
    print("  damage_type: bleeding, frostbite, shock")


# ------------------------------------------------------------- 2. лут сундуков

def gen_loot_tables() -> None:
    # обычные сундуки подземелий: материалы + редкое оружие
    write_json(RP / "loot_tables/chests/dungeon_common.json", {
        "type": "minecraft:chest",
        "pools": [
            pool({"type": "minecraft:uniform", "min": 2.0, "max": 4.0}, [
                item("rpgmod:steel_ingot", 10, (1, 4)),
                item("rpgmod:silver_ingot", 6, (1, 2)),
                item("rpgmod:arcane_dust", 6, (2, 5)),
                item("rpgmod:ember_ingot", 2, (1, 2)),
                item("minecraft:emerald", 6, (1, 3)),
                item("rpgmod:mana_crystal", 3, (1, 1)),
                empty(4),
            ]),
            pool(1, [
                item("rpgmod:steel_sword", 4, functions=[roll_weapon(2, 5, 0.15)]),
                item("rpgmod:steel_dagger", 4, functions=[roll_weapon(2, 5, 0.15)]),
                item("rpgmod:steel_axe", 3, functions=[roll_weapon(3, 6, 0.15)]),
                item("rpgmod:silver_sword", 2, functions=[roll_weapon(4, 7, 0.3)]),
                empty(6),
            ]),
        ],
    })

    # сокровищница: много ценного + гарантированное оружие приличного уровня
    write_json(RP / "loot_tables/chests/dungeon_treasure.json", {
        "type": "minecraft:chest",
        "pools": [
            pool({"type": "minecraft:uniform", "min": 2.0, "max": 3.0}, [
                item("rpgmod:steel_sword", 3, functions=[roll_weapon(5, 9, 0.4)]),
                item("rpgmod:steel_greatsword", 3, functions=[roll_weapon(5, 9, 0.4)]),
                item("rpgmod:silver_spear", 2, functions=[roll_weapon(6, 10, 0.5)]),
                item("rpgmod:silver_mace", 2, functions=[roll_weapon(6, 10, 0.5)]),
            ]),
            pool(3, [
                item("rpgmod:dusk_ingot", 3, (1, 2)),
                item("rpgmod:silver_ingot", 8, (2, 5)),
                item("rpgmod:arcane_dust", 8, (3, 7)),
                item("rpgmod:ember_ingot", 5, (1, 4)),
                item("rpgmod:frost_shard", 5, (1, 3)),
                item("rpgmod:void_shard", 2, (1, 1)),
                item("rpgmod:mana_crystal", 6, (1, 3)),
                item("rpgmod:dungeon_key", 4, (1, 1)),
                item("minecraft:emerald", 8, (3, 8)),
                item("minecraft:golden_apple", 3, (1, 2)),
            ]),
        ],
    })

    # лут с босса: легендарное оружие
    write_json(RP / "loot_tables/chests/dungeon_boss.json", {
        "type": "minecraft:chest",
        "pools": [
            pool(1, [
                item("rpgmod:dusksteel_greatsword", 1, functions=[roll_weapon(10, 15, 1.0)]),
            ]),
            pool(1, [
                item("rpgmod:dusksteel_sword", 1, functions=[roll_weapon(9, 14, 0.8)]),
                item("rpgmod:dusksteel_mace", 1, functions=[roll_weapon(9, 14, 0.8)]),
                item("rpgmod:dusksteel_spear", 1, functions=[roll_weapon(9, 14, 0.8)]),
                item("rpgmod:dusksteel_axe", 1, functions=[roll_weapon(9, 14, 0.8)]),
                item("rpgmod:dusksteel_dagger", 1, functions=[roll_weapon(9, 14, 0.8)]),
            ]),
            pool(2, [
                item("rpgmod:dusk_ingot", 6, (2, 5)),
                item("rpgmod:void_shard", 5, (1, 3)),
                item("rpgmod:mana_crystal", 6, (2, 5)),
                item("rpgmod:arcane_dust", 6, (4, 8)),
                item("rpgmod:dungeon_key", 4, (1, 2)),
                item("minecraft:emerald_block", 3, (1, 1)),
                item("minecraft:diamond", 4, (2, 6)),
            ]),
        ],
    })

    # замок: смесь оружия и брони
    write_json(RP / "loot_tables/chests/castle.json", {
        "type": "minecraft:chest",
        "pools": [
            pool(2, [
                item("rpgmod:steel_sword", 3, functions=[roll_weapon(4, 8, 0.3)]),
                item("rpgmod:silver_greatsword", 3, functions=[roll_weapon(5, 9, 0.4)]),
                item("rpgmod:silver_axe", 2, functions=[roll_weapon(5, 9, 0.4)]),
                empty(3),
            ]),
            pool(2, [
                item("rpgmod:warrior_chestplate", 4, (1, 1)),
                item("rpgmod:warrior_helmet", 4, (1, 1)),
                item("rpgmod:ranger_leggings", 3, (1, 1)),
                item("rpgmod:mage_chestplate", 2, (1, 1)),
                item("rpgmod:druid_boots", 2, (1, 1)),
                empty(4),
            ]),
            pool(2, [
                item("rpgmod:steel_ingot", 8, (3, 6)),
                item("rpgmod:silver_ingot", 6, (2, 4)),
                item("rpgmod:arcane_dust", 4, (2, 6)),
                item("minecraft:emerald", 6, (2, 6)),
                item("minecraft:gold_ingot", 4, (2, 6)),
            ]),
        ],
    })

    # башня
    write_json(RP / "loot_tables/chests/tower.json", {
        "type": "minecraft:chest",
        "pools": [
            pool(2, [
                item("rpgmod:steel_sword", 3, functions=[roll_weapon(3, 6, 0.2)]),
                item("rpgmod:steel_spear", 3, functions=[roll_weapon(3, 6, 0.2)]),
                empty(5),
            ]),
            pool(3, [
                item("rpgmod:arcane_dust", 6, (1, 4)),
                item("rpgmod:mana_crystal", 4, (1, 2)),
                item("rpgmod:frost_shard", 3, (1, 2)),
                item("minecraft:emerald", 5, (1, 4)),
                item("minecraft:book", 3, (1, 3)),
                item("minecraft:bread", 4, (1, 4)),
            ]),
        ],
    })

    # деревня: еда, материалы, броня класса (оружие у жителей не лежит — только лут с врагов)
    write_json(RP / "loot_tables/chests/village_house.json", {
        "type": "minecraft:chest",
        "pools": [
            pool({"type": "minecraft:uniform", "min": 2.0, "max": 3.0}, [
                item("minecraft:bread", 8, (1, 5)),
                item("minecraft:carrot", 6, (2, 5)),
                item("minecraft:potato", 6, (2, 5)),
                item("rpgmod:steel_ingot", 5, (1, 3)),
                item("minecraft:emerald", 6, (1, 3)),
                item("rpgmod:arcane_dust", 3, (1, 3)),
                empty(3),
            ]),
        ],
    })

    for cls, armor in (("warrior", "warrior"), ("ranger", "ranger"),
                       ("mage", "mage"), ("druid", "druid")):
        write_json(RP / f"loot_tables/chests/village_workshop_{cls}.json", {
            "type": "minecraft:chest",
            "pools": [
                pool(1, [
                    item(f"rpgmod:{armor}_helmet", 4, (1, 1)),
                    item(f"rpgmod:{armor}_chestplate", 3, (1, 1)),
                    item(f"rpgmod:{armor}_leggings", 3, (1, 1)),
                    item(f"rpgmod:{armor}_boots", 4, (1, 1)),
                    empty(3),
                ]),
                pool(2, [
                    item("rpgmod:steel_ingot", 6, (1, 4)),
                    item("rpgmod:arcane_dust", 4, (1, 4)),
                    item("rpgmod:mana_crystal", 3, (1, 1)),
                    item("minecraft:emerald", 6, (1, 5)),
                ]),
            ],
        })

    print("  loot_tables: dungeon_common, dungeon_treasure, dungeon_boss, castle, tower, village_house, "
          "village_workshop_x4")


# ------------------------------------------------- 3. глобальные лут-модификаторы

def gen_loot_modifiers() -> None:
    def modifier(name: str, tables: list[str], min_level: int, max_level: int, hone: float) -> None:
        write_json(RP / f"loot_modifiers/{name}.json", {
            "type": "rpgmod:add_weapon",
            "conditions": [{"condition": "forge:loot_table_id", "loot_table": table} for table in tables],
            "min_level": min_level,
            "max_level": max_level,
            "hone_chance": hone,
        })

    modifier("weapon_stronghold", [
        "minecraft:chests/stronghold_library", "minecraft:chests/stronghold_corridor",
        "minecraft:chests/stronghold_crossing", "minecraft:chests/stronghold_catacombs",
    ], 4, 9, 0.35)

    modifier("weapon_ruined_portal", ["minecraft:chests/ruined_portal"], 1, 4, 0.1)

    modifier("weapon_nether", [
        "minecraft:chests/nether_bridge", "minecraft:chests/bastion_treasure",
        "minecraft:chests/bastion_other", "minecraft:chests/bastion_bridge",
        "minecraft:chests/bastion_hoglin_stable", "minecraft:chests/ruined_portal_nether",
    ], 5, 11, 0.4)

    modifier("weapon_end", [
        "minecraft:chests/end_city_treasure", "minecraft:chests/ancient_city",
        "minecraft:chests/ancient_city_ice_box",
    ], 8, 15, 0.6)

    modifier("weapon_bonus_chest", ["minecraft:chests/spawn_bonus_chest"], 1, 2, 0.0)

    write_json(FORGE / "loot_modifiers/global_loot_modifiers.json", {
        "replace": False,
        "entries": [
            "rpgmod:weapon_stronghold",
            "rpgmod:weapon_ruined_portal",
            "rpgmod:weapon_nether",
            "rpgmod:weapon_end",
            "rpgmod:weapon_bonus_chest",
        ],
    })
    print("  loot_modifiers: 5 модификаторов + список forge/loot_modifiers/global_loot_modifiers.json")


# ------------------------------------------------------------ 4. структуры/данжи

def gen_structures() -> None:
    # в каких биомах разрешены наши структуры
    write_json(RP / "tags/worldgen/biome/has_structure/dungeon.json", {"replace": False, "values": [
        "#minecraft:is_forest", "#minecraft:is_taiga", "#minecraft:is_jungle",
        "#minecraft:is_savanna", "#minecraft:is_badlands", "#minecraft:is_hill",
        "#minecraft:is_mountain", "#minecraft:is_beach",
        "minecraft:plains", "minecraft:sunflower_plains", "minecraft:desert",
        "minecraft:snowy_plains", "minecraft:ice_spikes", "minecraft:swamp",
        "minecraft:mangrove_swamp", "minecraft:meadow", "minecraft:cherry_grove",
        "minecraft:stony_shore", "minecraft:mushroom_fields",
        "minecraft:snowy_taiga", "minecraft:old_growth_pine_taiga", "minecraft:old_growth_spruce_taiga",
        "minecraft:grove", "minecraft:snowy_slopes", "minecraft:frozen_peaks",
        "minecraft:jagged_peaks", "minecraft:stony_peaks", "minecraft:windswept_hills",
    ]})
    # замки и башни — только на суше и подальше от океана
    write_json(RP / "tags/worldgen/biome/has_structure/castle.json", {"replace": False, "values": [
        "#minecraft:is_forest", "#minecraft:is_taiga", "#minecraft:is_hill", "#minecraft:is_mountain",
        "minecraft:plains", "minecraft:sunflower_plains", "minecraft:meadow",
        "minecraft:windswept_hills", "minecraft:snowy_plains", "minecraft:cherry_grove",
    ]})
    write_json(RP / "tags/worldgen/biome/has_structure/tower.json", {"replace": False, "values": [
        "#minecraft:is_forest", "#minecraft:is_taiga", "#minecraft:is_savanna", "#minecraft:is_hill",
        "minecraft:plains", "minecraft:sunflower_plains", "minecraft:desert",
        "minecraft:snowy_plains", "minecraft:swamp", "minecraft:meadow",
    ]})

    monster_spawns = {
        "monster": {
            "bounding_box": "piece",
            "spawns": [
                {"type": "rpgmod:cave_stalker", "weight": 10, "minCount": 1, "maxCount": 3},
                {"type": "minecraft:zombie", "weight": 6, "minCount": 1, "maxCount": 2},
                {"type": "minecraft:skeleton", "weight": 4, "minCount": 1, "maxCount": 2},
            ],
        },
    }

    def structure(name: str, pool_name: str, biomes: str, step: str, max_distance: int,
                  project: bool, spawn_overrides: dict | None) -> None:
        data = {
            "type": "minecraft:jigsaw",
            "biomes": biomes,
            "max_distance_from_center": max_distance,
            "size": 1,  # один готовый .nbt = целое подземелье (легко редактировать)
            "spawn_overrides": spawn_overrides or {"monster": {"bounding_box": "piece", "spawns": []}},
            "start_height": {"absolute": 0},
            "start_pool": pool_name,
            "step": step,
            "terrain_adaptation": "beard_thin",
            "use_expansion_hack": False,
        }
        if project:
            data["project_start_to_heightmap"] = "WORLD_SURFACE_WG"
        write_json(RP / f"worldgen/structure/{name}.json", data)

    structure("dungeon_labyrinth", "rpgmod:dungeon/labyrinth", "#rpgmod:has_structure/dungeon",
              "underground_structures", 96, True, monster_spawns)
    structure("surface_castle", "rpgmod:surface/castle", "#rpgmod:has_structure/castle",
              "surface_structures", 96, True, monster_spawns)
    structure("surface_tower", "rpgmod:surface/tower", "#rpgmod:has_structure/tower",
              "surface_structures", 64, True, monster_spawns)

    # пулы шаблонов: по одному элементу каждый
    def template_pool(pool_name: str, template: str) -> None:
        write_json(RP / f"worldgen/template_pool/{pool_name}.json", {
            "fallback": "minecraft:empty",
            "elements": [{
                "weight": 1,
                "element": {
                    "element_type": "minecraft:legacy_single_pool_element",
                    "location": f"rpgmod:{template}",
                    "processors": {"processors": []},
                    "projection": "rigid",
                },
            }],
        })

    template_pool("dungeon/labyrinth", "dungeon/labyrinth")
    template_pool("surface/castle", "surface/castle")
    template_pool("surface/tower", "surface/tower")

    # наборы структур: как часто искать место (spacing/separation в чанках)
    write_json(RP / "worldgen/structure_set/dungeons.json", {
        "placement": {"type": "minecraft:random_spread", "spacing": 40, "separation": 18, "salt": 710001},
        "structures": [{"structure": "rpgmod:dungeon_labyrinth", "weight": 1}],
    })
    write_json(RP / "worldgen/structure_set/surface_dungeons.json", {
        "placement": {"type": "minecraft:random_spread", "spacing": 56, "separation": 24, "salt": 710002},
        "structures": [
            {"structure": "rpgmod:surface_castle", "weight": 1},
            {"structure": "rpgmod:surface_tower", "weight": 2},
        ],
    })
    print("  structures: dungeon_labyrinth, surface_castle, surface_tower + пулы + наборы + теги биомов")


# ----------------------------------------------------------------- 5. блоки/теги

def gen_block_tags() -> None:
    pickaxe = [f"rpgmod:{name}" for name in (
        "rune_bricks", "cracked_rune_bricks", "gilded_rune_bricks",
        "frost_bricks", "ember_bricks", "soul_lamp", "boss_altar", "village_hearth")]
    write_json(MC / "tags/blocks/mineable/pickaxe.json", {"replace": False, "values": pickaxe})
    write_json(MC / "tags/blocks/needs_stone_tool.json", {"replace": False, "values": [
        "rpgmod:rune_bricks", "rpgmod:cracked_rune_bricks", "rpgmod:gilded_rune_bricks",
    ]})
    # наши блоки не ломаются поршнями/драконом
    write_json(MC / "tags/blocks/dragon_immune.json", {"replace": False, "values": ["rpgmod:boss_altar"]})
    write_json(MC / "tags/blocks/wither_immune.json", {"replace": False, "values": ["rpgmod:boss_altar"]})
    print("  tags: mineable/pickaxe, needs_stone_tool, dragon_immune, wither_immune")


# ---------------------------------------------------------------------- запуск

def validate_json() -> int:
    """Проверяем, что все JSON'ы в resources валидны (дешёвая страховка от опечаток)."""
    errors = 0
    for path in sorted(RES.rglob("*.json")):
        try:
            json.loads(path.read_text(encoding="utf-8"))
        except json.JSONDecodeError as error:
            errors += 1
            print(f"  ОШИБКА JSON: {path.relative_to(ROOT)}: {error}")
    return errors


def main() -> None:
    print("Генерируем data-JSON'ы мода:")
    gen_damage_types()
    gen_loot_tables()
    gen_loot_modifiers()
    gen_structures()
    gen_block_tags()
    errors = validate_json()
    total = len(list(RES.rglob("*.json")))
    print(f"Всего JSON-файлов в resources: {total}, ошибок: {errors}")


if __name__ == "__main__":
    main()
