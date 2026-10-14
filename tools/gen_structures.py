#!/usr/bin/env python3
"""Генератор NBT-шаблонов (структур) для RPG Overhaul.

Запуск:  python3 tools/gen_structures.py

Пишет в src/main/resources/data/rpgmod/structures/:
  dungeon/labyrinth.nbt   — подземный лабиринт с наземным входом, сокровищницей и залом босса
  surface/castle.nbt      — замок (большое надземное подземелье с алтарём босса)
  surface/tower.nbt       — башня с сундуками
  village/house_small.nbt, house_big.nbt, farm.nbt
  village/workshop_warrior.nbt, workshop_ranger.nbt, workshop_mage.nbt, workshop_druid.nbt

Формат NBT — как у ванильных структур (проверено на igloo/top.nbt из 1.20.1):
  корень { DataVersion, size:[I;x,y,z], palette:[{Name,Properties?}], blocks:[{pos,state,nbt?}], entities:[] }

Все шаблоны можно открыть в игре структурным блоком (Structure Block -> Load) и отредактировать,
а затем положить обратно — игра подхватит изменения автоматически.
"""
from __future__ import annotations

import gzip
import struct
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
STRUCTURES = ROOT / "src/main/resources/data/rpgmod/structures"

DATA_VERSION = 3465  # Minecraft 1.20.1

# ---------------------------------------------------------------- запись NBT

TAG_BYTE, TAG_SHORT, TAG_INT, TAG_LONG, TAG_FLOAT, TAG_DOUBLE = 1, 2, 3, 4, 5, 6
TAG_BYTE_ARRAY, TAG_STRING, TAG_LIST, TAG_COMPOUND, TAG_INT_ARRAY = 7, 8, 9, 10, 11
TAG_LONG_ARRAY = 12

TAG_NAMES = {
    TAG_BYTE: "byte", TAG_SHORT: "short", TAG_INT: "int", TAG_LONG: "long",
    TAG_FLOAT: "float", TAG_DOUBLE: "double", TAG_BYTE_ARRAY: "byte[]",
    TAG_STRING: "string", TAG_LIST: "list", TAG_COMPOUND: "compound",
    TAG_INT_ARRAY: "int[]", TAG_LONG_ARRAY: "long[]",
}


def _payload(tag: int, value) -> bytes:
    if tag == TAG_BYTE:
        return struct.pack(">b", value)
    if tag == TAG_SHORT:
        return struct.pack(">h", value)
    if tag == TAG_INT:
        return struct.pack(">i", value)
    if tag == TAG_LONG:
        return struct.pack(">q", value)
    if tag == TAG_FLOAT:
        return struct.pack(">f", value)
    if tag == TAG_DOUBLE:
        return struct.pack(">d", value)
    if tag == TAG_STRING:
        raw = value.encode("utf-8")
        return struct.pack(">H", len(raw)) + raw
    if tag == TAG_INT_ARRAY:
        return struct.pack(">i", len(value)) + b"".join(struct.pack(">i", v) for v in value)
    if tag == TAG_BYTE_ARRAY:
        return struct.pack(">i", len(value)) + bytes(value)
    if tag == TAG_LONG_ARRAY:
        return struct.pack(">i", len(value)) + b"".join(struct.pack(">q", v) for v in value)
    if tag == TAG_LIST:
        # value = (tagId, [items])
        item_tag, items = value
        out = struct.pack(">bi", item_tag, len(items))
        for item in items:
            out += _payload(item_tag, item)
        return out
    if tag == TAG_COMPOUND:
        out = b""
        for name, (item_tag, item_value) in value.items():
            out += struct.pack(">b", item_tag)
            raw = name.encode("utf-8")
            out += struct.pack(">H", len(raw)) + raw
            out += _payload(item_tag, item_value)
        return out + b"\x00"
    raise ValueError(f"неизвестный тег {tag}")


def _named(name: str, tag: int, value) -> bytes:
    raw = name.encode("utf-8")
    return struct.pack(">bH", tag, len(raw)) + raw + _payload(tag, value)


def compound(**kwargs) -> dict:
    """Читаемый способ собирать compound'ы: compound(a=(TAG_INT, 5), b=(TAG_STRING, 'x'))."""
    return kwargs


def i(value: int) -> tuple:
    return TAG_INT, value


def s(value: str) -> tuple:
    return TAG_STRING, value


