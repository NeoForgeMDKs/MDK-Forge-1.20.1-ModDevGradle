#!/usr/bin/env python3
"""Генератор lang-файлов, моделей предметов/блоков и blockstate-файлов для RPG Overhaul.

Запуск:  python3 tools/gen_lang_models.py

Куда пишется:
  src/main/resources/assets/rpgmod/lang/ru_ru.json
  src/main/resources/assets/rpgmod/lang/en_us.json
  src/main/resources/assets/rpgmod/models/item/*.json
  src/main/resources/assets/rpgmod/models/block/*.json
  src/main/resources/assets/rpgmod/blockstates/*.json

Если вы добавили новый предмет в ModItems — допишите его id в списки ниже и перезапустите скрипт.
"""
from __future__ import annotations

import json
import shutil
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
ASSETS = ROOT / "src/main/resources/assets"
PKG = ASSETS / "rpgmod"

# ------------------------------------------------------------------ списки предметов

MATERIALS = [
    "steel_ingot", "silver_ingot", "dusk_ingot", "arcane_dust",
    "frost_shard", "ember_ingot", "void_shard", "mana_crystal", "dungeon_key",
]

WEAPON_MATERIALS = ["steel", "silver", "dusksteel"]
WEAPON_CLASSES = ["dagger", "sword", "greatsword", "axe", "spear", "mace"]
WEAPONS = [f"{m}_{c}" for m in WEAPON_MATERIALS for c in WEAPON_CLASSES]

ARMOR_CLASSES = ["warrior", "ranger", "mage", "druid"]
ARMOR_PIECES = ["helmet", "chestplate", "leggings", "boots"]
ARMOR = [f"{c}_{p}" for c in ARMOR_CLASSES for p in ARMOR_PIECES]

STAVES = ["fire_staff", "frost_staff", "storm_staff", "nature_staff"]
SPAWN_EGGS = ["dungeon_warden_spawn_egg", "cave_stalker_spawn_egg"]

BLOCKS = [
    "rune_bricks", "cracked_rune_bricks", "gilded_rune_bricks",
    "frost_bricks", "ember_bricks", "soul_lamp", "boss_altar", "village_hearth",
]

# ------------------------------------------------------------------- русские названия

# Прилагательное материала в трёх родах (кинжал - м, копьё - с, булава - ж)
RU_MATERIAL_WORD = {
    "steel": {"m": "Стальной", "f": "Стальная", "n": "Стальное"},
    "silver": {"m": "Серебряный", "f": "Серебряная", "n": "Серебряное"},
    "dusksteel": {"m": "Сумрачный", "f": "Сумрачная", "n": "Сумрачное"},
}
RU_CLASS_WORD = {
    "dagger": "кинжал", "sword": "меч", "greatsword": "двуручник",
    "axe": "топор", "spear": "копьё", "mace": "булава",
}
RU_CLASS_GENDER = {
    "dagger": "m", "sword": "m", "greatsword": "m",
    "axe": "m", "spear": "n", "mace": "f",
}
EN_MATERIAL_WORD = {"steel": "Steel", "silver": "Silver", "dusksteel": "Dusksteel"}
EN_CLASS_WORD = {
    "dagger": "Dagger", "sword": "Sword", "greatsword": "Greatsword",
    "axe": "Axe", "spear": "Spear", "mace": "Mace",
}

RU_ARMOR_CLASS = {"warrior": "воина", "ranger": "стрелка", "mage": "мага", "druid": "друида"}
RU_ARMOR_PIECE = {"helmet": "Шлем", "chestplate": "Нагрудник", "leggings": "Поножи", "boots": "Ботинки"}
EN_ARMOR_CLASS = {"warrior": "Warrior", "ranger": "Ranger", "mage": "Mage", "druid": "Druid"}
EN_ARMOR_PIECE = {"helmet": "Helmet", "chestplate": "Chestplate", "leggings": "Leggings", "boots": "Boots"}


def weapon_names() -> tuple[dict[str, str], dict[str, str]]:
    ru, en = {}, {}
    for mat in WEAPON_MATERIALS:
        for cls in WEAPON_CLASSES:
            gender = RU_CLASS_GENDER[cls]
            ru[f"{mat}_{cls}"] = f"{RU_MATERIAL_WORD[mat][gender]} {RU_CLASS_WORD[cls]}"
            en[f"{mat}_{cls}"] = f"{EN_MATERIAL_WORD[mat]} {EN_CLASS_WORD[cls]}"
    return ru, en


