#!/usr/bin/env python3
"""Генератор ВСЕХ текстур мода RPG Overhaul (заглушки-плейсхолдеры).

Запуск:  python3 tools/gen_textures.py

Пишет в src/main/resources/assets/rpgmod/textures/:
  item/*.png              — 16x16 иконки предметов (оружие, броня, материалы, посохи, яйца)
  block/*.png             — 16x16 текстуры блоков
  entity/*.png            — текстуры мобов (64x64 скин-разметка) и блик снаряда
  models/armor/*.png      — 64x32 слои брони (сетка классов)

ВНИМАНИЕ: это сгенерированные заглушки, чтобы мод выглядел «как есть» без чужого арта.
Замените PNG-файлы своим артом (или откройте их в редакторе) — код менять не нужно,
имена файлов уже совпадают с тем, что ждёт игра.
"""
from __future__ import annotations

import math
from pathlib import Path

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parent.parent
TEX = ROOT / "src/main/resources/assets/rpgmod/textures"

# ------------------------------------------------------------------------ палитры

PALETTES: dict[str, dict[str, tuple[int, int, int]]] = {
    "steel": {"base": (176, 188, 200), "dark": (110, 122, 135), "light": (226, 234, 242), "handle": (86, 66, 48)},
    "silver": {"base": (226, 234, 245), "dark": (150, 162, 178), "light": (255, 255, 255), "handle": (86, 66, 48)},
    "dusksteel": {"base": (96, 82, 140), "dark": (52, 42, 78), "light": (160, 140, 210), "handle": (48, 34, 66)},
    "iron": {"base": (216, 216, 216), "dark": (150, 150, 150), "light": (245, 245, 245), "handle": (86, 66, 48)},
    "gold": {"base": (245, 209, 74), "dark": (180, 140, 30), "light": (255, 240, 160), "handle": (86, 66, 48)},
    "diamond": {"base": (74, 237, 217), "dark": (32, 150, 140), "light": (190, 255, 245), "handle": (86, 66, 48)},
    "netherite": {"base": (74, 65, 69), "dark": (38, 32, 35), "light": (120, 108, 112), "handle": (86, 66, 48)},
    "amethyst": {"base": (180, 124, 255), "dark": (110, 60, 180), "light": (225, 195, 255), "handle": (86, 66, 48)},
    "frost": {"base": (159, 232, 255), "dark": (80, 160, 200), "light": (225, 250, 255), "handle": (86, 66, 48)},
    "ember": {"base": (255, 122, 60), "dark": (170, 60, 20), "light": (255, 200, 120), "handle": (86, 66, 48)},
    "void": {"base": (122, 79, 191), "dark": (60, 34, 100), "light": (185, 150, 240), "handle": (48, 34, 66)},
}
# материалы оружия -> палитра
WEAPON_PALETTE = {"steel": "steel", "silver": "silver", "dusksteel": "dusksteel"}
# материалы заточки/слитков -> палитра
MATERIAL_TEXTURE = {
    "steel_ingot": ("ingot", "steel"),
    "silver_ingot": ("ingot", "silver"),
    "dusk_ingot": ("ingot", "dusksteel"),
    "ember_ingot": ("ingot", "ember"),
    "arcane_dust": ("dust", "amethyst"),
    "frost_shard": ("shard", "frost"),
    "void_shard": ("shard", "void"),
    "mana_crystal": ("crystal", "diamond"),
    "dungeon_key": ("key", "gold"),
}
STAFF_ORB = {"fire_staff": (255, 122, 0), "frost_staff": (127, 227, 255),
             "storm_staff": (169, 133, 255), "nature_staff": (98, 224, 107)}
ARMOR_CLASS_TINT = {
    "warrior": (192, 57, 43),
    "ranger": (39, 174, 96),
    "mage": (142, 68, 173),
    "druid": (22, 160, 133),
}


def rgba(color: tuple[int, int, int], alpha: int = 255) -> tuple[int, int, int, int]:
    return color[0], color[1], color[2], alpha