def int_array(values: list[int]) -> tuple:
    return TAG_INT_ARRAY, values


# ------------------------------------------------------------ конструктор структур

AIR = "minecraft:air"


class Template:
    """Строит структуру и сохраняет её в .nbt."""

    def __init__(self, size_x: int, size_y: int, size_z: int):
        self.size = (size_x, size_y, size_z)
        self.blocks: dict[tuple[int, int, int], tuple[str, dict, dict | None]] = {}

    # -- примитивы ---------------------------------------------------------
    def set(self, x: int, y: int, z: int, block: str, props: dict | None = None, nbt: dict | None = None) -> None:
        sx, sy, sz = self.size
        if not (0 <= x < sx and 0 <= y < sy and 0 <= z < sz):
            raise IndexError(f"блок вне границ шаблона: {x},{y},{z} (размер {self.size})")
        self.blocks[(x, y, z)] = (block, props or {}, nbt)

    def fill(self, x0: int, y0: int, z0: int, x1: int, y1: int, z1: int, block: str,
             props: dict | None = None) -> None:
        for x in range(min(x0, x1), max(x0, x1) + 1):
            for y in range(min(y0, y1), max(y0, y1) + 1):
                for z in range(min(z0, z1), max(z0, z1) + 1):
                    self.set(x, y, z, block, props)

    def walls(self, x0: int, y0: int, z0: int, x1: int, y1: int, z1: int, block: str,
              props: dict | None = None) -> None:
        for x in range(x0, x1 + 1):
            for y in range(y0, y1 + 1):
                for z in range(z0, z1 + 1):
                    if x in (x0, x1) or z in (z0, z1):
                        self.set(x, y, z, block, props)

    def hollow(self, x0: int, y0: int, z0: int, x1: int, y1: int, z1: int, block: str,
               props: dict | None = None) -> None:
        """Пустая коробка (стены + пол + потолок)."""
        self.walls(x0, y0, z0, x1, y1, z1, block, props)
        for x in range(x0, x1 + 1):
            for z in range(z0, z1 + 1):
                self.set(x, y0, z, block, props)
                self.set(x, y1, z, block, props)

    def clear_interior(self, x0: int, y0: int, z0: int, x1: int, y1: int, z1: int) -> None:
        self.fill(x0, y0, z0, x1, y1, z1, AIR)

    def ladder_shaft(self, x: int, z: int, y_bottom: int, y_top: int, facing: str = "south") -> None:
        """Шахта 1x1 с лестницами: подниматься/спускаться можно не копая."""
        for y in range(y_bottom, y_top + 1):
            self.set(x, y, z, AIR)
            self.set(x, y, z, "minecraft:ladder", {"facing": facing})

    def chest(self, x: int, y: int, z: int, loot_table: str) -> None:
        self.set(x, y, z, "minecraft:chest", None, {"id": "minecraft:chest", "LootTable": loot_table})

    def bed(self, x: int, y: int, z: int) -> None:
        self.set(x, y, z, "minecraft:red_bed", {"part": "foot", "facing": "south", "occupied": "false"})
        self.set(x, y, z + 1, "minecraft:red_bed", {"part": "head", "facing": "south", "occupied": "false"})

    # -- сохранение --------------------------------------------------------
    def save(self, relative_path: str) -> None:
        palette: list[tuple[str, dict]] = []
        palette_index: dict[tuple[str, dict], int] = {}

        def index_of(block: str, props: dict) -> int:
            key = (block, tuple(sorted(props.items())))
            if key not in palette_index:
                palette_index[key] = len(palette)
                palette.append((block, props))
            return palette_index[key]

        block_entries = []
        for (x, y, z), (block, props, nbt) in sorted(self.blocks.items()):
            entry = compound(pos=int_array([x, y, z]), state=i(index_of(block, props)))
            if nbt:
                entry["nbt"] = (TAG_COMPOUND, compound(**{k: (TAG_STRING, v) for k, v in nbt.items()}))
            block_entries.append(entry)

        palette_entries = []
        for block, props in palette:
            entry = compound(Name=s(block))
            if props:
                entry["Properties"] = (TAG_COMPOUND, compound(**{k: (TAG_STRING, v) for k, v in props.items()}))
            palette_entries.append(entry)

        root = {
            "DataVersion": i(DATA_VERSION),
            "size": int_array(list(self.size)),
            "palette": (TAG_LIST, (TAG_COMPOUND, palette_entries)),
            "blocks": (TAG_LIST, (TAG_COMPOUND, block_entries)),
            "entities": (TAG_LIST, (TAG_COMPOUND, [])),
        }
        payload = _named("", TAG_COMPOUND, root)
        path = STRUCTURES / relative_path
        path.parent.mkdir(parents=True, exist_ok=True)
        with gzip.open(path, "wb") as handle:
            handle.write(payload)
        print(f"  {relative_path}: {self.size[0]}x{self.size[1]}x{self.size[2]}, "
              f"блоков {len(self.blocks)}, палитра {len(palette)}, {path.stat().st_size // 1024} КиБ")