RU_WEAPON_NAMES, EN_WEAPON_NAMES = weapon_names()

# ------------------------------------------------------------------- словари строк

RU: dict[str, str] = {
    # вкладка творческого режима
    "itemGroup.rpgmod.main": "RPG Overhaul",

    # материалы
    "item.rpgmod.steel_ingot": "Стальной слиток",
    "item.rpgmod.silver_ingot": "Серебряный слиток",
    "item.rpgmod.dusk_ingot": "Сумрачный слиток",
    "item.rpgmod.arcane_dust": "Арканная пыль",
    "item.rpgmod.frost_shard": "Осколок льда",
    "item.rpgmod.ember_ingot": "Тлеющий слиток",
    "item.rpgmod.void_shard": "Осколок пустоты",
    "item.rpgmod.mana_crystal": "Кристалл маны",
    "item.rpgmod.dungeon_key": "Ключ подземелья",
    "item.rpgmod.steel_ingot.desc": "Сплав железа и угля. Основа оружия и брони.",
    "item.rpgmod.silver_ingot.desc": "Серебро. Подходит для отделки оружия и украшений.",
    "item.rpgmod.dusk_ingot.desc": "Редкий сплав из глубин подземелий.",
    "item.rpgmod.arcane_dust.desc": "Пыль, собранная с рунических кирпичей.",
    "item.rpgmod.frost_shard.desc": "Осколок вечного льда. Заточка холодом.",
    "item.rpgmod.ember_ingot.desc": "Слиток, выкованный в жерле вулкана.",
    "item.rpgmod.void_shard.desc": "Осколок пустоты. Оружие с ним пьёт жизни врагов.",
    "item.rpgmod.mana_crystal.desc": "Кристалл чистой маны. Восстанавливает ману.",
    "item.rpgmod.dungeon_key.desc": "Ключ от алтаря босса в подземелье.",

    # посохи
    "item.rpgmod.fire_staff": "Посох огня",
    "item.rpgmod.frost_staff": "Посох мороза",
    "item.rpgmod.storm_staff": "Посох бури",
    "item.rpgmod.nature_staff": "Посох природы",

    # яйца призыва
    "item.rpgmod.dungeon_warden_spawn_egg": "Яйцо призыва: Хранитель подземелий",
    "item.rpgmod.cave_stalker_spawn_egg": "Яйцо призыва: Пещерный хищник",

    # блоки
    "block.rpgmod.rune_bricks": "Рунические кирпичи",
    "block.rpgmod.cracked_rune_bricks": "Потрескавшиеся рунические кирпичи",
    "block.rpgmod.gilded_rune_bricks": "Позолоченные рунические кирпичи",
    "block.rpgmod.frost_bricks": "Ледяные кирпичи",
    "block.rpgmod.ember_bricks": "Огненные кирпичи",
    "block.rpgmod.soul_lamp": "Лампа душ",
    "block.rpgmod.boss_altar": "Алтарь босса",
    "block.rpgmod.village_hearth": "Очаг деревни",

    # сущности
    "entity.rpgmod.spell_projectile": "Снаряд заклинания",
    "entity.rpgmod.dungeon_warden": "Хранитель подземелий",
    "entity.rpgmod.cave_stalker": "Пещерный хищник",

    # эффекты
    "effect.rpgmod.bleeding": "Кровотечение",
    "effect.rpgmod.frostbite": "Обморожение",
    "effect.rpgmod.shocked": "Разряд",
    "effect.rpgmod.nature_blessing": "Благословение природы",

    # классы оружия
    "weapon_class.rpgmod.dagger": "Кинжал",
    "weapon_class.rpgmod.sword": "Меч",
    "weapon_class.rpgmod.greatsword": "Двуручник",
    "weapon_class.rpgmod.axe": "Топор",
    "weapon_class.rpgmod.spear": "Копьё",
    "weapon_class.rpgmod.mace": "Булава",
    "weapon_class.rpgmod.dagger.desc": "Быстрый, но слабый. Хорош против одиночных целей.",
    "weapon_class.rpgmod.sword.desc": "Универсальное оружие: баланс урона и скорости.",
    "weapon_class.rpgmod.greatsword.desc": "Самый сильный удар, но медленный замах.",
    "weapon_class.rpgmod.axe.desc": "Тяжёлый удар, ломает броню противника.",
    "weapon_class.rpgmod.spear.desc": "Бьёт дальше остальных, удобно держать врага на расстоянии.",
    "weapon_class.rpgmod.mace.desc": "Дробящий удар, оглушает и отбрасывает.",

    # материалы оружия
    "weapon_material.rpgmod.steel": "Сталь",
    "weapon_material.rpgmod.silver": "Серебро",
    "weapon_material.rpgmod.dusksteel": "Сумрачная сталь",

    # качество
    "weapon_quality.rpgmod.common": "Обычное",
    "weapon_quality.rpgmod.uncommon": "Необычное",
    "weapon_quality.rpgmod.rare": "Редкое",
    "weapon_quality.rpgmod.epic": "Эпическое",
    "weapon_quality.rpgmod.legendary": "Легендарное",

    # аффиксы
    "weapon_affix.rpgmod.sharp": "Острый",
    "weapon_affix.rpgmod.swift": "Быстрый",
    "weapon_affix.rpgmod.vampiric": "Вампирский",
    "weapon_affix.rpgmod.runic": "Рунический",
    "weapon_affix.rpgmod.reaching": "Дальнобойный",
    "weapon_affix.rpgmod.stalwart": "Крепкий",
    "weapon_affix.rpgmod.frostbound": "Скованный льдом",
    "weapon_affix.rpgmod.emberforged": "Раскалённый",
    "weapon_affix.rpgmod.stormbound": "Громовой",
    "weapon_affix.rpgmod.verdant": "Живительный",

    # заточка
    "hone.rpgmod.none": "Без заточки",
    "hone.rpgmod.none.desc": "Обычное оружие без заточки.",
    "hone.rpgmod.iron": "Железная заточка",
    "hone.rpgmod.iron.desc": "+10% урона, +8 прочности.",
    "hone.rpgmod.gold": "Золотая заточка",
    "hone.rpgmod.gold.desc": "+25% опыта оружия.",
    "hone.rpgmod.diamond": "Алмазная заточка",
    "hone.rpgmod.diamond.desc": "+15% урона, +20% прочности.",
    "hone.rpgmod.netherite": "Незеритовая заточка",
    "hone.rpgmod.netherite.desc": "+25% урона, +35 прочности.",
    "hone.rpgmod.amethyst": "Аметистовая заточка",
    "hone.rpgmod.amethyst.desc": "+15% урона магией.",
    "hone.rpgmod.frost": "Ледяная заточка",
    "hone.rpgmod.frost.desc": "Удар замораживает и замедляет врага.",
    "hone.rpgmod.ember": "Огненная заточка",
    "hone.rpgmod.ember.desc": "Удар поджигает врага.",
    "hone.rpgmod.void": "Заточка пустоты",
    "hone.rpgmod.void.desc": "Удар ослабляет и ворует здоровье.",

    # характеристики
    "stat.rpgmod.strength": "Сила",
    "stat.rpgmod.vitality": "Живучесть",
    "stat.rpgmod.intellect": "Интеллект",
    "stat.rpgmod.strength.desc": "Увеличивает урон в ближнем бою.",
    "stat.rpgmod.vitality.desc": "Увеличивает максимальное здоровье.",
    "stat.rpgmod.intellect.desc": "Увеличивает ману и силу заклинаний.",

    # элементы магии
    "element.rpgmod.fire": "Огонь",
    "element.rpgmod.frost": "Мороз",
    "element.rpgmod.lightning": "Молния",
    "element.rpgmod.nature": "Природа",
    "element.rpgmod.fire.desc": "Поджигает цель.",
    "element.rpgmod.frost.desc": "Замедляет и обмораживает цель.",
    "element.rpgmod.lightning.desc": "Оглушает цель и бьёт разрядом.",
    "element.rpgmod.nature.desc": "Лечит союзников и дарует благословение природы.",

    # заклинания
    "spell.rpgmod.firebolt": "Огненная стрела",
    "spell.rpgmod.flame_nova": "Вспышка пламени",
    "spell.rpgmod.frost_shard": "Ледяной шип",
    "spell.rpgmod.frost_nova": "Ледяная волна",
    "spell.rpgmod.spark_bolt": "Искровой разряд",
    "spell.rpgmod.chain_lightning": "Цепная молния",
    "spell.rpgmod.healing_bloom": "Цветение лечения",
    "spell.rpgmod.natures_renewal": "Обновление природы",
    "spell.rpgmod.firebolt.desc": "Быстрый снаряд огня.",
    "spell.rpgmod.flame_nova.desc": "Волна огня вокруг кастующего.",
    "spell.rpgmod.frost_shard.desc": "Снаряд льда, замедляющий цель.",
    "spell.rpgmod.frost_nova.desc": "Круг холода, обмораживающий врагов вокруг.",
    "spell.rpgmod.spark_bolt.desc": "Мощный разряд молнии.",
    "spell.rpgmod.chain_lightning.desc": "Молния перескакивает между врагами.",
    "spell.rpgmod.healing_bloom.desc": "Лечит вас и союзников рядом.",
    "spell.rpgmod.natures_renewal.desc": "Снимает эффекты и сильно лечит союзников.",

    # классы персонажа (определяются надетым сетом брони)
    "class.rpgmod.none": "Без класса",
    "class.rpgmod.warrior": "Воин",
    "class.rpgmod.ranger": "Стрелок",
    "class.rpgmod.mage": "Маг",
    "class.rpgmod.druid": "Друид",
    "class.rpgmod.none.bonus": "Наденьте полный сет брони одного класса, чтобы получить классовые бонусы.",
    "class.rpgmod.warrior.bonus": "Полный сет: +4 брони, +15% урона в ближнем бою.",
    "class.rpgmod.ranger.bonus": "Полный сет: +20% скорости передвижения, +8% скорости атаки, +1 брони.",
    "class.rpgmod.mage.bonus": "Полный сет: +40 к максимуму маны.",
    "class.rpgmod.druid.bonus": "Полный сет: +15 маны, +2 брони, постоянная регенерация.",

    # типы деревень
    "village_type.rpgmod.warrior": "Военное поселение",
    "village_type.rpgmod.ranger": "Охотничье поселение",
    "village_type.rpgmod.mage": "Поселение магов",
    "village_type.rpgmod.druid": "Поселение друидов",

    # подсказки предметов
    "tooltip.rpgmod.quality": "Качество: %s",
    "tooltip.rpgmod.weapon_class": "Класс: %s",
    "tooltip.rpgmod.weapon_level": "Уровень оружия: %s",
    "tooltip.rpgmod.weapon_xp": "Опыт оружия: %s",
    "tooltip.rpgmod.damage": "Урон: %s",
    "tooltip.rpgmod.attack_speed": "Скорость атаки: %s",
    "tooltip.rpgmod.reach": "Дальность: %s",
    "tooltip.rpgmod.crit": "Шанс крита: %s",
    "tooltip.rpgmod.lifesteal": "Похищение жизни: %s",
    "tooltip.rpgmod.armor_bonus": "Броня: %s",
    "tooltip.rpgmod.mana_bonus": "Мана: %s",
    "tooltip.rpgmod.affixes": "Аффиксы:",
    "tooltip.rpgmod.affix": " %s %s",
    "tooltip.rpgmod.loot_only": "Можно найти только в сундуках и подземельях.",
    "tooltip.rpgmod.hone_hint": "Подковка: наковальня + материал (слиток, осколок...).",
    "tooltip.rpgmod.hone": "Заточка: %s",
    "tooltip.rpgmod.hone.none": "Без заточки",
    "tooltip.rpgmod.set_part": "Часть сета: %s",
    "tooltip.rpgmod.staff_element": "Стихия: %s",
    "tooltip.rpgmod.staff_spell": "Заклинание: %s",
    "tooltip.rpgmod.staff_mana": "Мана: %s",
    "tooltip.rpgmod.staff_cycle": "Shift + ПКМ — сменить заклинание",

    # сообщения
    "msg.rpgmod.weapon_level_up": "Оружие «%s» достигло уровня %s!",
    "msg.rpgmod.need_staff": "Для заклинаний нужен посох в руке",
    "msg.rpgmod.not_enough_mana": "Не хватает маны: нужно %s, есть %s",
    "msg.rpgmod.mana_left": "Мана: %s/%s",
    "msg.rpgmod.altar_needs_key": "Алтарю нужен ключ подземелья",
    "msg.rpgmod.village_info": "Деревня: %s | жителей: %s | очков роста: %s",
    "msg.rpgmod.spell_selected": "Заклинание: %s",
    "msg.rpgmod.level_up": "Уровень персонажа %s! Свободных очков: %s",
    "msg.rpgmod.class_set": "Активный класс: %s",
    "msg.rpgmod.too_strong": "Враг слишком силён (ур. %s против вашего %s)",
    "msg.rpgmod.no_stat_points": "Нет свободных очков характеристик",
    "msg.rpgmod.stat_spent": "Характеристика «%s» улучшена. Осталось очков: %s",
    "msg.rpgmod.cmd_level": "Уровень персонажа: %s",
    "msg.rpgmod.cmd_points": "Свободных очков: %s",
    "msg.rpgmod.cmd_stats": "Сила: %s, живучесть: %s, интеллект: %s",
    "msg.rpgmod.cmd_mana": "Мана: %s",
    "msg.rpgmod.cmd_class": "Класс: %s",

    # HUD
    "hud.rpgmod.mana": "Мана %s/%s",
    "hud.rpgmod.level": "Ур. %s",
    "hud.rpgmod.class": "Класс: %s",
    "hud.rpgmod.stat_points": "Очки характеристик: %s (/rpg spend)",

    # имена
    "name.rpgmod.weapon": "%s %s %s",
    "name.rpgmod.enemy_level": "Lv.%s %s",
}

