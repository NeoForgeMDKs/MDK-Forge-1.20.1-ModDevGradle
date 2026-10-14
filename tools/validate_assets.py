#!/usr/bin/env python3
"""Проверка целостности ресурсов мода — «компилятор» для JSON'ов и текстур.

Запуск:  python3 tools/validate_assets.py

Что проверяется:
  1. у каждого предмета есть модель, текстура и название в ru_ru/en_us;
  2. у каждого блока есть модель, blockstate и текстура;
  3. все ключи переводов, которые использует код (translatable("...")), есть в lang-файлах;
  4. все ссылки rpgmod:<id> в data-JSON'ах указывают на существующие предметы/блоки;
  5. все .nbt-шаблоны, упомянутые в template_pool, существуют;
  6. все лут-таблицы, указанные в сундуках структур, существуют.

Скрипт ничего не меняет — только печатает найденные проблемы и код возврата 1,
если что-то не сходится (удобно втыкать в CI).
"""
from __future__ import annotations

import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
RES = ROOT / "src/main/resources"
ASSETS = RES / "assets/rpgmod"
DATA = RES / "data"
FORGE_LIST = RES / "data/forge/loot_modifiers/global_loot_modifiers.json"
MC = RES / "data/minecraft"
JAVA = ROOT / "src/main/java/com/closky/rpgmod"

sys.path.insert(0, str(Path(__file__).parent))
import gen_lang_models as gl  # noqa: E402  (списки предметов/блоков живут там)

problems: list[str] = []


def fail(message: str) -> None:
    problems.append(message)


def load_lang(locale: str) -> dict:
    path = ASSETS / f"lang/{locale}.json"
    if not path.exists():
        fail(f"нет lang-файла {path.relative_to(ROOT)}")
        return {}
    return json.loads(path.read_text(encoding="utf-8"))


# --------------------------------------------------------------- 1. предметы

def check_items(ru: dict, en: dict) -> None:
    ids = (gl.MATERIALS + gl.WEAPONS + gl.STAVES + gl.ARMOR + gl.SPAWN_EGGS + gl.BLOCKS)
    for item_id in ids:
        model = ASSETS / f"models/item/{item_id}.json"
        if not model.exists():
            fail(f"нет модели предмета: models/item/{item_id}.json")
        if item_id not in gl.BLOCKS:
            texture = ASSETS / f"textures/item/{item_id}.png"
            if not texture.exists():
                fail(f"нет текстуры предмета: textures/item/{item_id}.png")
        # у блока-предмета ключ перевода — block.rpgmod.<id>, у обычного предмета — item.rpgmod.<id>
        key = f"block.rpgmod.{item_id}" if item_id in gl.BLOCKS else f"item.rpgmod.{item_id}"
        if key not in ru and key not in en:
            fail(f"нет перевода для {item_id} (ожидался ключ {key})")
    print(f"  предметов проверено: {len(ids)}")


# ---------------------------------------------------------------- 2. блоки

def check_blocks() -> None:
    for name in gl.BLOCKS:
        for path, what in ((ASSETS / f"blockstates/{name}.json", "blockstate"),
                           (ASSETS / f"models/block/{name}.json", "модель блока"),
                           (ASSETS / f"textures/block/{name}.png", "текстура блока")):
            if not path.exists():
                fail(f"нет {what}: {path.relative_to(ROOT)}")
    print(f"  блоков проверено: {len(gl.BLOCKS)}")


# ------------------------------------------------------- 3. ключи переводов

KEY_PATTERN = re.compile(r'translatable\(\s*"([^"]+)"')