# ------------------------------------------------------------------- генераторы

RUNE = "rpgmod:rune_bricks"
RUNE_CRACKED = "rpgmod:cracked_rune_bricks"
RUNE_GILDED = "rpgmod:gilded_rune_bricks"
SOUL_LAMP = "rpgmod:soul_lamp"
BOSS_ALTAR = "rpgmod:boss_altar"
HEARTH = "rpgmod:village_hearth"
STONE_BRICK = "minecraft:stone_bricks"
COBBLE = "minecraft:cobblestone"
OAK_PLANKS = "minecraft:oak_planks"
OAK_LOG = "minecraft:oak_log"
GLASS = "minecraft:glass"
TORCH = "minecraft:torch"
DIRT_PATH = "minecraft:dirt_path"

LOOT_DUNGEON_COMMON = "rpgmod:chests/dungeon_common"
LOOT_DUNGEON_TREASURE = "rpgmod:chests/dungeon_treasure"
LOOT_DUNGEON_BOSS = "rpgmod:chests/dungeon_boss"
LOOT_CASTLE = "rpgmod:chests/castle"
LOOT_TOWER = "rpgmod:chests/tower"
LOOT_VILLAGE = "rpgmod:chests/village_house"


def maze_grid(width: int, height: int, seed: int) -> list[list[bool]]:
    """Лабиринт «recursive backtracker». True = стена."""
    import random

    rnd = random.Random(seed)
    grid = [[True] * width for _ in range(height)]
    cells_w, cells_h = (width - 1) // 2, (height - 1) // 2
    start = (rnd.randrange(cells_w), rnd.randrange(cells_h))
    grid[start[0] * 2 + 1][start[1] * 2 + 1] = False
    stack = [start]
    while stack:
        cx, cy = stack[-1]
        neighbours = []
        for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            nx, ny = cx + dx, cy + dy
            if 0 <= nx < cells_w and 0 <= ny < cells_h and grid[nx * 2 + 1][ny * 2 + 1]:
                neighbours.append((nx, ny, dx, dy))
        if not neighbours:
            stack.pop()
            continue
        nx, ny, dx, dy = rnd.choice(neighbours)
        grid[cx * 2 + 1 + dx][cy * 2 + 1 + dy] = False
        grid[nx * 2 + 1][ny * 2 + 1] = False
        stack.append((nx, ny))
    return grid