EN: dict[str, str] = {
    "itemGroup.rpgmod.main": "RPG Overhaul",

    "item.rpgmod.steel_ingot": "Steel Ingot",
    "item.rpgmod.silver_ingot": "Silver Ingot",
    "item.rpgmod.dusk_ingot": "Dusk Ingot",
    "item.rpgmod.arcane_dust": "Arcane Dust",
    "item.rpgmod.frost_shard": "Frost Shard",
    "item.rpgmod.ember_ingot": "Ember Ingot",
    "item.rpgmod.void_shard": "Void Shard",
    "item.rpgmod.mana_crystal": "Mana Crystal",
    "item.rpgmod.dungeon_key": "Dungeon Key",
    "item.rpgmod.steel_ingot.desc": "Iron and coal alloy. The base for weapons and armor.",
    "item.rpgmod.silver_ingot.desc": "Silver. Good for weapon trim and jewelry.",
    "item.rpgmod.dusk_ingot.desc": "A rare alloy from the deep dungeons.",
    "item.rpgmod.arcane_dust.desc": "Dust scraped off rune bricks.",
    "item.rpgmod.frost_shard.desc": "A shard of eternal ice. Cold honing.",
    "item.rpgmod.ember_ingot.desc": "An ingot forged in a volcano's maw.",
    "item.rpgmod.void_shard.desc": "A shard of the void. Weapons made with it drain life.",
    "item.rpgmod.mana_crystal.desc": "A crystal of pure mana. Restores mana.",
    "item.rpgmod.dungeon_key.desc": "Opens the boss altar inside a dungeon.",

    "item.rpgmod.fire_staff": "Fire Staff",
    "item.rpgmod.frost_staff": "Frost Staff",
    "item.rpgmod.storm_staff": "Storm Staff",
    "item.rpgmod.nature_staff": "Nature Staff",

    "item.rpgmod.dungeon_warden_spawn_egg": "Dungeon Warden Spawn Egg",
    "item.rpgmod.cave_stalker_spawn_egg": "Cave Stalker Spawn Egg",

    "block.rpgmod.rune_bricks": "Rune Bricks",
    "block.rpgmod.cracked_rune_bricks": "Cracked Rune Bricks",
    "block.rpgmod.gilded_rune_bricks": "Gilded Rune Bricks",
    "block.rpgmod.frost_bricks": "Frost Bricks",
    "block.rpgmod.ember_bricks": "Ember Bricks",
    "block.rpgmod.soul_lamp": "Soul Lamp",
    "block.rpgmod.boss_altar": "Boss Altar",
    "block.rpgmod.village_hearth": "Village Hearth",

    "entity.rpgmod.spell_projectile": "Spell Projectile",
    "entity.rpgmod.dungeon_warden": "Dungeon Warden",
    "entity.rpgmod.cave_stalker": "Cave Stalker",

    "effect.rpgmod.bleeding": "Bleeding",
    "effect.rpgmod.frostbite": "Frostbite",
    "effect.rpgmod.shocked": "Shocked",
    "effect.rpgmod.nature_blessing": "Nature's Blessing",

    "weapon_class.rpgmod.dagger": "Dagger",
    "weapon_class.rpgmod.sword": "Sword",
    "weapon_class.rpgmod.greatsword": "Greatsword",
    "weapon_class.rpgmod.axe": "Axe",
    "weapon_class.rpgmod.spear": "Spear",
    "weapon_class.rpgmod.mace": "Mace",
    "weapon_class.rpgmod.dagger.desc": "Fast but weak. Great against single targets.",
    "weapon_class.rpgmod.sword.desc": "Universal weapon: a balance of damage and speed.",
    "weapon_class.rpgmod.greatsword.desc": "The heaviest hit, but a slow swing.",
    "weapon_class.rpgmod.axe.desc": "Heavy strike that cuts through armor.",
    "weapon_class.rpgmod.spear.desc": "Longest reach — keep enemies at a distance.",
    "weapon_class.rpgmod.mace.desc": "Crushing blow that stuns and knocks back.",

    "weapon_material.rpgmod.steel": "Steel",
    "weapon_material.rpgmod.silver": "Silver",
    "weapon_material.rpgmod.dusksteel": "Dusksteel",

    "weapon_quality.rpgmod.common": "Common",
    "weapon_quality.rpgmod.uncommon": "Uncommon",
    "weapon_quality.rpgmod.rare": "Rare",
    "weapon_quality.rpgmod.epic": "Epic",
    "weapon_quality.rpgmod.legendary": "Legendary",

    "weapon_affix.rpgmod.sharp": "Sharp",
    "weapon_affix.rpgmod.swift": "Swift",
    "weapon_affix.rpgmod.vampiric": "Vampiric",
    "weapon_affix.rpgmod.runic": "Runic",
    "weapon_affix.rpgmod.reaching": "Reaching",
    "weapon_affix.rpgmod.stalwart": "Stalwart",
    "weapon_affix.rpgmod.frostbound": "Frostbound",
    "weapon_affix.rpgmod.emberforged": "Emberforged",
    "weapon_affix.rpgmod.stormbound": "Stormbound",
    "weapon_affix.rpgmod.verdant": "Verdant",

    "hone.rpgmod.none": "No honing",
    "hone.rpgmod.none.desc": "A plain weapon with no honing.",
    "hone.rpgmod.iron": "Iron honing",
    "hone.rpgmod.iron.desc": "+10% damage, +8 durability.",
    "hone.rpgmod.gold": "Gold honing",
    "hone.rpgmod.gold.desc": "+25% weapon XP.",
    "hone.rpgmod.diamond": "Diamond honing",
    "hone.rpgmod.diamond.desc": "+15% damage, +20% durability.",
    "hone.rpgmod.netherite": "Netherite honing",
    "hone.rpgmod.netherite.desc": "+25% damage, +35 durability.",
    "hone.rpgmod.amethyst": "Amethyst honing",
    "hone.rpgmod.amethyst.desc": "+15% magic damage.",
    "hone.rpgmod.frost": "Frost honing",
    "hone.rpgmod.frost.desc": "Hits freeze and slow the enemy.",
    "hone.rpgmod.ember": "Ember honing",
    "hone.rpgmod.ember.desc": "Hits set the enemy on fire.",
    "hone.rpgmod.void": "Void honing",
    "hone.rpgmod.void.desc": "Hits weaken the enemy and steal health.",

    "stat.rpgmod.strength": "Strength",
    "stat.rpgmod.vitality": "Vitality",
    "stat.rpgmod.intellect": "Intellect",
    "stat.rpgmod.strength.desc": "Increases melee damage.",
    "stat.rpgmod.vitality.desc": "Increases maximum health.",
    "stat.rpgmod.intellect.desc": "Increases mana and spell power.",

    "element.rpgmod.fire": "Fire",
    "element.rpgmod.frost": "Frost",
    "element.rpgmod.lightning": "Lightning",
    "element.rpgmod.nature": "Nature",
    "element.rpgmod.fire.desc": "Sets the target on fire.",
    "element.rpgmod.frost.desc": "Slows and freezes the target.",
    "element.rpgmod.lightning.desc": "Shocks the target with lightning.",
    "element.rpgmod.nature.desc": "Heals allies and grants nature's blessing.",

    "spell.rpgmod.firebolt": "Firebolt",
    "spell.rpgmod.flame_nova": "Flame Nova",
    "spell.rpgmod.frost_shard": "Frost Shard",
    "spell.rpgmod.frost_nova": "Frost Nova",
    "spell.rpgmod.spark_bolt": "Spark Bolt",
    "spell.rpgmod.chain_lightning": "Chain Lightning",
    "spell.rpgmod.healing_bloom": "Healing Bloom",
    "spell.rpgmod.natures_renewal": "Nature's Renewal",
    "spell.rpgmod.firebolt.desc": "A fast projectile of fire.",
    "spell.rpgmod.flame_nova.desc": "A wave of fire around the caster.",
    "spell.rpgmod.frost_shard.desc": "An ice projectile that slows the target.",
    "spell.rpgmod.frost_nova.desc": "A ring of cold that freezes nearby enemies.",
    "spell.rpgmod.spark_bolt.desc": "A powerful lightning bolt.",
    "spell.rpgmod.chain_lightning.desc": "Lightning that jumps between enemies.",
    "spell.rpgmod.healing_bloom.desc": "Heals you and nearby allies.",
    "spell.rpgmod.natures_renewal.desc": "Cleanses effects and strongly heals allies.",

    "class.rpgmod.none": "No class",
    "class.rpgmod.warrior": "Warrior",
    "class.rpgmod.ranger": "Ranger",
    "class.rpgmod.mage": "Mage",
    "class.rpgmod.druid": "Druid",
    "class.rpgmod.none.bonus": "Wear a full set of one class's armor to gain class bonuses.",
    "class.rpgmod.warrior.bonus": "Full set: +4 armor, +15% melee damage.",
    "class.rpgmod.ranger.bonus": "Full set: +20% movement speed, +8% attack speed, +1 armor.",
    "class.rpgmod.mage.bonus": "Full set: +40 maximum mana.",
    "class.rpgmod.druid.bonus": "Full set: +15 mana, +2 armor, constant regeneration.",

    "village_type.rpgmod.warrior": "Warrior Village",
    "village_type.rpgmod.ranger": "Ranger Village",
    "village_type.rpgmod.mage": "Mage Village",
    "village_type.rpgmod.druid": "Druid Village",

    "tooltip.rpgmod.quality": "Quality: %s",
    "tooltip.rpgmod.weapon_class": "Class: %s",
    "tooltip.rpgmod.weapon_level": "Weapon level: %s",
    "tooltip.rpgmod.weapon_xp": "Weapon XP: %s",
    "tooltip.rpgmod.damage": "Damage: %s",
    "tooltip.rpgmod.attack_speed": "Attack speed: %s",
    "tooltip.rpgmod.reach": "Reach: %s",
    "tooltip.rpgmod.crit": "Crit chance: %s",
    "tooltip.rpgmod.lifesteal": "Lifesteal: %s",
    "tooltip.rpgmod.armor_bonus": "Armor: %s",
    "tooltip.rpgmod.mana_bonus": "Mana: %s",
    "tooltip.rpgmod.affixes": "Affixes:",
    "tooltip.rpgmod.affix": " %s %s",
    "tooltip.rpgmod.loot_only": "Can only be found in dungeon chests.",
    "tooltip.rpgmod.hone_hint": "Honing: anvil + material (ingot, shard, ...).",
    "tooltip.rpgmod.hone": "Honing: %s",
    "tooltip.rpgmod.hone.none": "No honing",
    "tooltip.rpgmod.set_part": "Set piece: %s",
    "tooltip.rpgmod.staff_element": "Element: %s",
    "tooltip.rpgmod.staff_spell": "Spell: %s",
    "tooltip.rpgmod.staff_mana": "Mana: %s",
    "tooltip.rpgmod.staff_cycle": "Shift + right click — switch spell",

    "msg.rpgmod.weapon_level_up": "Weapon \"%s\" reached level %s!",
    "msg.rpgmod.need_staff": "You need a staff to cast spells",
    "msg.rpgmod.not_enough_mana": "Not enough mana: need %s, have %s",
    "msg.rpgmod.mana_left": "Mana: %s/%s",
    "msg.rpgmod.altar_needs_key": "The altar needs a dungeon key",
    "msg.rpgmod.village_info": "Village: %s | population: %s | growth points: %s",
    "msg.rpgmod.spell_selected": "Spell: %s",
    "msg.rpgmod.level_up": "Character level %s! Free points: %s",
    "msg.rpgmod.class_set": "Active class: %s",
    "msg.rpgmod.too_strong": "Enemy is too strong (level %s vs your %s)",
    "msg.rpgmod.no_stat_points": "No free stat points",
    "msg.rpgmod.stat_spent": "Stat \"%s\" improved. Points left: %s",
    "msg.rpgmod.cmd_level": "Character level: %s",
    "msg.rpgmod.cmd_points": "Free points: %s",
    "msg.rpgmod.cmd_stats": "Strength: %s, vitality: %s, intellect: %s",
    "msg.rpgmod.cmd_mana": "Mana: %s",
    "msg.rpgmod.cmd_class": "Class: %s",

    "hud.rpgmod.mana": "Mana %s/%s",
    "hud.rpgmod.level": "Lv. %s",
    "hud.rpgmod.class": "Class: %s",
    "hud.rpgmod.stat_points": "Stat points: %s (/rpg spend)",

    "name.rpgmod.weapon": "%s %s %s",
    "name.rpgmod.enemy_level": "Lv.%s %s",
}


