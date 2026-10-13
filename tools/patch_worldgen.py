#!/usr/bin/env python3
"""Переработка генерации мира: патчим ванильные worldgen-JSON'ы и кладём их в мод.

Запуск:  python3 tools/patch_worldgen.py

Мод-ресурсы работают как data pack более высокого приоритета, чем ванильный,
поэтому файл `data/minecraft/worldgen/noise_settings/overworld.json` в моде
ПОЛНОСТЬЮ заменяет ванильный — именно так и переделывается генерация.

Что делает скрипт:
  1. Скачивает ванильные JSON'ы (1.20.1) из репозитория misode/mcmeta
     (через GitHub API; при обычном интернете — через raw.githubusercontent).
  2. Патчит их по параметрам ниже.
  3. Пишет результат в src/main/resources/data/minecraft/... (оверрайды)
     и в src/main/resources/data/rpgmod/... (наши дополнения: no-op фичи, структуры).

Параметры мира — правьте константы ниже и перезапускайте скрипт.
"""
from __future__ import annotations

import json
import shutil
import subprocess
import urllib.request
from pathlib import Path

# ------------------------------------------------------------------ НАСТРОЙКИ МИРА

# Высота мира (было 384). 512 = «удвоенная высота застройки» из ТЗ:
# min_y = -64, максимальная высота = -64 + 512 - 1 = 447.
WORLD_HEIGHT = 512
MIN_Y = -64
LOGICAL_HEIGHT = 512

# Шум поверхности: больше size_horizontal -> более крупный и плавный рельеф
# (равнины плавно переходят в горы, а не «дрожат»).
SURFACE_SIZE_HORIZONTAL = 2
SURFACE_SIZE_VERTICAL = 2

# Масштаб биомных шумов (температура/влажность/континентальность/эрозия/хребты).
# Чем МЕНЬШЕ значение, тем КРУПНЕЕ биомы. Ванильное 0.25.
BIOME_NOISE_SCALE = 0.15

# Уровень моря (крупные океаны). Ванильное 63.
SEA_LEVEL = 63

# Убираем ванильные «данжи» (комнаты мобов). True = убрать.
REMOVE_VANILLA_MONSTER_ROOMS = True

# ------------------------------------------------------------------------ служебное

ROOT = Path(__file__).resolve().parent.parent
RES = ROOT / "src/main/resources"
MC = RES / "data/minecraft"
RP = RES / "data/rpgmod"

MC_REF = "1.20.1-data"
MC_REPO = "misode/mcmeta"

CACHE = ROOT / "tools/.cache"


def download(rel_path: str) -> str:
    """Скачивает файл из misode/mcmeta (ветка 1.20.1-data)."""
    CACHE.mkdir(parents=True, exist_ok=True)
    cached = CACHE / rel_path.replace("/", "_")
    if cached.exists() and cached.stat().st_size > 0:
        return cached.read_text(encoding="utf-8")

    url = f"https://raw.githubusercontent.com/{MC_REPO}/{MC_REF}/{rel_path}"
    try:
        with urllib.request.urlopen(url, timeout=30) as response:
            text = response.read().decode("utf-8")
    except Exception:
        # запасной путь: GitHub API через gh (работает из песочницы)
        api = f"repos/{MC_REPO}/contents/{rel_path}?ref={MC_REF}"
        result = subprocess.run(
            ["gh", "api", "-H", "Accept: application/vnd.github.raw", api],
            capture_output=True, text=True)
        if result.returncode != 0:
            raise RuntimeError(f"не удалось скачать {rel_path}: {result.stderr.strip()}") from None
        text = result.stdout
    if len(text) < 50:
        raise RuntimeError(f"подозрительно короткий ответ для {rel_path}: {text[:120]!r}")
    cached.write_text(text, encoding="utf-8")
    return text


def write_json(path: Path, data) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")


def bump_scale(node, factor: float = 0.6) -> int:
    """Уменьшает xz_scale у всех shifted_noise внутри json-дерева (крупнее биомы)."""
    changed = 0
    if isinstance(node, dict):
        if node.get("type") == "minecraft:shifted_noise" and "xz_scale" in node:
            node["xz_scale"] = round(float(node["xz_scale"]) * factor, 5)
            changed += 1
        for value in node.values():
            changed += bump_scale(value, factor)
    elif isinstance(node, list):
        for value in node:
            changed += bump_scale(value, factor)
    return changed


# -------------------------------------------------------------- 1. высота и рельеф