def build_labyrinth() -> None:
    """Подземный лабиринт: вход-руины на поверхности, 2 уровня лабиринта, сокровищница, зал босса.

    Верхний слой шаблона (y = size-1) при генерации совмещается с поверхностью
    (project_start_to_heightmap = WORLD_SURFACE_WG), поэтому вход всегда на виду.
    """
    sx, sy, sz = 33, 40, 33
    t = Template(sx, sy, sz)
    top = sy - 1  # y = 39 — уровень поверхности

    # --- 1. Вход: площадка-руины на поверхности ------------------------------------
    t.fill(11, top - 1, 11, 21, top - 1, 21, RUNE_CRACKED)
    t.fill(12, top - 1, 12, 20, top - 1, 20, RUNE)
    # зубцы по периметру
    for x, z in [(11, 11), (21, 11), (11, 21), (21, 21), (16, 11), (11, 16), (21, 16), (16, 21)]:
        t.set(x, top, z, RUNE_GILDED)
    t.set(16, top, 11, RUNE)
    t.set(16, top, 21, RUNE)
    # проём вниз (дыра 3x3) — «каждый данж достижим без копания»
    t.fill(15, top - 1, 15, 17, top - 1, 17, AIR)
    t.fill(15, top - 4, 15, 17, top - 2, 17, AIR)

    # --- 2. Лестница вниз (зигзаг из блоков-ступеней) --------------------------------
    x, y, z = 16, top - 5, 16
    for step in range(10):
        t.fill(x - 1, y, z, x + 1, y, z, RUNE)          # ступень
        t.fill(x - 1, y, z - 1, x + 1, y, z - 1, AIR)   # просвет перед ступенью
        y -= 1
        z -= 1
    landing_y = y + 1

    # --- 3. Два уровня лабиринта -----------------------------------------------------
    for level, floor_y in enumerate((landing_y - 3, landing_y - 12)):
        grid = maze_grid(25, 25, seed=1337 + level)
        for gx in range(25):
            for gz in range(25):
                bx, bz = 4 + gx, 4 + gz
                t.fill(bx, floor_y, bz, bx, floor_y, bz, RUNE)
                t.fill(bx, floor_y + 4, bz, bx, floor_y + 4, bz, RUNE)  # потолок
                if grid[gx][gz]:
                    t.fill(bx, floor_y + 1, bz, bx, floor_y + 3, bz, RUNE_CRACKED if (gx + gz) % 7 == 0 else RUNE)
                else:
                    t.fill(bx, floor_y + 1, bz, bx, floor_y + 3, bz, AIR)
        # свет по углам
        for bx, bz in [(6, 6), (27, 6), (6, 27), (27, 27), (16, 16)]:
            t.set(bx, floor_y + 3, bz, SOUL_LAMP)
        # проход вниз/вверх (шахта с лестницей)
        shaft_x, shaft_z = 16, 16
        t.ladder_shaft(shaft_x, shaft_z, floor_y - 8, floor_y + 3)
        if level == 0:
            t.fill(16, floor_y - 1, 16, 16, floor_y - 1, 16, RUNE)  # ступень-порожек

    # --- 4. Сокровищница -------------------------------------------------------------
    treasure_y = landing_y - 21
    t.hollow(4, treasure_y, 4, 14, treasure_y + 5, 14, RUNE)
    t.set(9, treasure_y + 2, 9, SOUL_LAMP)
    t.chest(6, treasure_y + 1, 6, LOOT_DUNGEON_TREASURE)
    t.chest(12, treasure_y + 1, 6, LOOT_DUNGEON_TREASURE)
    t.chest(6, treasure_y + 1, 12, LOOT_DUNGEON_TREASURE)
    t.ladder_shaft(16, 16, treasure_y, landing_y - 13)

    # --- 5. Зал босса ----------------------------------------------------------------
    boss_y = landing_y - 21
    t.hollow(16, boss_y - 4, 10, 30, boss_y + 5, 24, RUNE_CRACKED)
    t.fill(17, boss_y - 4, 11, 29, boss_y - 4, 23, RUNE)      # пол зала
    t.fill(17, boss_y - 3, 11, 29, boss_y + 4, 23, AIR)       # объём зала
    for px, pz in [(18, 12), (28, 12), (18, 22), (28, 22)]:
        t.fill(px, boss_y - 3, pz, px, boss_y + 3, pz, RUNE_GILDED)   # колонны
        t.set(px, boss_y + 4, pz, SOUL_LAMP)
    t.set(23, boss_y - 3, 17, BOSS_ALTAR)                      # алтарь: ключ -> Хранитель
    t.chest(23, boss_y - 3, 21, LOOT_DUNGEON_BOSS)
    t.chest(23, boss_y - 3, 13, LOOT_DUNGEON_COMMON)
    # подъём из зала в лабиринт
    t.ladder_shaft(17, 12, boss_y - 3, boss_y + 4)

    t.save("dungeon/labyrinth.nbt")