def build_lang() -> tuple[dict[str, str], dict[str, str]]:
    ru = dict(RU)
    en = dict(EN)

    # оружие
    for key, name in RU_WEAPON_NAMES.items():
        ru[f"item.rpgmod.{key}"] = name
    for key, name in EN_WEAPON_NAMES.items():
        en[f"item.rpgmod.{key}"] = name

    # броня
    for cls in ARMOR_CLASSES:
        for piece in ARMOR_PIECES:
            ru[f"item.rpgmod.{cls}_{piece}"] = f"{RU_ARMOR_PIECE[piece]} {RU_ARMOR_CLASS[cls]}"
            en[f"item.rpgmod.{cls}_{piece}"] = f"{EN_ARMOR_CLASS[cls]} {EN_ARMOR_PIECE[piece]}"
    return ru, en


def dump(path: Path, data: dict) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


# ------------------------------------------------------------------------ модели

def item_model(path: Path, parent: str, texture: str | None) -> None:
    data: dict = {"parent": parent}
    if texture:
        data["textures"] = {"layer0": texture}
    dump(path, data)


def generate_models() -> None:
    item_dir = PKG / "models/item"
    block_dir = PKG / "models/block"
    state_dir = PKG / "blockstates"

    for name in MATERIALS + ARMOR:
        item_model(item_dir / f"{name}.json", "minecraft:item/generated", f"rpgmod:item/{name}")

    for name in WEAPONS + STAVES:
        item_model(item_dir / f"{name}.json", "minecraft:item/handheld", f"rpgmod:item/{name}")

    for name in SPAWN_EGGS:
        item_model(item_dir / f"{name}.json", "minecraft:item/template_spawn_egg", None)

    for name in BLOCKS:
        # модель блока — обычный куб с одной текстурой
        dump(block_dir / f"{name}.json", {
            "parent": "minecraft:block/cube_all",
            "textures": {"all": f"rpgmod:block/{name}"},
        })
        # предмет-блок ссылается на модель блока
        dump(item_dir / f"{name}.json", {"parent": f"rpgmod:block/{name}"})
        # blockstate: один вариант без поворотов
        dump(state_dir / f"{name}.json", {"variants": {"": {"model": f"rpgmod:block/{name}"}}})


def cleanup_old_assets() -> None:
    old = ASSETS / "examplemod"
    if old.exists():
        shutil.rmtree(old)
        print(f"удалено старое дерево ассетов: {old.relative_to(ROOT)}")


def main() -> None:
    cleanup_old_assets()
    ru, en = build_lang()
    dump(PKG / "lang/ru_ru.json", ru)
    dump(PKG / "lang/en_us.json", en)
    generate_models()
    print(f"ключей в ru_ru.json: {len(ru)}, в en_us.json: {len(en)}")
    print(f"моделей предметов: {len(list((PKG / 'models/item').glob('*.json')))}")
    print(f"моделей блоков: {len(list((PKG / 'models/block').glob('*.json')))}")
    print(f"blockstate: {len(list((PKG / 'blockstates').glob('*.json')))}")


if __name__ == "__main__":
    main()