def shade(color: tuple[int, int, int], factor: float) -> tuple[int, int, int]:
    return (
        max(0, min(255, int(color[0] * factor))),
        max(0, min(255, int(color[1] * factor))),
        max(0, min(255, int(color[2] * factor))),
    )


def new_image(size: int = 16) -> Image.Image:
    return Image.new("RGBA", (size, size), (0, 0, 0, 0))


def put(img: Image.Image, x: int, y: int, color: tuple[int, int, int], alpha: int = 255) -> None:
    if 0 <= x < img.width and 0 <= y < img.height and alpha > 0:
        img.putpixel((x, y), rgba(color, alpha))


def rect(img: Image.Image, x0: int, y0: int, x1: int, y1: int, color: tuple[int, int, int]) -> None:
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            put(img, x, y, color)


def line(img: Image.Image, x0: int, y0: int, x1: int, y1: int, color: tuple[int, int, int]) -> None:
    """Толстая (2 пикселя) линия — «метод Брезенхэма» с утолщением."""
    dx, dy = abs(x1 - x0), abs(y1 - y0)
    sx = 1 if x0 < x1 else -1
    sy = 1 if y0 < y1 else -1
    err = dx - dy
    x, y = x0, y0
    while True:
        put(img, x, y, color)
        put(img, x + 1, y, color)
        if x == x1 and y == y1:
            break
        e2 = 2 * err
        if e2 > -dy:
            err -= dy
            x += sx
        if e2 < dx:
            err += dx
            y += sy


def add_outline(img: Image.Image, color: tuple[int, int, int] = (24, 18, 30)) -> Image.Image:
    """Обводка в 1 пиксель по контуру непрозрачных пикселей (вид «иконка из игры»)."""
    result = img.copy()
    for y in range(img.height):
        for x in range(img.width):
            if img.getpixel((x, y))[3] != 0:
                continue
            neighbours = [(x - 1, y), (x + 1, y), (x, y - 1), (x, y + 1)]
            if any(0 <= nx < img.width and 0 <= ny < img.height and img.getpixel((nx, ny))[3] != 0
                   for nx, ny in neighbours):
                put(result, x, y, color, 255)
    return result


def save(img: Image.Image, path: Path) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    img.save(path)


# --------------------------------------------------------------- иконки материалов

def tex_ingot(pal: dict) -> Image.Image:
    img = new_image()
    rect(img, 4, 6, 11, 10, pal["base"])
    rect(img, 3, 7, 3, 10, pal["dark"])
    rect(img, 12, 6, 12, 9, pal["dark"])
    rect(img, 5, 5, 11, 5, pal["light"])
    rect(img, 4, 11, 11, 11, pal["dark"])
    put(img, 5, 6, pal["light"])
    put(img, 10, 6, pal["light"])
    return add_outline(img)


def tex_shard(pal: dict) -> Image.Image:
    img = new_image()
    line(img, 8, 3, 10, 12, pal["base"])
    put(img, 9, 12, pal["dark"])
    put(img, 8, 12, pal["dark"])
    line(img, 8, 3, 7, 10, pal["light"])
    line(img, 10, 6, 12, 10, pal["dark"])
    put(img, 8, 3, pal["light"])
    return add_outline(img)


def tex_dust(pal: dict) -> Image.Image:
    img = new_image()
    import random
    rnd = random.Random(7)
    for _ in range(26):
        x, y = rnd.randint(4, 11), rnd.randint(6, 11)
        put(img, x, y, pal["base"] if rnd.random() < 0.7 else pal["light"])
    rect(img, 4, 12, 11, 12, pal["dark"])
    return add_outline(img)


def tex_crystal(pal: dict) -> Image.Image:
    img = new_image()
    for y, (x0, x1) in enumerate([(7, 8), (6, 9), (6, 9), (5, 10), (5, 10), (5, 10), (5, 10), (6, 9), (6, 9), (7, 8)]):
        rect(img, x0, 3 + y, x1, 3 + y, pal["base"])
    line(img, 6, 5, 6, 11, pal["light"])
    line(img, 9, 5, 9, 11, pal["dark"])
    return add_outline(img)