def build_castle() -> None:
    """Надземный замок: стены, башни, внутренний двор, донжон с алтарём босса."""
    sx, sy, sz = 35, 24, 35
    t = Template(sx, sy, sz)
    ground = 1  # 1 — низ шаблона, верх (23) совместится с поверхностью

    # основание
    t.fill(1, ground, 1, 33, ground, 33, STONE_BRICK)
    # внешние стены высотой 6, толщиной 1
    t.walls(1, ground + 1, 1, 33, ground + 6, 33, STONE_BRICK)
    t.fill(2, ground + 7, 2, 32, ground + 7, 32, AIR)
    # зубцы
    for x in range(1, 34, 2):
        for z in (1, 33):
            t.set(x, ground + 7, z, STONE_BRICK)
    for z in range(1, 34, 2):
        for x in (1, 33):
            t.set(x, ground + 7, z, STONE_BRICK)
    # ворота (северная стена) + дорожка к краю шаблона
    t.fill(16, ground + 1, 1, 18, ground + 5, 1, AIR)
    t.fill(16, ground, 0, 18, ground, 0, DIRT_PATH)
    t.fill(15, ground, 1, 19, ground, 3, DIRT_PATH)
    t.fill(16, ground + 6, 1, 18, ground + 6, 1, RUNE_GILDED)
    # угловые башни
    for tx, tz in [(1, 1), (28, 1), (1, 28), (28, 28)]:
        t.hollow(tx, ground + 1, tz, tx + 5, ground + 11, tz + 5, RUNE)
        t.fill(tx + 1, ground + 1, tz + 1, tx + 4, ground + 10, tz + 4, AIR)
        t.set(tx + 2, ground + 2, tz + 2, SOUL_LAMP)
        t.set(tx + 3, ground + 6, tz + 3, SOUL_LAMP)
        t.ladder_shaft(tx + 2, tz + 2, ground + 1, ground + 11)
        t.fill(tx - 1, ground + 12, tz - 1, tx + 6, ground + 12, tz + 6, RUNE)
        t.fill(tx, ground + 13, tz, tx + 5, ground + 13, tz + 5, AIR)
        for x in range(tx, tx + 6):
            for z in (tz, tz + 5):
                if x % 2 == 0:
                    t.set(x, ground + 13, z, RUNE_GILDED)
        for z in range(tz, tz + 6):
            for x in (tx, tx + 5):
                if z % 2 == 0:
                    t.set(x, ground + 13, z, RUNE_GILDED)

    # донжон в центре
    t.hollow(11, ground + 1, 11, 23, ground + 9, 23, RUNE_CRACKED)
    t.fill(12, ground + 1, 12, 22, ground + 8, 22, AIR)
    t.fill(11, ground + 1, 11, 23, ground + 1, 23, RUNE)        # пол
    t.fill(16, ground + 2, 11, 18, ground + 5, 11, AIR)         # вход
    t.set(17, ground + 6, 11, RUNE_GILDED)
    # окна
    for y in (ground + 4, ground + 7):
        for x in (14, 20):
            t.set(x, y, 11, GLASS)
            t.set(x, y, 23, GLASS)
        for z in (14, 20):
            t.set(11, y, z, GLASS)
            t.set(23, y, z, GLASS)
    # второй этаж
    t.fill(12, ground + 5, 12, 22, ground + 5, 22, RUNE)
    t.fill(16, ground + 6, 16, 18, ground + 8, 18, AIR)
    t.ladder_shaft(17, 14, ground + 1, ground + 8)
    # тронный зал: алтарь босса + сундуки
    t.set(17, ground + 6, 21, BOSS_ALTAR)
    t.chest(13, ground + 2, 12, LOOT_CASTLE)
    t.chest(21, ground + 2, 12, LOOT_CASTLE)
    t.chest(13, ground + 6, 21, LOOT_DUNGEON_BOSS)
    t.chest(21, ground + 6, 21, LOOT_DUNGEON_TREASURE)
    t.set(15, ground + 6, 15, SOUL_LAMP)
    t.set(19, ground + 6, 15, SOUL_LAMP)

    # внутренний двор: дорожки и факелы
    t.fill(4, ground, 15, 10, ground, 19, DIRT_PATH)
    t.fill(24, ground, 15, 30, ground, 19, DIRT_PATH)
    t.fill(15, ground, 4, 19, ground, 10, DIRT_PATH)
    for x, z in [(6, 6), (28, 6), (6, 28), (28, 28), (6, 19), (28, 19), (19, 6), (19, 28)]:
        t.set(x, ground + 1, z, OAK_LOG)
        t.set(x, ground + 2, z, TORCH)

    t.save("surface/castle.nbt")