def collect_code_keys() -> set[str]:
    keys: set[str] = set()
    for path in JAVA.rglob("*.java"):
        text = path.read_text(encoding="utf-8")
        keys.update(KEY_PATTERN.findall(text))
    # ключи, собираемые из id перечислений
    def ids(path: str, pattern: str) -> list[str]:
        return re.findall(pattern, (JAVA / path).read_text(encoding="utf-8"))

    for group, path, pattern, suffix in (
        ("weapon_class", "item/WeaponClass.java", r"\(\"([a-z_]+)\"", True),
        ("weapon_material", "item/WeaponMaterial.java", r"\(\"([a-z_]+)\"", False),
        ("weapon_quality", "item/WeaponQuality.java", r"\(\"([a-z_]+)\"", False),
        ("weapon_affix", "item/WeaponAffix.java", r"\(\"([a-z_]+)\"", False),
        ("hone", "item/HoneMaterial.java", r"\(\"([a-z_]+)\"", True),
    ):
        for value in ids(path, pattern):
            keys.add(f"{group}.rpgmod.{value}")
            if suffix:
                keys.add(f"{group}.rpgmod.{value}.desc")
    for value in ids("player/StatType.java", r"\(\"([a-z_]+)\""):
        keys.add(f"stat.rpgmod.{value}")
        keys.add(f"stat.rpgmod.{value}.desc")
    for value in ids("player/PlayerClass.java", r"\(\"([a-z_]+)\""):
        keys.add(f"class.rpgmod.{value}")
        keys.add(f"class.rpgmod.{value}.bonus")
    for value in ids("magic/SpellElement.java", r"\(\"([a-z_]+)\""):
        keys.add(f"element.rpgmod.{value}")
        keys.add(f"element.rpgmod.{value}.desc")
    for value in ids("magic/Spells.java", r"new \w+Spell\(\"([a-z_]+)\""):
        keys.add(f"spell.rpgmod.{value}")
        keys.add(f"spell.rpgmod.{value}.desc")
    for value in ids("village/VillageType.java", r"\(\"([a-z_]+)\""):
        keys.add(f"village_type.rpgmod.{value}")
    return {key for key in keys if not key.endswith(".")}


def check_lang(ru: dict, en: dict) -> None:
    code_keys = collect_code_keys()
    # ключи, собранные склейкой строк в коде, скрипт не видит — их проверяем по группам
    extra = set()
    for prefix in ("msg.rpgmod.", "tooltip.rpgmod.", "hud.rpgmod.", "name.rpgmod.", "spell.rpgmod.",
                   "element.rpgmod.", "stat.rpgmod.", "hone.rpgmod.", "weapon_class.rpgmod.",
                   "weapon_material.rpgmod.", "weapon_quality.rpgmod.", "weapon_affix.rpgmod.",
                   "village_type.rpgmod.", "effect.rpgmod.", "entity.rpgmod.", "item.rpgmod.",
                   "class.rpgmod.",
                   "block.rpgmod.", "itemGroup.rpgmod."):
        for key in list(ru) + list(en):
            if key.startswith(prefix):
                extra.add(key)
    for key in sorted(code_keys | extra):
        if key.startswith("minecraft.") or key.startswith("itemGroup."):
            continue
        # ключи-параметры (например "spell.rpgmod." из склейки) не проверяем
        if key.endswith("."):
            continue
        if key not in ru:
            fail(f"нет русского перевода: {key}")
        if key not in en:
            fail(f"нет английского перевода: {key}")
    missing_ru = [k for k in ru if k not in en]
    missing_en = [k for k in en if k not in ru]
    for key in missing_ru[:20]:
        fail(f"ключ есть в ru_ru, но нет в en_us: {key}")
    for key in missing_en[:20]:
        fail(f"ключ есть в en_us, но нет в ru_ru: {key}")
    print(f"  ключей переводов: ru={len(ru)}, en={len(en)}, использовано в коде: {len(code_keys)}")


# ------------------------------------------------- 4. ссылки rpgmod:<id> в data

KNOWN_ITEMS = set(gl.MATERIALS + gl.WEAPONS + gl.STAVES + gl.ARMOR + gl.SPAWN_EGGS + gl.BLOCKS)


def registered_entity_types() -> set[str]:
    """Типы сущностей, зарегистрированные в ModEntities.java."""
    text = (JAVA / "registry/ModEntities.java").read_text(encoding="utf-8")
    return set(re.findall(r'ENTITIES\.register\("([a-z0-9_]+)"', text))