def tex_key(pal: dict) -> Image.Image:
    img = new_image()
    # кольцо
    for x, y in [(7, 3), (8, 3), (9, 3), (6, 4), (10, 4), (6, 5), (10, 5), (7, 6), (8, 6), (9, 6)]:
        put(img, x, y, pal["base"])
    put(img, 8, 4, (0, 0, 0), 0)
    # стержень и зубцы
    rect(img, 8, 7, 8, 13, pal["base"])
    rect(img, 9, 11, 10, 11, pal["base"])
    rect(img, 9, 13, 10, 13, pal["dark"])
    put(img, 8, 7, pal["light"])
    return add_outline(img)


# ------------------------------------------------------------------ иконки оружия

def weapon_shape(kind: str) -> tuple[Image.Image, Image.Image]:
    """Возвращает (клинок, рукоять) отдельными слоями, чтобы покрасить их разными цветами."""
    blade = new_image()
    handle = new_image()

    if kind in ("sword", "dagger", "greatsword"):
        if kind == "dagger":
            line(blade, 8, 8, 12, 4, (255, 255, 255))
            line(handle, 7, 9, 5, 11, (255, 255, 255))
            line(handle, 6, 8, 8, 10, (255, 255, 255))  # гарда
            put(handle, 4, 12, (255, 255, 255))
        elif kind == "sword":
            line(blade, 6, 10, 12, 4, (255, 255, 255))
            line(blade, 7, 11, 12, 6, (255, 255, 255))
            line(handle, 5, 11, 3, 13, (255, 255, 255))
            line(handle, 4, 8, 8, 12, (255, 255, 255))  # гарда
            put(handle, 2, 14, (255, 255, 255))
        else:  # greatsword
            line(blade, 4, 11, 12, 3, (255, 255, 255))
            line(blade, 5, 12, 12, 5, (255, 255, 255))
            line(blade, 4, 12, 11, 5, (255, 255, 255))
            line(handle, 4, 12, 2, 14, (255, 255, 255))
            line(handle, 3, 9, 7, 13, (255, 255, 255))
            put(handle, 1, 15, (255, 255, 255))
    elif kind == "axe":
        line(handle, 4, 13, 9, 7, (255, 255, 255))
        line(handle, 5, 14, 10, 8, (255, 255, 255))
        # лезвие топора
        for x, y in [(10, 3), (11, 3), (12, 3), (9, 4), (10, 4), (11, 4), (12, 4), (13, 4),
                     (9, 5), (10, 5), (11, 5), (12, 5), (13, 5), (9, 6), (10, 6), (11, 6), (12, 6)]:
            put(blade, x, y, (255, 255, 255))
    elif kind == "spear":
        line(handle, 3, 14, 10, 6, (255, 255, 255))
        for x, y in [(11, 4), (12, 3), (12, 4), (11, 5), (12, 5), (10, 4), (11, 3)]:
            put(blade, x, y, (255, 255, 255))
        put(handle, 9, 5, (255, 255, 255))
    elif kind == "mace":
        line(handle, 4, 13, 8, 8, (255, 255, 255))
        # шар с шипами
        for y, (x0, x1) in enumerate([(9, 12), (8, 13), (8, 13), (8, 13), (9, 12)]):
            rect(blade, x0, 3 + y, x1, 3 + y, (255, 255, 255))
        for x, y in [(8, 4), (13, 4), (8, 6), (13, 6), (10, 1), (11, 1), (10, 8), (11, 8)]:
            put(blade, x, y, (255, 255, 255))
    return blade, handle