def build_tower() -> None:
    """Башня: 5 этажей, лестничная шахта, сундуки наверху."""
    sx, sy, sz = 11, 26, 11
    t = Template(sx, sy, sz)
    ground = 1

    t.fill(2, ground, 2, 8, ground, 8, COBBLE)
    t.walls(2, ground + 1, 2, 8, ground + 20, 8, RUNE)
    t.fill(3, ground + 1, 3, 7, ground + 20, 7, AIR)

    # этажи каждые 4 блока + проёмы
    for floor in range(1, 5):
        y = ground + floor * 4
        t.fill(3, y, 3, 7, y, 7, OAK_PLANKS)
        t.clear_interior(4, y + 1, 4, 6, y + 3, 6)

    # вход
    t.fill(5, ground + 1, 2, 6, ground + 3, 2, AIR)
    t.fill(5, ground, 2, 6, ground, 2, DIRT_PATH)
    t.fill(5, ground, 0, 6, ground, 1, DIRT_PATH)

    # лестничная шахта в центре
    t.ladder_shaft(5, 5, ground + 1, ground + 20)

    # окна и свет
    for y in (ground + 3, ground + 7, ground + 11, ground + 15, ground + 19):
        for x in (3, 7):
            t.set(x, y, 2, GLASS)
            t.set(x, y, 8, GLASS)
        for z in (3, 7):
            t.set(2, y, z, GLASS)
            t.set(8, y, z, GLASS)
    t.set(5, ground + 3, 3, SOUL_LAMP)
    t.set(5, ground + 11, 3, SOUL_LAMP)

    # сундуки
    t.chest(3, ground + 1, 7, LOOT_TOWER)
    t.chest(7, ground + 1, 3, LOOT_VILLAGE)
    t.chest(3, ground + 5, 3, LOOT_TOWER)
    t.chest(7, ground + 9, 7, LOOT_DUNGEON_COMMON)
    t.chest(3, ground + 13, 7, LOOT_DUNGEON_TREASURE)
    t.chest(7, ground + 17, 3, LOOT_TOWER)
    # алтарь босса на самом верху — башня считается «большим данжем»
    t.set(5, ground + 21, 5, BOSS_ALTAR)
    t.fill(4, ground + 21, 4, 6, ground + 21, 6, RUNE_GILDED)

    t.save("surface/tower.nbt")


# ------------------------------------------------------------------ деревня

def village_shell(t: Template, x0: int, y0: int, z0: int, x1: int, y1: int, z1: int,
                  floor_block: str = OAK_PLANKS) -> None:
    """Стены из брёвен, пол, крыша из досок, потолок-просвет."""
    t.walls(x0, y0, z0, x1, y1, z1, OAK_LOG)
    t.fill(x0 + 1, y0, z0 + 1, x1 - 1, y0, z1 - 1, floor_block)
    t.fill(x0 + 1, y0 + 1, z0 + 1, x1 - 1, y1 - 1, z1 - 1, AIR)
    t.fill(x0, y1, z0, x1, y1, z1, OAK_PLANKS)  # крыша


def build_house_small() -> None:
    t = Template(11, 8, 11)
    village_shell(t, 1, 1, 1, 9, 5, 9)
    # дверной проём
    t.fill(4, 1, 1, 6, 3, 1, AIR)
    t.fill(4, 1, 0, 6, 1, 0, DIRT_PATH)
    # мебель
    t.bed(2, 2, 2)
    t.set(8, 2, 8, "minecraft:crafting_table")
    t.set(8, 2, 7, "minecraft:furnace")
    t.chest(2, 2, 4, LOOT_VILLAGE)
    t.set(5, 2, 5, HEARTH)
    t.set(3, 4, 3, TORCH)
    t.set(7, 4, 7, TORCH)
    t.save("village/house_small.nbt")


def build_house_big() -> None:
    t = Template(15, 9, 13)
    village_shell(t, 1, 1, 1, 13, 6, 11)
    t.fill(6, 1, 1, 8, 3, 1, AIR)
    t.fill(6, 1, 0, 8, 1, 0, DIRT_PATH)
    t.bed(2, 2, 2)
    t.bed(2, 2, 6)
    t.bed(2, 2, 9)
    t.set(12, 2, 2, "minecraft:crafting_table")
    t.set(12, 2, 3, "minecraft:furnace")
    t.set(12, 2, 4, "minecraft:barrel")
    t.chest(12, 2, 9, LOOT_VILLAGE)
    t.chest(12, 2, 10, LOOT_VILLAGE)
    t.set(7, 2, 5, HEARTH)
    t.set(3, 5, 3, TORCH)
    t.set(11, 5, 3, TORCH)
    t.set(3, 5, 9, TORCH)
    t.set(11, 5, 9, TORCH)
    t.save("village/house_big.nbt")