def check_data_references() -> None:
    loot_root = DATA / "rpgmod/loot_tables"
    tables = {p.relative_to(loot_root).with_suffix("").as_posix() for p in loot_root.rglob("*.json")}
    structure_root = DATA / "rpgmod/worldgen/structure"
    structures = {p.stem for p in structure_root.glob("*.json")}
    templates = {p.relative_to(DATA / "rpgmod/structures").with_suffix("").as_posix()
                 for p in (DATA / "rpgmod/structures").rglob("*.nbt")}
    entities = registered_entity_types()
    biome_tags = {p.stem for p in (DATA / "rpgmod/tags/worldgen/biome/has_structure").glob("*.json")}

    checked = 0

    # 1) предметы внутри лут-таблиц
    for path in sorted(loot_root.rglob("*.json")):
        data = json.loads(path.read_text(encoding="utf-8"))
        for match in re.finditer(r'"name":\s*"rpgmod:([a-z0-9_]+)"', json.dumps(data)):
            if match.group(1) not in KNOWN_ITEMS:
                fail(f"{path.relative_to(ROOT)}: нет такого предмета rpgmod:{match.group(1)}")
            checked += 1

    # 2) наши блоки в тегах блоков
    for path in sorted((MC / "tags/blocks").rglob("*.json")):
        data = json.loads(path.read_text(encoding="utf-8"))
        for value in data.get("values", []):
            name = value.get("id") if isinstance(value, dict) else value
            if isinstance(name, str) and name.startswith("rpgmod:"):
                if name.split(":", 1)[1] not in gl.BLOCKS:
                    fail(f"{path.relative_to(ROOT)}: нет такого блока {name}")
                checked += 1

    # 3) структуры -> наборы структур
    for path in sorted((DATA / "rpgmod/worldgen/structure_set").glob("*.json")):
        text = path.read_text(encoding="utf-8")
        for name in re.findall(r'"structure":\s*"rpgmod:([a-z0-9_]+)"', text):
            if name not in structures:
                fail(f"{path.relative_to(ROOT)}: нет структуры rpgmod:{name}")
            checked += 1

    # 4) структуры -> пулы, биомные теги, мобы
    for path in sorted(structure_root.glob("*.json")):
        text = path.read_text(encoding="utf-8")
        for pool_name in re.findall(r'"start_pool":\s*"rpgmod:([a-z0-9_/]+)"', text):
            if not (DATA / f"rpgmod/worldgen/template_pool/{pool_name}.json").exists():
                fail(f"{path.relative_to(ROOT)}: нет пула шаблонов rpgmod:{pool_name}")
            checked += 1
        for tag in re.findall(r'"biomes":\s*"#rpgmod:has_structure/([a-z0-9_]+)"', text):
            if tag not in biome_tags:
                fail(f"{path.relative_to(ROOT)}: нет тега биомов rpgmod:has_structure/{tag}")
            checked += 1
        for entity in re.findall(r'"type":\s*"rpgmod:([a-z0-9_]+)"', text):
            if entity not in entities:
                fail(f"{path.relative_to(ROOT)}: нет сущности rpgmod:{entity}")
            checked += 1

    # 5) пулы -> .nbt шаблоны
    for path in sorted((DATA / "rpgmod/worldgen/template_pool").rglob("*.json")):
        text = path.read_text(encoding="utf-8")
        for location in re.findall(r'"location":\s*"rpgmod:([a-z0-9_/]+)"', text):
            if location not in templates:
                fail(f"{path.relative_to(ROOT)}: нет шаблона data/rpgmod/structures/{location}.nbt")
            checked += 1

    # 6) сундуки внутри .nbt -> лут-таблицы
    import gzip
    for path in (DATA / "rpgmod/structures").rglob("*.nbt"):
        try:
            raw = gzip.decompress(path.read_bytes())
        except OSError:
            fail(f"не удалось прочитать шаблон {path.relative_to(ROOT)}")
            continue
        for match in re.finditer(rb"rpgmod:chests/[a-z_]+", raw):
            table = match.group(0).decode("utf-8").split(":", 1)[1]
            if table not in tables:
                fail(f"{path.relative_to(ROOT)}: сундук ссылается на несуществующую таблицу rpgmod:{table}")
            checked += 1

    # 7) лут-модификаторы -> лут-таблицы ванилы (просто проверяем, что type наш)
    for path in sorted((DATA / "rpgmod/loot_modifiers").glob("*.json")):
        data = json.loads(path.read_text(encoding="utf-8"))
        if data.get("type") != "rpgmod:add_weapon":
            fail(f"{path.relative_to(ROOT)}: неизвестный type {data.get('type')}")
        for condition in data.get("conditions", []):
            if condition.get("condition") != "forge:loot_table_id":
                fail(f"{path.relative_to(ROOT)}: неизвестное условие {condition.get('condition')}")

    global_list = FORGE_LIST
    if not global_list.exists():
        fail("нет data/forge/loot_modifiers/global_loot_modifiers.json")
    else:
        entries = json.loads(global_list.read_text(encoding="utf-8"))["entries"]
        for entry in entries:
            if not (DATA / f"rpgmod/loot_modifiers/{entry.split(':', 1)[1]}.json").exists():
                fail(f"global_loot_modifiers.json: нет файла для {entry}")
            checked += 1

    print(f"  лут-таблиц: {len(tables)}, структур: {len(structures)}, шаблонов: {len(templates)}, "
          f"проверено ссылок: {checked}")