def tex_weapon(kind: str, pal: dict) -> Image.Image:
    blade_mask, handle_mask = weapon_shape(kind)
    blade = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    handle = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y in range(16):
        for x in range(16):
            if blade_mask.getpixel((x, y))[3] != 0:
                # светлый блик сверху-справа, тёмный низ-слева
                color = pal["light"] if (x - y) % 5 == 0 else pal["base"]
                blade.putpixel((x, y), rgba(color))
            if handle_mask.getpixel((x, y))[3] != 0:
                handle.putpixel((x, y), rgba(pal["handle"]))
    combined = Image.alpha_composite(handle, blade)
    return add_outline(combined)


def tex_staff(orb: tuple[int, int, int], wood: tuple[int, int, int] = (110, 76, 46)) -> Image.Image:
    img = new_image()
    line(img, 3, 13, 9, 6, wood)
    line(img, 4, 14, 10, 7, shade(wood, 0.75))
    for y, (x0, x1) in enumerate([(9, 11), (8, 12), (8, 12), (9, 11)]):
        rect(img, x0, 2 + y, x1, 2 + y, orb)
    put(img, 9, 3, shade(orb, 1.35))
    put(img, 11, 4, shade(orb, 0.7))
    return add_outline(img)


# ------------------------------------------------------------------- иконки брони

def armor_shape(kind: str) -> Image.Image:
    img = new_image()
    tint_hole = (40, 40, 48)
    if kind == "helmet":
        rect(img, 6, 2, 9, 2, (255, 255, 255))
        rect(img, 5, 3, 10, 3, (255, 255, 255))
        rect(img, 4, 4, 11, 8, (255, 255, 255))
        rect(img, 4, 9, 6, 10, (255, 255, 255))
        rect(img, 9, 9, 11, 10, (255, 255, 255))
        rect(img, 6, 6, 9, 7, shade(tint_hole, 0.45))  # вырез для лица
    elif kind == "chestplate":
        rect(img, 5, 2, 10, 3, (255, 255, 255))
        rect(img, 4, 4, 11, 11, (255, 255, 255))
        rect(img, 2, 4, 3, 7, (255, 255, 255))
        rect(img, 12, 4, 13, 7, (255, 255, 255))
        rect(img, 7, 5, 8, 9, shade(tint_hole, 0.5))
    elif kind == "leggings":
        rect(img, 4, 3, 11, 6, (255, 255, 255))
        rect(img, 4, 7, 6, 13, (255, 255, 255))
        rect(img, 9, 7, 11, 13, (255, 255, 255))
    else:  # boots
        rect(img, 3, 8, 6, 13, (255, 255, 255))
        rect(img, 9, 8, 12, 13, (255, 255, 255))
        rect(img, 3, 13, 7, 13, (255, 255, 255))
        rect(img, 8, 13, 12, 13, (255, 255, 255))
    return img


def tex_armor_piece(kind: str, tint: tuple[int, int, int]) -> Image.Image:
    mask = armor_shape(kind)
    img = new_image()
    for y in range(16):
        for x in range(16):
            if mask.getpixel((x, y))[3] != 0:
                factor = 1.0 if y < 8 else 0.85
                color = shade(tint, factor)
                if x - y <= -4:
                    color = shade(tint, 1.25)  # блик
                img.putpixel((x, y), rgba(color))
    return add_outline(img)


# ------------------------------------------------------------------- блоки

def brick_pattern(base: tuple[int, int, int], mortar: tuple[int, int, int]) -> Image.Image:
    img = new_image()
    rect(img, 0, 0, 15, 15, base)
    for y in range(0, 16, 4):
        rect(img, 0, y, 15, y, mortar)
    for row, y in enumerate(range(0, 16, 4)):
        offset = 0 if row % 2 == 0 else 4
        for x in range(offset, 16, 8):
            rect(img, x, y, x, min(15, y + 3), mortar)
    # лёгкая зернистость
    for y in range(16):
        for x in range(16):
            if (x * 7 + y * 13) % 11 == 0:
                px = img.getpixel((x, y))
                if px[3] and px[:3] == base:
                    img.putpixel((x, y), rgba(shade(base, 0.88)))
    return img