def build_farm() -> None:
    t = Template(15, 7, 15)
    # грядки
    for x in range(1, 14):
        for z in range(1, 14):
            if x in (7,) or z in (7,):
                t.set(x, 1, z, "minecraft:water")
                t.set(x, 0, z, "minecraft:dirt")
            else:
                t.set(x, 0, z, "minecraft:dirt")
                t.set(x, 1, z, "minecraft:farmland")
                if (x + z) % 3 != 0:
                    t.set(x, 2, z, "minecraft:wheat", {"age": "7"})
    # забор по периметру
    for x in range(0, 15):
        for z in (0, 14):
            t.set(x, 1, z, "minecraft:oak_fence")
    for z in range(1, 14):
        for x in (0, 14):
            t.set(x, 1, z, "minecraft:oak_fence")
    # ворота
    t.fill(7, 1, 0, 7, 1, 0, AIR)
    t.fill(7, 0, 0, 7, 0, 0, DIRT_PATH)
    # сарай
    village_shell(t, 3, 3, 3, 11, 6, 9)
    t.fill(6, 3, 9, 8, 5, 9, AIR)
    t.set(4, 4, 4, "minecraft:composter")
    t.set(10, 4, 4, "minecraft:barrel")
    t.chest(4, 4, 8, LOOT_VILLAGE)
    t.set(7, 4, 4, HEARTH)
    t.set(5, 5, 6, TORCH)
    t.set(9, 5, 6, TORCH)
    t.save("village/farm.nbt")


def build_workshop(kind: str, job_blocks: list[str], loot_table: str) -> None:
    """Мастерская: здесь стоят рабочие блоки, определяющие профессии жителей деревни."""
    t = Template(15, 9, 13)
    village_shell(t, 1, 1, 1, 13, 6, 11)
    t.fill(6, 1, 1, 8, 3, 1, AIR)
    t.fill(6, 1, 0, 8, 1, 0, DIRT_PATH)

    # рабочие блоки «по кругу»
    spots = [(2, 2), (3, 2), (2, 3), (11, 2), (12, 2), (11, 3), (2, 9), (3, 9), (11, 9), (12, 9)]
    for index, block in enumerate(job_blocks):
        x, z = spots[index % len(spots)]
        t.set(x, 2, z, block)

    # спальные места (жители размножаются, только если есть лишние кровати)
    t.bed(4, 2, 8)
    t.bed(7, 2, 8)
    t.bed(10, 2, 8)

    t.chest(2, 2, 5, loot_table)
    t.chest(12, 2, 5, LOOT_VILLAGE)
    t.set(6, 2, 6, HEARTH)          # очаг — «мэрия» деревни, регистрирует её на сервере
    t.set(3, 5, 3, TORCH)
    t.set(11, 5, 3, TORCH)
    t.set(3, 5, 9, TORCH)
    t.set(11, 5, 9, TORCH)
    t.save(f"village/workshop_{kind}.nbt")


def main() -> None:
    print("Генерируем структуры (data/rpgmod/structures):")
    build_labyrinth()
    build_castle()
    build_tower()
    build_house_small()
    build_house_big()
    build_farm()
    build_workshop("warrior",
                   ["minecraft:blast_furnace", "minecraft:smithing_table", "minecraft:grindstone",
                    "minecraft:anvil", "minecraft:stonecutter"],
                   "rpgmod:chests/village_workshop_warrior")
    build_workshop("ranger",
                   ["minecraft:fletching_table", "minecraft:cartography_table", "minecraft:target",
                    "minecraft:loom", "minecraft:barrel"],
                   "rpgmod:chests/village_workshop_ranger")
    build_workshop("mage",
                   ["minecraft:brewing_stand", "minecraft:lectern", "minecraft:enchanting_table",
                    "minecraft:bookshelf", "minecraft:cauldron"],
                   "rpgmod:chests/village_workshop_mage")
    build_workshop("druid",
                   ["minecraft:composter", "minecraft:barrel", "minecraft:cauldron",
                    "minecraft:beehive", "minecraft:campfire"],
                   "rpgmod:chests/village_workshop_druid")
    print("Готово. Шаблоны открываются структруным блоком в игре.")


if __name__ == "__main__":
    main()