def patch_noise_settings() -> None:
    text = download("data/minecraft/worldgen/noise_settings/overworld.json")
    data = json.loads(text)

    data["noise"]["height"] = WORLD_HEIGHT
    data["noise"]["min_y"] = MIN_Y
    data["noise"]["size_horizontal"] = SURFACE_SIZE_HORIZONTAL
    data["noise"]["size_vertical"] = SURFACE_SIZE_VERTICAL
    data["sea_level"] = SEA_LEVEL

    # крупные биомы: уменьшаем масштаб у инлайновых биомных шумов (temperature и т.п.)
    factor = BIOME_NOISE_SCALE / 0.25
    changed = bump_scale(data["noise_router"], factor)

    write_json(MC / "worldgen/noise_settings/overworld.json", data)
    print(f"  noise_settings/overworld.json: height={WORLD_HEIGHT}, "
          f"size_horizontal={SURFACE_SIZE_HORIZONTAL}, инлайновых биомных шумов изменено: {changed}")


def patch_biome_density_functions() -> None:
    factor = BIOME_NOISE_SCALE / 0.25
    for name in ("continents", "erosion", "ridges"):
        rel = f"data/minecraft/worldgen/density_function/overworld/{name}.json"
        data = json.loads(download(rel))
        bump_scale(data, factor)
        write_json(MC / f"worldgen/density_function/overworld/{name}.json", data)
        print(f"  density_function/overworld/{name}.json: xz_scale -> {data['argument']['xz_scale']}")


def patch_dimension_type() -> None:
    data = json.loads(download("data/minecraft/dimension_type/overworld.json"))
    data["height"] = WORLD_HEIGHT
    data["logical_height"] = LOGICAL_HEIGHT
    data["min_y"] = MIN_Y
    write_json(MC / "dimension_type/overworld.json", data)
    print(f"  dimension_type/overworld.json: height={WORLD_HEIGHT}, min_y={MIN_Y}")


# --------------------------------------------------- 2. убираем ванильные «данжи»

def remove_monster_rooms() -> None:
    if not REMOVE_VANILLA_MONSTER_ROOMS:
        return
    # placed_feature с count = 0 никогда не запускается -> комнат мобов в мире нет
    for name in ("monster_room", "monster_room_deep"):
        data = json.loads(download(f"data/minecraft/worldgen/placed_feature/{name}.json"))
        data["placement"] = [{"type": "minecraft:count", "count": 0}]
        write_json(MC / f"worldgen/placed_feature/{name}.json", data)
        print(f"  placed_feature/{name}.json: отключено (count = 0)")


# --------------------------------------------- 3. наша фича-заглушка (для структур)

def write_no_op_feature() -> None:
    """Фича, которая ничего не делает (воздух в воздухе) — нужна как безопасная заглушка."""
    write_json(RP / "worldgen/configured_feature/no_op.json", {
        "type": "minecraft:simple_block",
        "config": {
            "to_place": {
                "type": "minecraft:simple_state_provider",
                "state": {"Name": "minecraft:air"},
            },
        },
    })
    print("  rpgmod:worldgen/configured_feature/no_op.json записан")


def cleanup_vanilla_boss_structures() -> None:
    """Убираем ванильные структуры-«данжи» и деревни: остаются только наши + крепости.

    Делается тегами биомов: структура не появится ни в одном биоме, если её тег пуст.
    Крепости (stronghold) оставляем — иначе не попасть в Край.
    """
    disable = [
        # подземелья и руины
        "ancient_city", "mineshaft", "mineshaft_mesa",
        "desert_pyramid", "jungle_temple", "igloo", "swamp_hut", "trail_ruins",
        "woodland_mansion", "pillager_outpost",
        # вода
        "ocean_monument", "ocean_ruin_cold", "ocean_ruin_warm",
        "shipwreck", "shipwreck_beached", "buried_treasure",
        # деревни — вместо них наши «умные» деревни с очагом
        "village_plains", "village_desert", "village_savanna", "village_snowy", "village_taiga",
    ]
    for name in disable:
        write_json(MC / f"tags/worldgen/biome/has_structure/{name}.json",
                   {"replace": True, "values": []})
    print(f"  отключено ванильных структур: {len(disable)} (крепости и порталы оставлены)")


def main() -> None:
    if CACHE.exists():
        shutil.rmtree(CACHE)
    print("Патчим генерацию мира (ванильные JSON'ы -> оверрайды в моде):")
    patch_noise_settings()
    patch_biome_density_functions()
    patch_dimension_type()
    remove_monster_rooms()
    write_no_op_feature()
    cleanup_vanilla_boss_structures()
    print("Готово. Настройки мира — константы в начале этого файла.")


if __name__ == "__main__":
    main()