def tex_block(name: str) -> Image.Image:
    if name == "rune_bricks":
        img = brick_pattern((86, 92, 104), (52, 56, 64))
        for x, y in [(3, 2), (4, 2), (3, 1), (11, 6), (12, 6), (11, 7), (5, 10), (6, 10), (6, 11)]:
            put(img, x, y, (86, 214, 255))
        return img
    if name == "cracked_rune_bricks":
        img = brick_pattern((78, 80, 88), (48, 50, 56))
        for x, y in [(2, 2), (3, 3), (4, 3), (5, 4), (9, 9), (10, 10), (11, 10), (12, 11)]:
            put(img, x, y, (32, 32, 36))
        return img
    if name == "gilded_rune_bricks":
        img = brick_pattern((86, 92, 104), (52, 56, 64))
        for y in range(0, 16, 4):
            rect(img, 0, y, 15, y, (214, 170, 60))
        for x, y in [(6, 2), (7, 2), (6, 6), (10, 10), (10, 11)]:
            put(img, x, y, (255, 226, 140))
        return img
    if name == "frost_bricks":
        img = brick_pattern((150, 205, 235), (96, 150, 190))
        for x, y in [(2, 1), (7, 6), (12, 10), (4, 9), (13, 3)]:
            put(img, x, y, (240, 252, 255))
        return img
    if name == "ember_bricks":
        img = brick_pattern((70, 40, 34), (44, 24, 20))
        for x, y in [(2, 2), (3, 3), (4, 3), (5, 4), (10, 9), (11, 10), (12, 10)]:
            put(img, x, y, (255, 130, 40))
        for x, y in [(3, 2), (11, 9)]:
            put(img, x, y, (255, 220, 120))
        return img
    if name == "soul_lamp":
        img = brick_pattern((64, 70, 82), (40, 44, 52))
        rect(img, 3, 3, 12, 12, (30, 40, 56))
        rect(img, 4, 4, 11, 11, (90, 200, 255))
        rect(img, 6, 6, 9, 9, (220, 250, 255))
        return img
    if name == "boss_altar":
        img = new_image()
        rect(img, 0, 0, 15, 15, (46, 42, 50))
        for y in range(16):
            for x in range(16):
                if (x + y) % 2 == 0:
                    put(img, x, y, (58, 52, 62))
        # рунический круг
        for angle in range(0, 360, 15):
            x = 8 + int(round(math.cos(math.radians(angle)) * 5))
            y = 8 + int(round(math.sin(math.radians(angle)) * 5))
            put(img, x, y, (230, 180, 70))
        rect(img, 6, 6, 9, 9, (150, 40, 40))
        rect(img, 7, 7, 8, 8, (255, 120, 90))
        return img
    if name == "village_hearth":
        img = brick_pattern((96, 90, 84), (60, 56, 52))
        rect(img, 4, 4, 11, 11, (40, 34, 30))
        rect(img, 6, 6, 9, 10, (255, 140, 40))
        rect(img, 7, 8, 8, 10, (255, 215, 120))
        rect(img, 5, 11, 10, 12, (70, 60, 52))
        return img
    return new_image()


# ------------------------------------------------------------------- прочие текстуры

def tex_spawn_egg(base: tuple[int, int, int], spot: tuple[int, int, int]) -> Image.Image:
    img = new_image()
    for y, (x0, x1) in enumerate([(5, 10), (4, 11), (3, 12), (3, 12), (3, 12), (3, 12),
                                  (3, 12), (4, 11), (4, 11), (5, 10), (6, 9)]):
        rect(img, x0, 2 + y, x1, 2 + y, base)
    for x, y in [(6, 4), (10, 5), (5, 7), (9, 8), (7, 10), (11, 7)]:
        put(img, x, y, spot)
    return add_outline(img)