# ------------------------------------------------------------- 5. текстуры мобов

def check_entity_assets() -> None:
    for name in ("cave_stalker", "dungeon_warden", "spell_projectile"):
        path = ASSETS / f"textures/entity/{name}.png"
        if not path.exists():
            fail(f"нет текстуры сущности: {path.relative_to(ROOT)}")
    for cls in gl.ARMOR_CLASSES:
        for layer in (1, 2):
            path = ASSETS / f"textures/models/armor/{cls}_layer_{layer}.png"
            if not path.exists():
                fail(f"нет текстуры брони: {path.relative_to(ROOT)}")
    print("  текстуры сущностей и брони на месте")


# ------------------------------------------- 6. грабли компиляции (проверено на практике)

# pattern -> пояснение, что делать
FORBIDDEN_JAVA_PATTERNS = [
    (r"import\s+org\.jetbrains\.annotations\.",
     "аннотаций org.jetbrains.annotations нет на classpath проекта (ModDevGradle 1.20.1). "
     "Уберите импорт: компилятор их не требует, а IDE подскажет типы и так."),
    (r"import\s+net\.minecraft\.world\.item\.Ingredient;",
     "Ingredient лежит в net.minecraft.world.item.crafting.Ingredient (в 1.20.1)."),
    (r"damageSources\(\)\.source\(\s*[A-Za-z_][A-Za-z0-9_.]*\s*\);",
     "в 1.20.1 метод DamageSources#source(ResourceKey) приватный — "
     "вызывайте source(key, cause, direct), например source(KEY, null, null)."),
    (r"VillagerTrades\.BasicItemListing",
     "класса BasicItemListing нет в 1.20.1 — используйте VillagerTrades.ItemsForEmeralds / "
     "VillagerTrades.EmeraldForItems."),
]


def check_java_patterns() -> None:
    for path in sorted(JAVA.rglob("*.java")):
        text = path.read_text(encoding="utf-8")
        for pattern, advice in FORBIDDEN_JAVA_PATTERNS:
            if re.search(pattern, text):
                fail(f"{path.relative_to(ROOT)}: {advice}")
    print(f"  java-файлов проверено на известные грабли: {len(list(JAVA.rglob('*.java')))}")


def main() -> int:
    ru, en = load_lang("ru_ru"), load_lang("en_us")
    print("Проверка ресурсов мода:")
    check_items(ru, en)
    check_blocks()
    check_lang(ru, en)
    check_data_references()
    check_entity_assets()
    check_java_patterns()
    if problems:
        print(f"\nНайдено проблем: {len(problems)}")
        for problem in problems:
            print(f"  - {problem}")
        return 1
    print("\nВсё сходится: проблем нет.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