def tex_glow(size: int = 32) -> Image.Image:
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    center = (size - 1) / 2.0
    for y in range(size):
        for x in range(size):
            dist = math.hypot(x - center, y - center) / center
            alpha = max(0.0, 1.0 - dist) ** 1.6
            if alpha > 0.02:
                img.putpixel((x, y), (255, 255, 255, int(alpha * 255)))
    return img


# ------------------------------------------------------- текстуры гуманоидов (скины)

# (x, y, w, h) — разметка частей тела скина 64x64, как в скине игрока
SKIN_FACES = {
    "head": [(8, 0, 8, 8, 1.15), (16, 0, 8, 8, 0.8), (0, 8, 8, 8, 0.9),
             (8, 8, 8, 8, 1.0), (16, 8, 8, 8, 0.9), (24, 8, 8, 8, 0.85)],
    "body": [(20, 16, 8, 4, 1.15), (28, 16, 8, 4, 0.8), (16, 20, 4, 12, 0.9),
             (20, 20, 8, 12, 1.0), (28, 20, 4, 12, 0.9), (32, 20, 8, 12, 0.85)],
    "right_arm": [(44, 16, 4, 4, 1.15), (48, 16, 4, 4, 0.8), (40, 20, 4, 12, 0.9),
                  (44, 20, 4, 12, 1.0), (48, 20, 4, 12, 0.9), (52, 20, 4, 12, 0.85)],
    "left_arm": [(36, 48, 4, 4, 1.15), (40, 48, 4, 4, 0.8), (32, 52, 4, 12, 0.9),
                 (36, 52, 4, 12, 1.0), (40, 52, 4, 12, 0.9), (44, 52, 4, 12, 0.85)],
    "right_leg": [(4, 16, 4, 4, 1.15), (8, 16, 4, 4, 0.8), (0, 20, 4, 12, 0.9),
                  (4, 20, 4, 12, 1.0), (8, 20, 4, 12, 0.9), (12, 20, 4, 12, 0.85)],
    "left_leg": [(20, 48, 4, 4, 1.15), (24, 48, 4, 4, 0.8), (16, 52, 4, 12, 0.9),
                 (20, 52, 4, 12, 1.0), (24, 52, 4, 12, 0.9), (28, 52, 4, 12, 0.85)],
}


def humanoid_skin(base: tuple[int, int, int], accent: tuple[int, int, int],
                  eye: tuple[int, int, int]) -> Image.Image:
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    for faces in SKIN_FACES.values():
        for (x, y, w, h, factor) in faces:
            rect(img, x, y, x + w - 1, y + h - 1, shade(base, factor))
    # «шум» по коже, чтобы не выглядело плоско
    for y in range(64):
        for x in range(64):
            px = img.getpixel((x, y))
            if px[3] and (x * 3 + y * 5) % 9 == 0:
                img.putpixel((x, y), rgba(shade(px[:3], 0.9)))
    # лицо: глаза и «шрам»
    for x, y in [(9, 11), (10, 11), (13, 11), (14, 11)]:
        put(img, x, y, eye)
    put(img, 11, 13, accent)
    put(img, 12, 13, accent)
    return img


def build_preview_sheet(folder: Path, output: Path, columns: int = 12, scale: int = 4) -> None:
    """Собирает все иконки папки в одну картинку-превью (для документации и быстрой проверки)."""
    files = sorted(folder.glob("*.png"))
    if not files:
        return
    pad, cell = 2, 16 * scale + 2
    rows = (len(files) + columns - 1) // columns
    sheet = Image.new("RGBA", (columns * cell, rows * cell), (40, 40, 48, 255))
    for index, path in enumerate(files):
        image = Image.open(path).convert("RGBA").resize((16 * scale, 16 * scale), Image.NEAREST)
        sheet.paste(image, ((index % columns) * cell + pad, (index // columns) * cell + pad), image)
    output.parent.mkdir(parents=True, exist_ok=True)
    sheet.save(output)
    print(f"  превью: {output.relative_to(ROOT)} ({len(files)} иконок)")


# ------------------------------------------------- агрегация: что и куда писать

WEAPON_KINDS = ["dagger", "sword", "greatsword", "axe", "spear", "mace"]
WEAPON_MATERIALS = ["steel", "silver", "dusksteel"]
ARMOR_CLASSES = ["warrior", "ranger", "mage", "druid"]
ARMOR_PIECES = ["helmet", "chestplate", "leggings", "boots"]
BLOCKS = ["rune_bricks", "cracked_rune_bricks", "gilded_rune_bricks", "frost_bricks",
          "ember_bricks", "soul_lamp", "boss_altar", "village_hearth"]


def main() -> None:
    count = 0

    # предметы-материалы
    for name, (shape, palette) in MATERIAL_TEXTURE.items():
        pal = PALETTES[palette]
        img = {"ingot": tex_ingot, "shard": tex_shard, "dust": tex_dust,
               "crystal": tex_crystal, "key": tex_key}[shape](pal)
        save(img, TEX / f"item/{name}.png")
        count += 1

    # оружие
    for material in WEAPON_MATERIALS:
        pal = PALETTES[WEAPON_PALETTE[material]]
        for kind in WEAPON_KINDS:
            save(tex_weapon(kind, pal), TEX / f"item/{material}_{kind}.png")
            count += 1

    # посохи
    for name, orb in STAFF_ORB.items():
        save(tex_staff(orb), TEX / f"item/{name}.png")
        count += 1

    # броня (предметы)
    for cls in ARMOR_CLASSES:
        tint = ARMOR_CLASS_TINT[cls]
        for piece in ARMOR_PIECES:
            save(tex_armor_piece(piece, tint), TEX / f"item/{cls}_{piece}.png")
            count += 1

    # яйца призыва
    save(tex_spawn_egg((91, 30, 30), (192, 57, 43)), TEX / "item/dungeon_warden_spawn_egg.png")
    save(tex_spawn_egg((30, 59, 46), (98, 224, 107)), TEX / "item/cave_stalker_spawn_egg.png")
    count += 2

    # блоки
    for name in BLOCKS:
        save(tex_block(name), TEX / f"block/{name}.png")
        count += 1

    # мобы (разметка скина 64x64) и блик снаряда
    save(humanoid_skin((46, 60, 48), (120, 60, 40), (220, 240, 120)), TEX / "entity/cave_stalker.png")
    save(humanoid_skin((96, 40, 38), (200, 170, 80), (255, 200, 90)), TEX / "entity/dungeon_warden.png")
    save(tex_glow(32), TEX / "entity/spell_projectile.png")
    count += 3

    # слои брони 64x32 (сетка: голова 0,0 / тело 16,16 / руки 40,16 / ноги 0,16)
    layer1_faces = [(0, 0, 32, 16, 1.0), (16, 16, 24, 16, 1.0), (40, 16, 16, 16, 0.95), (0, 16, 16, 16, 0.9)]
    for cls in ARMOR_CLASSES:
        tint = ARMOR_CLASS_TINT[cls]
        for layer in (1, 2):
            img = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
            faces = layer1_faces if layer == 1 else [(0, 16, 16, 16, 1.0), (16, 16, 24, 16, 1.0)]
            for (x, y, w, h, factor) in faces:
                rect(img, x, y, x + w - 1, y + h - 1, shade(tint, factor))
            # золотая окантовка — «классовая» деталь
            for (x, y, w, h, _f) in faces:
                rect(img, x, y, x + w - 1, y, shade(tint, 1.35))
            save(img, TEX / f"models/armor/{cls}_layer_{layer}.png")
            count += 1

    # контрольный лист (превью) — чтобы одним взглядом видеть все иконки
    build_preview_sheet(TEX / "item", ROOT / "docs/preview/textures_items.png", columns=12)
    build_preview_sheet(TEX / "block", ROOT / "docs/preview/textures_blocks.png", columns=8)
    print(f"готово, файлов текстур: {count}")


if __name__ == "__main__":
    main()
